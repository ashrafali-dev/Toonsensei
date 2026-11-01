package com.ashraf.novelstudio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.LruCache
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class HomeRecent(val title: String, val url: String, val site: String, val timeMs: Long)
data class HomeSite(val name: String, val url: String)

// ============================================================================ favicons

/**
 * Website icons for the home screen. Memory cache + files/favicons on disk, fetched off the main thread.
 * Tries Google's icon service first (sharp 128px icons for almost every site), then the site's own /favicon.ico.
 */
object Favicons {
    private val mem = LruCache<String, Bitmap>(80)
    private val pool = Executors.newFixedThreadPool(3)
    private val ui = Handler(Looper.getMainLooper())
    private val failed = HashSet<String>()

    fun load(app: Context, host: String, cb: (Bitmap?) -> Unit) {
        mem.get(host)?.let { cb(it); return }
        if (host in failed) { cb(null); return }
        pool.execute {
            val f = file(app, host)
            if (f.exists() && f.length() > 0) {
                val bmp = BitmapFactory.decodeFile(f.absolutePath)
                if (bmp != null) {
                    mem.put(host, bmp)
                    ui.post { cb(bmp) }
                    return@execute
                }
            }
            val bmp = fetch(app, host)
            if (bmp != null) { mem.put(host, bmp); ui.post { cb(bmp) } }
            else { failed.add(host); ui.post { cb(null) } }
        }
    }

    private fun file(app: Context, host: String): File =
        File(File(app.filesDir, "favicons").apply { mkdirs() }, host.replace(Regex("[^A-Za-z0-9.-]"), "_") + ".png")

    private fun fetch(app: Context, host: String): Bitmap? {
        val candidates = listOf(
            "https://www.google.com/s2/favicons?domain=$host&sz=128",
            "https://$host/favicon.ico"
        )
        for (u in candidates) {
            try {
                val c = URL(u).openConnection() as HttpURLConnection
                c.connectTimeout = 5000
                c.readTimeout = 5000
                c.instanceFollowRedirects = true
                c.setRequestProperty("User-Agent", "Mozilla/5.0")
                if (c.responseCode != 200) { c.disconnect(); continue }
                val key = host
                val bytes = c.inputStream.use { it.readBytes() }
                c.disconnect()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: continue
                if (bmp.width < 8) continue
                try {
                    file(app, key).outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                } catch (_: Exception) {}
                return bmp
            } catch (_: Exception) {}
        }
        return null
    }
}

// ============================================================================ home screen

/**
 * Full-screen home (ui spec board 2): mascot + wordmark, one search pill, the mode chips
 * (Novel, Scanning, Real-time, Manual Lite, Link → Translate with the Soon badge), a one-line
 * caption and the home dock (Library, Menu). Picking a mode opens its start page (board 21):
 * short guidance, the search pill again, recently read and the supported sites of that mode.
 * It swallows touches so the browser below stays idle.
 */
class HomeView(private val ctx: Context) : FrameLayout(ctx) {
    var onOpen: (String) -> Unit = {}
    var onSearch: (String) -> Unit = {}
    var onMenu: () -> Unit = {}
    var onLibrary: () -> Unit = {}
    var onMode: (Int) -> Unit = {}
    var onLinkTranslate: () -> Unit = {}
    var onHub: (Hub.Part) -> Unit = {}
    var onBackToHome: () -> Unit = {}

    private val mp = ViewGroup.LayoutParams.MATCH_PARENT
    private val wc = ViewGroup.LayoutParams.WRAP_CONTENT
    private val bold: Typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    private val medium: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val midBold: Typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    private val serif: Typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    private val scroll = ScrollView(ctx)
    private val content = LinearLayout(ctx)
    private val input = EditText(ctx)
    private val searchIcon = ImageView(ctx)
    private var iconIsUrl = false
    private var token = 0
    private var riseIndex = 0
    // Active mode chip (0 Novel, 1 Scanning, 2 Real-time, 3 Manual Lite) and the mode-start page state.
    private var activeModeIdx = 0
    private var modeStartIdx = -1
    private var lastRecents = emptyList<HomeRecent>()
    private var lastNovel = emptyList<HomeSite>()
    private var lastComic = emptyList<HomeSite>()
    private val searchPill: View by lazy { buildSearchBar() }

    /** Mode chips of the home board: (icon, label, one-line caption, comics mode). */
    private data class ModeDef(val idx: Int, val ic: Ic, val title: String, val caption: String, val comic: Boolean, val startCaption: String)
    private val modeDefs = listOf(
        ModeDef(0, Ic.BOOK, "Novel", "Reads the chapter text and translates it on the page.", false,
            "Open a novel site. Chapter text is translated on the page."),
        ModeDef(1, Ic.SCAN, "Scanning", "Scans the whole comic chapter first, so names and events stay consistent.", true,
            "Open a comic site. The whole chapter is prepared, then translated."),
        ModeDef(2, Ic.CROSSHAIR, "Real-time", "Translates what is on the screen as you scroll.", true,
            "Open a comic site. Panels are translated as you scroll."),
        ModeDef(3, Ic.CURSOR, "Manual Lite", "Reads one screen when you tap. Light on battery and RAM.", true,
            "Open a comic site, then tap a page to read just that screen.")
    )

    private val palette = intArrayOf(
        0xFFA9B18F.toInt(), 0xFF6E8B75.toInt(), 0xFFC08A3E.toInt(), 0xFF8F7360.toInt(),
        0xFF7E8C9E.toInt(), 0xFF9C7E8A.toInt(), 0xFF748C6A.toInt(), 0xFF8A7E60.toInt()
    )

    init {
        isClickable = true
        content.orientation = LinearLayout.VERTICAL
        scroll.isVerticalScrollBarEnabled = false
        scroll.overScrollMode = OVER_SCROLL_NEVER
        scroll.clipToPadding = false
        scroll.setPadding(0, 0, 0, ctx.dpi(84))
        scroll.addView(content, ViewGroup.LayoutParams(mp, wc))
        addView(scroll, FrameLayout.LayoutParams(mp, mp))
        addView(homeDock(), FrameLayout.LayoutParams(mp, wc).also { it.gravity = Gravity.BOTTOM })
    }

    // ---------------------------------------------------------------- search bar

    private fun looksLikeUrl(s: String): Boolean {
        val t = s.trim()
        return t.startsWith("http") || (!t.contains(' ') && t.contains('.'))
    }

    private fun refreshIcon(text: String) {
        val url = looksLikeUrl(text)
        if (url == iconIsUrl) return
        iconIsUrl = url
        searchIcon.setImageDrawable(IconDrawable(if (url) Ic.GLOBE else Ic.SEARCH, UiTheme.faint))
    }

    private fun submit() {
        val t = input.text.toString().trim()
        if (t.isEmpty()) return
        hideKeyboard()
        input.setText("")
        onSearch(t)
    }

    fun hideKeyboard() {
        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
        input.clearFocus()
    }

    private fun buildSearchBar(): View {
        searchIcon.setImageDrawable(IconDrawable(Ic.SEARCH, UiTheme.faint))
        input.apply {
            hint = "Search or paste a link"
            setHintTextColor(UiTheme.faint)
            setTextColor(UiTheme.text)
            textSize = 14f
            background = null
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(0, 0, 0, 0)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) { submit(); true } else false
            }
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { refreshIcon(s?.toString().orEmpty()) }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ctx.dpi(20), 0, ctx.dpi(20), 0)
            background = Glass.solid(ctx, UiTheme.surface1, 27, UiTheme.stroke)
            isClickable = true
            contentDescription = "Search or paste a link"
            setOnClickListener {
                input.requestFocus()
                val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
            addView(searchIcon, LinearLayout.LayoutParams(ctx.dpi(20), ctx.dpi(20)).also { it.marginEnd = ctx.dpi(12) })
            addView(input, LinearLayout.LayoutParams(0, wc, 1f))
        }
    }

    // ---------------------------------------------------------------- helpers

    private fun colorFor(s: String): Int = palette[Math.abs(s.hashCode() % palette.size)]

    private fun hostOf(url: String): String? = try {
        Uri.parse(url).host?.removePrefix("www.")
    } catch (_: Exception) {
        null
    }

    private fun ago(ms: Long): String {
        if (ms <= 0L) return ""
        val m = (System.currentTimeMillis() - ms) / 60000L
        return when {
            m < 1 -> "just now"
            m < 60 -> "$m min ago"
            m < 24 * 60 -> "${m / 60} h ago"
            m < 48 * 60 -> "Yesterday"
            else -> "${m / (24 * 60)} days ago"
        }
    }

    private fun <T : View> rise(v: T): T {
        v.alpha = 0f
        v.translationY = ctx.dpf(12)
        v.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(riseIndex++ * 55L)
            .setDuration(380L)
            .setInterpolator(DecelerateInterpolator())
            .start()
        return v
    }

    private fun pressable(v: View, onClick: () -> Unit) {
        v.isClickable = true
        v.setOnClickListener { onClick() }
        v.setOnTouchListener { view, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90L).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
            }
            false
        }
    }

    private fun label(t: String): TextView = TextView(ctx).apply {
        text = t
        textSize = 13f
        setTextColor(UiTheme.faint)
        setPadding(0, ctx.dpi(18), 0, ctx.dpi(10))
    }

    /** Website icon (fetched favicon on a light tile); until it arrives, a coloured tile with the site's initial. */
    private fun siteIcon(site: HomeSite, sizeDp: Int, radiusDp: Int, textSp: Float): View {
        val size = ctx.dpi(sizeDp)
        val frame = FrameLayout(ctx)
        val base = colorFor(site.name)
        frame.background = GradientDrawable().apply {
            setColor(base)
            cornerRadius = ctx.dpf(radiusDp)
        }
        val letter = TextView(ctx).apply {
            text = site.name.take(1).uppercase()
            setTextColor(Color.WHITE)
            textSize = textSp
            typeface = midBold
            gravity = Gravity.CENTER
        }
        val img = ImageView(ctx).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            visibility = View.GONE
            val p = (size * 0.2f).toInt()
            setPadding(p, p, p, p)
        }
        frame.addView(letter, FrameLayout.LayoutParams(mp, mp))
        frame.addView(img, FrameLayout.LayoutParams(mp, mp))
        val mine = token
        val host = hostOf(site.url)
        if (host != null) {
            Favicons.load(ctx, host) { bmp ->
                if (bmp != null && mine == token) {
                    frame.background = GradientDrawable().apply {
                        setColor(0xFFF4F6FB.toInt())
                        cornerRadius = ctx.dpf(radiusDp)
                    }
                    img.setImageBitmap(bmp)
                    img.visibility = View.VISIBLE
                    letter.visibility = View.GONE
                }
            }
        }
        return frame
    }

    /** Flat letter cover for a recent read (spec board 21: 40x54, radius 6). */
    private fun cover(title: String, wDp: Int, hDp: Int, radiusDp: Int, textSp: Float): View {
        val base = colorFor(title)
        return TextView(ctx).apply {
            text = title.trim().take(1).uppercase()
            textSize = textSp
            typeface = serif
            setTextColor(UiTheme.bg)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(base)
                cornerRadius = ctx.dpf(radiusDp)
            }
            layoutParams = LinearLayout.LayoutParams(ctx.dpi(wDp), ctx.dpi(hDp))
        }
    }

    private fun meta(r: HomeRecent): String {
        val t = ago(r.timeMs)
        return if (t.isEmpty()) r.site else "${r.site} \u00B7 $t"
    }

    // ---------------------------------------------------------------- wordmark header (board 2)

    private fun wordmark(): View {
        // "Toon Sensei" in a serif face; the Shippori Mincho asset is a proposal, so serif bold stands in.
        return TextView(ctx).apply {
            text = "Toon Sensei"
            textSize = 26f
            typeface = serif
            setTextColor(UiTheme.text)
            letterSpacing = 0.02f
        }
    }

    private fun mascotHeader(bigMascot: Boolean): View = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(ctx.dpi(22), ctx.dpi(44), ctx.dpi(22), 0)
        addView(MascotView(ctx), LinearLayout.LayoutParams(ctx.dpi(if (bigMascot) 140 else 84), ctx.dpi(if (bigMascot) 140 else 84)))
        addView(wordmark(), LinearLayout.LayoutParams(wc, wc).also {
            it.topMargin = ctx.dpi(6); it.bottomMargin = ctx.dpi(16)
        })
    }

    // ---------------------------------------------------------------- mode chips (board 2)

    /** Horizontal-wrap container (chip rows wrap like the board's flexbox). */
    private inner class FlowRow(context: Context) : ViewGroup(context) {
        var hGap = ctx.dpi(8)
        var vGap = ctx.dpi(8)
        var center = false

        override fun onMeasure(wSpec: Int, hSpec: Int) {
            val specW = MeasureSpec.getSize(wSpec)
            var x = 0
            var y = 0
            var rowH = 0
            for (i in 0 until childCount) {
                val ch = getChildAt(i)
                if (ch.visibility == View.GONE) continue
                measureChild(ch, wSpec, hSpec)
                val cw = ch.measuredWidth
                val chh = ch.measuredHeight
                if (x > 0 && x + cw > specW) {
                    x = 0
                    y += rowH + vGap
                    rowH = 0
                }
                x += cw + hGap
                rowH = maxOf(rowH, chh)
            }
            setMeasuredDimension(specW, y + rowH)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            val width = r - l
            var x = 0
            var y = 0
            var rowH = 0
            var rowStart = 0
            var rowW = 0
            fun layoutRow(rowEnd: Int, w: Int) {
                var cx = if (center) ((width - w) / 2).coerceAtLeast(0) else 0
                for (i in rowStart until rowEnd) {
                    val ch = getChildAt(i)
                    if (ch.visibility == View.GONE) continue
                    ch.layout(cx, y, cx + ch.measuredWidth, y + ch.measuredHeight)
                    cx += ch.measuredWidth + hGap
                }
                y += rowH + vGap
            }
            var i = 0
            while (i < childCount) {
                val ch = getChildAt(i)
                if (ch.visibility == View.GONE) { i++; continue }
                val cw = ch.measuredWidth
                if (x > 0 && x + cw > width) {
                    layoutRow(i, rowW)
                    x = 0; rowH = 0; rowStart = i; rowW = 0
                }
                rowW += if (x > 0) cw + hGap else cw
                x += cw + hGap
                rowH = maxOf(rowH, ch.measuredHeight)
                i++
            }
            layoutRow(i, rowW)
        }
    }

    private fun modeChip(def: ModeDef?, title: String, ic: Ic, active: Boolean, badge: String? = null, onTap: () -> Unit): View {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ctx.dpi(16), 0, ctx.dpi(16), 0)
            background = if (active)
                Glass.solid(ctx, UiTheme.text, 22, UiTheme.text, 1)
            else
                Glass.ripple(ctx, Glass.solid(ctx, UiTheme.surface1, 22, UiTheme.stroke, 1), 22)
            contentDescription = title
        }
        val iconColor = if (active) UiTheme.bg else UiTheme.text
        box.addView(ImageView(ctx).apply { setImageDrawable(IconDrawable(ic, iconColor)) }, LinearLayout.LayoutParams(ctx.dpi(18), ctx.dpi(18)).also { it.marginEnd = ctx.dpi(8) })
        box.addView(TextView(ctx).apply {
            text = title
            textSize = 14f
            setTextColor(if (active) UiTheme.bg else UiTheme.text)
        })
        if (badge != null) {
            box.addView(TextView(ctx).apply {
                text = badge
                textSize = 11f
                setTextColor(UiTheme.faint)
                setPadding(ctx.dpi(7), ctx.dpi(2), ctx.dpi(7), ctx.dpi(2))
                background = Glass.solid(ctx, UiTheme.surface2, 8)
            }, LinearLayout.LayoutParams(wc, wc).also { it.marginStart = ctx.dpi(8) })
        }
        box.setOnClickListener { onTap() }
        box.layoutParams = ViewGroup.LayoutParams(wc, ctx.dpi(44))
        return box
    }

    private fun chipsRow(): View {
        val flow = FlowRow(ctx).apply { center = true }
        // gap 8 comes from FlowRow defaults
        modeDefs.forEach { def ->
            flow.addView(modeChip(def, def.title, def.ic, activeModeIdx == def.idx) {
                onMode(def.idx)
            })
        }
        flow.addView(modeChip(null, "Link \u2192 Translate", Ic.LINK, false, badge = "Soon") {
            onLinkTranslate()
        })
        return flow
    }

    private fun caption(t: String, padTop: Int = 12): View = TextView(ctx).apply {
        text = t
        textSize = 13f
        setTextColor(UiTheme.faint)
        gravity = Gravity.CENTER
        setLineSpacing(0f, 1.16f)
        setPadding(ctx.dpi(22), ctx.dpi(padTop), ctx.dpi(22), 0)
    }

    // ---------------------------------------------------------------- home dock (board 2)

    private fun dockItem(ic: Ic, label: String, onTap: () -> Unit): View {
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, ctx.dpi(3), 0, ctx.dpi(3))
            contentDescription = label
        }
        col.addView(ImageView(ctx).apply { setImageDrawable(IconDrawable(ic, UiTheme.faint)) }, LinearLayout.LayoutParams(ctx.dpi(22), ctx.dpi(22)))
        col.addView(TextView(ctx).apply {
            text = label
            textSize = 11f
            setTextColor(UiTheme.faint)
        }, LinearLayout.LayoutParams(wc, wc).also { it.topMargin = ctx.dpi(3) })
        col.setOnClickListener { onTap() }
        col.layoutParams = LinearLayout.LayoutParams(ctx.dpi(64), ctx.dpi(52))
        return col
    }

    private fun homeDock(): View = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(ctx.dpi(20), ctx.dpi(6), ctx.dpi(20), ctx.dpi(16))
        background = UiTheme.bg
        addView(dockItem(Ic.LIBRARY, "Library") { onLibrary() })
        addView(View(ctx), LinearLayout.LayoutParams(0, 1, 1f))
        addView(dockItem(Ic.MENU, "Menu") { onMenu() })
    }.let { wrap ->
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(View(ctx).apply { setBackgroundColor(UiTheme.stroke) }, LinearLayout.LayoutParams(mp, maxOf(1, (resources.displayMetrics.density * 0.5f).toInt())))
            addView(wrap)
        }
    }

    // ---------------------------------------------------------------- recent rows (board 21)

    private fun recentRow(r: HomeRecent): View {
        val title = TextView(ctx).apply {
            text = r.title
            textSize = 15f
            typeface = medium
            setTextColor(UiTheme.text)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        val sub = TextView(ctx).apply {
            text = meta(r)
            textSize = 13f
            setTextColor(UiTheme.faint)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        val texts = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            addView(title)
            addView(sub)
        }
        val chev = ImageView(ctx).apply { setImageDrawable(IconDrawable(Ic.CHEVRON, UiTheme.faint)) }
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, ctx.dpi(10), 0, ctx.dpi(10))
            addView(cover(r.title, 40, 54, 6, 18f), LinearLayout.LayoutParams(ctx.dpi(40), ctx.dpi(54)).also { it.marginEnd = ctx.dpi(14) })
            addView(texts, LinearLayout.LayoutParams(0, wc, 1f))
            addView(chev, LinearLayout.LayoutParams(ctx.dpi(16), ctx.dpi(16)))
        }
        pressable(row) { onOpen(r.url) }
        return row
    }

    private fun supportedChips(sites: List<HomeSite>): View {
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        sites.forEach { s ->
            val c = TextView(ctx).apply {
                text = s.name
                textSize = 14f
                setTextColor(UiTheme.text)
                gravity = Gravity.CENTER
                setPadding(ctx.dpi(18), 0, ctx.dpi(18), 0)
                background = Glass.ripple(ctx, Glass.solid(ctx, UiTheme.surface1, 22, UiTheme.stroke, 1), 22)
                contentDescription = s.name
            }
            pressable(c) { onOpen(s.url) }
            row.addView(c, LinearLayout.LayoutParams(wc, ctx.dpi(44)).also { it.marginEnd = ctx.dpi(10) })
        }
        return HorizontalScrollView(ctx).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = OVER_SCROLL_NEVER
            addView(row)
        }
    }

    // ---------------------------------------------------------------- mode start page (board 21)

    private fun modeStartPage(def: ModeDef): View = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        // top bar: back + mode name
        addView(LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(ctx.dpi(6), ctx.dpi(6), ctx.dpi(6), 0)
            addView(ctx.iconButton(Ic.BACK, "Back", 24, 44, UiTheme.text) { modeStartIdx = -1; onBackToHome() }, LinearLayout.LayoutParams(ctx.dpi(44), ctx.dpi(44)))
            addView(TextView(ctx).apply {
                text = def.title
                textSize = 17f
                typeface = medium
                setTextColor(UiTheme.text)
            }, LinearLayout.LayoutParams(0, wc, 1f).also { it.marginStart = ctx.dpi(4) })
        }, LinearLayout.LayoutParams(mp, ctx.dpi(56)))
        addView(LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dpi(20), ctx.dpi(4), ctx.dpi(20), 0)
            addView(TextView(ctx).apply {
                text = def.startCaption
                textSize = 14f
                setTextColor(UiTheme.faint)
                setLineSpacing(0f, 1.2f)
            })
            addView(reparent(searchPill), LinearLayout.LayoutParams(mp, ctx.dpi(54)).also { it.topMargin = ctx.dpi(16) })
            if (lastRecents.isNotEmpty()) {
                addView(label("Recently read"))
                lastRecents.take(4).forEachIndexed { i, r ->
                    addView(recentRow(r))
                    if (i < minOf(4, lastRecents.size) - 1) {
                        addView(View(ctx).apply { setBackgroundColor(UiTheme.strokeSoft) }, LinearLayout.LayoutParams(mp, maxOf(1, (resources.displayMetrics.density * 0.5f).toInt())))
                    }
                }
            }
            val sites = if (def.comic) lastComic else lastNovel
            if (sites.isNotEmpty()) {
                addView(label("Supported sites"))
                addView(supportedChips(sites))
            }
        }, LinearLayout.LayoutParams(mp, wc))
    }

    private fun reparent(v: View): View {
        (v.parent as? ViewGroup)?.removeView(v)
        return v
    }

    // ---------------------------------------------------------------- public

    fun setActiveMode(idx: Int) { activeModeIdx = idx }

    /** Shows the per-mode start page (board 21); -1 returns to the home board. */
    fun showModeStart(idx: Int) {
        modeStartIdx = idx
        render(lastRecents, lastNovel, lastComic)
    }

    fun showHomePage() {
        modeStartIdx = -1
        render(lastRecents, lastNovel, lastComic)
    }

    fun render(recents: List<HomeRecent>, novelSites: List<HomeSite>, comicSites: List<HomeSite>) {
        lastRecents = recents
        lastNovel = novelSites
        lastComic = comicSites
        token++
        riseIndex = 0
        content.removeAllViews()

        // mode-start page (board 21) replaces the home content
        if (modeStartIdx in modeDefs.indices) {
            content.addView(rise(modeStartPage(modeDefs[modeStartIdx])), LinearLayout.LayoutParams(mp, wc))
            scroll.scrollTo(0, 0)
            return
        }

        // home (board 2): mascot, wordmark, search pill, mode chips, caption, optional updates strip
        content.addView(rise(mascotHeader(bigMascot = false)), LinearLayout.LayoutParams(mp, wc))
        content.addView(
            rise(reparent(searchPill)),
            LinearLayout.LayoutParams(mp, ctx.dpi(54)).also {
                it.setMargins(ctx.dpi(22), 0, ctx.dpi(22), 0)
            }
        )
        content.addView(
            rise(chipsRow()),
            LinearLayout.LayoutParams(mp, wc).also { it.topMargin = ctx.dpi(16); it.marginStart = ctx.dpi(22); it.marginEnd = ctx.dpi(22) }
        )
        val def = modeDefs.getOrNull(activeModeIdx)
        content.addView(rise(caption(def?.caption
            ?: "Paste a chapter link, pick a mode, and read in your own language.")), LinearLayout.LayoutParams(mp, wc))
        // quiet note while a fresh install has no history yet
        if (recents.isEmpty()) {
            content.addView(rise(TextView(ctx).apply {
                text = "Tip: on a novel or comic site, the chapter is translated right on the page \u2014 no copy-paste."
                textSize = 13f
                setTextColor(UiTheme.faint)
                setLineSpacing(0f, 1.2f)
                gravity = Gravity.CENTER
                setPadding(ctx.dpi(32), ctx.dpi(22), ctx.dpi(32), 0)
            }), LinearLayout.LayoutParams(mp, wc))
        }
        content.addView(View(ctx), LinearLayout.LayoutParams(1, ctx.dpi(14)))
        scroll.scrollTo(0, 0)
    }
}

package com.ashraf.novelstudio

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

class ReaderActivity : Activity() {
    private var list: List<Tr> = emptyList()
    private var idx = 0
    private var fs = 18f
    private var theme = 0
    private lateinit var root: FrameLayout
    private lateinit var header: LinearLayout
    private lateinit var titleTv: TextView
    private lateinit var countTv: TextView
    private lateinit var body: TextView
    private lateinit var scroll: ScrollView
    private lateinit var capsule: LinearLayout
    private lateinit var themeBtn: android.widget.ImageView
    private lateinit var readBar: ProgressBar

    // Reader themes (draft B05): 0 paper (day), 1 sepia, 2 night (charcoal), 3 white. The user's
    // saved preference stays on the old 0/1/2 scale, so 3 is only reachable from the theme cycle.
    // themes: 0 paper, 1 sepia, 2 night, 3 white
    private val bg = intArrayOf(0xFFF7F4EE.toInt(), 0xFFF4ECD8.toInt(), 0xFF121211.toInt(), 0xFFFFFFFF.toInt())
    private val fg = intArrayOf(0xFF3D3832.toInt(), 0xFF3B2F1E.toInt(), 0xFFE8E3D9.toInt(), 0xFF111111.toInt())
    private val dim = intArrayOf(0xFF6B665E.toInt(), 0xFF8A7B5C.toInt(), 0xFF9B9488.toInt(), 0xFF777777.toInt())
    private val card = intArrayOf(0xFFFBFAF6.toInt(), 0x33FFFFFF, 0xFF1B1B1A.toInt(), 0x11000000)
    private val pill = intArrayOf(0xFFFBFAF6.toInt(), 0xE63B2F1E.toInt(), 0xFF1B1B1A.toInt(), 0xE6111111.toInt())
    private val accent = UiTheme.accent
    private var novel = ""

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun pillBg(color: Int, radiusDp: Int): GradientDrawable = GradientDrawable().apply {
        cornerRadius = dp(radiusDp).toFloat()
        setColor(color)
    }

    private fun btn(label: String, desc: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        textSize = 15f
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        gravity = Gravity.CENTER
        contentDescription = desc
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        isClickable = true
        setOnClickListener { onClick() }
    }

    /** Icon button on the reader capsule (48 dp, stroke icon from the Paper & Seal set). */
    private fun iconBtn(ic: ToonIc, desc: String, onClick: () -> Unit): android.widget.ImageView = android.widget.ImageView(this).apply {
        setImageDrawable(ToonIconDrawable(ic, ToonTheme.colors(this@ReaderActivity).title))
        val p = dp(13)
        setPadding(p, p, p, p)
        contentDescription = desc
        layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        isClickable = true
        setOnClickListener { onClick() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        novel = intent.getStringExtra("novel") ?: ""
        val id = intent.getLongExtra("id", -1L)
        list = Store.chapters(this, novel)
        if (list.isEmpty()) { finish(); return }
        idx = list.indexOfFirst { it.id == id }.coerceAtLeast(0)
        fs = Prefs.get(this, "fs", "18").toFloatOrNull() ?: 18f
        theme = Prefs.get(this, "rtheme", "0").toIntOrNull() ?: 0

        // header: chapter title + counter chip
        titleTv = TextView(this).apply {
            textSize = 14f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(dp(14), dp(10), dp(14), dp(0))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        countTv = TextView(this).apply {
            textSize = 11.5f
            setPadding(dp(10), dp(5), dp(10), dp(5))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).also { it.setMargins(dp(10), dp(8), dp(10), 0) }
        }
        header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(titleTv)
            addView(countTv)
        }

        body = TextView(this).apply {
            setPadding(dp(20), dp(14), dp(20), dp(110))
            setLineSpacing(0f, 1.45f)
            setTextIsSelectable(true)
        }
        Prefs.fontFile(this).takeIf { it.isNotEmpty() }?.let {
            try {
                body.typeface = if (it.startsWith("dl_")) Typeface.createFromFile(LangFonts.file(this, it.removePrefix("dl_")))
                else Typeface.createFromAsset(assets, "fonts/$it.ttf")
            } catch (e: Exception) { }
        }

        readBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = android.content.res.ColorStateList.valueOf(if (theme == 2) UiTheme.palette.accent else ToonTheme.day.accent)
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(if (theme == 2) 0x33FFFFFF else 0x22000000)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.TOP
            )
        }

        scroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            addView(body)
        }

        // floating control capsule: prev, font, theme, next
        capsule = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            elevation = dp(12).toFloat()
            setPadding(dp(6), 0, dp(6), 0)
            addView(iconBtn(ToonIc.BACK, "Previous chapter") { go(-1) })
            addView(btn("A−", "Smaller text") { setFont(fs - 1f) })
            addView(btn("A+", "Larger text") { setFont(fs + 1f) })
            themeBtn = iconBtn(ToonIc.MOON, "Reading theme") {
                theme = (theme + 1) % 4
                Prefs.put(this@ReaderActivity, "rtheme", theme.toString())
                applyTheme()
            }
            addView(themeBtn)
            addView(iconBtn(ToonIc.FORWARD, "Next chapter") { go(1) })
        }

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }

        root = FrameLayout(this).apply {
            addView(inner, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(readBar)
            addView(capsule, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).also { it.setMargins(0, 0, 0, dp(18)) })
        }
        setContentView(root)
        applyTheme()
        show()
    }

    private fun setFont(v: Float) {
        fs = v.coerceIn(12f, 34f)
        Prefs.put(this, "fs", fs.toString())
        body.textSize = fs
    }

    private fun applyTheme() {
        root.setBackgroundColor(bg[theme])
        body.setTextColor(fg[theme])
        titleTv.setTextColor(fg[theme])
        header.setBackgroundColor(card[theme])
        countTv.setTextColor(if (theme == 2) accent else accent)
        countTv.background = pillBg(if (theme == 2) 0xFF242322.toInt() else 0x22000000, 14)
        capsule.background = pillBg(pill[theme], 25)
        for (i in 0 until capsule.childCount) {
            when (val v = capsule.getChildAt(i)) {
                is TextView -> v.setTextColor(if (theme == 2) 0xFFE8E3D9.toInt() else fg[theme])
                is android.widget.ImageView -> v.setImageDrawable(
                    ToonIconDrawable(if (v === themeBtn) ToonIc.MOON else if (capsule.indexOfChild(v) == 0) ToonIc.BACK else ToonIc.FORWARD,
                        if (theme == 2) 0xFFE8E3D9.toInt() else fg[theme])
                )
            }
        }
        body.textSize = fs
    }

    private fun go(d: Int) {
        val n = idx + d
        if (n in list.indices) { idx = n; show() }
    }

    private fun show() {
        val t = list[idx]
        titleTv.text = "$novel  •  ${t.label()}"
        countTv.text = "${idx + 1}/${list.size}"
        readBar.progress = (((idx + 1).toFloat() / list.size) * 100).toInt()
        body.text = Store.read(this, t.id)
        scroll.post { scroll.scrollTo(0, 0) }
        Prefs.put(this, "last_$novel", t.id.toString())
    }
}

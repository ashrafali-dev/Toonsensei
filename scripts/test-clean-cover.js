// Runs the REAL bubble-renderer JS from WebModeWriter.kt under a minimal fake DOM and asserts that a bubble whose
// lettering the local cleaner already erased (it.cl) no longer gets a background cover rectangle painted with it.bg.
//
// This is the check the Kotlin unit tests cannot do: they only cover pure maths. Here the actual page JS executes.
const fs = require('fs');
const path = require('path');

const SRC = process.env.WRITER || path.join(__dirname, '../app/src/main/java/com/ashraf/novelstudio/WebModeWriter.kt');
const kt = fs.readFileSync(SRC, 'utf8');

// The renderer is two Kotlin triple-quoted raw strings: the page script (PART_1) and the bubble renderer (PART_2).
function rawString(name) {
  const head = 'const val ' + name + ' = """';
  const start = kt.indexOf(head);
  if (start < 0) throw new Error('raw string not found: ' + name);
  const after = kt.slice(start + head.length);
  const end = after.indexOf('"""');
  if (end < 0) throw new Error('unterminated raw string: ' + name);
  return after.slice(0, end);
}

function extractJs() {
  return rawString('BUBBLES_PART_1') + '\n' + rawString('BUBBLES_PART_2');
}

// ---- minimal fake DOM ---------------------------------------------------------------------------
class El {
  constructor(tag) {
    this.tagName = tag;
    this.children = [];
    this.style = {
      cssText: '',
      setProperty(k, v) { this['prop_' + k] = v; },
      removeProperty(k) { delete this['prop_' + k]; },
    };
    this.attrs = {};
    this.className = '';
    this.textContent = '';
  }
  setAttribute(k, v) { this.attrs[k] = v; }
  getAttribute(k) { return this.attrs[k]; }
  appendChild(c) { this.children.push(c); return c; }
  addEventListener() {}
  remove() {}
  all() {
    const acc = [this];
    for (const c of this.children) acc.push(...c.all());
    return acc;
  }
}

function makeDoc() {
  docCreated = [];
  return {
    createElement: (t) => {
      const e = new El(t);
      if (t === 'div') docCreated.push(e);
      return e;
    },
    documentElement: new El('html'),
    querySelectorAll: () => [],
    querySelector: () => null,
    getElementById: () => null,
    contains: () => false,
    createDocumentFragment: () => new El('frag'),
    scrollingElement: null,
    head: new El('head'),
    body: new El('body'),
  };
}

let docCreated = [];
const GATE_FIXED = 'if(!it.er&&!it.cl){';
const GATE_OLD = 'if(!it.er){';

function renderBubble(items, opts) {
  const oldGate = !!(opts && opts.oldGate);
  let body = extractJs();
  if (oldGate) {
    if (body.indexOf(GATE_FIXED) < 0) throw new Error('fixed gate not found: cannot build the pre-fix variant');
    body = body.replace(GATE_FIXED, GATE_OLD);
  }
  const doc = makeDoc();
  const ctx = {
    document: doc,
    window: { innerHeight: 800, innerWidth: 360, devicePixelRatio: 1, addEventListener: () => {} },
    __CLEAN_SOURCES__: {},
    __ITEMS__: items,
    __SOURCES__: [],
    __FONT__: 'sans-serif',
    __SIZE__: 400,
    __PID__: 'p1',
    __PARAS__: [],
    __TITLE__: '',
    console,
    // Never invoke callbacks: the page's scheduler re-enters forever when timers fire synchronously.
    requestAnimationFrame: () => 0,
    setTimeout: () => 0,
    clearTimeout: () => {},
    addEventListener: () => {},
    innerWidth: 360,
    innerHeight: 800,
    devicePixelRatio: 1,
    getComputedStyle: () => ({ backgroundColor: 'rgb(0,0,0)' }),
  };
  // Run the real page IIFE: it builds one '__ns_b' element per item and attaches __nsCover to it.
  if (process.env.DEBUG_JS) {
    // Surface the exception the page script swallows in its own try/catch.
    ctx.console = { log: (...a) => console.log('    [js]', ...a), warn: () => {}, error: (...a) => console.log('    [js-err]', ...a) };
    const wrapped = body.replace('(function(items,sources,font,pid){', '(function(items,sources,font,pid){ window.__nsTrace=[];');
    new Function(...Object.keys(ctx), wrapped)(...Object.values(ctx));
  } else {
    new Function(...Object.keys(ctx), body)(...Object.values(ctx));
  }
  // The overlay div is created but appended through a path the fake does not model, so walk every created node.
  const all = [];
  for (const root of docCreated) all.push(...root.all());
  const found = all.filter((e) => e.className === '__ns_b');
  if (!found.length && process.env.DEBUG_JS) {
    const ids = new Function('return 1');
    console.log('    total fake nodes:', all.length,
      ' classes:', [...new Set(all.map((e) => e.className).filter(Boolean))].join('|') || '(none)');
  }
  return found;
}

// The bubble as the 10:05 log describes it: cleaned=true, two cv silhouette rectangles, bg measured as black.
const cleanedBubble = {
  t: 'এটা ডাবারও', s: 'https://image-comic.pstatic.net/_003.jpg',
  cx: 0.5, cy: 0.3, w: 0.7, h: 0.2, a: 0, f: 0.05,
  bg: 'rgb(0,0,0)', fg: 'rgb(255,255,255)', g2: '', sf: true, o: '',
  cv: [0.0, 0.0, 0.6, 0.2, 0.1, 0.2, 0.5, 0.5],
  cl: true, er: false,
};

let failures = 0;
function check(name, cond, detail) {
  if (cond) {
    console.log('  ok   ' + name);
  } else {
    failures++;
    console.log('  FAIL ' + name + (detail ? '  -> ' + detail : ''));
  }
}

function cvRects(cover) {
  return cover ? cover.all().filter((e) => e.className === '__ns_cv') : [];
}

function isBlack(rect) {
  return /rgb\(\s*0\s*,\s*0\s*,\s*0\s*\)/.test(String(rect.style.cssText));
}

const variants = [
  { label: 'BEFORE fix (gate: !it.er only)', oldGate: true, expectBlack: true },
  { label: 'AFTER fix (gate: !it.er && !it.cl)', oldGate: false, expectBlack: false },
];

for (const v of variants) {
  console.log('\n' + v.label);
  const built = renderBubble([cleanedBubble], { oldGate: v.oldGate });
  const b = built[0];
  const cover = b && b.__nsCover;
  const rects = cvRects(cover);
  const black = rects.filter(isBlack).length;
  console.log('    bubbles=' + built.length + '  cover=' + (cover ? 'created' : 'none') +
    '  cv-rects=' + rects.length + '  painted-black=' + black);
  if (v.expectBlack) {
    check('pre-fix paints the black silhouette (the harness can see the real bug)', black > 0, 'black=' + black);
  } else {
    check('cleaned bubble gets NO cover rectangle', !cover);
    check('therefore no black cv rectangle is drawn', black === 0, 'black=' + black);
  }
}

console.log('\nNOT cleaned (cl=false) must keep its conservative cover');
{
  const b = renderBubble([{ ...cleanedBubble, cl: false, bg: 'rgb(255,255,255)' }])[0];
  const cover = b && b.__nsCover;
  check('still gets a cover', !!cover);
  const rects = cvRects(cover);
  check('and it is painted with the bubble colour, not black', rects.every((r) => !isBlack(r)));
}

console.log('\n' + (failures === 0 ? 'PASS' : 'FAIL (' + failures + ')'));
process.exit(failures === 0 ? 0 : 1);

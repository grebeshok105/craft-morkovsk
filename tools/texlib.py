"""Minimal pure-python 16x16 RGBA PNG writer + pixel helpers for texture gen.

No external deps — just zlib+struct so it runs on any python3.
"""
import os
import struct
import zlib


def _chunk(tag: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data))


def write_png(path: str, pixels, w: int = 16, h: int = 16):
    """pixels: list of h rows, each row list of w (r,g,b,a) tuples."""
    raw = bytearray()
    for row in pixels:
        raw.append(0)  # filter type 0
        for (r, g, b, a) in row:
            raw += bytes((r & 255, g & 255, b & 255, a & 255))
    png = (b"\x89PNG\r\n\x1a\n"
           + _chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
           + _chunk(b"IDAT", zlib.compress(bytes(raw), 9))
           + _chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def hex_rgb(s: str):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16))


def hex_rgba(s: str, a: int = 255):
    r, g, b = hex_rgb(s)
    return (r, g, b, a)


def shade(c, f: float):
    return (int(c[0] * f), int(c[1] * f), int(c[2] * f), c[3])


class Canvas:
    def __init__(self, w: int = 16, h: int = 16):
        self.w, self.h = w, h
        self.px = [[(0, 0, 0, 0) for _ in range(w)] for _ in range(h)]
        self._noise_state = 0x2F6E2B1

    def noise(self) -> float:
        # deterministic LCG
        self._noise_state = (1103515245 * self._noise_state + 12345) % (2 ** 31)
        return self._noise_state / (2 ** 31)

    def set(self, x: int, y: int, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = c

    def rect(self, x0, y0, x1, y1, c):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def fill_noise(self, base, alt, density=0.35):
        for y in range(self.h):
            for x in range(self.w):
                self.set(x, y, alt if self.noise() < density else base)

    def border(self, c):
        for x in range(self.w):
            self.set(x, 0, c)
            self.set(x, self.h - 1, c)
        for y in range(self.h):
            self.set(0, y, c)
            self.set(self.w - 1, y, c)

    def blob(self, cx, cy, rx, ry, c):
        for y in range(self.h):
            for x in range(self.w):
                dx = (x - cx) / rx
                dy = (y - cy) / ry
                if dx * dx + dy * dy <= 1.0:
                    self.set(x, y, c)


# ---------- patterns ----------

def soil(base_hex, alt_hex):
    c = Canvas()
    c.fill_noise(hex_rgba(base_hex), hex_rgba(alt_hex), 0.4)
    return c.px


def produce(main_hex, dark_hex):
    c = Canvas()
    main, dark = hex_rgba(main_hex), hex_rgba(dark_hex)
    c.blob(8, 9, 5.5, 5.0, main)            # body
    c.rect(7, 1, 8, 3, hex_rgba("3f7d2c"))  # stem
    c.set(9, 2, hex_rgba("57a83f"))         # leaf
    for y in range(11, 15):                 # shaded bottom
        for x in range(4, 13):
            if c.px[y][x][3] != 0:
                c.set(x, y, dark)
    c.set(5, 6, shade(main, 1.25))          # highlight
    c.set(6, 5, shade(main, 1.25))
    return c.px


def seeds(main_hex, tip_hex):
    c = Canvas()
    main, tip = hex_rgba(main_hex), hex_rgba(tip_hex)
    for i, (x, y) in enumerate([(5, 10), (8, 12), (10, 9), (7, 8), (9, 6)]):
        c.set(x, y, main)
        c.set(x, y - 1, tip)
        if i % 2 == 0:
            c.set(x + 1, y, main)
    return c.px


def crop_stage(foliage_hex, fruit_hex, stage: int):
    """0..7 — stems grow, fruit color appears at stage>=5."""
    c = Canvas()
    fol, fru = hex_rgba(foliage_hex), hex_rgba(fruit_hex)
    height = 2 + stage  # 2..9
    columns = [4, 8, 11]
    for cx in columns:
        h = height - (0 if cx == 8 else (1 if stage > 2 else 2))
        for y in range(15, 15 - max(1, h), -1):
            c.set(cx, y, fol)
        c.set(cx + 1, 15, shade(fol, 0.8))
        if stage >= 3:
            c.set(cx - 1, 15 - h + 1, shade(fol, 1.15))  # leaf
        if stage >= 5:
            c.set(cx, 15 - h, fru)  # tip = produce color
            if stage >= 7:
                c.set(cx + 1, 15 - h + 1, fru)
    return c.px


def machine(base_hex, accent_hex):
    c = Canvas()
    base, accent = hex_rgba(base_hex), hex_rgba(accent_hex)
    c.fill_noise(base, shade(base, 0.85), 0.25)
    c.border(shade(base, 0.55))
    c.rect(3, 3, 12, 6, hex_rgba("1c2b1e"))    # screen
    c.rect(4, 4, 5, 5, accent)                 # indicator
    c.rect(3, 9, 12, 12, shade(base, 0.7))     # lower grille
    for x in (5, 8, 11):
        c.set(x, 10, shade(base, 0.45))
    return c.px


def item_icon(main_hex, dark_hex):
    c = Canvas()
    main, dark = hex_rgba(main_hex), hex_rgba(dark_hex)
    c.blob(8, 8, 5, 5, main)
    for y in range(10, 14):
        for x in range(4, 13):
            if c.px[y][x][3] != 0:
                c.set(x, y, dark)
    c.set(6, 5, shade(main, 1.3))
    c.set(7, 4, shade(main, 1.3))
    return c.px


def cable(main_hex, dark_hex):
    c = Canvas()
    main, dark = hex_rgba(main_hex), hex_rgba(dark_hex)
    c.rect(6, 0, 9, 15, main)   # vertical core
    c.rect(0, 6, 15, 9, main)   # horizontal core
    for y in range(16):
        c.set(6, y, dark)
        c.set(9, y, dark)
    for x in range(16):
        c.set(x, 6, dark)
        c.set(x, 9, dark)
    c.rect(7, 7, 8, 8, shade(main, 1.3))
    return c.px

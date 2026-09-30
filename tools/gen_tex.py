#!/usr/bin/env python3
"""CLI texture generator.

usage: gen_tex.py <out_png> <pattern> <color1> [color2] [stage]

patterns: soil <base> <alt> | produce <main> <dark> | seeds <main> <tip> |
          crop <foliage> <fruit> <stage> | machine <base> <accent> |
          item <main> <dark> | cable <main> <dark>
"""
import sys
import texlib


def main():
    if len(sys.argv) < 4:
        print(__doc__)
        return 1
    out, pattern, c1 = sys.argv[1], sys.argv[2], sys.argv[3]
    c2 = sys.argv[4] if len(sys.argv) > 4 and not sys.argv[4].isdigit() else None
    arg5 = sys.argv[5] if len(sys.argv) > 5 else (sys.argv[4] if len(sys.argv) > 4 else "0")
    stage = int(arg5) if str(arg5).isdigit() else 0

    fn = {
        "soil": lambda: texlib.soil(c1, c2),
        "produce": lambda: texlib.produce(c1, c2),
        "seeds": lambda: texlib.seeds(c1, c2),
        "crop": lambda: texlib.crop_stage(c1, c2 or c1, stage),
        "machine": lambda: texlib.machine(c1, c2),
        "item": lambda: texlib.item_icon(c1, c2),
        "cable": lambda: texlib.cable(c1, c2),
    }.get(pattern)
    if fn is None:
        print(f"unknown pattern: {pattern}")
        return 1
    texlib.write_png(out, fn())
    return 0


if __name__ == "__main__":
    sys.exit(main())

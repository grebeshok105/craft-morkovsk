#!/usr/bin/env python3
"""Merges src/main/resources/assets/craftmorkovsk/lang_parts/*.json into en_us.json.

Every subsystem owns one fragment file (lang_parts/<pkg>.json) so parallel
branches never conflict on the monolithic lang file.
"""
import glob
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..",
                    "src/main/resources/assets/craftmorkovsk")
OUT = os.path.join(ROOT, "lang", "en_us.json")


def main():
    merged = {}
    for path in sorted(glob.glob(os.path.join(ROOT, "lang_parts", "*.json"))):
        with open(path) as f:
            part = json.load(f)
        for k, v in part.items():
            if k in merged and merged[k] != v:
                print(f"WARN duplicate lang key {k} ({os.path.basename(path)})")
            merged[k] = v
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w") as f:
        json.dump(dict(sorted(merged.items())), f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"merged {len(merged)} keys -> {OUT}")


if __name__ == "__main__":
    main()

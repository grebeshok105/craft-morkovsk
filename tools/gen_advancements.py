#!/usr/bin/env python3
"""Generates advancement JSONs (all granted programmatically via Award.grant)."""
import json
import os

DATA = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                    "data", "craftmorkovsk", "advancements")
MOD = "craftmorkovsk"

# path -> (parent, icon item, title key suffix, frame)
ADV = {
    "root": (None, f"{MOD}:morkov", "root", "task"),
    "first_harvest": ("root", f"{MOD}:morkov", "first_harvest", "task"),
    "dirt_professional": ("first_harvest", f"{MOD}:rich_farmland", "dirt_professional", "task"),
    "golden_harvest": ("dirt_professional", f"{MOD}:golden_carrot_plant", "golden_harvest", "goal"),
    "giant_morkov": ("dirt_professional", f"{MOD}:giant_morkov", "giant_morkov", "goal"),
    "morkovsk_supreme": ("golden_harvest", f"{MOD}:morkovsk_supreme", "morkovsk_supreme", "challenge"),
    "the_carrot": ("morkovsk_supreme", f"{MOD}:the_carrot", "the_carrot", "challenge"),
}


def main():
    for path, (parent, icon, key, frame) in ADV.items():
        adv = {
            "display": {
                "icon": {"item": icon},
                "title": {"translate": f"advancements.{MOD}.{key}.title"},
                "description": {"translate": f"advancements.{MOD}.{key}.description"},
                "frame": frame,
                "show_toast": True,
                "announce_to_chat": True,
            },
            "criteria": {"grant": {"trigger": "minecraft:impossible"}},
        }
        if parent:
            adv["parent"] = f"{MOD}:{parent}"
        else:
            adv["display"]["background"] = "minecraft:textures/block/dirt.png"
            adv["display"]["show_toast"] = False
            adv["display"]["announce_to_chat"] = False
        out = os.path.join(DATA, f"{path}.json")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        with open(out, "w") as f:
            json.dump(adv, f, indent=2)
            f.write("\n")
    print(f"wrote {len(ADV)} advancements")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Generates assets for core (non-crop) content: soil blocks, energy machines,
fertilizer items, compost bin, plus their lang fragment."""
import json
import os

import texlib

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "craftmorkovsk")
MOD = "craftmorkovsk"

# name -> (display name, base color, alt color)
SOILS = {
    "exhausted_soil": ("Exhausted Soil", "#8a7a5c", "#6f6248"),
    "rich_farmland": ("Rich Farmland", "#5b3d24", "#6e4c2e"),
    "rich_soil": ("Rich Soil", "#4a2f1a", "#5e4023"),
    "wet_soil": ("Wet Soil", "#3d2a18", "#2e2012"),
    "compost_soil": ("Compost Soil", "#3a2515", "#54401f"),
    "fertilized_soil": ("Fertilized Soil", "#43311c", "#6b5a2e"),
}

MACHINES = {
    "energy_cable": ("Energy Cable", "cable", "#8a7048", "#5c4a2e"),
    "small_generator": ("Small Generator", "machine", "#7a6a4f", "#e0a828"),
    "biomass_generator": ("Biomass Generator", "machine", "#5f7a3f", "#6fae4f"),
    "battery": ("Battery", "machine", "#6a5a44", "#e0a828"),
    "compost_bin": ("Compost Bin", "machine", "#6b4f33", "#8a5c2e"),
}

ITEMS = {
    "compost": ("Compost", "#54401f", "#3a2b14"),
    "manure_fertilizer": ("Manure Fertilizer", "#6b4a28", "#4a3119"),
    "mineral_fertilizer": ("Mineral Fertilizer", "#9aa8b0", "#6a767e"),
    "morkovsk_fertilizer": ("Morkovsk Fertilizer", "#d4b03a", "#8f7420"),
}


def jwrite(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def simple_block(name, tex_name):
    jwrite(f"{ASSETS}/blockstates/{name}.json",
           {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    jwrite(f"{ASSETS}/models/block/{name}.json",
           {"parent": "minecraft:block/cube_all",
            "textures": {"all": f"{MOD}:block/{tex_name}"}})
    jwrite(f"{ASSETS}/models/item/{name}.json",
           {"parent": f"{MOD}:block/{name}"})


def simple_item(name, tex_name):
    jwrite(f"{ASSETS}/models/item/{name}.json",
           {"parent": "minecraft:item/generated",
            "textures": {"layer0": f"{MOD}:item/{tex_name}"}})


def main():
    lang = {}

    for name, (disp, base, alt) in SOILS.items():
        simple_block(name, name)
        texlib.write_png(f"{ASSETS}/textures/block/{name}.png", texlib.soil(base, alt))
        lang[f"block.{MOD}.{name}"] = disp

    for name, (disp, pattern, c1, c2) in MACHINES.items():
        simple_block(name, name)
        px = texlib.cable(c1, c2) if pattern == "cable" else texlib.machine(c1, c2)
        texlib.write_png(f"{ASSETS}/textures/block/{name}.png", px)
        lang[f"block.{MOD}.{name}"] = disp

    for name, (disp, c1, c2) in ITEMS.items():
        simple_item(name, name)
        texlib.write_png(f"{ASSETS}/textures/item/{name}.png", texlib.item_icon(c1, c2))
        lang[f"item.{MOD}.{name}"] = disp

    jwrite(f"{ASSETS}/lang_parts/core.json", lang)
    print(f"generated {len(SOILS) + len(MACHINES)} block + {len(ITEMS)} item assets")


if __name__ == "__main__":
    main()

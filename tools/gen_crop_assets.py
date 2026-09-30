#!/usr/bin/env python3
"""Generates all data/asset files for the crop catalog:
blockstates, block models (8 growth stages), item models, textures,
the crop lang fragment, and the compostable/biomass item tags.

Keep CROPS in sync with CropCatalog.java.
"""
import json
import os

import texlib

ROOT = os.path.join(os.path.dirname(__file__), "..", "src/main", "resources")
ASSETS = os.path.join(ROOT, "assets", "craftmorkovsk")
DATA = os.path.join(ROOT, "data", "craftmorkovsk")
MOD = "craftmorkovsk"

# id -> (display name, foliage color, produce color)
CROPS = {
    "morkov": ("Morkov", "4d8a3e", "e8862b"),
    "carrot_red": ("Red Carrot", "4d8a3e", "c94f2e"),
    "carrot_purple": ("Purple Carrot", "4d8a3e", "7a4a9e"),
    "potato": ("Potato", "5f8f45", "b58d4d"),
    "tomato": ("Tomato", "4d8a3e", "d43c2c"),
    "corn": ("Corn", "6b9440", "e5c038"),
    "onion": ("Onion", "5f8f45", "d8c9a3"),
    "garlic": ("Garlic", "6b9440", "e8e0cc"),
    "cabbage": ("Cabbage", "3f7d2c", "7fb84a"),
    "lettuce": ("Lettuce", "5fae4a", "9fd06b"),
    "cucumber": ("Cucumber", "4d8a3e", "3f7d2c"),
    "strawberry": ("Strawberry", "3f7d2c", "e0455a"),
    "blueberry": ("Blueberry", "4d8a3e", "4a5fae"),
    "grape": ("Grape", "3f7d2c", "7a4a9e"),
    "rice": ("Rice", "a8b060", "e0d898"),
    "wheat_spelt": ("Spelt Wheat", "c9b040", "d8c060"),
    "wheat_rye": ("Rye", "b09840", "c8a860"),
    "pumpkin_morkovsk": ("Morkovsk Pumpkin", "4d8a3e", "d9821f"),
    "melon_morkovsk": ("Morkovsk Melon", "4d8a3e", "3fae3f"),
    "sunflower": ("Sunflower", "5f8f45", "f0c020"),
    "pepper_chili": ("Chili Pepper", "4d8a3e", "c92c1f"),
    "pepper_bell": ("Bell Pepper", "4d8a3e", "e8b023"),
    "herb_basil": ("Basil", "2f6e2b", "4d9e45"),
    "herb_mint": ("Mint", "5fae7a", "7fd0a0"),
    "herb_thyme": ("Thyme", "6b8f55", "9fb87f"),
    "golden_carrot_plant": ("Golden Carrot Plant", "b8a030", "f0c020"),
    "giant_morkov": ("Giant Morkov", "3f7d2c", "e8862b"),
    "morkovsk_supreme": ("Morkovsk Supreme", "8f7d1f", "ff9a1f"),
    "the_carrot": ("THE CARROT", "ffd700", "ff6a00"),
}


def jwrite(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def gen_crop(cid, name, foliage, fruit):
    # blockstate: age property 0..7 -> stage models
    variants = {}
    for age in range(8):
        variants[f"age={age}"] = {"model": f"{MOD}:block/{cid}_crop_stage{age}"}
    jwrite(f"{ASSETS}/blockstates/{cid}_crop.json", {"variants": variants})

    # block models: vanilla cross model with our texture
    for stage in range(8):
        jwrite(f"{ASSETS}/models/block/{cid}_crop_stage{stage}.json", {
            "parent": "minecraft:block/crop",
            "textures": {"crop": f"{MOD}:block/{cid}_crop_stage{stage}"},
        })
        texlib.write_png(
            f"{ASSETS}/textures/block/{cid}_crop_stage{stage}.png",
            texlib.crop_stage(foliage, fruit, stage))

    # item models
    jwrite(f"{ASSETS}/models/item/{cid}_seeds.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"{MOD}:item/{cid}_seeds"},
    })
    jwrite(f"{ASSETS}/models/item/{cid}.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"{MOD}:item/{cid}"},
    })

    # item textures
    texlib.write_png(f"{ASSETS}/textures/item/{cid}_seeds.png",
                     texlib.seeds("8a6b3a", foliage))
    texlib.write_png(f"{ASSETS}/textures/item/{cid}.png",
                     texlib.produce(fruit, "#5a3a1a"))
    return {
        f"block.{MOD}.{cid}_crop": name,
        f"item.{MOD}.{cid}_seeds": f"{name} Seeds",
        f"item.{MOD}.{cid}": name,
    }


def main():
    lang = {}
    for cid, (name, foliage, fruit) in CROPS.items():
        lang.update(gen_crop(cid, name, foliage, fruit))

    jwrite(f"{ASSETS}/lang_parts/crop.json", lang)

    # item tags used by the compost bin / biomass generator
    seeds_ids = [f"{MOD}:{cid}_seeds" for cid in CROPS]
    produce_ids = [f"{MOD}:{cid}" for cid in CROPS]
    jwrite(f"{DATA}/tags/items/compostable.json",
           {"replace": False, "values": seeds_ids + produce_ids})
    jwrite(f"{DATA}/tags/items/biomass_fuel.json",
           {"replace": False, "values": seeds_ids + produce_ids})
    rich = [f"{MOD}:{c}" for c in ("golden_carrot_plant", "giant_morkov",
                                   "morkovsk_supreme", "the_carrot")]
    jwrite(f"{DATA}/tags/items/compostable_rich.json",
           {"replace": False, "values": rich})

    print(f"generated assets for {len(CROPS)} crops")


if __name__ == "__main__":
    main()

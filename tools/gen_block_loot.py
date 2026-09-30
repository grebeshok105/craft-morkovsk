#!/usr/bin/env python3
"""Generates simple 'drops itself' loot tables for core blocks."""
import json
import os

DATA = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                    "data", "craftmorkovsk", "loot_tables", "blocks")
MOD = "craftmorkovsk"

BLOCKS = [
    "exhausted_soil", "rich_farmland", "rich_soil", "wet_soil", "compost_soil",
    "fertilized_soil", "energy_cable", "small_generator", "biomass_generator",
    "battery", "compost_bin",
]


def main():
    for b in BLOCKS:
        table = {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": f"{MOD}:{b}"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        }
        path = os.path.join(DATA, f"{b}.json")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as f:
            json.dump(table, f, indent=2)
            f.write("\n")
    print(f"wrote {len(BLOCKS)} block loot tables")


if __name__ == "__main__":
    main()

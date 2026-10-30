#!/usr/bin/env python3
"""Convert LibGDX TexturePacker atlases (.txt) into Godot 4 resources.

- gameAssets.txt (static regions)  -> assets/atlases/regions/<name>.tres   (AtlasTexture)
- *Animation*.txt (frame sheets)    -> assets/animations/<name>.tres        (SpriteFrames)

Run from the repository root or the godot/ directory:
    python3 godot/tools/convert_atlases.py

The generated .tres files reference the original PNG sheets, so the artwork is
never duplicated. Re-run whenever a sheet changes.
"""
from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path

GODOT_DIR = Path(__file__).resolve().parent.parent
ATLAS_DIR = GODOT_DIR / "assets" / "atlases"
REGION_OUT = ATLAS_DIR / "regions"
ANIM_OUT = GODOT_DIR / "assets" / "animations"

# Original LibGDX FRAME_DURATION was 0.025 s -> 40 fps.
ANIM_FPS = 40.0

# Which animations loop (mirrors Assets.AssetAnimations in the Java code).
LOOPING = {
    "spikeAnimations",
    "deathSpikeAnimation",
    "playButtonSawAnimation",
    "suicideParticleAnimations",
    "tutorialArrowAnimation",
}


@dataclass
class Region:
    name: str
    x: int
    y: int
    w: int
    h: int
    index: int = -1
    orig_w: int = 0
    orig_h: int = 0
    # LibGDX strips whitespace; offset is measured from the LEFT and BOTTOM edges
    # of the original image (y-up).
    off_x: int = 0
    off_y: int = 0

    def margin(self) -> str | None:
        """Godot AtlasTexture.margin that pads the trimmed frame back to its original size."""
        if (self.orig_w, self.orig_h) in ((0, 0), (self.w, self.h)):
            return None
        top = self.orig_h - self.h - self.off_y
        return f"Rect2({self.off_x}, {top}, {self.orig_w - self.w}, {self.orig_h - self.h})"


@dataclass
class Atlas:
    image: str
    regions: list[Region] = field(default_factory=list)


def parse_atlas(path: Path) -> Atlas:
    lines = [ln.rstrip("\n") for ln in path.read_text().splitlines()]
    # Skip leading blank lines; first non-blank line is the image file name.
    i = 0
    while i < len(lines) and not lines[i].strip():
        i += 1
    atlas = Atlas(image=lines[i].strip())
    i += 1
    # Skip header key/values (format, filter, repeat).
    while i < len(lines) and ":" in lines[i] and not lines[i].startswith(" "):
        i += 1
    current: Region | None = None
    while i < len(lines):
        ln = lines[i]
        i += 1
        if not ln.strip():
            continue
        if not ln.startswith(" "):
            current = Region(name=ln.strip(), x=0, y=0, w=0, h=0)
            atlas.regions.append(current)
            continue
        assert current is not None
        key, _, value = ln.strip().partition(":")
        value = value.strip()
        if key == "xy":
            current.x, current.y = (int(v) for v in value.split(","))
        elif key == "size":
            current.w, current.h = (int(v) for v in value.split(","))
        elif key == "orig":
            current.orig_w, current.orig_h = (int(v) for v in value.split(","))
        elif key == "offset":
            current.off_x, current.off_y = (int(v) for v in value.split(","))
        elif key == "index":
            current.index = int(value)
    return atlas


def texture_lines(region: Region) -> list[str]:
    lines = ['atlas = ExtResource("1")', f"region = Rect2({region.x}, {region.y}, {region.w}, {region.h})"]
    margin = region.margin()
    if margin:
        lines.append(f"margin = {margin}")
    return lines


def frames_in_order(regions: list[Region]) -> list[Region]:
    """LibGDX returns regions in file order, except indexed frames are sorted by index."""
    if all(r.index == -1 for r in regions):
        return regions
    return sorted(regions, key=lambda r: (r.index if r.index != -1 else 10**9))


def write_region(atlas_png: str, region: Region) -> None:
    out = REGION_OUT / f"{region.name}.tres"
    out.write_text(
        "\n".join(
            [
                '[gd_resource type="AtlasTexture" load_steps=2 format=3]',
                "",
                f'[ext_resource type="Texture2D" path="res://assets/atlases/{atlas_png}" id="1"]',
                "",
                "[resource]",
                *texture_lines(region),
                "",
            ]
        )
    )


def write_sprite_frames(name: str, atlas_png: str, frames: list[Region], loop: bool) -> None:
    out = ANIM_OUT / f"{name}.tres"
    lines = [
        f'[gd_resource type="SpriteFrames" load_steps={len(frames) + 2} format=3]',
        "",
        f'[ext_resource type="Texture2D" path="res://assets/atlases/{atlas_png}" id="1"]',
        "",
    ]
    for n, fr in enumerate(frames, start=1):
        lines += [f'[sub_resource type="AtlasTexture" id="f{n}"]', *texture_lines(fr), ""]
    frame_entries = ", ".join(
        f'{{\n"duration": 1.0,\n"texture": SubResource("f{n}")\n}}' for n in range(1, len(frames) + 1)
    )
    lines += [
        "[resource]",
        "animations = [{",
        f'"frames": [{frame_entries}],',
        f'"loop": {"true" if loop else "false"},',
        '"name": &"default",',
        f'"speed": {ANIM_FPS}',
        "}]",
        "",
    ]
    out.write_text("\n".join(lines))


def main() -> None:
    REGION_OUT.mkdir(parents=True, exist_ok=True)
    ANIM_OUT.mkdir(parents=True, exist_ok=True)
    for txt in sorted(ATLAS_DIR.glob("*.txt")):
        atlas = parse_atlas(txt)
        stem = txt.stem
        if stem == "gameAssets":
            for r in atlas.regions:
                write_region(atlas.image, r)
            print(f"{stem}: {len(atlas.regions)} regions")
        else:
            frames = frames_in_order(atlas.regions)
            write_sprite_frames(stem, atlas.image, frames, stem in LOOPING)
            print(f"{stem}: {len(frames)} frames, loop={stem in LOOPING}")


if __name__ == "__main__":
    main()

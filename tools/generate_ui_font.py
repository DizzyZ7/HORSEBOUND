#!/usr/bin/env python3
"""Generate the HORSEBOUND Unicode BMFont atlas from DejaVu Sans.

The generated atlas is deterministic for a fixed Pillow/FreeType combination and
contains every character used by the Russian and English localization catalogs,
plus stable Latin/Cyrillic/punctuation ranges needed by dynamic player-facing text.
"""
from __future__ import annotations

import argparse
import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ATLAS_WIDTH = 2048
FONT_SIZE = 32
PADDING = 4


def find_font(explicit: str | None) -> Path:
    candidates = [
        explicit,
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/dejavu/DejaVuSans.ttf",
        "C:/Windows/Fonts/DejaVu.ttf",
    ]
    for candidate in candidates:
        if candidate and Path(candidate).is_file():
            return Path(candidate)
    raise SystemExit("DejaVu Sans was not found. Pass --font /path/to/DejaVuSans.ttf")


def localization_characters(root: Path) -> set[str]:
    chars: set[str] = set(chr(code) for code in range(32, 127))
    # Stable UI coverage for names, future copy, keyboard labels and diagnostics.
    for start, end in (
        (0x00A0, 0x0250),  # Latin-1 + Latin Extended A/B + IPA subset
        (0x0400, 0x0530),  # Cyrillic + Cyrillic Supplement
        (0x2000, 0x2070),  # General punctuation
        (0x20A0, 0x20D0),  # Currency symbols
        (0x2190, 0x2200),  # Arrows
        (0x2200, 0x2300),  # Mathematical operators
    ):
        chars.update(chr(code) for code in range(start, end))
    for path in sorted((root / "src/main/resources/i18n").glob("messages_*.properties")):
        text = path.read_text(encoding="utf-8")
        chars.update(ch for ch in text if ch not in "\r\n\t")
    return chars


def next_power_of_two(value: int) -> int:
    return 1 << max(0, math.ceil(math.log2(max(1, value))))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=str(Path(__file__).resolve().parents[1]))
    parser.add_argument("--font")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    font_path = find_font(args.font)
    font = ImageFont.truetype(str(font_path), FONT_SIZE)
    ascent, descent = font.getmetrics()
    line_height = ascent + descent + 4

    chars = sorted(localization_characters(root), key=ord)
    metrics: list[dict[str, int | str]] = []
    x = PADDING
    y = PADDING
    row_height = 0
    for ch in chars:
        left, top, right, bottom = font.getbbox(ch)
        width = max(0, right - left)
        height = max(0, bottom - top)
        advance = max(1, round(font.getlength(ch)))
        packed_width = max(1, width)
        packed_height = max(1, height)
        if x + packed_width + PADDING > ATLAS_WIDTH:
            x = PADDING
            y += row_height + PADDING
            row_height = 0
        metrics.append({
            "char": ch,
            "id": ord(ch),
            "x": x,
            "y": y,
            "width": width,
            "height": height,
            "xoffset": left,
            "yoffset": top,
            "xadvance": advance,
        })
        x += packed_width + PADDING
        row_height = max(row_height, packed_height)

    atlas_height = max(64, next_power_of_two(y + row_height + PADDING))
    atlas = Image.new("RGBA", (ATLAS_WIDTH, atlas_height), (255, 255, 255, 0))
    draw = ImageDraw.Draw(atlas)
    for item in metrics:
        if item["width"] == 0 or item["height"] == 0:
            continue
        draw.text(
            (int(item["x"]) - int(item["xoffset"]), int(item["y"]) - int(item["yoffset"])),
            str(item["char"]),
            font=font,
            fill=(255, 255, 255, 255),
        )

    output_dir = root / "src/main/resources/fonts"
    output_dir.mkdir(parents=True, exist_ok=True)
    png_path = output_dir / "horsebound-ui.png"
    fnt_path = output_dir / "horsebound-ui.fnt"
    atlas.save(png_path, format="PNG", optimize=False, compress_level=6)

    lines = [
        f'info face="HORSEBOUND UI" size={FONT_SIZE} bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1',
        f"common lineHeight={line_height} base={ascent} scaleW={ATLAS_WIDTH} scaleH={atlas_height} pages=1 packed=0",
        'page id=0 file="horsebound-ui.png"',
        f"chars count={len(metrics)}",
    ]
    for item in metrics:
        lines.append(
            "char id={id} x={x} y={y} width={width} height={height} "
            "xoffset={xoffset} yoffset={yoffset} xadvance={xadvance} page=0 chnl=15".format(**item)
        )
    fnt_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Generated {len(metrics)} glyphs: {png_path} ({atlas.size[0]}x{atlas.size[1]})")


if __name__ == "__main__":
    main()

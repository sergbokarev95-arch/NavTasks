#!/usr/bin/env python3
"""Export EXACT launcher vector foreground to transparent RGBA PNG for HONOR themes.

Android adaptive icons cannot switch backgrounds based on the active OEM launcher
theme. The transparent PNG must be merged into the user's MagicOS Liquid Glass
theme (package mapping com.navtasks.app.glass), NOT installed by the APK.
"""
from pathlib import Path
from xml.etree import ElementTree as ET
from html import escape
import argparse
import cairosvg
from PIL import Image

NS = "{http://schemas.android.com/apk/res/android}"


def attr(node, name, default=None):
    return node.attrib.get(NS + name, default)


def svg_children(node):
    elements = []
    for child in node:
        kind = child.tag.rsplit("}", 1)[-1]
        if kind == "group":
            x, y = attr(child, "pivotX", "0"), attr(child, "pivotY", "0")
            tx, ty = attr(child, "translateX", "0"), attr(child, "translateY", "0")
            sx, sy = attr(child, "scaleX", "1"), attr(child, "scaleY", "1")
            r = attr(child, "rotation", "0")
            transform = (f"translate({tx} {ty}) translate({x} {y}) "
                         f"rotate({r}) scale({sx} {sy}) translate(-{x} -{y})")
            elements.append(f'<g transform="{transform}">{svg_children(child)}</g>')
        elif kind == "path":
            fill = attr(child, "fillColor", "none")
            stroke = attr(child, "strokeColor", "none")
            if fill.startswith("@"):
                fill = "none"
            if stroke.startswith("@"):
                stroke = "none"
            params = {
                "d": attr(child, "pathData", ""), "fill": fill,
                "fill-opacity": attr(child, "fillAlpha", "1"),
                "stroke": stroke, "stroke-width": attr(child, "strokeWidth", "1"),
                "stroke-opacity": attr(child, "strokeAlpha", "1"),
                "stroke-linecap": attr(child, "strokeLineCap", "butt"),
                "stroke-linejoin": attr(child, "strokeLineJoin", "miter"),
            }
            elements.append("<path " + " ".join(
                f'{k}="{escape(v)}"' for k, v in params.items()) + "/>")
        else:
            raise ValueError(f"Unexpected vector node: {kind}")
    return "".join(elements)


def export(source: Path, output: Path, size: int = 512):
    root = ET.parse(source).getroot()
    assert root.tag.endswith("vector")
    w, h = attr(root, "viewportWidth", "108"), attr(root, "viewportHeight", "108")
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}">'
           f'{svg_children(root)}</svg>')
    cairosvg.svg2png(bytestring=svg.encode(), write_to=str(output),
                     output_width=size, output_height=size)
    image = Image.open(output)
    assert image.mode == "RGBA"
    assert image.getpixel((0, 0))[3] == 0
    assert image.getpixel((size-1, size-1))[3] == 0
    assert image.getpixel((size//2, size//2))[3] > 0
    print(f"Exported foreground-only transparent asset: {output}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--size", type=int, default=512)
    args = parser.parse_args()
    export(args.source, args.output, args.size)

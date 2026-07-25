#!/usr/bin/env python3
"""
MapLibre style.json Label Resizer Script
Increases text labels by 30% and text-halo-width by +0.5 for better mobile readability.
"""

import json
import argparse
import sys
from pathlib import Path

def scale_value(val, factor=1.3, zoom=None):
    """
    Scale a numeric text size value and round appropriately.
    If zoom level is provided, progressively boosts high zoom (zoom >= 14) 
    and max zoom (zoom >= 16) labels so street names/POIs are extra large when zoomed in.
    """
    if isinstance(val, (int, float)):
        mult = factor
        if isinstance(zoom, (int, float)):
            if zoom >= 16:
                mult = factor * 1.5   # ~1.95x boost at max zoom
            elif zoom >= 14:
                mult = factor * 1.25  # ~1.625x boost at high zoom
        res = val * mult
        rounded = round(res, 2)
        return int(rounded) if rounded.is_integer() else rounded
    return val

def process_text_size(text_size, factor=1.3):
    """Process text-size property which can be a static number, stops dict, or expression array."""
    if isinstance(text_size, (int, float)):
        # Static font size receives default scaling + extra boost for general visibility
        return scale_value(text_size, factor * 1.2)
    
    elif isinstance(text_size, dict) and "stops" in text_size:
        # Dynamic zoom function using stops: {"base": 1, "stops": [[12, 10], [18, 14]]}
        new_stops = []
        for stop in text_size["stops"]:
            if isinstance(stop, (list, tuple)) and len(stop) == 2:
                zoom, size = stop
                new_stops.append([zoom, scale_value(size, factor, zoom=zoom)])
            else:
                new_stops.append(stop)
        text_size["stops"] = new_stops
        return text_size
    
    elif isinstance(text_size, list):
        # MapLibre GL expression syntax
        # e.g., ["interpolate", ["linear"], ["zoom"], zoom1, size1, zoom2, size2, ...]
        # or ["step", ["zoom"], default_size, stop1, size1, ...]
        if len(text_size) > 0 and text_size[0] in ("interpolate", "step"):
            expr_type = text_size[0]
            if expr_type == "interpolate" and len(text_size) >= 5:
                new_expr = list(text_size[:3])
                for i in range(3, len(text_size), 2):
                    zoom = text_size[i]
                    val = text_size[i+1] if i+1 < len(text_size) else None
                    new_expr.append(zoom)
                    if val is not None:
                        new_expr.append(scale_value(val, factor, zoom=zoom) if isinstance(val, (int, float)) else val)
                return new_expr
            elif expr_type == "step" and len(text_size) >= 4:
                new_expr = list(text_size[:2])
                default_val = text_size[2]
                new_expr.append(scale_value(default_val, factor) if isinstance(default_val, (int, float)) else default_val)
                for i in range(3, len(text_size), 2):
                    stop = text_size[i]
                    val = text_size[i+1] if i+1 < len(text_size) else None
                    new_expr.append(stop)
                    if val is not None:
                        new_expr.append(scale_value(val, factor, zoom=stop) if isinstance(val, (int, float)) else val)
                return new_expr
    return text_size

def process_style(style_data, factor=1.3, halo_add=0.5):
    """Iterate through layers array and update symbol layer label sizes and halos."""
    layers = style_data.get("layers", [])
    symbol_layer_count = 0
    modified_text_size_count = 0
    modified_halo_count = 0

    for layer in layers:
        if not isinstance(layer, dict):
            continue
        
        if layer.get("type") == "symbol":
            symbol_layer_count += 1
            
            # 1. Update text-size in layout
            layout = layer.get("layout", {})
            if isinstance(layout, dict) and "text-size" in layout:
                layout["text-size"] = process_text_size(layout["text-size"], factor)
                layer["layout"] = layout
                modified_text_size_count += 1
            
            # 2. Update text-halo-width in paint
            paint = layer.get("paint", {})
            if isinstance(paint, dict) and "text-halo-width" in paint:
                halo_width = paint["text-halo-width"]
                if isinstance(halo_width, (int, float)):
                    new_halo = round(halo_width + halo_add, 2)
                    paint["text-halo-width"] = int(new_halo) if new_halo.is_integer() else new_halo
                    modified_halo_count += 1
                elif isinstance(halo_width, dict) and "stops" in halo_width:
                    new_stops = []
                    for stop in halo_width["stops"]:
                        if isinstance(stop, (list, tuple)) and len(stop) == 2:
                            z, hw = stop
                            if isinstance(hw, (int, float)):
                                nh = round(hw + halo_add, 2)
                                new_stops.append([z, int(nh) if nh.is_integer() else nh])
                            else:
                                new_stops.append(stop)
                        else:
                            new_stops.append(stop)
                    halo_width["stops"] = new_stops
                    modified_halo_count += 1
                layer["paint"] = paint

    return style_data, {
        "symbol_layers": symbol_layer_count,
        "modified_text_sizes": modified_text_size_count,
        "modified_halos": modified_halo_count
    }

def main():
    parser = argparse.ArgumentParser(description="Update MapLibre style.json to enlarge label font sizes.")
    parser.add_argument("input_file", nargs="?", default="app/src/main/assets/style.json", help="Path to input style.json file")
    parser.add_argument("output_file", nargs="?", default=None, help="Path to output style JSON file (defaults to style_large.json in same dir)")
    parser.add_argument("--scale", type=float, default=1.3, help="Font size multiplier factor (default: 1.3 for 30% increase)")
    parser.add_argument("--halo-add", type=float, default=0.5, help="Halo width addition (default: 0.5)")
    parser.add_argument("--indent", type=int, default=2, help="JSON indentation level (default: 2)")

    args = parser.parse_args()

    input_path = Path(args.input_file)
    if not input_path.exists():
        print(f"Error: Input file '{input_path}' not found.", file=sys.stderr)
        sys.exit(1)

    output_path = Path(args.output_file) if args.output_file else input_path.parent / "style_large.json"

    with open(input_path, "r", encoding="utf-8") as f:
        style_data = json.load(f)

    updated_style, stats = process_style(style_data, factor=args.scale, halo_add=args.halo_add)

    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(updated_style, f, ensure_ascii=False, indent=args.indent)

    print(f"Successfully processed MapLibre style!")
    print(f"  Input:  {input_path}")
    print(f"  Output: {output_path}")
    print(f"  Symbol Layers Processed: {stats['symbol_layers']}")
    print(f"  Text Sizes Scaled:       {stats['modified_text_sizes']}")
    print(f"  Text Halos Adjusted:     {stats['modified_halos']}")

if __name__ == "__main__":
    main()

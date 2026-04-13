#!/usr/bin/env python3
"""
Extract DrawingML content from .xlsx files in a directory tree.
DrawingML contains vector diagrams (process flows, screen transitions, architecture)
embedded as XML shapes in xl/drawings/*.xml within the ZIP structure.

Usage: python extract_drawingml.py <input_dir> <output_file>
"""

import os
import sys
import zipfile
import xml.etree.ElementTree as ET


# Common DrawingML/SpreadsheetDrawing namespaces
NAMESPACES = {
    "xdr": "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing",
    "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
    "r": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
    "mc": "http://schemas.openxmlformats.org/markup-compatibility/2006",
    "c": "http://schemas.openxmlformats.org/drawingml/2006/chart",
}


def extract_text_from_element(elem):
    """Recursively extract all text from an XML element and its children."""
    texts = []
    if elem.text and elem.text.strip():
        texts.append(elem.text.strip())
    for child in elem:
        texts.extend(extract_text_from_element(child))
    if elem.tail and elem.tail.strip():
        texts.append(elem.tail.strip())
    return texts


def extract_drawingml_from_xlsx(filepath):
    """Extract DrawingML text content from an Excel file."""
    results = []
    base_name = os.path.basename(filepath)

    try:
        with zipfile.ZipFile(filepath, "r") as zf:
            drawing_files = [
                name for name in zf.namelist()
                if name.startswith("xl/drawings/") and name.endswith(".xml")
            ]

            for drawing_file in sorted(drawing_files):
                try:
                    with zf.open(drawing_file) as f:
                        content = f.read()

                    root = ET.fromstring(content)
                    texts = extract_text_from_element(root)

                    if texts:
                        drawing_name = os.path.basename(drawing_file)
                        results.append((drawing_name, texts))
                except ET.ParseError as e:
                    results.append((drawing_file, [f"XML parse error: {e}"]))

    except (zipfile.BadZipFile, Exception) as e:
        results.append(("ERROR", [f"Failed to process: {e}"]))

    return results


def main():
    if len(sys.argv) < 3:
        print("Usage: python extract_drawingml.py <input_dir> <output_file>")
        sys.exit(1)

    input_dir = sys.argv[1]
    output_file = sys.argv[2]

    file_count = 0
    drawing_count = 0

    with open(output_file, "w", encoding="utf-8") as out:
        for root, dirs, files in os.walk(input_dir):
            for fname in sorted(files):
                if not fname.lower().endswith(".xlsx"):
                    continue

                fpath = os.path.join(root, fname)
                rel_path = os.path.relpath(fpath, input_dir)
                drawings = extract_drawingml_from_xlsx(fpath)

                if drawings:
                    file_count += 1
                    for drawing_name, texts in drawings:
                        if texts:
                            drawing_count += 1
                            out.write("=" * 60 + "\n")
                            out.write(f"FILE: {rel_path}\n")
                            out.write(f"DRAWING: {drawing_name}\n")
                            out.write("=" * 60 + "\n")
                            for text in texts:
                                out.write(text + "\n")
                            out.write("\n")

    print(f"Extracted {drawing_count} drawings from {file_count} files -> {output_file}")


if __name__ == "__main__":
    main()

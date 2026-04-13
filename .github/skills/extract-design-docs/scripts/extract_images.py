#!/usr/bin/env python3
"""
Extract embedded images from .xlsx and .docx files in a directory tree.
Images are saved with descriptive filenames preserving the source document context.

Usage: python extract_images.py <input_dir> <output_dir>
"""

import os
import sys
import zipfile
import subprocess


def ensure_packages():
    """Install required packages if missing."""
    for pkg in ["Pillow"]:
        try:
            __import__("PIL")
        except ImportError:
            subprocess.check_call([sys.executable, "-m", "pip", "install", pkg, "-q"])


def sanitize_filename(name):
    """Remove/replace characters that are invalid in filenames."""
    for ch in r'<>:"/\|?*':
        name = name.replace(ch, "_")
    return name


def extract_images_from_office(filepath, output_dir):
    """Extract images from an Office Open XML file (.xlsx or .docx)."""
    extracted = 0
    ext = os.path.splitext(filepath)[1].lower()
    base_name = sanitize_filename(os.path.splitext(os.path.basename(filepath))[0])

    # Office files are ZIP archives
    media_prefixes = []
    if ext == ".xlsx":
        media_prefixes = ["xl/media/"]
    elif ext == ".docx":
        media_prefixes = ["word/media/"]

    try:
        with zipfile.ZipFile(filepath, "r") as zf:
            for entry in zf.namelist():
                is_media = any(entry.startswith(p) for p in media_prefixes)
                if not is_media:
                    continue

                img_ext = os.path.splitext(entry)[1].lower()
                if img_ext not in (".png", ".jpg", ".jpeg", ".gif", ".bmp", ".emf", ".wmf", ".tiff"):
                    continue

                extracted += 1
                out_name = f"{base_name}_{extracted:03d}{img_ext}"
                out_path = os.path.join(output_dir, out_name)

                with zf.open(entry) as src, open(out_path, "wb") as dst:
                    dst.write(src.read())

    except (zipfile.BadZipFile, Exception) as e:
        print(f"  Warning: Could not process {filepath}: {e}")

    return extracted


def main():
    if len(sys.argv) < 3:
        print("Usage: python extract_images.py <input_dir> <output_dir>")
        sys.exit(1)

    input_dir = sys.argv[1]
    output_dir = sys.argv[2]

    ensure_packages()
    os.makedirs(output_dir, exist_ok=True)

    total_images = 0
    file_count = 0

    for root, dirs, files in os.walk(input_dir):
        for fname in sorted(files):
            ext = os.path.splitext(fname)[1].lower()
            if ext not in (".xlsx", ".docx"):
                continue

            fpath = os.path.join(root, fname)
            count = extract_images_from_office(fpath, output_dir)
            if count > 0:
                file_count += 1
                total_images += count
                print(f"  {fname}: {count} images")

    print(f"\nTotal: {total_images} images from {file_count} files -> {output_dir}")


if __name__ == "__main__":
    main()

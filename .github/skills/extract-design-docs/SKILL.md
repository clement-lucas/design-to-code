---
name: extract-design-docs
description: "Extract text, tables, images, and DrawingML diagrams from Office design documents (.xlsx, .docx). Use when: read design docs, extract specifications, parse Excel specs, extract images from documents, DrawingML extraction, design document analysis."
---

# Extract Design Documents

Extract structured content from Japanese design documents (Office files) for downstream code generation.

## When to Use

- Starting a design-to-code workflow
- Analyzing `.xlsx` or `.docx` design specification files
- Extracting screen mockups, ER diagrams, or architectural diagrams from Office documents

## Prerequisites

- Python 3.x installed and available in PATH
- The skill will auto-install required packages: `openpyxl`, `python-docx`, `Pillow`

## Procedure

### Step 1: Discover Design Documents

Search the workspace for design document directories. Standard Japanese project structure:

```
design-docs/
  010_要件定義/      (Requirements)
  020_方式設計/      (Architecture)
  030_アプリ設計/    (Application Design)
  080_ツール/        (Tools)
  100_レビュー記録/  (Review Records)
```

List all `.xlsx` and `.docx` files recursively. Create a catalog showing:
- File path
- Document category (from folder name)
- Document ID (from filename, e.g., WA10101, BA10601, N21AA01)

### Step 2: Extract Text Content

Run the [text extraction script](./scripts/extract_text.py) against the design documents directory:

```
python extract_text.py <input_dir> <output_file>
```

This extracts all text from every sheet/page of every `.xlsx` and `.docx` file into a single consolidated text file. Each document is delimited with clear headers showing the source file and sheet name.

The output format is:
```
========================================
FILE: path/to/document.xlsx
SHEET: シート名
========================================
[cell contents in row/column order]
```

### Step 3: Extract Images

Run the [image extraction script](./scripts/extract_images.py) against the design documents directory:

```
python extract_images.py <input_dir> <output_dir>
```

This extracts embedded images (screen mockups, ER diagrams, flow charts) from Office documents. Images are saved with descriptive filenames: `{document_name}_{sheet}_{index}.{ext}`

After extraction, view key images to understand:
- Screen layouts and UI component placement
- ER diagram relationships
- Screen transition flows

### Step 4: Extract DrawingML

Run the [DrawingML extraction script](./scripts/extract_drawingml.py) against the design documents directory:

```
python extract_drawingml.py <input_dir> <output_file>
```

Office documents often contain architectural diagrams as DrawingML (XML-based vector graphics) embedded in `xl/drawings/` within the `.xlsx` ZIP structure. These contain:
- Process flow diagrams
- Screen transition diagrams
- System architecture diagrams

The script extracts text content from DrawingML shapes, preserving the relationships between elements.

### Step 5: Catalog and Summarize

After extraction, create a document catalog mapping each design document to its role:

| Doc ID Pattern | Type | Content |
|---------------|------|---------|
| WA1xxxx | Screen spec | Individual screen behavior, fields, validations, events |
| BA1xxxx | Batch spec | Batch job input/output, processing logic, scheduling |
| N21AAxx | Interface spec | File format definitions, import/export layouts |

Summarize key findings:
- Total number of screens, batch jobs, interfaces
- Database table count and key relationships
- Authentication/authorization requirements
- Message and code master definitions

## Output

After this skill completes, the following artifacts should exist:
- `docs_output.txt` — Consolidated text from all design documents
- `images_output/` — Extracted images organized by source document
- `drawings_output.txt` — DrawingML text content
- A mental model of the system ready for code generation

## Tips

- Japanese text encoding: Always use UTF-8 when reading/writing
- Large documents may have 100+ sheets — extract all of them
- Some sheets contain only formatting or are empty — skip gracefully
- Images in `.xlsx` files are stored in `xl/media/` within the ZIP
- DrawingML shapes are in `xl/drawings/*.xml` within the ZIP

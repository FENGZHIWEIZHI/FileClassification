# File Classifier (文件分类工具)

A Java Swing desktop application that automatically classifies files by their extension into a hierarchical folder structure.

## Features

- **Automatic Classification** - Categorizes files into 19+ primary categories (Video, Audio, Image, Document, Archive, Code, Config, Executable, Font, eBook, Database, Disk Image, 3D/CAD, Certificate, Torrent, Email, Temp, and Others) with detailed secondary subcategories
- **Copy or Move** - Choose to copy files (keeping originals) or move/cut files to the target location
- **Recursive Scanning** - Optionally scan subdirectories of the source folder
- **Dry Run / Preview** - Preview the classification results before executing any file operations
- **Duplicate Handling** - Automatically resolves filename conflicts by appending a numeric suffix
- **Progress Tracking** - Shows real-time progress during scanning and file operations
- **Sortable Table** - Results displayed in a sortable table showing filename, extension, primary category, secondary category, and target path

## Requirements

- Java 17 or later
- Any operating system with Java support (Windows, macOS, Linux)

## Build & Run

### Compile

```
javac -d out src/com/fileclassifier/FileCategory.java src/com/fileclassifier/FileClassifierApp.java
```

### Run

```
java -cp out com.fileclassifier.FileClassifierApp
```

Or simply:

```
javac -d out src/com/fileclassifier/*.java && java -cp out com.fileclassifier.FileClassifierApp
```

## Usage

1. **Select Source Folder** - Click "Browse..." next to the source folder field to choose the folder containing files to classify
2. **Select Target Folder** - Choose where classified folders should be created (defaults to the source folder)
3. **Configure Options**:
   - Check or uncheck "包含子文件夹" (Include subfolders) to enable or disable recursive scanning
   - Select "复制（保留源文件）" (Copy - keep original files) or "剪切（移动文件）" (Cut - move files) for the operation mode
   - Check "仅预览(不实际移动)" (Preview only) to see results without modifying any files
4. **Click "扫描文件" (Scan Files)** - Scans the source folder and displays the classification results
5. **Review Results** - Inspect the table to verify the categorization
6. **Click "执行分类" (Execute Classification)** - Performs the actual file copy or move operations

## Classification System

The application uses a two-level classification hierarchy based on file extensions:

| Primary Category | Example Subcategories |
|-----------------|----------------------|
| Video Files | MP4, AVI, MKV, MOV, WMV, FLV, WebM, MPEG, 3GP, RMVB |
| Audio Files | MP3, WAV, FLAC, AAC, OGG, WMA, M4A, APE, MIDI, AMR |
| Image Files | JPG, PNG, GIF, BMP, SVG, WebP, ICO, TIFF, PSD, RAW, HEIC |
| Document Files | PDF, Word, Excel, PPT, TXT, RTF, OpenOffice |
| Archive Files | ZIP, RAR, 7Z, TAR, GZ, BZ2, XZ, ISO |
| Code Files | Java, Python, JavaScript, TypeScript, HTML, CSS, C/C++, C#, Go, Rust, Ruby, PHP, SQL, Shell, Swift, Kotlin |
| Config Files | JSON, XML, YAML, INI, Properties, TOML, ENV |
| Executable Files | Windows EXE/MSI, Linux bin/deb/rpm, Mac app/dmg/pkg, Android APK, DLL/SO libraries |
| Font Files | TTF, OTF, WOFF/WOFF2 |
| eBook Files | EPUB, MOBI/AZW, DJVU |
| Database Files | SQLite, Access, Backup files |
| Disk Images | ISO/CUE/BIN, VMDK/VDI/VHD |
| 3D/CAD Files | STL, OBJ, FBX, Blender, 3DS, STEP, IGES, DWG/DXF |
| Certificate/Key Files | PEM, DER, Key files, PKCS, GPG |
| Torrent Files | BT torrent, Magnet links |
| Email Files | EML, MSG, PST/OST, MBOX |
| Temp Files | TMP, Cache, Thumbnails, Lock files |
| Other Files | Uncategorized, Files without extensions |

Files with unrecognized extensions are placed in the "其他文件/未分类" (Other/Uncategorized) folder.

## Project Structure

```
FileClassification/
├── src/
│   └── com/
│       └── fileclassifier/
│           ├── FileCategory.java      # Enum-based classification system with all categories and extension mappings
│           └── FileClassifierApp.java # Swing GUI application with scanning and file operation logic
├── README.md
└── .gitignore
```

## License

This project is provided as-is. Feel free to use and modify it.
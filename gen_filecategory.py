#!/usr/bin/env python3
"""Generate FileCategory.java with clean content."""
import pathlib

java = r"""package com.fileclassifier;

import java.util.*;

public enum FileCategory {

    VIDEO("视频文件", Map.ofEntries(
        Map.entry("MP4视频", Set.of("mp4", "m4v")),
        Map.entry("AVI视频", Set.of("avi")),
        Map.entry("MKV视频", Set.of("mkv")),
        Map.entry("MOV视频", Set.of("mov", "qt")),
        Map.entry("WMV视频", Set.of("wmv", "asf")),
        Map.entry("FLV视频", Set.of("flv", "f4v")),
        Map.entry("WebM视频", Set.of("webm")),
        Map.entry("MPEG视频", Set.of("mpeg", "mpg", "mpe", "mts", "m2ts", "ts", "vob")),
        Map.entry("3GP视频", Set.of("3gp", "3g2")),
        Map.entry("RMVB视频", Set.of("rmvb", "rm")),
        Map.entry("其他视频", Set.of("ogv", "divx", "xvid", "h264", "hevc"))
    )),

    AUDIO("音频文件", Map.ofEntries(
        Map.entry("MP3音频", Set.of("mp3")),
        Map.entry("WAV音频", Set.of("wav", "wave")),
        Map.entry("FLAC音频", Set.of("flac")),
        Map.entry("AAC音频", Set.of("aac")),
        Map.entry("OGG音频", Set.of("ogg", "oga", "opus")),
        Map.entry("WMA音频", Set.of("wma")),
        Map.entry("M4A音频", Set.of("m4a", "aiff", "aif")),
        Map.entry("APE音频", Set.of("ape")),
        Map.entry("MIDI音频", Set.of("mid", "midi")),
        Map.entry("AMR音频", Set.of("amr")),
        Map.entry("其他音频", Set.of("ac3", "dts", "ra", "voc", "au", "caf"))
    )),

    IMAGE("图片文件", Map.ofEntries(
        Map.entry("JPG图片", Set.of("jpg", "jpeg", "jpe", "jfif")),
        Map.entry("PNG图片", Set.of("png")),
        Map.entry("GIF图片", Set.of("gif")),
        Map.entry("BMP图片", Set.of("bmp", "dib")),
        Map.entry("SVG图片", Set.of("svg", "svgz")),
        Map.entry("WebP图片", Set.of("webp")),
        Map.entry("ICO图标", Set.of("ico", "cur")),
        Map.entry("TIFF图片", Set.of("tiff", "tif")),
        Map.entry("PSD图片", Set.of("psd", "psb")),
        Map.entry("RAW图片", Set.of("raw", "cr2", "nef", "arw", "dng", "orf", "rw2", "pef", "raf", "srf")),
        Map.entry("HEIC图片", Set.of("heic", "heif")),
        Map.entry("其他图片", Set.of("exr", "hdr", "pcx", "tga", "ai", "eps", "cdr"))
    )),

    DOCUMENT("文档文件", Map.ofEntries(
        Map.entry("PDF文档", Set.of("pdf")),
        Map.entry("Word文档", Set.of("doc", "docx", "docm", "dot", "dotx", "wps", "wpt")),
        Map.entry("Excel表格", Set.of("xls", "xlsx", "xlsm", "xlt", "xltx", "csv", "tsv", "et", "ett")),
        Map.entry("PPT演示", Set.of("ppt", "pptx", "pptm", "pot", "potx", "pps", "ppsx", "dps", "dpt")),
        Map.entry("TXT文本", Set.of("txt", "text", "log", "md", "markdown", "rst", "nfo", "readme")),
        Map.entry("RTF文档", Set.of("rtf")),
        Map.entry("OpenOffice文档", Set.of("odt", "ods", "odp", "odg", "odf")),
        Map.entry("其他文档", Set.of("pages", "numbers", "key", "latex", "tex", "wpd"))
    )),

    ARCHIVE("压缩文件", Map.ofEntries(
        Map.entry("ZIP压缩", Set.of("zip", "zipx", "jar", "war", "ear", "apk", "ipa", "cbz")),
        Map.entry("RAR压缩", Set.of("rar", "rev", "cbr")),
        Map.entry("7Z压缩", Set.of("7z")),
        Map.entry("TAR压缩", Set.of("tar", "tgz", "tbz2", "txz", "tlz")),
        Map.entry("GZ压缩", Set.of("gz", "gzip")),
        Map.entry("BZ2压缩", Set.of("bz2", "bzip2")),
        Map.entry("XZ压缩", Set.of("xz")),
        Map.entry("ISO镜像", Set.of("iso", "nrg", "mdf", "mds", "cue", "bin")),
        Map.entry("其他压缩", Set.of("lz", "lzma", "z", "ace", "arc", "uue"))
    )),

    CODE("代码文件", Map.ofEntries(
        Map.entry("Java代码", Set.of("java", "class")),
        Map.entry("Python代码", Set.of("py", "pyc", "pyo", "pyd", "pyw", "ipynb")),
        Map.entry("JavaScript代码", Set.of("js", "jsx", "mjs", "cjs")),
        Map.entry("TypeScript代码", Set.of("ts", "tsx")),
        Map.entry("HTML文件", Set.of("html", "htm", "xhtml", "shtml")),
        Map.entry("CSS样式", Set.of("css", "scss", "sass", "less", "styl")),
        Map.entry("C/C++代码", Set.of("c", "cpp", "cxx", "cc", "h", "hpp", "hxx")),
        Map.entry("C#代码", Set.of("cs", "csproj", "sln")),
        Map.entry("Go代码", Set.of("go")),
        Map.entry("Rust代码", Set.of("rs")),
        Map.entry("Ruby代码", Set.of("rb", "rake", "gemspec")),
        Map.entry("PHP代码", Set.of("php", "phtml", "php3", "php4", "php5")),
        Map.entry("SQL代码", Set.of("sql", "psql", "tsql")),
        Map.entry("Shell脚本", Set.of("sh", "bash", "zsh", "fish", "bat", "cmd", "ps1", "psm1")),
        Map.entry("Swift代码", Set.of("swift")),
        Map.entry("Kotlin代码", Set.of("kt", "kts")),
        Map.entry("其他代码", Set.of("scala", "lua", "r", "dart", "pl", "pm",
                "groovy", "gradle", "m", "mm", "hs", "lhs", "erl", "hrl", "ex", "exs"))
    )),

    CONFIG("配置文件", Map.ofEntries(
        Map.entry("JSON文件", Set.of("json", "jsonc", "json5")),
        Map.entry("XML文件", Set.of("xml", "xsl", "xsd", "wsdl", "rss", "atom")),
        Map.entry("YAML文件", Set.of("yaml", "yml")),
        Map.entry("INI配置", Set.of("ini", "cfg", "conf", "config")),
        Map.entry("Properties配置", Set.of("properties", "prop")),
        Map.entry("TOML配置", Set.of("toml")),
        Map.entry("ENV配置", Set.of("env", "envrc")),
        Map.entry("其他配置", Set.of("plist", "reg", "inf", "editorconfig", "gitignore", "dockerignore"))
    )),

    EXECUTABLE("可执行文件", Map.ofEntries(
        Map.entry("Windows程序", Set.of("exe", "msi", "msu", "com", "scr", "pif")),
        Map.entry("Linux程序", Set.of("run", "bin", "deb", "rpm", "AppImage")),
        Map.entry("Mac程序", Set.of("app", "dmg", "pkg")),
        Map.entry("Android程序", Set.of("apk", "aab")),
        Map.entry("库文件", Set.of("dll", "so", "dylib", "lib", "a", "ocx")),
        Map.entry("其他可执行", Set.of("bat", "cmd", "ps1", "vbs", "wsf"))
    )),

    FONT("字体文件", Map.ofEntries(
        Map.entry("TrueType字体", Set.of("ttf", "ttc")),
        Map.entry("OpenType字体", Set.of("otf", "otc")),
        Map.entry("Web字体", Set.of("woff", "woff2")),
        Map.entry("其他字体", Set.of("eot", "fon", "fnt", "bdf", "pcf", "pfa", "pfb"))
    )),

    EBOOK("电子书文件", Map.ofEntries(
        Map.entry("EPUB电子书", Set.of("epub")),
        Map.entry("MOBI电子书", Set.of("mobi", "azw", "azw3", "kfx")),
        Map.entry("DJVU电子书", Set.of("djvu", "djv")),
        Map.entry("其他电子书", Set.of("fb2", "lit", "lrf", "prc", "pdb", "pml", "snb", "tcr"))
    )),

    DATABASE("数据库文件", Map.ofEntries(
        Map.entry("SQLite数据库", Set.of("db", "sqlite", "sqlite3", "db3", "s3db")),
        Map.entry("Access数据库", Set.of("mdb", "accdb", "accde", "accdr")),
        Map.entry("备份文件", Set.of("bak", "dump", "sql.gz", "dmp")),
        Map.entry("其他数据库", Set.of("frm", "ibd", "myd", "myi", "ndf", "ldf", "mdf"))
    )),

    DISK_IMAGE("磁盘映像", Map.ofEntries(
        Map.entry("光盘映像", Set.of("iso", "cue", "bin", "mdf", "mds", "nrg", "img", "ccd", "dmg")),
        Map.entry("虚拟磁盘", Set.of("vmdk", "vdi", "vhd", "vhdx", "qcow2", "qcow", "vmem")),
        Map.entry("其他映像", Set.of("ima", "flp", "dsk"))
    )),

    CAD_3D("3D/CAD文件", Map.ofEntries(
        Map.entry("STL模型", Set.of("stl")),
        Map.entry("OBJ模型", Set.of("obj", "mtl")),
        Map.entry("FBX模型", Set.of("fbx")),
        Map.entry("Blender文件", Set.of("blend")),
        Map.entry("3DS模型", Set.of("3ds", "max")),
        Map.entry("STEP模型", Set.of("step", "stp", "stpc")),
        Map.entry("IGES模型", Set.of("iges", "igs")),
        Map.entry("DWG图纸", Set.of("dwg", "dxf")),
        Map.entry("其他3D", Set.of("glb", "gltf", "dae", "amf", "3mf",
                "prt", "asm", "sldprt", "sldasm", "ipt", "iam"))
    )),

    CERTIFICATE("证书密钥", Map.ofEntries(
        Map.entry("PEM证书", Set.of("pem", "crt", "cert", "ca-bundle")),
        Map.entry("DER证书", Set.of("der", "cer", "csr")),
        Map.entry("密钥文件", Set.of("key", "pub", "ppk")),
        Map.entry("PKCS证书", Set.of("p12", "pfx", "p7b", "p7c", "keystore", "jks")),
        Map.entry("其他证书", Set.of("gpg", "asc", "sig"))
    )),

    TORRENT("种子文件", Map.ofEntries(
        Map.entry("BT种子", Set.of("torrent")),
        Map.entry("磁力链接", Set.of("magnet"))
    )),

    EMAIL("邮件文件", Map.ofEntries(
        Map.entry("EML邮件", Set.of("eml", "emlx")),
        Map.entry("MSG邮件", Set.of("msg")),
        Map.entry("邮箱数据", Set.of("pst", "ost", "mbox")),
        Map.entry("其他邮件", Set.of("mbx", "dat"))
    )),

    TEMP("临时文件", Map.ofEntries(
        Map.entry("临时文件", Set.of("tmp", "temp", "swp", "swo")),
        Map.entry("缓存文件", Set.of("cache", "cch")),
        Map.entry("缩略图", Set.of("thumb", "thumbs", "db-wal", "db-shm")),
        Map.entry("其他临时", Set.of("pid", "lock", "lck"))
    )),

    OTHER("其他文件", Map.ofEntries(
        Map.entry("未分类", Set.of()),
        Map.entry("无后缀文件", Set.of())
    ));

    private final String categoryName;
    private final Map<String, Set<String>> subCategories;
    private final Map<String, String> extensionToSubCategory;

    FileCategory(String categoryName, Map<String, Set<String>> subCategories) {
        this.categoryName = categoryName;
        this.subCategories = Collections.unmodifiableMap(subCategories);
        Map<String, String> builder = new HashMap<>();
        for (var entry : subCategories.entrySet()) {
            for (String ext : entry.getValue()) {
                builder.put(ext.toLowerCase(), entry.getKey());
            }
        }
        this.extensionToSubCategory = Collections.unmodifiableMap(builder);
    }

    public String getCategoryName() { return categoryName; }

    public Map<String, Set<String>> getSubCategories() { return subCategories; }

    public String getSubCategory(String extension) {
        return extensionToSubCategory.get(extension.toLowerCase());
    }

    public static CategoryResult classify(String extension) {
        if (extension == null || extension.isEmpty()) {
            return new CategoryResult(OTHER, "无后缀文件");
        }
        String ext = extension.toLowerCase();
        for (FileCategory category : values()) {
            if (category == OTHER) continue;
            String subCategory = category.getSubCategory(ext);
            if (subCategory != null) {
                return new CategoryResult(category, subCategory);
            }
        }
        return new CategoryResult(OTHER, "未分类");
    }

    public record CategoryResult(FileCategory category, String subCategory) {}
}
"""

out = pathlib.Path("src/com/fileclassifier/FileCategory.java")
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(java, encoding="utf-8")
print("Generated successfully:", out)

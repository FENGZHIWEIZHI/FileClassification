package com.fileclassifier;

import java.util.*;

/**
 * Defines the two-level file classification system.
 * Level 1: Main category (e.g., "视频文件")
 * Level 2: Extension groups within that category
 */
public enum FileCategory {

    // ==================== 视频文件 ====================
    VIDEO("视频文件", Map.of(
        "MP4视频", Set.of("mp4", "m4v"),
        "AVI视频", Set.of("avi"),
        "MKV视频", Set.of("mkv"),
        "MOV视频", Set.of("mov", "qt"),
        "WMV视频", Set.of("wmv", "asf"),
        "FLV视频", Set.of("flv", "f4v"),
        "WebM视频", Set.of("webm"),
        "MPEG视频", Set.of("mpeg", "mpg", "mpe", "mts", "m2ts", "ts", "vob"),
        "3GP视频", Set.of("3gp", "3g2"),
        "RMVB视频", Set.of("rmvb", "rm"),
        "其他视频", Set.of("ogv", "divx", "xvid", "h264", "hevc")
    )),

    // ==================== 音频文件 ====================
    AUDIO("音频文件", Map.of(
        "MP3音频", Set.of("mp3"),
        "WAV音频", Set.of("wav", "wave"),
        "FLAC音频", Set.of("flac"),
        "AAC音频", Set.of("aac"),
        "OGG音频", Set.of("ogg", "oga", "opus"),
        "WMA音频", Set.of("wma"),
        "M4A音频", Set.of("m4a", "aiff", "aif"),
        "APE音频", Set.of("ape"),
        "MIDI音频", Set.of("mid", "midi"),
        "AMR音频", Set.of("amr"),
        "其他音频", Set.of("ac3", "dts", "ra", "voc", "au", "caf")
    )),

    // ==================== 图片文件 ====================
    IMAGE("图片文件", Map.of(
        "JPG图片", Set.of("jpg", "jpeg", "jpe", "jfif"),
        "PNG图片", Set.of("png"),
        "GIF图片", Set.of("gif"),
        "BMP图片", Set.of("bmp", "dib"),
        "SVG图片", Set.of("svg", "svgz"),
        "WebP图片", Set.of("webp"),
        "ICO图标", Set.of("ico", "cur"),
        "TIFF图片", Set.of("tiff", "tif"),
        "PSD图片", Set.of("psd", "psb"),
        "RAW图片", Set.of("raw", "cr2", "nef", "arw", "dng", "orf", "rw2", "pef", "raf", "srf"),
        "HEIC图片", Set.of("heic", "heif"),
        "其他图片", Set.of("exr", "hdr", "pcx", "tga", "ai", "eps", "cdr")
    )),

    // ==================== 文档文件 ====================
    DOCUMENT("文档文件", Map.of(
        "PDF文档", Set.of("pdf"),
        "Word文档", Set.of("doc", "docx", "docm", "dot", "dotx", "wps", "wpt"),
        "Excel表格", Set.of("xls", "xlsx", "xlsm", "xlt", "xltx", "csv", "tsv", "et", "ett"),
        "PPT演示", Set.of("ppt", "pptx", "pptm", "pot", "potx", "pps", "ppsx", "dps", "dpt"),
        "TXT文本", Set.of("txt", "text", "log", "md", "markdown", "rst", "nfo", "readme"),
        "RTF文档", Set.of("rtf"),
        "OpenOffice文档", Set.of("odt", "ods", "odp", "odg", "odf"),
        "其他文档", Set.of("pages", "numbers", "key", "latex", "tex", "wpd")
    )),

    // ==================== 压缩文件 ====================
    ARCHIVE("压缩文件", Map.of(
        "ZIP压缩", Set.of("zip", "zipx", "jar", "war", "ear", "apk", "ipa", "cbz"),
        "RAR压缩", Set.of("rar", "rev", "cbr"),
        "7Z压缩", Set.of("7z"),
        "TAR压缩", Set.of("tar", "tgz", "tbz2", "txz", "tlz"),
        "GZ压缩", Set.of("gz", "gzip"),
        "BZ2压缩", Set.of("bz2", "bzip2"),
        "XZ压缩", Set.of("xz"),
        "ISO镜像", Set.of("iso", "nrg", "mdf", "mds", "cue", "bin"),
        "其他压缩", Set.of("lz", "lzma", "z", "ace", "arc", "uue")
    )),

    // ==================== 代码/编程文件 ====================
    CODE("代码文件", Map.of(
        "Java代码", Set.of("java", "class", "jar"),
        "Python代码", Set.of("py", "pyc", "pyo", "pyd", "pyw", "ipynb"),
        "JavaScript代码", Set.of("js", "jsx", "mjs", "cjs"),
        "TypeScript代码", Set.of("ts", "tsx"),
        "HTML文件", Set.of("html", "htm", "xhtml", "shtml"),
        "CSS样式", Set.of("css", "scss", "sass", "less", "styl"),
        "C/C++代码", Set.of("c", "cpp", "cxx", "cc", "h", "hpp", "hxx"),
        "C#代码", Set.of("cs", "csproj", "sln"),
        "Go代码", Set.of("go"),
        "Rust代码", Set.of("rs"),
        "Ruby代码", Set.of("rb", "rake", "gemspec"),
        "PHP代码", Set.of("php", "phtml", "php3", "php4", "php5"),
        "SQL代码", Set.of("sql", "psql", "tsql"),
        "Shell脚本", Set.of("sh", "bash", "zsh", "fish", "bat", "cmd", "ps1", "psm1"),
        "Swift代码", Set.of("swift"),
        "Kotlin代码", Set.of("kt", "kts"),
        "其他代码", Set.of("scala", "lua", "r", "dart", "pl", "pm", "groovy", "gradle", "m", "mm", "hs", "lhs", "erl", "hrl", "ex", "exs")
    )),

    // ==================== 配置文件 ====================
    CONFIG("配置文件", Map.of(
        "JSON文件", Set.of("json", "jsonc", "json5"),
        "XML文件", Set.of("xml", "xsl", "xsd", "wsdl", "rss", "atom"),
        "YAML文件", Set.of("yaml", "yml"),
        "INI配置", Set.of("ini", "cfg", "conf", "config"),
        "Properties配置", Set.of("properties", "prop"),
        "TOML配置", Set.of("toml"),
        "ENV配置", Set.of("env", "envrc"),
        "其他配置", Set.of("plist", "reg", "inf", "editorconfig", "gitignore", "dockerignore")
    )),

    // ==================== 可执行文件 ====================
    EXECUTABLE("可执行文件", Map.of(
        "Windows程序", Set.of("exe", "msi", "msu", "com", "scr", "pif"),
        "Linux程序", Set.of("sh", "run", "bin", "deb", "rpm", "AppImage"),
        "Mac程序", Set.of("app", "dmg", "pkg"),
        "Android程序", Set.of("apk", "aab"),
        "库文件", Set.of("dll", "so", "dylib", "lib", "a", "ocx"),
        "其他可执行", Set.of("bat", "cmd", "ps1", "vbs", "wsf")
    )),

    // ==================== 字体文件 ====================
    FONT("字体文件", Map.of(
        "TrueType字体", Set.of("ttf", "ttc"),
        "OpenType字体", Set.of("otf", "otc"),
        "Web字体", Set.of("woff", "woff2"),
        "其他字体", Set.of("eot", "fon", "fnt", "bdf", "pcf", "pfa", "pfb")
    )),

    // ==================== 电子书 ====================
    EBOOK("电子书文件", Map.of(
        "EPUB电子书", Set.of("epub"),
        "MOBI电子书", Set.of("mobi", "azw", "azw3", "kfx"),
        "DJVU电子书", Set.of("djvu", "djv"),
        "其他电子书", Set.of("fb2", "lit", "lrf", "prc", "pdb", "pml", "snb", "tcr")
    )),

    // ==================== 数据库文件 ====================
    DATABASE("数据库文件", Map.of(
        "SQLite数据库", Set.of("db", "sqlite", "sqlite3", "db3", "s3db"),
        "Access数据库", Set.of("mdb", "accdb", "accde", "accdr"),
        "备份文件", Set.of("bak", "dump", "sql.gz", "dmp"),
        "其他数据库", Set.of("frm", "ibd", "myd", "myi", "ndf", "ldf", "mdf")
    )),

    // ==================== 磁盘映像/虚拟磁盘 ====================
    DISK_IMAGE("磁盘映像", Map.of(
        "光盘映像", Set.of("iso", "cue", "bin", "mdf", "mds", "nrg", "img", "ccd", "dmg"),
        "虚拟磁盘", Set.of("vmdk", "vdi", "vhd", "vhdx", "qcow2", "qcow", "vmem"),
        "其他映像", Set.of("ima", "flp", "dsk")
    )),

    // ==================== 3D/CAD文件 ====================
    CAD_3D("3D/CAD文件", Map.of(
        "STL模型", Set.of("stl"),
        "OBJ模型", Set.of("obj", "mtl"),
        "FBX模型", Set.of("fbx"),
        "Blender文件", Set.of("blend"),
        "3DS模型", Set.of("3ds", "max"),
        "STEP模型", Set.of("step", "stp", "stpc"),
        "IGES模型", Set.of("iges", "igs"),
        "DWG图纸", Set.of("dwg", "dxf"),
        "其他3D", Set.of("glb", "gltf", "dae", "amf", "3mf", "prt", "asm", "sldprt", "sldasm", "ipt", "iam")
    )),

    // ==================== 证书/密钥 ====================
    CERTIFICATE("证书密钥", Map.of(
        "PEM证书", Set.of("pem", "crt", "cert", "ca-bundle"),
        "DER证书", Set.of("der", "cer", "csr"),
        "密钥文件", Set.of("key", "pub", "ppk"),
        "PKCS证书", Set.of("p12", "pfx", "p7b", "p7c", "keystore", "jks"),
        "其他证书", Set.of("pem", "gpg", "asc", "sig")
    )),

    // ==================== 种子文件 ====================
    TORRENT("种子文件", Map.of(
        "BT种子", Set.of("torrent"),
        "磁力链接", Set.of("magnet")
    )),

    // ==================== 邮件文件 ====================
    EMAIL("邮件文件", Map.of(
        "EML邮件", Set.of("eml", "emlx"),
        "MSG邮件", Set.of("msg"),
        "邮箱数据", Set.of("pst", "ost", "mbox"),
        "其他邮件", Set.of("mbx", "dat")
    )),

    // ==================== 临时/缓存文件 ====================
    TEMP("临时文件", Map.of(
        "临时文件", Set.of("tmp", "temp", "swp", "swo", "~"),
        "缓存文件", Set.of("cache", "cch"),
        "缩略图", Set.of("thumb", "thumbs", "db-wal", "db-shm"),
        "其他临时", Set.of("pid", "lock", "lck")
    )),

    // ==================== 其他文件 ====================
    OTHER("其他文件", Map.of(
        "未分类", Set.of()
    ));

    private final String categoryName;
    private final Map<String, Set<String>> subCategories;
    // Reverse map: extension -> subCategory name
    private final Map<String, String> extensionToSubCategory;

    FileCategory(String categoryName, Map<String, Set<String>> subCategories) {
        this.categoryName = categoryName;
        this.subCategories = subCategories;
        this.extensionToSubCategory = new HashMap<>();
        for (var entry : subCategories.entrySet()) {
            for (String ext : entry.getValue()) {
                extensionToSubCategory.put(ext.toLowerCase(), entry.getKey());
            }
        }
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Map<String, Set<String>> getSubCategories() {
        return subCategories;
    }

    /**
     * Get the sub-category name for a given extension
     */
    public String getSubCategory(String extension) {
        return extensionToSubCategory.get(extension.toLowerCase());
    }

    /**
     * Classify a file extension into its category
     */
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

    /**
     * DTO for classification result
     */
    public record CategoryResult(FileCategory category, String subCategory) {
        // e.g., (VIDEO, "MP4视频"), (OTHER, "未分类")
    }
}
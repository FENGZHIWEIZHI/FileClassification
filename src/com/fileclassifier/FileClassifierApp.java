"package com.fileclassifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fileclassifier.FileCategory.CategoryResult;

/**
 * 文件分类工具主界面
 * 支持选择源文件夹和目标文件夹，预览分类并执行移动
 */
public class FileClassifierApp extends JFrame {

    private JTextField sourceFolderField;
    private JButton sourceBrowseButton;
    private JTextField targetFolderField;
    private JButton targetBrowseButton;
    private JButton scanButton;
    private JButton executeButton;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JCheckBox recursiveCheck;
    private JCheckBox dryRunCheck;

    private Map<File, CategoryResult> scannedFiles;
    private File sourceDir;
    private File targetDir;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FileClassifierApp().setVisible(true));
    }

    public FileClassifierApp() {
        super("文件分类工具 - File Classifier");
        initUI();
        setSize(1100, 750);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        // Menu
        JMenuBar mb = new JMenuBar();
        JMenu fm = new JMenu("文件");
        JMenuItem exit = new JMenuItem("退出");
        exit.addActionListener(e -> System.exit(0));
        fm.add(exit);
        mb.add(fm);
        setJMenuBar(mb);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top panel ---
        JPanel top = new JPanel(new BorderLayout(5, 5));

        // Source folder row
        JPanel srcRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        srcRow.add(new JLabel("源文件夹:"));
        sourceFolderField = new JTextField(35);
        srcRow.add(sourceFolderField);
        sourceBrowseButton = new JButton("浏览...");
        sourceBrowseButton.addActionListener(e -> browseFolder(true));
        srcRow.add(sourceBrowseButton);

        // Target folder row
        JPanel tgtRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tgtRow.add(new JLabel("目标文件夹:"));
        targetFolderField = new JTextField(35);
        tgtRow.add(targetFolderField);
        targetBrowseButton = new JButton("浏览...");
        targetBrowseButton.addActionListener(e -> browseFolder(false));
        tgtRow.add(targetBrowseButton);

        JPanel folderGrid = new JPanel(new GridLayout(2, 1, 5, 5));
        folderGrid.add(srcRow);
        folderGrid.add(tgtRow);

        scanButton = new JButton("扫描文件");
        scanButton.addActionListener(this::scanFiles);
        JPanel scanWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scanWrap.add(scanButton);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.add(folderGrid, BorderLayout.CENTER);
        topRow.add(scanWrap, BorderLayout.EAST);

        JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        recursiveCheck = new JCheckBox("包含子文件夹", true);
        dryRunCheck = new JCheckBox("仅预览(不实际移动)", false);
        opts.add(recursiveCheck);
        opts.add(dryRunCheck);

        top.add(topRow, BorderLayout.NORTH);
        top.add(opts, BorderLayout.SOUTH);

        // --- Table ---
        String[] cols = {"文件名", "后缀", "一级分类", "二级分类", "目标路径"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        fileTable = new JTable(tableModel);
        fileTable.setFillsViewportHeight(true);
        fileTable.setAutoCreateRowSorter(true);
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(350);
        JScrollPane scroll = new JScrollPane(fileTable);

        // --- Bottom ---
        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        statsLabel = new JLabel("就绪");
        JPanel statsP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsP.add(statsLabel);
        executeButton = new JButton("执行分类");
        executeButton.setEnabled(false);
        executeButton.addActionListener(this::executeClassification);
        JPanel actP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actP.add(executeButton);
        bottom.add(statsP, BorderLayout.CENTER);
        bottom.add(actP, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        statusLabel = new JLabel(" ");
        JPanel statusP = new JPanel(new BorderLayout(5, 5));
        statusP.add(statusLabel, BorderLayout.NORTH);
        statusP.add(progressBar, BorderLayout.SOUTH);

        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.add(statusP, BorderLayout.NORTH);
        south.add(bottom, BorderLayout.SOUTH);

        main.add(top, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        main.add(south, BorderLayout.SOUTH);
        setContentPane(main);
    }

    // --- Logic ---

    private void browseFolder(boolean isSource) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JTextField field = isSource ? sourceFolderField : targetFolderField;
        fc.setDialogTitle(isSource ? "选择源文件夹" : "选择目标文件夹");
        String current = field.getText().trim();
        if (!current.isEmpty() && new File(current).exists())
            fc.setCurrentDirectory(new File(current));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            field.setText(fc.getSelectedFile().getAbsolutePath());
    }

    private void scanFiles(ActionEvent e) {
        String srcPath = sourceFolderField.getText().trim();
        if (srcPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择源文件夹!");
            return;
        }
        sourceDir = new File(srcPath);
        if (!sourceDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "源文件夹无效!");
            return;
        }
        String tgtPath = targetFolderField.getText().trim();
        if (tgtPath.isEmpty()) {
            targetDir = sourceDir;
            targetFolderField.setText(sourceDir.getAbsolutePath());
        } else {
            targetDir = new File(tgtPath);
        }

        tableModel.setRowCount(0);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("正在扫描文件...");
        setTopEnabled(false);
        executeButton.setEnabled(false);

        boolean recursive = recursiveCheck.isSelected();
        new SwingWorker<Map<File, CategoryResult>, Void>() {
            protected Map<File, CategoryResult> doInBackground() {
                Map<File, CategoryResult> map = new LinkedHashMap<>();
                try {
                    var stream = recursive
                        ? Files.walk(sourceDir.toPath())
                        : sourceDir.toPath().resolve(".").getFileSystem() == null ? null
                            : Files.list(sourceDir.toPath());
                    (recursive
                        ? Files.walk(sourceDir.toPath())
                        : Files.list(sourceDir.toPath()))
                        .filter(Files::isRegularFile)
                        .forEach(p -> {
                            File f = p.toFile();
                            String n = f.getName();
                            int dot = n.lastIndexOf('.');
                            String ext = (dot > 0 && dot < n.length()-1) ? n.substring(dot+1) : "";
                            map.put(f, FileCategory.classify(ext));
                        });
                } catch (IOException ex) { ex.printStackTrace(); }
                return map;
            }
            protected void done() {
                try {
                    scannedFiles = get();
                    refreshTable();
                    statsLabel.setText("扫描完成: " + scannedFiles.size() + " 个文件, "
                        + categoryCount(scannedFiles) + " 个分类");
                    statusLabel.setText("扫描完成");
                    executeButton.setEnabled(!scannedFiles.isEmpty());
                } catch (Exception ex) {
                    statusLabel.setText("出错: " + ex.getMessage());
                    JOptionPane.showMessageDialog(FileClassifierApp.this, "扫描出错: " + ex.getMessage());
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    setTopEnabled(true);
                }
            }
        }.execute();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (var e : scannedFiles.entrySet()) {
            File f = e.getKey();
            CategoryResult r = e.getValue();
            String target = new File(targetDir,
                r.category().getCategoryName() + File.separator
                + r.subCategory() + File.separator
                + f.getName()).getAbsolutePath();
            tableModel.addRow(new Object[]{
                f.getName(), extension(f),
                r.category().getCategoryName(), r.subCategory(), target
            });
        }
    }

    private String extension(File f) {
        String n = f.getName();
        int dot = n.lastIndexOf('.');
        return dot > 0 && dot < n.length()-1 ? n.substring(dot+1) : "(无后缀)";
    }

    private int categoryCount(Map<File, CategoryResult> map) {
        Set<String> s = new HashSet<>();
        for (CategoryResult r : map.values()) s.add(r.category().getCategoryName());
        return s.size();
    }

    private void executeClassification(ActionEvent e) {
        if (scannedFiles == null || scannedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先扫描文件!");
            return;
        }
        boolean dry = dryRunCheck.isSelected();
        String msg = dry ? "仅预览，不实际移动文件。继续?"
            : "文件将移动到目标文件夹并按分类组织。确认?";
        if (JOptionPane.showConfirmDialog(this, msg, "确认", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;

        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(scannedFiles.size());
        statusLabel.setText(dry ? "预览中..." : "正在移动文件...");
        setTopEnabled(false);
        executeButton.setEnabled(false);

        new SwingWorker<List<String>, Integer>() {
            protected List<String> doInBackground() {
                List<String> errors = new ArrayList<>();
                int i = 0;
                for (var entry : scannedFiles.entrySet()) {
                    try {
                        moveOne(entry.getKey(), entry.getValue(), dry);
                    } catch (IOException ex) {
                        errors.add(entry.getKey().getName() + ": " + ex.getMessage());
                    }
                    publish(++i);
                }
                return errors;
            }
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size()-1));
            }
            protected void done() {
                try {
                    List<String> errors = get();
                    progressBar.setVisible(false);
                    if (errors.isEmpty()) {
                        statusLabel.setText("完成!");
                        JOptionPane.showMessageDialog(FileClassifierApp.this,
                            (dry ? "预览完成" : "分类完成") + "! 共 " + scannedFiles.size() + " 个文件");
                    } else {
                        statusLabel.setText(errors.size() + " 个错误");
                        JOptionPane.showMessageDialog(FileClassifierApp.this,
                            "部分文件失败:\n" + String.join("\n",
                                errors.subList(0, Math.min(10, errors.size()))));
                    }
                } catch (Exception ex) {
                    statusLabel.setText("失败: " + ex.getMessage());
                } finally {
                    setTopEnabled(true);
                    executeButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void moveOne(File file, CategoryResult result, boolean dry) throws IOException {
        Path destDir = targetDir.toPath()
            .resolve(result.category().getCategoryName())
            .resolve(result.subCategory());
        Path destFile = destDir.resolve(file.getName());
        if (!dry) {
            Files.createDirectories(destDir);
            if (Files.exists(destFile)) {
                String base = baseName(file.getName());
                String ext = extName(file.getName());
                int c = 1;
                while (Files.exists(destFile)) {
                    String nn = ext.isEmpty() ? base + "_" + c : base + "_" + c + "." + ext;
                    destFile = destDir.resolve(nn);
                    c++;
                }
            }
            Files.move(file.toPath(), destFile, StandardCopyOption.ATOMIC_MOVE);
        }
    }

    private String baseName(String fn) {
        int d = fn.lastIndexOf('.');
        return d > 0 ? fn.substring(0, d) : fn;
    }

    private String extName(String fn) {
        int d = fn.lastIndexOf('.');
        return (d > 0 && d < fn.length()-1) ? fn.substring(d+1) : "";
    }

    private void setTopEnabled(boolean v) {
        sourceBrowseButton.setEnabled(v);
        targetBrowseButton.setEnabled(v);
        scanButton.setEnabled(v);
    }
}"
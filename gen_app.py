#!/usr/bin/env python3
"""Generate FileClassifierApp.java with source & target folder UI."""
import pathlib

java = r"""package com.fileclassifier;

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

    public FileClassifierApp() {
        super("文件分类工具 - File Classifier");
        initUI();
        setSize(1100, 750);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ---- Top panel: source + target folder selection ----
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // Source folder row
        JPanel sourceRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sourceRow.add(new JLabel("源文件夹:"));
        sourceFolderField = new JTextField(35);
        sourceRow.add(sourceFolderField);
        sourceBrowseButton = new JButton("浏览...");
        sourceBrowseButton.addActionListener(e -> browseFolder(true));
        sourceRow.add(sourceBrowseButton);

        // Target folder row
        JPanel targetRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetRow.add(new JLabel("目标文件夹:"));
        targetFolderField = new JTextField(35);
        targetRow.add(targetFolderField);
        targetBrowseButton = new JButton("浏览...");
        targetBrowseButton.addActionListener(e -> browseFolder(false));
        targetRow.add(targetBrowseButton);

        JPanel folderPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        folderPanel.add(sourceRow);
        folderPanel.add(targetRow);

        scanButton = new JButton("扫描文件");
        scanButton.addActionListener(this::scanFiles);
        JPanel scanWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scanWrapper.add(scanButton);

        JPanel topRowContainer = new JPanel(new BorderLayout());
        topRowContainer.add(folderPanel, BorderLayout.CENTER);
        topRowContainer.add(scanWrapper, BorderLayout.EAST);

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        recursiveCheck = new JCheckBox("包含子文件夹", true);
        dryRunCheck = new JCheckBox("仅预览（不实际移动）", false);
        optionsPanel.add(recursiveCheck);
        optionsPanel.add(dryRunCheck);

        topPanel.add(topRowContainer, BorderLayout.NORTH);
        topPanel.add(optionsPanel, BorderLayout.SOUTH);

        // ---- Center: table ----
        String[] columns = {"文件名", "后缀", "一级分类", "二级分类", "目标路径"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        fileTable = new JTable(tableModel);
        fileTable.setFillsViewportHeight(true);
        fileTable.setAutoCreateRowSorter(true);
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(350);
        JScrollPane scrollPane = new JScrollPane(fileTable);

        // ---- Bottom ----
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsLabel = new JLabel("就绪");
        statsPanel.add(statsLabel);
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        executeButton = new JButton("执行分类");
        executeButton.setEnabled(false);
        executeButton.addActionListener(this::executeClassification);
        actionPanel.add(executeButton);
        bottomPanel.add(statsPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        statusLabel = new JLabel(" ");
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(progressBar, BorderLayout.SOUTH);

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.add(statusPanel, BorderLayout.NORTH);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void browseFolder(boolean isSource) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JTextField field = isSource ? sourceFolderField : targetFolderField;
        chooser.setDialogTitle(isSource ? "选择源文件夹" : "选择目标文件夹");
        if (!field.getText().isEmpty()) {
            File f = new File(field.getText());
            if (f.exists()) chooser.setCurrentDirectory(f);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void scanFiles(ActionEvent e) {
        String srcPath = sourceFolderField.getText().trim();
        if (srcPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择源文件夹！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        sourceDir = new File(srcPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "源文件夹无效！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String tgtPath = targetFolderField.getText().trim();
        if (tgtPath.isEmpty()) {
            targetDir = sourceDir;
            targetFolderField.setText(sourceDir.getAbsolutePath());
        } else {
            targetDir = new File(tgtPath);
        }

        boolean recursive = recursiveCheck.isSelected();
        tableModel.setRowCount(0);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("正在扫描文件...");
        setControlsEnabled(false);
        executeButton.setEnabled(false);

        new SwingWorker<Map<File, CategoryResult>, Void>() {
            @Override
            protected Map<File, CategoryResult> doInBackground() {
                return scanDirectory(sourceDir, recursive);
            }
            @Override
            protected void done() {
                try {
                    scannedFiles = get();
                    populateTable(scannedFiles);
                    statsLabel.setText(String.format("扫描完成：共 %d 个文件，%d 个一级分类",
                            scannedFiles.size(), getCategoryCount(scannedFiles)));
                    statusLabel.setText("扫描完成");
                    executeButton.setEnabled(!scannedFiles.isEmpty());
                } catch (Exception ex) {
                    statusLabel.setText("扫描出错: " + ex.getMessage());
                    JOptionPane.showMessageDialog(FileClassifierApp.this,
                            "扫描错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    setControlsEnabled(true);
                }
            }
        }.execute();
    }

    private Map<File, CategoryResult> scanDirectory(File root, boolean recursive) {
        Map<File, CategoryResult> results = new LinkedHashMap<>();
        try {
            if (recursive) {
                Files.walk(root.toPath()).filter(Files::isRegularFile)
                        .forEach(p -> classifyFile(p.toFile(), results));
            } else {
                File[] files = root.listFiles(File::isFile);
                if (files != null) for (File f : files) classifyFile(f, results);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return results;
    }

    private void classifyFile(File file, Map<File, CategoryResult> results) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        String ext = (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
        results.put(file, FileCategory.classify(ext));
    }

    private void populateTable(Map<File, CategoryResult> files) {
        tableModel.setRowCount(0);
        for (var e : files.entrySet()) {
            File f = e.getKey();
            CategoryResult r = e.getValue();
            String targetPath = buildTargetPath(targetDir, f, r);
            tableModel.addRow(new Object[]{
                f.getName(), getExtension(f),
                r.category().getCategoryName(), r.subCategory(),
                targetPath
            });
        }
    }

    private String getExtension(File file) {
        String n = file.getName();
        int dot = n.lastIndexOf('.');
        return (dot > 0 && dot < n.length() - 1) ? n.substring(dot + 1) : "（无后缀）";
    }

    private String buildTargetPath(File root, File file, CategoryResult result) {
        String cat = result.category().getCategoryName();
        String sub = result.subCategory();
        return new File(root, cat + File.separator + sub + File.separator + file.getName()).getAbsolutePath();
    }

    private int getCategoryCount(Map<File, CategoryResult> files) {
        Set<String> cats = new HashSet<>();
        for (CategoryResult r : files.values()) cats.add(r.category().getCategoryName());
        return cats.size();
    }

    private void executeClassification(ActionEvent e) {
        if (scannedFiles == null || scannedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先扫描文件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean dryRun = dryRunCheck.isSelected();
        String msg = dryRun ? "仅预览分类，不实际移动文件。继续？"
                : "文件将移动到目标文件夹并按分类组织。确认？";
        if (JOptionPane.showConfirmDialog(this, msg, "确认", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(scannedFiles.size());
        statusLabel.setText(dryRun ? "预览中..." : "正在移动文件...");
        setControlsEnabled(false);
        executeButton.setEnabled(false);

        new SwingWorker<List<String>, Integer>() {
            @Override
            protected List<String> doInBackground() {
                List<String> errors = new ArrayList<>();
                int cnt = 0;
                for (var entry : scannedFiles.entrySet()) {
                    try {
                        executeMove(entry.getKey(), entry.getValue(), dryRun);
                    } catch (IOException ex) {
                        errors.add(entry.getKey().getName() + ": " + ex.getMessage());
                    }
                    cnt++;
                    publish(cnt);
                }
                return errors;
            }
            @Override
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }
            @Override
            protected void done() {
                try {
                    List<String> errors = get();
                    progressBar.setVisible(false);
                    if (errors.isEmpty()) {
                        statusLabel.setText("操作完成！");
                        JOptionPane.showMessageDialog(FileClassifierApp.this,
                                (dryRun ? "预览完成。" : "文件分类完成！") + "\n处理 " + scannedFiles.size() + " 个文件。");
                    } else {
                        statusLabel.setText("有 " + errors.size() + " 个文件出错");
                        JOptionPane.showMessageDialog(FileClassifierApp.this,
                                "部分文件出错：\n" + String.join("\n", errors.subList(0, Math.min(10, errors.size()))));
                    }
                } catch (Exception ex) {
                    statusLabel.setText("操作失败: " + ex.getMessage());
                } finally {
                    setControlsEnabled(true);
                    executeButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void executeMove(File file, CategoryResult result, boolean dryRun) throws IOException {
        String catName = result.category().getCategoryName();
        String subName = result.subCategory();
        Path destDir = targetDir.toPath().resolve(catName).resolve(subName);
        Path destFile = destDir.resolve(file.getName());
        if (!dryRun) {
            Files.createDirectories(destDir);
            if (Files.exists(destFile)) {
                String base = getBaseName(file.getName());
                String ext = getExt(file.getName());
                int counter = 1;
                while (Files.exists(destFile)) {
                    String newName = ext.isEmpty() ? base + "_" + counter : base + "_" + counter + "." + ext;
                    destFile = destDir.resolve(newName);
                    counter++;
                }
            }
            Files.move(file.toPath(), destFile, StandardCopyOption.ATOMIC_MOVE);
        }
    }

    private String getBaseName(String fn) {
        int dot = fn.lastIndexOf('.');
        return dot > 0 ? fn.substring(0, dot) : fn;
    }

    private String getExt(String fn) {
        int dot = fn.lastIndexOf('.');
        return (dot > 0 && dot < fn.length() - 1) ? fn.substring(dot + 1) : "";
    }

    private void setControlsEnabled(boolean enabled) {
        sourceBrowseButton.setEnabled(enabled);
        targetBrowseButton.setEnabled(enabled);
        scanButton.setEnabled(enabled);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FileClassifierApp().setVisible(true));
    }
}
"""

out = pathlib.Path("src/com/fileclassifier/FileClassifierApp.java")
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(java, encoding="utf-8")
print("Generated", out)

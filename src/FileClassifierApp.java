package com.fileclassifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fileclassifier.FileCategory.CategoryResult;

/**
 * Main application window for File Classifier.
 * Provides a GUI to select a folder, preview classification, and execute file organization.
 */
public class FileClassifierApp extends JFrame {

    private JTextField sourceFolderField;
    private JButton browseButton;
    private JButton scanButton;
    private JButton executeButton;
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JCheckBox recursiveCheck;
    private JCheckBox dryRunCheck;

    // Scanned files classification result: File -> (Category, SubCategory)
    private Map<File, CategoryResult> scannedFiles;
    // Source directory
    private File sourceDir;

    public FileClassifierApp() {
        super("文件分类工具 - File Classifier");
        initUI();
        setSize(1100, 750);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel: source folder selection
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        JPanel folderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        folderPanel.add(new JLabel("源文件夹:"));
        sourceFolderField = new JTextField(35);
        sourceFolderField.setEditable(true);
        folderPanel.add(sourceFolderField);
        browseButton = new JButton("浏览...");
        browseButton.addActionListener(this::browseFolder);
        folderPanel.add(browseButton);

        scanButton = new JButton("扫描文件");
        scanButton.addActionListener(this::scanFiles);
        folderPanel.add(scanButton);

        // Options
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        recursiveCheck = new JCheckBox("包含子文件夹", true);
        dryRunCheck = new JCheckBox("仅预览（不实际移动）", false);
        optionsPanel.add(recursiveCheck);
        optionsPanel.add(dryRunCheck);

        topPanel.add(folderPanel, BorderLayout.NORTH);
        topPanel.add(optionsPanel, BorderLayout.SOUTH);

        // Center panel: table
        String[] columns = {"文件名", "后缀", "一级分类", "二级分类", "目标路径"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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

        // Bottom panel: buttons and stats
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

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusLabel = new JLabel(" ");
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

    private void browseFolder(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("选择要分类的文件夹");
        if (sourceFolderField.getText() != null && !sourceFolderField.getText().isEmpty()) {
            File current = new File(sourceFolderField.getText());
            if (current.exists()) {
                chooser.setCurrentDirectory(current);
            }
        }
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            sourceFolderField.setText(selected.getAbsolutePath());
        }
    }

    private void scanFiles(ActionEvent e) {
        String folderPath = sourceFolderField.getText().trim();
        if (folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择源文件夹！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        sourceDir = new File(folderPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "指定的文件夹不存在或不是有效目录！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean recursive = recursiveCheck.isSelected();
        tableModel.setRowCount(0);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("正在扫描文件...");
        scanButton.setEnabled(false);
        browseButton.setEnabled(false);
        executeButton.setEnabled(false);

        SwingWorker<Map<File, CategoryResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<File, CategoryResult> doInBackground() {
                return scanDirectory(sourceDir, recursive);
            }

            @Override
            protected void done() {
                try {
                    scannedFiles = get();
                    populateTable(scannedFiles);
                    String stats = String.format("扫描完成：共 %d 个文件，可分为 %d 个一级分类",
                            scannedFiles.size(), getCategoryCount(scannedFiles));
                    statsLabel.setText(stats);
                    statusLabel.setText("扫描完成");
                    executeButton.setEnabled(!scannedFiles.isEmpty());
                } catch (InterruptedException | ExecutionException ex) {
                    statusLabel.setText("扫描出错: " + ex.getMessage());
                    JOptionPane.showMessageDialog(FileClassifierApp.this,
                            "扫描文件时发生错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    scanButton.setEnabled(true);
                    browseButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private Map<File, CategoryResult> scanDirectory(File root, boolean recursive) {
        Map<File, CategoryResult> results = new LinkedHashMap<>();
        try {
            if (recursive) {
                Files.walk(root.toPath())
                        .filter(Files::isRegularFile)
                        .forEach(p -> classifyFile(p.toFile(), results));
            } else {
                File[] files = root.listFiles(File::isFile);
                if (files != null) {
                    for (File f : files) {
                        classifyFile(f, results);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return results;
    }

    private void classifyFile(File file, Map<File, CategoryResult> results) {
        String name = file.getName();
        int dotIdx = name.lastIndexOf('.');
        String extension = (dotIdx >= 0 && dotIdx < name.length() - 1) ? name.substring(dotIdx + 1) : "";
        CategoryResult result = FileCategory.classify(extension);
        results.put(file, result);
    }

    private void populateTable(Map<File, CategoryResult> files) {
        tableModel.setRowCount(0);
        for (var entry : files.entrySet()) {
            File file = entry.getKey();
            CategoryResult result = entry.getValue();
            String fileName = file.getName();
            String ext = result.category().getCategoryName().equals("其他文件") ? getExtension(file) : getExtension(file);
            String targetPath = sourceDir != null ? buildTargetPath(sourceDir, file, result) : "";
            tableModel.addRow(new Object[]{
                    fileName,
                    getExtension(file),
                    result.category().getCategoryName(),
                    result.subCategory(),
                    targetPath
            });
        }
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx >= 0 && dotIdx < name.length() - 1) {
            return name.substring(dotIdx + 1);
        }
        return "（无后缀）";
    }

    private String buildTargetPath(File rootDir, File file, CategoryResult result) {
        String categoryDir = result.category().getCategoryName();
        String subDir = result.subCategory();
        // For OTHER category, don't add subcategory if it's "未分类"
        if (result.category() == FileCategory.OTHER) {
            return new File(rootDir, categoryDir + File.separator + subDir + File.separator + file.getName()).getAbsolutePath();
        }
        return new File(rootDir, categoryDir + File.separator + subDir + File.separator + file.getName()).getAbsolutePath();
    }

    private int getCategoryCount(Map<File, CategoryResult> files) {
        Set<String> categories = new HashSet<>();
        for (CategoryResult result : files.values()) {
            categories.add(result.category().getCategoryName());
        }
        return categories.size();
    }

    private void executeClassification(ActionEvent e) {
        if (scannedFiles == null || scannedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先扫描文件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean dryRun = dryRunCheck.isSelected();
        String message = dryRun ? "将预览分类操作，不实际移动文件。继续？"
                : "将在源文件夹内创建分类目录并移动文件。确认继续？";
        int confirm = JOptionPane.showConfirmDialog(this, message, "确认操作", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(scannedFiles.size());
        statusLabel.setText(dryRun ? "预览中..." : "正在移动文件...");
        executeButton.setEnabled(false);
        scanButton.setEnabled(false);
        browseButton.setEnabled(false);

        SwingWorker<List<String>, Integer> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                List<String> errors = new ArrayList<>();
                int processed = 0;
                for (var entry : scannedFiles.entrySet()) {
                    File file = entry.getKey();
                    CategoryResult result = entry.getValue();
                    try {
                        moveFile(file, result, dryRun);
                    } catch (IOException ex) {
                        errors.add(file.getName() + ": " + ex.getMessage());
                    }
                    processed++;
                    publish(processed);
                    if (isCancelled()) break;
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
                        if (!dryRun) {
                            JOptionPane.showMessageDialog(FileClassifierApp.this,
                                    "文件分类完成！\n共处理 " + scannedFiles.size() + " 个文件。");
                        } else {
                            JOptionPane.showMessageDialog(FileClassifierApp.this,
                                    "预览完成。\n将处理 " + scannedFiles.size() + " 个文件。");
                        }
                    } else {
                        statusLabel.setText("操作完成，但有错误");
                        JOptionPane.showMessageDialog(FileClassifierApp.this,
                                "部分文件处理失败：\n" + String.join("\n", errors.subList(0, Math.min(10, errors.size()))));
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    statusLabel.setText("操作失败: " + ex.getMessage());
                } finally {
                    executeButton.setEnabled(true);
                    scanButton.setEnabled(true);
                    browseButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void moveFile(File file, CategoryResult result, boolean dryRun) throws IOException {
        if (sourceDir == null) return;

        String categoryDirName = result.category().getCategoryName();
        String subDirName = result.subCategory();

        Path targetDir = sourceDir.toPath()
                .resolve(categoryDirName)
                .resolve(subDirName);

        if (!dryRun) {
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(file.getName());
            // Handle duplicate file names
            if (Files.exists(targetFile)) {
                String baseName = getBaseName(file.getName());
                String extension = getExtensionFromName(file.getName());
                int counter = 1;
                while (Files.exists(targetFile)) {
                    String newName = extension.isEmpty()
                            ? baseName + "_" + counter
                            : baseName + "_" + counter + "." + extension;
                    targetFile = targetDir.resolve(newName);
                    counter++;
                }
            }
            Files.move(file.toPath(), targetFile, StandardCopyOption.ATOMIC_MOVE);
        } else {
            // Dry run: just check if directory would be created
            if (Files.exists(targetDir) && Files.exists(targetDir.resolve(file.getName()))) {
                // Would need rename
            }
        }
    }

    private String getBaseName(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        return (dotIdx > 0) ? fileName.substring(0, dotIdx) : fileName;
    }

    private String getExtensionFromName(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        return (dotIdx > 0 && dotIdx < fileName.length() - 1) ? fileName.substring(dotIdx + 1) : "";
    }

    public static void main(String[] args) {
        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            FileClassifierApp app = new FileClassifierApp();
            app.setVisible(true);
        });
    }
}
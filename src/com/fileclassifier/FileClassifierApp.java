package com.fileclassifier;

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
    private JRadioButton copyModeRadio;
    private JRadioButton moveModeRadio;

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
        JMenuBar mb = new JMenuBar();
        JMenu fm = new JMenu("文件");
        JMenuItem exit = new JMenuItem("退出");
        exit.addActionListener(e -> System.exit(0));
        fm.add(exit);
        mb.add(fm);
        setJMenuBar(mb);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout(5, 5));

        JPanel srcRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        srcRow.add(new JLabel("源文件夹:"));
        sourceFolderField = new JTextField(35);
        srcRow.add(sourceFolderField);
        sourceBrowseButton = new JButton("浏览...");
        sourceBrowseButton.addActionListener(e -> browseFolder(true));
        srcRow.add(sourceBrowseButton);

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

        // 复制/移动 单选按钮
        ButtonGroup modeGroup = new ButtonGroup();
        copyModeRadio = new JRadioButton("复制（保留源文件）", true);
        moveModeRadio = new JRadioButton("剪切（移动文件）", false);
        modeGroup.add(copyModeRadio);
        modeGroup.add(moveModeRadio);
        opts.add(new JLabel("  操作方式:"));
        opts.add(copyModeRadio);
        opts.add(moveModeRadio);

        top.add(topRow, BorderLayout.NORTH);
        top.add(opts, BorderLayout.SOUTH);

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

    private void browseFolder(boolean isSource) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JTextField field = isSource ? sourceFolderField : targetFolderField;
        fc.setDialogTitle(isSource ? "选择源文件夹" : "选择目标文件夹");
        String cur = field.getText().trim();
        if (!cur.isEmpty() && new File(cur).exists()) fc.setCurrentDirectory(new File(cur));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            field.setText(fc.getSelectedFile().getAbsolutePath());
    }

    private void scanFiles(ActionEvent e) {
        String src = sourceFolderField.getText().trim();
        if (src.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择源文件夹!");
            return;
        }
        sourceDir = new File(src);
        if (!sourceDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "源文件夹无效!");
            return;
        }
        String tgt = targetFolderField.getText().trim();
        targetDir = tgt.isEmpty() ? sourceDir : new File(tgt);
        if (tgt.isEmpty()) targetFolderField.setText(sourceDir.getAbsolutePath());

        tableModel.setRowCount(0);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("正在扫描文件...");
        setTopEnabled(false);
        executeButton.setEnabled(false);

        boolean rec = recursiveCheck.isSelected();
        new SwingWorker<Map<File, CategoryResult>, Void>() {
            protected Map<File, CategoryResult> doInBackground() {
                Map<File, CategoryResult> map = new LinkedHashMap<>();
                try {
                    (rec ? Files.walk(sourceDir.toPath()) : Files.list(sourceDir.toPath()))
                            .filter(Files::isRegularFile)
                            .forEach(p -> {
                                File f = p.toFile();
                                String n = f.getName();
                                int dot = n.lastIndexOf('.');
                                String ext = dot>0 && dot<n.length()-1 ? n.substring(dot+1) : "";
                                map.put(f, FileCategory.classify(ext));
                            });
                } catch (IOException ex) { ex.printStackTrace(); }
                return map;
            }
            protected void done() {
                try {
                    scannedFiles = get();
                    tableModel.setRowCount(0);
                    for (var en : scannedFiles.entrySet()) {
                        File f = en.getKey();
                        CategoryResult r = en.getValue();
                        String target = new File(targetDir,
                                r.category().getCategoryName() + "/" + r.subCategory() + "/" + f.getName()).getPath();
                        tableModel.addRow(new Object[]{
                                f.getName(), extension(f),
                                r.category().getCategoryName(), r.subCategory(), target
                        });
                    }
                    statsLabel.setText("扫描: " + scannedFiles.size() + " 文件, "
                            + scannedFiles.values().stream().map(CategoryResult::category).distinct().count() + " 分类");
                    statusLabel.setText("扫描完成");
                    executeButton.setEnabled(!scannedFiles.isEmpty());
                } catch (Exception ex) {
                    statusLabel.setText("出错: " + ex.getMessage());
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(false);
                    setTopEnabled(true);
                }
            }
        }.execute();
    }

    private String extension(File f) {
        String n = f.getName();
        int dot = n.lastIndexOf('.');
        return dot>0 && dot<n.length()-1 ? n.substring(dot+1) : "(无后缀)";
    }

    private void executeClassification(ActionEvent e) {
        if (scannedFiles == null || scannedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先扫描文件!");
            return;
        }
        boolean dry = dryRunCheck.isSelected();
        boolean copyMode = copyModeRadio.isSelected();
        String modeText = copyMode ? "复制（保留源文件）" : "剪切（移动文件）";
        String msg = dry ? "仅预览。继续?" : "将以【" + modeText + "】模式处理文件。确认?";
        if (JOptionPane.showConfirmDialog(this, msg, "确认", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setMaximum(scannedFiles.size());
        statusLabel.setText(dry ? "预览中..." : "正在" + modeText + "...");
        setTopEnabled(false);
        executeButton.setEnabled(false);

        new SwingWorker<java.util.List<String>, Integer>() {
            protected java.util.List<String> doInBackground() {
                java.util.List<String> errors = new ArrayList<>();
                int i = 0;
                for (var en : scannedFiles.entrySet()) {
                    try { processFile(en.getKey(), en.getValue(), dry, copyMode); }
                    catch (IOException ex) { errors.add(en.getKey().getName() + ": " + ex.getMessage()); }
                    publish(++i);
                }
                return errors;
            }
            protected void process(java.util.List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size()-1));
            }
            protected void done() {
                try {
                    java.util.List<String> errors = get();
                    progressBar.setVisible(false);
                    if (errors.isEmpty()) {
                        statusLabel.setText("完成!");
                        JOptionPane.showMessageDialog(FileClassifierApp.this,
                                (dry ? "预览" : "分类") + "完成! " + scannedFiles.size() + " 文件");
                    } else {
                        statusLabel.setText(errors.size() + " 错误");
                        JOptionPane.showMessageDialog(FileClassifierApp.this,
                                "错误:\n" + String.join("\n", errors.subList(0, Math.min(10, errors.size()))));
                    }
                } catch (Exception ex) {
                    statusLabel.setText("失败: " + ex.getMessage());
                } finally { setTopEnabled(true); executeButton.setEnabled(true); }
            }
        }.execute();
    }

    private void processFile(File file, CategoryResult result, boolean dry, boolean copyMode) throws IOException {
        Path destDir = targetDir.toPath().resolve(result.category().getCategoryName()).resolve(result.subCategory());
        Path destFile = destDir.resolve(file.getName());
        if (!dry) {
            Files.createDirectories(destDir);
            if (Files.exists(destFile)) {
                String base = baseName(file.getName()), ext = extName(file.getName());
                int c = 1;
                while (Files.exists(destFile)) {
                    destFile = destDir.resolve(ext.isEmpty() ? base + "_" + c : base + "_" + c + "." + ext);
                    c++;
                }
            }
            if (copyMode) {
                Files.copy(file.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(file.toPath(), destFile, StandardCopyOption.ATOMIC_MOVE);
            }
        }
    }

    private String baseName(String s) { int d=s.lastIndexOf('.'); return d>0 ? s.substring(0,d) : s; }
    private String extName(String s) { int d=s.lastIndexOf('.'); return d>0 && d<s.length()-1 ? s.substring(d+1) : ""; }
    private void setTopEnabled(boolean v) {
        sourceBrowseButton.setEnabled(v);
        targetBrowseButton.setEnabled(v);
        scanButton.setEnabled(v);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new FileClassifierApp().setVisible(true));
    }
}
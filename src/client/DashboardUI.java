package client;

import model.LoggedInUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardUI extends JFrame {

    private static final Logger LOGGER      = Logger.getLogger(DashboardUI.class.getName());
    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 6000;

    private File         selectedFile;
    private JLabel       lblFileName;
    private JLabel       lblFileSize;
    private JLabel       lblStatus;
    private JButton      btnSend;
    private JProgressBar progressBar;
    private JPanel       fileChipPanel;

    public DashboardUI() {
        setTitle("SmartShare — Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(420, 480);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(0xF8F7FF));
        setContentPane(main);

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x534AB7));
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel titleLbl = new JLabel("SmartShare");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(new Color(0xEEEDFE));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setForeground(new Color(0xEEEDFE));
        btnLogout.setBackground(new Color(0x6B64C8));
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> doLogout());

        topBar.add(titleLbl,  BorderLayout.WEST);
        topBar.add(btnLogout, BorderLayout.EAST);
        main.add(topBar);

        // ── Content ──────────────────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(0xF8F7FF));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Welcome row
        JLabel lblHi = new JLabel("Good day, " + LoggedInUser.username + "!");
        lblHi.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHi.setForeground(new Color(0x1A1A2E));
        lblHi.setAlignmentX(LEFT_ALIGNMENT);
        content.add(lblHi);

        JLabel lblSub = new JLabel("Ready to share files?");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(0x6B6893));
        lblSub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(lblSub);
        content.add(Box.createVerticalStrut(20));

        // ── Drop zone ────────────────────────────────────────────────────────
        JPanel dropZone = new JPanel(new GridBagLayout());
        dropZone.setBackground(new Color(0xEEEDFE));
        dropZone.setBorder(BorderFactory.createLineBorder(new Color(0xAFA9EC), 2));
        dropZone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        dropZone.setAlignmentX(LEFT_ALIGNMENT);
        dropZone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel dropInner = new JPanel();
        dropInner.setOpaque(false);
        dropInner.setLayout(new BoxLayout(dropInner, BoxLayout.Y_AXIS));

        JLabel dropIcon = new JLabel("⬆", SwingConstants.CENTER);
        dropIcon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        dropIcon.setForeground(new Color(0x534AB7));
        dropIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel dropTitle = new JLabel("Drop a file here");
        dropTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dropTitle.setForeground(new Color(0x3C3489));
        dropTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel dropSub = new JLabel("or click to browse");
        dropSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dropSub.setForeground(new Color(0x6B6893));
        dropSub.setAlignmentX(CENTER_ALIGNMENT);

        dropInner.add(dropIcon);
        dropInner.add(Box.createVerticalStrut(4));
        dropInner.add(dropTitle);
        dropInner.add(dropSub);
        dropZone.add(dropInner);

        dropZone.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { chooseFile(); }
        });

        new DropTarget(dropZone, new DropTargetAdapter() {
            @Override public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) setSelectedFile(files.get(0));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        content.add(dropZone);
        content.add(Box.createVerticalStrut(12));

        // ── File chip ────────────────────────────────────────────────────────
        fileChipPanel = new JPanel(new BorderLayout(10, 0));
        fileChipPanel.setBackground(Color.WHITE);
        fileChipPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0DEF7), 1),
            new EmptyBorder(10, 14, 10, 14)));
        fileChipPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        fileChipPanel.setAlignmentX(LEFT_ALIGNMENT);
        fileChipPanel.setVisible(false);

        JLabel fileIcon = new JLabel("📄");
        fileIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel chipText = new JPanel();
        chipText.setOpaque(false);
        chipText.setLayout(new BoxLayout(chipText, BoxLayout.Y_AXIS));
        lblFileName = new JLabel("file.txt");
        lblFileName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFileSize = new JLabel("0 KB");
        lblFileSize.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFileSize.setForeground(new Color(0x6B6893));
        chipText.add(lblFileName);
        chipText.add(lblFileSize);

        JButton btnClear = new JButton("✕");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.setForeground(new Color(0x6B6893));
        btnClear.setContentAreaFilled(false);
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearFile());

        fileChipPanel.add(fileIcon,  BorderLayout.WEST);
        fileChipPanel.add(chipText,  BorderLayout.CENTER);
        fileChipPanel.add(btnClear,  BorderLayout.EAST);
        content.add(fileChipPanel);
        content.add(Box.createVerticalStrut(10));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(new Color(0x534AB7));
        progressBar.setBackground(new Color(0xEEEDFE));
        progressBar.setBorderPainted(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressBar.setAlignmentX(LEFT_ALIGNMENT);
        progressBar.setVisible(false);
        content.add(progressBar);
        content.add(Box.createVerticalStrut(6));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(0x6B6893));
        lblStatus.setAlignmentX(LEFT_ALIGNMENT);
        content.add(lblStatus);
        content.add(Box.createVerticalStrut(12));

        // ── Buttons ──────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        btnSend = new JButton("Send File");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSend.setForeground(Color.WHITE);
        btnSend.setBackground(new Color(0x534AB7));
        btnSend.setOpaque(true);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setEnabled(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(e -> doSend());

        JButton btnHistory = new JButton("View History");
        btnHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnHistory.setForeground(new Color(0x534AB7));
        btnHistory.setBackground(new Color(0xF8F7FF));
        btnHistory.setOpaque(true);
        btnHistory.setBorder(BorderFactory.createLineBorder(new Color(0xAFA9EC), 1));
        btnHistory.setFocusPainted(false);
        btnHistory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHistory.addActionListener(e -> new HistoryUI().setVisible(true));

        btnRow.add(btnSend);
        btnRow.add(btnHistory);
        content.add(btnRow);

        main.add(content);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setSelectedFile(chooser.getSelectedFile());
        }
    }

    private void setSelectedFile(File f) {
        selectedFile = f;
        long kb = f.length() / 1024;
        lblFileName.setText(f.getName());
        lblFileSize.setText((kb < 1 ? "<1" : kb) + " KB");
        fileChipPanel.setVisible(true);
        btnSend.setEnabled(true);
        lblStatus.setText(" ");
    }

    private void clearFile() {
        selectedFile = null;
        fileChipPanel.setVisible(false);
        btnSend.setEnabled(false);
        lblStatus.setText(" ");
    }

    private void doSend() {
        if (selectedFile == null) return;
        btnSend.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        lblStatus.setText("Sending…");

        new SwingWorker<Void, Integer>() {
            @Override protected Void doInBackground() throws Exception {
                try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
                     OutputStream out = socket.getOutputStream();
                     PrintWriter header = new PrintWriter(new OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8), true);
                     FileInputStream fis = new FileInputStream(selectedFile)) {
                    header.println(LoggedInUser.username);
                    header.println(selectedFile.getName());
                    header.println(selectedFile.length());
                    header.flush();
                    long total = selectedFile.length(), sent = 0;
                    byte[] buf = new byte[8192]; int read;
                    while ((read = fis.read(buf)) > 0) {
                        out.write(buf, 0, read);
                        sent += read;
                        publish((int)(sent * 100 / total));
                    }
                    out.flush();
                }
                return null;
            }
            @Override protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }
            @Override protected void done() {
                try {
                    get();
                    progressBar.setValue(100);
                    lblStatus.setText("✓  Sent successfully!");
                    lblStatus.setForeground(new Color(0x1D9E75));
                    JOptionPane.showMessageDialog(DashboardUI.this, "File sent successfully!");
                    clearFile();
                    progressBar.setVisible(false);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Send failed", ex);
                    lblStatus.setText("✗  Send failed.");
                    lblStatus.setForeground(new Color(0xE24B4A));
                    progressBar.setVisible(false);
                    btnSend.setEnabled(true);
                    JOptionPane.showMessageDialog(DashboardUI.this, "Could not send:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            LoggedInUser.clear();
            new LoginUI().setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DashboardUI().setVisible(true));
    }
}
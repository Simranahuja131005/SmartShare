package client;

import database.DBConnection;
import model.LoggedInUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoryUI extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(HistoryUI.class.getName());

    private JLabel            lblTotalFiles;
    private JLabel            lblTotalSize;
    private DefaultTableModel tableModel;

    public HistoryUI() {
        setTitle("File History — " + LoggedInUser.username);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(480, 460);
        setLocationRelativeTo(null);
        buildUI();
        loadHistory();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(0xF8F7FF));
        setContentPane(main);

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x534AB7));
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));
        topBar.setPreferredSize(new Dimension(0, 52));

        JButton btnBack = new JButton("← Back");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setForeground(new Color(0xEEEDFE));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> dispose());

        JLabel titleLbl = new JLabel("File History", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(new Color(0xEEEDFE));

        topBar.add(btnBack,  BorderLayout.WEST);
        topBar.add(titleLbl, BorderLayout.CENTER);
        main.add(topBar, BorderLayout.NORTH);

        // ── Content ──────────────────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(0xF8F7FF));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        statsRow.setAlignmentX(LEFT_ALIGNMENT);

        lblTotalFiles = new JLabel("—");
        lblTotalFiles.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalSize  = new JLabel("—");
        lblTotalSize.setFont(new Font("Segoe UI", Font.BOLD, 22));

        statsRow.add(statCard("📤", "Files Sent",  lblTotalFiles));
        statsRow.add(statCard("💾", "Total Size",  lblTotalSize));
        content.add(statsRow);
        content.add(Box.createVerticalStrut(18));

        // Section title
        JLabel secTitle = new JLabel("RECENT TRANSFERS");
        secTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        secTitle.setForeground(new Color(0x6B6893));
        secTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(secTitle);
        content.add(Box.createVerticalStrut(8));

        // Table
        tableModel = new DefaultTableModel(new String[]{"File Name", "Sent At"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(new Color(0x1A1A2E));
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(0xE0DEF7));
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(0xEEEDFE));
        table.setSelectionForeground(new Color(0x3C3489));
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8F7FF));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(new Color(0x6B6893));
        header.setBackground(new Color(0xF8F7FF));
        header.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0DEF7), 1));
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        content.add(scroll);
        content.add(Box.createVerticalStrut(14));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setForeground(new Color(0x534AB7));
        btnRefresh.setBackground(new Color(0xF8F7FF));
        btnRefresh.setOpaque(true);
        btnRefresh.setBorder(BorderFactory.createLineBorder(new Color(0xAFA9EC), 1));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRefresh.setAlignmentX(LEFT_ALIGNMENT);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadHistory());
        content.add(btnRefresh);

        main.add(content, BorderLayout.CENTER);
    }

    private JPanel statCard(String emoji, String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0DEF7), 1),
            new EmptyBorder(12, 16, 12, 16)));

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        valueLabel.setForeground(new Color(0x1A1A2E));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(0x6B6893));

        textPanel.add(valueLabel);
        textPanel.add(lbl);
        card.add(emojiLbl, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        lblTotalFiles.setText("…");
        lblTotalSize.setText("…");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT filename, sent_at FROM file_history WHERE username = ? ORDER BY sent_at DESC")) {
            pst.setString(1, LoggedInUser.username);
            try (ResultSet rs = pst.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getString("filename"), rs.getTimestamp("sent_at")});
                    count++;
                }
                lblTotalFiles.setText(String.valueOf(count));
                lblTotalSize.setText(fetchTotalSize());
                if (count == 0) tableModel.addRow(new Object[]{"No files sent yet.", ""});
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "History load error", e);
            JOptionPane.showMessageDialog(this, "Could not load history:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fetchTotalSize() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT COALESCE(SUM(file_size), 0) AS total FROM file_history WHERE username = ?")) {
            pst.setString(1, LoggedInUser.username);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    long bytes = rs.getLong("total");
                    if (bytes < 1024) return bytes + " B";
                    if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
                    return (bytes / (1024 * 1024)) + " MB";
                }
            }
        } catch (Exception ignored) {}
        return "N/A";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HistoryUI().setVisible(true));
    }
}
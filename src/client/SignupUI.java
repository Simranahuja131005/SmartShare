package client;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignupUI extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(SignupUI.class.getName());

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;
    private JLabel         lblError;

    public SignupUI() {
        setTitle("SmartShare — Sign Up");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(400, 530);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(0xF8F7FF));
        main.setBorder(new EmptyBorder(40, 50, 40, 50));
        setContentPane(main);

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(0x534AB7));
        title.setAlignmentX(CENTER_ALIGNMENT);
        main.add(title);

        JLabel sub = new JLabel("Join SmartShare today", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(0x6B6893));
        sub.setAlignmentX(CENTER_ALIGNMENT);
        main.add(sub);
        main.add(Box.createVerticalStrut(30));

        main.add(fieldLabel("USERNAME"));
        main.add(Box.createVerticalStrut(4));
        txtUsername = new JTextField();
        styleField(txtUsername);
        main.add(txtUsername);
        main.add(Box.createVerticalStrut(14));

        main.add(fieldLabel("PASSWORD"));
        main.add(Box.createVerticalStrut(4));
        txtPassword = new JPasswordField();
        styleField(txtPassword);
        main.add(txtPassword);
        main.add(Box.createVerticalStrut(14));

        main.add(fieldLabel("CONFIRM PASSWORD"));
        main.add(Box.createVerticalStrut(4));
        txtConfirm = new JPasswordField();
        styleField(txtConfirm);
        main.add(txtConfirm);
        main.add(Box.createVerticalStrut(6));

        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(new Color(0xE24B4A));
        lblError.setAlignmentX(LEFT_ALIGNMENT);
        main.add(lblError);
        main.add(Box.createVerticalStrut(10));

        JButton btnCreate = styledButton("Create Account", true);
        btnCreate.addActionListener(e -> doSignup());
        main.add(btnCreate);
        main.add(Box.createVerticalStrut(10));

        JButton btnBack = styledButton("Back to Login", false);
        btnBack.addActionListener(e -> { new LoginUI().setVisible(true); dispose(); });
        main.add(btnBack);
    }

    private void doSignup() {
        String username = txtUsername.getText().trim();
        char[] p1 = txtPassword.getPassword();
        char[] p2 = txtConfirm.getPassword();
        String password = new String(p1);
        String confirm  = new String(p2);
        Arrays.fill(p1, '\0'); Arrays.fill(p2, '\0');

        if (username.isEmpty() || password.isEmpty()) { lblError.setText("Please fill all fields."); return; }
        if (username.length() > 50) { lblError.setText("Username max 50 characters."); return; }
        if (password.length() < 6)  { lblError.setText("Password must be at least 6 characters."); return; }
        if (!password.equals(confirm)) { lblError.setText("Passwords do not match."); return; }

        String hashed = LoginUI.sha256(password);
        if (hashed == null) { lblError.setText("Internal error."); return; }

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement check = con.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
                check.setString(1, username);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) { lblError.setText("Username already taken."); return; }
                }
            }
            try (PreparedStatement pst = con.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
                pst.setString(1, username);
                pst.setString(2, hashed);
                pst.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Account created! You can now sign in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            new LoginUI().setVisible(true);
            dispose();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Signup error", e);
            lblError.setText("DB error: " + e.getMessage());
        }
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(new Color(0x6B6893));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0DEF7), 1),
            new EmptyBorder(6, 10, 6, 10)));
        f.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton styledButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", primary ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(primary ? Color.WHITE : new Color(0x534AB7));
        btn.setBackground(primary ? new Color(0x534AB7) : new Color(0xF8F7FF));
        btn.setOpaque(true);
        btn.setBorder(primary
            ? BorderFactory.createEmptyBorder(10, 0, 10, 0)
            : BorderFactory.createLineBorder(new Color(0xAFA9EC), 1));
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignupUI().setVisible(true));
    }
}
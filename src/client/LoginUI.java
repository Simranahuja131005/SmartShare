package client;

import database.DBConnection;
import model.LoggedInUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginUI extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(LoginUI.class.getName());

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JLabel         lblError;

    public LoginUI() {
        setTitle("SmartShare — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(400, 480);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        // Main panel
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(0xF8F7FF));
        main.setBorder(new EmptyBorder(40, 50, 40, 50));
        setContentPane(main);

        // Logo + title
        JLabel logoIcon = new JLabel("⬆", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setOpaque(true);
        logoIcon.setBackground(new Color(0x534AB7));
        logoIcon.setPreferredSize(new Dimension(40, 40));
        logoIcon.setMaximumSize(new Dimension(40, 40));
        logoIcon.setBorder(new EmptyBorder(4, 4, 4, 4));
        logoIcon.setAlignmentX(CENTER_ALIGNMENT);
        main.add(logoIcon);
        main.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("SmartShare", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(0x534AB7));
        title.setAlignmentX(CENTER_ALIGNMENT);
        main.add(title);

        JLabel sub = new JLabel("Sign in to your account", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(0x6B6893));
        sub.setAlignmentX(CENTER_ALIGNMENT);
        main.add(sub);
        main.add(Box.createVerticalStrut(30));

        // Username
        JLabel lblU = new JLabel("USERNAME");
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblU.setForeground(new Color(0x6B6893));
        lblU.setAlignmentX(LEFT_ALIGNMENT);
        main.add(lblU);
        main.add(Box.createVerticalStrut(4));

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0DEF7), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        txtUsername.setAlignmentX(LEFT_ALIGNMENT);
        main.add(txtUsername);
        main.add(Box.createVerticalStrut(16));

        // Password
        JLabel lblP = new JLabel("PASSWORD");
        lblP.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblP.setForeground(new Color(0x6B6893));
        lblP.setAlignmentX(LEFT_ALIGNMENT);
        main.add(lblP);
        main.add(Box.createVerticalStrut(4));

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0DEF7), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        txtPassword.setAlignmentX(LEFT_ALIGNMENT);
        main.add(txtPassword);
        main.add(Box.createVerticalStrut(6));

        // Error label
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(new Color(0xE24B4A));
        lblError.setAlignmentX(LEFT_ALIGNMENT);
        main.add(lblError);
        main.add(Box.createVerticalStrut(10));

        // Sign in button
        JButton btnLogin = new JButton("Sign in");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(0x534AB7));
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());
        main.add(btnLogin);
        main.add(Box.createVerticalStrut(10));

        // Sign up button
        JButton btnSignup = new JButton("Create an account");
        btnSignup.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnSignup.setForeground(new Color(0x534AB7));
        btnSignup.setBackground(new Color(0xF8F7FF));
        btnSignup.setOpaque(true);
        btnSignup.setBorder(BorderFactory.createLineBorder(new Color(0xAFA9EC), 1));
        btnSignup.setFocusPainted(false);
        btnSignup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSignup.setAlignmentX(LEFT_ALIGNMENT);
        btnSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSignup.addActionListener(e -> { new SignupUI().setVisible(true); dispose(); });
        main.add(btnSignup);

        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        char[] pwdChars = txtPassword.getPassword();
        String password = new String(pwdChars);
        Arrays.fill(pwdChars, '\0');

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter username and password.");
            return;
        }

        String hashed = sha256(password);
        if (hashed == null) { lblError.setText("Internal error."); return; }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT id FROM users WHERE username = ? AND password = ?")) {
            pst.setString(1, username);
            pst.setString(2, hashed);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    LoggedInUser.username = username;
                    LoggedInUser.userId   = rs.getInt("id");
                    new DashboardUI().setVisible(true);
                    dispose();
                } else {
                    lblError.setText("Invalid username or password.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login error", e);
            lblError.setText("DB error: " + e.getMessage());
        }
    }

    static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return null;
        }
    }

    static JLabel makeLogoIcon() {
        JLabel icon = new JLabel("⬆", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        icon.setForeground(Color.WHITE);
        icon.setOpaque(true);
        icon.setBackground(new Color(0x534AB7));
        icon.setPreferredSize(new Dimension(28, 28));
        return icon;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
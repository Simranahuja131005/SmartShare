package client;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * SmartShareTheme — central place for every colour, font, and component
 * factory used across all screens.
 *
 * Palette  (purple / teal accent)
 *   PRIMARY        #534AB7   purple-600
 *   PRIMARY_LIGHT  #EEEDFE   purple-50
 *   PRIMARY_MID    #AFA9EC   purple-200
 *   PRIMARY_DARK   #3C3489   purple-800
 *   ACCENT         #1D9E75   teal-400
 *   BG             #F8F7FF   near-white with purple tint
 *   SURFACE        #FFFFFF
 *   BORDER         #E0DEF7   purple-100ish
 *   TEXT           #1A1A2E
 *   TEXT_MUTED     #6B6893
 *   TEXT_HINT      #A9A6C8
 *   DANGER         #E24B4A
 *   SUCCESS        #1D9E75
 */
public class SmartShareTheme {

    // ── Palette ───────────────────────────────────────────────────────────────
    public static final Color PRIMARY       = new Color(0x534AB7);
    public static final Color PRIMARY_LIGHT = new Color(0xEEEDFE);
    public static final Color PRIMARY_MID   = new Color(0xAFA9EC);
    public static final Color PRIMARY_DARK  = new Color(0x3C3489);
    public static final Color ACCENT        = new Color(0x1D9E75);
    public static final Color BG            = new Color(0xF8F7FF);
    public static final Color SURFACE       = Color.WHITE;
    public static final Color BORDER        = new Color(0xE0DEF7);
    public static final Color TEXT          = new Color(0x1A1A2E);
    public static final Color TEXT_MUTED    = new Color(0x6B6893);
    public static final Color TEXT_HINT     = new Color(0xA9A6C8);
    public static final Color DANGER        = new Color(0xE24B4A);
    public static final Color SUCCESS       = new Color(0x1D9E75);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,   20);
    public static final Font FONT_H2     = new Font("Segoe UI", Font.BOLD,   15);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN,  11);
    public static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,   11);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD,   13);

    // ── Radius ────────────────────────────────────────────────────────────────
    public static final int R_SM = 8;
    public static final int R_MD = 12;
    public static final int R_LG = 16;

    private SmartShareTheme() {}

    // ── Component factories ───────────────────────────────────────────────────

    /** Full-width primary (purple) button with rounded corners. */
    public static JButton primaryButton(String text) {
        return new RoundButton(text, PRIMARY, Color.WHITE, PRIMARY_DARK);
    }

    /** Full-width ghost button (white bg, purple border). */
    public static JButton ghostButton(String text) {
        return new RoundButton(text, SURFACE, PRIMARY, new Color(0xCECBF6));
    }

    /** Danger (red) button used for destructive actions. */
    public static JButton dangerButton(String text) {
        return new RoundButton(text, DANGER, Color.WHITE, new Color(0xA32D2D));
    }

    /** Styled text field. */
    public static JTextField styledField() {
        JTextField f = new JTextField();
        styleTextField(f);
        return f;
    }

    /** Styled password field. */
    public static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        styleTextField(f);
        return f;
    }

    private static void styleTextField(JTextField f) {
        f.setFont(FONT_BODY);
        f.setForeground(TEXT);
        f.setBackground(SURFACE);
        f.setBorder(new CompoundBorder(
            new RoundBorder(BORDER, R_SM),
            new EmptyBorder(8, 12, 8, 12)
        ));
        f.setOpaque(true);
        // hover / focus highlight
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new RoundBorder(PRIMARY_MID, R_SM),
                    new EmptyBorder(8, 12, 8, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new RoundBorder(BORDER, R_SM),
                    new EmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    /** Small muted uppercase label (field caption). */
    public static JLabel captionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    /** Large bold title label. */
    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT);
        return l;
    }

    /** Applies the global look-and-feel tweaks. Call before any UI is created. */
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background",      BG);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("Label.foreground",      TEXT);
        UIManager.put("TextField.caretForeground", PRIMARY);
        UIManager.put("PasswordField.caretForeground", PRIMARY);
    }

    // ── Inner helpers ─────────────────────────────────────────────────────────

    /** Rounded border drawn with Graphics2D. */
    public static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int   radius;
        public RoundBorder(Color c, int r) { color = c; radius = r; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1,1,1,1); }
    }

    /** A JButton that paints itself as a fully rounded rectangle. */
    public static class RoundButton extends JButton {
        private final Color bgNormal, bgHover, fgColor;

        public RoundButton(String text, Color bg, Color fg, Color hover) {
            super(text);
            this.bgNormal = bg;
            this.bgHover  = hover;
            this.fgColor  = fg;
            setFont(FONT_BUTTON);
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited (MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            g2.setColor(hover ? bgHover : bgNormal);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), R_MD, R_MD));
            // border for ghost buttons
            if (bgNormal.equals(SURFACE)) {
                g2.setColor(PRIMARY_MID);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, R_MD, R_MD));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded panel — use as a card surface. */
    public static class RoundPanel extends JPanel {
        private final int radius;
        private Color borderColor = BORDER;

        public RoundPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        public void setBorderColor(Color c) { this.borderColor = c; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, radius, radius));
            g2.dispose();
        }
    }

    /** Top bar panel with purple background. */
    public static class TopBar extends JPanel {
        public TopBar() {
            setBackground(PRIMARY);
            setPreferredSize(new Dimension(0, 52));
            setBorder(new EmptyBorder(0, 20, 0, 20));
            setLayout(new BorderLayout());
        }
        public JLabel makeTitle(String text) {
            JLabel l = new JLabel(text);
            l.setFont(FONT_H2);
            l.setForeground(PRIMARY_LIGHT);
            return l;
        }
    }

    /** Circular avatar label with initials. */
    public static JLabel avatarLabel(String initials) {
        JLabel lbl = new JLabel(initials, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_LIGHT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(PRIMARY_DARK);
        lbl.setPreferredSize(new Dimension(34, 34));
        lbl.setOpaque(false);
        return lbl;
    }
}

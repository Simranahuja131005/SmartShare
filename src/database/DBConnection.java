package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBConnection — upgraded
 *
 * Changes from original:
 *  - DB credentials moved to constants (easy to change / externalize later)
 *  - getConnection() now throws SQLException instead of swallowing it,
 *    so callers can react properly (show error dialogs, log, etc.)
 *  - Static initializer loads the JDBC driver explicitly for compatibility
 *    with older environments and the jre7 JAR you are using.
 *  - Added testConnection() helper used by TestDB.
 */
public class DBConnection {

    private static final Logger LOGGER =
            Logger.getLogger(DBConnection.class.getName());

    // ── Change these three constants to point at your own database ──────────
    private static final String URL  = "jdbc:postgresql://localhost:5432/smartshare";
    private static final String USER = "smartshare_user";
    private static final String PASS = "smart123";
    // ────────────────────────────────────────────────────────────────────────

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE,
                    "PostgreSQL JDBC driver not found on classpath.", e);
        }
    }

    private DBConnection() { /* utility class — no instances */ }

    /**
     * Opens and returns a new {@link Connection}.
     * The caller is responsible for closing it (use try-with-resources).
     *
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USER, PASS);
        LOGGER.fine("DB connection established.");
        return con;
    }

    /**
     * Quick smoke-test — returns true if a connection can be opened.
     */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Connection test failed.", e);
            return false;
        }
    }
}

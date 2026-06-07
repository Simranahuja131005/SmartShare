package server;

import database.DBConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileServer — upgraded
 *
 * Changes from original:
 *  1. Multi-threaded: each client is handled on its own thread via a thread
 *     pool, so multiple clients can transfer files simultaneously.
 *  2. Protocol: the client now sends a small header before the file bytes:
 *         <username>\n<filename>\n<filesize-in-bytes>\n
 *     The server reads that header, then reads exactly filesize bytes for the
 *     file. This fixes two original bugs:
 *       a) received files had no extension (were named "received_<timestamp>").
 *       b) the read loop used != -1 which hangs on a live TCP stream —
 *          replaced with an exact-byte-count read.
 *  3. Files are saved to a configurable OUTPUT_DIR (default: "received_files/").
 *  4. Every transfer is recorded in the file_history table.
 *  5. Proper logging instead of System.out.println.
 */
public class FileServer {

    private static final Logger LOGGER =
            Logger.getLogger(FileServer.class.getName());

    static final int    PORT       = 6000;
    static final String OUTPUT_DIR = "received_files/";

    public static void main(String[] args) {

        // Ensure the output directory exists
        new File(OUTPUT_DIR).mkdirs();

        // Thread pool: up to 10 simultaneous transfers
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.info("SmartShare server started on port " + PORT);

            while (true) {
                Socket client = serverSocket.accept();
                LOGGER.info("Client connected: " + client.getInetAddress());
                pool.submit(() -> handleClient(client));
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server error.", e);
        } finally {
            pool.shutdown();
        }
    }

    // ── Per-client handler ────────────────────────────────────────────────────

    private static void handleClient(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             InputStream rawIn = socket.getInputStream()) {

            // ── Read header ──────────────────────────────────────────────────
            // Header lines sent by DashboardUI (in order):
            //   1. username
            //   2. filename
            //   3. file size in bytes
            String username = reader.readLine();
            String filename  = reader.readLine();
            long   fileSize  = Long.parseLong(reader.readLine());

            if (username == null || filename == null || fileSize < 0) {
                LOGGER.warning("Malformed header — dropping connection.");
                return;
            }

            // Sanitise filename to prevent path-traversal attacks
            filename = new File(filename).getName();

            LOGGER.info("Receiving: " + filename + " (" + fileSize
                    + " bytes) from " + username);

            // ── Read exactly fileSize bytes ───────────────────────────────────
            File dest = new File(OUTPUT_DIR, filename);
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                byte[] buffer    = new byte[8192];
                long   remaining = fileSize;
                int    read;
                while (remaining > 0
                       && (read = rawIn.read(buffer, 0,
                               (int) Math.min(buffer.length, remaining))) > 0) {
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
            }

            LOGGER.info("File saved to: " + dest.getAbsolutePath());

            // ── Log to database ───────────────────────────────────────────────
            logTransfer(username, filename, fileSize);

        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error handling client.", e);
        }
    }

    // ── DB logging ────────────────────────────────────────────────────────────

    private static void logTransfer(String username, String filename, long fileSize) {
        String sql = "INSERT INTO file_history (username, filename, file_size) "
                   + "VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);
            pst.setString(2, filename);
            pst.setLong(3, fileSize);
            pst.executeUpdate();
            LOGGER.info("Transfer logged to DB.");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not log transfer to DB.", e);
        }
    }
}
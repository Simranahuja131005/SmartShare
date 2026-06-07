package model;

/**
 * LoggedInUser — simple session holder.
 *
 * Changes from original:
 *  - Added userId so the server can record transfers by user ID, not just name.
 *  - Added clear() so logout actually wipes the session state.
 */
public class LoggedInUser {

    /** Username of the currently authenticated user. */
    public static String username;

    /** Primary-key id from the users table. */
    public static int userId;

    private LoggedInUser() {}

    /** Call this on logout to wipe all session state. */
    public static void clear() {
        username = null;
        userId   = 0;
    }
}

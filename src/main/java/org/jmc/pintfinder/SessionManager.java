package org.jmc.pintfinder;

/**
 * Utility class for managing session state within the PintFinder application.
 * Stores and provides access to the currently logged-in user's UID.
 * HOLD THE SESSION AS AN ID TO REFERENCE (LOGIN NAME..ETC)
 */
public class SessionManager {
    private static String currentUserUID;

    /**
     * Sets the UID of the currently logged-in user.
     *
     * @param uid the user's unique identifier
     */
    public static void setCurrentUserUid(String uid){
        currentUserUID = uid;
    }

    /**
     * Retrieves the UID of the currently logged-in user.
     *
     * @return the current user's UID
     */
    public static String getCurrentUserUid(){
        return currentUserUID;
    }
}

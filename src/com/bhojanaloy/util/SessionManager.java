package com.bhojanaloy.util;

import com.bhojanaloy.model.User;

/** Holds the currently logged-in user for the running application instance. */
public class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void login(User user) { currentUser = user; }

    public static void logout() { currentUser = null; }

    public static User getCurrentUser() { return currentUser; }

    public static boolean isLoggedIn() { return currentUser != null; }
}

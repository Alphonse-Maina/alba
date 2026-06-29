package com.kinyozi.pos.util;

import com.kinyozi.pos.model.User;

public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser() { return currentUser; }
    public static void logout() { currentUser = null; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static boolean isAdmin() { return currentUser != null && currentUser.isAdmin(); }
    public static boolean isManager() { return currentUser != null && currentUser.isManager(); }
}
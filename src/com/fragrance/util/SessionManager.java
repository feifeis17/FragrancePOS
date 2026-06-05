package com.fragrance.util;

public class SessionManager {
    private static int    userId;
    private static String username;
    private static String role;     // "Admin" / "Operator" / "User"

    public static void setSession(int id, String user, String r) {
        userId = id; username = user; role = r;
    }

    public static int    getUserId()   { return userId; }
    public static String getUsername() { return username; }
    public static String getRole()     { return role; }
    public static boolean isAdmin()    { return "Admin".equals(role); }
    public static boolean isOperator() { return "Operator".equals(role); }
    public static void clearSession()  { userId = 0; username = null; role = null; }
}
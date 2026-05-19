package com.health.ui.utils;

public class SessionManager {

    private static SessionManager instance;
    private String authToken;
    private String userRole;
    private int userId;
    private String userFullName;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setSession(String token, String role, int userId, String fullName) {
        this.authToken = token;
        this.userRole = role;
        this.userId = userId;
        this.userFullName = fullName;
    }

    public void clearSession() {
        authToken = null;
        userRole = null;
        userId = 0;
        userFullName = null;
    }

    public String getAuthToken() { return authToken; }
    public String getUserRole() { return userRole; }
    public int getUserId() { return userId; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String fullName) { this.userFullName = fullName; }
    public boolean isLoggedIn() { return authToken != null; }
}
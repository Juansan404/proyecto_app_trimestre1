package com.javafx.util;

public class SessionManager {

    private static SessionManager instance;
    private String token;
    private Long userId;
    private String email;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public String getToken()          { return token; }
    public void   setToken(String t)  { this.token = t; }

    public Long   getUserId()         { return userId; }
    public void   setUserId(Long id)  { this.userId = id; }

    public String getEmail()          { return email; }
    public void   setEmail(String e)  { this.email = e; }

    public String getRole()           { return role; }
    public void   setRole(String r)   { this.role = r; }

    public boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        token  = null;
        userId = null;
        email  = null;
        role   = null;
    }
}

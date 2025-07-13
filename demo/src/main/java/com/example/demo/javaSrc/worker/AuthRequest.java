package com.example.demo.javaSrc.worker;

public class AuthRequest {
    private String email;
    private String password;
    private String role;

    public String getEmail()    { return email; }
    public String getPassword() { return password; }
    public String getRole()     { return role; }
    public void setEmail(String e)    { this.email = e; }
    public void setPassword(String p) { this.password = p; }
    public void setRole(String r)     { this.role = r; }

    public AuthRequest(String email, String password, String role) {
    this.email = email;
    this.password = password;
    this.role = role;
    }

}
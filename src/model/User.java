package model;

public class User {
    private final int userId;
    private final int roleId;
    private final String username;
    private final String fullName;
    private final String email;
    private final String status;

    public User(int userId, int roleId, String username,
                String fullName, String email, String status) {
        this.userId = userId;
        this.roleId = roleId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    public int getUserId() { return userId; }
    public int getRoleId() { return roleId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
}
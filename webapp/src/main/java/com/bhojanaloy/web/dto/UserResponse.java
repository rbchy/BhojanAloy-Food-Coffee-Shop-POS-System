package com.bhojanaloy.web.dto;

import com.bhojanaloy.model.User;

/** Safe subset of User to send to the browser — never expose passwordHash/salt. */
public class UserResponse {
    private int userId;
    private String username;
    private String fullName;
    private String role;
    private Integer employeeId;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.userId = u.getUserId();
        r.username = u.getUsername();
        r.fullName = u.getFullName();
        r.role = u.getRole();
        r.employeeId = u.getEmployeeId();
        return r;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public Integer getEmployeeId() { return employeeId; }
}

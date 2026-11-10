package com.bhojanaloy.web.controller;

import com.bhojanaloy.dao.UserDAO;
import com.bhojanaloy.model.User;
import com.bhojanaloy.web.dto.ApiError;
import com.bhojanaloy.web.dto.LoginRequest;
import com.bhojanaloy.web.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDAO userDAO = new UserDAO();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest httpRequest) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(new ApiError("Username and password are required"));
        }

        User user = userDAO.authenticate(req.getUsername(), req.getPassword());
        if (user == null) {
            return ResponseEntity.status(401).body(new ApiError("Invalid username or password"));
        }

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role", user.getRole());
        session.setAttribute("employeeId", user.getEmployeeId());

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok().body("{\"message\":\"logged out\"}");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body(new ApiError("Not logged in"));
        }
        return ResponseEntity.ok(sessionUserInfo(session));
    }

    private Map<String, Object> sessionUserInfo(HttpSession session) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("userId", session.getAttribute("userId"));
        info.put("username", session.getAttribute("username"));
        info.put("fullName", session.getAttribute("fullName"));
        info.put("role", session.getAttribute("role"));
        info.put("employeeId", session.getAttribute("employeeId"));
        return info;
    }
}

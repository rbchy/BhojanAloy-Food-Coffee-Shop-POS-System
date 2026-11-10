package com.bhojanaloy.web.controller;

import com.bhojanaloy.config.DBConnection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    /** Used to confirm the app + DB are up after deployment (also handy for uptime checks). */
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("app", "UP");
        status.put("db", DBConnection.testConnection() ? "UP" : "DOWN");
        return status;
    }
}

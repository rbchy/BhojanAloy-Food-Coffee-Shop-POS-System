package com.bhojanaloy.web;

import com.bhojanaloy.config.DBConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Bhojan-Aloy web demo (used for public QA automation
 * testing). Reuses the DAO/model/service classes from the original Swing
 * desktop app unchanged; only the presentation layer (this "web" package)
 * and DBConnection (env-var aware) are new.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("DB connection check: " + (DBConnection.testConnection() ? "OK" : "FAILED"));
    }
}

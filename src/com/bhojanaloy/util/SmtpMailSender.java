package com.bhojanaloy.util;

import com.bhojanaloy.config.EmailConfig;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A minimal, dependency-free SMTP client: sends a plain-text email over an
 * implicit-TLS connection (port 465) using AUTH LOGIN. No external mail
 * library (JavaMail/jakarta.mail) is required — everything here is built
 * into the JDK (javax.net.ssl, java.util.Base64), so there's no extra jar
 * to download and no risk of the classpath/jar-filename problems that come
 * with third-party libraries.
 *
 * This talks real SMTP and will really send an email once
 * {@link EmailConfig#isConfigured()} is true.
 */
public class SmtpMailSender {

    private SmtpMailSender() {}

    public static void send(String toAddress, String subject, String body) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (Socket socket = factory.createSocket(EmailConfig.SMTP_HOST, EmailConfig.SMTP_PORT)) {
            socket.setSoTimeout(15000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            readResponse(in, "220");
            command(out, in, "EHLO bhojanaloy.local", "250");
            command(out, in, "AUTH LOGIN", "334");
            command(out, in, Base64.getEncoder().encodeToString(EmailConfig.SMTP_USERNAME.getBytes(StandardCharsets.UTF_8)), "334");
            command(out, in, Base64.getEncoder().encodeToString(EmailConfig.SMTP_APP_PASSWORD.getBytes(StandardCharsets.UTF_8)), "235");
            command(out, in, "MAIL FROM:<" + EmailConfig.SMTP_USERNAME + ">", "250");
            command(out, in, "RCPT TO:<" + toAddress + ">", "250");
            command(out, in, "DATA", "354");

            String message = "From: " + EmailConfig.FROM_NAME + " <" + EmailConfig.SMTP_USERNAME + ">\r\n" +
                    "To: <" + toAddress + ">\r\n" +
                    "Subject: " + subject + "\r\n" +
                    "MIME-Version: 1.0\r\n" +
                    "Content-Type: text/plain; charset=UTF-8\r\n" +
                    "\r\n" +
                    body.replace("\r\n.\r\n", "\r\n..\r\n") + "\r\n.\r\n";
            out.print(message);
            out.flush();
            readResponse(in, "250");
            command(out, in, "QUIT", "221");
        }
    }

    private static void command(PrintWriter out, BufferedReader in, String cmd, String expectedCode) throws IOException {
        out.print(cmd + "\r\n");
        out.flush();
        readResponse(in, expectedCode);
    }

    private static void readResponse(BufferedReader in, String expectedCode) throws IOException {
        String line;
        String last = null;
        do {
            line = in.readLine();
            if (line == null) throw new IOException("SMTP server closed the connection unexpectedly.");
            last = line;
        } while (line.length() >= 4 && line.charAt(3) == '-'); // multi-line responses like "250-" continue
        if (last == null || !last.startsWith(expectedCode)) {
            throw new IOException("Unexpected SMTP response (wanted " + expectedCode + "): " + last);
        }
    }
}
package com.fragrance.util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityHelper {
    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Error saat melakukan hashing password: ", e);
        }
    }
}

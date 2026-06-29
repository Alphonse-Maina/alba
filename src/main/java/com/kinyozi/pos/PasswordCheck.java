package com.kinyozi.pos;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordCheck {
    public static void main(String[] args) {

        String password = "admin123";

        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        boolean matches = BCrypt.checkpw(password, hash);

        System.out.println("Password matches: " + matches);

        String hashnew = BCrypt.hashpw("admin123", BCrypt.gensalt(10));
        System.out.println(hashnew);
    }
}
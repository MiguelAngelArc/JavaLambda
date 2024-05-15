package com.zeus.domain.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface PasswordHasher {
    String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException;
    boolean verifyPassword(String storedHash, String password) throws NoSuchAlgorithmException, InvalidKeySpecException;
}

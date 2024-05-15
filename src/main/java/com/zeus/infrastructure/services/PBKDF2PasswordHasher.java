package com.zeus.infrastructure.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.zeus.domain.services.PasswordHasher;

public class PBKDF2PasswordHasher implements PasswordHasher {
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;

    @Override
    public String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash) + "#" + Base64.getEncoder().encodeToString(salt);
    }

    @Override
    public boolean verifyPassword(String storedHash, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] hashParts = storedHash.split("#");
        String passwordHash = hashParts[0];
        byte[] passwordSalt = Base64.getDecoder().decode(hashParts[1]);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), passwordSalt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = factory.generateSecret(spec).getEncoded();
        String newHash = Base64.getEncoder().encodeToString(hash);

        return newHash.equals(passwordHash);
    }

}

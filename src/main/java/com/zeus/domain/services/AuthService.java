package com.zeus.domain.services;

import java.util.concurrent.CompletableFuture;

import com.zeus.models.entities.User;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface AuthService {
    CompletableFuture<String> signIn(User user);
    CompletableFuture<String> signUp(User user) throws NoSuchAlgorithmException, InvalidKeySpecException;
}

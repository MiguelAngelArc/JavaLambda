package com.zeus.domain.services;

import com.zeus.domain.dataaccess.NoSqlRepository;
import com.zeus.models.entities.User;
import com.zeus.models.enums.ErrorCodes;

import io.netty.util.internal.StringUtil;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.CompletableFuture;

public class AuthServiceImpl implements AuthService {
    private JwtService jwtService;
    private NoSqlRepository<User> usersRepository;
    private PasswordHasher passwordHasher;
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String REGEX_PASSWORD = "^[\\w\\d_+&*-@#%!?]{8,32}$";
    private static final String REGEX_USERNAME = "^[\\w\\d\\s]{4,64}$";

    public AuthServiceImpl(
        JwtService jwtService, NoSqlRepository<User> usersRepository, PasswordHasher passwordHasher
    ) {
        this.jwtService = jwtService;
        this.usersRepository = usersRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public CompletableFuture<String> signIn(User user) {
        if (!user.getEmail().matches(REGEX_EMAIL))
            throw new RuntimeException(ErrorCodes.INVALID_EMAIL.toString());
        if (!user.getPassword().matches(REGEX_PASSWORD))
            throw new RuntimeException(ErrorCodes.INVALID_PASSWORD.toString());

        CompletableFuture<User> userTask = usersRepository.getItem(user.getEmail());

        return userTask.thenApplyAsync(userFound -> {
            try {
                if (userFound == null || StringUtil.isNullOrEmpty(userFound.getId()))
                    throw new RuntimeException(ErrorCodes.EMAIL_NOT_FOUND.toString());

                if (!passwordHasher.verifyPassword(userFound.getPassword(), user.getPassword()))
                    throw new RuntimeException(ErrorCodes.WRONG_PASSWORD.toString());

                return jwtService.createJwt(user.getEmail());
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<String> signUp(User user) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (!user.getEmail().matches(REGEX_EMAIL))
            throw new RuntimeException(ErrorCodes.INVALID_EMAIL.toString());
        if (!user.getPassword().matches(REGEX_PASSWORD))
            throw new RuntimeException(ErrorCodes.INVALID_PASSWORD.toString());
        if (!user.getUserName().matches(REGEX_USERNAME))
            throw new RuntimeException(ErrorCodes.INVALID_USERNAME.toString());
        
        user.setId(user.getEmail()); // TODO: Change this to use query, because now id and email is the same
        user.setPassword(passwordHasher.hashPassword(user.getPassword()));
        CompletableFuture<User> userTask = usersRepository.getItem(user.getEmail());
        return userTask.thenComposeAsync(u -> { 
            if (u != null && !StringUtil.isNullOrEmpty(u.getId()))
                throw new RuntimeException(ErrorCodes.EMAIL_ALREADY_IN_USE.toString());

            return usersRepository.addOrUpdateItem(user).thenApplyAsync(r -> jwtService.createJwt(user.getEmail()));
        });
    }
    
}

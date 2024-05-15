package com.zeus.webapi.controllers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.zeus.domain.services.AuthService;
import com.zeus.models.entities.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class UsersController {
    private AuthService authService;
    private Gson gson;

    
    public UsersController(AuthService authService) {
        this.authService = authService;
        gson = new Gson();
    }

    public APIGatewayProxyResponseEvent signIn(APIGatewayProxyRequestEvent input)
        throws InterruptedException, ExecutionException 
    {
        User user = gson.fromJson(input.getBody(), User.class);
        CompletableFuture<String> signInTask = authService.signIn(user);
        String token = signInTask.get();
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withBody(String.format("{\"token\": \"%s\"}", token));
    }

    public APIGatewayProxyResponseEvent signUp(APIGatewayProxyRequestEvent input)
        throws InterruptedException, ExecutionException, NoSuchAlgorithmException, InvalidKeySpecException  
    {
        User user = gson.fromJson(input.getBody(), User.class);
        CompletableFuture<String> signUpTask = authService.signUp(user);
        String token = signUpTask.get();
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withBody(String.format("{\"token\": \"%s\"}", token));
    }
}


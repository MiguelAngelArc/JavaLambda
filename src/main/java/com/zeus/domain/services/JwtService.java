package com.zeus.domain.services;

import io.jsonwebtoken.Claims;

public interface JwtService {
    String createJwt(String userId);
    Claims readJwt(String token);
}

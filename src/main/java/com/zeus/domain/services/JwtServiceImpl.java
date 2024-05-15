package com.zeus.domain.services;


import java.util.Date;

import javax.crypto.SecretKey;

import com.zeus.models.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


public class JwtServiceImpl implements JwtService {
    private JwtConfig jwtConfig;

    public JwtServiceImpl(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public String createJwt(String userId) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getHmacSecret());
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        Date now = new Date();

        return Jwts.builder()
            .subject(userId)
            .issuedAt(now)
            .issuer(jwtConfig.getIssuer())
            .expiration(new Date(now.getTime() + jwtConfig.getTtlSeconds() * 1000))
            .signWith(key)
            .compact();
    }

    @Override
    public Claims readJwt(String token) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getHmacSecret());
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
            .verifyWith(key)
            .requireIssuer(jwtConfig.getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

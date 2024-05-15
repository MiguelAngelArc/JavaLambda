package com.zeus.models.config;

public class JwtConfig {
    private String hmacSecret;
    private String issuer;
    private int ttlSeconds;

    public JwtConfig(String hmacSecret, String issuer, int ttlSeconds) {
        this.hmacSecret = hmacSecret;
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
    }

    public String getHmacSecret() {
        return hmacSecret;
    }
    public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret;
    }
    public String getIssuer() {
        return issuer;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    public int getTtlSeconds() {
        return ttlSeconds;
    }
    public void setTtlSeconds(int timeToLive) {
        this.ttlSeconds = timeToLive;
    }
    
}

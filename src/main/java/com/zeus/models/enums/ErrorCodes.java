package com.zeus.models.enums;

public enum ErrorCodes {
    UNKNOWN_ERROR("unknown-error"),
    NOTE_DOES_NOT_BELONG_TO_USER("note-does-not-belong-to-user"),
    NOTE_NOT_FOUND("note-not-found"),
    INVALID_EMAIL("invaid-email"),
    INVALID_PASSWORD("invalid-password"),
    INVALID_USERNAME("invalid-username"),
    EMAIL_NOT_FOUND("email-not-found"),
    EMAIL_ALREADY_IN_USE("email-already-in-use"),
    UNAUTHORIZED("unauthorized"),
    WRONG_PASSWORD("wrong-password");

    private final String prettyName;

    ErrorCodes(String prettyName) {
        this.prettyName = prettyName;
    }

    @Override
    public String toString() {
        return prettyName;
    }
}

package com.zeus.webapi.errors;

import com.zeus.models.enums.ErrorCodes;

import java.util.HashMap;
import java.util.Map;

public final class HttpErrors {
    public static final Map<String, HttpErrorInfo> CommonErrors = new HashMap<>() {{
        put(
            ErrorCodes.UNKNOWN_ERROR.toString(), 
            new HttpErrorInfo(ErrorCodes.UNKNOWN_ERROR.toString(), "An unknown error occurred", 500)
        );
        put(
            ErrorCodes.NOTE_DOES_NOT_BELONG_TO_USER.toString(), 
            new HttpErrorInfo(ErrorCodes.NOTE_DOES_NOT_BELONG_TO_USER.toString(), "Note does not belong to you", 403)
        );
        put(
            ErrorCodes.NOTE_NOT_FOUND.toString(), 
            new HttpErrorInfo(ErrorCodes.NOTE_NOT_FOUND.toString(), "Note not found", 404)
        );
        put(
            ErrorCodes.INVALID_EMAIL.toString(), 
            new HttpErrorInfo(ErrorCodes.INVALID_EMAIL.toString(), "Invalid email", 400)
        );
        put(
            ErrorCodes.INVALID_PASSWORD.toString(), 
            new HttpErrorInfo(ErrorCodes.INVALID_PASSWORD.toString(), "Invalid password", 400)
        );
        put(
            ErrorCodes.INVALID_USERNAME.toString(), 
            new HttpErrorInfo(ErrorCodes.INVALID_USERNAME.toString(), "Invalid username", 400)
        );
        put(
            ErrorCodes.EMAIL_NOT_FOUND.toString(), 
            new HttpErrorInfo(ErrorCodes.EMAIL_NOT_FOUND.toString(), "Email not found", 404)
        );
        put(
            ErrorCodes.INVALID_USERNAME.toString(), 
            new HttpErrorInfo(ErrorCodes.EMAIL_ALREADY_IN_USE.toString(), "Email already in use", 400)
        );
        put(
            ErrorCodes.UNAUTHORIZED.toString(), 
            new HttpErrorInfo(ErrorCodes.UNAUTHORIZED.toString(), "You are not authorized to perform this operation", 401)
        );
        put(
            ErrorCodes.WRONG_PASSWORD.toString(), 
            new HttpErrorInfo(ErrorCodes.WRONG_PASSWORD.toString(), "Wrong password", 400)
        );
    }};

    
}

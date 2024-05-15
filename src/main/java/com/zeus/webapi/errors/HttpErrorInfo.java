package com.zeus.webapi.errors;

import com.google.gson.annotations.SerializedName;

public final class HttpErrorInfo {
    @SerializedName(value = "ErrorCode")
    private String errorCode;
    @SerializedName(value = "Message")
    private String message;
    private transient int httpStatusCode;

    public HttpErrorInfo(String errorCode, String message, int httpStatusCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }        
}

package com.antonio.exception;

public class HttpException extends RuntimeException {
    private final int status;

    public HttpException(int status, String body) {
        super("HTTP " + status + ": " + body);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

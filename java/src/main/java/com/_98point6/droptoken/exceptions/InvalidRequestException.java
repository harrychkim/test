package com._98point6.droptoken.exceptions;

public abstract class InvalidRequestException extends RuntimeException {
    public InvalidRequestException() {
        super();
    }
    public InvalidRequestException(String s) {
        super(s);
    }
}

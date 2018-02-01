package com._98point6.droptoken.exceptions;

public abstract class InvalidMoveException extends RuntimeException {
    public InvalidMoveException() {
        super();
    }
    public InvalidMoveException(String s) {
        super(s);
    }
}

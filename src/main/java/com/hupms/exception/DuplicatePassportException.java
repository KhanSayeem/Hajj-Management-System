package com.hupms.exception;

public class DuplicatePassportException extends RuntimeException {
    public DuplicatePassportException(String message) {
        super(message);
    }
}

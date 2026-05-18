package com.hupms.exception;

public class GroupCapacityExceededException extends RuntimeException {
    public GroupCapacityExceededException(String message) {
        super(message);
    }
}

package com.fiveguys.trip_planner.exception;

public class DuplicatePhoneException extends RuntimeException {
    public DuplicatePhoneException(String message) {
        super(message);
    }
}
package com.skocz.capco.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Integer userId) {
        super("User with id: " + userId + " not found");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}

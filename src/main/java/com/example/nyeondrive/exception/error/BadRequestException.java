package com.example.nyeondrive.exception.error;

public class BadRequestException extends IllegalArgumentException{
    public BadRequestException(String message) {
        super(message);
    }
}

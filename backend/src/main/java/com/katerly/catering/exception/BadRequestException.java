package com.katerly.catering.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { 
        super(message); 
    }
}
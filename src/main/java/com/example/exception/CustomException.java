package com.example.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private int httpStatus;
    private String message;

    public CustomException(String message, int httpStatus) {
        super(message.toString());
        this.httpStatus = httpStatus;
        this.message = message;

    }
}

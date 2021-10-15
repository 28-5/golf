package com.reborn.golf.dto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ImpossibleDeleteException extends RuntimeException  {
    public ImpossibleDeleteException(String message) {
        super(message);
    }
}

package com.guardian.cloud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChildProfileNotFoundException
        extends RuntimeException {

    public ChildProfileNotFoundException(
            UUID childId
    ) {
        super(
                "Child profile not found: "
                        + childId
        );
    }
}
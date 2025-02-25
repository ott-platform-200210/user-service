package org.engicodes.userservice.model;

import lombok.Getter;

@Getter
public enum Permissions {
    UPLOAD("can upload videos"),
    LIST("can list videos"),
    DELETE("can delete videos"),
    WATCH("can watch videos");

    private final String description;

    Permissions(String description) {
        this.description = description;
    }
}
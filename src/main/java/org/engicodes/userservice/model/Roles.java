package org.engicodes.userservice.model;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum Roles {
    USER(Set.of(
            Permissions.WATCH,
            Permissions.LIST
    )),
    ADMIN(Set.of(
            Permissions.WATCH,
            Permissions.LIST,
            Permissions.DELETE,
            Permissions.UPLOAD
    ));
    private final Set<Permissions> permissions;

    Roles(Set<Permissions> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getAuthorities() {
        return permissions.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}

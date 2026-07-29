package com.nexaerp.mobile.core.permission;

import java.util.Collections;
import java.util.Set;

public final class PermissionEvaluator {
    private final Set<String> permissions;

    public PermissionEvaluator(Set<String> permissions) {
        this.permissions = permissions == null ? Collections.emptySet() : permissions;
    }

    public boolean has(String permission) {
        return permission != null && permissions.contains(permission);
    }
}

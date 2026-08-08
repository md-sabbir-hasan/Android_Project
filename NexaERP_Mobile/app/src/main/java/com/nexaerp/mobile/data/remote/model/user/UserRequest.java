package com.nexaerp.mobile.data.remote.model.user;

import java.util.Set;

public class UserRequest {
    private String name;
    private String email;
    private Set<Long> roleIds;

    public UserRequest() {}

    public UserRequest(String name, String email, Set<Long> roleIds) {
        this.name = name;
        this.email = email;
        this.roleIds = roleIds;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(Set<Long> roleIds) { this.roleIds = roleIds; }
}
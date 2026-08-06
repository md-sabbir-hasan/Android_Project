package com.nexaerp.mobile.data.remote.model.role;

public class PermissionResponse {
    private Long id;
    private String code;
    private String name;
    private String module;

    public PermissionResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
}
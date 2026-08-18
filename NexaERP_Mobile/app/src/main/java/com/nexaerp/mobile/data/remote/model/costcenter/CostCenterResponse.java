package com.nexaerp.mobile.data.remote.model.costcenter;

public class CostCenterResponse {
    private Long id;
    private String code;
    private String name;
    private Boolean isActive;

    public CostCenterResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
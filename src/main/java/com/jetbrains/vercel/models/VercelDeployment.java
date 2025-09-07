package com.jetbrains.vercel.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VercelDeployment {
    @JsonProperty("uid")
    private String uid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("state")
    private String state; // BUILDING, READY, ERROR, CANCELED

    @JsonProperty("type")
    private String type; // LAMBDAS, STATIC

    @JsonProperty("createdAt")
    private long createdAt;

    @JsonProperty("buildingAt")
    private Long buildingAt;

    @JsonProperty("readyAt")
    private Long readyAt;

    @JsonProperty("projectId")
    private String projectId;

    @JsonProperty("target")
    private String target; // production, preview, development

    @JsonProperty("source")
    private String source; // git, cli, import

    @JsonProperty("creator")
    private Creator creator;

    @JsonProperty("inspectorUrl")
    private String inspectorUrl;

    @JsonProperty("meta")
    private Map<String, Object> meta;

    @JsonProperty("aliasError")
    private AliasError aliasError;

    // Constructors
    public VercelDeployment() {}

    public VercelDeployment(String uid, String name, String url, String state) {
        this.uid = uid;
        this.name = name;
        this.url = url;
        this.state = state;
    }

    // Getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getBuildingAt() {
        return buildingAt;
    }

    public void setBuildingAt(Long buildingAt) {
        this.buildingAt = buildingAt;
    }

    public Long getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Long readyAt) {
        this.readyAt = readyAt;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public String getInspectorUrl() {
        return inspectorUrl;
    }

    public void setInspectorUrl(String inspectorUrl) {
        this.inspectorUrl = inspectorUrl;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public AliasError getAliasError() {
        return aliasError;
    }

    public void setAliasError(AliasError aliasError) {
        this.aliasError = aliasError;
    }

    // Helper methods
    public boolean isReady() {
        return "READY".equals(state);
    }

    public boolean isBuilding() {
        return "BUILDING".equals(state);
    }

    public boolean hasError() {
        return "ERROR".equals(state);
    }

    public boolean isCanceled() {
        return "CANCELED".equals(state);
    }

    public boolean isQueued() {
        return "QUEUED".equals(state);
    }

    public String getFullUrl() {
        if (url != null && !url.startsWith("http")) {
            return "https://" + url;
        }
        return url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Creator {
        @JsonProperty("uid")
        private String uid;

        @JsonProperty("email")
        private String email;

        @JsonProperty("username")
        private String username;

        // Getters and setters
        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AliasError {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        // Getters and setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

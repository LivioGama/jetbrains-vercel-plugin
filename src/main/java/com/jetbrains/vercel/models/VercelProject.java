package com.jetbrains.vercel.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VercelProject {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("framework")
    private String framework;

    @JsonProperty("targets")
    private ProjectTargets targets;

    @JsonProperty("gitRepository")
    private GitRepository gitRepository;

    @JsonProperty("updatedAt")
    private long updatedAt;

    @JsonProperty("createdAt")
    private long createdAt;

    // Constructors
    public VercelProject() {}

    public VercelProject(String id, String name, String framework) {
        this.id = id;
        this.name = name;
        this.framework = framework;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public ProjectTargets getTargets() {
        return targets;
    }

    public void setTargets(ProjectTargets targets) {
        this.targets = targets;
    }

    public GitRepository getGitRepository() {
        return gitRepository;
    }

    public void setGitRepository(GitRepository gitRepository) {
        this.gitRepository = gitRepository;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectTargets {
        @JsonProperty("production")
        private ProjectTarget production;

        // Getters and setters
        public ProjectTarget getProduction() {
            return production;
        }

        public void setProduction(ProjectTarget production) {
            this.production = production;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectTarget {
        @JsonProperty("id")
        private String id;

        @JsonProperty("domain")
        private String domain;

        @JsonProperty("target")
        private String target;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitRepository {
        @JsonProperty("type")
        private String type;

        @JsonProperty("repo")
        private String repo;

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRepo() {
            return repo;
        }

        public void setRepo(String repo) {
            this.repo = repo;
        }
    }
}

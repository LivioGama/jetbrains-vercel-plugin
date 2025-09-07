package com.jetbrains.vercel.services;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jetbrains.vercel.models.VercelDeployment;
import com.jetbrains.vercel.models.VercelProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service(Service.Level.PROJECT)
@State(name = "VercelProjectService", storages = @Storage("vercel.xml"))
public final class VercelProjectService implements PersistentStateComponent<VercelProjectService.State> {
    private static final Logger LOG = Logger.getInstance(VercelProjectService.class);

    private final Project project;
    private State myState = new State();
    private List<VercelProject> cachedProjects = new ArrayList<>();
    private List<VercelDeployment> cachedDeployments = new ArrayList<>();

    public VercelProjectService(Project project) {
        this.project = project;
    }

    public static VercelProjectService getInstance(Project project) {
        return project.getService(VercelProjectService.class);
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    // API Token management
    public String getApiToken() {
        return myState.apiToken;
    }

    public void setApiToken(String apiToken) {
        myState.apiToken = apiToken;
        LOG.info("API token updated");
    }

    public boolean hasApiToken() {
        return myState.apiToken != null && !myState.apiToken.trim().isEmpty();
    }

    // Auto-refresh settings
    public boolean isAutoRefreshEnabled() {
        return myState.autoRefreshEnabled;
    }

    public void setAutoRefreshEnabled(boolean enabled) {
        myState.autoRefreshEnabled = enabled;
        LOG.info("Auto-refresh " + (enabled ? "enabled" : "disabled"));
    }

    public int getRefreshIntervalSeconds() {
        return myState.refreshIntervalSeconds;
    }

    public void setRefreshIntervalSeconds(int seconds) {
        myState.refreshIntervalSeconds = Math.max(5, Math.min(120, seconds)); // Clamp between 5-120 seconds
        LOG.info("Refresh interval set to " + myState.refreshIntervalSeconds + " seconds");
    }

    // Backward compatibility method
    public int getRefreshIntervalMinutes() {
        return myState.refreshIntervalSeconds / 60;
    }

    public void setRefreshIntervalMinutes(int minutes) {
        setRefreshIntervalSeconds(minutes * 60);
    }

    // Projects and deployments management
    public List<VercelProject> getCachedProjects() {
        return new ArrayList<>(cachedProjects);
    }

    public void setCachedProjects(List<VercelProject> projects) {
        this.cachedProjects = new ArrayList<>(projects);
        LOG.info("Cached " + projects.size() + " projects");
    }

    public List<VercelDeployment> getCachedDeployments() {
        return new ArrayList<>(cachedDeployments);
    }

    public void setCachedDeployments(List<VercelDeployment> deployments) {
        this.cachedDeployments = new ArrayList<>(deployments);
        LOG.info("Cached " + deployments.size() + " deployments");
    }

    // Data fetching methods
    public CompletableFuture<List<VercelProject>> refreshProjects() {
        if (!hasApiToken()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        VercelApiService apiService = VercelApiService.getInstance();
        return apiService.getProjects(getApiToken())
                .thenApply(projects -> {
                    setCachedProjects(projects);
                    return projects;
                })
                .exceptionally(throwable -> {
                    LOG.error("Failed to refresh projects", throwable);
                    return new ArrayList<>();
                });
    }

    public CompletableFuture<List<VercelDeployment>> refreshDeployments() {
        if (!hasApiToken()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        VercelApiService apiService = VercelApiService.getInstance();
        return apiService.getAllDeployments(getApiToken())
                .thenApply(deployments -> {
                    setCachedDeployments(deployments);
                    return deployments;
                })
                .exceptionally(throwable -> {
                    LOG.error("Failed to refresh deployments", throwable);
                    return new ArrayList<>();
                });
    }

    public CompletableFuture<List<VercelDeployment>> refreshDeploymentsForProject(String projectId) {
        if (!hasApiToken()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        VercelApiService apiService = VercelApiService.getInstance();
        return apiService.getDeployments(getApiToken(), projectId)
                .exceptionally(throwable -> {
                    LOG.error("Failed to refresh deployments for project " + projectId, throwable);
                    return new ArrayList<>();
                });
    }

    public CompletableFuture<Boolean> testApiToken() {
        if (!hasApiToken()) {
            return CompletableFuture.completedFuture(false);
        }

        VercelApiService apiService = VercelApiService.getInstance();
        return apiService.testApiToken(getApiToken());
    }

    // Utility methods
    public void clearCache() {
        cachedProjects.clear();
        cachedDeployments.clear();
        LOG.info("Cache cleared");
    }

    public boolean isConfigured() {
        return hasApiToken();
    }

    public Project getProject() {
        return project;
    }

    // Filter methods
    public List<VercelDeployment> getDeploymentsForProject(String projectId) {
        return cachedDeployments.stream()
                .filter(deployment -> projectId.equals(deployment.getProjectId()))
                .toList();
    }

    public List<VercelDeployment> getDeploymentsByState(String state) {
        return cachedDeployments.stream()
                .filter(deployment -> state.equals(deployment.getState()))
                .toList();
    }

    public List<VercelDeployment> getProductionDeployments() {
        return cachedDeployments.stream()
                .filter(deployment -> "production".equals(deployment.getTarget()))
                .toList();
    }

    // State class for persistence
    public static class State {
        public String apiToken = "";
        public boolean autoRefreshEnabled = true;
        public int refreshIntervalSeconds = 10; // Default to 10 seconds
        public long lastRefreshTime = 0;

        // Default constructor required for XML serialization
        public State() {}
    }
}

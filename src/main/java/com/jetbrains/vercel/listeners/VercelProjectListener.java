package com.jetbrains.vercel.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.jetbrains.vercel.services.VercelProjectService;
import org.jetbrains.annotations.NotNull;

public class VercelProjectListener implements ProjectManagerListener {
    private static final Logger LOG = Logger.getInstance(VercelProjectListener.class);

    @Override
    @SuppressWarnings("removal")
    public void projectOpened(@NotNull Project project) {
        LOG.info("Project opened: " + project.getName());

        // Initialize the Vercel project service
        VercelProjectService projectService = VercelProjectService.getInstance(project);

        // If configured, perform initial data load
        if (projectService.isConfigured()) {
            LOG.info("Vercel plugin is configured, performing initial data load");
            projectService.refreshProjects().thenCompose(projects -> {
                LOG.info("Loaded " + projects.size() + " projects on project open");
                return projectService.refreshDeployments();
            }).thenAccept(deployments -> {
                LOG.info("Loaded " + deployments.size() + " deployments on project open");
            }).exceptionally(throwable -> {
                LOG.warn("Failed to load Vercel data on project open", throwable);
                return null;
            });
        } else {
            LOG.info("Vercel plugin not configured, skipping initial data load");
        }
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        LOG.info("Project closed: " + project.getName());

        // Clear cache when project is closed to free memory
        VercelProjectService projectService = VercelProjectService.getInstance(project);
        projectService.clearCache();
    }
}

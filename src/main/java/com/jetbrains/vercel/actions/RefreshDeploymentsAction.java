package com.jetbrains.vercel.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.jetbrains.vercel.services.VercelProjectService;
import org.jetbrains.annotations.NotNull;

public class RefreshDeploymentsAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(RefreshDeploymentsAction.class);

    public RefreshDeploymentsAction() {
        super("Refresh Vercel Deployments", "Refresh all Vercel projects and deployments", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        VercelProjectService projectService = VercelProjectService.getInstance(project);

        if (!projectService.isConfigured()) {
            Messages.showWarningDialog(
                project,
                "Vercel API token is not configured. Please configure it in Settings.",
                "Vercel Not Configured"
            );
            return;
        }

        LOG.info("Refreshing Vercel deployments manually");

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            projectService.refreshProjects().thenCompose(projects -> {
                LOG.info("Refreshed " + projects.size() + " projects");
                return projectService.refreshDeployments();
            }).thenAccept(deployments -> {
                LOG.info("Refreshed " + deployments.size() + " deployments");
                ApplicationManager.getApplication().invokeLater(() -> {
                    Messages.showInfoMessage(
                        project,
                        "Successfully refreshed " + deployments.size() + " deployments",
                        "Refresh Complete"
                    );
                });
            }).exceptionally(throwable -> {
                LOG.error("Failed to refresh deployments", throwable);
                ApplicationManager.getApplication().invokeLater(() -> {
                    Messages.showErrorDialog(
                        project,
                        "Failed to refresh deployments: " + throwable.getMessage(),
                        "Refresh Failed"
                    );
                });
                return null;
            });
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}

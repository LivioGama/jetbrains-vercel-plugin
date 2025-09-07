package com.jetbrains.vercel.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import com.jetbrains.vercel.models.VercelDeployment;
import com.jetbrains.vercel.models.VercelProject;
import com.jetbrains.vercel.services.VercelProjectService;
import com.jetbrains.vercel.settings.VercelSettingsChangeListener;
import com.jetbrains.vercel.settings.VercelSettingsTopics;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VercelToolWindowPanel extends JPanel {
    private static final Logger LOG = Logger.getInstance(VercelToolWindowPanel.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    private final Project project;
    private final VercelProjectService projectService;
    private final Tree deploymentsTree;
    private final DefaultTreeModel treeModel;
    private final JBLabel statusLabel;
    private final MessageBusConnection messageBusConnection;
    private Timer autoRefreshTimer;
    private boolean isRefreshing = false;
    private int animationFrame = 0;

    public VercelToolWindowPanel(Project project) {
        this.project = project;
        this.projectService = VercelProjectService.getInstance(project);

        // Set up message bus connection for settings changes
        this.messageBusConnection = project.getMessageBus().connect();
        this.messageBusConnection.subscribe(VercelSettingsTopics.SETTINGS_CHANGED, new VercelSettingsChangeListener() {
            @Override
            public void onSettingsChanged() {
                // Refresh deployments when settings change
                SwingUtilities.invokeLater(() -> refreshDeployments());
            }

            @Override
            public void onApiTokenChanged(boolean hasToken) {
                // If token was added, refresh immediately
                if (hasToken) {
                    SwingUtilities.invokeLater(() -> refreshDeployments());
                }
            }

            @Override
            public void onAutoRefreshChanged(boolean enabled, int intervalSeconds) {
                // Update auto-refresh timer when settings change
                scheduleAutoRefreshTimer();
            }
        });

        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(8));

        // Create tree model and tree
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Vercel Projects");
        this.treeModel = new DefaultTreeModel(rootNode);
        this.deploymentsTree = new Tree(treeModel);
        this.deploymentsTree.setRootVisible(false);
        this.deploymentsTree.setShowsRootHandles(true);
        this.deploymentsTree.setCellRenderer(new VercelTreeCellRenderer());

        // Setup auto-refresh timer with configurable interval
        this.autoRefreshTimer = new Timer("VercelAutoRefresh", true);
        scheduleAutoRefreshTimer();

        // Add double-click listener to open deployments in browser
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent e) {
                return handleDoubleClick();
            }
        }.installOn(deploymentsTree);

        // Create toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new RefreshAction());
        actionGroup.add(new AutoRefreshToggleAction());
        actionGroup.add(new ConfigureAction());
        actionGroup.addSeparator();
        actionGroup.add(new OpenDashboardAction());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("VercelToolWindow", actionGroup, true);
        toolbar.setTargetComponent(this);

        // Create status label
        this.statusLabel = new JBLabel("Ready");
        this.statusLabel.setBorder(JBUI.Borders.empty(4, 8));

        // Layout components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolbar.getComponent(), BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(new JBScrollPane(deploymentsTree), BorderLayout.CENTER);

        // Initial load
        refreshDeployments();

        // Schedule auto-refresh timer (will start when API token is configured)
        scheduleAutoRefreshTimer();

        // Show initial auto-refresh status
        if (projectService.isAutoRefreshEnabled()) {
            int interval = projectService.getRefreshIntervalSeconds();
            updateStatusLabel("Auto-refresh enabled (" + interval + "s interval)");
        } else {
            updateStatusLabel("Auto-refresh disabled");
        }
    }

    private boolean handleDoubleClick() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) deploymentsTree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.getUserObject() instanceof VercelDeployment) {
            VercelDeployment deployment = (VercelDeployment) selectedNode.getUserObject();
            String url = deployment.getFullUrl();
            if (url != null) {
                BrowserUtil.browse(url);
                return true;
            }
        }
        return false;
    }

    private void refreshDeployments() {
        if (!projectService.isConfigured()) {
            updateStatusLabel("Not configured - please set API token in settings");
            showConfigurationPrompt();
            return;
        }

        isRefreshing = true;
        updateStatusLabel("Refreshing...");

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            projectService.refreshProjects().thenCompose(projects -> {
                return projectService.refreshDeployments().thenApply(deployments -> {
                    SwingUtilities.invokeLater(() -> {
                        updateTree(projects, deployments);
                        updateStatusLabel("Last updated: " + DATE_FORMAT.format(new Date()));
                        isRefreshing = false;
                    });
                    return deployments;
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    updateStatusLabel("Error: " + throwable.getMessage());
                    isRefreshing = false;
                    LOG.error("Failed to refresh deployments", throwable);
                });
                return null;
            });
        });
    }

    private void updateTree(List<VercelProject> projects, List<VercelDeployment> deployments) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.removeAllChildren();

        for (VercelProject vercelProject : projects) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(vercelProject);
            rootNode.add(projectNode);

            // Add deployments for this project
            List<VercelDeployment> projectDeployments = deployments.stream()
                    .filter(d -> vercelProject.getId().equals(d.getProjectId()))
                    .limit(10) // Limit to 10 most recent deployments
                    .toList();

            for (VercelDeployment deployment : projectDeployments) {
                DefaultMutableTreeNode deploymentNode = new DefaultMutableTreeNode(deployment);
                projectNode.add(deploymentNode);
            }
        }

        treeModel.reload();

        // Expand all project nodes
        for (int i = 0; i < deploymentsTree.getRowCount(); i++) {
            deploymentsTree.expandRow(i);
        }

        // Repaint tree to update animations
        deploymentsTree.repaint();
    }

    private void updateStatusLabel(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    private void showConfigurationPrompt() {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.removeAllChildren();

        DefaultMutableTreeNode configNode = new DefaultMutableTreeNode("Configure Vercel API Token");
        rootNode.add(configNode);
        treeModel.reload();
    }

    // Custom tree cell renderer
    private class VercelTreeCellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
                                        boolean leaf, int row, boolean hasFocus) {
            if (value instanceof DefaultMutableTreeNode node) {
                Object userObject = node.getUserObject();

                if (userObject instanceof VercelProject project) {
                    setIcon(AllIcons.Nodes.ModuleGroup);
                    append(project.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                    append(" (" + project.getFramework() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                } else if (userObject instanceof VercelDeployment deployment) {
                    setIcon(getDeploymentIcon(deployment));
                    append(deployment.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

                    String stateText = getStateDisplayText(deployment);
                    append(" - " + stateText, getStateAttributes(deployment.getState()));

                    // Show additional info for queued deployments
                    if ("QUEUED".equals(deployment.getState())) {
                        append(" (Waiting in queue)", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
                    }

                    append(" (" + DATE_FORMAT.format(new Date(deployment.getCreatedAt())) + ")",
                           SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES);
                } else if (userObject instanceof String) {
                    setIcon(AllIcons.General.Settings);
                    append((String) userObject, SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES);
                }
            }
        }

        private Icon getDeploymentIcon(VercelDeployment deployment) {
            return switch (deployment.getState()) {
                case "READY" -> AllIcons.RunConfigurations.TestPassed;
                case "BUILDING" -> AllIcons.Process.Step_2; // Use building icon
                case "ERROR" -> AllIcons.RunConfigurations.TestError;
                case "CANCELED" -> AllIcons.RunConfigurations.TestIgnored;
                case "QUEUED" -> AllIcons.Process.Step_1; // Show queued icon
                default -> AllIcons.Process.Step_1;
            };
        }

        private String getStateDisplayText(VercelDeployment deployment) {
            return switch (deployment.getState()) {
                case "BUILDING" -> "Building...";
                case "QUEUED" -> "Queued";
                default -> deployment.getState();
            };
        }

        private SimpleTextAttributes getStateAttributes(String state) {
            return switch (state) {
                case "READY" -> new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GREEN.darker());
                case "BUILDING" -> new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.ORANGE.darker());
                case "ERROR" -> new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.RED);
                case "CANCELED" -> new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY);
                case "QUEUED" -> new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, JBColor.BLUE.darker());
                default -> SimpleTextAttributes.REGULAR_ATTRIBUTES;
            };
        }
    }

    // Action classes
    private class RefreshAction extends AnAction {
        public RefreshAction() {
            super("Refresh", "Refresh deployments", AllIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            refreshDeployments();
        }
    }

    private class ConfigureAction extends AnAction {
        public ConfigureAction() {
            super("Settings", "Configure Vercel settings", AllIcons.General.Settings);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            String currentToken = projectService.getApiToken();
            String newToken = Messages.showInputDialog(
                project,
                "Enter your Vercel API token:",
                "Vercel Configuration",
                Messages.getQuestionIcon(),
                currentToken,
                null
            );

            if (newToken != null && !newToken.trim().isEmpty()) {
                projectService.setApiToken(newToken.trim());
                refreshDeployments();
            }
        }
    }

    private class OpenDashboardAction extends AnAction {
        public OpenDashboardAction() {
            super("Open Dashboard", "Open Vercel Dashboard", AllIcons.Ide.External_link_arrow);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            BrowserUtil.browse("https://vercel.com/dashboard");
        }
    }

    private class AutoRefreshToggleAction extends ToggleAction {
        public AutoRefreshToggleAction() {
            super("Auto Refresh", "Toggle automatic refresh every 10 seconds", AllIcons.Actions.StartDebugger);
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return projectService.isAutoRefreshEnabled();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            projectService.setAutoRefreshEnabled(state);
            scheduleAutoRefreshTimer(); // Update timer based on new setting
            if (state) {
                int interval = projectService.getRefreshIntervalSeconds();
                updateStatusLabel("Auto-refresh enabled (" + interval + "s interval)");
            } else {
                updateStatusLabel("Auto-refresh disabled");
            }
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(projectService.isConfigured());
        }
    }

    private void scheduleAutoRefreshTimer() {
        // Cancel existing timer if running
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
        }

        autoRefreshTimer = new Timer("VercelAutoRefresh", true);

        if (projectService.isAutoRefreshEnabled() && projectService.isConfigured()) {
            int intervalSeconds = projectService.getRefreshIntervalSeconds();
            long intervalMillis = intervalSeconds * 1000L; // Convert seconds to milliseconds

            autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (!isRefreshing) {
                        SwingUtilities.invokeLater(() -> refreshDeployments());
                    }
                }
            }, intervalMillis, intervalMillis); // Use configurable interval
        }
    }

    // Cleanup timer and message bus connection when component is disposed
    public void dispose() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
        }
        if (messageBusConnection != null) {
            messageBusConnection.disconnect();
        }
    }
}

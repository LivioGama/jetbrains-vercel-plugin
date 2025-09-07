package com.jetbrains.vercel.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.jetbrains.vercel.services.VercelProjectService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VercelSettingsConfigurable implements Configurable {
    private final Project project;
    private VercelSettingsPanel settingsPanel;

    public VercelSettingsConfigurable(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Vercel";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsPanel = new VercelSettingsPanel(project);
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(settingsPanel.getPanel(), BorderLayout.NORTH);
        wrapperPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        return wrapperPanel;
    }

    @Override
    public boolean isModified() {
        if (settingsPanel == null) {
            return false;
        }
        return settingsPanel.isModified();
    }

    @Override
    public void apply() {
        if (settingsPanel != null) {
            settingsPanel.apply();
        }
    }

    @Override
    public void reset() {
        if (settingsPanel != null) {
            settingsPanel.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        settingsPanel = null;
    }

    private static class VercelSettingsPanel {
        private final Project project;
        private final VercelProjectService projectService;

        private JPanel mainPanel;
        private JBPasswordField apiTokenField;
        private JBCheckBox autoRefreshCheckBox;
        private JBTextField refreshIntervalField;
        private JButton testConnectionButton;
        private JBLabel connectionStatusLabel;

        public VercelSettingsPanel(Project project) {
            this.project = project;
            this.projectService = VercelProjectService.getInstance(project);
            createUI();
            loadSettings();
        }

        private void createUI() {
            apiTokenField = new JBPasswordField();
            apiTokenField.setColumns(35);

            autoRefreshCheckBox = new JBCheckBox("Enable automatic refresh");

            refreshIntervalField = new JBTextField();
            refreshIntervalField.setColumns(8);

            testConnectionButton = new JButton("Test Connection");
            testConnectionButton.addActionListener(new TestConnectionListener());

            connectionStatusLabel = new JBLabel("");
            connectionStatusLabel.setBorder(JBUI.Borders.emptyLeft(10));

            // Create main panel with GridBagLayout for precise control
            mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = JBUI.insets(5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0; // Allow horizontal expansion
            gbc.weighty = 1.0; // Allow vertical expansion

            // Row 1: API Token label and field
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
            gbc.gridwidth = 1;
            mainPanel.add(new JBLabel("API Token:"), gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(apiTokenField, gbc);

            // Row 2: Test connection button and status
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
            gbc.gridwidth = 1;
            mainPanel.add(Box.createHorizontalStrut(0), gbc); // Empty cell for alignment

            gbc.gridx = 1; gbc.weightx = 1.0;
            JPanel testStatusPanel = new JPanel(new BorderLayout(10, 0));
            testStatusPanel.add(testConnectionButton, BorderLayout.WEST);
            testStatusPanel.add(connectionStatusLabel, BorderLayout.CENTER);
            mainPanel.add(testStatusPanel, gbc);

            // Row 3: Auto-refresh checkbox
            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
            gbc.gridwidth = 1;
            mainPanel.add(Box.createHorizontalStrut(0), gbc); // Empty cell for alignment

            gbc.gridx = 1; gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(autoRefreshCheckBox, gbc);

            // Row 4: Refresh interval label and field
            gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
            gbc.gridwidth = 1;
            mainPanel.add(new JBLabel("Refresh interval:"), gbc);

            gbc.gridx = 1; gbc.weightx = 0.0;
            gbc.gridwidth = 1;
            mainPanel.add(refreshIntervalField, gbc);

            gbc.gridx = 2; gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(new JBLabel("seconds"), gbc);

            // Row 5: Separator
            gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = JBUI.insets(10, 5, 5, 5);
            mainPanel.add(new JSeparator(), gbc);

            // Row 6: Usage instructions
            gbc.gridy = 5;
            gbc.insets = JBUI.insets(8);
            JBLabel usageLabel = new JBLabel("<html><b>Getting Started:</b><br>" +
                    "1. Get your API token from <a href=\"https://vercel.com/account/tokens\">Vercel Dashboard</a><br>" +
                    "2. Enter the token above and test the connection<br>" +
                    "3. Enable auto-refresh for automatic updates<br>" +
                    "4. Open the Vercel tool window to view deployments</html>");
            usageLabel.setBorder(JBUI.Borders.empty(5));
            mainPanel.add(usageLabel, gbc);

            // Add tooltip to API token field
            apiTokenField.setToolTipText("Your Vercel API token from https://vercel.com/account/tokens");
            refreshIntervalField.setToolTipText("How often to automatically refresh deployments (5-120 seconds)");

            // Configure panel to fill available space and align to top
            mainPanel.setBorder(JBUI.Borders.empty(5, 15, 15, 15)); // Minimal top, normal sides/bottom
            mainPanel.setAlignmentY(Component.TOP_ALIGNMENT);

            // Ensure the panel expands to fill available space
            gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 6; // After the last row
            gbc.weighty = 1.0; // Push everything to the top
            gbc.fill = GridBagConstraints.VERTICAL;
            mainPanel.add(Box.createVerticalGlue(), gbc);
        }

        private void loadSettings() {
            apiTokenField.setText(projectService.getApiToken());
            autoRefreshCheckBox.setSelected(projectService.isAutoRefreshEnabled());
            refreshIntervalField.setText(String.valueOf(projectService.getRefreshIntervalSeconds()));

            // Enable/disable refresh interval field based on auto-refresh setting
            refreshIntervalField.setEnabled(autoRefreshCheckBox.isSelected());

            autoRefreshCheckBox.addActionListener(e ->
                refreshIntervalField.setEnabled(autoRefreshCheckBox.isSelected()));
        }

        public JPanel getPanel() {
            return mainPanel;
        }

        public boolean isModified() {
            String currentToken = projectService.getApiToken();
            String uiToken = new String(apiTokenField.getPassword());

            return !uiToken.equals(currentToken) ||
                   autoRefreshCheckBox.isSelected() != projectService.isAutoRefreshEnabled() ||
                   !refreshIntervalField.getText().equals(String.valueOf(projectService.getRefreshIntervalSeconds()));
        }

        public void apply() {
            String oldToken = projectService.getApiToken();
            boolean oldAutoRefresh = projectService.isAutoRefreshEnabled();
            int oldInterval = projectService.getRefreshIntervalSeconds();

            String newToken = new String(apiTokenField.getPassword()).trim();
            projectService.setApiToken(newToken);

            boolean newAutoRefresh = autoRefreshCheckBox.isSelected();
            projectService.setAutoRefreshEnabled(newAutoRefresh);

            int newInterval = oldInterval;
            try {
                newInterval = Integer.parseInt(refreshIntervalField.getText().trim());
                if (newInterval < 5 || newInterval > 120) {
                    Messages.showErrorDialog(project, "Refresh interval must be between 5 and 120 seconds.", "Invalid Interval");
                    // Reset to default
                    projectService.setRefreshIntervalSeconds(10);
                    refreshIntervalField.setText("10");
                    newInterval = 10;
                } else {
                    projectService.setRefreshIntervalSeconds(newInterval);
                }
            } catch (NumberFormatException e) {
                Messages.showErrorDialog(project, "Please enter a valid number for the refresh interval.", "Invalid Input");
                // Reset to default if invalid
                projectService.setRefreshIntervalSeconds(10);
                refreshIntervalField.setText("10");
                newInterval = 10;
            }

            // Publish settings change messages
            MessageBus messageBus = project.getMessageBus();
            VercelSettingsChangeListener publisher = messageBus.syncPublisher(VercelSettingsTopics.SETTINGS_CHANGED);

            // Notify about API token change
            if (!oldToken.equals(newToken)) {
                publisher.onApiTokenChanged(!newToken.isEmpty());
            }

            // Notify about auto-refresh change
            if (oldAutoRefresh != newAutoRefresh || oldInterval != newInterval) {
                publisher.onAutoRefreshChanged(newAutoRefresh, newInterval);
            }

            // General settings change notification
            publisher.onSettingsChanged();
        }

        public void reset() {
            loadSettings();
            connectionStatusLabel.setText("");
        }

        private class TestConnectionListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String token = new String(apiTokenField.getPassword()).trim();

                if (token.isEmpty()) {
                    connectionStatusLabel.setText("⚠ Please enter an API token");
                    connectionStatusLabel.setForeground(Color.ORANGE);
                    return;
                }

                testConnectionButton.setEnabled(false);
                connectionStatusLabel.setText("Testing...");
                connectionStatusLabel.setForeground(Color.BLUE);

                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    projectService.setApiToken(token); // Temporarily set for testing

                    projectService.testApiToken().thenAccept(isValid -> {
                        SwingUtilities.invokeLater(() -> {
                            testConnectionButton.setEnabled(true);

                            if (isValid) {
                                connectionStatusLabel.setText("✓ Connection successful");
                                connectionStatusLabel.setForeground(new Color(0, 128, 0));
                            } else {
                                connectionStatusLabel.setText("✗ Invalid token");
                                connectionStatusLabel.setForeground(Color.RED);
                            }
                        });
                    }).exceptionally(throwable -> {
                        SwingUtilities.invokeLater(() -> {
                            testConnectionButton.setEnabled(true);
                            connectionStatusLabel.setText("✗ Connection failed");
                            connectionStatusLabel.setForeground(Color.RED);

                            Messages.showErrorDialog(
                                project,
                                "Connection test failed: " + throwable.getMessage(),
                                "Connection Test Failed"
                            );
                        });
                        return null;
                    });
                });
            }
        }
    }
}

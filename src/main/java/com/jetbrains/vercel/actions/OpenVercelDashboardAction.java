package com.jetbrains.vercel.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

public class OpenVercelDashboardAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(OpenVercelDashboardAction.class);
    private static final String VERCEL_DASHBOARD_URL = "https://vercel.com/dashboard";

    public OpenVercelDashboardAction() {
        super("Open Vercel Dashboard", "Open Vercel Dashboard in browser", AllIcons.Ide.External_link_arrow);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        LOG.info("Opening Vercel Dashboard: " + VERCEL_DASHBOARD_URL);
        BrowserUtil.browse(VERCEL_DASHBOARD_URL);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always enabled - no project dependency required for opening dashboard
        e.getPresentation().setEnabled(true);
    }
}

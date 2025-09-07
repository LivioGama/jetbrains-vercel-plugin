package com.jetbrains.vercel.settings;

import com.intellij.util.messages.Topic;

/**
 * Topics for Vercel settings change notifications.
 */
public class VercelSettingsTopics {

    /**
     * Topic for listening to Vercel settings changes.
     */
    public static final Topic<VercelSettingsChangeListener> SETTINGS_CHANGED =
        Topic.create("Vercel Settings Changed", VercelSettingsChangeListener.class);
}

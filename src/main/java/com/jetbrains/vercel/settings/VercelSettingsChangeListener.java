package com.jetbrains.vercel.settings;

/**
 * Listener interface for Vercel settings changes.
 * Components can implement this to be notified when settings are modified.
 */
public interface VercelSettingsChangeListener {

    /**
     * Called when Vercel settings have been changed.
     * Implementations should refresh their state accordingly.
     */
    void onSettingsChanged();

    /**
     * Called when the API token has been changed.
     * @param hasToken true if a valid token is now configured
     */
    default void onApiTokenChanged(boolean hasToken) {
        onSettingsChanged();
    }

    /**
     * Called when auto-refresh settings have been changed.
     * @param enabled true if auto-refresh is enabled
     * @param intervalSeconds the refresh interval in seconds
     */
    default void onAutoRefreshChanged(boolean enabled, int intervalSeconds) {
        onSettingsChanged();
    }
}

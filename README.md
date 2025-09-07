# Vercel Deployments Plugin for IntelliJ IDEA

A JetBrains IntelliJ IDEA plugin that allows you to view and manage your Vercel project deployments directly from your IDE.

## Features

- 📋 View all your Vercel projects and deployments
- 🔄 Real-time deployment status monitoring  
- 🌐 Quick access to deployment URLs
- ⚙️ Easy configuration with Vercel API tokens
- 🔄 Automatic refresh capabilities
- 🚀 Open deployments directly in browser
- 📊 Visual indicators for deployment status

## How to Test the Plugin

### Prerequisites

1. **Java 17 or higher** - Required for building and running
2. **IntelliJ IDEA 2023.2 or later** - Target IDE
3. **Vercel Account** - You'll need a Vercel account with projects
4. **Vercel API Token** - Get one from [https://vercel.com/account/tokens](https://vercel.com/account/tokens)

### Step 1: Build the Plugin

1. Open terminal in the project root directory
2. Run the build command:
```bash
./gradlew buildPlugin
```

The plugin JAR will be created in `build/distributions/`

### Step 2: Install the Plugin in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Go to **File** → **Settings** (Windows/Linux) or **IntelliJ IDEA** → **Preferences** (macOS)
3. Navigate to **Plugins**
4. Click the gear icon (⚙️) and select **Install Plugin from Disk...**
5. Browse to `build/distributions/jetbrains-vercel-plugin-1.0.0.zip`
6. Click **OK** and restart IntelliJ IDEA

### Step 3: Configure the Plugin

1. After restart, go to **File** → **Settings** → **Tools** → **Vercel**
2. Enter your Vercel API token (get it from [https://vercel.com/account/tokens](https://vercel.com/account/tokens))
3. Click **Test Connection** to verify the token works
4. Optionally enable auto-refresh and set the interval
5. Click **Apply** and **OK**

### Step 4: Use the Plugin

1. Open the **Vercel Deployments** tool window:
   - Go to **View** → **Tool Windows** → **Vercel Deployments**
   - Or look for the Vercel icon in the right tool window bar
2. The tool window will show:
   - Your Vercel projects (expandable nodes)
   - Recent deployments for each project
   - Deployment status indicators (Ready ✓, Building 🔄, Error ✗, etc.)
3. **Double-click** any deployment to open it in your browser
4. Use the **Refresh** button to manually update the data
5. Use **Settings** button to reconfigure the API token
6. Use **Open Dashboard** to go to Vercel's web dashboard

### Step 5: Test Different Scenarios

**Test basic functionality:**
- Verify projects are loaded correctly
- Check that deployments show with proper status
- Test double-clicking to open deployment URLs

**Test error handling:**
- Try with an invalid API token
- Test with no internet connection
- Verify error messages are displayed properly

**Test UI interactions:**
- Expand/collapse project nodes in the tree
- Use toolbar buttons (Refresh, Settings, Dashboard)
- Test the settings panel

## Development

### Project Structure

```
src/main/java/com/jetbrains/vercel/
├── actions/           # IDE actions (refresh, open dashboard)
├── listeners/         # Project lifecycle listeners  
├── models/           # Data models for Vercel API
├── services/         # API service and project service
├── settings/         # Plugin configuration UI
└── ui/              # Tool window and UI components

src/main/resources/
├── META-INF/plugin.xml  # Plugin configuration
└── icons/              # Plugin icons
```

### Key Classes

- **VercelApiService**: Handles all Vercel API communication
- **VercelProjectService**: Manages project state and caching
- **VercelToolWindowPanel**: Main UI for viewing deployments
- **VercelSettingsConfigurable**: Configuration panel for API token

### Building for Development

```bash
# Build the plugin
./gradlew buildPlugin

# Run IntelliJ with the plugin loaded
./gradlew runIde

# Run tests (when implemented)
./gradlew test
```

## Troubleshooting

### Plugin doesn't load
- Check IntelliJ IDEA version compatibility (2023.2+)
- Verify Java 17+ is being used
- Check IDE logs: **Help** → **Show Log in Finder/Explorer**

### API connection fails
- Verify your Vercel API token is correct
- Check internet connectivity
- Ensure the token has proper permissions

### No deployments shown
- Make sure you have projects in your Vercel account
- Try refreshing manually with the Refresh button
- Check that projects have deployments

### Performance issues
- Reduce refresh interval in settings
- Disable auto-refresh if not needed
- Clear cache by restarting IntelliJ

## API Token Setup

1. Go to [https://vercel.com/account/tokens](https://vercel.com/account/tokens)
2. Click **Create Token**
3. Give it a descriptive name (e.g., "IntelliJ Plugin")
4. Select appropriate scope (usually default is fine)
5. Copy the token and paste it in the plugin settings

**Security Note**: Keep your API token secure and never commit it to version control.

## Support

If you encounter issues:

1. Check the IntelliJ IDEA logs: **Help** → **Show Log in Finder/Explorer**
2. Look for entries with `VercelApiService` or `VercelProjectService`
3. Verify your Vercel account has projects and deployments
4. Test your API token directly with Vercel's API

## License

This plugin is provided as-is for demonstration purposes.

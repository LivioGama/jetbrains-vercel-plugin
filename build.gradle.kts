plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.jetbrains.vercel"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.2")
}

// Configure Gradle IntelliJ Plugin
intellij {
    version.set("2024.2.1")
    type.set("IU") // IntelliJ IDEA Ultimate Edition

    plugins.set(listOf(
        // Required for basic IDE functionality
    ))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("253.*")

        // Plugin description
        pluginDescription.set("""
            <h1>Vercel Deployments Plugin</h1>
            <p>View and manage your Vercel project deployments directly from IntelliJ IDEA.</p>

            <h2>Features:</h2>
            <ul>
                <li>View all your Vercel projects and their deployments</li>
                <li>Quick access to deployment URLs</li>
                <li>Real-time deployment status monitoring</li>
                <li>Easy configuration with Vercel API tokens</li>
                <li>Automatic refresh capabilities</li>
                <li>Open deployments directly in browser</li>
            </ul>

            <h2>Getting Started:</h2>
            <ol>
                <li>Get your Vercel API token from <a href="https://vercel.com/account/tokens">https://vercel.com/account/tokens</a></li>
                <li>Configure the token in Settings → Tools → Vercel</li>
                <li>Open the Vercel tool window to view your deployments</li>
                <li>Double-click any deployment to open it in your browser</li>
            </ol>

            <p>Perfect for developers who want to monitor their Vercel deployments without leaving their IDE.</p>
        """)

        changeNotes.set("""
            <h3>Version 1.0.0</h3>
            <ul>
                <li>Initial release of Vercel Deployments plugin</li>
                <li>View projects and deployments in dedicated tool window</li>
                <li>Configure Vercel API token in settings</li>
                <li>Automatic and manual refresh capabilities</li>
                <li>Quick access to Vercel dashboard</li>
                <li>Double-click to open deployments in browser</li>
                <li>Visual indicators for deployment status (Ready, Building, Error, etc.)</li>
                <li>Project lifecycle integration</li>
            </ul>
        """)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    // Configure test task
    test {
        useJUnitPlatform()
    }
}

// Java version configuration
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

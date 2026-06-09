plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "dk.utilis"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Develop against IntelliJ Community. The version should be <= the IDE
        // you'll actually run the plugin in. (Ultimate users: this is fine too;
        // we only use platform APIs.)
        intellijIdeaCommunity("2025.1")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"             // 2025.1. Lower it if you run an older IDE.
            untilBuild = provider { null } // don't cap the upper bound
        }
    }
}

kotlin {
    jvmToolchain(21)   // IntelliJ 2024.2+ is built against JDK 21
}

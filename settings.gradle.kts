pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mapbox Maven repository for Maps SDK v11+
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                // Set MAPBOX_DOWNLOADS_TOKEN in gradle.properties or as environment variable
                password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").getOrElse(System.getenv("MAPBOX_DOWNLOADS_TOKEN") ?: "")
            }
        }
    }
}

rootProject.name = "YorkShire_Crimes"
include(":app")

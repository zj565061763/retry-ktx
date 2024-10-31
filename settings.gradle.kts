pluginManagement {
   repositories {
      gradlePluginPortal()
      google()
      mavenCentral()
   }
}

dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      maven("https://jitpack.io")
   }
}

rootProject.name = "retry-ktx"
include(":app")
include(":lib")

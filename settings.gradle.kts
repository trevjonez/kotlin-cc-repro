rootProject.name = "kotlin-cc-repro"
pluginManagement {
  repositories {
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity").version("3.18.1")
  id("com.gradle.common-custom-user-data-gradle-plugin").version("2.0.2")
  id("org.gradle.toolchains.foojay-resolver-convention").version("0.8.0")
}

val isCi = System.getenv("CI") != null

develocity {
  allowUntrustedServer = true
  buildScan {
    termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
    termsOfUseAgree.set("yes")
    uploadInBackground = !isCi
    capture {
      buildLogging = true
      fileFingerprints = true
      testLogging = true
    }
  }
}

buildCache {
  local {
    isEnabled = true
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

include("settings-plugin")

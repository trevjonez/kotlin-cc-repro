import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `kotlin-dsl`
  `maven-publish`
}

gradlePlugin {
  plugins {
    create("repro") {
      id = "com.issue.repro"
      implementationClass = "com.issue.repro.GradleSettingsPlugin"
    }
  }
}

dependencies {
  api(plugin(libs.plugins.gradle.custom.user.data))
  api(plugin(libs.plugins.gradle.enterprise))

  testImplementation(platform(libs.junit.bom))
  testRuntimeOnly(libs.junit.launcher)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotlin.test)
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    showStandardStreams = true
    events(*TestLogEvent.values())
  }
}

val jdkVersion = libs.versions.jdk.get().toInt()

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(jdkVersion))
  }
}

kotlin {
  jvmToolchain(jdkVersion)
}

fun plugin(pluginDep: Provider<PluginDependency>): Provider<String> {
  return pluginDep.map { plugin ->
    "${plugin.pluginId}:${plugin.pluginId}.gradle.plugin:${plugin.version.requiredVersion}"
  }
}

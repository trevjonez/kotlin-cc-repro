package com.issue.repro

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.initialization.Settings

class MacModelTaggingConvention : SettingsPluginConvention {

  override fun configure(target: Settings, develocity: DevelocityConfiguration) {
    val os = target.providers.systemProperty("os.name").get()
    if (os.contains("Mac", ignoreCase = true)) {
      val sysctlExec = target.providers.exec {
        isIgnoreExitValue = true
        commandLine("sysctl", "-n", "hw.model")
      }
      develocity.buildScan.background {
        val tagValue = sysctlExec.standardOutput.asText.orNull
          ?.trim()
          ?.takeIf { it.isNotBlank() }
          ?: "Mac hw.model Unknown"
        tag(tagValue)
      }
    }
  }
}
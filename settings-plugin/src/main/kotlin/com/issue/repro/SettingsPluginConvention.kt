package com.issue.repro

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.initialization.Settings

fun interface SettingsPluginConvention {

  fun configure(target: Settings, develocity: DevelocityConfiguration)
}
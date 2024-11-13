package com.issue.repro

import com.gradle.CommonCustomUserDataGradlePlugin
import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import com.gradle.develocity.agent.gradle.DevelocityPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.apply

abstract class GradleSettingsPlugin : Plugin<Settings> {

  private val conventions = listOf(
    CpuPartTaggingConvention(),
    GhaScanLinkSummaryConvention(),
    MacModelTaggingConvention(),
  )

  override fun apply(target: Settings) {
    target.apply<DevelocityPlugin>()
    target.apply<CommonCustomUserDataGradlePlugin>()
    target.extensions.configure<DevelocityConfiguration>("develocity") {
      conventions.forEach {
        it.configure(target, this)
      }
    }
  }
}

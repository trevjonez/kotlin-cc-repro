package com.issue.repro

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.initialization.Settings
import java.io.File

class GhaScanLinkSummaryConvention : SettingsPluginConvention {
  override fun configure(target: Settings, develocity: DevelocityConfiguration) {
    val actionsEnvFlag = target.providers.environmentVariable("GITHUB_ACTIONS")
    val actionsSummaryPath = target.providers.environmentVariable("GITHUB_STEP_SUMMARY")
    if (actionsEnvFlag.isPresent &&
      actionsSummaryPath.isPresent
    ) {
      val taskList = target.startParameter.taskNames.joinToString(" ")
      develocity.buildScan.buildScanPublished {
        val summaryFile = File(actionsSummaryPath.get())
        if (!summaryFile.exists()) {
          summaryFile.createNewFile()
        }
        summaryFile.appendText("Build scan - $taskList: $buildScanUri")
      }
    }
  }
}
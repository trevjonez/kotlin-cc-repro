package com.issue.repro

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import org.gradle.api.initialization.Settings
import java.io.File

class CpuPartTaggingConvention : SettingsPluginConvention {

  override fun configure(target: Settings, develocity: DevelocityConfiguration) {
    val os = target.providers.systemProperty("os.name").get()
    val sysctlExec = target.providers.exec {
      isIgnoreExitValue = true
      commandLine("sysctl", "-n", "machdep.cpu.brand_string")
    }
    val wmicExec = target.providers.exec {
      isIgnoreExitValue = true
      commandLine("wmic", "cpu", "get", "name")
    }
    develocity.buildScan.background {
      val tagValue = when {
        os.contains("Mac", ignoreCase = true) ->
          sysctlExec.standardOutput.asText.orNull?.trim()

        os.contains("Linux", ignoreCase = true) ->
          findLinuxCpuPart(File("/proc/cpuinfo").readLines())

        os.contains("Windows", ignoreCase = true) ->
          wmicExec.standardOutput.asText.orNull?.let {
            findWindowsCpuPart(it.split("\n"))
          }

        else -> "Unknown CPU"
      }?.takeIf { it.isNotBlank() } ?: "Unknown CPU"
      tag(tagValue)
    }
  }

  fun findLinuxCpuPart(cpuinfo: List<String>): String? =
    cpuinfo.find { it.contains("model name") }?.split(":")?.last()?.trim()

  fun findWindowsCpuPart(wmic: List<String>): String =
    wmic.last { it.isNotBlank() }

}

package com.issue.repro

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GradleSettingsPluginTest {

  @field:TempDir(cleanup = CleanupMode.NEVER)
  lateinit var testProjectDir: File

  @Test
  fun  `settings plugin can apply cleanly`() {
    File(testProjectDir, "build.gradle.kts")
    val propsFile = File(testProjectDir, "gradle.properties")
    propsFile.writeText(
      //language=properties
      """
      group=com.issue.repro.test
      version=1.0.0-SNAPSHOT

      org.gradle.caching=true
      org.gradle.parallel=true
      org.gradle.configuration-cache=true
      org.gradle.configuration-cache-problems=fail
      org.gradle.configuration-cache.max-problems=0
      org.gradle.configuration-cache.parallel=true
      """.trimIndent()
    )
    val settingsFile = File(testProjectDir, "settings.gradle.kts")
    //language=kts
    settingsFile.writeText(
      """
      rootProject.name = "Settings-Convention-Plugin-Integration-Test-Project"
      pluginManagement {
          repositories {
              mavenLocal()
              gradlePluginPortal()
          }
      }
      plugins {
        id("com.issue.repro")
      }
      dependencyResolutionManagement {
          repositories {
              mavenCentral()
              google()
              gradlePluginPortal()
          }
      }
      develocity {
        allowUntrustedServer = true //Easier than adding the vpn cert to the JVM
        buildScan {
          termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
          termsOfUseAgree.set("yes")
        } 
      }
      """.trimIndent()
    )
    repeat(10) {
      settingsFile.appendText("\ninclude(\"sub-project-$it\")\n")
      val subDir = File(testProjectDir, "sub-project-$it")
      subDir.mkdir()
      val subBuild = File(subDir, "build.gradle.kts")
      subBuild.writeText(
        //language=kts
        """
        plugins {
          id("org.jetbrains.kotlin.jvm").version(embeddedKotlinVersion)
        }    
        kotlin {
            jvmToolchain(21)
        }
        tasks.withType<Test> {
          useJUnitPlatform()
        }
        dependencies {
          implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
          testImplementation(platform("org.junit:junit-bom:5.11.3"))
          testRuntimeOnly("org.junit.platform:junit-platform-launcher")
          testImplementation("org.junit.jupiter:junit-jupiter")
          testImplementation("org.jetbrains.kotlin:kotlin-test")
          testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        }
        """.trimIndent()
      )
      val mainSrcDir = File(subDir, "src/main/kotlin/com/repro/foo$it")
      mainSrcDir.mkdirs()
      val mainSrc = File(mainSrcDir, "MainFoo$it.kt")
      mainSrc.writeText(
        //language=kotlin
        """
        package com.repro.foo${it}    
        
        fun main(): String {
            return "Hello $it"
        }
        """.trimIndent()
      )

      val testSrcDir = File(subDir, "src/test/kotlin/com/repro/foo$it")
      testSrcDir.mkdirs()
      val testSrc = File(testSrcDir, "MainFoo${it}Test.kt")
      testSrc.writeText(
        //language=kotlin
        """
        package com.repro.foo${it}    

        import org.junit.jupiter.api.Test

        class MainFoo${it}Test {
        
          @Test
          fun testMain() {
            check(main().equals("Hello $it"))
          }
        }
        """.trimIndent()
      )
    }

    val firstRun = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withPluginClasspath()
      .forwardOutput()
      .withArguments("test")
      .build()

    assertTrue { firstRun.output.contains("Calculating task graph as no cached configuration is available for tasks: test") }
    assertTrue { firstRun.output.contains("0 problems were found storing the configuration cache") }

    assertTrue { firstRun.output.contains("Publishing build scan...") }

    val secondRun = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withPluginClasspath()
      .forwardOutput()
      .withArguments("test", "--info")
      .build()

    assertTrue { secondRun.output.contains("Reusing configuration cache.") }
    assertTrue { secondRun.output.contains("Configuration cache entry reused.") }
    assertTrue {
      secondRun.tasks
        .filter { it?.path?.endsWith(":test") == true }
        .all { it.outcome == TaskOutcome.UP_TO_DATE }
    }

    val cleanRun = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withPluginClasspath()
      .forwardOutput()
      .withArguments("clean")
      .build()
    assertTrue { cleanRun.output.contains("Calculating task graph as no cached configuration is available for tasks: clean") }

    val thirdRun = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withPluginClasspath()
      .forwardOutput()
      .withArguments("test", "--info")
      .build()

    assertTrue { thirdRun.output.contains("Reusing configuration cache.") }
    assertTrue { thirdRun.output.contains("Configuration cache entry reused.") }
    assertTrue {
      thirdRun.tasks
        .filter { it?.path?.endsWith(":test") == true }
        .all { it.outcome == TaskOutcome.FROM_CACHE }
    }
  }
}

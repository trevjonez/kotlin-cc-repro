package com.issue.repro

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CpuPartTaggingConventionTest {

  private val convention = CpuPartTaggingConvention()

  @Test
  fun findLinuxCpuPart() {
    assertEquals(
      "AMD Ryzen Threadripper 3990X 64-Core Processor",
      convention.findLinuxCpuPart(
        listOf(
          "processor       : 127",
          "vendor_id       : AuthenticAMD",
          "cpu family      : 23",
          "model           : 49",
          "model name      : AMD Ryzen Threadripper 3990X 64-Core Processor",
          "stepping        : 0",
          "microcode       : 0x8301039"
        )
      )
    )
  }

  @Test
  fun findWindowsCpuPart() {
    assertEquals(
      "Intel(R) Xeon(R) CPU @ 2.30GHz",
      convention.findWindowsCpuPart(
        listOf(
          "Name",
          "Intel(R) Xeon(R) CPU @ 2.30GHz"
        )
      )
    )
  }
}
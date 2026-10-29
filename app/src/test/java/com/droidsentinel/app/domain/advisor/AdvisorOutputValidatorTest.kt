package com.droidsentinel.app.domain.advisor

import com.droidsentinel.app.domain.model.AnalysisSource
import com.droidsentinel.app.domain.model.ScanFinding
import com.droidsentinel.app.domain.model.Severity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvisorOutputValidatorTest {
    private val fallback = AdvisorResponse("fallback", emptyList(), emptyList(), false)

    @Test
    fun rejectsMalformedJson() {
        val parsed = AdvisorOutputValidator.parseOrNull("not json", fallback)
        assertTrue(parsed.summary == "fallback")
        assertFalse(parsed.usedRemoteModel)
    }

    @Test
    fun parsesStructuredOutput() {
        val parsed = AdvisorOutputValidator.parseOrNull(
            """{"summary":"Review SMS access","nextSteps":["Open settings"],"caveats":["Not proof"]}""",
            fallback,
        )
        assertTrue(parsed.usedRemoteModel)
        assertTrue(parsed.summary.contains("SMS"))
    }

    @Test
    fun remoteCannotInventMalwareVerdict() {
        val response = AdvisorResponse("This app is malware.", emptyList(), emptyList(), true)
        val findings = listOf(
            ScanFinding("1", "META_SMS", "SMS", Severity.MEDIUM, listOf("SMS"), "x", "y", source = AnalysisSource.METADATA),
        )
        assertTrue(AdvisorOutputValidator.wouldOverrideEvidence(response, findings))
    }
}

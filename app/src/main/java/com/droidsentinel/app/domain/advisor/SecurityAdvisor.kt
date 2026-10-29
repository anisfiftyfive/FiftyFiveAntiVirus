package com.droidsentinel.app.domain.advisor

import com.droidsentinel.app.domain.model.AppScanSummary
import com.droidsentinel.app.domain.model.ScanFinding
import com.droidsentinel.app.domain.model.ScanResult
import com.droidsentinel.app.domain.model.SignatureScanStatus
import org.json.JSONObject

data class AdvisorResponse(
    val summary: String,
    val nextSteps: List<String>,
    val caveats: List<String>,
    val usedRemoteModel: Boolean,
)

interface SecurityAdvisor {
    fun explain(result: ScanResult): AdvisorResponse
    fun explain(summary: AppScanSummary): AdvisorResponse
}

class LocalSecurityAdvisor : SecurityAdvisor {
    override fun explain(result: ScanResult): AdvisorResponse {
        val next = mutableListOf<String>()
        val caveats = result.limitations.toMutableList()
        val summary = when (result.signatureStatus) {
            SignatureScanStatus.THREAT_DETECTED -> {
                next += "Review the signature evidence and uninstall any app you did not intend to keep."
                next += "A BloomFilter hit is a possible match, not a malware family name."
                "Hypatia reported a possible signature match. Treat this as a high-priority review, not a courtroom verdict."
            }
            SignatureScanStatus.NO_MATCH_FOUND -> {
                next += "Review metadata findings even when no signature matched."
                caveats += "No signature match only means the completed scan did not hit the loaded Hypatia set."
                "Signature scanning completed with no match in the loaded Hypatia databases."
            }
            SignatureScanStatus.DATABASE_UNAVAILABLE -> {
                next += "Open Settings and download the signed Hypatia databases."
                "Installed apps were inspected from metadata only because a verified signature database is not loaded."
            }
            SignatureScanStatus.NOT_SCANNED -> {
                next += "If you expected a file scan, pick an APK with the system document picker."
                "Signature scanning did not run for this target."
            }
            SignatureScanStatus.SCAN_FAILED -> {
                next += "Retry the scan. If it fails again, capture the error from Settings diagnostics."
                "The scanner failed before it could finish."
            }
            SignatureScanStatus.SCAN_INCOMPLETE -> {
                next += "Run the scan again and let it finish."
                "The scan was cancelled or only partially completed."
            }
        }
        if (result.metadataFindingCount > 0) {
            next += "Open each high-severity metadata finding and confirm the permission is needed."
        }
        return AdvisorResponse(summary, next.distinct(), caveats.distinct(), usedRemoteModel = false)
    }

    override fun explain(summary: AppScanSummary): AdvisorResponse {
        val high = summary.findings.filter { it.severity.ordinal >= 3 }
        val text = buildString {
            append(summary.app.label)
            append(" was inspected locally. ")
            append(
                when (summary.signature.status) {
                    SignatureScanStatus.THREAT_DETECTED -> "A possible Hypatia signature match was reported."
                    SignatureScanStatus.NO_MATCH_FOUND -> "No signature match was found for the readable APK."
                    else -> summary.signature.message ?: "Signature scanning did not complete for this app."
                },
            )
            if (high.isNotEmpty()) {
                append(" Metadata analysis flagged: ")
                append(high.joinToString { it.title })
                append('.')
            }
        }
        return AdvisorResponse(
            summary = text,
            nextSteps = summary.findings.take(3).map { it.recommendedAction },
            caveats = listOf("Permissions are contextual indicators, not proof of malware."),
            usedRemoteModel = false,
        )
    }
}

object AdvisorOutputValidator {
    fun parseOrNull(raw: String, fallback: AdvisorResponse): AdvisorResponse {
        return try {
            val json = JSONObject(raw)
            val summary = json.getString("summary").trim()
            val next = json.optJSONArray("nextSteps")?.let { array ->
                (0 until array.length()).map { array.getString(it) }
            }.orEmpty()
            val caveats = json.optJSONArray("caveats")?.let { array ->
                (0 until array.length()).map { array.getString(it) }
            }.orEmpty()
            if (summary.isBlank()) fallback else AdvisorResponse(summary, next, caveats, usedRemoteModel = true)
        } catch (_: Throwable) {
            fallback
        }
    }

    fun wouldOverrideEvidence(response: AdvisorResponse, findings: List<ScanFinding>): Boolean {
        val lowered = response.summary.lowercase()
        val claimsMalware = lowered.contains("is malware") || lowered.contains("confirmed malware")
        val hasSignature = findings.any { it.ruleId == "HYPATIA_MATCH" }
        return claimsMalware && !hasSignature
    }
}

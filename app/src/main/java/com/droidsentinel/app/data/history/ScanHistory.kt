package com.droidsentinel.app.data.history

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.droidsentinel.app.domain.model.ScanResult
import com.droidsentinel.app.domain.model.ScanTargetType
import com.droidsentinel.app.domain.model.SignatureScanStatus
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey val id: String,
    val targetName: String,
    val targetPackage: String?,
    val targetType: String,
    val startedAt: Long,
    val completedAt: Long?,
    val durationMs: Long,
    val signatureStatus: String,
    val databaseLabel: String?,
    val threatCount: Int,
    val metadataFindingCount: Int,
    val appsInspected: Int,
    val payloadJson: String,
)

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY startedAt DESC")
    fun observe(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ScanHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun clear()
}

@Database(entities = [ScanHistoryEntity::class], version = 1, exportSchema = false)
abstract class ScanHistoryDatabase : RoomDatabase() {
    abstract fun dao(): ScanHistoryDao
}

object ScanHistoryMapper {
    fun toEntity(result: ScanResult): ScanHistoryEntity {
        val payload = JSONObject()
            .put("limitations", JSONArray(result.limitations))
            .put("error", result.errorMessage)
            .put("sha256", result.hashes?.sha256)
            .put("md5", result.hashes?.md5)
            .put("scanned", result.appsSignatureScanned)
            .put("skipped", result.appsSkipped)
            .put("failed", result.appsFailed)
            .put("findingCount", result.findings.size)
        return ScanHistoryEntity(
            id = result.id,
            targetName = result.targetName,
            targetPackage = result.targetPackage,
            targetType = result.targetType.name,
            startedAt = result.startedAt,
            completedAt = result.completedAt,
            durationMs = result.durationMs,
            signatureStatus = result.signatureStatus.name,
            databaseLabel = result.databaseLabel,
            threatCount = result.threatCount,
            metadataFindingCount = result.metadataFindingCount,
            appsInspected = result.appsInspected,
            payloadJson = payload.toString(),
        )
    }

    fun status(entity: ScanHistoryEntity): SignatureScanStatus =
        runCatching { SignatureScanStatus.valueOf(entity.signatureStatus) }.getOrDefault(SignatureScanStatus.NOT_SCANNED)

    fun type(entity: ScanHistoryEntity): ScanTargetType =
        runCatching { ScanTargetType.valueOf(entity.targetType) }.getOrDefault(ScanTargetType.DEVICE)
}

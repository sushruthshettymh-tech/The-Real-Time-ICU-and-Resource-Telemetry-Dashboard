package com.example.data.local

import androidx.room.*
import com.example.data.model.HospitalResource
import com.example.data.model.ResourceLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospital_resources ORDER BY category ASC")
    fun getAllResources(): Flow<List<HospitalResource>>

    @Query("SELECT * FROM hospital_resources WHERE id = :id")
    suspend fun getResourceById(id: String): HospitalResource?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: HospitalResource)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<HospitalResource>)

    @Query("SELECT * FROM resource_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<ResourceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ResourceLog)

    @Query("DELETE FROM resource_logs")
    suspend fun clearAllLogs()

    @Transaction
    suspend fun updateResourceWithLog(resourceId: String, amount: Int, changeMsg: String) {
        val resource = getResourceById(resourceId) ?: return
        val newValue = (resource.currentValue + amount).coerceIn(0, resource.maxValue)
        val updatedResource = resource.copy(currentValue = newValue, lastUpdated = System.currentTimeMillis())
        insertResource(updatedResource)
        
        val log = ResourceLog(
            resourceId = resourceId,
            resourceName = resource.name,
            changeAmount = amount,
            newValue = newValue,
            message = changeMsg
        )
        insertLog(log)
    }
}

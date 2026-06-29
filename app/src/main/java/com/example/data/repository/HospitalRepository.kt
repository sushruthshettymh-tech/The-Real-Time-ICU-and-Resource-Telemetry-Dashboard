package com.example.data.repository

import com.example.data.local.HospitalDao
import com.example.data.model.HospitalResource
import com.example.data.model.ResourceLog
import kotlinx.coroutines.flow.Flow

class HospitalRepository(private val hospitalDao: HospitalDao) {

    val allResources: Flow<List<HospitalResource>> = hospitalDao.getAllResources()
    val allLogs: Flow<List<ResourceLog>> = hospitalDao.getAllLogs()

    suspend fun getResourceById(id: String): HospitalResource? {
        return hospitalDao.getResourceById(id)
    }

    suspend fun updateResourceValue(resourceId: String, amount: Int, message: String) {
        hospitalDao.updateResourceWithLog(resourceId, amount, message)
    }

    suspend fun insertResource(resource: HospitalResource) {
        hospitalDao.insertResource(resource)
    }

    suspend fun insertLog(log: ResourceLog) {
        hospitalDao.insertLog(log)
    }

    suspend fun clearLogs() {
        hospitalDao.clearAllLogs()
    }

    suspend fun resetDatabase() {
        hospitalDao.clearAllLogs()
        val defaultResources = listOf(
            HospitalResource("icu_beds", "ICU Bed Capacity", 35, 50, "Beds", "ICU Beds"),
            HospitalResource("ventilators", "Ventilator Usage", 12, 25, "Units", "Ventilators"),
            HospitalResource("oxygen_supply", "Oxygen Supply Level", 6500, 10000, "Liters", "Oxygen Supply"),
            HospitalResource("emergency_beds", "Emergency Ward Beds", 18, 30, "Beds", "ER Beds"),
            HospitalResource("ppe_kits", "PPE Kits Inventory", 920, 1500, "Kits", "Inventory")
        )
        hospitalDao.insertResources(defaultResources)
        
        val logs = listOf(
            ResourceLog(
                resourceId = "icu_beds",
                resourceName = "ICU Bed Capacity",
                changeAmount = 0,
                newValue = 35,
                message = "System database reset to initial values."
            ),
            ResourceLog(
                resourceId = "oxygen_supply",
                resourceName = "Oxygen Supply Level",
                changeAmount = 0,
                newValue = 6500,
                message = "Oxygen levels calibrated to 65% capacity."
            ),
            ResourceLog(
                resourceId = "ventilators",
                resourceName = "Ventilator Usage",
                changeAmount = 0,
                newValue = 12,
                message = "12 active ventilators tracked in telemetry."
            )
        )
        for (log in logs) {
            hospitalDao.insertLog(log)
        }
    }
}

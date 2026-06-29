package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.HospitalResource
import com.example.data.model.ResourceLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HospitalResource::class, ResourceLog::class], version = 1, exportSchema = false)
abstract class HospitalDatabase : RoomDatabase() {
    abstract fun hospitalDao(): HospitalDao

    companion object {
        @Volatile
        private var INSTANCE: HospitalDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HospitalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HospitalDatabase::class.java,
                    "hospital_resource_database"
                )
                .addCallback(HospitalDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class HospitalDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.hospitalDao())
                }
            }
        }

        suspend fun populateDatabase(dao: HospitalDao) {
            // Delete existing to start clean
            dao.clearAllLogs()
            
            val initialResources = listOf(
                HospitalResource(
                    id = "icu_beds",
                    name = "ICU Bed Capacity",
                    currentValue = 35,
                    maxValue = 50,
                    unit = "Beds",
                    category = "ICU Beds"
                ),
                HospitalResource(
                    id = "ventilators",
                    name = "Ventilator Usage",
                    currentValue = 12,
                    maxValue = 25,
                    unit = "Units",
                    category = "Ventilators"
                ),
                HospitalResource(
                    id = "oxygen_supply",
                    name = "Oxygen Supply Level",
                    currentValue = 6500,
                    maxValue = 10000,
                    unit = "Liters",
                    category = "Oxygen Supply"
                ),
                HospitalResource(
                    id = "emergency_beds",
                    name = "Emergency Ward Beds",
                    currentValue = 18,
                    maxValue = 30,
                    unit = "Beds",
                    category = "ER Beds"
                ),
                HospitalResource(
                    id = "ppe_kits",
                    name = "PPE Kits Inventory",
                    currentValue = 920,
                    maxValue = 1500,
                    unit = "Kits",
                    category = "Inventory"
                )
            )
            dao.insertResources(initialResources)

            // Seed some initial logs
            val initialLogs = listOf(
                ResourceLog(
                    resourceId = "icu_beds",
                    resourceName = "ICU Bed Capacity",
                    changeAmount = 0,
                    newValue = 35,
                    message = "System initialized: ICU capacity set to 35/50."
                ),
                ResourceLog(
                    resourceId = "oxygen_supply",
                    resourceName = "Oxygen Supply Level",
                    changeAmount = 0,
                    newValue = 6500,
                    message = "System initialized: Oxygen volume is 6,500L."
                ),
                ResourceLog(
                    resourceId = "ventilators",
                    resourceName = "Ventilator Usage",
                    changeAmount = 0,
                    newValue = 12,
                    message = "System initialized: 12 ventilators currently active."
                )
            )
            for (log in initialLogs) {
                dao.insertLog(log)
            }
        }
    }
}

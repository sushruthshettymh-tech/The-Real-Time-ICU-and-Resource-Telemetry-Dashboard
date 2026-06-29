package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HospitalDatabase
import com.example.data.model.HospitalResource
import com.example.data.model.ResourceLog
import com.example.data.repository.HospitalRepository
import com.example.data.api.GeminiClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HospitalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HospitalRepository
    
    val resources: StateFlow<List<HospitalResource>>
    val logs: StateFlow<List<ResourceLog>>

    // UI state for Gemini API Advisor
    private val _aiResponse = MutableStateFlow<String>("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow<Boolean>(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        val database = HospitalDatabase.getDatabase(application, viewModelScope)
        repository = HospitalRepository(database.hospitalDao())
        
        resources = repository.allResources
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        logs = repository.allLogs
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun updateResource(resourceId: String, amount: Int, reason: String) {
        viewModelScope.launch {
            val resource = repository.getResourceById(resourceId) ?: return@launch
            val changeWord = if (amount > 0) "increased" else "decreased"
            val absAmount = Math.abs(amount)
            val message = if (reason.isNotEmpty()) {
                reason
            } else {
                "${resource.name} $changeWord by $absAmount ${resource.unit}."
            }
            repository.updateResourceValue(resourceId, amount, message)
        }
    }

    fun saveNewCapacity(resourceId: String, newCapacity: Int) {
        viewModelScope.launch {
            val resource = repository.getResourceById(resourceId) ?: return@launch
            val updated = resource.copy(maxValue = newCapacity, lastUpdated = System.currentTimeMillis())
            repository.insertResource(updated)
            
            val log = ResourceLog(
                resourceId = resourceId,
                resourceName = resource.name,
                changeAmount = 0,
                newValue = resource.currentValue,
                message = "Facility capacity limits updated: set max threshold limit to $newCapacity ${resource.unit}."
            )
            repository.insertLog(log)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }

    // Trigger AI report / predictive analytics based on CURRENT database state
    fun runAiAnalysis() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Analyzing live hospital data and formulating predictive metrics..."
            
            // Build current snapshot prompt
            val snapshotText = resources.value.joinToString("\n") { 
                "- ${it.name}: ${it.currentValue}/${it.maxValue} ${it.unit} (${(it.currentValue.toFloat() / it.maxValue * 100).toInt()}% utilized)"
            }

            val prompt = """
                Here is the current live real-time status of our hospital resources:
                $snapshotText

                Please perform a professional medical resource audit:
                1. Identify any critical bottlenecks, low supplies, or over-utilization (e.g., ICU beds above 80%, oxygen below 30%, or high ventilator utilization).
                2. Estimate when resources like Oxygen might run out based on basic safety assumptions.
                3. Provide 3 highly practical recommendations for resource optimization and staffing allocation.
                4. Give a brief summary score (e.g., Hospital Readiness Status: GOOD, WARN, or CRITICAL).
            """.trimIndent()

            val response = GeminiClient.generateResponse(prompt)
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }
    
    // Trigger custom AI question answering
    fun askAiAdvisor(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResponse.value = "Consulting clinical database..."
            
            val snapshotText = resources.value.joinToString("\n") { 
                "- ${it.name}: ${it.currentValue}/${it.maxValue} ${it.unit}"
            }

            val prompt = """
                The user has asked the following clinical resource question:
                "$question"

                Current Hospital Resources Status:
                $snapshotText

                Please answer the question accurately, focusing on how our current resource levels impact patient care and providing clear, actionable medical resource planning advice.
            """.trimIndent()

            val response = GeminiClient.generateResponse(prompt)
            _aiResponse.value = response
            _isAiLoading.value = false
        }
    }
}

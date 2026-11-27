package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.DatasetAnalysis
import com.example.data.model.MetricDataPoint
import com.example.data.repository.TraceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DataWorkspaceViewModel(private val repository: TraceRepository) : ViewModel() {

    private val datasets = repository.getAnalysisDatasets()

    private val _selectedDatasetIndex = MutableStateFlow(0)
    val selectedDatasetIndex: StateFlow<Int> = _selectedDatasetIndex.asStateFlow()

    private val _customDataPoints = MutableStateFlow<List<MetricDataPoint>>(
        listOf(
            MetricDataPoint("Northern Farmers Farmgate", 4000f, 0f, "Price/Crate", "NGN"),
            MetricDataPoint("Lagos Wholesale Intermediary", 12500f, 0f, "Price/Crate", "NGN"),
            MetricDataPoint("Retail Final Market", 18000f, 0f, "Price/Crate", "NGN")
        )
    )
    val customDataPoints: StateFlow<List<MetricDataPoint>> = _customDataPoints.asStateFlow()

    fun selectDataset(index: Int) {
        if (index in datasets.indices) {
            _selectedDatasetIndex.value = index
        }
    }

    fun getDatasets(): List<DatasetAnalysis> = datasets

    fun currentDataset(): DatasetAnalysis = datasets[_selectedDatasetIndex.value]

    fun addCustomDataPoint(label: String, value: Float, category: String, unit: String) {
        if (label.isBlank() || value <= 0f) return
        _customDataPoints.value = _customDataPoints.value + MetricDataPoint(label, value, 0f, category, unit)
    }

    fun removeCustomDataPoint(index: Int) {
        if (index in _customDataPoints.value.indices) {
            _customDataPoints.value = _customDataPoints.value.filterIndexed { i, _ -> i != index }
        }
    }
}

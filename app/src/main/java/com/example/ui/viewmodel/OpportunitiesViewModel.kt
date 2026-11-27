package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CatalogOpportunity
import com.example.data.model.TrackedOpportunity
import com.example.data.repository.LiveGrantsRepository
import com.example.data.repository.TraceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OpportunitiesViewModel(
    private val repository: TraceRepository,
    private val liveGrantsRepository: LiveGrantsRepository? = null
) : ViewModel() {

    private val curatedCatalog = repository.getCatalogOpportunities()

    private val liveGrantsFlow: Flow<List<CatalogOpportunity>> =
        liveGrantsRepository?.observeLiveGrants()?.catch { emit(emptyList()) } ?: flowOf(emptyList())

    val trackedOpportunities: StateFlow<List<TrackedOpportunity>> = repository.observeTrackedOpportunities()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedOpportunity = MutableStateFlow<CatalogOpportunity?>(null)
    val selectedOpportunity: StateFlow<CatalogOpportunity?> = _selectedOpportunity.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val _activeLiveKeyword = MutableStateFlow("")
    val activeLiveKeyword: StateFlow<String> = _activeLiveKeyword.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    val lastSyncMillis: StateFlow<Long?> =
        (liveGrantsRepository?.observeLastSyncMillis()?.catch { emit(null) } ?: flowOf(null))
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = null
            )

    val liveGrantCount: StateFlow<Int> = liveGrantsFlow
        .combine(flowOf(Unit)) { list, _ -> list.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )

    val catalogOpportunities: StateFlow<List<CatalogOpportunity>> = combine(
        liveGrantsFlow,
        _selectedCategoryFilter,
        _searchQuery
    ) { liveList, cat, rawQuery ->
        val merged = liveList + curatedCatalog
        val query = rawQuery.trim().lowercase()

        merged.filter { opp ->
            val matchesCat = cat == "All" || opp.category.equals(cat, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                opp.title.lowercase().contains(query) ||
                opp.organization.lowercase().contains(query) ||
                opp.focusArea.lowercase().contains(query) ||
                opp.description.lowercase().contains(query)
            matchesCat && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = curatedCatalog
    )

    init {
        viewModelScope.launch {
            if (liveGrantsRepository != null && liveGrantsRepository.isCacheStale()) {
                refreshLiveGrants(keyword = "")
            }
        }
    }

    fun getCatalog(): List<CatalogOpportunity> = catalogOpportunities.value

    fun refreshLiveGrants(keyword: String = "") {
        val repo = liveGrantsRepository ?: return
        if (_isSyncing.value) return

        val trimmedKeyword = keyword.trim()
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            _activeLiveKeyword.value = trimmedKeyword

            val result = repo.syncLiveGrants(trimmedKeyword)
            result.onFailure { err ->
                _syncError.value = err.message ?: "Unable to reach Grants.gov live feed."
            }
            _isSyncing.value = false
        }
    }

    fun dismissSyncError() {
        _syncError.value = null
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectOpportunity(opp: CatalogOpportunity) {
        _selectedOpportunity.value = opp
        if (opp.isLiveFeed && liveGrantsRepository != null) {
            viewModelScope.launch {
                _isLoadingDetail.value = true
                liveGrantsRepository.fetchLiveOpportunityDetail(opp.id)
                    .onSuccess { enriched ->
                        if (enriched != null && _selectedOpportunity.value?.id == opp.id) {
                            _selectedOpportunity.value = enriched
                        }
                    }
                _isLoadingDetail.value = false
            }
        }
    }

    fun clearSelectedOpportunity() {
        _selectedOpportunity.value = null
        _isLoadingDetail.value = false
    }

    fun trackOpportunity(opp: CatalogOpportunity, stage: String, notes: String) {
        viewModelScope.launch {
            repository.trackOpportunity(opp, stage, notes)
        }
    }

    fun updateTrackedStage(oppId: String, newStage: String, notes: String?) {
        viewModelScope.launch {
            repository.updateTrackedStage(oppId, newStage, notes)
        }
    }
}

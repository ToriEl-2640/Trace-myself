package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LibraryItem
import com.example.data.repository.AiResearchRepository
import com.example.data.repository.AiResult
import com.example.data.repository.TraceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: TraceRepository,
    private val aiRepository: AiResearchRepository
) : ViewModel() {

    val libraryItems: StateFlow<List<LibraryItem>> = repository.observeLibrary()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _selectedDomainFilter = MutableStateFlow("All")
    val selectedDomainFilter: StateFlow<String> = _selectedDomainFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSimplifying = MutableStateFlow<String?>(null) // itemId being simplified
    val isSimplifying: StateFlow<String?> = _isSimplifying.asStateFlow()

    fun setDomainFilter(domain: String) {
        _selectedDomainFilter.value = domain
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(item: LibraryItem) {
        viewModelScope.launch {
            repository.toggleBookmark(item.id, !item.isBookmarked)
        }
    }

    fun addCustomItem(
        title: String,
        source: String,
        authors: String,
        year: String,
        domain: String,
        summary: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addLibraryItem(
                title = title,
                source = source,
                authors = authors,
                publicationYear = year,
                domain = domain,
                summary = summary,
                notes = notes
            )
        }
    }

    fun requestAiSimplification(item: LibraryItem) {
        val textToSimplify = if (item.summary.isNotBlank()) item.summary else item.title
        _isSimplifying.value = item.id
        viewModelScope.launch {
            when (val res = aiRepository.simplifyResearchPaper(textToSimplify)) {
                is AiResult.Success -> {
                    // Save simplified output into library item notes
                    repository.addLibraryItem(
                        title = "Simplified: ${item.title}".take(100),
                        source = "${item.source} (TRACE Plain English)",
                        authors = item.authors,
                        publicationYear = item.publicationYear,
                        domain = item.domain,
                        summary = item.summary,
                        simplifiedExplanation = res.data,
                        notes = res.data
                    )
                }
                is AiResult.Error -> {
                    // No-op
                }
            }
            _isSimplifying.value = null
        }
    }
}

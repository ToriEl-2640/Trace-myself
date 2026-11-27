package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ResearchTopic
import com.example.data.model.TopicCategory
import com.example.data.repository.TraceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopicExplorerViewModel(private val repository: TraceRepository) : ViewModel() {

    private val allTopics = repository.getCuratedTopics()

    private val _selectedCategory = MutableStateFlow<TopicCategory?>(null)
    val selectedCategory: StateFlow<TopicCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredTopics = MutableStateFlow<List<ResearchTopic>>(allTopics)
    val filteredTopics: StateFlow<List<ResearchTopic>> = _filteredTopics.asStateFlow()

    private val _selectedTopic = MutableStateFlow<ResearchTopic?>(null)
    val selectedTopic: StateFlow<ResearchTopic?> = _selectedTopic.asStateFlow()

    fun selectCategory(category: TopicCategory?) {
        _selectedCategory.value = category
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun selectTopic(topic: ResearchTopic) {
        _selectedTopic.value = topic
    }

    fun clearSelectedTopic() {
        _selectedTopic.value = null
    }

    private fun applyFilters() {
        val cat = _selectedCategory.value
        val query = _searchQuery.value.trim().lowercase()

        _filteredTopics.value = allTopics.filter { topic ->
            val matchesCategory = cat == null || topic.category == cat
            val matchesQuery = query.isBlank() ||
                    topic.title.lowercase().contains(query) ||
                    topic.sector.lowercase().contains(query) ||
                    topic.region.lowercase().contains(query) ||
                    topic.overview.lowercase().contains(query) ||
                    topic.relatedDisciplines.any { it.lowercase().contains(query) }
            matchesCategory && matchesQuery
        }
    }

    fun importTopicToLab(topic: ResearchTopic, onImported: (String) -> Unit) {
        viewModelScope.launch {
            val newId = repository.createProblem(
                title = topic.title,
                sector = topic.sector,
                location = topic.region,
                problemStatement = topic.overview,
                tags = listOf(topic.category.displayName, topic.sector) + topic.relatedDisciplines.take(2)
            )
            onImported(newId)
        }
    }
}

package com.studyflow.app.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.database.StudyFlowDatabase
import com.studyflow.app.data.database.SubjectEntity
import com.studyflow.app.data.repository.StudyRepository
import com.studyflow.app.model.SubjectWithStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SubjectListState(
    val subjects: List<SubjectWithStats> = emptyList(),
    val isLoading: Boolean = true
)

class SubjectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(
        StudyFlowDatabase.getInstance(application).subjectDao(),
        StudyFlowDatabase.getInstance(application).sessionDao(),
        StudyFlowDatabase.getInstance(application).streakDao()
    )

    private val _state = MutableStateFlow(SubjectListState())
    val state: StateFlow<SubjectListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allSubjects.collectLatest { subjects ->
                buildSubjectStats(subjects)
            }
        }
    }

    private suspend fun buildSubjectStats(subjects: List<SubjectEntity>) {
        val statsList = subjects.map { subject ->
            val minutes = repository.getSubjectMinutes(subject.id).first() ?: 0
            SubjectWithStats(
                id = subject.id,
                name = subject.name,
                colorIndex = subject.colorIndex,
                iconName = subject.iconResName,
                totalMinutes = minutes,
                sessionCount = 0,
                targetHoursPerWeek = subject.targetHoursPerWeek
            )
        }
        _state.update {
            it.copy(subjects = statsList, isLoading = false)
        }
    }

    fun addSubject(name: String, colorIndex: Int, targetHours: Int) {
        viewModelScope.launch {
            repository.addSubject(
                SubjectEntity(
                    name = name,
                    colorIndex = colorIndex,
                    targetHoursPerWeek = targetHours
                )
            )
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch { repository.updateSubject(subject) }
    }

    fun deleteSubject(subject: SubjectWithStats) {
        viewModelScope.launch {
            repository.deleteSubject(
                SubjectEntity(
                    id = subject.id,
                    name = subject.name,
                    colorIndex = subject.colorIndex
                )
            )
        }
    }

    fun archiveSubject(id: Long) {
        viewModelScope.launch { repository.deleteSubject(SubjectEntity(id = id, name = "")) }
    }

    fun refresh() {
        viewModelScope.launch {
            val subjects = repository.getAllSubjectsList()
            buildSubjectStats(subjects)
        }
    }
}

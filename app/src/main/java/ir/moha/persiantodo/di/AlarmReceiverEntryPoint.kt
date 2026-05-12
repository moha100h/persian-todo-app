package ir.moha.persiantodo.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.moha.persiantodo.domain.repository.TaskRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmReceiverEntryPoint {
    fun taskRepository(): TaskRepository
}

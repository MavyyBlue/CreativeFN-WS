package dev.creativelogic.model

import kotlinx.coroutines.flow.Flow

interface MechanicRepository {
    fun observeAll(): Flow<List<MechanicDocumentV1>>
    suspend fun save(document: MechanicDocumentV1)
}

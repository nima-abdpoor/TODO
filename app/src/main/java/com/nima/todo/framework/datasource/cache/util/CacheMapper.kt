package com.nima.todo.framework.datasource.cache.util

import com.nima.todo.business.domain.model.Note
import com.nima.todo.business.domain.util.EntityMapper
import com.nima.todo.framework.datasource.cache.model.NoteCacheEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheMapper @Inject constructor() : EntityMapper<NoteCacheEntity, Note> {

    override fun mapFromEntity(entity: NoteCacheEntity): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            updated_at = entity.updated_at,
            created_at = entity.created_at
        )
    }

    override fun mapToEntity(domainModel: Note): NoteCacheEntity {
        return NoteCacheEntity(
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body,
            updated_at = domainModel.updated_at,
            created_at = domainModel.created_at
        )
    }

    fun entityListToNoteList(entities: List<NoteCacheEntity>): List<Note> {
        return entities.map { en -> mapFromEntity(en) }
    }

    fun noteListToEntityList(notes: List<Note>): List<NoteCacheEntity> {
        return notes.map { note -> mapToEntity(note) }
    }
}
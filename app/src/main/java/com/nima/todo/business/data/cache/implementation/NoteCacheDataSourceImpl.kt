package com.nima.todo.business.data.cache.implementation

import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.domain.model.Note
import com.nima.todo.framework.datasource.cache.abstraction.NoteDaoService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCacheDataSourceImpl
@Inject
constructor(
    private val noteDaoService: NoteDaoService
) : NoteCacheDataSource {
    override suspend fun insertNote(note: Note) = noteDaoService.insertNote(note)

    override suspend fun deleteNote(primaryKey: String) = noteDaoService.deleteNote(primaryKey)

    override suspend fun deleteNotes(notes: List<Note>) =
        noteDaoService.deleteNotes(notes)

    override suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String?, timestamp: String?) =
        noteDaoService.updateNote(primaryKey, newTitle, newBody,timestamp)

    override suspend fun searchNotes(query: String, filterAndOrder: String, page: Int) =
        noteDaoService.returnOrderedQuery(query, filterAndOrder, page)

    override suspend fun searchNoteById(id: String) =
        noteDaoService.searchNoteById(id)

    override suspend fun getNumNotes() =
        noteDaoService.getNumNotes()

    override suspend fun insertNotes(notes: List<Note>) =
        noteDaoService.insertNotes(notes)

    override suspend fun getAllNotes(): List<Note> {
        return noteDaoService.getAllNotes()
    }
}
package com.nima.todo.business.data.network.implimentation

import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.domain.model.Note
import com.nima.todo.framework.datasource.network.abstraction.NoteFireStoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteNetworkDataSourceImpl @Inject
constructor(
    private val fireStoreService  : NoteFireStoreService
) : NoteNetworkDataSource{
    override suspend fun insertOrUpdateNote(note: Note) {
        return fireStoreService.insertOrUpdateNote(note)
    }

    override suspend fun deleteNote(primaryKey: String) {
        return fireStoreService.deleteNote(primaryKey)
    }

    override suspend fun insertDeletedNote(note: Note) {
        return fireStoreService.insertDeletedNote(note)
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        return fireStoreService.insertDeletedNotes(notes)
    }

    override suspend fun deleteDeletedNote(note: Note) {
        return fireStoreService.deleteDeletedNote(note)
    }

    override suspend fun getDeletedNotes(): List<Note> {
        return fireStoreService.getDeletedNotes()
    }

    override suspend fun deleteAllNotes() {
        fireStoreService.deleteAllNotes()
    }

    override suspend fun searchNote(note: Note): Note? {
        return fireStoreService.searchNote(note)
    }

    override suspend fun getAllNotes(): List<Note> {
        return fireStoreService.getAllNotes()
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        return fireStoreService.insertOrUpdateNotes(notes)
    }

}

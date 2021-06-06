package com.nima.todo.business.interactors

import com.nima.todo.business.data.cache.abstraction.NoteCacheDataSource
import com.nima.todo.business.data.network.abstraction.NoteNetworkDataSource
import com.nima.todo.business.domain.model.NoteFactory
import com.nima.todo.business.interactors.notelist.InsertNewNotes
import com.nima.todo.di.DependencyContainer

class InsertNewNoteTest {
    private val insertNewNote: InsertNewNotes

    // dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        insertNewNote = InsertNewNotes(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }
}
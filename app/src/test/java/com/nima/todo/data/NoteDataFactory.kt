package com.nima.todo.data

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.nima.todo.business.domain.model.Note

class NoteDataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceListOfNotes(): List<Note> {
        return Gson()
            .fromJson(
                getNotesFromFile("note_list.json"),
                object : TypeToken<List<Note>>() {}.type
            )
    }

    fun produceHashMapOfNotes(noteList: List<Note>): HashMap<String, Note>{
        val map = HashMap<String, Note>()
        for(note in noteList){
            map[note.id] = note
        }
        return map
    }

    fun produceEmptyListOfNotes(): List<Note>{
        return ArrayList()
    }

    private fun getNotesFromFile(fileName: String): String {
        return testClassLoader.getResource(fileName).readText()
    }

}
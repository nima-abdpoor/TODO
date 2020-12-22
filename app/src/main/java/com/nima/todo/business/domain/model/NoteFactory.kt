package com.nima.todo.business.domain.model

import com.nima.todo.business.domain.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


@Singleton
class NoteFactory
@Inject
constructor(
    private val dateUtil: DateUtil
) {
     fun createSingleNote(
        id: String? = null,
        title: String,
        body: String? = null
    ) : Note{
        return Note(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            body = body ?: "" ,
            updated_at = dateUtil.getCurrentTimestamp(),
            created_at = dateUtil.getCurrentTimestamp()
        )
    }

    fun createNoteList(numNotes : Int) : List<Note> {
        val list =  ArrayList<Note>()
        for (i in 0 until numNotes){
            list.add(
                createSingleNote(
                    id = null,
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString()
                )
            )
        }
        return list
    }
}
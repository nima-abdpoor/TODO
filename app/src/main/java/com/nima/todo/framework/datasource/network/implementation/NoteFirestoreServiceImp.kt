package com.nima.todo.framework.datasource.network.implementation

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nima.todo.business.domain.model.Note
import com.nima.todo.framework.datasource.network.abstraction.NoteFireStoreService
import com.nima.todo.framework.datasource.network.mappers.NetworkMapper
import com.nima.todo.framework.datasource.network.model.NoteNetworkEntity
import com.nima.todo.util.cLog
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteFirestoreServiceImp @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val fireStore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
) : NoteFireStoreService {

    override suspend fun insertOrUpdateNote(note: Note) {
        val entity = networkMapper.mapToEntity(note)
        entity.updated_at = Timestamp.now()
        fireStore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener { cLog(it.message) }
            .await()
    }

    override suspend fun deleteNote(primaryKey: String) {
        fireStore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(primaryKey)
            .delete()
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    override suspend fun insertDeletedNote(note: Note) {
        val entity = networkMapper.mapToEntity(note)
        fireStore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        if (notes.size > 500) {
            throw Exception("Cannot delete more than 500 notes at a time in firestore.")
        }

        val collectionRef = fireStore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)

        fireStore.runBatch { batch ->
            for (note in notes) {
                val documentRef = collectionRef.document(note.id)
                batch.set(documentRef, networkMapper.mapToEntity(note))
            }
        }.addOnFailureListener {
            // send error reports to Firebase Crashlytics
            cLog(it.message)
        }.await()
    }

    override suspend fun deleteDeletedNote(note: Note) {
        val entity = networkMapper.mapToEntity(note)
        fireStore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(entity.id)
            .delete()
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    override suspend fun getDeletedNotes(): List<Note> {
        return networkMapper.entityListToNoteList(
            fireStore
                .collection(DELETES_COLLECTION)
                .document(USER_ID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {
                    // send error reports to Firebase Crashlytics
                    cLog(it.message)
                }
                .await().toObjects(NoteNetworkEntity::class.java)
        )
    }

    override suspend fun deleteAllNotes() {
        fireStore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
        fireStore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
    }

    override suspend fun searchNote(note: Note): Note? {
        return fireStore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)
            .document(note.id)
            .get()
            .await()
            .toObject(NoteNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }
    }

    override suspend fun getAllNotes(): List<Note> {
        return networkMapper.entityListToNoteList(
            fireStore
                .collection(NOTES_COLLECTION)
                .document(USER_ID)
                .collection(NOTES_COLLECTION)
                .get()
                .addOnFailureListener {
                    // send error reports to Firebase Crashlytics
                    cLog(it.message)
                }
                .await()
                .toObjects(NoteNetworkEntity::class.java)
        )
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        if (notes.size >= 500) {
            throw RuntimeException("Cannot insert more than 500 notes at a time into fireStore.")
        }
        val collectionRef = fireStore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)

        fireStore.runBatch { batch ->
            for (note in notes) {
                val entity = networkMapper.mapToEntity(note)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef.document(entity.id)
                batch.set(documentRef, entity)
            }
        }.addOnFailureListener { cLog(it.message) }
            .await()
    }

    companion object {
        const val NOTES_COLLECTION = "notes"
        const val USERS_COLLECTION = "users"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID = "sNueppCFTvSSJGJKiKrqV4BFEii2" // hardcoded for single user
        const val EMAIL = "nimaabdpoor@gmail.com"
    }
}
package fi.tuni.sinipelto.laskuvelho.data.repository

import androidx.annotation.WorkerThread
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query

interface IRepository<TEntity> {

    @WorkerThread
    suspend fun getAll(): Query

    @WorkerThread
    suspend fun get(id: String): DocumentReference

    @WorkerThread
    suspend fun insert(entity: TEntity)

    @WorkerThread
    suspend fun update(entity: TEntity)

    @WorkerThread
    suspend fun remove(entity: TEntity)
}
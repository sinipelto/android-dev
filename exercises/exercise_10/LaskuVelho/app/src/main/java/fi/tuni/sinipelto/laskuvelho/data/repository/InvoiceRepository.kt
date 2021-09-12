package fi.tuni.sinipelto.laskuvelho.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity

class InvoiceRepository(private val db: FirebaseFirestore) :
    IRepository<InvoiceEntity> {

    private fun colRef(): CollectionReference =
        db.collection(DataConstants.COLLECTION_NAME)
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "unauth")
            .collection(DataConstants.SUBCOLLECTION_NAME)

    private fun allInvoicesDueDescQuery(): Query =
        colRef().orderBy("dueDate", Query.Direction.ASCENDING) // Order by due date, oldest first

    /// Get all documents in correct order as POJOs
    @WorkerThread
    override suspend fun getAll(): Query = allInvoicesDueDescQuery()

    /// Get single document as POJO
    @WorkerThread
    override suspend fun get(id: String): DocumentReference {
        return colRef().document(id)
    }

    /// Insert a brand new invoice to DB. Auto-generates new document ID. WARNING: non-idempodent!
    @WorkerThread
    override suspend fun insert(entity: InvoiceEntity) {
        // Dont know ID yet => use coll.add not coll.set
        colRef().add(entity).addOnSuccessListener {
            Log.d(DataConstants.LOG_TAG, "Entity added to db successfully.")
        }.addOnFailureListener {
            Log.e(DataConstants.LOG_TAG, "Failed to insert new document: ${it.message}")
            throw it
        }
    }

    @WorkerThread
    override suspend fun update(entity: InvoiceEntity) {
        colRef().document(entity.uid!!).set(entity)
            .addOnSuccessListener { Log.d(DataConstants.LOG_TAG, "Entity updated successfully.") }
            .addOnFailureListener {
                Log.e(DataConstants.LOG_TAG, "Could not update document: ${it.message}")
                throw it
            }
    }

    @WorkerThread
    override suspend fun remove(entity: InvoiceEntity) {
        colRef().document(entity.uid!!).delete()
    }

}
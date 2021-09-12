package fi.tuni.sinipelto.laskuvelho.data

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import fi.tuni.sinipelto.laskuvelho.data.model.InvoiceType
import java.util.*

abstract class AppDatabase {

    // Companion for static access (no object instance)
    companion object {

        // Static database Singleton instance stored here
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: FirebaseFirestore? = null

        // Get existing database instance or create new if does not exist yet
        // Lazy loading (not created until requested)
        fun getDatabase(): FirebaseFirestore {
            // We need to lock down the creation process to avoid multiple threads to create the
            // databse instance at the same time
            return INSTANCE ?: synchronized(this) {
                Log.d(DataConstants.LOG_TAG, "Creating database...")
                // Create NON-NULL type of instance inside sync context
                val inst = FirebaseFirestore.getInstance()
                // Set this instance to the created database object
                INSTANCE = inst
                inst // Sync instance
            }
        }

        // Hack to prevent compiler warning 'param always 20' => param count e.g.: "15".toInt()
        private fun generateRandomEntries(count: Int): Collection<InvoiceEntity> {
            val rnd = Random(Date().time)
            val list = mutableListOf<InvoiceEntity>()
            for (i in 0..count) {
                list.add(PREP_DATA[rnd.nextInt(PREP_DATA.size)]) // rnd elem betw 0.. size - 1
            }
            return list
        }

        // Private collection of data to be inserted on creation
        // Date(EpochMilliseconds: Long)
        private val PREP_DATA = listOf(
            InvoiceEntity(
                null,
                "Tampereen Vesilaitos Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.WaterInvoice),
                15.30,
                Date(1_600_000_000_000)
            ),
            InvoiceEntity(
                null,
                "Telia Oyj",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                9.99,
                Date(1_150_000_000_000)
            ),
            InvoiceEntity(
                null,
                "Pikavippi Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.CreditInvoice),
                137.68,
                Date(1_050_055_000_000)
            ),
            InvoiceEntity(
                null,
                "Huoltopalvelu Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.MaintenanceInvoice),
                95.00,
                Date(1_300_000_000_000)
            ),
            InvoiceEntity(
                null,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.MaintenanceInvoice),
                9999999999.52838892377895,
                Date(1_300_000_000_000)
            ),
            InvoiceEntity(
                null,
                "Elisa Oyj",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                9.99,
                Date(1_150_000_000_000)
            ),
            InvoiceEntity(
                null,
                "DNA Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                9.99,
                Date(1_150_000_000_000)
            ),
            InvoiceEntity(
                null,
                "Puhelinfirma Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                10.238763284576,
                Date(1_700_000_000_000)
            ),
            InvoiceEntity(
                null,
                "SATO",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.RentInvoice),
                697.238763284576,
                Date()
            ),
            InvoiceEntity(
                null,
                "Verkkokauppa.com Oyj / Apuraha",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.EcommerceInvoice),
                100.990000103127362167,
                Date(Date().time + 1_000_000)
            ),
        )
    }
}
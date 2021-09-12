package fi.tuni.sinipelto.laskuvelho.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import fi.tuni.sinipelto.laskuvelho.data.constant.DataConstants
import fi.tuni.sinipelto.laskuvelho.data.dao.InvoiceDao
import fi.tuni.sinipelto.laskuvelho.data.entity.InvoiceEntity
import fi.tuni.sinipelto.laskuvelho.data.model.InvoiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

// No migration plan for the database in this application => do NOT export the db schema to file
// A global application-level database
@Database(entities = [InvoiceEntity::class], version = 1, exportSchema = false)
@TypeConverters(fi.tuni.sinipelto.laskuvelho.data.converter.TypeConverters::class) // We need a type converter class to handle non-primitive types between app <-> db
abstract class AppDatabase : RoomDatabase() {

    // Connect DAO for invoices table
    abstract fun invoiceDao(): InvoiceDao

    // Companion for static access (no object instance)
    companion object {
        // Static database Singleton instance stored here
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Get existing database instance or create new if does not exist yet
        // Lazy loading (not created until requested)
        fun getDatabase(ctx: Context/*, scope: CoroutineScope*/): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(DataConstants.LOG_TAG, "Creating database...")
                // Create NON-NULL type of instance inside sync context
                val inst = Room.databaseBuilder(
                    ctx.applicationContext,
                    AppDatabase::class.java,
                    DataConstants.APP_DATABASE_NAME
                )
//                    .addCallback(DatabasePopulator(scope)) // Enabled for DEV purposes
                    .build()

                // Set this instance to the created database object
                INSTANCE = inst
                inst
            }
        }

        // Class for pre-populating the database on creation
        private class DatabasePopulator(private val scope: CoroutineScope) : Callback() {
            // Override database creation function
            override fun onCreate(db: SupportSQLiteDatabase) {
                // Do all parent operations first
                super.onCreate(db)
                Log.d(DataConstants.LOG_TAG, "Populating datbase with prepopulated values...")
                scope.launch { // Inside the let scope (db not null), insert values to the db using the DAO obj
                    INSTANCE?.let { db ->
                        scope.launch {
                            val dao = db.invoiceDao()
                            dao.deleteAllInvoices()
                            dao.insertInvoices(PREP_DATA)
                        }
                    }
                }
                Log.d(DataConstants.LOG_TAG, "Database pre-population done.")
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
                0,
                "Tampereen Vesilaitos Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.WaterInvoice),
                15.30,
                Date(1_600_000_000_000)
            ),
            InvoiceEntity(
                0,
                "Telia Oyj",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                9.99,
                Date(1_150_000_000_000)
            ),
            InvoiceEntity(
                0,
                "Pikavippi Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.CreditInvoice),
                137.68,
                Date(1_050_055_000_000)
            ),
            InvoiceEntity(
                0,
                "Huoltopalvelu Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.MaintenanceInvoice),
                95.00,
                Date(1_300_000_000_000)
            ),
            InvoiceEntity(
                0,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.MaintenanceInvoice),
                9999999999.52838892377895,
                Date(1_300_000_000_000)
            ),
            InvoiceEntity(
                0,
                "Elisa Oyj",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                9.99,
                Date(1_150_000_000_000)
            ),
            InvoiceEntity(
                0,
                "DNA Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                9.99,
                Date(1_150_000_000_000)
            ),
            InvoiceEntity(
                0,
                "Puhelinfirma Oy",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.PhoneInvoice),
                10.238763284576,
                Date(1_700_000_000_000)
            ),
            InvoiceEntity(
                0,
                "SATO",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.RentInvoice),
                697.238763284576,
                Date()
            ),
            InvoiceEntity(
                0,
                "Verkkokauppa.com Oyj / Apuraha",
                InvoiceType.getInvoiceTypeCodeById(InvoiceType.Companion.Type.EcommerceInvoice),
                100.990000103127362167,
                Date(Date().time + 1_000_000)
            ),
        )
    }
}
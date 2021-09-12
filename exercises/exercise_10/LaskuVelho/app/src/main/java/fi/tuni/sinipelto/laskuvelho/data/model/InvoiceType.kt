package fi.tuni.sinipelto.laskuvelho.data.model

import android.content.Context
import fi.tuni.sinipelto.laskuvelho.R

// Static mapping of available invoice types
// Does not change after compilation (readonly), thus can be static
class InvoiceType(context: Context, val id: Type) {

    private val STR_MAPPING: Map<Type, String> = mapOf(
        Type.OtherInvoice to context.getString(R.string.invoicetype_other),
        Type.TravelInvoice to context.getString(R.string.invoicetype_travel),
        Type.MaintenanceInvoice to context.getString(R.string.invoicetype_maintenance),
        Type.ElectricityInvoice to context.getString(R.string.invoicetype_electricity),
        Type.CreditInvoice to context.getString(R.string.invoicetype_credit),
        Type.WaterInvoice to context.getString(R.string.invoicetype_water),
        Type.RentInvoice to context.getString(R.string.invoicetype_rent),
        Type.EcommerceInvoice to context.getString(R.string.invoicetype_ecommerce),
        Type.PhoneInvoice to context.getString(R.string.invoicetype_phone),
        Type.LoanInvoice to context.getString(R.string.invoicetype_loan),
        Type.CreditCardInvoice to context.getString(R.string.invoicetype_creditcard),
        Type.CarInvoice to context.getString(R.string.invoicetype_car)
    )

    private val testUnit: Unit = testClass()

    // To use this class as stringable object
    override fun toString(): String {
        // Cannot ever be null, since string equivalent always mapped in the static mapping
        return STR_MAPPING[id]!!
    }

    fun getInvoiceTypeNames(): Collection<String> = STR_MAPPING.values

    fun getInvoiceTypeNameByCode(code: Int): String {
        val id = ID_MAPPING[code] // Will throw if code doesnt exist
        return STR_MAPPING[id]!! // Cannot be null => Num-Type mapping is 1:1
    }

    // A self-testing compoennt that is always run when this class is created
    // to ensure no duplicate entries is accidentally entered in the listing
    private fun testClass() {
        if (STR_MAPPING.size != Type.values().size) throw Exception("STR_MAPPING <-> Type count mismatch!")
        if (STR_MAPPING.size != ID_MAPPING.size) throw Exception("STR_MAPPING <-> ID_MAPPING count mismatch!")

        if (STR_MAPPING.keys.any {
                !Type.values().contains(it)
            }) throw Exception("STR_MAPPING value not found in Type!")
        if (STR_MAPPING.keys.any { !ID_MAPPING.containsValue(it) }) throw Exception("STR_MAPPING value not found in ID_MAPPING!")

        if (Type.values()
                .any { !STR_MAPPING.containsKey(it) }
        ) throw Exception("Type value not found in STR_MAPPING!")
        if (ID_MAPPING.values
                .any { !STR_MAPPING.containsKey(it) }
        ) throw Exception("ID_MAPPING value not found in STR_MAPPING!")

        if (Type.values().size != ID_MAPPING.size) throw Exception("Type <-> ID_MAPPING size mismatch!")
        if (Type.values()
                .any { !ID_MAPPING.containsValue(it) }
        ) throw Exception("Type value not found in ID_MAPPING!")
        if (ID_MAPPING.values.any {
                !Type.values().contains(it)
            }) throw Exception("ID_MAPPING value not found in Type!")
    }

    companion object {

        enum class Type {
            OtherInvoice,
            TravelInvoice,
            MaintenanceInvoice,
            ElectricityInvoice,
            CreditInvoice,
            CreditCardInvoice,
            WaterInvoice,
            RentInvoice,
            EcommerceInvoice,
            PhoneInvoice,
            LoanInvoice,
            CarInvoice,
        }

        // Existing entries should NOT be modified
        // DO NOT MODIFY EXISTING ENTRIES FOR BACKWARDS COMPABILITY
        private val ID_MAPPING: Map<Int, Type> = mapOf(
            0 to Type.OtherInvoice,
            1 to Type.TravelInvoice,
            2 to Type.MaintenanceInvoice,
            3 to Type.ElectricityInvoice,
            4 to Type.CreditInvoice,
            5 to Type.WaterInvoice,
            6 to Type.RentInvoice,
            7 to Type.EcommerceInvoice,
            8 to Type.PhoneInvoice,
            9 to Type.LoanInvoice,
            10 to Type.CreditCardInvoice,
            11 to Type.CarInvoice,
        )

        fun getInvoiceIdByTypeCode(id: Int): Type {
            return ID_MAPPING.entries.first { i -> i.key == id }.value
        }

        fun getInvoiceTypeCodeById(type: Type): Int {
            return ID_MAPPING.entries.first { i -> i.value == type }.key
        }

        fun getObjects(context: Context): Collection<InvoiceType> {
            return Type.values().map { InvoiceType(context, it) }
        }

    }
}
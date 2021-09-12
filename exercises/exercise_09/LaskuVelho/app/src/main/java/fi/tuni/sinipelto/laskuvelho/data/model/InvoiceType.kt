package fi.tuni.sinipelto.laskuvelho.data.model

// Static mapping of available invoice types
// Does not change after compilation (readonly), thus can be static
class InvoiceType(val id: Type) {

    // To use this class as stringable object
    override fun toString(): String {
        // Cannot ever be null, since string equivalent always mapped in the static mapping
        return STR_MAPPING[id]!!
    }

    companion object {

        enum class Type {
            Invoice,
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
        }

        // Existing entries should NOT be modified
        // DO NOT MODIFY EXISTING ENTRIES FOR BACKWARDS COMPABILITY
        private val ID_MAPPING: Map<Int, Type> = mapOf(
            0 to Type.Invoice,
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
        )

        private val STR_MAPPING: Map<Type, String> = mapOf(
            Type.Invoice to "Muu lasku",
            Type.TravelInvoice to "Matkalasku",
            Type.MaintenanceInvoice to "Huoltolasku",
            Type.ElectricityInvoice to "Sähkölasku",
            Type.CreditInvoice to "Luottolasku",
            Type.WaterInvoice to "Vesilasku",
            Type.RentInvoice to "Vuokralasku",
            Type.EcommerceInvoice to "Verkkokauppalasku",
            Type.PhoneInvoice to "Puhelinlasku",
            Type.LoanInvoice to "Lainalyhennys",
            Type.CreditCardInvoice to "Luottokorttilasku"
        )

        fun getObjects(): Collection<InvoiceType> {
            return Type.values().map { InvoiceType(it) }
        }

        fun getInvoiceTypeNames(): Collection<String> = STR_MAPPING.values

        fun getInvoiceTypeNameByCode(code: Int): String {
            val id = ID_MAPPING[code] // Will throw if code doesnt exist
            return STR_MAPPING[id]!! // Cannot be null => Num-Type mapping is 1:1
        }

        fun getInvoiceTypeCodeById(type: Type): Int {
            return ID_MAPPING.entries.first { i -> i.value == type }.key
        }
    }
}
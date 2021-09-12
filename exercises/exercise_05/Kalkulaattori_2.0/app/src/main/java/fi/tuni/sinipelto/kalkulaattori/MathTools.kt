package fi.tuni.sinipelto.kalkulaattori

import java.math.BigDecimal
import java.math.RoundingMode

class MathTools {
    companion object {
        fun sum(a: Double, b: Double): BigDecimal {
            return (a + b).toBigDecimal().setScale(4, RoundingMode.HALF_UP)
        }

        fun ret(a: Double, b: Double): BigDecimal {
            return (a - b).toBigDecimal().setScale(4, RoundingMode.HALF_UP)
        }

        fun multi(a: Double, b: Double): BigDecimal {
            return (a * b).toBigDecimal().setScale(4, RoundingMode.HALF_UP)
        }

        fun div(a: Double, b: Double): BigDecimal? {
            // Cannot divide by zero
            if (b == 0.0) return null
            return (a / b).toBigDecimal().setScale(4, RoundingMode.HALF_UP)
        }
    }
}
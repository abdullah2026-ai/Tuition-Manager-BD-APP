package com.example

import com.example.data.model.FeePayment
import com.example.data.model.MonthlyFeeDue
import com.example.data.model.Student
import com.example.ui.screens.StudentFeeCalculation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeeCalculationTest {

    private fun calculateStudentFee(
        student: Student,
        monthYear: String,
        payments: List<FeePayment>,
        dues: List<MonthlyFeeDue>
    ): StudentFeeCalculation {
        val monthDueOverride = dues.find { it.studentId == student.id && it.monthYear == monthYear }
        val expectedFee = monthDueOverride?.dueAmount ?: student.monthlyFee
        val studentPayments = payments.filter { it.studentId == student.id && it.monthYear == monthYear }
        val paid = studentPayments.sumOf { it.amount }
        val balance = (expectedFee - paid).coerceAtLeast(0.0)
        val advance = (paid - expectedFee).coerceAtLeast(0.0)

        val status = when {
            expectedFee <= 0.0 -> "PAID"
            paid >= expectedFee -> "PAID"
            paid > 0.0 -> "PARTIAL"
            else -> "DUE"
        }

        return StudentFeeCalculation(
            student = student,
            expectedFee = expectedFee,
            paid = paid,
            balance = balance,
            advance = advance,
            status = status
        )
    }

    @Test
    fun testFullyPaidStatus() {
        val student = Student(id = 1, name = "Tanvir", monthlyFee = 2000.0)
        val payments = listOf(
            FeePayment(id = 1, studentId = 1, monthYear = "2026-10", amount = 2000.0, paymentDate = "2026-10-05")
        )

        val calc = calculateStudentFee(student, "2026-10", payments, emptyList())
        assertEquals("PAID", calc.status)
        assertEquals(2000.0, calc.expectedFee, 0.001)
        assertEquals(2000.0, calc.paid, 0.001)
        assertEquals(0.0, calc.balance, 0.001)
        assertEquals(0.0, calc.advance, 0.001)
    }

    @Test
    fun testMultiplePartialPayments() {
        val student = Student(id = 1, name = "Tanvir", monthlyFee = 2000.0)
        val payments = listOf(
            FeePayment(id = 1, studentId = 1, monthYear = "2026-10", amount = 500.0, paymentDate = "2026-10-05"),
            FeePayment(id = 2, studentId = 1, monthYear = "2026-10", amount = 700.0, paymentDate = "2026-10-10")
        )

        val calc = calculateStudentFee(student, "2026-10", payments, emptyList())
        assertEquals("PARTIAL", calc.status)
        assertEquals(2000.0, calc.expectedFee, 0.001)
        assertEquals(1200.0, calc.paid, 0.001)
        assertEquals(800.0, calc.balance, 0.001)
        assertEquals(0.0, calc.advance, 0.001)
    }

    @Test
    fun testMultiplePaymentsCompletingFee() {
        val student = Student(id = 1, name = "Tanvir", monthlyFee = 2000.0)
        val payments = listOf(
            FeePayment(id = 1, studentId = 1, monthYear = "2026-10", amount = 1000.0, paymentDate = "2026-10-05"),
            FeePayment(id = 2, studentId = 1, monthYear = "2026-10", amount = 1000.0, paymentDate = "2026-10-15")
        )

        val calc = calculateStudentFee(student, "2026-10", payments, emptyList())
        assertEquals("PAID", calc.status)
        assertEquals(2000.0, calc.expectedFee, 0.001)
        assertEquals(2000.0, calc.paid, 0.001)
        assertEquals(0.0, calc.balance, 0.001)
    }

    @Test
    fun testOverpaymentHandling() {
        val student = Student(id = 1, name = "Tanvir", monthlyFee = 2000.0)
        val payments = listOf(
            FeePayment(id = 1, studentId = 1, monthYear = "2026-10", amount = 2500.0, paymentDate = "2026-10-05")
        )

        val calc = calculateStudentFee(student, "2026-10", payments, emptyList())
        assertEquals("PAID", calc.status)
        assertEquals(2000.0, calc.expectedFee, 0.001)
        assertEquals(2500.0, calc.paid, 0.001) // Full payment recorded and preserved!
        assertEquals(0.0, calc.balance, 0.001) // Balance is never negative
        assertEquals(500.0, calc.advance, 0.001) // Advance surplus correctly calculated
    }

    @Test
    fun testUnpaidDueStatus() {
        val student = Student(id = 1, name = "Tanvir", monthlyFee = 1500.0)
        val calc = calculateStudentFee(student, "2026-10", emptyList(), emptyList())

        assertEquals("DUE", calc.status)
        assertEquals(1500.0, calc.expectedFee, 0.001)
        assertEquals(0.0, calc.paid, 0.001)
        assertEquals(1500.0, calc.balance, 0.001)
        assertEquals(0.0, calc.advance, 0.001)
    }

    @Test
    fun testHistoricalMonthFeeOverride() {
        // Base student monthlyFee is 3000.0 in profile
        val student = Student(id = 1, name = "Tanvir", monthlyFee = 3000.0)
        // But for past month 2026-07, a MonthlyFeeDue override of 2200.0 was recorded
        val dues = listOf(
            MonthlyFeeDue(id = 1, studentId = 1, monthYear = "2026-07", dueAmount = 2200.0)
        )
        val payments = listOf(
            FeePayment(id = 1, studentId = 1, monthYear = "2026-07", amount = 2200.0, paymentDate = "2026-07-02")
        )

        val calc = calculateStudentFee(student, "2026-07", payments, dues)
        assertEquals("PAID", calc.status)
        assertEquals(2200.0, calc.expectedFee, 0.001)
        assertEquals(2200.0, calc.paid, 0.001)
        assertEquals(0.0, calc.balance, 0.001)
    }

    @Test
    fun testZeroFeeStudent() {
        val student = Student(id = 1, name = "Tanvir", monthlyFee = 0.0)
        val calc = calculateStudentFee(student, "2026-10", emptyList(), emptyList())
        assertEquals("PAID", calc.status)
        assertEquals(0.0, calc.balance, 0.001)
    }
}

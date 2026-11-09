import java.util.List;

/**
 * Self-contained unit and regression test suite for Akosua's Pharmacy.
 * Validates domain invariants, edge cases, pricing, and error boundaries without external dependencies.
 */
public class PharmacyTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("Starting Akosua's Pharmacy Domain Test Suite...");

        testNormalOtcDispense();
        testPrescriptionWithDoctor();
        testPrescriptionWithoutDoctorRejected();
        testRegisteredCustomerDiscount();
        testOverDispensePreventedAndStockProtected();
        testNegativeQuantityRejected();
        testRestockFunctionality();
        testUnmodifiableTransactionsLedger();
        testControlledSubstancesReadiness();

        System.out.println("\n--------------------------------------------------");
        System.out.println("Test Results: " + testsPassed + " / " + testsRun + " passed.");
        if (testsPassed == testsRun) {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY!");
        } else {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String testName) {
        testsRun++;
        if (expected == null && actual == null) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else if (expected != null && expected.equals(actual)) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName + " - Expected: " + expected + ", Got: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        assertEquals(true, condition, testName);
    }

    private static void testNormalOtcDispense() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        Customer c = new Customer("Kwame", CustomerType.WALK_IN);
        p.addMedication(m);
        p.addCustomer(c);

        p.dispense(c, m, 5, null);
        assertEquals(45, m.getStock(), "OTC Dispense reduces stock correctly");
        assertEquals(1, p.getTransactions().size(), "One transaction recorded");
        assertEquals(50.0, p.getTransactions().get(0).getTotal(), "Total cost is 50.0 for walk-in");
    }

    private static void testPrescriptionWithDoctor() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 20.0, 30, Category.PRESCRIPTION);
        Customer c = new Customer("Ama", CustomerType.WALK_IN);
        p.addMedication(m);

        p.dispense(c, m, 2, "Dr. Boateng");
        assertEquals(28, m.getStock(), "Prescription dispense reduces stock");
        assertEquals(1, p.getTransactions().size(), "Transaction recorded");
    }

    private static void testPrescriptionWithoutDoctorRejected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 20.0, 30, Category.PRESCRIPTION);
        Customer c = new Customer("Ama", CustomerType.WALK_IN);
        p.addMedication(m);

        p.dispense(c, m, 2, null);
        assertEquals(30, m.getStock(), "Stock remains unchanged when prescriber is null");
        assertEquals(0, p.getTransactions().size(), "No transaction recorded when prescriber is missing");
    }

    private static void testRegisteredCustomerDiscount() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Vitamins", 100.0, 20, Category.OTC);
        Customer c = new Customer("Esi", CustomerType.REGISTERED);
        p.addMedication(m);

        p.dispense(c, m, 1, null);
        assertEquals(95.0, p.getTransactions().get(0).getTotal(), "Registered customer receives 5% discount (95.0 for 100.0 item)");
    }

    private static void testOverDispensePreventedAndStockProtected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 30.0, 40, Category.PRESCRIPTION);
        Customer c = new Customer("Kofi", CustomerType.WALK_IN);
        p.addMedication(m);

        // Attempt to dispense 100 when only 40 are in stock
        p.dispense(c, m, 100, "Dr. Boateng");
        assertEquals(40, m.getStock(), "Stock must not become negative (-60); remains 40");
        assertEquals(0, p.getTransactions().size(), "No transaction recorded for failed over-dispense");
        assertEquals(0, c.getHistory().size(), "No history recorded on customer profile for failed dispense");
    }

    private static void testNegativeQuantityRejected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        Customer c = new Customer("Kofi", CustomerType.WALK_IN);

        p.dispense(c, m, -5, null);
        assertEquals(50, m.getStock(), "Negative quantity dispense rejected; stock unchanged");
    }

    private static void testRestockFunctionality() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        p.restock(m, 25);

        assertEquals(75, m.getStock(), "Restock increases stock accurately");
        assertEquals(1, p.getTransactions().size(), "Restock transaction recorded in ledger");
        assertEquals("RESTOCK", p.getTransactions().get(0).getType(), "Transaction type is RESTOCK");
    }

    private static void testUnmodifiableTransactionsLedger() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        Customer c = new Customer("Kofi", CustomerType.WALK_IN);
        p.dispense(c, m, 1, null);

        List<Transaction> txs = p.getTransactions();
        boolean exceptionThrown = false;
        try {
            txs.clear();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown, "External modification to getTransactions() throws UnsupportedOperationException");
    }

    private static void testControlledSubstancesReadiness() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Morphine", 50.0, 10, Category.CONTROLLED);
        Customer c = new Customer("Patient X", CustomerType.REGISTERED);

        // Prescriber mandatory for controlled substances
        p.dispense(c, m, 1, null);
        assertEquals(10, m.getStock(), "Controlled substance without prescriber rejected");

        p.dispense(c, m, 1, "Dr. Mensah");
        assertEquals(9, m.getStock(), "Controlled substance with prescriber dispensed safely");
    }
}

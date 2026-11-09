import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Regression and domain validation suite for Akosua's Pharmacy.
 * Silently validates all business rules and invariants, then presents the standard pharmacy execution.
 */
public class PharmacyTest {

    private static final PrintStream CONSOLE = System.out;
    private static final PrintStream SILENT = new PrintStream(OutputStream.nullOutputStream());

    public static void main(String[] args) {
        // Step 1: Run all 18 regression assertions silently
        runAllAssertionsSilently();

        // Step 2: Present the clean, professional pharmacy execution matching the original program
        PharmacyTracker.main(args);
    }

    private static void runAllAssertionsSilently() {
        System.setOut(SILENT);
        try {
            testNormalOtcDispense();
            testPrescriptionWithDoctor();
            testPrescriptionWithoutDoctorRejected();
            testRegisteredCustomerDiscount();
            testOverDispensePreventedAndStockProtected();
            testNegativeQuantityRejected();
            testRestockFunctionality();
            testUnmodifiableTransactionsLedger();
            testControlledSubstancesReadiness();
        } finally {
            System.setOut(CONSOLE);
        }
    }

    private static void assertTrue(boolean condition, String errorMessage) {
        if (!condition) {
            System.setOut(CONSOLE);
            System.err.println("Verification Failure: " + errorMessage);
            System.exit(1);
        }
    }

    private static void testNormalOtcDispense() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        Customer c = new Customer("Kwame", CustomerType.WALK_IN);
        p.addMedication(m);
        p.addCustomer(c);

        p.dispense(c, m, 5, null);
        assertTrue(m.getStock() == 45, "OTC dispense must decrement stock accurately");
        assertTrue(p.getTransactions().size() == 1, "OTC dispense must record transaction");
        assertTrue(p.getTransactions().get(0).getTotal() == 50.0, "Walk-in customer must be charged regular price");
    }

    private static void testPrescriptionWithDoctor() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 20.0, 30, Category.PRESCRIPTION);
        Customer c = new Customer("Ama", CustomerType.WALK_IN);
        p.addMedication(m);

        p.dispense(c, m, 2, "Dr. Boateng");
        assertTrue(m.getStock() == 28, "Prescription dispense must reduce stock when doctor is provided");
        assertTrue(p.getTransactions().size() == 1, "Prescription transaction must be logged");
    }

    private static void testPrescriptionWithoutDoctorRejected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 20.0, 30, Category.PRESCRIPTION);
        Customer c = new Customer("Ama", CustomerType.WALK_IN);
        p.addMedication(m);

        p.dispense(c, m, 2, null);
        assertTrue(m.getStock() == 30, "Missing prescriber must leave stock unchanged");
        assertTrue(p.getTransactions().isEmpty(), "Missing prescriber must log no transaction");
    }

    private static void testRegisteredCustomerDiscount() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Vitamins", 100.0, 20, Category.OTC);
        Customer c = new Customer("Esi", CustomerType.REGISTERED);
        p.addMedication(m);

        p.dispense(c, m, 1, null);
        assertTrue(p.getTransactions().get(0).getTotal() == 95.0, "Registered customer must receive 5% discount");
    }

    private static void testOverDispensePreventedAndStockProtected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 30.0, 40, Category.PRESCRIPTION);
        Customer c = new Customer("Kofi", CustomerType.WALK_IN);
        p.addMedication(m);

        // Attempting to dispense 100 when only 40 are available
        p.dispense(c, m, 100, "Dr. Boateng");
        assertTrue(m.getStock() == 40, "Over-dispense must be rejected; stock must remain 40");
        assertTrue(p.getTransactions().isEmpty(), "Over-dispense must record no transaction");
        assertTrue(c.getHistory().isEmpty(), "Over-dispense must record no customer history");
    }

    private static void testNegativeQuantityRejected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        Customer c = new Customer("Kofi", CustomerType.WALK_IN);

        p.dispense(c, m, -5, null);
        assertTrue(m.getStock() == 50, "Negative dispense quantity must be rejected");
    }

    private static void testRestockFunctionality() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        p.restock(m, 25);

        assertTrue(m.getStock() == 75, "Restocking must increment stock accurately");
        assertTrue(p.getTransactions().size() == 1, "Restocking must record supplier transaction");
        assertTrue("RESTOCK".equals(p.getTransactions().get(0).getType()), "Transaction type must be RESTOCK");
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
        assertTrue(exceptionThrown, "External tampering with getTransactions() must throw UnsupportedOperationException");
    }

    private static void testControlledSubstancesReadiness() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Morphine", 50.0, 10, Category.CONTROLLED);
        Customer c = new Customer("Patient X", CustomerType.REGISTERED);

        p.dispense(c, m, 1, null);
        assertTrue(m.getStock() == 10, "Controlled substance without prescriber must be rejected");

        p.dispense(c, m, 1, "Dr. Mensah");
        assertTrue(m.getStock() == 9, "Controlled substance with prescriber must be dispensed safely");
    }
}

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise regression and domain specification suite for Akosua's Pharmacy.
 * Validates domain rules, invariants, and edge cases in isolated, presentation-ready format.
 */
public class PharmacyTest {

    private static final PrintStream CONSOLE = System.out;
    private static final PrintStream SILENT = new PrintStream(OutputStream.nullOutputStream());

    private static final List<String> results = new ArrayList<>();
    private static int totalCount = 0;
    private static int successCount = 0;

    public static void main(String[] args) {
        executeTestSuite();
        displayPresentationResults();
    }

    private static void executeTestSuite() {
        // Execute all test cases with internal console noise silenced
        runSilently(PharmacyTest::testNormalOtcDispense);
        runSilently(PharmacyTest::testPrescriptionWithDoctor);
        runSilently(PharmacyTest::testPrescriptionWithoutDoctorRejected);
        runSilently(PharmacyTest::testRegisteredCustomerDiscount);
        runSilently(PharmacyTest::testOverDispensePreventedAndStockProtected);
        runSilently(PharmacyTest::testNegativeQuantityRejected);
        runSilently(PharmacyTest::testRestockFunctionality);
        runSilently(PharmacyTest::testUnmodifiableTransactionsLedger);
        runSilently(PharmacyTest::testControlledSubstancesReadiness);
    }

    private static void runSilently(Runnable testTask) {
        System.setOut(SILENT);
        try {
            testTask.run();
        } finally {
            System.setOut(CONSOLE);
        }
    }

    private static void verify(boolean condition, String description) {
        totalCount++;
        if (condition) {
            successCount++;
            results.add(String.format("  %-72s  [OK]", description));
        } else {
            results.add(String.format("  %-72s  [FAILED]", description));
        }
    }

    private static void displayPresentationResults() {
        CONSOLE.println();
        CONSOLE.println("========================================================================================");
        CONSOLE.println("                 AKOSUA'S PHARMACY - SYSTEM VERIFICATION REPORT                         ");
        CONSOLE.println("========================================================================================");

        for (int i = 0; i < results.size(); i++) {
            CONSOLE.printf("  %02d. %s%n", (i + 1), results.get(i).trim());
        }

        CONSOLE.println("========================================================================================");
        CONSOLE.printf("  Summary: %d of %d specifications verified successfully.%n", successCount, totalCount);
        CONSOLE.println("========================================================================================");
        CONSOLE.println();
    }

    private static void testNormalOtcDispense() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        Customer c = new Customer("Kwame", CustomerType.WALK_IN);
        p.addMedication(m);
        p.addCustomer(c);

        p.dispense(c, m, 5, null);
        verify(m.getStock() == 45, "OTC dispense decrements inventory stock accurately");
        verify(p.getTransactions().size() == 1, "OTC dispense records audit transaction in ledger");
        verify(p.getTransactions().get(0).getTotal() == 50.0, "Walk-in customer billed at standard retail price");
    }

    private static void testPrescriptionWithDoctor() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 20.0, 30, Category.PRESCRIPTION);
        Customer c = new Customer("Ama", CustomerType.WALK_IN);
        p.addMedication(m);

        p.dispense(c, m, 2, "Dr. Boateng");
        verify(m.getStock() == 28, "Prescription dispense reduces stock when prescriber is provided");
        verify(p.getTransactions().size() == 1, "Prescription transaction committed to ledger");
    }

    private static void testPrescriptionWithoutDoctorRejected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 20.0, 30, Category.PRESCRIPTION);
        Customer c = new Customer("Ama", CustomerType.WALK_IN);
        p.addMedication(m);

        p.dispense(c, m, 2, null);
        verify(m.getStock() == 30, "Missing prescriber halts transaction and preserves stock");
        verify(p.getTransactions().isEmpty(), "Missing prescriber prevents transaction creation");
    }

    private static void testRegisteredCustomerDiscount() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Vitamins", 100.0, 20, Category.OTC);
        Customer c = new Customer("Esi", CustomerType.REGISTERED);
        p.addMedication(m);

        p.dispense(c, m, 1, null);
        verify(p.getTransactions().get(0).getTotal() == 95.0, "Registered customer receives 5% loyalty discount");
    }

    private static void testOverDispensePreventedAndStockProtected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Amoxicillin", 30.0, 40, Category.PRESCRIPTION);
        Customer c = new Customer("Kofi", CustomerType.WALK_IN);
        p.addMedication(m);

        // Attempting to dispense 100 when only 40 are available
        p.dispense(c, m, 100, "Dr. Boateng");
        verify(m.getStock() == 40, "Over-dispense rejected: stock remains positive (never negative)");
        verify(p.getTransactions().isEmpty(), "Failed over-dispense creates no phantom transaction");
        verify(c.getHistory().isEmpty(), "Failed over-dispense creates no customer history record");
    }

    private static void testNegativeQuantityRejected() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        Customer c = new Customer("Kofi", CustomerType.WALK_IN);

        p.dispense(c, m, -5, null);
        verify(m.getStock() == 50, "Non-positive dispense quantity rejected safely");
    }

    private static void testRestockFunctionality() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Paracetamol", 10.0, 50, Category.OTC);
        p.restock(m, 25);

        verify(m.getStock() == 75, "Restocking increments inventory count accurately");
        verify(p.getTransactions().size() == 1, "Restocking records supplier audit entry in ledger");
        verify("RESTOCK".equals(p.getTransactions().get(0).getType()), "Restock transaction type recorded as RESTOCK");
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
        verify(exceptionThrown, "Audit ledger is tamper-proof (throws on external modification)");
    }

    private static void testControlledSubstancesReadiness() {
        Pharmacy p = new Pharmacy("Test Pharmacy");
        Medication m = new Medication("Morphine", 50.0, 10, Category.CONTROLLED);
        Customer c = new Customer("Patient X", CustomerType.REGISTERED);

        p.dispense(c, m, 1, null);
        verify(m.getStock() == 10, "Controlled substance without prescriber rejected");

        p.dispense(c, m, 1, "Dr. Mensah");
        verify(m.getStock() == 9, "Controlled substance with prescriber dispensed safely");
    }
}

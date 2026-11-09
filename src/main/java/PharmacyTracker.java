/**
 * Application entry point for Akosua's Pharmacy stock and dispensing tracker.
 *
 * <p>This class acts as the <em>presentation and bootstrap layer</em> only.
 * All domain logic — stock invariants, prescriber validation, discount calculation,
 * and audit-trail management — lives in the domain model classes
 * ({@link Medication}, {@link Customer}, {@link Pharmacy}).
 * {@code PharmacyTracker} simply wires up the domain objects and delegates.</p>
 *
 * <p>The scenario encoded in {@link #main(String[])} mirrors the original
 * single-file starter program exactly, including the over-dispense attempt that
 * exposed the negative-stock bug. The rebuilt architecture safely rejects it.</p>
 *
 * <p><strong>To run:</strong></p>
 * <pre>
 *   javac -d target/classes src/main/java/*.java
 *   java  -cp target/classes PharmacyTracker
 * </pre>
 */
public class PharmacyTracker {

    /**
     * Bootstraps and runs the daily pharmacy simulation.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Pharmacy p = new Pharmacy("Akosua's Pharmacy");

        Medication paracetamol = new Medication("Paracetamol 500mg", 12.50, 100, "OTC");
        Medication amoxicillin = new Medication("Amoxicillin 250mg", 30.00, 40, "PRESCRIPTION");
        p.addMedication(paracetamol);
        p.addMedication(amoxicillin);

        Customer kofi = new Customer("Kofi Mensah", "WALK_IN");
        Customer ama  = new Customer("Ama Owusu",   "REGISTERED");
        ama.setPhone("0244 000 000"); // registered patients have contact details on file
        p.addCustomer(kofi);
        p.addCustomer(ama);

        // a normal morning
        p.dispense(kofi, paracetamol, 2,  null);           // OTC — no prescriber needed
        p.dispense(ama,  amoxicillin,  1, "Dr. Boateng");  // prescription-only; Ama gets 5% discount
        p.restock(paracetamol, 50);                        // supplier delivery

        // ... and the sort of thing that keeps Akosua up at night:
        p.dispense(kofi, amoxicillin, 100, "Dr. Boateng"); // far more than we have — safely rejected

        p.printReport();
    }
}

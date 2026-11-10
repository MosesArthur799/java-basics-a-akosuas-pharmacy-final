import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain service and aggregate root representing Akosua's Pharmacy.
 *
 * <p>This class orchestrates all core business workflows: medication dispensing,
 * inventory restocking, prescriber verification, loyalty discount calculation,
 * and audit-trail management.</p>
 *
 * <p><strong>Invariant guarantees upheld by this class:</strong></p>
 * <ul>
 *   <li>Stock can never drop below zero — rejected dispenses leave all state unchanged.</li>
 *   <li>Prescription and controlled medications require a named prescriber.</li>
 *   <li>Registered patients receive a 5% loyalty discount automatically.</li>
 *   <li>The transaction ledger is exposed only as an unmodifiable view, preventing
 *       external tampering with the pharmacy's financial and legal records.</li>
 * </ul>
 */
public class Pharmacy {

    private final String name;
    private final List<Medication>   meds         = new ArrayList<>();
    private final List<Customer>     customers    = new ArrayList<>();
    private final List<Transaction>  transactions = new ArrayList<>();

    /**
     * Creates a new pharmacy with the given trading name.
     *
     * @param name the pharmacy's display name, used in reports and receipts
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public Pharmacy(String name) {
        this.name = Objects.requireNonNull(name, "Pharmacy name cannot be null");
    }

    /**
     * Registers a medication in the pharmacy's inventory catalog.
     *
     * @param m the medication to add
     * @throws NullPointerException if {@code m} is {@code null}
     */
    public void addMedication(Medication m) {
        meds.add(Objects.requireNonNull(m, "Medication cannot be null"));
    }

    /**
     * Registers a customer in the pharmacy's client records.
     *
     * @param c the customer to add
     * @throws NullPointerException if {@code c} is {@code null}
     */
    public void addCustomer(Customer c) {
        customers.add(Objects.requireNonNull(c, "Customer cannot be null"));
    }

    /**
     * Dispenses a medication to a customer, enforcing all business rules atomically.
     *
     * <p>Processing order:</p>
     * <ol>
     *   <li>Validates that neither argument is {@code null} and quantity is positive.</li>
     *   <li>Checks that a prescriber is named for prescription or controlled items.</li>
     *   <li>Delegates stock reduction to {@link Medication#reduceStock(int)}, which
     *       enforces the non-negativity invariant and throws
     *       {@link InsufficientStockException} if the request cannot be fulfilled.</li>
     *   <li>On success: applies loyalty discount, commits audit entry, updates
     *       customer history, and prints a formatted receipt.</li>
     * </ol>
     *
     * <p>If any check fails, all state (stock, history, ledger) is left completely
     * unchanged — there is no partial update.</p>
     *
     * @param c          the customer receiving the medication
     * @param m          the medication being dispensed
     * @param qty        the number of units requested (must be &gt; 0)
     * @param prescriber the prescribing physician's name; required for
     *                   {@link Category#PRESCRIPTION} and {@link Category#CONTROLLED} items
     * @throws NullPointerException     if {@code c} or {@code m} is {@code null}
     */
    public void dispense(Customer c, Medication m, int qty, String prescriber) {
        Objects.requireNonNull(c, "Customer cannot be null");
        Objects.requireNonNull(m, "Medication cannot be null");

        // Guard: quantity must be a positive integer
        if (qty <= 0) {
            System.out.println("Cannot dispense " + qty + " x " + m.getName()
                    + ": quantity must be positive");
            return;
        }

        // Guard: prescription and controlled substances require an authorized prescriber
        if (m.isPrescriptionRequired() && (prescriber == null || prescriber.isBlank())) {
            System.out.println("Cannot dispense " + qty + " x " + m.getName()
                    + ": prescriber required");
            return;
        }

        // Atomically reduce stock — throws InsufficientStockException if qty > current stock,
        // which unwinds before any money is charged or ledger entry written.
        try {
            m.reduceStock(qty);
        } catch (InsufficientStockException e) {
            System.out.println(e.getMessage());
            return;
        }

        // Apply loyalty discount: registered patients receive 5% off all purchases
        double total = m.getPrice() * qty * (1.0 - c.getDiscountRate());

        // Commit audit trail and customer history (only reached on a fully valid transaction)
        c.addHistory("Got " + qty + " x " + m.getName());
        transactions.add(new Transaction(c.getName(), m.getName(), qty, total, TransactionType.DISPENSE));

        // Print a formatted receipt matching the original program's output
        if (m.isPrescriptionRequired()) {
            System.out.println("Dispensed " + qty + " x " + m.getName()
                    + " to " + c.getName()
                    + " (prescribed by " + prescriber + ")"
                    + " for GHS " + total);
        } else {
            System.out.println("Dispensed " + qty + " x " + m.getName()
                    + " to " + c.getName()
                    + " for GHS " + total);
        }
    }

    /**
     * Restocks a medication and records a supplier transaction in the audit ledger.
     *
     * @param m   the medication being restocked
     * @param qty the number of units received from the supplier (must be &gt; 0)
     * @throws NullPointerException if {@code m} is {@code null}
     */
    public void restock(Medication m, int qty) {
        Objects.requireNonNull(m, "Medication cannot be null");

        if (qty <= 0) {
            System.out.println("Cannot restock " + qty + " x " + m.getName()
                    + ": quantity must be positive");
            return;
        }

        m.increaseStock(qty);
        transactions.add(new Transaction("(supplier)", m.getName(), qty, 0.0, TransactionType.RESTOCK));
        System.out.println("Restocked " + qty + " x " + m.getName());
    }

    /**
     * Returns an unmodifiable view of the daily transaction audit log.
     *
     * <p>The returned list is a read-only window onto the internal ledger.
     * Any attempt to mutate it (via {@code add}, {@code remove}, {@code clear}, etc.)
     * will throw {@link java.lang.UnsupportedOperationException} at runtime,
     * protecting the pharmacy's financial and legal records from accidental or
     * malicious tampering.</p>
     *
     * @return an unmodifiable view of all transactions recorded today
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Returns an unmodifiable view of the medication inventory catalog.
     *
     * @return an unmodifiable list of all registered medications
     */
    public List<Medication> getMeds() {
        return Collections.unmodifiableList(meds);
    }

    /**
     * Returns an unmodifiable view of the registered customer list.
     *
     * @return an unmodifiable list of all registered customers
     */
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    /** @return the pharmacy's trading name */
    public String getName() {
        return name;
    }

    /**
     * Prints the end-of-day summary report to standard output.
     *
     * <p>Output format matches the original program exactly, listing current
     * stock levels for every medication and the total number of transactions
     * recorded during the session.</p>
     */
    public void printReport() {
        System.out.println();
        System.out.println("=== " + name + " - end of day ===");
        System.out.println("Stock levels:");
        for (Medication m : meds) {
            System.out.println("  " + m.getName() + " (" + m.getCategory() + "): " + m.getStock() + " in stock");
        }
        System.out.println("Transactions today: " + transactions.size());
    }
}

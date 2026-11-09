import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain service and aggregate root representing Akosua's Pharmacy.
 * Orchestrates business workflows, pricing, dispensing validation, and ledger auditing.
 */
public class Pharmacy {
    private final String name;
    private final List<Medication> meds = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public Pharmacy(String name) {
        this.name = Objects.requireNonNull(name, "Pharmacy name cannot be null");
    }

    public void addMedication(Medication m) {
        if (m != null) {
            meds.add(m);
        }
    }

    public void addCustomer(Customer c) {
        if (c != null) {
            customers.add(c);
        }
    }

    /**
     * Dispenses medicine to a customer, enforcing prescription rules, loyalty discounts,
     * and the non-negotiable invariant that stock cannot drop below zero.
     *
     * @param c The customer receiving the medication
     * @param m The medication being dispensed
     * @param qty The quantity to dispense
     * @param prescriber The name of the prescribing physician (mandatory for prescription items)
     */
    public void dispense(Customer c, Medication m, int qty, String prescriber) {
        if (c == null || m == null) {
            throw new IllegalArgumentException("Customer and Medication cannot be null");
        }
        if (qty <= 0) {
            System.out.println("Cannot dispense non-positive quantity: " + qty);
            return;
        }

        // Rule 1: Validate prescriber mandate for prescription / controlled items
        if (m.isPrescriptionRequired()) {
            if (prescriber == null || prescriber.isBlank()) {
                System.out.println("Cannot dispense " + m.getName() + " without a prescriber");
                return;
            }
        }

        // Rule 2: Enforce the core business invariant (Stock can never drift below zero)
        if (qty > m.getStock()) {
            System.out.println("Cannot dispense " + qty + " x " + m.getName() + ": only " + m.getStock() + " in stock");
            return;
        }

        // Atomically mutate stock in the domain model
        try {
            m.reduceStock(qty);
        } catch (InsufficientStockException e) {
            System.out.println(e.getMessage());
            return;
        }

        // Rule 3: Calculate pricing and loyalty discount (5% for registered patients)
        double total = m.getPrice() * qty;
        if (c.isRegistered()) {
            total = total * (1.0 - c.getDiscountRate());
        }

        // Commit audit trails
        c.addHistory("Got " + qty + " x " + m.getName());
        transactions.add(new Transaction(c.getName(), m.getName(), qty, total, "DISPENSE"));

        // User-facing presentation
        if (m.isPrescriptionRequired()) {
            System.out.println("Dispensed " + qty + " x " + m.getName() + " to " + c.getName()
                    + " (prescribed by " + prescriber + ") for GHS " + total);
        } else {
            System.out.println("Dispensed " + qty + " x " + m.getName() + " to " + c.getName()
                    + " for GHS " + total);
        }
    }

    /**
     * Restocks a medication and records a restock transaction in the audit ledger.
     *
     * @param m The medication being restocked
     * @param qty The quantity being added to inventory
     */
    public void restock(Medication m, int qty) {
        if (m == null) {
            throw new IllegalArgumentException("Medication cannot be null");
        }
        if (qty <= 0) {
            System.out.println("Cannot restock non-positive quantity: " + qty);
            return;
        }

        m.increaseStock(qty);
        transactions.add(new Transaction("(supplier)", m.getName(), qty, 0, "RESTOCK"));
        System.out.println("Restocked " + qty + " x " + m.getName());
    }

    /**
     * Returns an unmodifiable view of the daily transaction audit log.
     * Prevents external code from tampering with or clearing the financial ledger.
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Medication> getMeds() {
        return Collections.unmodifiableList(meds);
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public String getName() {
        return name;
    }

    /**
     * Prints the end-of-day summary report.
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

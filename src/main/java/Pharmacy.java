import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Coordinates pharmacy inventory and dispensing. */
public class Pharmacy {

    private final String name;
    private final List<Medication> meds = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    public Pharmacy(String name) {
        this.name = Objects.requireNonNull(name, "Pharmacy name cannot be null");
    }

    public void addMedication(Medication m) {
        meds.add(Objects.requireNonNull(m, "Medication cannot be null"));
    }

    public void addCustomer(Customer c) {
        customers.add(Objects.requireNonNull(c, "Customer cannot be null"));
    }

    public void dispense(Customer c, Medication m, int qty, String prescriber) {
        Objects.requireNonNull(c, "Customer cannot be null");
        Objects.requireNonNull(m, "Medication cannot be null");

        if (qty <= 0) {
            System.out.println("Cannot dispense " + qty + " x " + m.getName()
                    + ": quantity must be positive");
            return;
        }

        if (m.isPrescriptionRequired() && (prescriber == null || prescriber.isBlank())) {
            System.out.println("Cannot dispense " + qty + " x " + m.getName()
                    + ": prescriber required");
            return;
        }

        try {
            m.reduceStock(qty);
        } catch (InsufficientStockException e) {
            // Handle an expected stock rejection here; unrelated exceptions still surface.
            System.out.println(e.getMessage());
            return;
        }

        double total = m.getPrice() * qty * (1.0 - c.getDiscountRate());

        // Only a successful dispense is added to the customer's history and ledger.
        c.addHistory("Got " + qty + " x " + m.getName());
        transactions.add(new Transaction(c.getName(), m.getName(), qty, total, TransactionType.DISPENSE));

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

    // Let callers inspect the ledger without changing it.
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

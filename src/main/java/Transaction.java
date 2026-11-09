import java.util.Objects;

/**
 * Immutable domain event value object representing a historical transaction in Akosua's Pharmacy.
 * Preserves an unalterable point-in-time audit record of dispensing or restocking events.
 */
public final class Transaction {
    private final int id;
    private final String customerName;
    private final String medName;
    private final int qty;
    private final double total;
    private final String type;

    private static int counter = 1;

    public Transaction(String customerName, String medName, int qty, double total, String type) {
        this.id = counter++;
        this.customerName = Objects.requireNonNull(customerName, "Customer name cannot be null");
        this.medName = Objects.requireNonNull(medName, "Medication name cannot be null");
        this.qty = qty;
        this.total = total;
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMedName() {
        return medName;
    }

    public int getQty() {
        return qty;
    }

    public double getTotal() {
        return total;
    }

    public String getType() {
        return type;
    }
}

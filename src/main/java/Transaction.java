import java.util.Objects;

/**
 * Immutable domain event value object representing a completed transaction in Akosua's Pharmacy.
 *
 * <p>Once constructed, a {@code Transaction} is structurally frozen — all fields are
 * {@code final} and no mutators exist. This guarantees that the financial and audit
 * ledger can never be silently altered after the fact, satisfying the pharmacy's
 * regulatory obligation to maintain tamper-proof dispensing records.</p>
 *
 * <p>Transaction type is encoded as a {@link TransactionType} enum constant rather than
 * a raw string, eliminating the possibility of typo-driven silent failures in reporting
 * or audit logic.</p>
 */
public final class Transaction {

    private final int id;
    private final String customerName;
    private final String medName;
    private final int qty;
    private final double total;
    private final TransactionType type;

    private static int counter = 1;

    /**
     * Creates a new immutable transaction record.
     *
     * @param customerName name of the customer or {@code "(supplier)"} for restocks
     * @param medName      name of the medication involved
     * @param qty          number of units dispensed or restocked
     * @param total        financial total in GHS (zero for restock events)
     * @param type         {@link TransactionType#DISPENSE} or {@link TransactionType#RESTOCK}
     * @throws NullPointerException if any string or type argument is {@code null}
     */
    public Transaction(String customerName, String medName, int qty, double total, TransactionType type) {
        this.id           = counter++;
        this.customerName = Objects.requireNonNull(customerName, "Customer name cannot be null");
        this.medName      = Objects.requireNonNull(medName,      "Medication name cannot be null");
        this.qty          = qty;
        this.total        = total;
        this.type         = Objects.requireNonNull(type,         "Transaction type cannot be null");
    }

    /** @return auto-generated sequence identifier for this transaction */
    public int getId() {
        return id;
    }

    /** @return name of the customer who received the medication, or {@code "(supplier)"} for restocks */
    public String getCustomerName() {
        return customerName;
    }

    /** @return name of the medication involved in this transaction */
    public String getMedName() {
        return medName;
    }

    /** @return number of units dispensed or restocked */
    public int getQty() {
        return qty;
    }

    /** @return financial total charged in GHS; always {@code 0.0} for restock transactions */
    public double getTotal() {
        return total;
    }

    /** @return the type of this transaction — {@link TransactionType#DISPENSE} or {@link TransactionType#RESTOCK} */
    public TransactionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, type=%s, med='%s', qty=%d, total=%.2f, customer='%s'}",
                id, type, medName, qty, total, customerName);
    }
}

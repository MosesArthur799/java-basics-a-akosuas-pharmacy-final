import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a customer of Akosua's Pharmacy.
 *
 * <p>Encapsulates client identity, membership tier ({@link CustomerType#WALK_IN} or
 * {@link CustomerType#REGISTERED}), an optional verified contact number, and a
 * read-only dispense history that the pharmacy keeps for registered patients.</p>
 *
 * <p>Dispense history is exposed only as an unmodifiable list — external code
 * cannot retroactively alter a patient's medication record.</p>
 */
public class Customer {

    private final int          id;
    private final String       name;
    private final CustomerType kind;
    private       String       phone;
    private final List<String> history = new ArrayList<>();

    private static int counter = 1;

    /**
     * Creates a new customer with a name and membership tier.
     *
     * @param name the customer's full name
     * @param kind membership tier — {@link CustomerType#WALK_IN} or
     *             {@link CustomerType#REGISTERED}
     * @throws NullPointerException if {@code name} or {@code kind} is {@code null}
     */
    public Customer(String name, CustomerType kind) {
        this.id   = counter++;
        this.name = Objects.requireNonNull(name, "Customer name cannot be null");
        this.kind = Objects.requireNonNull(kind, "Customer kind cannot be null");
    }

    /**
     * Convenience constructor that accepts the customer type as a plain string.
     * Delegates to {@link CustomerType#fromString(String)} for case-insensitive parsing.
     *
     * @param name    the customer's full name
     * @param kindStr type string — e.g. {@code "WALK_IN"} or {@code "REGISTERED"}
     * @throws IllegalArgumentException if the string does not match a known type
     */
    public Customer(String name, String kindStr) {
        this(name, CustomerType.fromString(kindStr));
    }

    /**
     * Records a contact number on file for this customer.
     * Only meaningful for {@link CustomerType#REGISTERED} patients.
     *
     * @param phone the contact number to store; may be {@code null} to clear it
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** @return the contact number on file, or {@code null} if none has been set */
    public String getPhone() {
        return phone;
    }

    /**
     * Appends an entry to the customer's dispense history.
     * Blank or {@code null} entries are silently ignored.
     *
     * @param entry a human-readable record of what was dispensed
     */
    public void addHistory(String entry) {
        if (entry != null && !entry.isBlank()) {
            this.history.add(entry);
        }
    }

    /**
     * Returns the customer's dispense history as an unmodifiable list.
     *
     * <p>External code cannot remove or alter history entries — the record
     * is append-only from the pharmacy's perspective.</p>
     *
     * @return an unmodifiable view of the dispense history
     */
    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** @return the auto-generated sequence identifier for this customer */
    public int getId() {
        return id;
    }

    /** @return the customer's full name */
    public String getName() {
        return name;
    }

    /**
     * Returns the membership tier label string — e.g. {@code "WALK_IN"}.
     * Used for display purposes.
     *
     * @return string label of the customer's membership tier
     * @see #getKindEnum() for the type-safe enum value
     */
    public String getKind() {
        return kind.getLabel();
    }

    /**
     * Returns the type-safe {@link CustomerType} enum constant for this customer.
     *
     * @return the membership tier enum
     */
    public CustomerType getKindEnum() {
        return kind;
    }

    /**
     * @return {@code true} if this customer holds a registered membership
     *         and is entitled to loyalty discounts
     */
    public boolean isRegistered() {
        return kind == CustomerType.REGISTERED;
    }

    /**
     * @return the fractional discount rate applicable to this customer's purchases;
     *         {@code 0.0} for walk-in customers, {@code 0.05} (5%) for registered patients
     */
    public double getDiscountRate() {
        return kind.getDiscountRate();
    }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s', kind=%s}", id, name, kind);
    }
}

import java.util.Objects;

/**
 * Domain entity representing a pharmaceutical product held in Akosua's inventory.
 *
 * <p>All stock mutations are routed through {@link #reduceStock(int)} and
 * {@link #increaseStock(int)}, both of which validate their arguments before
 * touching state. This design ensures the core business invariant —
 * <em>stock can never be negative</em> — is enforced at the entity boundary
 * regardless of which caller initiates the mutation.</p>
 *
 * <p>The {@link #reduceStock(int)} method is marked {@code synchronized} so
 * concurrent callers cannot race to push stock below zero.</p>
 */
public class Medication {

    private final int      id;
    private final String   name;
    private final double   price;
    private       int      stock;
    private final Category category;

    private static int counter = 1;

    /**
     * Creates a new medication with the given attributes.
     *
     * @param name     display name shown on receipts and reports
     * @param price    unit price in GHS; must be &ge; 0
     * @param stock    initial stock count; must be &ge; 0
     * @param category regulatory classification — {@link Category#OTC},
     *                 {@link Category#PRESCRIPTION}, or {@link Category#CONTROLLED}
     * @throws NullPointerException     if {@code name} or {@code category} is {@code null}
     * @throws IllegalArgumentException if {@code price} or {@code stock} is negative
     */
    public Medication(String name, double price, int stock, Category category) {
        if (price < 0.0) {
            throw new IllegalArgumentException("Price cannot be negative: " + price);
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative: " + stock);
        }
        this.id       = counter++;
        this.name     = Objects.requireNonNull(name,     "Medication name cannot be null");
        this.price    = price;
        this.stock    = stock;
        this.category = Objects.requireNonNull(category, "Category cannot be null");
    }

    /**
     * Convenience constructor that accepts the category as a plain string.
     * Delegates to {@link Category#fromString(String)} for case-insensitive parsing.
     *
     * @param name        display name
     * @param price       unit price in GHS
     * @param stock       initial stock count
     * @param categoryStr category string — e.g. {@code "OTC"}, {@code "PRESCRIPTION"}
     * @throws IllegalArgumentException if the string does not match a known category
     */
    public Medication(String name, double price, int stock, String categoryStr) {
        this(name, price, stock, Category.fromString(categoryStr));
    }

    /**
     * Atomically decrements inventory by the requested quantity.
     *
     * <p>This is the single controlled gateway through which stock reductions
     * must flow. The check {@code qty <= stock} is performed here, inside the
     * entity, so the invariant holds even if alternative dispense paths are
     * added in future.</p>
     *
     * @param qty number of units to dispense; must be &gt; 0
     * @throws IllegalArgumentException  if {@code qty} is zero or negative
     * @throws InsufficientStockException if {@code qty} exceeds current stock
     */
    public synchronized void reduceStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Dispense quantity must be positive: " + qty);
        }
        if (qty > this.stock) {
            throw new InsufficientStockException(this.name, qty, this.stock);
        }
        this.stock -= qty;
    }

    /**
     * Atomically increments inventory by the given quantity.
     *
     * @param qty number of units received from supplier; must be &gt; 0
     * @throws IllegalArgumentException if {@code qty} is zero or negative
     */
    public synchronized void increaseStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive: " + qty);
        }
        this.stock += qty;
    }

    /** @return the auto-generated sequence identifier for this medication */
    public int getId() {
        return id;
    }

    /** @return the display name used on receipts and stock reports */
    public String getName() {
        return name;
    }

    /** @return the unit price in GHS */
    public double getPrice() {
        return price;
    }

    /** @return the current stock count; always &ge; 0 */
    public synchronized int getStock() {
        return stock;
    }

    /**
     * Returns the category label string — e.g. {@code "OTC"}, {@code "PRESCRIPTION"}.
     * Used for display in reports and receipts.
     *
     * @return string representation of the regulatory category
     * @see #getCategoryEnum() for the type-safe enum value
     */
    public String getCategory() {
        return category.getLabel();
    }

    /**
     * Returns the type-safe {@link Category} enum constant for this medication.
     *
     * @return the regulatory category enum
     */
    public Category getCategoryEnum() {
        return category;
    }

    /**
     * Convenience method: returns {@code true} if this medication requires a
     * named prescriber before it can be dispensed.
     *
     * @return {@code true} for {@link Category#PRESCRIPTION} and
     *         {@link Category#CONTROLLED}; {@code false} for {@link Category#OTC}
     */
    public boolean isPrescriptionRequired() {
        return category.isPrescriberRequired();
    }

    @Override
    public String toString() {
        return String.format("Medication{id=%d, name='%s', category=%s, price=%.2f, stock=%d}",
                id, name, category, price, stock);
    }
}

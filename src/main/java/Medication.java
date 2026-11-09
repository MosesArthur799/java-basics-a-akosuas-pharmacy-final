import java.util.Objects;

/**
 * Domain entity representing pharmaceutical inventory in Akosua's Pharmacy.
 * Enforces atomic stock mutations and preserves the non-negative inventory invariant.
 */
public class Medication {
    private final int id;
    private final String name;
    private final double price;
    private int stock;
    private final Category category;

    private static int counter = 1;

    public Medication(String name, double price, int stock, Category category) {
        if (price < 0.0) {
            throw new IllegalArgumentException("Price cannot be negative: " + price);
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative: " + stock);
        }
        this.id = counter++;
        this.name = Objects.requireNonNull(name, "Medication name cannot be null");
        this.price = price;
        this.stock = stock;
        this.category = Objects.requireNonNull(category, "Category cannot be null");
    }

    public Medication(String name, double price, int stock, String categoryStr) {
        this(name, price, stock, Category.fromString(categoryStr));
    }

    /**
     * Atomically decrements inventory by the specified quantity.
     * Enforces the critical invariant that stock can never drop below zero.
     *
     * @param qty Number of units to dispense
     * @throws IllegalArgumentException if quantity is non-positive
     * @throws InsufficientStockException if quantity exceeds available stock
     */
    public synchronized void reduceStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Dispense quantity must be greater than zero: " + qty);
        }
        if (qty > this.stock) {
            throw new InsufficientStockException(this.name, qty, this.stock);
        }
        this.stock -= qty;
    }

    /**
     * Atomically increments inventory by the specified quantity.
     *
     * @param qty Number of units to add to stock
     * @throws IllegalArgumentException if quantity is non-positive
     */
    public synchronized void increaseStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than zero: " + qty);
        }
        this.stock += qty;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public synchronized int getStock() {
        return stock;
    }

    public String getCategory() {
        return category.getLabel();
    }

    public Category getCategoryEnum() {
        return category;
    }

    public boolean isPrescriptionRequired() {
        return category.isPrescriberRequired();
    }
}

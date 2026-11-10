import java.util.Objects;

/** A medication and its current inventory. */
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

    // Checking and subtracting under the same lock prevents concurrent overselling.
    public synchronized void reduceStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Dispense quantity must be positive: " + qty);
        }
        if (qty > this.stock) {
            throw new InsufficientStockException(this.name, qty, this.stock);
        }
        this.stock -= qty;
    }

    public synchronized void increaseStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive: " + qty);
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

    @Override
    public String toString() {
        return String.format("Medication{id=%d, name='%s', category=%s, price=%.2f, stock=%d}",
                id, name, category, price, stock);
    }
}

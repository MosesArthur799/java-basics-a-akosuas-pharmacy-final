import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A pharmacy customer and their dispense history. */
public class Customer {

    private final int id;
    private final String name;
    private final CustomerType kind;
    private String phone;
    private final List<String> history = new ArrayList<>();

    private static int counter = 1;

    public Customer(String name, CustomerType kind) {
        this.id = counter++;
        this.name = Objects.requireNonNull(name, "Customer name cannot be null");
        this.kind = Objects.requireNonNull(kind, "Customer kind cannot be null");
    }

    public Customer(String name, String kindStr) {
        this(name, CustomerType.fromString(kindStr));
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void addHistory(String entry) {
        if (entry != null && !entry.isBlank()) {
            this.history.add(entry);
        }
    }

    // Expose history for reading without giving callers access to the mutable list.
    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind.getLabel();
    }

    public CustomerType getKindEnum() {
        return kind;
    }

    public boolean isRegistered() {
        return kind == CustomerType.REGISTERED;
    }

    public double getDiscountRate() {
        return kind.getDiscountRate();
    }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s', kind=%s}", id, name, kind);
    }
}

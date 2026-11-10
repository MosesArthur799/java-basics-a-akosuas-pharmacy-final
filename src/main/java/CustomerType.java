// Each membership type carries the discount used when calculating a sale.
public enum CustomerType {
    WALK_IN("WALK_IN", 0.0),
    REGISTERED("REGISTERED", 0.05);

    private final String label;
    private final double discountRate;

    CustomerType(String label, double discountRate) {
        this.label = label;
        this.discountRate = discountRate;
    }

    public String getLabel() {
        return label;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public static CustomerType fromString(String kindStr) {
        if (kindStr == null || kindStr.isBlank()) {
            throw new IllegalArgumentException("Customer kind cannot be null or empty");
        }
        String normalized = kindStr.trim().toUpperCase();
        for (CustomerType type : values()) {
            if (type.name().equals(normalized) || type.label.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown customer kind: " + kindStr);
    }

    @Override
    public String toString() {
        return label;
    }
}

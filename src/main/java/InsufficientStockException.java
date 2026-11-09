/**
 * Domain exception thrown when an attempted dispense exceeds currently available inventory.
 * Halts execution deterministically to prevent negative stock drift.
 */
public class InsufficientStockException extends RuntimeException {
    private final String medicationName;
    private final int requestedQty;
    private final int availableStock;

    public InsufficientStockException(String medicationName, int requestedQty, int availableStock) {
        super("Cannot dispense " + requestedQty + " x " + medicationName + ": only " + availableStock + " in stock");
        this.medicationName = medicationName;
        this.requestedQty = requestedQty;
        this.availableStock = availableStock;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public int getRequestedQty() {
        return requestedQty;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}

/** Raised when a dispense request exceeds available stock. */
public class InsufficientStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String medicationName;
    private final int requestedQty;
    private final int availableStock;

    // Keep the failed request details available to callers, not just in the message.
    public InsufficientStockException(String medicationName, int requestedQty, int availableStock) {
        super("Cannot dispense " + requestedQty + " x " + medicationName
                + ": only " + availableStock + " in stock");
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

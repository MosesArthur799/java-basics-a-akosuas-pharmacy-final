/**
 * Thrown when a dispense request exceeds the medication's current stock level.
 *
 * <p>This exception is raised inside {@link Medication#reduceStock(int)} —
 * the entity's own invariant guard — before any state mutation occurs.
 * As a result, catching this exception guarantees that neither stock nor the
 * audit ledger has been partially modified.</p>
 *
 * <p>Callers should catch this exception at the service boundary
 * ({@link Pharmacy#dispense}) to surface a user-friendly message without
 * propagating stack traces to the console.</p>
 */
public class InsufficientStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String medicationName;
    private final int    requestedQty;
    private final int    availableStock;

    /**
     * Constructs the exception with a formatted message and the diagnostic context
     * needed for structured error handling and logging.
     *
     * @param medicationName name of the medication for which dispensing failed
     * @param requestedQty   the quantity the caller attempted to dispense
     * @param availableStock the quantity actually on hand at the time of failure
     */
    public InsufficientStockException(String medicationName, int requestedQty, int availableStock) {
        super("Cannot dispense " + requestedQty + " x " + medicationName
                + ": only " + availableStock + " in stock");
        this.medicationName  = medicationName;
        this.requestedQty    = requestedQty;
        this.availableStock  = availableStock;
    }

    /** @return the name of the medication that triggered this exception */
    public String getMedicationName() {
        return medicationName;
    }

    /** @return the quantity that was requested but could not be fulfilled */
    public int getRequestedQty() {
        return requestedQty;
    }

    /** @return the stock level that was available at the time of the failed request */
    public int getAvailableStock() {
        return availableStock;
    }
}

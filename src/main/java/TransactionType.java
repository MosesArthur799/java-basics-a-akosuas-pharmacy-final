/**
 * Represents the kind of stock movement recorded in the pharmacy audit ledger.
 *
 * <ul>
 *   <li>{@link #DISPENSE} — medication issued to a customer or patient.</li>
 *   <li>{@link #RESTOCK} — inventory received from a supplier.</li>
 * </ul>
 */
public enum TransactionType {
    DISPENSE,
    RESTOCK
}

# Akosua's Pharmacy — Stock & Dispensing Tracker

This Java application models a small pharmacy's inventory and dispensing workflows. The [diagnosis and design document](DIAGNOSIS.md) records the analysis and proposed architecture; the [architecture diagram](architecture_blueprint.png) illustrates the domain relationships.

## Requirements

- JDK 21 or later

## Build and run

Build production and test classes:

**Windows**
```powershell
.\build.bat
```

**macOS / Linux**
```bash
sh ./build.sh
```

Run the demo:

**Windows**
```powershell
java -cp target\classes PharmacyTracker
```

**macOS / Linux**
```bash
java -cp target/classes PharmacyTracker
```

The demo dispenses two Paracetamol, dispenses one Amoxicillin, restocks
Paracetamol, then attempts to dispense more Amoxicillin than is available.
Expected output:

```
Dispensed 2 x Paracetamol 500mg to Kofi Mensah for GHS 25.0
Dispensed 1 x Amoxicillin 250mg to Ama Owusu (prescribed by Dr. Boateng) for GHS 28.5
Restocked 50 x Paracetamol 500mg
Cannot dispense 100 x Amoxicillin 250mg: only 39 in stock

=== Akosua's Pharmacy - end of day ===
Stock levels:
  Paracetamol 500mg (OTC): 148 in stock
  Amoxicillin 250mg (PRESCRIPTION): 39 in stock
Transactions today: 3
```

## Tests

Run the dependency-free regression suite:

**Windows**
```powershell
.\run-tests.bat
```

**macOS / Linux**
```bash
sh ./run-tests.sh
```

Success prints `All 18 assertions passed.` and exits with code `0`. A failure
prints its check description to standard error and exits non-zero. The test
runner does not execute or print the application demo. Failure descriptions do
not include separate expected and actual values.

The 18 assertions cover:

| # | Assertion |
|---:|---|
| 1 | OTC dispense reduces stock by the requested amount |
| 2 | OTC dispense records a transaction |
| 3 | Walk-in customer pays the undiscounted total |
| 4 | Prescription dispense with a doctor reduces stock |
| 5 | Successful prescription dispense records a transaction |
| 6 | Missing prescriber leaves prescription stock unchanged |
| 7 | Missing prescriber records no transaction |
| 8 | Registered customer receives the 5% discount |
| 9 | Over-dispense leaves stock unchanged |
| 10 | Over-dispense records no transaction |
| 11 | Over-dispense adds no customer history |
| 12 | Negative dispense quantity leaves stock unchanged |
| 13 | Restocking increments stock |
| 14 | Restocking records a transaction |
| 15 | Restock transaction has type `RESTOCK` |
| 16 | Transaction list rejects external mutation |
| 17 | Controlled dispense without a prescriber leaves stock unchanged |
| 18 | Controlled dispense with a prescriber reduces stock |

## Design and behavior

- `Medication.reduceStock(int)` rejects non-positive quantities and throws
  `InsufficientStockException` when a request exceeds available stock. It checks
  before changing inventory.
- `Pharmacy` coordinates dispensing and restocking. It validates prescribers,
  applies registered-customer discounts, and records successful transactions.
- `Category` and `CustomerType` are enums that hold category and discount policy;
  medication subclasses are not used.
- `Category.CONTROLLED` is implemented and requires a named prescriber, like
  prescription medication.
- `Transaction` records are immutable, and `Pharmacy.getTransactions()` returns
  an unmodifiable view. This is an in-memory safeguard only; it is not durable
  storage, cryptographic tamper evidence, or a regulatory audit system.
- The dispense total is `price × quantity × (1 − discount rate)`.

The starter program's over-dispense scenario was run from commit `ef61ab0`. It
dispensed 100 units after the preceding sale left 39, ending at -61 stock with
four transactions. The rebuilt demo rejects the request, leaves 39 in stock,
and records only the three successful transactions. The output above is from
the rebuilt application.

## Project layout

```
src/main/java/     Application and domain types
src/test/java/     Dependency-free regression suite
build.bat          Windows compilation script
build.sh           macOS/Linux compilation script
run-tests.bat      Windows regression test runner
run-tests.sh       macOS/Linux regression test runner
DIAGNOSIS.md       Diagnostic report and architecture design
architecture_blueprint.png
                   Architecture diagram
```

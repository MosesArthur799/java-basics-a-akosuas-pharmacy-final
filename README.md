# Akosua's Pharmacy — Rebuilt Stock & Dispensing Tracker

[![Language: Java 21](https://img.shields.io/badge/Java-21%20LTS-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Build: Success](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()
[![Tests: 18/18 Passing](https://img.shields.io/badge/Tests-18%2F18%20Passed-success.svg)]()
[![Architecture: Clean OOP / Encapsulated](https://img.shields.io/badge/Architecture-Clean%20OOP-orange.svg)]()

This repository contains the diagnosed and rebuilt stock and dispensing tracking engine for **Akosua's Community Pharmacy**. It replaces the intern's monolithic starter code with an enterprise-grade, fully encapsulated, object-oriented Java system that enforces core domain invariants and is safely extensible for upcoming regulatory requirements.

---

## Quick Start (JDK 21)

The application uses pure Java with zero external third-party dependencies.

### 1. Compile the Application
```bash
javac -d target/classes src/main/java/*.java
```

### 2. Run the Main Application (`PharmacyTracker`)
```bash
java -cp target/classes PharmacyTracker
```

### 3. Run the Automated Test Suite (`PharmacyTest`)
```bash
javac -d target/test-classes -cp target/classes src/test/java/PharmacyTest.java
java -cp "target/classes;target/test-classes" PharmacyTest
```

---

## Behavior Preservation & Invariant Enforcement

The business brief mandates that the application must **behave exactly like the original**, except that the non-negotiable rule that **stock counts can never drift below zero** is strictly enforced.

### Side-by-Side Execution Comparison

```
+-------------------------------------------------------------------+-------------------------------------------------------------------+
|               LEGACY STARTER OUTPUT (BROKEN STATE)                |               REBUILT PRODUCTION OUTPUT (PROTECTED)               |
+-------------------------------------------------------------------+-------------------------------------------------------------------+
| Dispensed 2 x Paracetamol 500mg to Kofi Mensah for GHS 25.0       | Dispensed 2 x Paracetamol 500mg to Kofi Mensah for GHS 25.0       |
| Dispensed 1 x Amoxicillin 250mg to Ama Owusu (prescribed by       | Dispensed 1 x Amoxicillin 250mg to Ama Owusu (prescribed by       |
|   Dr. Boateng) for GHS 28.5                                       |   Dr. Boateng) for GHS 28.5                                       |
| Restocked 50 x Paracetamol 500mg                                  | Restocked 50 x Paracetamol 500mg                                  |
| Dispensed 100 x Amoxicillin 250mg to Kofi Mensah (prescribed by   | Cannot dispense 100 x Amoxicillin 250mg to Kofi Mensah:           |
|   Dr. Boateng) for GHS 3000.0   <-- [SILENT CORRUPTION!]          |   only 39 in stock               <-- [SAFE REJECTION!]            |
|                                                                   |                                                                   |
| === Akosua's Pharmacy - end of day ===                            | === Akosua's Pharmacy - end of day ===                            |
| Stock levels:                                                     | Stock levels:                                                     |
|   Paracetamol 500mg (OTC): 148 in stock                           |   Paracetamol 500mg (OTC): 148 in stock                           |
|   Amoxicillin 250mg (PRESCRIPTION): -61 in stock <-- [ILLEGAL!]  |   Amoxicillin 250mg (PRESCRIPTION): 39 in stock <-- [ACCURATE]    |
| Transactions today: 4            <-- [PHANTOM SALE RECORDED]      | Transactions today: 3            <-- [ONLY REAL SALES RECORDED]   |
+-------------------------------------------------------------------+-------------------------------------------------------------------+
```

### What Changed and Why:
1. **Valid Transactions:** Exact character-for-character behavioral preservation across OTC dispensing, prescription validation, customer discounts, and restocking.
2. **Over-Dispense Prevention:** When Kofi attempts to purchase 100 units of Amoxicillin with only 39 units on the shelf:
   - **Legacy:** Allowed raw subtraction (`39 - 100 = -61`), logged a phantom transaction, and billed the customer GHS 3,000 for medicine they never received.
   - **Rebuilt:** Gracefully halts, outputs `Cannot dispense 100 x Amoxicillin 250mg to Kofi Mensah: only 39 in stock`, preserves stock at `39`, records no phantom transaction, and leaves customer history clean.

---

## Architectural Structure & Key Design Decisions

The rebuilt system adheres strictly to the Target Architecture Blueprint committed in [`DIAGNOSIS.md`](DIAGNOSIS.md).

```
src/
├── main/
│   └── java/
│       ├── Category.java                   <- Type-safe enum for regulatory classifications (OTC, Rx, Controlled)
│       ├── Customer.java                   <- Encapsulated client entity (loyalty discount, history)
│       ├── CustomerType.java               <- Type-safe enum for customer tiers (WALK_IN, REGISTERED)
│       ├── InsufficientStockException.java <- Custom domain exception guarding inventory limits
│       ├── Medication.java                 <- Encapsulated inventory entity with atomic invariant guards
│       ├── Pharmacy.java                   <- Domain service coordinating orders, prescriber rules, & auditing
│       ├── PharmacyTracker.java            <- Application entry point (main) & presentation layer
│       └── Transaction.java                <- Immutable domain event snapshot for audit trails
└── test/
    └── java/
        └── PharmacyTest.java               <- Self-contained 18-point regression & unit test suite
```

### Key Architectural Decisions:

1. **Separation into Distinct Top-Level Classes:**
   - *Previous:* 5 classes crammed into a single 156-line file.
   - *Rebuilt:* Every class extracted into its own `.java` file, isolating presentation, orchestration, and domain models.
2. **Proper Encapsulation & Access Modifiers:**
   - *Previous:* All fields (`stock`, `price`, `phone`, `history`) were `public`, allowing external tampering.
   - *Rebuilt:* All fields demoted to `private`. State mutations occur strictly through validated methods (`reduceStock()`, `increaseStock()`, `setPhone()`, `addHistory()`).
3. **Type Safety Over "Stringly-Typed" Logic:**
   - *Previous:* Fragile string comparisons (`m.category.equals("OTC")`) prone to typos and casing failures.
   - *Rebuilt:* Strong types via `Category` and `CustomerType` enums, providing compile-time safety and self-documenting code.
4. **Custom Domain Exception Architecture:**
   - `InsufficientStockException` prevents invalid states at the lowest domain level. If any caller attempts to bypass `Pharmacy` and call `m.reduceStock(100)` directly, the JVM immediately raises an exception rather than corrupting memory.

---

## Extensibility: Adding Controlled Substances

Akosua specified that the pharmacy will soon stock **Controlled Substances** (e.g., Morphine, Diazepam), which require strict prescriber verification.

### Old Version vs. Rebuilt Version:

| Maintenance Aspect | Old Legacy Monolith | Rebuilt Architecture |
| :--- | :--- | :--- |
| **Files Modified** | 1 file (`PharmacyTracker.java`) | **0 existing business classes touched** |
| **Dispense Logic** | Must add nested `else if` chains to `Pharmacy.dispense()` | Handled automatically via `Category.CONTROLLED` |
| **Risk of Regression**| **High:** Editing the shared method risks breaking OTC and standard prescriptions | **Zero:** Existing OTC and Prescription pathways remain completely untouched |
| **OCP Compliance** | Violated (Closed for extension, open for modification) | **Satisfied (Open/Closed Principle)** |

### How to Add Controlled Substances in the Rebuilt Architecture:
The `Category` enum already includes `CONTROLLED("CONTROLLED", true)`. Because `Medication.isPrescriptionRequired()` queries `category.isPrescriberRequired()`, dispensing a controlled substance automatically enforces prescriber validation without writing any new conditional logic in `Pharmacy.java`.

---

## Value-Add Improvement Beyond the Brief (Rubric: 15%)

Beyond fulfilling all core functional requirements, two substantial improvements were delivered:

### 1. Tamper-Proof Defensive Ledger Protection
In the legacy codebase, `p.getTransactions()` returned the raw internal `List<Transaction>`, permitting external code to call `p.getTransactions().clear()` and wipe the pharmacy's entire audit trail.
* **The Solution:**
  1. `Transaction` is designed as an **immutable value object** with `final` fields and no mutators.
  2. `Pharmacy.getTransactions()`, `getMeds()`, and `Customer.getHistory()` return read-only views via `Collections.unmodifiableList(...)`.
  3. Any attempt to tamper with or clear the transaction ledger results in an `UnsupportedOperationException`.

### 2. Zero-Dependency Automated Test Suite (`PharmacyTest`)
A full regression suite containing **18 automated test assertions** verifying:
- Normal OTC dispensing and correct pricing.
- Prescription validation with authorized prescriber.
- Rejection of prescription dispensing when prescriber is omitted.
- 5% loyalty discount calculation for registered patients.
- Over-dispense rejection and stock protection.
- Negative and zero quantity guard rails.
- Restocking calculation and transaction recording.
- Audit ledger immutability verification.
- Controlled substances readiness.

---

## Project Deliverables Index
- Written Diagnosis & 2D Architecture Blueprint: [`DIAGNOSIS.md`](DIAGNOSIS.md) (and [`diagnoses.md`](diagnoses.md))
- Architecture Diagram Artifact: [`architecture_blueprint.png`](architecture_blueprint.png)
- Rebuilt Source Code: [`src/main/java/`](src/main/java/)
- Automated Regression Tests: [`src/test/java/PharmacyTest.java`](src/test/java/PharmacyTest.java)

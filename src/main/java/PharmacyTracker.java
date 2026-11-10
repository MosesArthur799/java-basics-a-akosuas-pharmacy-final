/** Sets up and runs the pharmacy demo. */
public class PharmacyTracker {

    public static void main(String[] args) {
        Pharmacy p = new Pharmacy("Akosua's Pharmacy");

        Medication paracetamol = new Medication("Paracetamol 500mg", 12.50, 100, "OTC");
        Medication amoxicillin = new Medication("Amoxicillin 250mg", 30.00, 40, "PRESCRIPTION");
        p.addMedication(paracetamol);
        p.addMedication(amoxicillin);

        Customer kofi = new Customer("Kofi Mensah", "WALK_IN");
        Customer ama = new Customer("Ama Owusu", "REGISTERED");
        ama.setPhone("0244 000 000");
        p.addCustomer(kofi);
        p.addCustomer(ama);

        p.dispense(kofi, paracetamol, 2, null);
        p.dispense(ama, amoxicillin, 1, "Dr. Boateng");
        p.restock(paracetamol, 50);
        p.dispense(kofi, amoxicillin, 100, "Dr. Boateng");

        p.printReport();
    }
}

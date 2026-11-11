package inventory;

import java.util.Scanner;

/** Console menu for the Stack-based Grocery Store Inventory System. */
public class ConsoleMain {
    private static final String DATA_FILE = "data/inventory.txt";
    private static final Scanner sc = new Scanner(System.in);
    private static final Inventory inv = new Inventory();

    // ---------------- input helpers ----------------
    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextLine()) { System.out.println(); System.exit(0); }
        return sc.nextLine().trim().replace('|', ' ');
    }

    private static int readInt(String prompt, int min) {
        while (true) {
            try {
                int v = Integer.parseInt(readLine(prompt));
                if (v >= min) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("   ! Enter a whole number >= " + min + ".");
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try {
                double v = Double.parseDouble(readLine(prompt));
                if (v >= 0) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("   ! Enter a valid positive number.");
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) return s;
            System.out.println("   ! This field cannot be empty.");
        }
    }

    // ---------------- menu actions ----------------
    private static void add() {
        System.out.println("\n--- ADD PRODUCT (PUSH) ---");
        int id = readInt("   Product ID        : ", 1);
        String name = readNonEmpty("   Name              : ");
        String cat = readNonEmpty("   Category          : ");
        int qty = readInt("   Quantity          : ", 0);
        double price = readDouble("   Price per unit Rs.: ");
        if (inv.addProduct(new Product(id, name, cat, qty, price)))
            System.out.println("   Product pushed onto the stack successfully.");
    }

    private static void update() {
        System.out.println("\n--- UPDATE PRODUCT ---");
        int id = readInt("   Enter ID to update: ", 1);
        Product p = inv.find(id);
        if (p == null) { System.out.println("   Product with ID " + id + " not found."); return; }
        System.out.println("   (press Enter to keep the current value)");
        String name = readLine("   Name [" + p.name + "]: ");
        String cat = readLine("   Category [" + p.category + "]: ");
        int qty = -1;
        String q = readLine("   Quantity [" + p.quantity + "]: ");
        if (!q.isEmpty()) {
            try { qty = Integer.parseInt(q); if (qty < 0) { qty = -1; System.out.println("   ! Negative ignored."); } }
            catch (NumberFormatException e) { System.out.println("   ! Invalid, kept old quantity."); }
        }
        double price = -1;
        String pr = readLine("   Price [" + p.price + "]: ");
        if (!pr.isEmpty()) {
            try { price = Double.parseDouble(pr); if (price < 0) { price = -1; System.out.println("   ! Negative ignored."); } }
            catch (NumberFormatException e) { System.out.println("   ! Invalid, kept old price."); }
        }
        if (inv.updateProduct(id, name, cat, qty, price)) System.out.println("   Product updated successfully.");
    }

    private static void delete() {
        System.out.println("\n--- DELETE PRODUCT ---");
        int id = readInt("   Enter ID to delete: ", 1);
        if (inv.deleteProduct(id)) System.out.println("   Product deleted successfully.");
        else System.out.println("   Product with ID " + id + " not found.");
    }

    private static void menu() {
        System.out.println("\n==================================================");
        System.out.println("   GROCERY STORE INVENTORY  (Stack Based)");
        System.out.println("==================================================");
        System.out.println("   1.  Add product              (CREATE - Push)");
        System.out.println("   2.  Display all products     (READ)");
        System.out.println("   3.  View top product         (READ - Peek)");
        System.out.println("   4.  Search by ID             (READ)");
        System.out.println("   5.  Search by name           (READ)");
        System.out.println("   6.  Update product           (UPDATE)");
        System.out.println("   7.  Delete product by ID     (DELETE)");
        System.out.println("   8.  Remove top product       (DELETE - Pop)");
        System.out.println("   9.  Undo last change         (History stack)");
        System.out.println("   10. Low stock report");
        System.out.println("   11. Inventory summary");
        System.out.println("   12. Save to file");
        System.out.println("   13. Toggle stack trace       (currently " + (inv.isTrace() ? "ON" : "OFF") + ")");
        System.out.println("   0.  Save & Exit");
    }

    public static void main(String[] args) {
        if (inv.load(DATA_FILE))
            System.out.println("Loaded " + inv.count() + " products from " + DATA_FILE);
        else
            System.out.println("No saved data found - starting with an empty inventory.");

        while (true) {
            menu();
            int choice = readInt("Enter choice: ", 0);
            System.out.println();
            switch (choice) {
                case 1:  add(); break;
                case 2:  inv.displayAll(); break;
                case 3:  inv.peekTop(); break;
                case 4:  inv.searchById(readInt("   Enter ID: ", 1)); break;
                case 5:  inv.searchByName(readNonEmpty("   Enter name (or part): ")); break;
                case 6:  update(); break;
                case 7:  delete(); break;
                case 8:  inv.popTop(); break;
                case 9:  inv.undo(); break;
                case 10: inv.lowStockReport(); break;
                case 11: inv.summary(); break;
                case 12: System.out.println(inv.save(DATA_FILE) ? "   Saved to " + DATA_FILE : "   ! Could not save file."); break;
                case 13: inv.setTrace(!inv.isTrace()); System.out.println("   Stack trace " + (inv.isTrace() ? "ON" : "OFF")); break;
                case 0:
                    System.out.println(inv.save(DATA_FILE) ? "Data saved. Goodbye!" : "Could not save data. Goodbye!");
                    return;
                default: System.out.println("   ! Invalid choice.");
            }
        }
    }
}

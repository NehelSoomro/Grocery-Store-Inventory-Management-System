package inventory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Grocery Store Inventory built entirely on STACKS.
 *
 *  items   : main stack  -> every product is one element.
 *            Top of the stack = most recently added product.
 *  history : second stack -> remembers actions for UNDO.
 *  temp    : helper stack created inside operations that must reach
 *            an element that is not on top. In a pure stack the only
 *            way is: pop, pop, pop ... then push everything back.
 */
public class Inventory {
    public static final int LOW_STOCK_LIMIT = 10;

    private final Stack<Product> items = new Stack<>();
    private final Stack<Action> history = new Stack<>();
    private boolean trace = true;           // print internal push/pop steps
    private boolean muted = false;          // temporarily silence the trace
    private Consumer<String> listener;      // GUI hook: receives trace/messages

    /** When a listener is set (GUI), messages go to it instead of the console. */
    public void setListener(Consumer<String> l) { listener = l; }

    public void setTrace(boolean t) { trace = t; }
    public boolean isTrace() { return trace; }
    public int count() { return items.size(); }

    private void emit(String s) {
        if (listener != null) listener.accept(s.trim()); else System.out.println(s);
    }

    private void log(String msg) {
        if (trace && !muted) emit("      [STACK] " + msg);
    }

    private void message(String msg) { emit("   " + msg); }

    // ------------------------------------------------------------------
    //  Internal helpers
    // ------------------------------------------------------------------

    /** Push everything from temp back onto the main stack (restores order). */
    private void restore(Stack<Product> temp) {
        int n = 0;
        while (!temp.isEmpty()) { items.push(temp.pop()); n++; }
        if (n > 0) log("Pushed " + n + " item(s) back (original order restored)");
    }

    /**
     * Pop items into temp until the product with 'id' is on top of the
     * main stack. Returns that product (still on the stack) or null.
     */
    private Product digTo(int id, Stack<Product> temp) {
        while (!items.isEmpty()) {
            Product p = items.peek();
            if (p.id == id) return p;
            temp.push(items.pop());
            log("Popped ID " + p.id + " -> moved to temp stack");
        }
        return null;
    }

    /** Insert p so that exactly 'pos' items lie beneath it. */
    private void insertAt(int pos, Product p) {
        Stack<Product> temp = new Stack<>();
        while (items.size() > pos) temp.push(items.pop());
        items.push(p);
        restore(temp);
    }

    // ------------------------------------------------------------------
    //  CREATE
    // ------------------------------------------------------------------

    /** PUSH a new product on top of the stack. */
    public boolean addProduct(Product p) { return addProduct(p, true); }

    private boolean addProduct(Product p, boolean record) {
        if (find(p.id) != null) {
            message("! A product with ID " + p.id + " already exists.");
            return false;
        }
        items.push(p);
        log("PUSH  ID " + p.id + " (" + p.name + ") on top");
        if (record) history.push(new Action(Action.Type.ADD, p.copy(), 0));
        return true;
    }

    // ------------------------------------------------------------------
    //  READ
    // ------------------------------------------------------------------

    private static void printHeader() {
        System.out.printf("   %-6s%-22s%-14s%-8s%-12s%n", "ID", "Name", "Category", "Qty", "Price(Rs)");
        System.out.println("   " + "-".repeat(62));
    }

    private static void printRow(Product p, String tag) {
        System.out.printf("   %-6d%-22s%-14s%-8d%-12.2f%s%n", p.id, cut(p.name, 20),
                cut(p.category, 12), p.quantity, p.price, tag);
    }

    private static String cut(String s, int n) { return s.length() > n ? s.substring(0, n) : s; }

    /** Display all products from TOP to BOTTOM (pop to temp, then restore). */
    public void displayAll() {
        if (items.isEmpty()) { System.out.println("   Inventory is empty."); return; }
        Stack<Product> temp = new Stack<>();
        int total = items.size(), i = 0;
        printHeader();
        while (!items.isEmpty()) {
            Product p = items.pop();
            printRow(p, i == 0 ? "<-- TOP" : (i == total - 1 ? "<-- BOTTOM" : ""));
            temp.push(p);
            i++;
        }
        restore(temp);
        System.out.println("   Total products: " + total);
    }

    /** PEEK: show the most recently added product without removing it. */
    public void peekTop() {
        if (items.isEmpty()) { System.out.println("   Inventory is empty."); return; }
        log("PEEK  -> top element read, nothing removed");
        printHeader();
        printRow(items.peek(), "<-- TOP");
    }

    /** Find a product by ID (returns a copy, or null). Uses pop/restore. */
    public Product find(int id) {
        Stack<Product> temp = new Stack<>();
        boolean saved = trace;
        Product p = digTo(id, temp);
        Product copy = (p == null) ? null : p.copy();
        restore(temp);
        trace = saved;
        return copy;
    }

    public void searchById(int id) {
        Product p = find(id);
        if (p == null) { System.out.println("   Product with ID " + id + " not found."); return; }
        printHeader();
        printRow(p, "");
    }

    /** Case-insensitive search by part of the name. */
    public void searchByName(String key) {
        String k = key.toLowerCase();
        Stack<Product> temp = new Stack<>();
        boolean any = false;
        while (!items.isEmpty()) {
            Product p = items.pop();
            temp.push(p);
            if (p.name.toLowerCase().contains(k)) {
                if (!any) printHeader();
                printRow(p, "");
                any = true;
            }
        }
        restore(temp);
        if (!any) System.out.println("   No product matches \"" + key + "\".");
    }

    // ------------------------------------------------------------------
    //  UPDATE
    // ------------------------------------------------------------------

    /** Dig down to the product, modify it, push everything back. Null/negative = keep old value. */
    public boolean updateProduct(int id, String name, String cat, int qty, double price) {
        return updateProduct(id, name, cat, qty, price, true);
    }

    private boolean updateProduct(int id, String name, String cat, int qty, double price, boolean record) {
        Stack<Product> temp = new Stack<>();
        Product target = digTo(id, temp);
        if (target == null) { restore(temp); return false; }

        Product old = target.copy();
        Product cur = items.pop();                  // take target off the top
        if (name != null && !name.isEmpty()) cur.name = name;
        if (cat != null && !cat.isEmpty()) cur.category = cat;
        if (qty >= 0) cur.quantity = qty;
        if (price >= 0) cur.price = price;
        items.push(cur);                            // push modified version
        log("Target ID " + id + " popped, modified and pushed back");
        restore(temp);

        if (record) history.push(new Action(Action.Type.UPDATE, old, 0));
        return true;
    }

    // ------------------------------------------------------------------
    //  DELETE
    // ------------------------------------------------------------------

    /** Delete by ID: dig down, discard the target, restore the rest. */
    public boolean deleteProduct(int id) { return deleteProduct(id, true); }

    private boolean deleteProduct(int id, boolean record) {
        Stack<Product> temp = new Stack<>();
        if (digTo(id, temp) == null) { restore(temp); return false; }

        Product gone = items.pop();
        int pos = items.size();                     // items beneath it
        log("POP   ID " + id + " (" + gone.name + ") discarded");
        restore(temp);

        if (record) history.push(new Action(Action.Type.DELETE, gone, pos));
        return true;
    }

    /** Pure stack POP: removes the most recently added product. */
    public boolean popTop() {
        if (items.isEmpty()) { message("Inventory is empty - nothing to pop."); return false; }
        Product p = items.pop();
        log("POP   top element ID " + p.id + " (" + p.name + ")");
        history.push(new Action(Action.Type.DELETE, p, items.size()));
        message("Removed top product: " + p.name + " (ID " + p.id + ")");
        return true;
    }

    // ------------------------------------------------------------------
    //  UNDO  (the history stack gives LIFO undo for free)
    // ------------------------------------------------------------------
    public boolean undo() {
        if (history.isEmpty()) { message("Nothing to undo."); return false; }
        Action a = history.pop();
        log("Popped last action from HISTORY stack");
        switch (a.type) {
            case ADD:
                deleteProduct(a.product.id, false);
                message("Undone: addition of \"" + a.product.name + "\"");
                break;
            case DELETE:
                insertAt(a.position, a.product);
                message("Undone: deletion of \"" + a.product.name + "\" (restored)");
                break;
            case UPDATE:
                updateProduct(a.product.id, a.product.name, a.product.category,
                        a.product.quantity, a.product.price, false);
                message("Undone: update of \"" + a.product.name + "\"");
                break;
        }
        return true;
    }

    // ------------------------------------------------------------------
    //  GUI helpers (return data instead of printing)
    // ------------------------------------------------------------------

    /** All products from TOP to BOTTOM (copies). Pops to temp, then restores. */
    public List<Product> getAll() {
        boolean m = muted; muted = true;
        List<Product> list = new ArrayList<>();
        Stack<Product> temp = new Stack<>();
        while (!items.isEmpty()) { Product p = items.pop(); list.add(p.copy()); temp.push(p); }
        restore(temp);
        muted = m;
        return list;
    }

    /** Products whose name contains 'key' or whose ID contains 'key' (stack traversal). */
    public List<Product> searchList(String key) {
        String k = key.toLowerCase();
        boolean m = muted; muted = true;
        List<Product> list = new ArrayList<>();
        Stack<Product> temp = new Stack<>();
        while (!items.isEmpty()) {
            Product p = items.pop();
            temp.push(p);
            if (p.name.toLowerCase().contains(k) || String.valueOf(p.id).contains(k)
                    || p.category.toLowerCase().contains(k)) list.add(p.copy());
        }
        restore(temp);
        muted = m;
        return list;
    }

    /** PEEK: returns a copy of the top product (null if empty). */
    public Product peek() {
        if (items.isEmpty()) return null;
        log("PEEK  -> top element read, nothing removed");
        return items.peek().copy();
    }

    public int historySize() { return history.size(); }

    // ------------------------------------------------------------------
    //  REPORTS
    // ------------------------------------------------------------------
    public void lowStockReport() {
        Stack<Product> temp = new Stack<>();
        boolean any = false;
        while (!items.isEmpty()) {
            Product p = items.pop();
            temp.push(p);
            if (p.quantity < LOW_STOCK_LIMIT) {
                if (!any) {
                    System.out.println("   Products with quantity below " + LOW_STOCK_LIMIT + ":");
                    printHeader();
                }
                printRow(p, "");
                any = true;
            }
        }
        restore(temp);
        if (!any) System.out.println("   All products are sufficiently stocked.");
    }

    public void summary() {
        Stack<Product> temp = new Stack<>();
        double value = 0;
        long units = 0;
        while (!items.isEmpty()) {
            Product p = items.pop();
            temp.push(p);
            value += p.quantity * p.price;
            units += p.quantity;
        }
        restore(temp);
        System.out.println("   Distinct products : " + items.size());
        System.out.println("   Total units       : " + units);
        System.out.printf("   Inventory value   : Rs. %.2f%n", value);
    }

    // ------------------------------------------------------------------
    //  FILE STORAGE   format: id|name|category|quantity|price  (bottom -> top)
    // ------------------------------------------------------------------
    public boolean save(String path) {
        File parent = new File(path).getAbsoluteFile().getParentFile();
        if (parent != null) parent.mkdirs();
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            Stack<Product> temp = new Stack<>();
            while (!items.isEmpty()) temp.push(items.pop());   // bottom is now on top
            while (!temp.isEmpty()) {                          // write bottom -> top
                Product p = temp.pop();
                out.println(p.id + "|" + p.name + "|" + p.category + "|" + p.quantity + "|" + p.price);
                items.push(p);                                 // rebuild stack
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean load(String path) {
        File f = new File(path);
        if (!f.exists()) return false;
        boolean saved = trace;
        trace = false;
        int loaded = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] t = line.split("\\|");
                if (t.length < 5) continue;
                try {
                    Product p = new Product(Integer.parseInt(t[0].trim()), t[1], t[2],
                            Integer.parseInt(t[3].trim()), Double.parseDouble(t[4].trim()));
                    if (addProduct(p, false)) loaded++;
                } catch (NumberFormatException ignored) { }
            }
        } catch (IOException e) {
            trace = saved;
            return false;
        }
        trace = saved;
        return loaded > 0;
    }
}

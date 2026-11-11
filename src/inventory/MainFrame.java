package inventory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Swing GUI for the Stack based Grocery Store Inventory. */
public class MainFrame extends JFrame {
    // palette
    private static final Color NAVY = new Color(0x1f2a44);
    private static final Color BG = new Color(0xf3f5f9);
    private static final Color BORDER = new Color(0xd9dee8);
    private static final Color GREEN = new Color(0x27ae60);
    private static final Color BLUE = new Color(0x2980b9);
    private static final Color RED = new Color(0xe74c3c);
    private static final Color ORANGE = new Color(0xe67e22);
    private static final Color PURPLE = new Color(0x8e44ad);
    private static final Color SLATE = new Color(0x5d6d7e);

    private final Inventory inv = new Inventory();
    private final String dataFile;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Stack Pos", "ID", "Name", "Category", "Qty", "Price (Rs)", "Value (Rs)"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final StackView stackView = new StackView();
    private final JTextField fId = new JTextField(), fName = new JTextField(), fCat = new JTextField(),
            fQty = new JTextField(), fPrice = new JTextField(), fSearch = new JTextField();
    private final JTextArea log = new JTextArea();
    private final JLabel status = new JLabel(" Ready");
    private final JLabel cardProducts = new JLabel(), cardUnits = new JLabel(),
            cardValue = new JLabel(), cardLow = new JLabel();

    public MainFrame(String dataFile) {
        super("Grocery Store Inventory  -  Stack Based  (SW 212 DSA)");
        this.dataFile = dataFile;
        inv.setListener(this::appendLog);

        buildUI();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { inv.save(MainFrame.this.dataFile); dispose(); System.exit(0); }
        });
        setSize(1280, 860);
        setMinimumSize(new Dimension(1100, 780));
        setLocationRelativeTo(null);

        inv.setListener(null);
        boolean loaded = inv.load(dataFile);
        inv.setListener(this::appendLog);
        if (loaded) appendLog("Loaded " + inv.count() + " products from file.");
        refresh();
        clearForm();
    }

    // ===================================================================
    //  UI construction
    // ===================================================================
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        setContentPane(root);

        // ---- header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(14, 22, 14, 22));
        JLabel title = new JLabel("Grocery Store Inventory");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel sub = new JLabel("Data Structure: STACK (LIFO)   •   Push • Pop • Peek • Undo   •   SW 212 Data Structures & Algorithms");
        sub.setForeground(new Color(0xb8c4de));
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);
        header.add(titles, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // ---- body ----
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(12, 12, 6, 12));
        root.add(body, BorderLayout.CENTER);

        body.add(buildStatsRow(), BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(12, 0));
        middle.setOpaque(false);
        middle.add(buildStackCard(), BorderLayout.WEST);
        middle.add(buildTableCard(), BorderLayout.CENTER);
        middle.add(buildFormCard(), BorderLayout.EAST);
        body.add(middle, BorderLayout.CENTER);

        body.add(buildLogCard(), BorderLayout.SOUTH);

        // ---- status bar ----
        status.setBorder(new EmptyBorder(6, 16, 6, 16));
        status.setOpaque(true);
        status.setBackground(Color.WHITE);
        root.add(status, BorderLayout.SOUTH);
    }

    private JPanel card(String titleText, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 12, 12, 12)));
        JLabel t = new JLabel(titleText);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
        t.setForeground(NAVY);
        p.add(t, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.add(statCard("Products in stack", cardProducts, BLUE));
        row.add(statCard("Total units", cardUnits, GREEN));
        row.add(statCard("Inventory value", cardValue, PURPLE));
        row.add(statCard("Low stock (< " + Inventory.LOW_STOCK_LIMIT + ")", cardLow, RED));
        return row;
    }

    private JPanel statCard(String label, JLabel value, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 5, 0, 0, accent),
                new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(8, 14, 8, 14))));
        JLabel l = new JLabel(label);
        l.setForeground(SLATE);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        value.setForeground(accent);
        p.add(l, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStackCard() {
        JScrollPane sp = new JScrollPane(stackView, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(new LineBorder(BORDER));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        JPanel c = card("Stack visualization", sp);
        c.setPreferredSize(new Dimension(250, 100));
        return c;
    }

    private JPanel buildTableCard() {
        // search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search (name / ID / category):"), BorderLayout.WEST);
        searchBar.add(fSearch, BorderLayout.CENTER);
        fSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        table.setRowHeight(28);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0xeceff5));
        table.setSelectionBackground(new Color(0xfff2c2));
        table.setSelectionForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader th = table.getTableHeader();
        th.setBackground(NAVY);
        th.setForeground(Color.WHITE);
        th.setFont(th.getFont().deriveFont(Font.BOLD));
        th.setReorderingAllowed(false);
        int[] widths = {80, 60, 220, 120, 60, 90, 100};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(c >= 4 ? RIGHT : LEFT);
                int qty = Integer.parseInt(String.valueOf(model.getValueAt(r, 4)));
                String pos = String.valueOf(model.getValueAt(r, 0));
                if (!sel) {
                    setBackground(pos.startsWith("TOP") ? new Color(0xdff7ef) : Color.WHITE);
                    setForeground(qty < Inventory.LOW_STOCK_LIMIT ? new Color(0xc0392b) : new Color(0x22304a));
                }
                setFont(getFont().deriveFont(pos.startsWith("TOP") || (c == 4 && qty < Inventory.LOW_STOCK_LIMIT)
                        ? Font.BOLD : Font.PLAIN));
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int r = table.getSelectedRow();
            if (r < 0) return;
            fId.setText(String.valueOf(model.getValueAt(r, 1)));
            fName.setText(String.valueOf(model.getValueAt(r, 2)));
            fCat.setText(String.valueOf(model.getValueAt(r, 3)));
            fQty.setText(String.valueOf(model.getValueAt(r, 4)));
            fPrice.setText(String.valueOf(model.getValueAt(r, 5)).replace(",", ""));
            stackView.setData(inv.getAll(), Integer.parseInt(fId.getText()));
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);

        JPanel inner = new JPanel(new BorderLayout(0, 8));
        inner.setOpaque(false);
        inner.add(searchBar, BorderLayout.NORTH);
        inner.add(sp, BorderLayout.CENTER);
        return card("Products  (top of stack first)", inner);
    }

    private JPanel buildFormCard() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.insets = new Insets(2, 0, 2, 0);
        fId.setEditable(false);
        fId.setBackground(new Color(0xeef1f6));
        fId.setForeground(NAVY);
        fId.setFont(fId.getFont().deriveFont(Font.BOLD));
        int row = 0;
        String[] labels = {"Product ID (automatic)", "Name", "Category", "Quantity", "Price per unit (Rs)"};
        JTextField[] fields = {fId, fName, fCat, fQty, fPrice};
        for (int i = 0; i < labels.length; i++) {
            gc.gridy = row++;
            JLabel l = new JLabel(labels[i]);
            l.setForeground(SLATE);
            form.add(l, gc);
            gc.gridy = row++;
            fields[i].setPreferredSize(new Dimension(240, 30));
            fields[i].setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(2, 8, 2, 8)));
            form.add(fields[i], gc);
        }

        JPanel btns = new JPanel(new GridLayout(4, 2, 8, 6));
        btns.setOpaque(false);
        btns.add(button("PUSH (Add)", GREEN, e -> doAdd()));
        btns.add(button("UPDATE", BLUE, e -> doUpdate()));
        btns.add(button("DELETE by ID", RED, e -> doDelete()));
        btns.add(button("POP Top", ORANGE, e -> doPop()));
        btns.add(button("PEEK Top", SLATE, e -> doPeek()));
        btns.add(button("UNDO", PURPLE, e -> doUndo()));
        btns.add(button("Clear Form", new Color(0x95a5a6), e -> clearForm()));
        btns.add(button("Low Stock", new Color(0xc0392b), e -> showLowStock()));
        gc.gridy = row++;
        gc.insets = new Insets(12, 0, 0, 0);
        form.add(btns, gc);

        gc.gridy = row;
        gc.weighty = 1;
        form.add(Box.createVerticalGlue(), gc);

        JPanel c = card("Product form", form);
        c.setPreferredSize(new Dimension(290, 100));
        return c;
    }

    private JButton button(String text, Color bg, ActionListener a) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12.5f));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(120, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(a);
        return b;
    }

    private JPanel buildLogCard() {
        log.setEditable(false);
        log.setBackground(new Color(0x16202f));
        log.setForeground(new Color(0x7fe3c4));
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        log.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane sp = new JScrollPane(log);
        sp.setBorder(new LineBorder(BORDER));
        sp.setPreferredSize(new Dimension(100, 105));
        JPanel c = card("Stack operations log  (every push / pop happening inside)", sp);
        JButton clr = new JButton("Clear log");
        clr.setFocusPainted(false);
        clr.addActionListener(e -> log.setText(""));
        JPanel top = (JPanel) c;
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        Component title = ((BorderLayout) top.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        top.remove(title);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(clr, BorderLayout.EAST);
        top.add(titleRow, BorderLayout.NORTH);
        return c;
    }

    // ===================================================================
    //  Actions
    // ===================================================================
    private void appendLog(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void setStatus(String s, boolean ok) {
        status.setForeground(ok ? new Color(0x1e8449) : new Color(0xc0392b));
        status.setText(" " + s);
    }

    private void error(String msg) {
        setStatus(msg, false);
        JOptionPane.showMessageDialog(this, msg, "Invalid input", JOptionPane.WARNING_MESSAGE);
    }

    /** Reads and validates the form. Returns null (after showing an error) if invalid. */
    private Product readForm(boolean forNew) {
        int id, qty;
        double price;
        if (forNew) id = nextId();
        else {
            try { id = Integer.parseInt(fId.getText().trim()); if (id < 1) throw new NumberFormatException(); }
            catch (NumberFormatException e) { error("Select a product from the table first."); return null; }
        }
        String name = fName.getText().trim().replace('|', ' ');
        String cat = fCat.getText().trim().replace('|', ' ');
        if (name.isEmpty()) { error("Name cannot be empty."); return null; }
        if (cat.isEmpty()) { error("Category cannot be empty."); return null; }
        try { qty = Integer.parseInt(fQty.getText().trim()); if (qty < 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { error("Quantity must be a whole number (0 or more)."); return null; }
        try { price = Double.parseDouble(fPrice.getText().trim()); if (price < 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { error("Price must be a valid number (0 or more)."); return null; }
        return new Product(id, name, cat, qty, price);
    }

    /** Next free product ID = highest existing ID + 1 (starts at 101). */
    private int nextId() {
        int max = 100;
        for (Product p : inv.getAll()) max = Math.max(max, p.id);
        return max + 1;
    }

    private void doAdd() {
        Product p = readForm(true);
        if (p == null) return;
        appendLog("\n>> PUSH  \"" + p.name + "\"");
        if (inv.addProduct(p)) {
            afterChange("Pushed \"" + p.name + "\" onto the stack.");
            clearForm();
        } else {
            setStatus("ID " + p.id + " already exists.", false);
            JOptionPane.showMessageDialog(this, "A product with ID " + p.id + " already exists.",
                    "Duplicate ID", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doUpdate() {
        if (table.getSelectedRow() < 0 && inv.find(parseIdOrZero()) == null) {
            error("Click a product in the table first, then change its details and press UPDATE.");
            return;
        }
        Product p = readForm(false);
        if (p == null) return;
        appendLog("\n>> UPDATE  ID " + p.id);
        if (inv.updateProduct(p.id, p.name, p.category, p.quantity, p.price))
            afterChange("Updated product ID " + p.id + ".");
        else {
            setStatus("Product with ID " + p.id + " not found.", false);
            JOptionPane.showMessageDialog(this, "No product with ID " + p.id + " found.",
                    "Not found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int parseIdOrZero() {
        try { return Integer.parseInt(fId.getText().trim()); } catch (NumberFormatException e) { return 0; }
    }

    private void doDelete() {
        int id;
        try { id = Integer.parseInt(fId.getText().trim()); }
        catch (NumberFormatException e) { error("Click a product in the table first."); return; }
        Product p = inv.find(id);
        if (p == null) { error("No product with ID " + id + " found."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Delete \"" + p.name + "\" (ID " + id + ")?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        appendLog("\n>> DELETE  ID " + id);
        inv.deleteProduct(id);
        afterChange("Deleted \"" + p.name + "\".");
        clearForm();
    }

    private void doPop() {
        appendLog("\n>> POP  (remove top)");
        if (inv.popTop()) afterChange("Popped the top product.");
        else setStatus("Stack is empty - nothing to pop.", false);
    }

    private void doPeek() {
        appendLog("\n>> PEEK");
        Product p = inv.peek();
        if (p == null) { setStatus("Stack is empty.", false); return; }
        setStatus("Top of stack: " + p.name + " (ID " + p.id + ")", true);
        JOptionPane.showMessageDialog(this,
                "Top of the stack (not removed):\n\nID: " + p.id + "\nName: " + p.name + "\nCategory: " + p.category
                        + "\nQuantity: " + p.quantity + "\nPrice: Rs " + p.price,
                "PEEK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doUndo() {
        appendLog("\n>> UNDO");
        if (inv.undo()) afterChange("Last change undone.");
        else setStatus("Nothing to undo.", false);
    }

    private void showLowStock() {
        StringBuilder sb = new StringBuilder();
        for (Product p : inv.getAll())
            if (p.quantity < Inventory.LOW_STOCK_LIMIT)
                sb.append("• ").append(p.name).append("  (ID ").append(p.id).append(")  -  only ")
                  .append(p.quantity).append(" left\n");
        if (sb.length() == 0) sb.append("All products are sufficiently stocked.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Low stock (below " + Inventory.LOW_STOCK_LIMIT + ")",
                JOptionPane.WARNING_MESSAGE);
    }

    private void clearForm() {
        fId.setText(String.valueOf(nextId())); fName.setText(""); fCat.setText(""); fQty.setText(""); fPrice.setText("");
        table.clearSelection();
        stackView.setData(inv.getAll(), -1);
    }

    private void afterChange(String msg) {
        inv.save(dataFile);
        refresh();
        setStatus(msg, true);
    }

    // ===================================================================
    //  Refresh table / stack drawing / stat cards
    // ===================================================================
    public void refresh() {
        List<Product> all = inv.getAll();
        String key = fSearch.getText().trim();
        Set<Integer> visible = null;
        if (!key.isEmpty()) {
            visible = new HashSet<>();
            for (Product p : inv.searchList(key)) visible.add(p.id);
        }

        model.setRowCount(0);
        double value = 0;
        long units = 0;
        int low = 0;
        for (int i = 0; i < all.size(); i++) {
            Product p = all.get(i);
            value += p.quantity * p.price;
            units += p.quantity;
            if (p.quantity < Inventory.LOW_STOCK_LIMIT) low++;
            if (visible != null && !visible.contains(p.id)) continue;
            String pos = i == 0 ? "TOP" : (i == all.size() - 1 ? "BOTTOM" : String.valueOf(all.size() - i));
            if (all.size() == 1) pos = "TOP";
            model.addRow(new Object[]{pos, p.id, p.name, p.category, p.quantity,
                    String.format("%.2f", p.price), String.format("%,.2f", p.quantity * p.price)});
        }
        cardProducts.setText(String.valueOf(all.size()));
        cardUnits.setText(String.format("%,d", units));
        cardValue.setText(String.format("Rs %,.0f", value));
        cardLow.setText(String.valueOf(low));

        int sel = -1;
        try { sel = Integer.parseInt(fId.getText().trim()); } catch (NumberFormatException ignored) { }
        stackView.setData(all, sel);
    }
}

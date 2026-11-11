package inventory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Draws the stack as boxes: top element first, so push/pop is visible. */
public class StackView extends JPanel {
    private List<Product> data = new ArrayList<>();
    private int selectedId = -1;

    private static final Color TOP_COLOR = new Color(0x1abc9c);
    private static final Color SEL_COLOR = new Color(0xf7c948);
    private static final Color BOX_COLOR = new Color(0xdbe4f3);
    private static final Color LOW_COLOR = new Color(0xf9c9c9);

    public StackView() { setBackground(Color.WHITE); }

    public void setData(List<Product> d, int selected) {
        data = d;
        selectedId = selected;
        setPreferredSize(new Dimension(200, 90 + Math.max(1, d.size()) * 48));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        Font base = getFont();

        g.setFont(base.deriveFont(Font.BOLD, 12f));
        g.setColor(new Color(0x1abc9c).darker());
        g.drawString("▼  TOP  (push / pop here)", 14, 20);

        if (data.isEmpty()) {
            g.setColor(Color.GRAY);
            g.setFont(base.deriveFont(Font.ITALIC, 13f));
            g.drawString("Stack is empty", w / 2 - 45, 60);
        }

        int y = 32;
        for (int i = 0; i < data.size(); i++) {
            Product p = data.get(i);
            Color c = i == 0 ? TOP_COLOR : (p.quantity < Inventory.LOW_STOCK_LIMIT ? LOW_COLOR : BOX_COLOR);
            if (p.id == selectedId) c = SEL_COLOR;
            g.setColor(new Color(0, 0, 0, 25));
            g.fill(new RoundRectangle2D.Double(16, y + 3, w - 30, 40, 12, 12));
            g.setColor(c);
            g.fill(new RoundRectangle2D.Double(14, y, w - 30, 40, 12, 12));
            g.setColor(i == 0 ? Color.WHITE : new Color(0x22304a));
            g.setFont(base.deriveFont(Font.BOLD, 13f));
            g.drawString(fit("#" + p.id + "  " + p.name, g.getFontMetrics(), w - 54), 24, y + 17);
            g.setFont(base.deriveFont(Font.PLAIN, 11f));
            g.drawString("Qty " + p.quantity + "   •   Rs " + String.format("%.0f", p.price), 24, y + 32);
            y += 48;
        }

        if (!data.isEmpty()) {
            g.setColor(new Color(0x22304a));
            g.fill(new RoundRectangle2D.Double(10, y, w - 22, 8, 6, 6));
            g.setFont(base.deriveFont(Font.BOLD, 11f));
            g.setColor(Color.GRAY);
            g.drawString("BOTTOM", w / 2 - 25, y + 24);
        }
        g.dispose();
    }

    private static String fit(String s, FontMetrics fm, int maxW) {
        if (fm.stringWidth(s) <= maxW) return s;
        while (s.length() > 1 && fm.stringWidth(s + "...") > maxW) s = s.substring(0, s.length() - 1);
        return s + "...";
    }
}

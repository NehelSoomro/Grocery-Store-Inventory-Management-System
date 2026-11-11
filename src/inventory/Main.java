package inventory;

import javax.swing.*;
import java.io.File;

/** Launches the graphical (Swing) version of the inventory system. */
public class Main {
    /** data/inventory.txt next to the jar (or next to the 'out' folder / project root). */
    static String dataPath() {
        try {
            File loc = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File base = loc.isFile() ? loc.getParentFile() : loc.getParentFile();   // jar dir or project root
            return new File(base, "data/inventory.txt").getPath();
        } catch (Exception e) {
            return "data/inventory.txt";
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainFrame(dataPath()).setVisible(true));
    }
}

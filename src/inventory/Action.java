package inventory;

/** A reversible action stored on the history stack (used by UNDO). */
public class Action {
    public enum Type { ADD, DELETE, UPDATE }

    public final Type type;
    public final Product product;   // snapshot of the product before the change
    public final int position;      // number of items beneath it in the stack

    public Action(Type type, Product product, int position) {
        this.type = type;
        this.product = product;
        this.position = position;
    }
}

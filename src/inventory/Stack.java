package inventory;

/**
 * Generic array-based Stack (LIFO) written from scratch.
 * java.util.Stack is NOT used, so every push/pop is our own logic.
 * Capacity doubles automatically when the stack becomes full.
 */
public class Stack<T> {
    private Object[] data;
    private int topIndex = -1;   // index of top element (-1 when empty)

    public Stack() { this(8); }

    public Stack(int capacity) {
        data = new Object[capacity > 0 ? capacity : 8];
    }

    public boolean isEmpty() { return topIndex == -1; }

    public int size() { return topIndex + 1; }

    /** PUSH: place element on top. O(1) amortised. */
    public void push(T value) {
        if (topIndex + 1 == data.length) {
            Object[] bigger = new Object[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, data.length);
            data = bigger;
        }
        data[++topIndex] = value;
    }

    /** POP: remove and return the top element. O(1). Throws on underflow. */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new IllegalStateException("Stack underflow");
        T value = (T) data[topIndex];
        data[topIndex--] = null;        // help garbage collector
        return value;
    }

    /** PEEK: read the top element without removing it. O(1). */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new IllegalStateException("Stack is empty");
        return (T) data[topIndex];
    }
}

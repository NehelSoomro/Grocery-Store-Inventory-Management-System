package inventory;

/** One record of the grocery store inventory. */
public class Product {
    public int id;
    public String name;
    public String category;
    public int quantity;
    public double price;        // price per unit (Rs.)

    public Product(int id, String name, String category, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public Product copy() {
        return new Product(id, name, category, quantity, price);
    }
}

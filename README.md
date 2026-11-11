# Grocery Store Inventory Management System (Stack Based)

**Course:** SW 212 – Data Structures & Algorithms  |  **Open Ended Lab** – Group B (Stack)
**Language:** Java (JDK 8+)  |  Department of Software Engineering, MUET SZAB Campus Khairpur Mir's

## Idea
A desktop application (Java Swing GUI) that manages the products of a grocery store. The whole inventory
lives inside a **hand-written Stack** (no `java.util.Stack`). The newest product is always
on top (LIFO).

## Project structure
```
src/inventory/Stack.java      generic array-based stack: push, pop, peek, isEmpty, size
src/inventory/Product.java    product record (id, name, category, quantity, price)
src/inventory/Action.java     reversible action stored for Undo
src/inventory/Inventory.java  all inventory operations built on stacks
src/inventory/Main.java       launches the GUI
src/inventory/MainFrame.java  Swing window (table, form, buttons, stats, log)
src/inventory/StackView.java  draws the live stack (boxes, TOP / BOTTOM)
src/inventory/ConsoleMain.java  optional text-menu version of the same system
StackInventory.jar            ready-to-run program (double-click)
data/inventory.txt            sample data, loaded at start and saved on exit
build.sh / build.bat          compile & run
```

## Run
Easiest: **double-click `StackInventory.jar`** (or `java -jar StackInventory.jar`).

Or compile from source:
```
./build.sh          (Linux / macOS)
build.bat           (Windows)
```
or manually:
```
mkdir out
javac -d out src/inventory/*.java
java -cp out inventory.Main          (GUI)
java -cp out inventory.ConsoleMain   (text menu version)
```
Data is stored in the `data/` folder next to the jar and saved automatically after every change.

## Using the GUI
* **Stack visualization** (left) – live picture of the stack, TOP product in green, low-stock in red.
* **Products table** (centre) – top of stack first, with search box.
* **Product form + buttons** (right) – the Product ID is generated automatically (next free number), you only type name, category, quantity and price: PUSH (Add), UPDATE, DELETE by ID, POP Top, PEEK Top, UNDO, Clear Form, Low Stock.
  Click a table row to load it into the form.
* **Operations log** (bottom) – shows every internal push/pop, ideal for the video demo.

## CRUD using Stack operations
| CRUD | Menu | How the stack is used |
|------|------|-----------------------|
| Create | PUSH button | `push()` new product on top (duplicate ID check first) |
| Read | table, PEEK, search | display = pop all to temp stack then push back; `peek()` shows top; search digs with temp stack |
| Update | UPDATE button | pop until target is on top, modify it, push back, restore popped items |
| Delete | DELETE / POP Top | DELETE: pop until target found, discard it, restore others. POP Top: plain `pop()` of the top product |

### Extra features
* **Undo** – a second stack (`history`) stores every change; undo pops the last action
  and reverses it (a deleted product returns to its exact old position).
* **Operations log** – shows every internal push/pop so the stack behaviour is visible
  (great for the video and report screenshots).
* **Low stock report**, live statistics cards, automatic file save/load.

## Complexity
| Operation | Time |
|-----------|------|
| push / pop / peek | O(1) |
| display / search / update / delete by ID | O(n) |
| undo | O(1) for add, O(n) for delete/update |

## Why Stack?
A stack gives LIFO access: the last product added is the first one reachable. Accessing
any other product requires popping the ones above it and pushing them back – exactly what
update/delete do here, which demonstrates the property of the data structure.

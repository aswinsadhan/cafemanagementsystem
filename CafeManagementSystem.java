import java.sql.*;
import java.util.Scanner;

public class CafeManagementSystem {
    static Connection conn;
    static Statement stmt;
    static ResultSet rs;

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cafe?characterEncoding=utf-8", "root", "");
            stmt = conn.createStatement();

            createTables(conn); // Call method to create necessary tables

            Scanner scanner = new Scanner(System.in);
            int choice;
            do {
                System.out.println("\nMenu:");
                System.out.println("1. View Menu");
                System.out.println("2. Add Menu Item");
                System.out.println("3. Delete Menu Item");
                System.out.println("4. Record Item Sold Today");
                System.out.println("5. View Items Sold Today");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        displayMenu(conn);
                        break;
                    case 2:
                        addMenuItem(conn, scanner);
                        break;
                    case 3:
                        delMenuItem(conn, scanner);
                        break;
                    case 4:
                        recordSale(conn, scanner);
                        break;
                    case 5:
                        displayItemsSoldToday(conn);
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            } while (choice != 6);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String createMenuTableSQL = "CREATE TABLE IF NOT EXISTS menu (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "description VARCHAR(255)," +
                "price DOUBLE NOT NULL)";

        String createSalesTableSQL = "CREATE TABLE IF NOT EXISTS sales (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "item_id INT NOT NULL," +
                "quantity INT NOT NULL," +
                "sale_date DATE NOT NULL," +
                "FOREIGN KEY (item_id) REFERENCES menu(id))";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createMenuTableSQL);
            stmt.execute(createSalesTableSQL);
            System.out.println("Menu and Sales tables created.");
        }
    }

    private static void addMenuItem(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nAdding a new menu item:");
        System.out.print("Enter item name: ");
        String name = scanner.next();
        System.out.print("Enter item description: ");
        String description = scanner.next();
        System.out.print("Enter item price: ");
        double price = scanner.nextDouble();

        String insertMenuItemSQL = "INSERT INTO menu (name, description, price) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertMenuItemSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
            System.out.println("Menu item added successfully.");
        }
    }

    private static void delMenuItem(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nDeleting a menu item:");
        System.out.print("Enter item name: ");
        String name = scanner.next();

        String delMenuItemSQL = "DELETE FROM menu WHERE name=?";

        try (PreparedStatement pstmt = conn.prepareStatement(delMenuItemSQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Menu item deleted successfully.");
        }
    }

    private static void recordSale(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("\nRecording item sold today:");
        displayMenu(conn);
        System.out.print("Enter the ID of the item sold: ");
        int itemId = scanner.nextInt();
        System.out.print("Enter the quantity sold: ");
        int quantity = scanner.nextInt();

        // Get today's date
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        String recordSaleSQL = "INSERT INTO sales (item_id, quantity, sale_date) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(recordSaleSQL)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, quantity);
            pstmt.setDate(3, today);
            pstmt.executeUpdate();
            System.out.println("Sale recorded successfully.");
        }
    }

    private static void displayMenu(Connection conn) throws SQLException {
        String selectMenuSQL = "SELECT * FROM menu";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectMenuSQL)) {

            System.out.println("\nMenu:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                System.out.println(id + ". " + name + " - " + description + " - $" + price);
            }
        }
    }

    private static void displayItemsSoldToday(Connection conn) throws SQLException {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        String selectItemsSoldTodaySQL = "SELECT m.name, s.quantity FROM menu m " +
                                         "INNER JOIN sales s ON m.id = s.item_id " +
                                         "WHERE s.sale_date = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(selectItemsSoldTodaySQL)) {
            pstmt.setDate(1, today);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nItems sold today:");
            while (rs.next()) {
                String itemName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                System.out.println(itemName + " - Quantity: " + quantity);
            }
        }
    }
}

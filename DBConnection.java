import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/complaint_portal",
                "root",
                "Harini@123"
            );

            System.out.println("Connected to MySQL successfully!");
            return con;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

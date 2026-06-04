import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Koneksi {
    private static Connection mysqlconfig;
    
    public static Connection configDB() throws SQLException {
        try {
            String url = "jdbc:mysql://localhost:3306/db_fragrance"; 
            String user = "root"; 
            String pass = "";
            
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            mysqlconfig = DriverManager.getConnection(url, user, pass);
            
            System.out.println("Koneksi Berhasil!"); 
            
        } catch (Exception e) {
            System.err.println("Koneksi Gagal: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Koneksi Database Gagal!\nPastikan XAMPP (MySQL) sudah menyala.\nError: " + e.getMessage());
        }
        return mysqlconfig;
    }
}
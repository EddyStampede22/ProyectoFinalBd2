import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PruebaMYSQL {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/videogame_collection"; // testdb es el nombre de tu base de datos
        String user = "root";
        String password = "caracalrooter123"; // vacío por defecto en XAMPP

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión exitosa a la base de datos.");
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }
}
import java.sql.*;
import java.util.Scanner;

public class PruebaMYSQL {

    static Connection conexion = null;
    public static void main(String[] args) {
        try {
            ConectarABaseD();
            if (conexion!=null) {
                //AgregarUsuario();
                AgregarPlataforma();
            }
            try {
                if (conexion != null){
                    conexion.close();
                }
            } catch (SQLException e) {
                System.out.println("Acceso no autorizado");
            }
        } catch (Exception s) {
            System.err.println("Error al insertar: " + s.getMessage());
        }
    }
    public static void ConectarABaseD() {
        String url = "jdbc:mysql://localhost:3306/videogame_collection";
        String mysqlUser = "root";
        String mysqlPassword = "caracalrooter123";
        try {
            conexion = DriverManager.getConnection(url, mysqlUser,mysqlPassword);
        } catch (SQLException s){
            System.out.println("URL:"+url);
            s.printStackTrace();
        }

    }
    public static void AgregarUsuario() {
        System.out.println("***Agregar nuevo usuario ***");
        Scanner scan = new Scanner(System.in);
        System.out.print("Username:");
        String userName = scan.nextLine();
        System.out.print("Password:");
        String password = scan.nextLine();
        System.out.print("Email:");
        String email = scan.nextLine();
        despliegaTabla("platform","platform_id,platform_name","1");
        System.out.print("Prefered platform: ");
        int prefedPlatform = scan.nextInt();
        scan.nextLine();
        System.out.print("Access Type: ");
        String accessType = scan.nextLine();
        try {
            String sql = "INSERT INTO users (username, password,email,preferred_platform_id,access_type) VALUES (?,SHA2(?,256),?,?,?)";
            PreparedStatement statement = conexion.prepareStatement(sql);
            System.out.println(statement.toString());
            statement.setString(1, userName);
            statement.setString(2, password);
            statement.setString(3, email);
            statement.setInt(4, prefedPlatform);
            statement.setString(5, accessType);

            //System.out.println("-> URL BD: " + conexion.getMetaData().getURL());
            //System.out.println("-> Autocommit: " + conexion.getAutoCommit());
            //System.out.println("-> SQL con parámetros: " + statement);

            System.out.println(statement.toString());
            statement.executeUpdate();
            if (!conexion.getAutoCommit()) {
                conexion.commit();
                System.out.println("-> Commit manual realizado");
            }
        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }
    }
    public static void AgregarPlataforma(){
        System.out.println("***Agregar plataforma ***");
        Scanner scan = new Scanner(System.in);
        System.out.print("Plataforma: ");
        String plataforma = scan.nextLine();
        try {
            String sql = "INSERT INTO platform (platform_name) VALUES (?)";
            PreparedStatement statement = conexion.prepareStatement(sql);
            System.out.println(statement.toString());
            statement.setString(1, plataforma);

            //System.out.println("-> URL BD: " + conexion.getMetaData().getURL());
            //System.out.println("-> Autocommit: " + conexion.getAutoCommit());
            //System.out.println("-> SQL con parámetros: " + statement);

            System.out.println(statement.toString());
            statement.executeUpdate();
            if (!conexion.getAutoCommit()) {
                conexion.commit();
                System.out.println("-> Commit manual realizado");
            }
        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }

    }
    public static void despliegaResultados(ResultSet resultados, String tabla) {
        System.out.println("Tabla:"+tabla);
        try {
            //Mostrado columnas
            ResultSetMetaData metaDatos = resultados.getMetaData();
            int columnas = metaDatos.getColumnCount();
            for (int i = 1; i <= columnas; i++) {
                System.out.print("\t," + metaDatos.getColumnName(i));
            }
            System.out.println("");
            //Mostramos los registros
            while(resultados.next()) {
                for(int i = 1; i <= columnas; i++) {
                    System.out.print("\t" + resultados.getObject(i));
                }
                System.out.println("");
            }
            System.out.println("");
        }  catch (SQLException s){
            s.printStackTrace();
        }
    }
    public static ResultSet transactionSelect(PreparedStatement inst){
        ResultSet result = null;
        try {
            //System.out.println("SQL:"+inst.toString());
            result = inst.executeQuery();
        } catch (SQLException s){
            s.printStackTrace();
        }
        return result;
    }
    public static void despliegaTabla(String tabla, String columna,String condicion) {
        try {
            if (conexion!=null) {
                conexion.setAutoCommit(false);
                //Preparamos SQL
                if (condicion.length()==0) {
                    condicion = "1";
                }
                String SQL = "SELECT $campo FROM $tableName WHERE $cond";
                String query = SQL.replace("$tableName", tabla);
                query = query.replace("$campo",columna);
                query = query.replace("$cond", condicion);
                PreparedStatement instruccionP = conexion.prepareStatement(query);
                ResultSet resultados = transactionSelect(instruccionP);
                conexion.commit(); // Comprometemos transaccion
                despliegaResultados(resultados,tabla);
            }
        }   catch (SQLException s){
            s.printStackTrace();
            try {
                if (conexion != null) {
                    conexion.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}

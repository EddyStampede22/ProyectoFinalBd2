import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
class menuItem {
    Boolean admin;
    int level;
    String menu;
    String menu_text;
    public menuItem(Boolean admin, int level, String menu, String menu_text) {
        this.admin = admin;
        this.level = level;
        this.menu = menu;
        this.menu_text = menu_text;
    }
}

public class PruebaAccesoMYSQL {
    static Connection conexion = null;
    static int userID = 0;
    static boolean admin = false;
    static HashMap<Integer,menuItem> opcionesMenu;
    static String ip = "10.10.219.191";
    static int usuarioID = 0; // Para actualizar usuarios
    public static void main(String[] args) {
        try{
            String t,c,w;
            //String t = "users";
            //String c = "username,conteo_rankings,promedio_rankings";
            //String w = "id="+userID;
            ConectarABaseD(ip);
            login();
            if (conexion != null) {
                cargarMenu();
                menuPrograma();
            }
            //if (admin == true) {

            //t = "`rankings` as A, disney as B ";
            //c = "A.id, A.id_show, B.title, A.rating, A.comments ";
            //w = "A.id_show = B.show_id "
            //    .concat("AND A.id_user =")
            //    .concat(Integer.toString(userID))
            //    .concat(" ORDER BY title ASC");
            //} else {
            //t = "`rankings` as A, disney as B ";
            //c = "A.id, A.id_show, B.title, A.rating ";
            //w = "A.id_show = B.show_id "
            //    .concat("AND A.id_user =")
            //    .concat(Integer.toString(userID))
            //    .concat(" ORDER BY title ASC");
            //}
            //despliegaTabla(t,c,w);
            //despliegaTabla(conexion,"rankings","*");
            //transactionInsert("s84", 4,"si me gusto");
            //capturaRanking();
            //despliegaTabla(t,c,w);
            try {
                if (conexion != null)
                    conexion.close();
            } catch (SQLException e) {
                System.out.println("Acceso no autorizado");
            }
        }  catch (Exception s){
            s.printStackTrace();
        }
    }
    public static void cargarMenu() {
        String SQL = "SELECT * FROM `menu_fcg` ";
        try {
            PreparedStatement instruccion = conexion.prepareStatement(SQL);
            ResultSet menu = transactionSelect(instruccion);
            HashMap<Integer,menuItem> menuOpcion = new HashMap<>();
            while (menu.next()) {
                Boolean tipoUsuario = menu.getBoolean("admin");
                int menuLevel = menu.getInt("level");
                String menuOpt = menu.getString("menu");
                String menuText= menu.getString("menu_text");
                menuItem m = new menuItem(tipoUsuario,menuLevel, menuOpt,menuText);
                menuOpcion.put(menuLevel, m);
            }
            opcionesMenu = menuOpcion;
        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }
    }
    public static int desplegarMenu(String tipo, int nivel) {
        int opcion = 0;
        Scanner scan = new Scanner(System.in);
        System.out.println("**** MENU " + tipo + " ****");
        Iterator<Map.Entry<Integer,menuItem>> iterador = opcionesMenu.entrySet().iterator();
        while ( iterador.hasNext()) {
            Map.Entry<Integer,menuItem> menu = iterador.next();
            menuItem item = menu.getValue();
            if (tipo.equals("Principal")) {
                if ((item.level % 10) == 0)
                    System.out.println(item.level+": "+item.menu_text);
            } else {
                if ((item.level >=nivel) && (item.level <= nivel+10)) {
                    if ((item.level % 10) != 0)
                        if (item.admin == true && item.level >=30)
                            System.out.println(item.level+": "+item.menu_text);
                        else if (item.admin == false && item.level <30)
                            System.out.println(item.level+": "+item.menu_text);
                }
            }
        }
        if (tipo.equals("Principal"))
            System.out.println("0. Salir");
        else
            System.out.println("9. Regresar");
        System.out.print("Seleccione su opcion :> ");
        opcion = scan.nextInt();
        return opcion;
    }
    public static void menuPrograma() {
        int op = 0;
        String tipo = "Principal";
        String t,c,w;
        int nivel = 0;
        do {
            op = desplegarMenu(tipo,nivel);
            switch (op){
                case  9:
                    tipo = "Principal";
                    break;
                case 10:
                    tipo = "Secundario";
                    nivel = 10;
                    break;
                case 11:
                    capturaRanking();
                    break;
                case 12:
                    modificaRanking();
                    break;
                case 13:
                    eliminaRanking();
                    break;
                case 20:
                    tipo = "Secundario";
                    nivel = 20;
                    break;
                case 21:
                    if (admin) {
                        t = "topten_admin_fcg";
                    } else {
                        t = "topten_user_fcg";
                    }
                    c = "* ";
                    w = "id_user = " + userID + " \n"
                            .concat("ORDER BY rating DESC, title ASC \n")
                            .concat("LIMIT 0,10");
                    despliegaTabla(t,c,w);
                    break;
                case 22:
                    if (admin) {
                        t = "topten_admin_fcg";
                    } else {
                        t = "topten_user_fcg";
                    }
                    c = "* ";
                    w = "id_user = " + userID + " \n"
                            .concat("ORDER BY rating ASC, title ASC \n")
                            .concat("LIMIT 0,10");
                    despliegaTabla(t,c,w);
                    break;
                case 23:
                    if (admin) {
                        t = "topten_admin_fcg";
                    } else {
                        t = "topten_user_fcg";
                    }
                    c = "* ";
                    w = "id_user = " + userID + " \n"
                            .concat("ORDER BY title ASC \n");
                    despliegaTabla(t,c,w);
                    break;
                case 30:
                    tipo = "Secundario";
                    nivel = 30;
                    break;
                case 31:
                    agregaUsuario();
                    break;
                case 32:
                    modificaUsuario();
                    break;
                case 33:
                    eliminaUsuario();
                    break;
                case 34:
                    modificaPassword();
                    break;
            }

        } while (op!= 0);
    }
    public static void ConectarABaseD(String ip) {

        String URL = "jdbc:mysql://" + ip
                + "/disney?useSSL=false&useTimezone=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try {
            Scanner scan = new Scanner(System.in);
            String mysqlUser     = "disney";
            String mysqlPassword = "Ma58toAa!YLtT9S9";
            conexion = DriverManager.getConnection(URL, mysqlUser,mysqlPassword);


        } catch (SQLException s){
            System.out.println("URL:"+URL);
            s.printStackTrace();
        }

    }
    public static void login() {
        try {
            // Login
            Scanner scan = new Scanner(System.in);
            int intentos = 3;
            boolean acceso = false;
            while (intentos > 0 && acceso==false) {
                System.out.println("Tienes "+intentos+" intentos");
                System.out.print("username:");
                String uName = scan.nextLine();

                System.out.print("Password:");
                String pwd = scan.nextLine();

                String SQL = "SELECT id,admin FROM users "
                        .concat("WHERE username = ? ")
                        .concat("AND SHA2(?,256) = password");
                PreparedStatement login = conexion.prepareStatement(SQL);
                login.setString(1, uName);
                login.setString(2, pwd);
                ResultSet authorized = login.executeQuery();
                if (authorized.next() == true) {
                    userID = authorized.getInt("id");
                    admin  = authorized.getBoolean("admin");
                    acceso = true;
                    System.out.println("\n**ACCESO CONCEDIDO**");
                } else {
                    System.out.println("\n**ACCESO DENEGADO**");
                    intentos -= 1;
                }
            }
            if (intentos <= 0) {
                conexion.close();
                conexion = null;
            }
        } catch (SQLException s) {
            System.out.println(s.getMessage());
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
    public static void despliegaResultados(ResultSet resultados,String tabla) {
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
    public static void transactionInsert( String showID, int rating,
                                          String comments) {
        try {
            // Select
            String SQL = "SELECT count(*) as conteo "
                    .concat("FROM rankings ")
                    .concat("WHERE id_user=?");
            PreparedStatement selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet inicial = selectStmt.executeQuery();
            int conteo_inicial  = 0;
            while (inicial.next()) {
                conteo_inicial = inicial.getInt("conteo");
            }

            // Insert
            SQL = "INSERT INTO `rankings`(`id_user`, `id_show`, `rating`, `comments`) \n" +
                    "VALUES (?,?,?,?);";
            PreparedStatement insertStmt = conexion.prepareStatement(SQL);
            insertStmt.setInt(1, userID);
            insertStmt.setString(2, showID);
            insertStmt.setInt(3,rating);
            insertStmt.setString(4,comments);
            System.out.println(insertStmt.toString());
            insertStmt.executeUpdate();
            //Update
            SQL = "UPDATE users AS `dest`,"
                    .concat("(SELECT id_user, count(*) as conteo, AVG(rating) as promedio ")
                    .concat("FROM `rankings` ")
                    .concat("WHERE id_user=? ")
                    .concat("GROUP BY id_user) AS `source` ")
                    .concat("SET ")
                    .concat("   `dest`.`conteo_rankings` = `source`.`conteo`,")
                    .concat("   `dest`.`promedio_rankings` =`source`.`promedio` ")
                    .concat("WHERE ")
                    .concat("`dest`.`id` = `source`.`id_user`");
            PreparedStatement updateSt = conexion.prepareStatement(SQL);
            updateSt.setInt(1, userID);
            System.out.println(updateSt.toString());
            updateSt.executeUpdate();
            // Select final
            SQL = "SELECT count(*) as conteo "
                    .concat("FROM rankings ")
                    .concat("WHERE id_user=?");
            selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet rsf = selectStmt.executeQuery();
            int conteo_final  = 0;
            while (rsf.next()) {
                conteo_final = rsf.getInt("conteo");
            }
            // Comparación
            if ((conteo_inicial+1) == conteo_final)
            { conexion.commit(); }
            else
            { conexion.rollback(); }

        } catch (SQLException s) {
            s.printStackTrace();
        }
        /*
        UPDATE users AS `dest`,
        (SELECT id_user, count(*) as conteo, AVG(rating) as promedio
        FROM `rankings`
        WHERE id_user=2
        GROUP BY id_user) AS `source`
        SET
           `dest`.`conteo_rankings` = `source`.`conteo`,
           `dest`.`promedio_rankings` =`source`.`promedio`
        WHERE
           `dest`.`id` = `source`.`id_user`
        */
    }
    public static void capturaRanking() {
        System.out.println("Captura de ranking/rating");
        System.out.println("Shows o películas a rankear");
        Scanner scan = new Scanner(System.in);
        System.out.print("Escribe una palabra a buscar en los titulos:");
        String cadena = scan.nextLine();
        if (cadena.length()>0) {
            String campos = "disney.show_id, "
                    + "disney.type, disney.title, "
                    + "disney.release_year, disney.listed_in";
            String tabla = "disney";
            String cond  = "title LIKE '%$palabra%' ORDER BY title ASC LIMIT 0,10";
            cond = cond.replace("$palabra",cadena);
            despliegaTabla(tabla,campos,cond);


            System.out.print("ID del show a rankear/ratear:");
            String showID = scan.next();

            System.out.print("Rating:");
            int rating =  scan.nextInt();
            System.out.print("Comentario:");
            String comentario = scan.next();
            transactionInsert(showID,rating,comentario);
        }

    }
    public static void modificaRanking() {
        System.out.println("Modifica ranking/rating");
        System.out.println("Shows o películas a rankear");
        Scanner scan = new Scanner(System.in);
        //System.out.print("Escribe una palabra a buscar en los titulos:");
        //String cadena = scan.nextLine();
        //if (cadena.length()>0) {
        String campos = "rankings.id, rankings.id_user, "
                + "disney.title, rankings.rating, rankings.comments";
        String tabla = "rankings, disney";
        String cond  =  "id_user = $usuario \n" +
                "AND rankings.id_show = disney.show_id";
        cond = cond.replace("$usuario",String.format("%d", userID));
        System.out.println(cond);
        despliegaTabla(tabla,campos,cond);


        System.out.print("ID del show a modificar:");
        int showID = scan.nextInt();

        System.out.print("Rating:");
        int rating =  scan.nextInt();
        System.out.print("Comentario:");
        String comentario = scan.next();
        transactionUpdate(showID,rating,comentario);
        //}
    }
    public static void transactionUpdate( int showID, int rating,
                                          String comments) {
        try {
            int rating_anterior = 0;
            String comentario_anterior = "";
            // Select
            String SQL = "SELECT rankings.rating, rankings.comments  "
                    .concat("FROM rankings ")
                    .concat("WHERE id=?");
            PreparedStatement selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, showID);
            ResultSet inicial = selectStmt.executeQuery();
            int conteo_inicial  = 0;
            while (inicial.next()) {
                rating_anterior = inicial.getInt("rating");
                comentario_anterior = inicial.getString("comments");
            }

            // UPDATE
            SQL = "UPDATE `rankings` "
                    + "SET `rating`=?,`comments`=? "
                    + "WHERE id = ?";
            PreparedStatement insertStmt = conexion.prepareStatement(SQL);
            insertStmt.setInt(1, rating);
            insertStmt.setString(2, comments);
            insertStmt.setInt(3,showID);
            System.out.println(insertStmt.toString());
            insertStmt.executeUpdate();
            //Update
            SQL = "UPDATE users AS `dest`,"
                    .concat("(SELECT id_user, count(*) as conteo, AVG(rating) as promedio ")
                    .concat("FROM `rankings` ")
                    .concat("WHERE id_user=? ")
                    .concat("GROUP BY id_user) AS `source` ")
                    .concat("SET ")
                    .concat("   `dest`.`conteo_rankings` = `source`.`conteo`,")
                    .concat("   `dest`.`promedio_rankings` =`source`.`promedio` ")
                    .concat("WHERE ")
                    .concat("`dest`.`id` = `source`.`id_user`");
            PreparedStatement updateSt = conexion.prepareStatement(SQL);
            updateSt.setInt(1, userID);
            System.out.println(updateSt.toString());
            updateSt.executeUpdate();
            // Select final
            SQL = "SELECT rankings.rating, rankings.comments  "
                    .concat("FROM rankings ")
                    .concat("WHERE id=?");
            selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, showID);
            ResultSet rsf = selectStmt.executeQuery();
            int rating_final  = 0;
            while (rsf.next()) {
                rating_final = rsf.getInt("rating");
            }
            // Comparación
            if ((rating_anterior) != rating_final)
            { conexion.commit(); }
            else
            { conexion.rollback(); }

        } catch (SQLException s) {
            s.printStackTrace();
        }
        /*
        UPDATE users AS `dest`,
        (SELECT id_user, count(*) as conteo, AVG(rating) as promedio
        FROM `rankings`
        WHERE id_user=2
        GROUP BY id_user) AS `source`
        SET
           `dest`.`conteo_rankings` = `source`.`conteo`,
           `dest`.`promedio_rankings` =`source`.`promedio`
        WHERE
           `dest`.`id` = `source`.`id_user`
        */
    }
    public static void eliminaRanking() {
        System.out.println("Elimina ranking/rating");
        System.out.println("Shows o películas a eliminar");
        Scanner scan = new Scanner(System.in);
        String campos = "rankings.id, rankings.id_user, "
                + "disney.title, rankings.rating, rankings.comments";
        String tabla = "rankings, disney";
        String cond  =  "id_user = $usuario \n" +
                "AND rankings.id_show = disney.show_id";
        cond = cond.replace("$usuario",String.format("%d", userID));
        System.out.println(cond);
        despliegaTabla(tabla,campos,cond);

        System.out.print("id del show a eliminar:");
        int showID = scan.nextInt();
        System.out.print("¿Eliminamos el id "+ showID + "? (si/no)");
        String confirmar =  scan.next();
        if (confirmar.toLowerCase().equals("si")== true)
            if (transactionDelete(showID)==true) {
                System.out.println("Registro "+showID+" eliminado");
            } else {
                System.out.println("Falla al intentar borrar registro" + showID);
            }
        else {
            System.out.println("El registro NO se ha eliminado");
        }
    }
    public static boolean transactionDelete( int idRanking) {
        boolean exito = false;
        try {

            // Select
            String SQL = "SELECT count(*) as conteo "
                    .concat("FROM rankings ")
                    .concat("WHERE id_user=?");
            PreparedStatement selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet inicial = selectStmt.executeQuery();
            int conteo_inicial  = 0;
            while (inicial.next()) {
                conteo_inicial = inicial.getInt("conteo");
            }

            // InsertDELETE FROM `rankings` WHERE rankings.id = 62
            SQL = "DELETE FROM `rankings` WHERE rankings.id=? \n" +
                    "AND rankings.id_user = ?";
            PreparedStatement insertStmt = conexion.prepareStatement(SQL);
            insertStmt.setInt(1, idRanking);
            insertStmt.setInt(2, userID);
            System.out.println(insertStmt.toString());
            insertStmt.executeUpdate();
            //Update
            SQL = "UPDATE users AS `dest`,"
                    .concat("(SELECT id_user, count(*) as conteo, AVG(rating) as promedio ")
                    .concat("FROM `rankings` ")
                    .concat("WHERE id_user=? ")
                    .concat("GROUP BY id_user) AS `source` ")
                    .concat("SET ")
                    .concat("   `dest`.`conteo_rankings` = `source`.`conteo`,")
                    .concat("   `dest`.`promedio_rankings` =`source`.`promedio` ")
                    .concat("WHERE ")
                    .concat("`dest`.`id` = `source`.`id_user`");
            PreparedStatement updateSt = conexion.prepareStatement(SQL);
            updateSt.setInt(1, userID);
            System.out.println(updateSt.toString());
            updateSt.executeUpdate();
            // Select final
            SQL = "SELECT count(*) as conteo "
                    .concat("FROM rankings ")
                    .concat("WHERE id_user=?");
            selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet rsf = selectStmt.executeQuery();
            int conteo_final  = 0;
            while (rsf.next()) {
                conteo_final = rsf.getInt("conteo");
            }
            // Comparación
            if ((conteo_inicial-1) == conteo_final)
            { conexion.commit(); exito = true;}
            else
            { conexion.rollback(); }

        } catch (SQLException s) {
            s.printStackTrace();
        }
        return exito;
    }
    public static void agregaUsuario() {
        System.out.println("***Agregar nuevo usuario ***");
        Scanner scan = new Scanner(System.in);
        System.out.print("Username:");
        String userName = scan.nextLine();
        System.out.print("Email:");
        String email = scan.nextLine();
        System.out.print("Password:");
        String passWord = scan.nextLine();
        System.out.print("Admin (true/false):");
        String adminType = scan.nextLine();
        boolean adminTypeBool = Boolean.parseBoolean(adminType);
        System.out.println(adminTypeBool);
        try {
            String SQL = "INSERT INTO users \n" +
                    "(username, email, password,admin,"
                    + "conteo_rankings, promedio_rankings,activo) "
                    + "VALUES (?,?,SHA2(?,256),?,0,0,true)";
            PreparedStatement statement = conexion.prepareStatement(SQL);
            System.out.println(statement.toString());
            statement.setString(1, userName);
            statement.setString(2, email);
            statement.setString(3, passWord);
            statement.setBoolean(4, adminTypeBool);
            System.out.println(statement.toString());
            if (actualizaBaseDatos(statement,"INSERT"))
                System.out.println("Usuario agregado exitosamente");;
        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }
    }
    public static void eliminaUsuario() {
        String t,c,w;
        t = "users";
        c = "*";
        w = "1";
        despliegaTabla(t,c,w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID de usuario a eliminar:");
        int idUsuario = scan.nextInt();
        if (idUsuario >0) {
            try {
                String SQL = "UPDATE users SET activo = 0 "
                        + "WHERE users.id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idUsuario);

                if (actualizaBaseDatos(statement,"DELETE")) {
                    System.out.println("Usuario eliminado exitosamente");
                } else {
                    System.out.println("El usuario no fue eliminado");
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            }
        }

    }
    public static void modificaUsuario() {
        String t,c,w;
        t = "users";
        c = "id, username, email, admin";
        w = "1";
        despliegaTabla(t,c,w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID de usuario a modificar:");
        int idUsuario = scan.nextInt();
        if (idUsuario >0) {
            usuarioID = idUsuario;
            try {
                String SQL = "SELECT username, email, admin "
                        + "FROM users "
                        + "WHERE users.id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idUsuario);
                String user ="";
                String email="";
                boolean admin=false;
                ResultSet rs = statement.executeQuery();
                while(rs.next()) {
                    user = rs.getString("username");
                    email= rs.getString("email");
                    admin= rs.getBoolean("admin");
                }
                System.out.println("username:"+user);
                System.out.println("email:"+email);
                System.out.println("admin:"+admin);
                String newUser, newEmail, newAdmin;
                Scanner sc = new Scanner(System.in);
                System.out.print("Nuevo username:");
                newUser = sc.nextLine();
                System.out.print("Nuevo email:");
                newEmail = sc.nextLine();
                System.out.print("Nuevo admin (true/false:");
                newAdmin = sc.nextLine();
                String campos = "";
                if (newUser.length()>0) {

                    campos = "username = '$user' ";
                    campos = campos.replace("$user", newUser);
                    if (newEmail.length()>0) {
                        campos = campos +", email ='$em' ";
                        campos = campos.replace("$em",newEmail);
                    }
                    if (newAdmin.length()>0) {
                        campos = campos + ", admin = $ad ";
                        campos = campos.replace("$ad",newAdmin);
                    }
                } else {
                    if (newEmail.length()>0) {
                        campos = "email = '$em' ";
                        campos = campos.replace("$em",newEmail);
                        if (newAdmin.length()>0) {
                            campos = campos + ", admin = $ad ";
                            campos = campos.replace("$ad",newAdmin);
                        }
                    } else {
                        if (newAdmin.length()>0) {
                            campos = campos + "admin = $ad ";
                            campos = campos.replace("$ad",newAdmin);
                        }
                    }
                }
                System.out.println("longitud de campos:("+campos.length()+ ")|"+campos);
                if (campos.isEmpty() == false) {
                    SQL = "UPDATE users SET "
                            + campos + "WHERE id = ?";
                    statement = conexion.prepareStatement(SQL);
                    statement.setInt(1, idUsuario);

                    if (actualizaBaseDatos(statement,"UPDATE")) {
                        System.out.println("Usuario modificado exitosamente");
                    } else {
                        System.out.println("El usuario no fue modificado");
                    }
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            }
        }

    }
    public static void modificaPassword() {
        if (admin) {
            String t,c,w;
            t = "users";
            c = "id, username, password";
            w = "1";
            despliegaTabla(t,c,w);
            Scanner scan = new Scanner(System.in);
            System.out.print("ID de usuario a modificar:");
            int idUsuario = scan.nextInt();
            if (idUsuario >0) {
                capturaPassword(idUsuario);
            }
        }else{
            capturaPassword(userID);
        }
    }
    public static void capturaPassword(int idDeUsuario){
        usuarioID = idDeUsuario;
        Scanner sc = new Scanner(System.in);
        System.out.print("Nuevo password:");
        String nuevoPassword = sc.nextLine();
        if (nuevoPassword.length()>4){
            System.out.print("Confirmar nuevo password:");
            String confPassword = sc.nextLine();
            if (nuevoPassword.equals(confPassword)) {
                try {
                    String SQL = "UPDATE users\n"
                            +"set password=SHA2(?,256) \n"
                            +"where id=?";
                    PreparedStatement statement =conexion.prepareStatement(SQL);
                    statement.setString(1, nuevoPassword);
                    statement.setInt(2, idDeUsuario);
                    System.out.println(statement.toString());
                    if (actualizaBaseDatos(statement,"UPDATE")) {
                        System.out.println("Password modificado exitosamente");
                    } else {
                        System.out.println("El password no fue modificado");
                    }
                } catch (SQLException s) {
                    System.out.println(s.getMessage());
                }
            }
        }else {
            System.out.println("la contraseña debe contar con más de 4 caracteres!");
        }
    }
    public static boolean actualizaBaseDatos(
            PreparedStatement instruccion, String accion) {
        boolean exito = false;
        try {
            conexion.setAutoCommit(false);
            if (accion.equals("INSERT")) {
                String SQL = "SELECT count(*) as cuenta "
                        + "FROM users ";
                PreparedStatement inst = conexion.prepareStatement(SQL);
                ResultSet inicial = inst.executeQuery();
                int conteo_inicial  = 0;
                while (inicial.next()) {
                    conteo_inicial = inicial.getInt("cuenta");
                }
                // Hacemos el INSERT
                conexion.setAutoCommit(false);
                instruccion.executeUpdate();
                ResultSet rsFinal = inst.executeQuery();
                int conteo_final  = 0;
                while (rsFinal.next()) {
                    conteo_final = rsFinal.getInt("cuenta");
                }
                if (conteo_final == (conteo_inicial + 1)) {
                    conexion.commit();
                    exito = true;
                } else {
                    conexion.rollback();
                }
            }
            if (accion.equals("DELETE")) {
                String SQL = "SELECT count(*) as cuenta "
                        + "FROM users "
                        + "WHERE activo = 0 ";
                PreparedStatement inst = conexion.prepareStatement(SQL);

                ResultSet inicial = inst.executeQuery();
                int conteo_inicial  = 0;
                while (inicial.next()) {
                    conteo_inicial = inicial.getInt("cuenta");
                }
                // Hacemos el UPDATE
                conexion.setAutoCommit(false);
                instruccion.executeUpdate();
                ResultSet rsFinal = inst.executeQuery();
                int conteo_final  = 0;
                while (rsFinal.next()) {
                    conteo_final = rsFinal.getInt("cuenta");
                }
                if (conteo_final == (conteo_inicial + 1)) {
                    conexion.commit();
                    exito = true;
                } else {
                    conexion.rollback();
                }
            }
            if (accion.equals("UPDATE")) {
                String SQL = "SELECT username, email, admin, password "
                        + "FROM users "
                        + "WHERE id = ?";
                PreparedStatement inst = conexion.prepareStatement(SQL);
                inst.setInt(1,usuarioID);
                System.out.println(inst.toString());
                ResultSet inicial = inst.executeQuery();
                String username_inicial="";
                String email_inicial="";
                String pwd_inicial="";
                boolean admin_inicial=false;
                while (inicial.next()) {
                    username_inicial = inicial.getString("username");
                    email_inicial = inicial.getString("email");
                    pwd_inicial = inicial.getString("password");
                    admin_inicial = inicial.getBoolean("admin");
                }
                // Ejecutar UPDATE
                instruccion.executeUpdate();
                String username_final="";
                String email_final="";
                String pwd_final="";
                boolean admin_final=false;
                ResultSet rsFinal = inst.executeQuery();
                while (rsFinal.next()) {
                    username_final = rsFinal.getString("username");
                    email_final = rsFinal.getString("email");
                    pwd_final = rsFinal.getString("password");
                    admin_final = rsFinal.getBoolean("admin");
                }
                if (username_inicial.equals(username_final)) {
                    if(email_inicial.equals(email_final)) {
                        if(pwd_inicial.equals(pwd_final)) {
                            if(admin_inicial == admin_final) {
                                conexion.rollback();
                            } else {
                                conexion.commit();
                                exito = true;
                            }
                        } else {
                            conexion.commit();
                            exito = true;
                        }
                    } else {
                        conexion.commit();
                        exito = true;
                    }
                } else {
                    conexion.commit();
                    exito = true;
                }
            }
        } catch (SQLException s) {
            System.out.println("actualizaBaseDatos:" +s.getMessage());
        }
        return exito;
    }
    // hola
}

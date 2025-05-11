import java.sql.*;
import java.util.*;

public class PruebaMYSQL {
    public static String admin="";
    public static int userID;
    static Connection conexion = null;
    public static int userType;
    private Map<Integer, List<MenuItem>> menuItemsByCategory = new HashMap<Integer, List<MenuItem>>();
    public static void main(String[] args) {
        try {
            ConectarABaseD();
            if (conexion!=null) {
                login();
                if (admin.equalsIgnoreCase("user")){
                    userType=1;
                    PruebaMYSQL menuSystem = new PruebaMYSQL(userType);
                    menuSystem.handleUserInput();
                }
                else {
                    userType=0;
                    PruebaMYSQL menuSystem = new PruebaMYSQL(userType);
                    menuSystem.handleUserInput();
                }
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
    // Funciones de conexión y login para la base de datos
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

                String SQL = "SELECT user_id,access_type FROM users "
                        .concat("WHERE username = ? ")
                        .concat("AND SHA2(?,256) = password");
                PreparedStatement login = conexion.prepareStatement(SQL);
                login.setString(1, uName);
                login.setString(2, pwd);
                ResultSet authorized = login.executeQuery();
                if (authorized.next()) {
                    userID = authorized.getInt("user_id");
                    admin  = authorized.getString("access_type");
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

    // Clase interna para representar una opción de menú
    public static class MenuItem {
        private int level;
        private String menu;
        private String menuText;

        public MenuItem(int level, String menu, String menuText) {
            this.level = level;
            this.menu = menu;
            this.menuText = menuText;
        }

        public int getLevel() {
            return level;
        }

        public String getMenu() {
            return menu;
        }

        public String getMenuText() {
            return menuText;
        }
    }
    // Constructor para la interfaz en consola
    public PruebaMYSQL(int userType) {
        loadMenuItems(userType);
    }

    // Funciones para el funciomamiento de la interfaz en consola (
    private void loadMenuItems(int userType) {
        try {
            String query = "SELECT level, menu, menu_text FROM menu WHERE admin = ? ORDER BY level";
            try (PreparedStatement stmt = conexion.prepareStatement(query)) {
                stmt.setInt(1, userType);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int level = rs.getInt("level");
                        String menu = rs.getString("menu");
                        String menuText = rs.getString("menu_text");

                        // Obtener la categoría (decena del nivel)
                        int category = level / 10;

                        // Agregar al mapa de categorías
                        menuItemsByCategory.computeIfAbsent(category, k -> new ArrayList<>())
                                .add(new MenuItem(level, menu, menuText));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar el menú: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Metodo para mostrar el menú principal (categorías)
    public void displayMenu() {
        System.out.println("\n========== SISTEMA DE MENÚ ==========\n");
        System.out.println("Seleccione una categoría:");

        // Iterar por las categorías de forma ordenada
        List<Integer> sortedCategories = new ArrayList<Integer>(menuItemsByCategory.keySet());
        Collections.sort(sortedCategories);

        int index = 1;
        for (Integer category : sortedCategories) {
            // Encontrar el título de la categoría (si existe)
            String categoryTitle = findCategoryTitle(category);
            if (categoryTitle != null) {
                System.out.println(index + ". " + categoryTitle);
            } else {
                System.out.println(index + ". Categoría " + category);
            }
            index++;
        }

        System.out.println("\n0. Salir");
        System.out.println("\n====================================");
    }
    // Metodo para mostrar las opciones de una categoría específica
    public void displayCategoryOptions(int categoryIndex) {
        List<Integer> sortedCategories = new ArrayList<Integer>(menuItemsByCategory.keySet());
        Collections.sort(sortedCategories);

        if (categoryIndex < 1 || categoryIndex > sortedCategories.size()) {
            System.out.println("Categoría no válida.");
            return;
        }

        int category = sortedCategories.get(categoryIndex - 1);
        String categoryTitle = findCategoryTitle(category);

        System.out.println("\n========== " + (categoryTitle != null ? categoryTitle : "Categoría " + category) + " ==========\n");

        List<MenuItem> items = menuItemsByCategory.get(category);
        List<MenuItem> menuOptions = new ArrayList<MenuItem>();

        // Recolectar opciones (sin incluir los títulos de categoría)
        for (MenuItem item : items) {
            if (item.getLevel() % 10 != 0) {
                menuOptions.add(item);
            }
        }

        // Mostrar opciones numeradas
        for (int i = 0; i < menuOptions.size(); i++) {
            MenuItem item = menuOptions.get(i);
            System.out.println((i + 1) + ". " + item.getMenuText());
        }

        System.out.println("\n0. Volver al menú principal");
        System.out.println("\n====================================");

        return;
    }
    // Metodo para encontrar el título de una categoría
    private String findCategoryTitle(int category) {
        List<MenuItem> items = menuItemsByCategory.get(category);
        if (items != null) {
            for (MenuItem item : items) {
                // Los títulos de categoría suelen tener unidades 0
                if (item.getLevel() == category * 10) {
                    return item.getMenuText();
                }
            }
        }
        return null;
    }
    // Metodo para manejar la selección del usuario
    public void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            // Mostrar menú principal (categorías)
            displayMenu();
            System.out.print("\nSeleccione una categoría (0 para salir): ");

            try {
                int categoryChoice = scanner.nextInt();

                if (categoryChoice == 0) {
                    exit = true;
                    System.out.println("¡Hasta pronto!");
                    continue;
                }

                // Obtener la categoría seleccionada
                List<Integer> sortedCategories = new ArrayList<Integer>(menuItemsByCategory.keySet());
                Collections.sort(sortedCategories);

                if (categoryChoice < 1 || categoryChoice > sortedCategories.size()) {
                    System.out.println("Categoría no válida.");
                    pressEnterToContinue(scanner);
                    continue;
                }

                int selectedCategory = sortedCategories.get(categoryChoice - 1);

                // Submenu - Mostrar opciones de la categoría seleccionada
                boolean backToMainMenu = false;
                while (!backToMainMenu) {
                    // Mostrar opciones de la categoría
                    displayCategoryOptions(categoryChoice);

                    System.out.print("\nSeleccione una opción (0 para volver): ");
                    int optionChoice = scanner.nextInt();

                    if (optionChoice == 0) {
                        backToMainMenu = true;
                        continue;
                    }

                    // Obtener las opciones de la categoría seleccionada
                    List<MenuItem> categoryItems = menuItemsByCategory.get(selectedCategory);
                    List<MenuItem> menuOptions = new ArrayList<MenuItem>();

                    // Filtrar solo las opciones (sin incluir títulos de categoría)
                    for (MenuItem item : categoryItems) {
                        if (item.getLevel() % 10 != 0) {
                            menuOptions.add(item);
                        }
                    }

                    if (optionChoice < 1 || optionChoice > menuOptions.size()) {
                        System.out.println("Opción no válida.");
                        pressEnterToContinue(scanner);
                        continue;
                    }

                    // Obtener la opción seleccionada
                    MenuItem selectedItem = menuOptions.get(optionChoice - 1);

                    // Ejecutar la acción correspondiente
                    executeAction(selectedItem.getMenu(), selectedItem);

                    pressEnterToContinue(scanner);
                }

            } catch (InputMismatchException e) {
                System.out.println("Por favor, ingrese un número válido.");
                scanner.nextLine(); // Limpiar el buffer
                pressEnterToContinue(scanner);
            }
        }

        scanner.close();
    }
    // Metodo para ejecutar la acción correspondiente según el código de menú
    private void executeAction(String menuCode, MenuItem menuItem) {
        System.out.println("\n======================================");
        System.out.println("Ejecutando: " + menuItem.getMenuText() + " [" + menuCode + "]");
        System.out.println("======================================");

        // Usar switch para ejecutar la acción correspondiente según el código de menú
        switch (menuCode) {
            // Opciones de Usuario (admin)
            case "ADD_USER":
                AgregarUsuario();
                break;
            case "MOD_USER":
                ModificarUsuario();
                break;
            case "DEL_USER":
                EliminarUsuario();
                break;
            case "MOD_PASSW":
                modificaPassword();
                break;

            // Opciones de Plataforma
            case "ADD_PLAT":
                AgregarPlataforma();
                break;
            case "MOD_PLAT":
                ModificarPlataforma();
                break;
            case "DEL_PLAT":
                EliminarPlataforma();
                break;

            // Opciones de Juego
            case "ADD_GAME":
                AgregarJuego();
                break;
            case "MOD_GAME":
                ModificarJuego();
                break;
            case "DEL_GAME":
                EliminarJuego();
                break;

            // Opciones de Rating
            case "ADD_RATING":
                CapturarRating();
                break;
            case "MOD_RATING":
                ModificarRating();
                break;
            case "DEL_RATING":
                EliminarRating();
                break;
            case "TOP_5_PROM":
                String t,c,w;
                t = "games JOIN platform ON games.platform_id = platform.platform_id ";
                c = "games.game_name, platform.platform_name, games.promedio_rating ";
                w = "games.activo=1 ".concat("\n ORDER BY games.promedio_rating DESC \n ").concat("LIMIT 10");
                despliegaTabla(t,c,w);
                break;

            case "TOP_10":
                String t1,c1,w1;
                t1 = "games JOIN platform ON games.platform_id = platform.platform_id ";
                c1 = "games.game_name, platform.platform_name, games.cantidad_usuarios ";
                w1 = "games.activo=1".concat("\n ORDER BY games.cantidad_usuarios DESC \n ").concat("LIMIT 10");
                despliegaTabla(t1,c1,w1);
                break;
            case "LIST_GAMES":
                String campos = "games.game_name,platform.platform_name, game_collection.rating";
                String tabla = "game_collection, games JOIN platform ON games.platform_id = platform.platform_id ";
                String cond  =  "user_id = $usuario \n" +
                        "AND game_collection.game_id = games.game_id AND games.activo =1";
                cond = cond.replace("$usuario",String.format("%d", userID));
                despliegaTabla(tabla,campos,cond);
               break;

            // Opciones de menú (categorías)
            case "MENU_USER":
            case "MENU_RATING":
                // Estas son opciones de categoría, no requieren acción
                System.out.println("Esta es una categoría de menú, no una acción ejecutable.");
                break;

            default:
                System.out.println("Acción no implementada: " + menuCode);
                break;
        }
    }
    // Metodo auxiliar para esperar que el usuario presione Enter
    private void pressEnterToContinue(Scanner scanner) {
        System.out.println("\nPresione Enter para continuar...");
        scanner.nextLine(); // Consumir la nueva línea pendiente
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Esperar input
        }
    }
    // )

    // Funciones para la gestion de usuarios
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
    public static void EliminarUsuario() {
        String t, c, w;
        t = "users";
        c = "*";
        w = "activo=1";
        despliegaTabla(t, c, w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID de usuario a eliminar:");
        int idUsuario = scan.nextInt();
        if (idUsuario > 0) {
            try {
                String SQL = "UPDATE users SET activo = 0 "
                        + "WHERE users.user_id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idUsuario);

                if (actualizaBaseDatos(statement)) {
                    System.out.println("Usuario eliminado exitosamente");
                } else {
                    System.out.println("El usuario no fue eliminado");
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            }
        }

    }
    public static void ModificarUsuario(){
        String t, c, w;
        t = "users";
        c = "user_id,username,password,email,preferred_platform_id,access_type,activo";
        w = "1";
        despliegaTabla(t, c, w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID de usuario a modificar:");
        int idUsuario = scan.nextInt();
        if (idUsuario >0) {
            try {
                String SQL = "SELECT username,email,preferred_platform_id,access_type,activo "
                        + "FROM users "
                        + "WHERE users.user_id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idUsuario);
                String user ="";
                String email="";
                int preferred_platform = 0;
                String accessType="";
                String active ="";
                ResultSet rs = statement.executeQuery();
                while(rs.next()) {
                    user = rs.getString("username");
                    email= rs.getString("email");
                    preferred_platform = rs.getInt("preferred_platform_id");
                    accessType= rs.getString("access_type");
                    active= rs.getString("activo");
           
                }
                System.out.println("username:"+user);
                System.out.println("email:"+email);
                System.out.println("Preferred platform:"+ preferred_platform);
                System.out.println("access_type:"+accessType);
                System.out.println("activo: "+active);
                String newUser, newEmail,newPreferred,newAcess;
                String newActivo = active;
                Scanner sc = new Scanner(System.in);
                System.out.print("Nuevo username:");
                newUser = sc.nextLine();
                System.out.print("Nuevo email:");
                newEmail = sc.nextLine();
                despliegaTabla("platform","platform_id,platform_name","1");
                System.out.print("Nuevo Preferred platform:");
                newPreferred = sc.nextLine();
                System.out.print("Nuevo access type:");
                newAcess = sc.nextLine();
                if (active.equalsIgnoreCase("0")){
                    System.out.println("¿Desea volver agregar al Usuario?: (si/no)");
                    String respuesta = sc.nextLine();
                    if (respuesta.equalsIgnoreCase("si")) {
                        newActivo = "1";
                    }
                }
                String campos = "";
                if (!newUser.isEmpty()) {
                    campos = "username = '$user' ";
                    campos = campos.replace("$user", newUser);
                    if (!newEmail.isEmpty()) {
                        campos = campos +", email ='$em' ";
                        campos = campos.replace("$em",newEmail);
                    }
                    if (!newPreferred.isEmpty()) {
                        campos = campos + ", preferred_platform_id = $pp ";
                        campos = campos.replace("$pp",newPreferred);
                    }
                    if (!newAcess.isEmpty()) {
                        campos = campos + ", access_type = '$a' ";
                        campos = campos.replace("$a",newAcess);
                    }
                    if (!newActivo.isEmpty()) {
                        campos = campos + ", activo = '$ac' ";
                        campos = campos.replace("$ac",newActivo);
                    }

                } else {
                    if (!newEmail.isEmpty()) {
                        campos = "email = '$em' ";
                        campos = campos.replace("$em",newEmail);
                        if (!newPreferred.isEmpty()) {
                            campos = campos + ", preferred_platform_id = $pp ";
                            campos = campos.replace("$pp",newPreferred);
                        }
                        if (!newAcess.isEmpty()) {
                            campos = campos + ", access_type = '$a' ";
                            campos = campos.replace("$a",newAcess);
                        }
                        if (!newActivo.isEmpty()) {
                            campos = campos + ", activo = '$ac' ";
                            campos = campos.replace("$ac",newActivo);
                        }
                    } else {
                        if (!newPreferred.isEmpty()) {
                            campos = campos + "preferred_platform_id = '$pp'";
                            campos = campos.replace("$pp",newPreferred);

                            if (!newAcess.isEmpty()) {
                                campos = campos + "access_type = '$a' ";
                                campos = campos.replace("$a",newAcess);
                            }
                            if (!newActivo.isEmpty()) {
                                campos = campos + ", activo = '$ac' ";
                                campos = campos.replace("$ac",newActivo);
                            }

                        }else {
                            if (!newAcess.isEmpty()) {
                                campos = campos + "access_type = '$a' ";
                                campos = campos.replace("$a",newAcess);
                                if (!newActivo.isEmpty()) {
                                    campos = campos + ",activo = '$ac' ";
                                    campos = campos.replace("$ac",newActivo);
                                }
                            }else {
                            if (!newActivo.isEmpty()) {
                                campos = campos + "activo = '$ac' ";
                                campos = campos.replace("$ac",newActivo);
                            }
                            }
                        }
                    }
                }
                System.out.println("longitud de campos:("+campos.length()+ ")|"+campos);
                if (!campos.isEmpty()) {
                    SQL = "UPDATE users SET "
                            + campos + "WHERE user_id = ?";
                    statement = conexion.prepareStatement(SQL);
                    statement.setInt(1, idUsuario);

                    if (actualizaBaseDatos(statement)) {
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
        if (admin.equalsIgnoreCase("admin")) {
            String t,c,w;
            t = "users";
            c = "user_id, username, password";
            w = "users.activo=1";
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
                            +"where user_id=?";
                    PreparedStatement statement =conexion.prepareStatement(SQL);
                    statement.setString(1, nuevoPassword);
                    statement.setInt(2, idDeUsuario);
                    System.out.println(statement.toString());
                    if (actualizaBaseDatos(statement)) {
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

    // Funciones para la gestion de plataformas
    public static void AgregarPlataforma(){
        System.out.println("***Agregar plataforma ***");
        String t, c, w;
        t = "platform";
        c = "platform_id,platform_name,activo";
        w = "1";
        despliegaTabla(t, c, w);
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
    public static void EliminarPlataforma(){
        System.out.println("***Eliminar plataforma ***");
        String t, c, w;
        t = "platform";
        c = "platform_id,platform_name";
        w = "activo=1";
        despliegaTabla(t, c, w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID de Plataforma a eliminar:");
        int idPlataforma = scan.nextInt();
        if (idPlataforma > 0) {
            try {
                String SQL = "UPDATE platform SET activo = 0 "
                        + "WHERE platform.platform_id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idPlataforma);

                if (actualizaBaseDatos(statement)) {
                    System.out.println("Plataforma eliminada exitosamente");
                } else {
                    System.out.println("La plataforma no fue eliminado");
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            }
        }

    }
    public static void ModificarPlataforma(){
        System.out.println("***Modificar plataforma ***");
        String t, c, w;
        t = "platform";
        c = "platform_id,platform_name,activo";
        w = "1";
        despliegaTabla(t, c, w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID de Plataforma a modificar:");
        int idPlataforma = scan.nextInt();
        if (idPlataforma > 0) {
            try {
                String SQL = "SELECT platform_name, activo "
                        + "FROM platform "
                        + "WHERE platform.platform_id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idPlataforma);
                String platform = "";
                String activo = "";
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    platform = rs.getString("platform_name");
                    activo = rs.getString("activo");

                }
                System.out.println(statement.toString());
                System.out.println("platform:" + platform);
                System.out.println("activo:" + activo);
                String newPlatform;
                String newActivo = activo;
                Scanner sc = new Scanner(System.in);
                System.out.print("Nueva Plataforma:");
                newPlatform = sc.nextLine();
                if (activo.equalsIgnoreCase("0")){
                System.out.print("¿Deseas volver agregarlo? (si/no) :");
                String respuesta = sc.nextLine();
                if (respuesta.equalsIgnoreCase("si")){
                    newActivo = "1";
                }
                }
                String campos = "";
                if (!newPlatform.isEmpty()) {
                    campos = "platform_name = '$pl' ";
                    campos = campos.replace("$pl", newPlatform);
                    if (!newActivo.isEmpty()) {
                        campos = campos + ", activo ='$a' ";
                        campos = campos.replace("$a",newActivo);
                    }
                }
                else {
                    if (!newActivo.isEmpty()) {
                        campos = "activo = '$a' ";
                        campos = campos.replace("$a", newActivo);
                    }
                }
                System.out.println("longitud de campos:(" + campos.length() + ")|" + campos);
                if (!campos.isEmpty()) {
                    SQL = "UPDATE platform SET "
                            + campos + "WHERE platform_id = ?";
                    statement = conexion.prepareStatement(SQL);
                    statement.setInt(1, idPlataforma);

                    if (actualizaBaseDatos(statement)) {
                        System.out.println("Plataforma modificada exitosamente");
                    } else {
                        System.out.println("La plataforma no fue modificada");
                    }
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            }
        }
    }

    // Funciones para la gestion de juegos
    public static void AgregarJuego(){
        System.out.println("Lista de Juegos Agregados");
        despliegaTabla("games JOIN platform ON games.platform_id = platform.platform_id","game_name,platform.platform_name","1");
        System.out.println("***Agregar nuevo juego ***");
        Scanner scan = new Scanner(System.in);
        System.out.print("Game name:");
        String gameName = scan.nextLine();
        despliegaTabla("platform","platform_id,platform_name","1");
        System.out.print("Platform Id:");
        int platformId = scan.nextInt();
        System.out.print("Year released:");
        int year = scan.nextInt();
        System.out.print("Image Url: ");
        String imageUrl = scan.nextLine();
        try {
            String sql = "INSERT INTO games (game_name, platform_id,year_released,image_url) VALUES (?,?,?,?)";
            PreparedStatement statement = conexion.prepareStatement(sql);
            System.out.println(statement.toString());
            statement.setString(1,gameName);
            statement.setInt(2, platformId);
            statement.setInt(3, year);;
            statement.setString(4, imageUrl);

            System.out.println(statement.toString());
            if (actualizaBaseDatos(statement)) {
                System.out.println("Juego Agregado exitosamente");
            } else {
                System.out.println("El juego no fue agregado");
            }

        } catch (SQLException s) {
            System.out.println(s.getMessage());
        }

    }
    public static void EliminarJuego(){
        System.out.println("***Eliminar juego ***");
        String t, c, w;
        t = "games";
        c = "*";
        w = "activo=1";
        despliegaTabla(t, c, w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID del Juego a eliminar:");
        int idJuego = scan.nextInt();
        if (idJuego > 0) {
            try {
                String SQL = "UPDATE games SET activo = 0 "
                        + "WHERE games.game_id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idJuego);

                if (actualizaBaseDatos(statement)) {
                    System.out.println("Juego eliminado exitosamente");
                } else {
                    System.out.println("El juego no fue eliminado");
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            }
        }
    }
    public static void ModificarJuego(){
        String t, c, w;
        t = "games";
        c = "game_id,game_name,platform_id,year_released,image_url,activo";
        w = "1";
        despliegaTabla(t, c, w);
        Scanner scan = new Scanner(System.in);
        System.out.print("ID de juego a modificar:");
        int idUsuario = scan.nextInt();
        if (idUsuario >0) {
            try {
                String SQL = "SELECT game_name,platform_id,year_released,image_url,activo "
                        + "FROM games "
                        + "WHERE games.game_id = ?";
                PreparedStatement statement = conexion.prepareStatement(SQL);
                statement.setInt(1, idUsuario);
                String game = "";
                int platform = 0;
                int year = 0;
                String image = "";
                String activo ="";
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    game = rs.getString("game_name");
                    platform = rs.getInt("platform_id");
                    year = rs.getInt("year_released");
                    image = rs.getString("image_url");
                    activo=rs.getString("activo");

                }
                System.out.println("game name:" + game);
                System.out.println("platfotm id:" + platform);
                System.out.println("year released:" + year);
                System.out.println("image url:" + image);
                System.out.println("activo:"+ activo);
                String newGame, newPlatform, newYear, newImage;
                String newActivo = activo;
                Scanner sc = new Scanner(System.in);
                System.out.print("Nuevo Juego:");
                newGame = sc.nextLine();
                despliegaTabla("platform", "platform_id,platform_name", "activo=1");
                System.out.print("Nueva Plataforma:");
                newPlatform = sc.nextLine();
                System.out.print("Nuevo Anio lanzamiento:");
                newYear = sc.nextLine();
                System.out.print("Nuevo imagen url:");
                newImage = sc.nextLine();
                if (activo.equalsIgnoreCase("0")){
                System.out.print("Desea volver agregar este juego?: (si/no)");
                String respuesta = sc.nextLine();
                if (respuesta.equalsIgnoreCase("si")) {
                    newActivo = "1";
                }
                }
                String campos = "";
                if (!newGame.isEmpty()) {
                    campos = "game_name = '$game' ";
                    campos = campos.replace("$game", newGame);
                    if (!newPlatform.isEmpty()) {
                        campos = campos + ", platform_id ='$pi' ";
                        campos = campos.replace("$pi", newPlatform);
                    }
                    if (!newYear.isEmpty()) {
                        campos = campos + ", year_released = $yr ";
                        campos = campos.replace("$yr", newYear);
                    }
                    if (!newImage.isEmpty()) {
                        campos = campos + ", image_url = '$i' ";
                        campos = campos.replace("$i", newImage);
                    }
                    if (!newActivo.isEmpty()){
                        campos = campos + ", activo = '$ac' ";
                        campos = campos.replace("$ac", newActivo);
                    }

                } else {
                    if (!newPlatform.isEmpty()) {
                        campos = "platform_id = '$pi' ";
                        campos = campos.replace("$pi", newPlatform);
                        if (!newYear.isEmpty()) {
                            campos = campos + ", year_released = $yr ";
                            campos = campos.replace("$yr", newYear);
                        }
                        if (!newImage.isEmpty()) {
                            campos = campos + ", image_url = '$i' ";
                            campos = campos.replace("$i", newImage);

                        }
                        if (!newActivo.isEmpty()){
                            campos = campos + ", activo = '$ac' ";
                            campos = campos.replace("$ac", newActivo);
                        }
                    } else {
                        if (!newYear.isEmpty()) {
                            campos ="year_released = '$yr'";
                            campos = campos.replace("$yr", newYear);
                            if (!newImage.isEmpty()) {
                                campos = campos + "image_url = '$i' ";
                                campos = campos.replace("$i", newImage);
                            }
                            if (!newActivo.isEmpty()){
                                campos = campos + ", activo = '$ac' ";
                                campos = campos.replace("$ac", newActivo);
                            }

                        }else {
                        if (!newImage.isEmpty()) {
                            campos ="image_url = '$i' ";
                            campos = campos.replace("$i", newImage);
                            if (!newActivo.isEmpty()){
                                campos =campos+ ", activo = '$ac' ";
                                campos = campos.replace("$ac", newActivo);
                            }
                        }else {
                        if (!newActivo.isEmpty()){
                            campos ="activo = '$ac' ";
                            campos = campos.replace("$ac", newActivo);
                        }
                        }
                        }
                    }
                }
                System.out.println("longitud de campos:(" + campos.length() + ")|" + campos);
                if (!campos.isEmpty()) {
                    SQL = "UPDATE games SET "
                            + campos + "WHERE game_id = ?";
                    statement = conexion.prepareStatement(SQL);
                    statement.setInt(1, idUsuario);

                    if (actualizaBaseDatos(statement)) {
                        System.out.println("Juego modificado exitosamente");
                    } else {
                        System.out.println("El juego no fue modificado");
                    }
                }
            } catch (SQLException s) {
                System.out.println(s.getMessage());
            }
        }

    }

    // Funciones para la gestion del Rating del juego
    public static void CapturarRating(){
        System.out.println("***Capturar coleccion ***");
        System.out.println("Juegos a rankear");
        Scanner scan = new Scanner(System.in);
        System.out.print("Escribe una palabra a buscar en los titulos:");
        String cadena = scan.nextLine();
        if (!cadena.isEmpty()) {
            String campos = "games.game_id, "
                    + "games.game_name, platform.platform_name, "
                    + "games.year_released, "+"games.image_url";
            String tabla = "games".concat("\n JOIN platform ON games.platform_id = platform.platform_id ");
            String cond  = "games.activo=1 AND game_name LIKE '%$palabra%' ORDER BY game_name ASC LIMIT 0,10";
            cond = cond.replace("$palabra",cadena);
            despliegaTabla(tabla,campos,cond);


            System.out.print("ID del juego a rankear/ratear:");
            String gameID = scan.next();

            System.out.print("Rating:");
            int rating =  scan.nextInt();
            transactionInsert(gameID,rating);
        }
    }
    public static void ModificarRating(){
        System.out.println("***Modificar coleccion ***");
        System.out.println("Juegos a rankear");
        Scanner scan = new Scanner(System.in);
        String campos = "game_collection.collection_id, game_collection.user_id, "
                + "games.game_name, game_collection.rating";
        String tabla = "game_collection, games";
        String cond  =  "user_id = $usuario \n" +
                "AND game_collection.game_id = games.game_id AND activo =1";
        cond = cond.replace("$usuario",String.format("%d", userID));
        despliegaTabla(tabla,campos,cond);


        System.out.print("ID del juego a modificar:");
        int gameID = scan.nextInt();

        System.out.print("Rating:");
        int rating =  scan.nextInt();
        transactionUpdate(gameID,rating);
        //}
    }
    public static void EliminarRating(){
        System.out.println("***Eliminar coleccion ***");
        System.out.println("Juegos a eliminar");
        Scanner scan = new Scanner(System.in);
        String campos = "game_collection.collection_id, game_collection.user_id, "
                + "games.game_name, game_collection.rating";
        String tabla = "game_collection, games";
        String cond  =  "user_id = $usuario \n" +
                "AND game_collection.game_id = games.game_id AND activo =1";
        cond = cond.replace("$usuario",String.format("%d", userID));
        System.out.println(cond);
        despliegaTabla(tabla,campos,cond);

        System.out.print("id del juego a eliminar:");
        int gameID = scan.nextInt();
        System.out.print("¿Eliminamos el id "+ gameID + "? (si/no)");
        String confirmar =  scan.next();
        if (confirmar.equalsIgnoreCase("si"))
            if (transactionDelete(gameID)) {
                System.out.println("Registro "+gameID+" eliminado");
            } else {
                System.out.println("Falla al intentar borrar registro: " + gameID);
            }
        else {
            System.out.println("El registro NO se ha eliminado");
        }

    }

    // Funciones Transactions
    public static void transactionInsert( String gameID, int rating) {
        try {
            // Select
            String SQL = "SELECT count(*) as conteo "
                    .concat("FROM game_collection ")
                    .concat("WHERE user_id=?");
            PreparedStatement selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet inicial = selectStmt.executeQuery();
            int conteo_inicial  = 0;
            while (inicial.next()) {
                conteo_inicial = inicial.getInt("conteo");
            }

            // Insert
            SQL = "INSERT INTO `game_collection`(`user_id`, `game_id`, `rating`) \n" +
                    "VALUES (?,?,?);";
            PreparedStatement insertStmt = conexion.prepareStatement(SQL);
            insertStmt.setInt(1, userID);
            insertStmt.setString(2, gameID);
            insertStmt.setInt(3,rating);
            System.out.println(insertStmt.toString());
            insertStmt.executeUpdate();
            //Update
            SQL = "UPDATE games AS `dest`,"
                    .concat("(SELECT game_id, count(*) as conteo, AVG(rating) as promedio ")
                    .concat("FROM `game_collection` ")
                    .concat("WHERE game_id=? ")
                    .concat("GROUP BY game_id) AS `source` ")
                    .concat("SET ")
                    .concat("   `dest`.`cantidad_usuarios` = `source`.`conteo`,")
                    .concat("   `dest`.`promedio_rating` =`source`.`promedio` ")
                    .concat("WHERE ")
                    .concat("`dest`.`game_id` = `source`.`game_id`");
            PreparedStatement updateSt = conexion.prepareStatement(SQL);
            updateSt.setString(1, gameID);
            System.out.println(updateSt.toString());
            updateSt.executeUpdate();
            // Select final
            SQL = "SELECT count(*) as conteo "
                    .concat("FROM game_collection ")
                    .concat("WHERE user_id=?");
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

    }
    public static void transactionUpdate( int gameID, int rating) {
        try {
            int rating_anterior = 0;
            // Select
            String SQL = "SELECT game_collection.rating  "
                    .concat("FROM game_collection ")
                    .concat("WHERE user_id=?");
            PreparedStatement selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet inicial = selectStmt.executeQuery();
            while (inicial.next()) {
                rating_anterior = inicial.getInt("rating");
            }

            // UPDATE
            SQL = "UPDATE `game_collection` "
                    + "SET `rating`=? "
                    + "WHERE collection_id = ?";
            PreparedStatement insertStmt = conexion.prepareStatement(SQL);
            insertStmt.setInt(1, rating);
            insertStmt.setInt(2, gameID);
            System.out.println(insertStmt.toString());
            insertStmt.executeUpdate();
            // Obtener el game_id a partir del collection_id
            String getGameID = "SELECT game_id FROM game_collection WHERE collection_id = ?";
            PreparedStatement getGameIDStmt = conexion.prepareStatement(getGameID);
            getGameIDStmt.setInt(1, gameID);
            ResultSet rs = getGameIDStmt.executeQuery();
            int realGameID = -1;
            if (rs.next()) {
                realGameID = rs.getInt("game_id");
            }
            //Update
            SQL = "UPDATE games AS `dest`,"
                    .concat("(SELECT game_id, count(*) as conteo, AVG(rating) as promedio ")
                    .concat("FROM `game_collection` ")
                    .concat("WHERE game_id=? ")
                    .concat("GROUP BY game_id) AS `source` ")
                    .concat("SET ")
                    .concat("   `dest`.`cantidad_usuarios` = `source`.`conteo`,")
                    .concat("   `dest`.`promedio_rating` =`source`.`promedio` ")
                    .concat("WHERE ")
                    .concat("`dest`.`game_id` = `source`.`game_id`");
            PreparedStatement updateSt = conexion.prepareStatement(SQL);
            updateSt.setInt(1, realGameID);
            System.out.println(updateSt.toString());
            updateSt.executeUpdate();
            int updatedRows = updateSt.executeUpdate();
            System.out.println("Filas actualizadas en games: " + updatedRows);
            // Select final
            SQL = "SELECT game_collection.rating  "
                    .concat("FROM game_collection ")
                    .concat("WHERE user_id=?");
            selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
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
    }
    public static boolean transactionDelete(int CollectionID){
        boolean exito = false;
        try {
            // Select
            String SQL = "SELECT count(*) as conteo "
                    .concat("FROM game_collection ")
                    .concat("WHERE user_id=?");
            PreparedStatement selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet inicial = selectStmt.executeQuery();
            int conteo_inicial  = 0;
            while (inicial.next()) {
                conteo_inicial = inicial.getInt("conteo");
            }
            // Obtener el game_id a partir del collection_id
            String getGameID = "SELECT game_id FROM game_collection WHERE collection_id = ?";
            PreparedStatement getGameIDStmt = conexion.prepareStatement(getGameID);
            getGameIDStmt.setInt(1, CollectionID);
            ResultSet rs = getGameIDStmt.executeQuery();
            int realGameID = -1;
            if (rs.next()) {
                realGameID = rs.getInt("game_id");
            }

            // Insert
            SQL = "DELETE FROM `game_collection` WHERE game_collection.collection_id=? \n"+
                    "AND game_collection.user_id = ?";
            PreparedStatement insertStmt = conexion.prepareStatement(SQL);
            insertStmt.setInt(1, CollectionID);
            insertStmt.setInt(2, userID);
            System.out.println(insertStmt.toString());
            insertStmt.executeUpdate();
            //Update
            SQL = "UPDATE games AS `dest`,"
                    .concat("(SELECT game_id, count(*) as conteo, AVG(rating) as promedio ")
                    .concat("FROM `game_collection` ")
                    .concat("WHERE game_id=? ")
                    .concat("GROUP BY game_id) AS `source` ")
                    .concat("SET ")
                    .concat("   `dest`.`cantidad_usuarios` = `source`.`conteo`,")
                    .concat("   `dest`.`promedio_rating` =`source`.`promedio` ")
                    .concat("WHERE ")
                    .concat("`dest`.`game_id` = `source`.`game_id`");
            PreparedStatement updateSt = conexion.prepareStatement(SQL);
            updateSt.setInt(1, realGameID);
            System.out.println(updateSt.toString());
            updateSt.executeUpdate();
            // Select final
            SQL = "SELECT count(*) as conteo "
                    .concat("FROM game_collection ")
                    .concat("WHERE user_id=?");
            selectStmt = conexion.prepareStatement(SQL);
            selectStmt.setInt(1, userID);
            ResultSet rsf = selectStmt.executeQuery();
            int conteo_final  = 0;
            while (rsf.next()) {
                conteo_final = rsf.getInt("conteo");
            }
            // Comparación
            if ((conteo_inicial-1) == conteo_final)
            { conexion.commit();
            exito=true;}
            else
            { conexion.rollback(); }

        } catch (SQLException s) {
            s.printStackTrace();
        }
        return exito;
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

    // Otras funciones
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
    public static boolean actualizaBaseDatos(PreparedStatement instruccion) throws SQLException {
        boolean exito = false;
        // Guarda el estado original de autoCommit
        boolean originalAutoCommit = conexion.getAutoCommit();
        conexion.setAutoCommit(false);
        try {
            int filasAfectadas = instruccion.executeUpdate();
            if (filasAfectadas > 0) {
                conexion.commit();
                exito = true;
            } else {
                conexion.rollback();
            }
        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            // Restaurar autoCommit, para no interferir con otras operaciones
            conexion.setAutoCommit(originalAutoCommit);
            // Cerrar el PreparedStatement
            try {
                instruccion.close();
            } catch (SQLException ignore) {}
        }
        return exito;
    }
}

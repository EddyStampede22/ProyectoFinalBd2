# Proyecto final de base de datos 2:
# Sistema de Administración de Colección de Videojuegos
## Integrantes del Equipo:
### Martinez Pimbert Jose Eduardo

## Instrucciones para compilación y ejecucción correcta del programa SistemaColeccionJuegos.java :
---

## 1. Requisitos Previos

- **Java Development Kit (JDK)**: versión 11 o superior.
  - Verifica la instalación: `java -version` y `javac -version`.
  - Asegúrate de tener la variable de entorno `JAVA_HOME` apuntando al directorio del JDK.

- **MySQL Server**: versión 5.7 o superior.
  - El programa usa JDBC para conectarse a la base de datos.
  - El usuario debe proporcionar sus propias credenciales de acceso a la base de datos.

- **Conector JDBC de MySQL**:
  - Descarga el `.jar` correspondiente (por ejemplo, `mysql-connector-java-5.1.48-bin.jar`).
  - Cópialo en el directorio `lib/` de tu proyecto.

## 2. Estructura de Directorios Recomendadas

```
proyecto/
├── lib/
│   └── mysql-connector-java-5.1.48-bin.jar
└── src/
    └── SistemaColeccionJuegos.java
```

## 3. Configuración de la Base de Datos

1. Inicia MySQL y crea la base de datos y las tablas mínimas:
   ```sql
   CREATE DATABASE videogame_collection;
   USE videogame_collection;

   -- Ejemplo de tabla `users`
   CREATE TABLE users (
     user_id INT AUTO_INCREMENT PRIMARY KEY,
     username VARCHAR(50) UNIQUE,
     password CHAR(64),
     email VARCHAR(100),
     preferred_platform_id INT,
     access_type ENUM('admin','user'),
     activo TINYINT DEFAULT 1
   );

   -- Tablas `platform`, `games`, `game_collection`, `menu`
   -- Define sus columnas según las consultas en el código.
   ```

2. Ajusta directamente tus credenciales en el método `ConectarABaseD()` de `SistemaColeccionJuegos.java`:
   ```java
   // Dentro de ConectarABaseD():
        String url = "jdbc:mysql://localhost:3306/videogame_collection";
        String mysqlUser = "TU_USUARIO";
        String mysqlPassword = "TU_CONTRASEÑA";
        try {
            conexion = DriverManager.getConnection(url, mysqlUser,mysqlPassword);
   ```

## 4. Compilación

1. Abre tu terminal o consola.
2. Ve al directorio `src/`:
   ```bash
   cd proyecto/src
   ```
3. Compila el código, incluyendo el conector MySQL en el classpath:

   - **Linux/macOS**:
     ```bash
     javac -cp "../lib/mysql-connector-java-5.1.48-bin.jar" SistemaColeccionJuegos.java
     ```

   - **Windows** (usa `;` en lugar de `:`):
     ```bat
     javac -cp "..\lib\mysql-connector-java-5.1.48-bin.jar" SistemaColeccionJuegos.java
     ```

4. Verifica que se haya generado `SistemaColeccionJuegos.class` en el mismo directorio.

## 5. Ejecución

Desde `src/`, ejecuta:

- **Linux/macOS**:
  ```bash
  java -cp ".:../lib/mysql-connector-java-5.1.48-bin.jar" SistemaColeccionJuegos
  ```

- **Windows**:
  ```bat
  java -cp ".;..\lib\mysql-connector-java-5.1.48-bin.jar" SistemaColeccionJuegos
  ```

### Parámetros de Ejecución

- No se requieren argumentos de línea de comandos.
- El programa solicitará:
  1. `username` y `password` (hasta 3 intentos).
  2. Mostrará un menú interactivo en consola.

## 6. Uso del Programa

1. **Login**: ingresa tu usuario y contraseña.
2. **Menú Principal**: selecciona una categoría (número).
3. **Opciones**:
   - Ingresa el número de la opción para ejecutar la acción.
   - `0` vuelve al menú anterior o sale del programa.

## 7. Limpieza y Recompilación

- Para limpiar clases compiladas:
  ```bash
  find . -name "*.class" -delete
  ```
- Luego repite el paso de compilación.

## 8. Solución de Problemas Comunes

| Error                                                      | Posible Solución                                                  |
|------------------------------------------------------------|-------------------------------------------------------------------|
| `java.sql.SQLException: Access denied for user ...`        | Verifica URL, usuario y contraseña usados en el método de conexión. |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver`        | Asegúrate de incluir el `.jar` en el classpath al compilar y ejecutar. |
| `SQLException: Table '...users' doesn't exist`            | Crea las tablas según el script SQL en la sección 3.             |
| `InputMismatchException` al leer entrada del usuario       | Ingresa un número válido; el menú solo acepta dígitos.            |

## 9. Notas Adicionales y Buenas Prácticas

- **Tipos de usuario**:
  - `admin`: puede crear/editar/eliminar usuarios ,plataformas y juegos.
  - `user`: puede editar su usuario o contraseña.
  - tanto `user` como `admin` puede ver sus colecciones al igual que ver los videojuegos con más usuarios en su colección, los primeros 5 videojuegos con mejor promedio y los que es la creación, modificación y eliminación de los ratings de los videojuegos.

- **Seguridad**:
  - Las contraseñas se almacenan con hash SHA-256.

- **Extensiones**:
  - Implementa un `Makefile` o scripts `.sh`/`.bat` para automatizar compilación y ejecución.
  - Considera usar un gestor de dependencias (Maven/Gradle) para manejar el conector JDBC.

- ### En este proyecto se hizo uso de Asistentes Digitales (ChatGTP y Claude). Al igual que el programa `SistemaColeccionJuegos.java` también toma referencias de `PruebAccesoMYSQL.java` que encontrarás en el repositorio.



package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author alfon
 */
public class Conexion {

    private static final String URL =
            "jdbc:mysql://localhost:3306/Agenda_Medica"
            + "?useSSL=false"
            + "&serverTimezone=UTC"
            + "&allowPublicKeyRetrieval=true";

    private static final String USUARIO = "root";

    private static final String PASSWORD = "CESBA1234";

    public static Connection conectar() {

        Connection conexion = null;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(
                    URL, 
                    USUARIO, 
                    PASSWORD
            );
            
            System.out.println(
                            "Conexion a Agenda_Medica exitosa."
            );

        } catch (ClassNotFoundException e) {
            System.out.println("No se encontró el driver de MySQL: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }

        return conexion;
    }
}

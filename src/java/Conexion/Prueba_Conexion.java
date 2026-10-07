/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Conexion;
import java.sql.Connection;
public class Prueba_Conexion {
    
        public static void main(String[] args) {
            
                Connection conexion = Conexion.conectar();
                
                if (conexion != null) {
                    
                    System.out.println(
                                   "=================================="
                    );
                    System.out.println(
                                    "CONEXION EXITOSA"
                    );
                    
                    System.out.println(
                                    "Base de datos : Agenda_Medica"
                    
                    );
                    
                    try {
                        
                        conexion.close() ;
                        
                    
                } catch (Exception e) {
                        
                        e.printStackTrace();
                        }
        } else {
    
                  System.out.println(
                                 "ERROR DE CONEXION"
                  );
                  
                  System.out.println(
                                "==================="
                  );
            }
        }
}
    
    

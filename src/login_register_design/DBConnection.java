
package login_register_design;
import java.sql.*;

/**
 *
 * @author User
 */
public class DBConnection {
    static final String DB_URL = "jdbc:mysql://localhost/registrar";
    static final String USER = "root";
    static final String PASS = "";
        
    public static Connection connectDB(){
        Connection conn = null;
        
        try{
            //register jdbc driver, not required for newer versions of
//            Class.forName("com.mysql.jdbd.driver");
            //open a connection
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            return conn;
        }catch(Exception ex){
            System.out.println("There are error while connecting to db");
            return null;
        }
    }
}

package eu.fbk.dh.utils.creep;

import java.sql.Connection;
import java.sql.DriverManager;

public class ProvaMysql {

    public static void main(String[] args) {
        try {
            Connection dbconn;
            Class.forName("com.mysql.cj.jdbc.Driver");

            dbconn = DriverManager
                    .getConnection(
                            "jdbc:mysql://localhost:3306/alicidb_deg?socket=/Applications/MAMP/tmp/mysql/mysql.sock&serverTimezone=UTC",
                            "alcide", "pippo");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

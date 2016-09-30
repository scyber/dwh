import ru.russianpost.qa.actions.DataManagement;

import java.io.FileInputStream;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by gpiskunov on 12.09.2016.
 */
public class ConnProdHive {
    public static final String[] INIT_QUERIES = new String[]{"add jar /usr/hdp/2.3.4.0-3485/hive/lib/hive-contrib-1.2.1.2.3.4.0-3485.jar", "CREATE TEMPORARY FUNCTION row_sequence as 'org.apache.hadoop.hive.contrib.udf.UDFRowSequence'", "set hive.support.sql11.reserved.keywords=false"};
    public static String User = "gpiskunov";
    public static String Password = "poiu#EDC@grigoriy";
    public static String hiveUrl = "jdbc:hive2://10.233.0.85:10010/";

    public static String hiveQuery = "select * from merge.matreshka_merge where bar_code = '14586797016224' and oper_date_time >= '2016-04-01' and oper_date_time <= '2016-04-26' ";


    public  static void main (String args[]) {
          ResultSet rs = null;

        Calendar calBefore = Calendar.getInstance();
         System.out.println(calBefore.getTime());

        try {

            //rs = ConnProdHive.executeHiveQuery(hiveUrl, User, Password, INIT_QUERIES, hiveQuery);
            //while (rs.next()){
            //  System.out.println(rs.getString(1));
        Thread.sleep(10000);


        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        Calendar calAfter = Calendar.getInstance();
        System.out.println(calAfter.getTime());
    }



    public static ResultSet executeProdHiveQuery (String URL, String User, String Password, String [] INIT_QUERIES, String hiveQuery ) throws SQLException {

        Statement stmnt = DataManagement.connectHive(URL, User, Password, INIT_QUERIES);
        ResultSet rs = null;
        try {
            rs = stmnt.executeQuery(hiveQuery);

        } catch (Exception ex ) {
            ex.printStackTrace();
        }

        return rs;
    }
    public static Statement connectHive (String URL, String User, String Password, String [] INIT_QUERIES ) throws SQLException {
        Statement stmnt = null;
        Connection con = null;
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }
        try {
            con = DriverManager.getConnection(URL, User, Password);
            /*System.out.println(con.toString());*/
            runInitializationQueries(con, stmnt, INIT_QUERIES );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // try {
        stmnt = con.createStatement();

        return stmnt;
    }
    private static void runInitializationQueries(Connection con, Statement stmnt, String[] INIT_QUERIES ) {
        //Statement stmnt = null;
        try {
            stmnt = con.createStatement();
            for (String query : INIT_QUERIES) {

                stmnt.execute(query);
            }
        } catch (SQLException e) {
            //log.error("Error while trying to execute hive init queries.", e);
            e.printStackTrace();
        }
    }

    public void executeHive (String hiveQuery, Statement hiveStatement ) {
        ResultSet rs = null;
        try {
            rs = hiveStatement.executeQuery(hiveQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static ResultSet executeHiveQuery (String URL, String User, String Password, String [] INIT_QUERIES, String hiveQuery ) throws SQLException {

        Statement stmnt = DataManagement.connectHive(URL, User, Password, INIT_QUERIES);
        ResultSet rs = null;
        try {
            rs = stmnt.executeQuery(hiveQuery);

        } catch (Exception ex ) {
            ex.printStackTrace();
        }

        return rs;
    }
    public String [] getHiveInitQueries() {
        Properties hiveCommonProperties = new Properties();
        try {
            FileInputStream outPutProperties;
            outPutProperties = new FileInputStream( "commonHive.properties" );
            hiveCommonProperties.load(outPutProperties);
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        String[] INIT_QUERIES = (String[]) hiveCommonProperties.get("init_queries").toString().split(",");
        System.out.println(INIT_QUERIES);
        return INIT_QUERIES;
    }
}

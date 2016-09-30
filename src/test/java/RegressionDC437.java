
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import java.io.IOException;
import static java.lang.System.exit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ntp.TimeStamp;
import org.json.simple.JSONObject;
import org.testng.*;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gpiskunov
 * TestRail Results
 *
 *      Status  1 - success passed , 2 - Blocked , 3 - Untested can't be used , 4 - retest, 5 - failed
 * 
 */
public class RegressionDC437 {
    public static Cluster clTest;
    public static Session ssTest;
    //public static String casandraAddr = "hw-test-dc2";
    //public static String conKeyspace = "matreshka";
    //public static String rtTracesTable = "matreshka.rt_traces";
    //public static String selectFromCasandra = "select barcode, oper_timestamp, oper_time_zone from matreshka.rt_traces limit 3";
    public static ResultSet rsCasandra;
    public static java.sql.ResultSet rs;
    public static List BarCodeHiveList = new ArrayList();
    public static List TimeStampHiveList = new ArrayList();
    public static List TimeZoneHiveMillsList = new ArrayList(); 
    
    //public static final String HIVE_URL = "jdbc:hive2://hw-test-dc1:10010/tracking";
    //public static final String USER = "hdfs";
    //public static final String PASSWORD = "";
    //public static final String[] INIT_QUERIES = new String[]{"add jar /usr/hdp/2.3.0.0-2557/hive/lib/hive-contrib-1.2.1.2.3.0.0-2557.jar", "CREATE TEMPORARY FUNCTION row_sequence as 'org.apache.hadoop.hive.contrib.udf.UDFRowSequence'", "set mapred.job.queue.name=test", "set hive.support.sql11.reserved.keywords=false"};
    public static final String[] INIT_QUERIES = new String[]{"add jar /usr/hdp/2.3.4.0-3485/hive/lib/hive-contrib-1.2.1.2.3.4.0-3485.jar", "CREATE TEMPORARY FUNCTION row_sequence as 'org.apache.hadoop.hive.contrib.udf.UDFRowSequence'", "set mapred.job.queue.name=test", "set hive.support.sql11.reserved.keywords=false"};
    public static Connection con = null;
    public static Statement stmt = null;
    //public static String SelectFromHive = "select id_of_operand1, oper_time_zone, oper_date_time from gpiskunov.operations where id_of_operand1 = ";
    public static List BarCodeCasandraList = new ArrayList();
    public static List TimeStampCasandraList = new ArrayList();
    public static List TimeZoneCasandraMillsList = new ArrayList(); 
    public static int LimitSelectItems ;
    
        
    
    
    
    @Test(enabled=true)
    @Parameters({"conKeyspace", "casandraAddr", "SelectFromCasandra", "HIVE_URL", "USER", "PASSWORD", "SelectFromHive",
        "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL" })
    public static void compareDataBetweenHiveAndCasandra (String conKeyspace, String casandraAddr, String SelectFromCasandra, 
            String HIVE_URL, String USER, String PASSWORD , String SelectFromHive, String TestRunID, String TestCaseID,
            String TestRailUser, String TestRailPassword, String TestRailURL) throws ParseException, IOException, APIException {
            
        TimeZone tz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Calendar calCasandra = Calendar.getInstance();
        //System.out.println(calCasandra.getTime());
        //exit(0);
            
            
            getDataTimefromCasandra (conKeyspace, casandraAddr, SelectFromCasandra );
            LimitSelectItems = BarCodeCasandraList.size();
            //System.exit(0);
            
            getDataTimefromHive ( HIVE_URL, USER, PASSWORD, SelectFromHive);
            //System.exit(0);
            
            for (int i = 0; i < LimitSelectItems; i++ ) {
            long timeZoneHive;
            long timeStampHive;
            long timeStampCasandra;
            long resultTimeHive;
            timeStampHive = Long.parseLong(TimeStampHiveList.get(i).toString());
            timeZoneHive = Long.parseLong(TimeZoneCasandraMillsList.get(i).toString());
            timeStampCasandra = Long.parseLong(TimeStampCasandraList.get(i).toString());
            
            resultTimeHive = timeStampHive - timeZoneHive;
            String Results;
            //System.out.println(" resultTimeHive " +  resultTimeHive + " timeStampHive " + timeStampHive + " timeZoneHive " + timeZoneHive + "TimeStmapCasandra " + timeStampCasandra );
            if ( resultTimeHive != timeStampCasandra ) {
                Results = " There are an issue with time " + " Time in Hive " + resultTimeHive + " Time in Casandra " + timeStampCasandra + " Expected to be equal ";
                System.out.println( Results );
                // Add To TestRail Results with Fail Status
                
                int StatusId = 5;
                addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                        TestRailPassword, TestRailURL, Results );
                Assert.assertEquals(resultTimeHive,timeStampCasandra );
                
            } else {
                Results = "The test has been passed successfully " + "resultTimeHive is equal to timeStampCasandra ";
                int StatusId = 1;
                addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                        TestRailPassword, TestRailURL, Results );
            // Add To TestRail Section Results with Success Status
            }
            
        }
    }
    
    public static void getDataTimefromHive (String HIVE_URL, String USER, String PASSWORD , String SelectFromHive) throws ParseException {
        coonectToCL (HIVE_URL, USER, PASSWORD);
        // get env.test.data from hive
        // Compare the Dates between barcode selections
        /*
        Выборка из Hive
        */
        String RpoBarCode;
        String DateTime;
        String TimeZoneOffset;
        
        Date getTSHive = new Date();
        
        try {
            
            for (int i = 0; i < LimitSelectItems; i++ ) {
                //System.out.println(BarCodeCasandraList.get(i));
                //System.out.println(SelectFromHive + "'" + BarCodeCasandraList.get(i) + "'");
                java.sql.ResultSet res = (java.sql.ResultSet) stmt.executeQuery(SelectFromHive + "'" + BarCodeCasandraList.get(i) + "'");
                Assert.assertNotNull(res);
                while (res.next()) {
                    
                    //System.out.println(res.getString(1) + " " + res.getString(3) + " " + res.getString(2));
                    //System.out.println(res.getString(3));
                    //
                    TimeZoneOffset = res.getString(2);
                    //
                    // Convert TimeZone to Mills
                    //long TimeZoneMills = Long.valueOf(TimeZoneFromHive);
                    TimeZone tzFromHive = TimeZone.getTimeZone(TimeZoneOffset);
                    //System.out.println("Long value of TimeZone Hive " + tzFromHive);
                    // Modify date&time format to mills                    
                    String tsSource = res.getString(3);
                    //System.out.println("Get Source Time from Hive " + tsSource );
                    SimpleDateFormat fr;
                    fr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date tsHive = fr.parse(tsSource);
                    //System.out.println("Get TimeStamp From hive " + tsHive.getTime());
                    //System.out.println("Get TimeZone offset from hive " + tzFromHive.toString());
                    TimeStampHiveList.add(tsHive.getTime());
                    //System.out.println(" Hive get time in mills " + fr.getCalendar().getTime());
                }
                
            }
            
            /*
            while ( res.next() ) {
                RpoBarCode = res.getString(1);
                DateTime = res.getString(3);
                TimeZoneFromHive = res.getString(2);
                //addElementsToList( RpoInNet, RpoBarCode);
                System.out.println("BarCode " + RpoBarCode + " Date Time " + DateTime + " TimeZone " + TimeZoneFromHive  );
                //Long CalculateFull = fullSimpleRest + 201 ;
                //System.out.println(CalculateFull);
            }
            */
            //System.out.println(RpoOutOfNet.toString());
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            //TO DO assert fail
        //    Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Assert.assertNotNull(BarCodeHiveList);
        Assert.assertNotNull(TimeStampHiveList);
        Assert.assertNotNull(TimeZoneHiveMillsList);
    }
    
    public static void getDataTimefromCasandra (String conKeyspace, String casandraAddr, String SelectFromCasandra ) {

        connectToCasandra(casandraAddr, conKeyspace );
        rsCasandra = ssTest.execute(SelectFromCasandra);
        
        for (Row row: rsCasandra) {
            
            //System.out.println(row.getString("barcode") + " " + row.getTimestamp("oper_timestamp") + " " + row.getInt("oper_time_zone") );
            BarCodeCasandraList.add(row.getString("barcode"));
            Date getTS = new Date();
            getTS =  row.getTimestamp("oper_timestamp") ;
            //System.out.println("GetTimeStamp from Cassandra " + getTS);
            //System.out.println("TimeStamp Cassandra in Mills " + getTS.getTime());
            TimeStampCasandraList.add(getTS.getTime());
            long TimeZoneMills = row.getInt("oper_time_zone");
            //System.out.println("Time in mills " + getTS.getTime() + " Time Zone shift in mills " + TimeZoneMills );
            
            //long timeInMills = row.getTimestamp("oper_timestamp")
            TimeZoneCasandraMillsList.add(row.getInt("oper_time_zone"));
            
            //System.out.println(calCasandra.setTimeInMillis(row.getTimestamp("oper_timestamp")));
        }
        //if (BarCodeCasandraList.isEmpty() || TimeStampCasandraList.isEmpty() || TimeZoneCasandraMillsList.isEmpty() ) {
            Assert.assertNotNull(BarCodeCasandraList);
            Assert.assertNotNull(TimeStampCasandraList);
            Assert.assertNotNull(TimeZoneCasandraMillsList);
        //}
        clTest.close();
    }
    public static void connectToCasandra (String casandraAddr, String conKeyspace ) {
        //Address to coonect
        
        clTest = Cluster.builder().addContactPoint(casandraAddr).build();
        // keyspace sortmaster
        ssTest = clTest.connect(conKeyspace);
        
    }
    public static void coonectToCL (String hiveURL, String User, String Password) {
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            //log.error("Class not found", e);
            
        }
         try {
            con = DriverManager.getConnection(hiveURL, User, Password);
            runInitializationQueries(con, stmt);
        } catch (SQLException e) {
            System.out.println("Error while connection to " + hiveURL + "(user: " + User + ")" + e);
            //log.error("Error while connection to " + hiveURL + "(user: " + user + ")", e);
        }
        
        
        try {
            stmt = con.createStatement();
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            //Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        //} catch (SQLException e) {
        //    System.out.println("Error while trying to create statement" +  e);
            //log.error("Error while trying to create statement", e);
        //}
       //return stmt;
    }
     
     private static void runInitializationQueries(Connection con, Statement stmnt) {
        //Statement stmnt = null;
        try {
            stmnt = con.createStatement();
            for (String query : INIT_QUERIES) {
                //log.debug("Executing: " + query);
                System.out.println("Executing: " + query);
                stmnt.execute(query);
            }
        } catch (SQLException e) {
            //log.error("Error while trying to execute hive init queries.", e);
            System.out.println("Error while trying to execute hive init queries." + e);
        
        }
     }
     // Add Results to TestRail Section
     public static void addToTestRailResults (String TestRunID, 
             String TestCaseID, int StatusId,
             String TestRailUser, String TestRailPassword, 
             String  TestRailURL, String Results) throws APIException, IOException {
         
        APIClient client = new APIClient(TestRailURL);
        client.setUser(TestRailUser);
	client.setPassword(TestRailPassword);
        Map data = new HashMap();
        data.put("status_id", new Integer(StatusId) );
        data.put("comment", Results  );
        JSONObject r = (JSONObject) client.sendPost("add_result_for_case/" + TestRunID + "/" + TestCaseID, data );
        
     }
     

}

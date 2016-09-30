/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import java.io.IOException;
import org.testng.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hive.jdbc.HiveDriver;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Parameters;
import java.sql.*;

import java.util.Map;
import java.util.HashMap;
import org.json.simple.JSONObject;



//import org.apache.log4j.Logger;


/**
 *
 * @author gpiskunov
 */
public class SummaryRest {
   
    
//    private static final Logger log = Logger.getLogger(SummaryRest.class);
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    public Connection con = null;
    public Statement stmt = null;
    public Connection connVertica = null;
    public Statement stmtVertica = null;
    public String resultString = "";
    public String queryDBMartreshkaMerge = "TRACKING";
    public String queryTable = "MATRESHKA_MERGE";
    
    public static final String HIVE_URL = "jdbc:hive2://hw-test-dc1:10010/tracking";
    public static final String USER = "hdfs";
    public static final String PASSWORD = "";
    //public static final String[] INIT_QUERIES = new String[]{"add jar /usr/hdp/2.3.0.0-2557/hive/lib/hive-contrib-1.2.1.2.3.0.0-2557.jar", "CREATE TEMPORARY FUNCTION row_sequence as 'org.apache.hadoop.hive.contrib.udf.UDFRowSequence'", "set mapred.job.queue.name=test", "set hive.support.sql11.reserved.keywords=false"};
    public static final String[] INIT_QUERIES = new String[]{"add jar /usr/hdp/2.3.4.0-3485/hive/lib/hive-contrib-1.2.1.2.3.4.0-3485.jar", "CREATE TEMPORARY FUNCTION row_sequence as 'org.apache.hadoop.hive.contrib.udf.UDFRowSequence'", "set mapred.job.queue.name=test", "set hive.support.sql11.reserved.keywords=false"};
    
    public static Properties prop = new Properties();
    
    public static String fullRest[] = new String[]{};
    public static String fullRestDate[] = new String[]{};
    
    public static List RestDates = new ArrayList();
    public static List RestSumValues  = new ArrayList();
    public static List RestWayValues = new ArrayList();
    public static List RestObjValues = new ArrayList();
    public static List RpoOutOfNet = new ArrayList();
    public static List RpoInNet = new ArrayList();
    public static List RpoInfoCheckList = new ArrayList();
 
    public String VerticaUser = "dbadmin";
    public String VerticaPass = "poiu#EDC@pass";
    public String VerticaURL = "jdbc:vertica://hw-test-dc6:5433/tracking";

     // Проверка РПО находящихся вне отчета
     // при падении следует выяснять поему РПО попало в таблицу balance_fact
     @Test(enabled=true)
     @Parameters({"getRpoOutOfNet", "selectRpoInReportTable", 
     "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL" })
     public void checkRpoOutOfNetList (String getRpoOutOfNet, String selectRpoInReportTable, 
             String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword,
             String TestRailURL) {
        coonectToCL (HIVE_URL, USER, PASSWORD);
        String RroBarCode;
        try {
            ResultSet res = stmt.executeQuery(getRpoOutOfNet );
            // TODO if res is null - fail add results to TestRail
            while ( res.next() ){
                RroBarCode = res.getString(1);
                addElementsToList(RpoOutOfNet, RroBarCode);
                //System.out.println("RPO out of Net " + RroBarCode );
                //Long CalculateFull = fullSimpleRest + 201;
                //System.out.println(CalculateFull);
            }
            for (int i =0; i< RpoOutOfNet.size(); i++ ) {
            //System.out.println(RpoOutOfNet.get(i));
            ResultSet resCheck = stmt.executeQuery(selectRpoInReportTable + "'" + RpoOutOfNet.get(i) + "'" );
            while ( resCheck.next() ){ 
                    RroBarCode = resCheck.getString(1);
                    assertNotNull(RroBarCode);
                }
            }
            //System.out.println(RpoOutOfNet.toString());
        } catch (SQLException ex) {
            String Results = ex.toString();
            int StatusId = 5; // The Test Failed
            // TODO Send ErrorEx to TestRail results
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.assertNotNull(ex);
            
            try {
                addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                        TestRailPassword, TestRailURL, Results);
            } catch (APIException | IOException ex1) {
                Assert.assertNotNull(ex1);
                ex1.printStackTrace();
                Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex1);
            } 
            
            
            ex.printStackTrace();
            
            //TO DO assert fail
            
        } 
        
         
     }
     // Проверка РПО попадающих в таблицу balance_fact 
     // если не попадает - проверяем - есть ли в rpo_info
     // если РПО в нем есть - ошибка, если нет - все хорошо у нас нет информации об РПО
     @Test(enabled=false)
     @Parameters({"getRpoInReport", "selectRpoInReportTable", "selectRpoFromInfo", 
     "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL" })
     public void checkRpoInNetList (String getRpoInReport, String selectRpoInReportTable, String selectRpoFromInfo,
             String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword,
             String TestRailURL) {
        coonectToCL (HIVE_URL, USER, PASSWORD);
        
        String RpoBarCode;
        try {
            ResultSet res = stmt.executeQuery(getRpoInReport);
            // TODO if res is null send Error to TestRail results
            
            while ( res.next() ) {
                RpoBarCode = res.getString(1);
                addElementsToList( RpoInNet, RpoBarCode);
                //System.out.println("RPO out of Net " + RroBarCode );
                //Long CalculateFull = fullSimpleRest + 201 ;
                //System.out.println(CalculateFull);
            }
            for (int i =0; i< RpoInNet.size(); i++ ) {
            //System.out.println(RpoOutOfNet.get(i));
            ResultSet resCheck = stmt.executeQuery(selectRpoInReportTable + "'" + RpoInNet.get(i) + "'" );
            while ( resCheck.next() ) { 
                    RpoBarCode = resCheck.getString(1);
                    // Проверяем - есть ли этот Баркод в rpo_info
                    if (RpoBarCode == null) {
                    //assertNotNull(RroBarCode );
                    // Формируем новый список котоырй сравним с rpo_info
                        addElementsToList( RpoInfoCheckList, RpoBarCode);
                    }
                }
            // Если РПО не находятся в отчете - проверяем есть ли они в
            // таблице rpo_info
            if (RpoInfoCheckList.size() != 0 ) {
                for (int j =0; j< RpoInfoCheckList.size(); j++ ) {
                    ResultSet resCheckRpoInfo = stmt.executeQuery(selectRpoFromInfo + "'" + RpoInfoCheckList.get(j) + "'" );
                    
                    while ( resCheckRpoInfo.next() ) { 
                        RpoBarCode = resCheck.getString(1);
                        if (RpoBarCode != null) {
                            System.out.println(" We have and issue RPO " + RpoBarCode);
                            String Results = "РПО находится не отчёте, но оно есть в RPO_INFO " + RpoBarCode ;
                            int StatusId = 5;// Failed
                            try {
                                // TODO Добавляем результат в Комментарий TestRail
                                addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                                        TestRailPassword, TestRailURL, Results);
                            } catch (APIException|IOException ex) {
                                //Fail to Add Results to TestRail
                                Assert.assertNotNull(ex);
                                Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                        }
                        assertNotNull(RpoBarCode);
                        // Проверяем - есть ли этот Баркод в rpo_info
                        
                    }
                }
            }
            }
            
            //System.out.println(RpoOutOfNet.toString());
            
        } catch (SQLException ex) {
            String Results = ex.toString();
            int StatusId = 5; // The Test Failed
            ex.printStackTrace();
            try {
                addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                        TestRailPassword, TestRailURL, Results);
            } catch (APIException|IOException ex1) {
                //TODO Fail to add results to TestRail
                
                Assert.assertNotNull(ex1);
                Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex1);
            } 
            Assert.assertNotNull(ex);
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        
         // TODO Add successfully passed results
        String Results = "The auto-test successfully passed ";
        int StatusId = 1; // The Test Failed
        try {
            addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                    TestRailPassword, TestRailURL, Results);
        } catch (APIException | IOException ex) {
            // Fail to Add the Results to TestRail System
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.assertNotNull(ex);
            
        } 
        
     }
     @Test(enabled=false)
     @Parameters({ "dropRPOInfo", "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL" })
     public void dropTestRPOInfoTable (String dropRPOInfo,
             String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword, String TestRailURL ) {
         coonectToCL (HIVE_URL, USER, PASSWORD);
                 
        try {
            stmt.execute(dropRPOInfo );           
            //System.out.println(RpoOutOfNet.toString());
           
        } catch (SQLException ex) {
            String Results  = ex.toString();
            int StatusId = 5; // Fail
             try {
                 addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                         TestRailPassword, TestRailURL, Results);
             } catch (APIException|IOException ex1) {
                 Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex1);
             }            
            ex.printStackTrace();
            //TO DO assert fail
            Assert.assertNotNull(ex);
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
     }
     @Test(enabled=true)
     @Parameters({ "dropMatreshkaMerge", "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL"  })
     public void dropTestMatreshkaMergeTable (String dropMatreshkaMerge, 
             String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword, String TestRailURL){
         coonectToCL (HIVE_URL, USER, PASSWORD);  
        try {
            stmt.execute(dropMatreshkaMerge);          
            //System.out.println(RpoOutOfNet.toString());
           
        } catch (SQLException ex) {
            String Results = ex.toString();
            int StatusId = 5;
             try {
                 addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                         TestRailPassword, TestRailURL, Results);
             } catch (APIException|IOException ex1) {
                 //Fail to add results to TestRail
                 Assert.assertNotNull(ex1);
                 Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex1);
             }
            ex.printStackTrace();
            //TO DO assert fail
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
     }
     @Test(enabled=false)
     @Parameters({"createMatreshkaMerge", "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL" })
     public void createTestMatreshkaMergeTable (String createMatreshkaMerge,
             String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword, String TestRailURL) {
         coonectToCL (HIVE_URL, USER, PASSWORD);  
        try {
            stmt.execute(createMatreshkaMerge );           
            //System.out.println(RpoOutOfNet.toString());
           
        } catch (SQLException ex) {
            String Results  = ex.toString();
            int StatusId = 5; // Fail
             try {
                 addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                         TestRailPassword, TestRailURL, Results);
             } catch (APIException|IOException ex1) {
                 Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex1);
             }            
            ex.printStackTrace();
            //TO DO assert fail
            Assert.assertNotNull(ex);
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
        } 
     }
     @Test(enabled=false)
     @Parameters({"createTestRpoInfo", "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL" })
     public void createTestRPOInfoTable (String createTestRpoInfo, 
             String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword, String TestRailURL) {
         coonectToCL (HIVE_URL, USER, PASSWORD);
        try {
            stmt.execute(createTestRpoInfo);
        } catch (SQLException ex) {
            //TO DO assert fail
            String Results  = ex.toString();
            int StatusId = 5; // Fail
             try {
                 addToTestRailResults ( TestRunID, TestCaseID, StatusId, TestRailUser,
                         TestRailPassword, TestRailURL, Results);
             } catch (APIException|IOException ex1) {
                 Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex1);
             }    
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
     }
     
     @Test(enabled=false)
     public void connectToVertica() throws SQLException{
         connectToVertica (VerticaUser, VerticaPass, VerticaURL);
         
         //ResultSet rsVertica = null;
         ResultSet rsVertica = stmtVertica.executeQuery("select * from dicts.cvpp_info limit 10 ");
            while (rsVertica.next()) {
                String postObjectIndex = rsVertica.getString(1);
                System.out.println(postObjectIndex);
            }
        connVertica.close();
     }
     public void addElementsToList (List RpoList, String Rpo  ) {
         RpoList.add( Rpo);
     }
     public String parseResultToString(ResultSet res, Integer columnCount) {
        
        try {
            while (res.next()) {
                
                /*
                for (int i = 1; i <= columnCount; i++) {
                    resultString = resultString + res.getString(i);
                }
                */
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Error, while parsing query result to string" + e);
        }
        return resultString;
    }
     
     public void coonectToCL (String hiveURL, String User, String Password) {
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
     
     private void runInitializationQueries(Connection con, Statement stmnt) {
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
        /*
        finally {
            try {
                stmnt.close();
            } catch (Exception e) {
                //log.error("Error while trying to close SQL Statement", e);
                System.out.println("Error while trying to close SQL Statement" + e);
            }
        }
        */
    }
     
     public void connectToVertica (String User, String Password, String ConnURL) {
         try {
           Class.forName("com.vertica.jdbc.Driver");
           //Class.forName("org.hsqldb.jdbcDriver");
           //DriverManager.registerDriver(new jdbcDriver());
         } catch(ClassNotFoundException ex) {
         
         }
         Properties connProp = new Properties();
         connProp.put("user", User);
         connProp.put("password", Password);
         
         
         try {
             connVertica = DriverManager.getConnection(ConnURL, connProp);
             stmtVertica = connVertica.createStatement();
             
         } catch (SQLException ex) { 
            System.out.println("Could not connect to vertica DB " + ConnURL);
            ex.printStackTrace();
            Logger.getLogger(SummaryRest.class.getName()).log(Level.SEVERE, null, ex);
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
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
}

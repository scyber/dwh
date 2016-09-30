import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by gpiskunov on 27.07.2016.
 */

public class BalanceRegression {

    public Connection con = null;
    public Statement stmt = null;
    public Connection connVertica = null;
    public Statement stmtVertica = null;
    public String resultString = "";
    public String queryDBMartreshkaMerge = "TRACKING";
    public String queryTable = "MATRESHKA_MERGE";

    //public static final String HIVE_URL = "jdbc:hive2://hw-test-dc1:10010/test_tmp";
    //public static final String USER = "hdfs";
    //public static final String PASSWORD = "";
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
    public static List countRpoInTheWay = new ArrayList();

    public String VerticaUser = "dbadmin";
    public String VerticaPass = "poiu#EDC@pass";
    public String VerticaURL = "jdbc:vertica://hw-test-dc6:5433/tracking";
    String jobIdOstatki ;
    //
    // Test Refresh Target Schama Test Balances Report
    //Test pass TestNG Params to Map


    @Test(enabled = true)
    /*@Parameters({"HiveUrl", "HiveTestSchema", "HiveUserName", "HiveUserNamePassword", "DropSchemaQuery" , "CreateSchemaQuery",
            "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL", "TestRailFailStatus", "TestRailSuccesStatus" })*/
    public void refreshTestBalancesSchema (ITestContext context /*String HIVE_URL, String HIVE_SCHEMA, String USER, String PASSWORD,
                                           String dropSchemaQuery, String createSchemaQuery, String TestRunID,
                                           String TestCaseID, String TestRailUser, String TestRailPassword,
                                           String  TestRailURL, String TestRailFailStatus, String TestRailSuccesStatus */ ) {

        //ToDo get parameters from TestSuite to Map
        Map<String,String> testParameters = context.getCurrentXmlTest().getTestParameters();
        //coonectToCL(HIVE_URL, USER, PASSWORD);
        //System.out.println("HiveUrl " + testParameters.get( "HiveUrl" ));
        //System.out.println("");


        coonectToCL(testParameters.get("HiveUrl").toString()+testParameters.get("HiveTestSchema").toString(),
                testParameters.get("HiveUserName").toString(), testParameters.get("HiveUserNamePassword").toString());
        // Drop Database
        try {
                stmt.execute(testParameters.get("DropSchemaQuery").toString());
        } catch (SQLException e) {
            String Results = e.getLocalizedMessage();
            //int StatusId = Integer.parseInt(testParameters.get("TestRailFailStatus"));
            try {
                addToTestRailResults (testParameters.get("TestRunID").toString(), testParameters.get("TestCaseID").toString(), Integer.parseInt(testParameters.get("TestRailFailStatus").toString()),
                        testParameters.get("TestRailUser").toString(),testParameters.get("TestRailPassword").toString(), testParameters.get("TestRailURL").toString(), Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        // Create Schema
        try {
            stmt.execute(testParameters.get("CreateSchemaQuery").toString());
        } catch (SQLException e) {
            String Results = e.getLocalizedMessage();
            try {
                addToTestRailResults (testParameters.get("TestRunID").toString(), testParameters.get("TestCaseID").toString(), Integer.parseInt(testParameters.get("TestRailFailStatus").toString()),
                        testParameters.get("TestRailUser").toString(), testParameters.get("TestRailPassword").toString(), testParameters.get("TestRailURL").toString(), Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        // Проверка РПО в Обьекте статус, в результирующих таблицах
        //ToDo finally add success status results to TestRail
        //
        String Results = "The test has been passed successfully  " ;
        //int StatusId = Integer.parseInt(TestRailSuccesStatus);
        try {
            addToTestRailResults (testParameters.get("TestRunID").toString(), testParameters.get("TestCaseID").toString(), Integer.parseInt(testParameters.get("TestRailSuccesStatus").toString()),
                    testParameters.get("TestRailUser").toString(), testParameters.get("TestRailPassword").toString(), testParameters.get("TestRailURL").toString(), Results);
        } catch (APIException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    // Test
    // Запуск OOZIE скриптов постороения остатков
    @Test (enabled = false )
    /*@Parameters({"oozieUrl", "oozieJobHDFSPath", "jobTracker", "queueName", "nameNode", "user.name",
            "oozie.use.system.libpath", "security_enabled", "security.passwords.encryption.enabled",
            "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL", "TestRailFailStatus", "TestRailSuccesStatus",
             "targetBalancesDB", "dictsDB", "sourceMergeDB", "date", "date_p1", "start_date", "date_m1", "date_m2",
            "date_m3", "date_m4", "date_m5", "date_m6"}) */
    public void runBalanceTestOozieJob ( ITestContext context /* String oozieUrl, String oozieJobHDFSPath,
                             String jobTracker, String queueName, String nameNode, String userName,
                             String oozieLibPath, String securityState, String securityPasswordEncriptionState,
                             String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword,
                             String  TestRailURL, String TestRailFailStatus, String TestRailSuccesStatus,
                             String targetBalancesDB, String dictsDB, String sourceMergeDB, String date, String date_p1,
                             String start_date, String date_m1, String date_m2, String date_m3, String date_m4, String date_m5, String date_m6 */ ) {
        //ToDo get parameters from TestSuite config
        Map<String,String> testParams = context.getCurrentXmlTest().getTestParameters();

        OozieClient cl = new OozieClient(testParams.get("oozieUrl").toString());
        Properties prop = cl.createConfiguration();
        Configuration conf = new Configuration();


        try {
            FileSystem fs = FileSystem.get(conf);
        } catch (IOException e) {
            // ToDo  Fail Test and add Fail results to TestRail
            String Results = e.getLocalizedMessage();
            //int StatusId = Integer.parseInt(testParams.get("TestRailFailStatus"));
            try {
                addToTestRailResults (testParams.get("TestRunID").toString(), testParams.get("TestCaseID").toString(),
                        Integer.parseInt(testParams.get("TestRailFailStatus").toString()), testParams.get("TestRailUser").toString(),
                        testParams.get("TestRailPassword").toString(), testParams.get("TestRailURL").toString(), Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }

        prop.setProperty(cl.APP_PATH, testParams.get("oozieJobHDFSPath").toString());
        prop.setProperty("jobTracker", testParams.get("jobTracker").toString());
        prop.setProperty("queueName", testParams.get("queueName").toString());
        prop.setProperty("nameNode", testParams.get("nameNode").toString());
        prop.setProperty("user.name", testParams.get("user.name").toString());
        prop.setProperty("oozie.use.system.libpath", testParams.get("oozie.use.system.libpath").toString());
        prop.setProperty("security_enabled", testParams.get("security_enabled").toString());
        prop.setProperty("security.passwords.encryption.enabled", testParams.get("security.passwords.encryption.enabled").toString());

        prop.setProperty("targetBalancesDB", testParams.get("targetBalancesDB").toString());
        prop.setProperty("dictsDB", testParams.get("dictsDB").toString());
        prop.setProperty("sourceMergeDB", testParams.get("sourceMergeDB").toString());
        prop.setProperty("date", testParams.get("date").toString());

        prop.setProperty("date_p1", testParams.get("date_p1").toString());
        prop.setProperty("start_date", testParams.get("start_date").toString());
        prop.setProperty("date_m1", testParams.get("date_m1").toString());
        prop.setProperty("date_m2", testParams.get("date_m2").toString());
        prop.setProperty("date_m3", testParams.get("date_m3").toString());
        prop.setProperty("date_m4", testParams.get("date_m4").toString());
        prop.setProperty("date_m5", testParams.get("date_m5").toString());
        prop.setProperty("date_m6", testParams.get("date_m6").toString());

        prop.setProperty("oozie.hive.log.level", "DEBUG");

        try {
            //System.out.println("Getting Job ID Ostatki status");
            jobIdOstatki = cl.run(prop);

            System.out.println(cl.getJobInfo(jobIdOstatki).getStatus());

            /*System.out.println (cl.getJobInfo(jobIdOstatki).getConf());
            System.out.println("Workflow details  " +  cl.getJobInfo(jobIdOstatki));
            System.out.println("Job id " + cl.getJobId(jobIdOstatki));*/
            while (cl.getJobInfo(jobIdOstatki).getStatus() == WorkflowJob.Status.RUNNING) {
                //ToDo

            }

        } catch (OozieClientException e) {
            /* ToDo add fail results to TestRail */
            String Results = e.getLocalizedMessage();
            //int StatusId = Integer.parseInt(testParams.get("TestRailFailStatus"));
            try {
                addToTestRailResults (testParams.get("TestRunID").toString(), testParams.get("TestCaseID").toString(), Integer.parseInt(testParams.get("TestRailFailStatus").toString()),
                        testParams.get("TestRailUser").toString(), testParams.get("TestRailPassword").toString(), testParams.get("TestRailURL").toString(), Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            /*
            * ToDo add fail results to Test Plan
            * */
            e.printStackTrace();
        }

        try {
            cl.getJobInfo(jobIdOstatki).getStatus();
            if (cl.getJobInfo(jobIdOstatki).getStatus() == WorkflowJob.Status.FAILED) {
                //ToDo add TestRail results Status fail Fail
                String Results = cl.getJobInfo(jobIdOstatki).getStatus().toString();
                //int StatusId = Integer.parseInt(testParams.get("TestRailFailStatus"));
                try {
                    addToTestRailResults (testParams.get("TestRunID").toString(), testParams.get("TestCaseID").toString(), Integer.parseInt(testParams.get("TestRailFailStatus").toString()),
                            testParams.get("TestRailUser").toString(), testParams.get("TestRailPassword").toString(), testParams.get("TestRailURL").toString(), Results);
                } catch (APIException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                //ToDo add Fail Results to TestPlan
                Assert.assertEquals(cl.getJobInfo(jobIdOstatki).getStatus().toString(), "SUCCESS");

            }
        } catch ( OozieClientException e) {
            // ToDo add Fail results to TestRail
            String Results = e.getLocalizedMessage();
            //int StatusId = Integer.parseInt(testParams.get("TestRailFailStatus"));
            try {
                addToTestRailResults (testParams.get("TestRunID").toString(), testParams.get("TestCaseID").toString(), Integer.parseInt(testParams.get("TestRailFailStatus").toString()),
                        testParams.get("TestRailUser").toString(), testParams.get("TestRailPassword").toString(), testParams.get("TestRailURL").toString(), Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
              e.printStackTrace();
        }
        //ToDo finally add success status results to TestRail
        //
        String Results = "The test has been passed successfully  " ;
        //int StatusId = Integer.parseInt(testParams.get("TestRailSuccesStatus"));
        try {
            addToTestRailResults (testParams.get("TestRunID").toString(), testParams.get("TestCaseID").toString(), Integer.parseInt(testParams.get("TestRailSuccesStatus").toString()),
                    testParams.get("TestRailUser").toString(), testParams.get("TestRailPassword").toString(), testParams.get("TestRailURL").toString(), Results);
        } catch (APIException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    // Проверка РПО в Пути - статус, наличие в результирующих таблицах
    @Test(enabled = false )
    @Parameters({"HiveUrl", "HiveTestSchema", "HiveUserName", "HiveUserNamePassword", "rpoInTheWayList" , "selectRPOinTheWayFirst", "selectRPOinTheWayLast",
                  "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL", "TestRailFailStatus", "TestRailSuccesStatus" })
    public  void checkRPOonTheWay (String HIVE_URL, String HIVE_SCHEMA, String USER, String PASSWORD,
                                   String RpoInTheWay, String selectRPOinTheWayFirst, String selectRPOinTheWayLast,
                                   String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword,
                                   String  TestRailURL, String TestRailFailStatus, String TestRailSuccesStatus ){
        //ToDo get Parameters of The Test

        coonectToCL(HIVE_URL+HIVE_SCHEMA, USER, PASSWORD);
        String countBarCodeInTheWay;
        List<String> rpoInThwWayList = new ArrayList(Arrays.asList(RpoInTheWay.split(", ")));

        //System.out.println("RPO in the way amount "  + rpoInThwWayList.size());

        try {
            //
            for (int i = 0; i<rpoInThwWayList.size(); i++ ) {

                System.out.println(selectRPOinTheWayFirst + "'" + rpoInThwWayList.get(i) + "'" + selectRPOinTheWayLast );

                ResultSet res = stmt.executeQuery(selectRPOinTheWayFirst + "'" + rpoInThwWayList.get(i) + "'" + selectRPOinTheWayLast  );
                // TODO if res is null - fail add results to TestRail
                while (res.next()) {
                    countBarCodeInTheWay = res.getString(1);
                    int countResults = Integer.parseInt(countBarCodeInTheWay);
                    if ( countResults != 1 ) {
                        // System.out.println("Fail results break next RPO select");
                        // System.out.println(" countBarCodeInTheWay " + countBarCodeInTheWay);

                        // ToDo add fail results to TestRail
                        String Results = new StringBuilder().append("PRO on the Way is not in the balance_fact table with rpo_state = 1 ").append(rpoInThwWayList.get(i)).toString();
                        int StatusId = Integer.parseInt(TestRailFailStatus);

                        try {
                            addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                                    TestRailPassword, TestRailURL, Results);
                        } catch (APIException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //ToDo fail results in test
                        //System.out.println(Results);
                        Assert.assertEquals( 1, countResults);

                    }
                    //addElementsToList(countRpoInTheWay, countBarCodeInTheWay);
                    //System.out.println("RPO out of Net " + RroBarCode );
                    //Long CalculateFull = fullSimpleRest + 201;
                    //System.out.println(CalculateFull);
                }
            }

        } catch (SQLException e) {
            String Results = e.getLocalizedMessage();
            int StatusId = Integer.parseInt(TestRailFailStatus);
            try {
                addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                        TestRailPassword, TestRailURL, Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            //ToDo add fail result due to get results
        }

        // Проверка РПО в Обьекте статус, в результирующих таблицах

        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //ToDo finally add success status results to TestRail
        //
        String Results = "The test has been passed successfully  " ;
        int StatusId = Integer.parseInt(TestRailSuccesStatus);
        try {
            addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                    TestRailPassword, TestRailURL, Results);
        } catch (APIException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // Проверка РПО в Обьекте - статус, наличие в результирующих таблицах
    @Test (enabled = false )
    @Parameters({"HiveUrl", "HiveTestSchema", "HiveUserName", "HiveUserNamePassword", "rpoInTheObjectList" , "selectRPOinTheObjectFirst", "selectRPOinTheObjectLast",
            "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL", "TestRailFailStatus", "TestRailSuccesStatus" })
    public void checkRPOInTheObject (String HIVE_URL, String HIVE_SCHEMA, String USER, String PASSWORD,
                                  String RpoInTheObject, String selectRPOinTheObjectFirst, String selectRPOinTheObjectLast,
                                  String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword,
                                  String  TestRailURL, String TestRailFailStatus, String TestRailSuccesStatus  ) {

        coonectToCL(HIVE_URL+HIVE_SCHEMA, USER, PASSWORD);
        String countBarCodeInTheWay;
        List<String> rpoInTheObjectList = new ArrayList(Arrays.asList(RpoInTheObject.split(", ")));
        //System.out.println("RPO in the way amount "  + rpoInThwWayList.size());

        try {
            //
            for (int i = 0; i<rpoInTheObjectList.size(); i++ ) {

                //System.out.println(selectRPOinTheWayFirst + "'" + rpoInThwWayList.get(i) + "'" + selectRPOinTheWayLast );
                ResultSet res = stmt.executeQuery(selectRPOinTheObjectFirst + "'" + rpoInTheObjectList.get(i) + "'" + selectRPOinTheObjectLast  );
                //
                while (res.next()) {
                    countBarCodeInTheWay = res.getString(1);
                    int countResults = Integer.parseInt(countBarCodeInTheWay);
                    if ( countResults != 1 ) {
                        // System.out.println("Fail results break next RPO select");
                        // System.out.println(" countBarCodeInTheWay " + countBarCodeInTheWay);
                        // ToDo add fail results to TestRail
                        String Results = "RPO int the Object should be in the balance_fact with rpo_state = 2 " + rpoInTheObjectList.get(i) ;
                        int StatusId = Integer.parseInt(TestRailFailStatus);
                        try {
                            addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                                    TestRailPassword, TestRailURL, Results);
                        } catch (APIException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        //ToDo fail results in test
                        Assert.assertEquals(countResults, 1);
                    }
                            //addElementsToList(countRpoInTheWay, countBarCodeInTheWay);
                            //System.out.println("RPO out of Net " + RroBarCode );
                            //Long CalculateFull = fullSimpleRest + 201;
                            //System.out.println(CalculateFull);
                }
            }

        } catch (SQLException e) {
            String Results = e.getLocalizedMessage();
            int StatusId = Integer.parseInt(TestRailFailStatus);
            try {
                addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                        TestRailPassword, TestRailURL, Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // ToDo add Fail Results to TestPlan
            e.printStackTrace();
            //ToDo add fail result due to get results

        }

        // Проверка РПО в Обьекте статус, в результирующих таблицах

        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //ToDo finally add success status results to TestRail
        //
        String Results = "The test has been passed successfully  " ;
        int StatusId = Integer.parseInt(TestRailSuccesStatus);
        try {
            addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                    TestRailPassword, TestRailURL, Results);
        } catch (APIException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }
    // РПО которые не должны попадать в результирующие таблицы
    @Test(enabled = false)
    @Parameters({"HiveUrl", "HiveTestSchema", "HiveUserName", "HiveUserNamePassword", "rpoOutOfReportList" , "selectRPOoutOfReport",
            "TestRunID", "TestCaseID", "TestRailUser", "TestRailPassword", "TestRailURL", "TestRailFailStatus", "TestRailSuccesStatus" })
    //ToDo пперенос параметров в Map для работы с ними
    public void checkRpoOutOfReport( String HIVE_URL, String HIVE_SCHEMA, String USER, String PASSWORD,String rpoOutOfReport, String selectRPOoutOfReport,
                                     String TestRunID, String TestCaseID, String TestRailUser, String TestRailPassword,
                                     String  TestRailURL, String TestRailFailStatus, String TestRailSuccesStatus ) {
                // ToDo check RPO out of the report

        coonectToCL(HIVE_URL+HIVE_SCHEMA, USER, PASSWORD);
        String countBarCodeOutOfReport;
        List<String> rpoOutOfReportList = new ArrayList(Arrays.asList(rpoOutOfReport.split(", ")));
        //System.out.println("RPO in the way amount "  + rpoInThwWayList.size());

        try {
            //
            for (int i = 0; i<rpoOutOfReportList.size(); i++ ) {
                //ToDo пеернос в StringBuilder построение запроса
                //System.out.println(selectRPOoutOfReport + "'" + rpoOutOfReportList.get(i) + "'" );
                ResultSet res = stmt.executeQuery(selectRPOoutOfReport + "'" + rpoOutOfReportList.get(i) + "' "  );
                //
                while (res.next()) {
                    countBarCodeOutOfReport = res.getString(1);
                    int countResults = Integer.parseInt(countBarCodeOutOfReport);
                    if ( countResults == 1 ) {
                        // System.out.println("Fail results break next RPO select");
                        // System.out.println(" countBarCodeInTheWay " + countBarCodeInTheWay);
                        // ToDo add fail results to TestRail
                        String Results = "RPO int the Object should not be in the balance_fact " + rpoOutOfReportList.get(i) ;
                        int StatusId = Integer.parseInt(TestRailFailStatus);
                        try {
                            addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                                    TestRailPassword, TestRailURL, Results);
                        } catch (APIException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        //ToDo fail results in test
                        Assert.assertEquals(countResults, null);
                    }
                    //addElementsToList(countRpoInTheWay, countBarCodeInTheWay);
                    //System.out.println("RPO out of Net " + RroBarCode );
                    //Long CalculateFull = fullSimpleRest + 201;
                    //System.out.println(CalculateFull);
                }
            }

        } catch (SQLException e) {
            String Results = e.getLocalizedMessage();
            int StatusId = Integer.parseInt(TestRailFailStatus);
            try {
                addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                        TestRailPassword, TestRailURL, Results);
            } catch (APIException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // ToDo add Fail Results to TestPlan
            e.printStackTrace();
            //ToDo add fail result due to get results

        }

        // Проверка РПО в Обьекте статус, в результирующих таблицах

        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //ToDo finally add success status results to TestRail
        //
        String Results = "The test has been passed successfully  " ;
        int StatusId = Integer.parseInt(TestRailSuccesStatus);
        try {
            addToTestRailResults (TestRunID, TestCaseID, StatusId, TestRailUser,
                    TestRailPassword, TestRailURL, Results);
        } catch (APIException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


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
        /*System.out.println(hiveURL);
        System.out.println(User);
        System.out.println(Password);*/
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            //log.error("Class not found", e);
            //ToDo fail results due to driver issue

        }
        try {
            con = DriverManager.getConnection(hiveURL, User, Password);
            /*System.out.println(con.toString());*/
            runInitializationQueries(con, stmt);
        } catch (SQLException e) {
            //ToDo fail results due to connection issue

            //System.out.println("Error while connection to " + hiveURL + "(user: " + User + ")" + e);
            //log.error("Error while connection to " + hiveURL + "(user: " + user + ")", e);
        }


        try {
            stmt = con.createStatement();
            /*System.out.println("Statement " + stmt.toString());*/
        } catch (SQLException ex) {
            System.out.println(ex);
            ex.printStackTrace();
            //ToDo fail results due to coonection issue


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
                //System.out.println("Executing: " + query);
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

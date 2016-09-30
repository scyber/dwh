package ru.russianpost.qa.actions;

import au.com.bytecode.opencsv.CSVReader;
import ru.russianpost.qa.env.Environment;

import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


/**
 * Created by gpiskunov on 26.08.2016.
 */
// Данный класс используется для формирования подключения к Hive, Vertica etc DataManagement
public class DataManagement {



    public static Statement connectHive (String URL, String User, String Password, String [] INIT_QUERIES ) throws SQLException {
        Statement stmnt = null;
        Connection con = null;
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //System.out.println(e);
            //log.error("Class not found", e);
            //ToDo fail results due to driver issue
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
            /*System.out.println("Statement " + stmt.toString());*/
        //} catch (SQLException ex) {
        //    System.out.println(ex);
        //    ex.printStackTrace();

        //}

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
    public void connectionClose (Statement stmnt) throws SQLException {
        stmnt.close();
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
        //System.out.println(hiveQuery);
        //System.out.println(INIT_QUERIES.toString());
        Statement stmnt = DataManagement.connectHive(URL, User, Password, INIT_QUERIES);
        ResultSet rs = null;
        try {
            rs = stmnt.executeQuery(hiveQuery);

        } catch (Exception ex ) {
            ex.printStackTrace();
        }

        return rs;
    }
    public static void executeHive (String URL, String User, String Password, String [] INIT_QUERIES, String hiveQuery) throws SQLException {
        Statement stmnt = DataManagement.connectHive(URL, User, Password, INIT_QUERIES);
        try {
            stmnt.execute(hiveQuery);

        } catch (Exception ex ) {
            ex.printStackTrace();
        }
    }
    public static List<String[]> getCsvRPOData (String testCaseID) {
        Array csvArray = null;
        FileReader inputData = null;
        String pathToCSVTestFile = Environment.PATH_TEST_DATA_FOLDER.toString() + testCaseID + ".csv";
        List<String[]> dataList = new ArrayList<>();

        try {
            inputData = new FileReader(pathToCSVTestFile);
            CSVReader csvData = new CSVReader(inputData, '\n');
            dataList = csvData.readAll();
        } catch (Exception ex ) {
            ex.printStackTrace();
        }
        /*System.out.println();
        for (Object[] item : rpoList  ) {
            System.out.println(Arrays.toString(item).replaceAll("[\\[\\]]", ""));
        }*/

        return dataList;
    }
     public static List builFailRPOList (String testCaseId, String targetFailTable) throws SQLException {
         List failRPOList = new ArrayList();
         //Выполняем select failRPO
         ResultSet rs = null;
         //System.out.println(Environment.selectFromFailTableQuery(testCaseId, targetFailTable));

        //try {


                    rs = DataManagement.executeHiveQuery(Environment.hiveCommonProperties.getProperty("hiveUrl").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserName").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserNamePassword").toString(),
                    Environment.getHiveInitQueries(Environment.hiveCommonProperties),
                    Environment.selectFromFailTableQuery(testCaseId, targetFailTable).toString());
                    //System.out.println(rs);
                    String failRpo;
                while (rs.next()) {
                    failRpo = rs.getString(1);

                    if (failRpo != null ) {
                        failRPOList.add(failRpo);
                    }
                }

       /* } catch ( Exception ex ) {
            ex.printStackTrace();
        }*/


         return failRPOList;
     }
     public static String checkRPOinTheObjectList (String testCaseID) throws SQLException {
         String failRpo = null;
         // To get The List from CSV
         List<String[]> rpoList = DataManagement.getCsvRPOData(testCaseID);
         // Select from hive Table if in the table

         for (String[] rpo : rpoList) {
             // Бегаем по списку - строим запрос для каждого значения
            String rpoForCheck = Arrays.toString(rpo).replaceAll("[\\[\\]]", "");
             //Строим строку запроса из РПО + параметров из properties
             StringBuilder selectQuery = new StringBuilder();
             selectQuery.append(Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"selectRpoBegin").toString())
                     .append("'")
                     .append(rpoForCheck)
                     .append("'")
                     .append(" ")
                    .append(Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"selectRpoTail").toString())
                    .append(" ");
             //System.out.println("select query " + selectQuery.toString());
             // Выполняем запрос для каждого нового запроса
            ResultSet rs = DataManagement.executeHiveQuery(Environment.hiveCommonProperties.getProperty("hiveUrl").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserName").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserNamePassword").toString(),
                    Environment.getHiveInitQueries(Environment.hiveCommonProperties),
                    selectQuery.toString());
             // Получаем ResultSet если он 0 - ошибка возвращаем РПО с ошибкой
             String countBarCode;
             int countResultsBarCode;
             while (rs.next()) {
                 countBarCode = rs.getString(1);
                 countResultsBarCode = Integer.parseInt(countBarCode);
                 if (countResultsBarCode != 1 ) {
                     failRpo = rpoForCheck;
                     break;
                 }
             }
             // выходим из цикла
         }
         // in case RPO from list return 0 - add to the failArray

         return failRpo ;
     }
    public static String checkRPOinTheWay (String testCaseID) throws SQLException {
        String failRpo = null;
        // To get The List from CSV
        List<String[]> rpoList = DataManagement.getCsvRPOData(testCaseID);
        // Select from hive Table if in the table


        for (String[] rpo : rpoList) {
            // Бегаем по списку - строим запрос для каждого значения
            String rpoForCheck = Arrays.toString(rpo).replaceAll("[\\[\\]]", "");
            //Строим строку запроса из РПО + параметров из properties
            StringBuilder selectQuery = new StringBuilder();
            selectQuery.append(Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"selectRpoBegin").toString())
                    .append("'")
                    .append(rpoForCheck)
                    .append("'")
                    .append(" ")
                    .append(Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"selectRpoTail").toString())
                    .append(" ");
            //System.out.println("select query " + selectQuery.toString());
            // Выполняем запрос для каждого нового запроса
            ResultSet rs = DataManagement.executeHiveQuery(Environment.hiveCommonProperties.getProperty("hiveUrl").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserName").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserNamePassword").toString(),
                    Environment.getHiveInitQueries(Environment.hiveCommonProperties),
                    selectQuery.toString());
            // Получаем ResultSet если он 0 - ошибка возвращаем РПО с ошибкой
            String countBarCode;
            int countResultsBarCode;
            while (rs.next()) {
                countBarCode = rs.getString(1);
                countResultsBarCode = Integer.parseInt(countBarCode);
                if (countResultsBarCode != 1 ) {
                    failRpo = rpoForCheck;
                    break;
                }
            }
            // выходим из цикла
        }
        // in case RPO from list return 0 - add to the failArray

        return failRpo ;
    }


    public static String checkRPOoutOfReport(String testCaseID) throws SQLException {
        String failRpo = "";
        // To get The List from CSV
        List<String[]> rpoList = DataManagement.getCsvRPOData(testCaseID);
        // Select from hive Table if in the table


        for (String[] rpo : rpoList) {
            // Бегаем по списку - строим запрос для каждого значения
            String rpoForCheck = Arrays.toString(rpo).replaceAll("[\\[\\]]", "");
            //Строим строку запроса из РПО + параметров из properties
            StringBuilder selectQuery = new StringBuilder();
            selectQuery.append(Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"selectRpoBegin").toString())
                    .append("'")
                    .append(rpoForCheck)
                    .append("'")
                    .append(" ")
                    .append(Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"selectRpoTail").toString())
                    .append(" ");
            //System.out.println("select query " + selectQuery.toString());
            // Выполняем запрос для каждого нового запроса
            ResultSet rs = DataManagement.executeHiveQuery(Environment.hiveCommonProperties.getProperty("hiveUrl").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserName").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserNamePassword").toString(),
                    Environment.getHiveInitQueries(Environment.hiveCommonProperties),
                    selectQuery.toString());
            // Получаем ResultSet если он 0 - ошибка возвращаем РПО с ошибкой
            //System.out.println("ResultSet " + rs.toString());
            String countBarCode;
            int countResultsBarCode;
            while (rs.next()) {
                countBarCode = rs.getString(1);
                countResultsBarCode = Integer.parseInt(countBarCode);
                //System.out.println("Count BarCode " + countResultsBarCode);
                if (countResultsBarCode != 0 ) {
                    failRpo = rpoForCheck;
                    break;
                }
            }
            // выходим из цикла
        }
        // in case RPO from list return 0 - add to the failArray

        return failRpo ;
    }
}

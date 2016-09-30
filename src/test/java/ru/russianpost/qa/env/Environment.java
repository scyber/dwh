package ru.russianpost.qa.env;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.hadoop.hive.ql.io.orc.OrcProto;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;
import org.testng.reporters.jq.Model;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by gpiskunov on 29.08.2016.
 */
public class Environment {
    public static final String env = "environment";
    public static String cur_environment = null;
    public static final String PATH_PROJECT_FOLDER = System.getProperty( "user.dir" );
    public static final String PATH_RESOURCES_FOLDER = PATH_PROJECT_FOLDER + "/src/resources";
    public static final String PATH_ENVIRONMENT_FOLDER = PATH_RESOURCES_FOLDER + "/env/";
    public static String currentEnvironment = CurrentSettings.getCurrentEnv();
    public static final String PATH_HIVE_FOLDER = PATH_ENVIRONMENT_FOLDER + currentEnvironment + "/hive/";
    public static final String PATH_TESTRAIL_FOLDER = PATH_ENVIRONMENT_FOLDER + currentEnvironment + "/testrail/";
    public static final String PATH_OOZIE_FOLDER = PATH_ENVIRONMENT_FOLDER + currentEnvironment + "/oozie/";
    public static final String PATH_TEST_DATA_FOLDER = PATH_ENVIRONMENT_FOLDER + currentEnvironment + "/data/";
    //private static final String PATH_TEST_CONFIG = PATH_ENVIRONMENT_FOLDER + "testConfig.properties";
    //private static final String PATH_PRE_CONFIG = PATH_ENVIRONMENT_FOLDER + "preprodConfig.properties";
    private static String PATH_CUR_ENV = PATH_ENVIRONMENT_FOLDER + "currentEnv.properties";

    private static String PATH_ENVIRONMENT = PATH_ENVIRONMENT_FOLDER + "Config.properties";
    public static String HIVE_COMMON_ENVIRONMENT = PATH_HIVE_FOLDER + "commonHive.properties";
    private static String OOZIE_COMMON_ENVIRONMENT = PATH_OOZIE_FOLDER + "commonOozie.properties";
    private static String TESTRAIL_COMMON_ENVIRONMENT = PATH_TESTRAIL_FOLDER + "commonTestRail.properties";


    public static Properties hiveCommonProperties = Environment.getProperties(HIVE_COMMON_ENVIRONMENT) ;
    public static Properties oozieCommonProperties = Environment.getProperties(OOZIE_COMMON_ENVIRONMENT);
    public static Properties testRailCommonProperties = Environment.getProperties(TESTRAIL_COMMON_ENVIRONMENT);


    //public static Properties hiveTestCaseProperties = getTestCaseProperties(Environment.PATH_HIVE_FOLDER, testCaseId);
    //public static Properties oozieProperties;

    /* read Environment Properties from currentEnv.properties config
    *
    * */
    //ToDo Добавить метод для построения путей к файлу properties конкретного теста
    public static String readPropertyFromEnvironment() {
        Properties prop = new Properties();
        InputStream inputStream = null;
        //String propertyValue = "";
        //System.out.println(PATH_CUR_ENV);
        try {
            inputStream = new FileInputStream( PATH_CUR_ENV );
            prop.load( inputStream );
            cur_environment = prop.getProperty (env);
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //System.out.println(" current environment " + cur_environment);
        return cur_environment;
    }
    public static String readPropertyFromConfig( String property, String fullConfigFilePath ) {
        Properties prop = new Properties();
        InputStream inputStream = null;
        String propertyValue = "";
        try {
            inputStream = new FileInputStream( fullConfigFilePath );
            prop.load( inputStream );
            propertyValue = prop.getProperty( property );
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return propertyValue;
    }
    // Реализуем метод построения пути к проперти на основании TestCaseCaseID + Test Environment
    //
    public static Properties getProperties(String pathToThePropertyFile){
        Properties prop = new Properties();
        InputStream inputStream = null;
        //String propertyValue = "";
        try {
            inputStream = new FileInputStream(pathToThePropertyFile);
            prop.load( inputStream );
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }
    public static String [] getHiveInitQueries(Properties hiveCommonProperties) {
        String[] INIT_QUERIES = (String[]) hiveCommonProperties.get("init_queries").toString().split(",");
        //System.out.println(INIT_QUERIES);
        return INIT_QUERIES;
    }
    public static String getTestCaseIdProperty (String pathToFolder, String testCaseID, String propName ) {
        String propValue ;
        String tailProperties = ".properties";
        String pathToThePropertisFile = pathToFolder + "C" + testCaseID + tailProperties;
        propValue = Environment.readPropertyFromConfig(propName, pathToThePropertisFile);
        return propValue;
    }
    public static Properties getTestCaseProperties (String pathToFolder, String testCaseID) {
        Properties prop = new Properties();
        String tailProperties = ".properties";
        String pathToThePropertisFile = pathToFolder + "C" + testCaseID + tailProperties;
        prop = Environment.getProperties(pathToThePropertisFile);
        return prop;
    }
    public static Properties mergeProperties (Properties... props) {
        Properties megedProperties = new Properties();
        for (Properties prop : props ) {
            megedProperties.putAll(prop);
        }
        return megedProperties;
    }
    public static String selectFromFailTableQuery (String testCaseID, String targetFailTable ) {
        StringBuilder hiveSelect = new StringBuilder() ;
        //ToDo getTargedTestDB
        //ToDo buildTargetFailTable

        hiveSelect.append("select * from ")
                  .append(Environment.getTestCaseProperties(Environment.PATH_OOZIE_FOLDER, testCaseID).getProperty("targetTestDB").toString())
                  .append(".")
                  .append(targetFailTable)
                  .append(" ");

        //System.out.println(hiveSelect.toString());
        return hiveSelect.toString();
    }


}

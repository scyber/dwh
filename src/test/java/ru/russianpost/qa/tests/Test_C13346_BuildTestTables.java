package ru.russianpost.qa.tests;

import org.apache.oozie.client.OozieClientException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import ru.russianpost.qa.actions.DataManagement;
import ru.russianpost.qa.actions.Oozie;
import ru.russianpost.qa.actions.TestRail;
import ru.russianpost.qa.env.Environment;

import java.util.Map;

import static org.apache.oozie.client.WorkflowJob.Status.RUNNING;

/**
 * Created by gpiskunov on 26.08.2016.
 */
public class Test_C13346_BuildTestTables {
    @Test(enabled = true)
    public void builTestCasesTables(ITestContext context) throws Exception {
            /* Читаем параметры теста в Map
            * */
        Map<String,String> testParameters = context.getCurrentXmlTest().getTestParameters();
        String testCaseID = testParameters.get("TestCaseID").toString();

        System.out.println("Run testCase " + testCaseID + " " + this.getClass().getName());
            String runOozieJobHDFSPath = "oozieJobBuildCheckTablesHDFSPath";
            try {
            Oozie.runOozieJob(testCaseID, runOozieJobHDFSPath );

            } catch (InterruptedException e) {

                e.printStackTrace();
            } catch (OozieClientException e) {
                e.printStackTrace();
            }

        TestRail.setTestRailResults(testCaseID, TestRail.setSuccessStatusId(), TestRail.setSuccessResults() );
        /*
        try {
            DataManagement.executeHive(Environment.hiveCommonProperties.getProperty("hiveUrl").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserName").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserNamePassword").toString(),
                    Environment.getHiveInitQueries(Environment.hiveCommonProperties),
                    Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"dropSchemaQuery"));

        } catch (Exception ex ) {

            String results = ex.getLocalizedMessage();
            TestRail.setTestRailResults(testCaseID, TestRail.setFailStatusId(), results );
            Assert.assertNotNull(ex);
            ex.printStackTrace();
        }
        //Create Hive testSchama
        try {
            DataManagement.executeHive(Environment.hiveCommonProperties.getProperty("hiveUrl").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserName").toString(),
                    Environment.hiveCommonProperties.getProperty("hiveUserNamePassword").toString(),
                    Environment.getHiveInitQueries(Environment.hiveCommonProperties),
                    Environment.getTestCaseIdProperty(Environment.PATH_HIVE_FOLDER, testCaseID,"createSchemaQuery"));
        } catch (Exception ex ) {
            String results = ex.getLocalizedMessage();
            TestRail.setTestRailResults(testCaseID, TestRail.setFailStatusId(), results );
            Assert.assertNotNull(ex);
            ex.printStackTrace();

        }
        TestRail.setTestRailResults(testCaseID, TestRail.setSuccessStatusId(), TestRail.setSuccessResults() );
        */

    }
}

package ru.russianpost.qa.tests;

import org.apache.oozie.client.OozieClientException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import ru.russianpost.qa.actions.DataManagement;
import ru.russianpost.qa.actions.Oozie;
import ru.russianpost.qa.actions.TestRail;
import ru.russianpost.qa.env.Environment;
import ru.yandex.qatools.allure.annotations.Step;

import java.util.List;
import java.util.Map;

/**
 * Created by gpiskunov on 26.08.2016.
 */
public class Test_C13345_CheckBalancesReport {

    @Test(enabled = true)
    public static void runTestCase13345(ITestContext context) {
        Map<String, String> testParameters = context.getCurrentXmlTest().getTestParameters();
        String testCaseID = testParameters.get("TestCaseID").toString();

        try {
            //Run Step 1
            //Test_C13345_CheckBalancesReport.buildReportBalancesTables(context);
            //Run Step 2
            Test_C13345_CheckBalancesReport.builTestCasesTables(context);
            //Run Step 3
            Test_C13345_CheckBalancesReport.getRpoInTheObject(context);
            //Run Step 4
            Test_C13345_CheckBalancesReport.getRpoInTheWay(context);
            //Run Step 5
            Test_C13345_CheckBalancesReport.getRpoOutOfReport(context);

            String results = "Test susccessfully passed";
            TestRail.setTestRailResults(testCaseID, TestRail.setSuccessStatusId(), TestRail.setSuccessResults());
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            String results = ex.getLocalizedMessage();
            TestRail.setTestRailResults(testCaseID, TestRail.setFailStatusId(), results );
            Assert.fail(ex.getLocalizedMessage());
        }

    }

    @Step("Построение отчёта через запуск Oozie Job")
    public static void buildReportBalancesTables(ITestContext context) throws Exception {
        Map<String, String> testParameters = context.getCurrentXmlTest().getTestParameters();
        String testCaseID = testParameters.get("TestCaseID").toString();
        //System.out.println("Run testCase " + testCaseID + " " + this.getClass().getName());
        String runOozieJobHDFSPath = "oozieJobBuildReportHDFSPath";
        Oozie.runOozieJob(testCaseID, runOozieJobHDFSPath);
    }

    @Step("Построение результирующих таблиц скриптами тестирования через запуск Oozie Job")
    //@Test(enabled = true)
    public static void builTestCasesTables(ITestContext context) throws Exception {

        Map<String, String> testParameters = context.getCurrentXmlTest().getTestParameters();
        String testCaseID = testParameters.get("TestCaseID").toString();
        //System.out.println("Run testCase " + testCaseID + " " + this.getClass().getName());
        String runOozieJobHDFSPath = "oozieJobBuildCheckTablesHDFSPath";
        Oozie.runOozieJob(testCaseID, runOozieJobHDFSPath);

        //TestRail.setTestRailResults(testCaseID, TestRail.setSuccessStatusId(), TestRail.setSuccessResults() );
    }

    @Step("Проверка результирующих таблиц построенных скритами тестирования ошибочно попадающих в Обьект  ")
    public static void getRpoInTheObject(ITestContext context) throws Exception {
        // Получаю TestCaseID
        Map<String, String> testParameters = context.getCurrentXmlTest().getTestParameters();
        String testCaseID = testParameters.get("TestCaseID").toString();
        String failRPO = null;

        List failRPOList;

        //System.out.println(Environment.selectFromFailTableQuery(testCaseID));
        //String targetInObjectTable =  Environment.getTestCaseIdProperty( Environment.PATH_OOZIE_FOLDER, testCaseID, "targetInObjectTable").toString();
        //System.out.println(Environment.selectFromFailTableQuery(testCaseID, targetInObjectTable ));
        failRPOList = DataManagement.builFailRPOList(testCaseID, Environment.getTestCaseIdProperty(Environment.PATH_OOZIE_FOLDER, testCaseID, "targetInObjectTable").toString());

        //System.out.println(failRPOList.toString());
        if (!failRPOList.isEmpty()) {
            // Тест возвращает ошибку по результатам обработки
            //Call TestRail add Results
            //Fail testResults
            String results = "The RpoFailList is not empty " + "Fail RPO in the Object List " + failRPOList;
            TestRail.setTestRailResults(testCaseID, TestRail.setFailStatusId(), results);
            throw new Exception(results);
            //Добавляем ошибку TestRaildjn
        }
    }

    @Step("Проверка результирующих таблиц построенных скриптами тестирвоания ошибочно попадающих в Пути  ")
    public static void getRpoInTheWay(ITestContext context) throws Exception {
        // Получаю TestCaseID
        Map<String, String> testParameters = context.getCurrentXmlTest().getTestParameters();
        String testCaseID = testParameters.get("TestCaseID").toString();
        List failRPOList;
        //System.out.println(Environment.selectFromFailTableQuery(testCaseID));
        failRPOList = DataManagement.builFailRPOList(testCaseID, Environment.getTestCaseIdProperty(Environment.PATH_OOZIE_FOLDER, testCaseID, "targetInWayTable").toString());
        //System.out.println(failRPOList.toString());
        if (!failRPOList.isEmpty()) {
            // Тест возвращает ошибку по результатам обработки
            //Call TestRail add Results
            //Fail testResults
            String results = "The RpoFailList is not empty " + "Fail RPO in the Way List " + failRPOList;
            TestRail.setTestRailResults(testCaseID, TestRail.setFailStatusId(), results);
            throw new Exception(results);
            //Добавляем ошибку TestRail
        }
    }

    @Step("Проверка результирующих таблиц построенных скриптами тестирования не попадающих в отчёт ")
    public static void getRpoOutOfReport(ITestContext context) throws Exception {
        // Получаю TestCaseID
        Map<String, String> testParameters = context.getCurrentXmlTest().getTestParameters();
        String testCaseID = testParameters.get("TestCaseID").toString();
        List failRPOList;
        //System.out.println(Environment.selectFromFailTableQuery(testCaseID));
        failRPOList = DataManagement.builFailRPOList(testCaseID, Environment.getTestCaseIdProperty(Environment.PATH_OOZIE_FOLDER, testCaseID, "targetOutReportTable").toString());
        //System.out.println(failRPOList.toString());
        if (!failRPOList.isEmpty()) {
            // Тест возвращает ошибку по результатам обработки
            //Call TestRail add Results
            //Fail testResults
            String results = "The RpoFailList is not empty " + "Fail RPO out of Report List " + failRPOList;
            TestRail.setTestRailResults(testCaseID, TestRail.setFailStatusId(), results);
            throw new Exception(results);
            //Добавляем ошибку TestRail
        } /*else {
            //Add TestRail Susccess Results
            String results = "Test susccessfully passed";
            TestRail.setTestRailResults(testCaseID, TestRail.setSuccessStatusId(), TestRail.setSuccessResults());
        }*/
    }

}

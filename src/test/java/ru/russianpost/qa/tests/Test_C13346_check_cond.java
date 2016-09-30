package ru.russianpost.qa.tests;

import org.testng.ITestContext;
import org.testng.annotations.Test;

/**
 * Created by gpiskunov on 29.09.2016.
 */
public class Test_C13346_check_cond {
    // Проверка условий, alure steps
    @Test(enabled = true)
    public void checkOozieStatusJobs(ITestContext context) throws Exception {

        Test_C13345_CheckBalancesReport.buildReportBalancesTables(context);


    }

}

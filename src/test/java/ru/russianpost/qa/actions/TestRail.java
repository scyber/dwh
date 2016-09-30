package ru.russianpost.qa.actions;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import ru.russianpost.qa.env.Environment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gpiskunov on 26.08.2016.
 */
public class TestRail {
    private static int statusId;
    private static String resultsTest;

    public static void setTestRailResults (String testCaseID, Integer statusId, String results /*,
                                    String TestCaseID, int StatusId,
                                    String TestRailUser, String TestRailPassword,
                                    String  TestRailURL, String Results*/) {


        APIClient client = new APIClient(Environment.testRailCommonProperties.getProperty("testRailURL").toString() );
        client.setUser(Environment.testRailCommonProperties.getProperty("testRailUser").toString());
        client.setPassword(Environment.testRailCommonProperties.getProperty("testRailPassword").toString());
        String testRunId = Environment.testRailCommonProperties.getProperty("testRailRunId").toString();
        Map data = new HashMap();
        data.put("status_id", new Integer(statusId) );
        data.put("comment", results  );
        try {
            JSONObject r = (JSONObject) client.sendPost("add_result_for_case/" + testRunId + "/" + testCaseID, data );
        } catch (IOException e ) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        }

    }
    // Пока не реализован
    public String getTestRailResults () {
        String statusTestCase = "" ;
        /*
        Внутренние статусы Тестового сценария
        Status int is 1 - success passed , 2 - Blocked , 3 - Untested can't be used , 4 - retest, 5 - failed
        * Пока не реализован - возвращаем статус теста в типе String
        * */

        return statusTestCase ;
    }
    public static int setSuccessStatusId() {
        return statusId = Integer.parseInt(Environment.testRailCommonProperties.getProperty("testRailSuccesStatus").toString());
    }
    public static int setFailStatusId(){
        return statusId = Integer.parseInt(Environment.testRailCommonProperties.getProperty("testRailFailStatus").toString());
    }
    public static String setSuccessResults(){

        return resultsTest = "The test has been passed susccessfully";
    }
}

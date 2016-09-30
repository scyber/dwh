package ru.russianpost.qa.actions;

import org.apache.hadoop.conf.Configuration;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;
import ru.russianpost.qa.env.Environment;

import java.util.Properties;

/**
 * Created by gpiskunov on 26.08.2016.
 */
public class Oozie {
    public static String runOozieJob (String testCaseID, String oozieJobPathName) throws Exception {
        String jobID = null ;
        Properties allOozieProperties = Environment.mergeProperties(Environment.oozieCommonProperties, Environment.getTestCaseProperties(Environment.PATH_OOZIE_FOLDER, testCaseID));

        OozieClient cl = new OozieClient(allOozieProperties.get("oozieUrl").toString());
        Properties runProperties = cl.createConfiguration();
        Configuration conf = new Configuration();
        runProperties.setProperty(cl.APP_PATH, allOozieProperties.getProperty(oozieJobPathName));
        runProperties.putAll(allOozieProperties);

                jobID = cl.run(runProperties);
            do {
                Thread.sleep(5000);
                cl.getJobInfo(jobID).getStatus();
            } while (cl.getJobInfo(jobID).getStatus() == WorkflowJob.Status.RUNNING);
        if(cl.getJobInfo(jobID).getStatus() == WorkflowJob.Status.KILLED || cl.getJobInfo(jobID).getStatus() == WorkflowJob.Status.FAILED) {
            String results = "Oozie job was failed";
            throw new Exception(results);
        }
        return String.valueOf(cl.getJobInfo(jobID).getStatus());
    }
    public static String getOozieJobStatus (OozieClient cl, String jobID) throws OozieClientException {

        return String.valueOf(cl.getJobInfo(jobID).getStatus());
    }


}

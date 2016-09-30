/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Properties;


public class OozieDemo {

    private static final Logger log = Logger.getLogger(OozieDemo.class);
    private static final String OOZIE_URL = "http://10.2.62.178:11000/oozie";
    Properties prop = null;

    //TODO: Move to oozie.properties file
    OozieClient wc = new OozieClient(OOZIE_URL);

    public OozieDemo() {
        prop = wc.createConfiguration();

    }


    @Test(enabled=true)
    public static void RunOozieOstatki() throws OozieClientException {
    String oozieUrl = "http://10.2.62.178:11000/oozie";
    String oozieJobHDFSPath = "hdfs://rpdc/user/hue/oozie/workspaces/hue-oozie-1471415274.03";
    //String jobIdOstatki = "0000026-160426142927939-oozie-oozi-W";
    String jobIdOstatki = null;
    OozieClient cl = new OozieClient(oozieUrl);


    Properties prop = cl.createConfiguration();

    Configuration conf = new Configuration();
    conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
    conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));
    try {
            FileSystem fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();
    }
    /*
        try {
            FileSystem fs = FileSystem.get(new URI("hdfs://hw-test-dc1:8020"), conf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    */

    prop.setProperty(cl.APP_PATH, oozieJobHDFSPath );
    prop.setProperty("jobTracker", "10.2.62.178:8050");
    prop.setProperty("queueName", "test");
    prop.setProperty("nameNode", "hdfs://rpdc");
    prop.setProperty("user.name", "hdfs");
    prop.setProperty("oozie.use.system.libpath", "true");
    prop.setProperty("security_enabled", "false");
    prop.setProperty("security.passwords.encryption.enabled", "false");
    prop.setProperty("oozie.script.param.targetBalancesDB", "test_tmp");
    prop.setProperty("oozie.script.param.date", "2015-12-31");
    prop.setProperty("oozie.script.param.dictsDB", "dicts");
     /*
    conf.setProperty("nameNode", "hdfs://hw-test-dc1:8020");
    conf.setProperty("oozie.libpath", "${nameNode}/user/hue/oozie/lib");

    */
    //conf.setProperty("oozie.wf.rerun.failnodes", "true");

        //System.out.println(cl.getServerConfiguration().toString());
        try {
            System.out.println("Getting Job ID Ostatki status");
            jobIdOstatki = cl.run(prop);

            System.out.println(cl.getJobInfo(jobIdOstatki).getStatus());
            //System.out.println(cl.getJobLog(jobIdOstatki));
            //ToDo надо реализовать перехвать ошибочно завершенных Jobs
            System.out.println (cl.getJobInfo(jobIdOstatki).getConf().toString());
            while (cl.getJobInfo(jobIdOstatki).getStatus() == WorkflowJob.Status.RUNNING) {
                //System.out.println("Job " +  cl.getJobLog(jobIdOstatki).toString());
                //System.out.println();
                //System.out.println (cl.getJobInfo(jobIdOstatki).getConf().toString());
            }
            if (cl.getBundleJobInfo(jobIdOstatki).getStatus().equals("KILLED")) {
                System.out.println("Job was not finished succesfully ");
            }

        } catch (OozieClientException e) {
            e.printStackTrace();
        }
        try {
            cl.getJobInfo(jobIdOstatki).getStatus();
            if (cl.getJobInfo(jobIdOstatki).getStatus() == WorkflowJob.Status.FAILED) {
                System.out.println("The Job has failed ");
            }
        } catch ( OozieClientException e) {
            e.printStackTrace();
        }


        System.out.println("Finish Oozie demo");


    }

}

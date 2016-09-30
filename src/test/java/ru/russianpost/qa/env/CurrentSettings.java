package ru.russianpost.qa.env;

/**
 * Created by gpiskunov on 30.08.2016.
 */
public class CurrentSettings {
    private static final String PATH_CURRENT_SETTINGS = Environment.PATH_ENVIRONMENT_FOLDER + "currentEnv.properties";
    private static final String propTail = ".properties";
    /**
     *
     */
    public static String getCurrentEnv() {
        return Environment.readPropertyFromConfig( "environment", PATH_CURRENT_SETTINGS );
    }
    public static String getCurrentMaintanProperties(String TestCaseID) {
        return Environment.readPropertyFromConfig("maintaince", Environment.PATH_ENVIRONMENT_FOLDER +
                CurrentSettings.PATH_CURRENT_SETTINGS + "C"+TestCaseID+propTail );
    }

}

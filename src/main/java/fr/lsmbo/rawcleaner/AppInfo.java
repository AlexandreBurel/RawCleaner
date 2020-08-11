package fr.lsmbo.rawcleaner;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInfo {
    protected static final Logger logger = LoggerFactory.getLogger(AppInfo.class);
    private static Boolean isInitialized = false;
    private static String appName = "Raw-Cleaner";
    private static String appSubName = "Clean raw files, but check them first !";
    private static String appVersion = "";
    private static String appDate = "";
    private static boolean isGuiVisible = false;

    private static void initialize() {
        if(!isInitialized) {
            try {
                final Properties properties = new Properties();
                properties.load(AppInfo.class.getClassLoader().getResourceAsStream("raw-cleaner.properties"));
                appName = properties.getProperty("name");
                appSubName = properties.getProperty("description");
                appVersion = properties.getProperty("version");
                appDate = properties.getProperty("build-date");
            } catch(Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
        isInitialized = true;
    }

    public static String getAppName() {
        initialize();
        return appName;
    }
    public static String getAppSubName() {
        initialize();
        return appSubName;
    }
    public static String getAppVersion() {
        initialize();
        return appVersion;
    }
    public static String getAppDate() {
        initialize();
        return appDate;
    }

    public static String getAppTitle() {
        initialize();
        return appName + " " + appVersion + " (" + appDate + ")";
    }

    public static String getLicence() {
        initialize();
        return 	"Copyright 2020 CNRS\n" +
                "Authors: Alexandre BUREL\n" +
                "Corresponding author: Alexandre BUREL\n" +
                "Email: alexandre.burel@unistra.fr\n" +
                "Affiliation: Laboratoire de Spectrométrie de Masse BioOrganique, Université de Strasbourg, CNRS, IPHC, UMR7178, F-67000 Strasbourg, France\n" +
                "Laboratory contact: Christine CARAPITO\n" +
                "Email: ccarapito@unistra.fr\n\n" +

                "This software is a computer program whose purpose is to verify files before deletion.\n\n " +

                "This software is governed by the CeCILL license under French law and " +
                "abiding by the rules of distribution of free software. You can use, " +
                "modify and/ or redistribute the software under the terms of the CeCILL " +
                "license as circulated by CEA, CNRS and INRIA at the following URL " +
                "http://www.cecill.info\n\n " +

                "As a counterpart to the access to the source code and rights to copy, " +
                "modify and redistribute granted by the license, users are provided only " +
                "with a limited warranty and the software's author, the holder of the " +
                "economic rights, and the successive licensors have only limited " +
                "liability. \n\n" +

                "In this respect, the user's attention is drawn to the risks associated " +
                "with loading, using, modifying and/or developing or reproducing the " +
                "software by the user in light of its specific status of free software, " +
                "that may mean that it is complicated to manipulate, and that also " +
                "therefore means that it is reserved for developers and experienced " +
                "professionals having in-depth computer knowledge. Users are therefore " +
                "encouraged to load and test the software's suitability as regards their " +
                "requirements in conditions enabling the security of their systems and/or " +
                "data to be ensured and, more generally, to use and operate it in the " +
                "same conditions as regards security.\n\n" +

                "The fact that you are presently reading this means that you have had " +
                "knowledge of the CeCILL license and that you accept its terms.";
    }

    public static void defineGuiAsVisible() {
        AppInfo.isGuiVisible = true;
    }
    public static boolean isGuiVisible() {
        return isGuiVisible;
    }
}

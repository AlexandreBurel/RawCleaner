package fr.lsmbo.rawcleaner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Global {

    protected static final Logger logger = LoggerFactory.getLogger(Global.class);

    private final static File settingsFile = new File(Objects.requireNonNull(Global.class.getClassLoader().getResource("settings.json")).getPath());
    public static Settings settings = new Settings();
    public static void loadSettings() throws Throwable {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(settingsFile));
        settings = gson.fromJson(reader, Settings.class);

        RAW_DATA_DIRECTORY = new File(settings.getRawDataDirectory());
        RAW_DATA_ARCHIVES = new File(settings.getArchiveDirectory());
        MS_ACCESS_PATH = new File(settings.getMsaccessPath());
        REPORTS_DIRECTORY = new File(settings.getDefaultReportDirectory());
        TEMP_DIRECTORY = new File(settings.getDefaultTempDirectory());
        RAW_DATA_EXTENSION = Arrays.stream(settings.getRawDataExtension().split(" ")).collect(Collectors.toList());
//        MACHINE_ID = settings.getMachineIdentifier();
        MACHINE_IDS = Arrays.stream(settings.getMachineIdentifiers().split(" ")).collect(Collectors.toList());
        AGE_LIMIT_IN_MONTH = settings.getMinimalNumberOfMonthsBeforeDeletion();
        MIN_UNARCHIVED_FILES = settings.getMinimalUnarchivedFilesBeforeWarning();

        if(settings.getMachineIdentifiers().equals("") || MACHINE_IDS.isEmpty()) throw new Exception("No machine identifier has been provided");
        // make sure the mandatory directories are available
        if(!RAW_DATA_DIRECTORY.exists() || !RAW_DATA_DIRECTORY.isDirectory()) throw new Exception("Data directory '"+RAW_DATA_DIRECTORY.getAbsolutePath()+"' is not available");
        if(!RAW_DATA_ARCHIVES.exists() || !RAW_DATA_ARCHIVES.isDirectory()) throw new Exception("Archive directory '"+RAW_DATA_ARCHIVES.getAbsolutePath()+"' is not available");
        // make sure the msaccess.exe file is available
        if(!MS_ACCESS_PATH.exists() || !MS_ACCESS_PATH.canExecute()) throw new Exception("MSAccess.exe is not available or not executable: "+MS_ACCESS_PATH.getAbsolutePath());
        // make sure the temporary directory exists, otherwise use the default temp directory
        if(!TEMP_DIRECTORY.exists() && !TEMP_DIRECTORY.mkdir()) TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
        // make sure the report directory exists, otherwise use the home directory
        if(!REPORTS_DIRECTORY.exists() && !REPORTS_DIRECTORY.mkdir()) REPORTS_DIRECTORY = new File(System.getProperty("user.home"));
    }

    public static File MS_ACCESS_PATH;
    public static File RAW_DATA_DIRECTORY;
    public static File RAW_DATA_ARCHIVES;
    public static File REPORTS_DIRECTORY;
    public static File TEMP_DIRECTORY;
    public static List<String> RAW_DATA_EXTENSION;
//    public static String MACHINE_ID;
    public static List<String> MACHINE_IDS;
    public static int AGE_LIMIT_IN_MONTH;
    public static int MIN_UNARCHIVED_FILES;

    public static Boolean IsRawData(File file) {
        boolean startsWithMachineId = false, endsWithRawDataExtension = false;
        int i = 0;
        while (!startsWithMachineId && i < MACHINE_IDS.size()) {
            if(file.getName().startsWith(MACHINE_IDS.get(i++))) startsWithMachineId = true;
        }
        i = 0;
        while (!endsWithRawDataExtension && i < RAW_DATA_EXTENSION.size()) {
            if(file.getName().endsWith(RAW_DATA_EXTENSION.get(i++))) endsWithRawDataExtension = true;
        }
        return startsWithMachineId && endsWithRawDataExtension;
//        return file.getName().startsWith(MACHINE_ID) && RAW_DATA_EXTENSION.stream().anyMatch(ext -> ext.equals(file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase()));
    }

    public final static Image ICON_ROOT = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("computer.png")));
    public final static Image ICON_FOLDER = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("folder.png")));
    public final static Image ICON_FOLDER_OPEN = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("folder-open.png")));
    public final static Image ICON_FILE = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("file.png")));
    public final static Image ICON_RAW = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("raw.png")));
    public final static Image ICON_RAW_GOOD = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("raw-good.png")));
    public final static Image ICON_RAW_WARNING = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("raw-warning.png")));
    public final static Image ICON_RAW_ERROR = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("raw-bad.png")));
    public final static Image ICON_RAW_CORRUPTED = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("raw-corrupted.png")));
    public final static Image ICON_YES = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("yes.png")));
    public final static Image ICON_NO = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("no.png")));
    public final static Image ICON_INTERROGATION = new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("interrogation.png")));

    public static final String[] MONTH_NAMES = {"janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre"};

    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMdd_HHmmss");
    public static  String formatDate(Long _date) { return formatDate(_date, true); }
    public static  String formatDate(Long _date, boolean humanReadable) { return humanReadable ? dateFormat.format(_date) : simpleDateFormat.format(_date); }
    public static  String simpleFormatDate(Long _date) { return simpleDateFormat2.format(_date); }

    private static final String[] units = new String[] { "octets", "ko", "Mo", "Go", "To" };
    public static String formatSize(Long _size) {
        if(_size <= 0) return "0";
        int digitGroups = (int) (Math.log10(_size)/Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(_size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getHostname() {
        String hostname = "Unknown";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Throwable t) {
            logger.error("Hostname can not be resolved", t);
        }
        return hostname;
    }

    public static String getUsername() {
        return System.getProperty("user.name");
    }
}

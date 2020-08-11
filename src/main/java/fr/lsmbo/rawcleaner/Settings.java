package fr.lsmbo.rawcleaner;

public class Settings {

    private String rawDataDirectory;
    private String archiveDirectory;
    private String msaccessPath;
    private String defaultReportDirectory;
    private String defaultTempDirectory;
    private String machineIdentifiers;
    private String rawDataExtension;
    private Integer minimalNumberOfMonthsBeforeDeletion;
    private Integer minimalUnarchivedFilesBeforeWarning;

    public Settings() {}

    public Settings(String rawDataDirectory, String archiveDirectory, String msaccessPath, String defaultReportDirectory, String defaultTempDirectory, String machineIdentifiers, String rawDataExtension, Integer minimalNumberOfMonthsBeforeDeletion, int minimalUnarchivedFilesBeforeWarning) {
        this.rawDataDirectory = rawDataDirectory;
        this.archiveDirectory = archiveDirectory;
        this.msaccessPath = msaccessPath;
        this.defaultReportDirectory = defaultReportDirectory;
        this.defaultTempDirectory = defaultTempDirectory;
        this.machineIdentifiers = machineIdentifiers;
        this.rawDataExtension = rawDataExtension;
        this.minimalNumberOfMonthsBeforeDeletion = minimalNumberOfMonthsBeforeDeletion;
        this.minimalUnarchivedFilesBeforeWarning = minimalUnarchivedFilesBeforeWarning;
    }

    public String getRawDataDirectory() {
        return rawDataDirectory;
    }

    public void setRawDataDirectory(String rawDataDirectory) {
        this.rawDataDirectory = rawDataDirectory;
    }

    public String getArchiveDirectory() {
        return archiveDirectory;
    }

    public void setArchiveDirectory(String archiveDirectory) {
        this.archiveDirectory = archiveDirectory;
    }

    public String getMsaccessPath() {
        return msaccessPath;
    }

    public void setMsaccessPath(String msaccessPath) {
        this.msaccessPath = msaccessPath;
    }

    public String getDefaultReportDirectory() {
        return defaultReportDirectory;
    }

    public void setDefaultReportDirectory(String defaultReportDirectory) {
        this.defaultReportDirectory = defaultReportDirectory;
    }

    public String getDefaultTempDirectory() {
        return defaultTempDirectory;
    }

    public void setDefaultTempDirectory(String defaultTempDirectory) {
        this.defaultTempDirectory = defaultTempDirectory;
    }

    public String getMachineIdentifiers() {
        return machineIdentifiers;
    }

    public void setMachineIdentifiers(String machineIdentifiers) {
        this.machineIdentifiers = machineIdentifiers;
    }

    public String getRawDataExtension() {
        return rawDataExtension;
    }

    public void setRawDataExtension(String rawDataExtension) {
        this.rawDataExtension = rawDataExtension;
    }

    public Integer getMinimalNumberOfMonthsBeforeDeletion() {
        return minimalNumberOfMonthsBeforeDeletion;
    }

    public void setMinimalNumberOfMonthsBeforeDeletion(Integer minimalNumberOfMonthsBeforeDeletion) {
        this.minimalNumberOfMonthsBeforeDeletion = minimalNumberOfMonthsBeforeDeletion;
    }

    public Integer getMinimalUnarchivedFilesBeforeWarning() {
        return minimalUnarchivedFilesBeforeWarning;
    }

    public void setMinimalUnarchivedFilesBeforeWarning(Integer minimalUnarchivedFilesBeforeWarning) {
        this.minimalUnarchivedFilesBeforeWarning = minimalUnarchivedFilesBeforeWarning;
    }

    public String toString() {
        return  "\nrawDataDirectory: " + rawDataDirectory +
                "\narchiveDirectory: " + archiveDirectory +
                "\nmsaccessPath: " + msaccessPath +
                "\ndefaultReportDirectory: " + defaultReportDirectory +
                "\ndefaultTempDirectory: " + defaultTempDirectory +
                "\nmachineIdentifier: " + machineIdentifiers +
                "\nrawDataExtension: " + rawDataExtension +
                "\nminimalNumberOfMonthsBeforeDeletion: " + minimalNumberOfMonthsBeforeDeletion +
                "\nminimalUnarchivedFilesBeforeWarning: " + minimalUnarchivedFilesBeforeWarning;
    }
}

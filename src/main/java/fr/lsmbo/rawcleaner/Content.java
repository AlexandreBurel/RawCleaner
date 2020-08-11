package fr.lsmbo.rawcleaner;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class Content {

    protected static final Logger logger = LoggerFactory.getLogger(Content.class);

    private final File file;
    private final CheckBox chkIsRawData = new CheckBox();
    private Long size = 0L; // useful if it's a directory
    private Long fileCount = 0L; // useful if it's a directory
    private FileTime creationDate;

    private final HashMap<File, Integer> archives = new HashMap<>();
    private Boolean isArchived = false;
    private Long archiveSize = 0L; // useful if it's a directory
    private Long archiveFileCount = 0L; // useful if it's a directory

    private final boolean isRoot;
    private boolean isOlderThanNMonths = false, isArchivedButNoArchiveFound = false, isLocked = false; // isCorrupted = false;
    private Boolean isCorrupted = null;

    public Content(File _file) {
        this(_file, Global.IsRawData(_file));
    }

    public Content(File _file, Boolean isRawData) {
        file = _file;
        chkIsRawData.setSelected(isRawData);
        isRoot = file.getAbsolutePath().equals(Global.RAW_DATA_DIRECTORY.getAbsolutePath());

        // compute size and nb file
        if(isRawData || file.isFile()) getTotalSize(file);

        // check attributes
        try {
            DosFileAttributes attributes = Files.readAttributes(file.toPath(), DosFileAttributes.class);
            // get creation date from attributes
            creationDate = attributes.creationTime();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(creationDate.toInstant(), ZoneId.systemDefault());
            long nbMonths = ChronoUnit.MONTHS.between(localDateTime, LocalDateTime.now());
            if(nbMonths > Global.AGE_LIMIT_IN_MONTH) isOlderThanNMonths = true;
            if(!isRoot && (!file.isDirectory() || isRawData) && !attributes.isArchive()) { // isArchive == true means that the file is ready to be archived, and therefore not archive yet
                // find archive
                naiveSearchForArchive(file);
                if(archives.size() == 0) isArchivedButNoArchiveFound = true;
                else isArchived = true;
            }
            if(attributes.isReadOnly()) isLocked = true;
        } catch (Throwable t) {
            // handle exception
            logger.error(t.getMessage(), t);
        }
    }

    private void getTotalSize(File root) {
        if(root.isDirectory()) {
            try {
                for (File file : Objects.requireNonNull(root.listFiles())) getTotalSize(file);
            } catch (NullPointerException ignored) {}
        } else {
            size += root.length();
            fileCount++;
        }
    }

    private void naiveSearchForArchive(File parent) {
        if(parent.isDirectory()) {
            for (File child : Objects.requireNonNull(parent.listFiles())) {
                naiveSearchForArchive(child);
            }
        } else {
            try {
                DosFileAttributes attributes = Files.readAttributes(parent.toPath(), DosFileAttributes.class);
                LocalDateTime localDateTime = LocalDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault());
                String path = Global.RAW_DATA_ARCHIVES.getAbsolutePath() + "/" + localDateTime.getYear() + "/" + Global.MONTH_NAMES[localDateTime.getMonthValue() - 1];
                File archive = new File(path, Global.RAW_DATA_DIRECTORY.toURI().relativize(parent.toURI()).getPath());
                if (archive.exists()) {
                    File archivePath = new File(path, file.getName());
                    int nb = archives.getOrDefault(archivePath, 0);
                    archives.put(archivePath, nb + 1);
                    archiveSize += archive.length();
                    archiveFileCount++;
                }
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    public void setIsCorrupted(boolean value) { isCorrupted = value; }

    public File getFile() { return file; }

    // getters for the GUI
    public String getName() {
        return isRoot ? file.getAbsolutePath() : file.getName();
    }
    public CheckBox getIsRawData() {
        if(isRoot) chkIsRawData.setVisible(false);
        return chkIsRawData;
    }
    public Long getLastModified() { return isRoot ? null : file.lastModified(); }
    public FileTime getCreationDate() { return isRoot ? null : creationDate; }
    public Long getSize() { return isRoot ? null : size; }
    public Long getFileCount() { return isRoot ? null : fileCount; }

    public Boolean getIsOlderThanNMonths() { return isOlderThanNMonths; }
    public String getArchive() { return isRoot ? null : archives.keySet().stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")); }

    public Boolean getIsUnlocked() { return isRoot ? null : !isLocked; }
    public Boolean getIsIdenticalToArchive() { return !archives.isEmpty() && size.equals(archiveSize) && fileCount.equals(archiveFileCount); }
    public Boolean isCorrupted() { return (isCorrupted != null && isCorrupted); }
    public Boolean getIsValid() { return isRoot || isCorrupted == null ? null : !isCorrupted; }
    public Boolean getIsArchivedButNoArchiveFound() { return isArchivedButNoArchiveFound; }
    public Boolean isCheckable() {
        if(isRoot || !chkIsRawData.isSelected()) return false;
        else {
            // raw files should always be checked, except for recent files
            LocalDateTime localDateTime = LocalDateTime.ofInstant(creationDate.toInstant(), ZoneId.systemDefault());
            return ChronoUnit.HOURS.between(localDateTime, LocalDateTime.now()) > 6;
        }
    }
    public boolean shouldBeArchivedButIsNot() {
        if(isRoot) return false;
        // no archives found
        boolean noArchivesFound = archives.size() == 0;
        // order than a week
        LocalDateTime localDateTime = LocalDateTime.ofInstant(creationDate.toInstant(), ZoneId.systemDefault());
        boolean isOlderThanAWeek = ChronoUnit.DAYS.between(localDateTime, LocalDateTime.now()) > 7;

        return isOlderThanAWeek && noArchivesFound;
    }

    private String formatRawArchiveDiff() {
        if(fileCount.equals(archiveFileCount) && size.equals(archiveSize))
            return "Raw data and archive are identical";

        String fileCountDiff = "";
        if(fileCount > archiveFileCount) fileCountDiff = (fileCount - archiveFileCount)+" file(s) less";
        else if(fileCount < archiveFileCount) fileCountDiff = (archiveFileCount - fileCount)+" file(s) more";

        String sizeDiff = "";
        if(size > archiveSize) sizeDiff = Global.formatSize(size - archiveSize)+" lighter";
        else if(size < archiveSize) sizeDiff = Global.formatSize(archiveSize-size)+" bigger";

        if(!fileCountDiff.equals("") && !sizeDiff.equals("")) return "Archive has "+fileCountDiff+" and is "+sizeDiff+" compared to the original data";
        else if(!fileCountDiff.equals("")) return "Archive has "+fileCountDiff+" compared to the original data";
        else return "Archive is "+sizeDiff+" compared to the original data";
    }

    private String gatherInformation() {
        // show differences between file and archive, if any
        String information = "";
        if(!isRoot) {
            if (!isOlderThanNMonths) information += "File is less than " + Global.AGE_LIMIT_IN_MONTH + " month and should not be deleted yet\n";
            if (!isArchived) information += "File is not archived and can't be deleted yet\n";
            if (isArchivedButNoArchiveFound) information += "File is tagged as archived, but no archive have been found\n";
//            if (isArchived && !getIsIdenticalToArchive()) information += "File and archive are not exactly identical: archive has " + archiveFileCount + " files and a total size of " + formatSize(archiveSize) + "\n";
            if (isArchived && !getIsIdenticalToArchive()) information += "File and archive are not exactly identical: " + formatRawArchiveDiff() + "\n";
            if (isLocked) information += "File is tagged as read-only and can't be deleted\n";
            if(chkIsRawData.isSelected() && !isCheckable()) information += "Raw file is not checked because it is too recent\n";
            if (isCorrupted != null && isCorrupted) information += "File appears to be corrupted, the archive will be corrupted too\n";
            if (archives.size() > 1) information += "File is archived in multiple directories: \n\t" + archives.keySet().stream().map(File::getAbsolutePath).collect(Collectors.joining("\n\t")) + "\n";
        }
        return information;
    }
    public Label getInformation() {
        // show differences between file and archive, if any
        if(isRoot) {
            return new Label("");
        } else {
            String information = gatherInformation();
            Label label = new Label(information);
            if (information.split("\\n").length > 1) label.setText("See tooltip for complete information...");
            Window.addTooltip(information, label);
            return label;
        }
    }
    public Boolean isDeletable() {
        return (isOlderThanNMonths && isArchived && !isLocked && !isArchivedButNoArchiveFound);
    }

    public Boolean getIsDeletable() { return isRoot ? null : isDeletable(); }

    private String formatSize(Long _size) {
        if(file.isDirectory() && !chkIsRawData.isSelected()) return "";
        return Global.formatSize(_size);
    }

    public String[] export() {
        String isValid = "";
        if(isCorrupted != null) isValid = isCorrupted ? "No" : "Yes";
        return new String[] {
                file.getAbsolutePath(), // File path
                chkIsRawData.isSelected() ? "Yes" : "No", // Is RAW data
                Global.formatDate(creationDate.toMillis()), // Creation date
                Global.formatDate(file.lastModified()), // last modified
                formatSize(size), // Size
                ""+size, // Size (octets)
                ""+fileCount, // File count
                archives.keySet().stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")), // Archive path
                isLocked ? "No" : "Yes", // Is unlocked
                isValid, // Is valid
                isDeletable() ? "Yes" : "No", // Is checked for deletion
                gatherInformation() // Information
        };
    }
    public String toString() {
        String[] data = export();
        data[7] = "'" + data[7].replaceAll("\n", "'\n'") + "'";
        data[11] = "'" + data[11].replaceAll("\n", "'\n'") + "'";
        return String.join("\t", data);
    }

}
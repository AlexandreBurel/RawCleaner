package fr.lsmbo.rawcleaner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class MsAccess {

    protected static final Logger logger = LoggerFactory.getLogger(MsAccess.class);
    private static String currentFilePath = null;

    public static HashMap<String, String> checkFiles(List<String> filePaths) {
        HashMap<String, String> metadataPerFile = new HashMap<>();
        try {
            logger.info("Preparing input files for MsAccess");
            // prepare file list
            File file = File.createTempFile("FileList", ".txt", Global.TEMP_DIRECTORY);
            file.deleteOnExit();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String path : filePaths) {
                writer.write(path + '\n');
            }
            writer.close();

            // extract version and build date so it wont have to be done later
            extractVersionAndBuildDate();
//            logger.info("Waiting, just for fun");
//            Thread.sleep(2500);

            logger.info("Checking raw files...");
            // call MsAccess and read the output
            new ProcessExecutor()
                    .command(Global.MS_ACCESS_PATH.getAbsolutePath(), "-f", file.getAbsolutePath(), "-o", Global.TEMP_DIRECTORY.getAbsolutePath(), "-x", "metadata", "--verbose")
                    .directory(Global.TEMP_DIRECTORY)
                    .redirectError(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            if (line.contains("Analyzing file")) {
                                // search for the corresponding RawFile
                                // TODO what if find returns null ?
                                currentFilePath = new File(line.replaceFirst(".*Analyzing file: ", "")).getAbsolutePath();
                                metadataPerFile.put(currentFilePath, "No metadata found, corrupt RAW file ?");
                            } else if (line.contains("[MetadataReporter] Writing file")) {
                                // open file and put its content into the array
                                File file = new File(line.replaceFirst(".*Writing file ", ""));
                                file.deleteOnExit();
                                try {
                                    metadataPerFile.replace(currentFilePath, new String(Files.readAllBytes(file.toPath())));
                                } catch (Throwable t) {
                                    logger.error(t.getMessage(), t);
                                }
                            } else if (line.contains("Corrupt RAW file"))
                                metadataPerFile.replace(currentFilePath, "Corrupt RAW file");
                        }
                    })
                    .execute(); // use .start().getFuture() to start the process in background and avoid blocking the whole thing

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return metadataPerFile;
    }

    private static String version, buildDate;
    private static void extractVersionAndBuildDate() {
        try {
            new ProcessExecutor().command(Global.MS_ACCESS_PATH.getAbsolutePath(), "--help").directory(Global.TEMP_DIRECTORY).redirectError(new LogOutputStream() {
                        @Override
                        protected void processLine(String line) {
                            if (line.contains("ProteoWizard release")) version = line.replaceAll("ProteoWizard release: ", "");
                            else if (line.contains("Build date: ")) buildDate = line.replaceAll("Build date: ", "");
                        }
                    })
                    .execute();
            logger.info("MS Access version: "+version);
            logger.info("MS Access build date: "+buildDate);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }
    public static String getVersion() {
        if(version == null) extractVersionAndBuildDate();
        return version;
    }
    public static String getBuildDate() {
        if(buildDate == null) extractVersionAndBuildDate();
        return buildDate;
    }

}

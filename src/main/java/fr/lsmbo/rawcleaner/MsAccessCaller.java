//package fr.lsmbo.rawcleaner;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.nio.file.Files;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.Callable;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.zeroturnaround.exec.ProcessExecutor;
//import org.zeroturnaround.exec.ProcessResult;
//import org.zeroturnaround.exec.stream.LogOutputStream;
//
//public class MsAccessCaller implements Runnable {
//
//    protected static final Logger logger = LoggerFactory.getLogger(MsAccessCaller.class);
//
//    private String currentFilePath = null;
//    private final List<String> filePaths;
//    private final HashMap<String, String> metadataPerFile = new HashMap<>();
//
//    public MsAccessCaller(List<String> _filePaths) {
//        filePaths = _filePaths;
//    }
//
//    public HashMap<String, String> getResults() {
//        return metadataPerFile;
//    }
//
//    public void run() {
////        HashMap<String, String> metadataPerFile = new HashMap<>();
//        try {
//            logger.info("Preparing input files for MsAccess");
//            // prepare file list
//            File file = File.createTempFile("FileList", ".txt", Global.TEMP_DIRECTORY);
//            file.deleteOnExit();
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//            for(String path: filePaths) {
//                writer.write(path+'\n');
//            }
//            writer.close();
//
//            logger.info("Waiting, just for fun");
//            Thread.sleep(2500);
//
//            logger.info("Checking raw files...");
//            // call MsAccess and read the output
//            ProcessResult result = new ProcessExecutor()
//                    .command(Global.MSACCESS_PATH.getAbsolutePath(), "-f", file.getAbsolutePath(), "-o", Global.TEMP_DIRECTORY.getAbsolutePath(), "-x", "metadata", "--verbose")
//                    .directory(Global.TEMP_DIRECTORY)
//                    .redirectError(new LogOutputStream() {
//                        @Override
//                        protected void processLine(String line) {
//                            //                            logger.debug(line);
//                            if(line.contains("Analyzing file")) {
//                                // search for the corresponding RawFile
//                                // TODO what if find returns null ?
//                                currentFilePath = new File(line.replaceFirst(".*Analyzing file: ", "")).getAbsolutePath();
//                                metadataPerFile.put(currentFilePath, "No metadata found, corrupt RAW file ?");
//                            } else if(line.contains("[MetadataReporter] Writing file")) {
//                                // open file and put its content into the array
//                                File file = new File(line.replaceFirst(".*Writing file ", ""));
//                                file.deleteOnExit();
//                                try {
//                                    metadataPerFile.replace(currentFilePath, new String(Files.readAllBytes(file.toPath())));
//                                } catch (Throwable t) {
//                                    logger.error(t.getMessage(), t);
//                                }
//                            } else if(line.contains("Corrupt RAW file")) metadataPerFile.replace(currentFilePath, "Corrupt RAW file");
//                        }
//                    })
//                    .execute(); // use .start().getFuture() to start the process in background and avoid blocking the whole thing
//
////            return metadataPerFile;
//
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
////        return null;
//    }
//}

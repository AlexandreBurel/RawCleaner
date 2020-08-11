package fr.lsmbo.rawcleaner;

import com.sun.jna.platform.FileUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

public class Window {

    protected static final Logger logger = LoggerFactory.getLogger(Window.class);

    @FXML
    StackPane stackPane;
    @FXML
    MenuBar menuBar;
    @FXML
    MenuItem mnuRefresh;
    @FXML
    MenuItem mnuExport;
    @FXML
    MenuItem mnuExportMissingFiles;
    @FXML
    MenuItem mnuClose;
    @FXML
    MenuItem mnuCheck;
    @FXML
    MenuItem mnuSelectArchivedData;
    @FXML
    MenuItem mnuDeleteSelection;
    @FXML
    MenuItem mnuAbout;
    @FXML
    TreeTableView<Content> table;
    @FXML
    TreeTableColumn<Content, String> tbcPath;
    @FXML
    TreeTableColumn<Content, Boolean> tbcIsRawData;
    @FXML
    TreeTableColumn<Content, FileTime> tbcCreationDate;
    @FXML
    TreeTableColumn<Content, Long> tbcLastModified;
    @FXML
    TreeTableColumn<Content, Long> tbcSize;
    @FXML
    TreeTableColumn<Content, Long> tbcFileCount;
    @FXML
    TreeTableColumn<Content, String> tbcArchive;
    @FXML
    TreeTableColumn<Content, Boolean> tbcIsUnlocked;
    @FXML
    TreeTableColumn<Content, Boolean> tbcIsValid;
    @FXML
    TreeTableColumn<Content, Boolean> tbcCheckForDeletion; // ok if both are not corrupted, both have same size, both have same number of items inside
    @FXML
    TreeTableColumn<Content, String> tbcInformation; // details why the file can or can not be deleted

    protected static Stage stage;
    private HashMap<String, TreeItem<Content>> localFiles; // always use lower case
    private boolean checkingHasBeenDone;

    @FXML
    private void initialize() {

        localFiles = new HashMap<>();
        checkingHasBeenDone = false;

        tbcPath.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        tbcIsRawData.setCellValueFactory(new TreeItemPropertyValueFactory<>("isRawData"));
        tbcCreationDate.setCellValueFactory(new TreeItemPropertyValueFactory<>("creationDate"));
        tbcLastModified.setCellValueFactory(new TreeItemPropertyValueFactory<>("lastModified"));
        tbcSize.setCellValueFactory(new TreeItemPropertyValueFactory<>("size"));
        tbcFileCount.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileCount"));
        tbcArchive.setCellValueFactory(new TreeItemPropertyValueFactory<>("archive"));
        tbcIsUnlocked.setCellValueFactory(new TreeItemPropertyValueFactory<>("isUnlocked"));
        tbcIsValid.setCellValueFactory(new TreeItemPropertyValueFactory<>("isValid"));
        tbcCheckForDeletion.setCellValueFactory(new TreeItemPropertyValueFactory<>("isDeletable"));
        tbcInformation.setCellValueFactory(new TreeItemPropertyValueFactory<>("information"));

        // set formatting methods
        tbcCreationDate.setCellFactory(column -> new TreeTableCell<Content, FileTime>() {
            @Override
            protected void updateItem(FileTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Global.formatDate(item.toMillis()));
            }
        });
        tbcLastModified.setCellFactory(column -> new TreeTableCell<Content, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Global.formatDate(item));
            }
        });
        tbcSize.setCellFactory(column -> new TreeTableCell<Content, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Global.formatSize(item));
            }
        });
        tbcIsUnlocked.setCellFactory(column -> new TreeTableCell<Content, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if(!empty && item != null) setGraphic(new ImageView(item ? Global.ICON_YES : Global.ICON_NO));
                else setGraphic(null);
            }
        });
        tbcIsValid.setCellFactory(column -> new TreeTableCell<Content, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null);
                if(checkingHasBeenDone) {
                    if (!empty && item != null) setGraphic(new ImageView(item ? Global.ICON_YES : Global.ICON_NO));
                } else {
                    setGraphic(new ImageView(Global.ICON_INTERROGATION));
                    addTooltip("Check raw files status to get this information", this);
                }
            }
        });
        tbcCheckForDeletion.setCellFactory(column -> new TreeTableCell<Content, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if(!empty && item != null) setGraphic(new ImageView(item ? Global.ICON_YES : Global.ICON_NO));
                else setGraphic(null);
            }
        });

        // search the directories for files
        populateFiles();

        Platform.runLater(this::checkMissingData);
    }

    public static void addTooltip(String tooltipText, Node node) {
        Tooltip tooltip = new Tooltip(tooltipText);
        // make text bigger and more readable
        tooltip.setStyle("-fx-font-size: 14");
        // +15 to avoid screen flicker due to tooltip and mouse cursor
        node.setOnMouseMoved(event -> tooltip.show(node, event.getScreenX(), event.getScreenY() + 15));
        node.setOnMouseExited(event -> tooltip.hide());
    }

    // TODO set as Future
    private void populateFiles() {
        // TODO add a progress icon
        table.setRoot(new TreeItem<>(new Content(Global.RAW_DATA_DIRECTORY)));
        table.getRoot().setGraphic(new ImageView(Global.ICON_ROOT));
        table.getRoot().setExpanded(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logger.info("Parsing raw files...");
        loop(table.getRoot());
        logger.info(localFiles.size() + " raw files have been found");

    }

    private void checkMissingData() {
        List<Content> suspiciousFiles = localFiles.values().stream().filter(item -> item.getValue().shouldBeArchivedButIsNot()).map(TreeItem::getValue).collect(Collectors.toList());
        if(suspiciousFiles.size() > Global.MIN_UNARCHIVED_FILES) {
            showAlertDialog(Alert.AlertType.WARNING, ""+suspiciousFiles.size()+" files are older than a week, but not found in the archives", "You should generate a 'Missing data' report ("+mnuExportMissingFiles.getAccelerator().toString()+") and send it to the administrators.");
        }
    }

    private void loop(TreeItem<Content> root) {
        try {
            for (File item : Objects.requireNonNull(root.getValue().getFile().listFiles())) {
                Content content = new Content(item);
                TreeItem<Content> entry = new TreeItem<>(content);
                if(content.getIsRawData().isSelected()) {
                    entry.setGraphic(new ImageView(Global.ICON_RAW));
                } else if(item.isDirectory()) {
                    entry.setGraphic(new ImageView(Global.ICON_FOLDER_OPEN));
                    entry.expandedProperty().addListener((observable, oldValue, newValue) ->
                            entry.setGraphic(new ImageView(entry.isExpanded() ? Global.ICON_FOLDER_OPEN : Global.ICON_FOLDER)));
                    entry.setExpanded(true);
                    loop(entry);
                } else {
                    entry.setGraphic(new ImageView(Global.ICON_FILE));
                }

                localFiles.put(item.getAbsolutePath().toLowerCase(), entry);
                root.getChildren().add(entry);
            }
        } catch (NullPointerException ignored) {}
    }

    @FXML
    private void checkMetadata() {
        // FIXME the progress indicator and the disabling of the nodes are not visible
        // show the progress bar
        toggleProgressIndicator();
        // run as a future
        Platform.runLater(() -> {
            logger.info("Start checking in a new thread");
//            MsAccessCaller mac = new MsAccessCaller(localFiles.values().stream()
//                    .filter(data -> data.getValue().getIsRawData().isSelected())
//                    .map(data -> data.getValue().getFile().getAbsolutePath())
//                    .collect(Collectors.toList()));
//            mac.call();

//            HashMap<String, String> metadataPerLocalFile = new HashMap<>();
//            try {
//                Thread.sleep(1000);
//            } catch (Throwable ignored) {}
//            try {
            HashMap<String, String> metadataPerLocalFile = MsAccess.checkFiles(localFiles.values().stream()
//                    .filter(data -> data.getValue().getIsRawData().isSelected())
                    .filter(data -> data.getValue().isCheckable())
                    .map(data -> data.getValue().getFile().getAbsolutePath())
                    .collect(Collectors.toList()));
//                MsAccessCaller mac = new MsAccessCaller(localFiles.values().stream()
//                    .filter(data -> data.getValue().getIsRawData().isSelected())
//                    .map(data -> data.getValue().getFile().getAbsolutePath())
//                    .collect(Collectors.toList()));
//                ExecutorService executor = Executors.newSingleThreadExecutor();
////                Future<HashMap<String, String>> result = executor.submit(mac);
//                Future result = executor.submit(mac);
//                while(!result.isDone()) { Thread.sleep(100); }
////                HashMap<String, String> metadataPerLocalFile = result.get();
//                HashMap<String, String> metadataPerLocalFile = mac.getResults();
//                executor.shutdown();
//            while(!MsAccess.isDone()) { Thread.sleep(100); }
            // update Data objects with metadata
            metadataPerLocalFile.forEach((filePath, metadata) -> localFiles.get(filePath.toLowerCase()).getValue()
                    .setIsCorrupted((metadata == null || metadata.toLowerCase().contains("corrupted raw file") || metadata.toLowerCase().contains("corrupt raw file"))));
//            } catch (Throwable ignored) {}

            // refresh table
            checkingHasBeenDone = true;
            toggleProgressIndicator();
            updateTreeItems(table.getRoot());
            table.refresh();

            // hide the progress bar
            logger.info("Checking is done");
        });
    }

    @FXML
    private void test() {
//        toggleProgressIndicator();
        logger.debug(Global.settings.toString());
    }

    private void toggleProgressIndicator() {
        if(stackPane.getChildren().size() == 1) {
            ProgressIndicator p = new ProgressIndicator();
            p.setMinSize(100, 100);
            p.setPrefSize(100, 100);
            p.setMaxSize(100, 100);
            stackPane.getChildren().add(p);
            stackPane.getChildren().get(1).toFront();
            menuBar.setDisable(true);
            table.setDisable(true);
        } else {
            stackPane.getChildren().remove(1);
            menuBar.setDisable(false);
            table.setDisable(false);
        }
    }

    private void updateTreeItems(TreeItem<Content> item) {
        for(TreeItem<Content> child: item.getChildren()) {
            // update icons
            Content content = child.getValue();
            if(content.getIsRawData().isSelected()) {
                if(!content.isCheckable()) child.setGraphic(new ImageView(Global.ICON_RAW_WARNING));
                else if(content.isCorrupted()) child.setGraphic(new ImageView(Global.ICON_RAW_CORRUPTED));
                else if(content.isDeletable()) {
                    if(content.getIsIdenticalToArchive()) child.setGraphic(new ImageView(Global.ICON_RAW_GOOD));
                    else child.setGraphic(new ImageView(Global.ICON_RAW_WARNING));
                }
                else child.setGraphic(new ImageView(Global.ICON_RAW_ERROR));
            } else content.setIsCorrupted(false);
            // set labels width
            content.getInformation().minWidthProperty().bind(tbcInformation.widthProperty());
            // recursive call
            if(child.getChildren().size() > 0) updateTreeItems(child);
        }
    }

    @FXML
    private void exportData() {
        try {
            File saveAs = openFileChooser();
            if (saveAs != null) {
                List<String[]> data = localFiles.values().stream().map(item -> item.getValue().export()).collect(Collectors.toList());
                Export.toXLSX(data, saveAs, "User report");
            }
        } catch (Throwable t) {
            showAlertDialog(Alert.AlertType.ERROR, "Error while exporting data", t.getMessage());
            logger.error(t.getMessage(), t);
        }
    }

    @FXML
    private void exportMissingData() {
        try {
            File saveAs = openFileChooser();
            if (saveAs != null) {
                List<String[]> data = localFiles.values().stream().filter(item -> item.getValue().shouldBeArchivedButIsNot()).map(item -> item.getValue().export()).collect(Collectors.toList());
                Export.toXLSX(data, saveAs, "Data not archived");
            }
        } catch (Throwable t) {
            showAlertDialog(Alert.AlertType.ERROR, "Error while exporting data", t.getMessage());
            logger.error(t.getMessage(), t);
        }
    }

    private File openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As...");
        fileChooser.setInitialDirectory(Global.REPORTS_DIRECTORY);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("All types", "*.*")
        );
        fileChooser.setInitialFileName(Export.getDefaultFile().getName());
        File outputFile = fileChooser.showSaveDialog(stage);
        // outputFile may be null if user canceled the file chooser
        if(outputFile != null && !outputFile.getName().endsWith(".xlsx")) outputFile = new File(outputFile.getAbsolutePath() + ".xlsx");
        return outputFile;
    }

    @FXML
    private void selectDeletableData() {
        logger.info("Selecting only the deletable entries");
        table.getSelectionModel().clearSelection();
        localFiles.values().stream().filter(node -> node.getValue().isDeletable()).forEach(node -> table.getSelectionModel().select(node));
    }

    @FXML
    private void deleteSelectedData() {
        // add a warning popup
        Optional<ButtonType> result = showAlertDialog(Alert.AlertType.CONFIRMATION, "Deleting "+table.getSelectionModel().getSelectedItems().size()+" file(s)", "Are you certain you want to delete these files ? The files will be put in the recycle bin.");
        if(result.orElse(ButtonType.CANCEL).equals(ButtonType.OK)) {
            // add a progress bar ?
            boolean startDeletion = false;
            try {

                // export the list of deleted files
                List<String[]> data = table.getSelectionModel().getSelectedItems().stream().map(item -> item.getValue().export()).collect(Collectors.toList());
                Export.toXLSX(data, Export.getDefaultFile(), "Automatic report before deleting files");

                // proceed to the deletion
                File[] fileList = table.getSelectionModel().getSelectedItems().stream().map(content -> content.getValue().getFile()).toArray(File[]::new);
                logger.info("Deleting files "+ Arrays.toString(fileList));
                FileUtils fileUtils = FileUtils.getInstance();
                startDeletion = true;
                fileUtils.moveToTrash(fileList);

                // after deletion, refresh the view
                logger.info("Refreshing the view");
                initialize();

            } catch (Throwable t) {
                if(startDeletion) showAlertDialog(Alert.AlertType.ERROR, "Error while deleting RAW files", t.getMessage());
                else showAlertDialog(Alert.AlertType.ERROR, "Error before deleting RAW files", "An error during the generation of the report has occurred, no files have been deleted.\n"+t.getMessage());
                logger.error(t.getMessage(), t);
            }
        }
    }

    private Optional<ButtonType> showAlertDialog(Alert.AlertType type, String header, String text) {
        Alert alert = new Alert(type);
        alert.setTitle("MS-Decoder");
        alert.setHeaderText(header);
        alert.setContentText(text);
        return alert.showAndWait();
    }

    public void setStage(Stage primaryStage) {
        stage = primaryStage;
        stage.setMaximized(true);
    }

    @FXML
    private void changeTableConstraints() {
        if(table.getColumnResizePolicy().equals(TreeTableView.CONSTRAINED_RESIZE_POLICY))
            table.setColumnResizePolicy(TreeTableView.UNCONSTRAINED_RESIZE_POLICY);
        else
            table.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void exitListener() {
        if(showAlertDialog(Alert.AlertType.CONFIRMATION, "Quit "+AppInfo.getAppName()+" ?", " Are you sure want to quit ?").orElse(ButtonType.CANCEL).equals(ButtonType.OK)) {
            beforeClosing();
            stage.close();
        }
    }

    private void beforeClosing() {
        try {
            Platform.exit();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }
}

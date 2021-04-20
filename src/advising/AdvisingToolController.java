/**
 * Course: SE 2800
 * Section 41
 * Dr. Jonathon Magana
 * Advising Tool
 * Created by: Derek Gauger, Kian Dettlaff, Roberto Garcia, and Tyler Faulkner
 * March 18th, 2021
 */

package advising;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Controller for the Advising Tool GUI using FXML
 */
public class AdvisingToolController {
    private final List<MenuItem> buttonFeatures = new LinkedList<>();

    /**
     * Alternate names for terms that could be used when a user searches
     */
    private List[] termAltNames = {
            Arrays.asList("first", "fall", "first quarter", "fall quarter", "1", "quarter 1"),
            Arrays.asList("second", "second quarter", "winter", "winter quarter", "2", "quarter 2"),
            Arrays.asList("third", "third quarter", "3", "spring", "spring quarter", "quarter 3")
    };

    private CourseManager manager;
    private File transcriptFile;
    private PDFManager pdfManager;

    @FXML
    ListView listView, detailView;

    @FXML
    TextField searchBar;

    @FXML
    MenuButton optionBox;

    @FXML
    Button recommendButton, feature2Button, feature3Button, feature4Button;

    @FXML
    ContextMenu courseMenu;

    @FXML
    private void initialize() {
        manager = new CourseManager();
        pdfManager = new PDFManager(manager);
        populateContextMenu();
        //Creating and setting each new feature/menu item
        //Course By Term
        MenuItem courseByTerm = new MenuItem("Courses By Term");
        courseByTerm.setOnAction(actionEvent -> showCourseByTerm());
        buttonFeatures.add(courseByTerm);


        //Attach to menuButton
        optionBox.getItems().addAll(buttonFeatures);
    }

    @FXML
    private void listCSCourses() {
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCSTrack());
    }

    @FXML
    private void listSECourses() {
        listView.getItems().clear();
        listView.getItems().addAll(manager.getSETrack());
    }

    @FXML
    private void listCourseToDate() {
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCoursesToDate());
    }

    @FXML
    public void recommendCourses() {
        try {
            if (transcriptFile != null) {
                listView.getItems().clear();
                listView.getItems().addAll(manager.recommendCourses());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listGraduationPlan() {
        listView.getItems().clear();
        listView.getItems().addAll(manager.graduationPlan());
    }

    public void populateContextMenu() {
        MenuItem prereqs = new MenuItem("Show Prerequisites");
        prereqs.setOnAction(actionEvent -> showPrerequisites());
        courseMenu.getItems().add(prereqs);
    }

    public void showCourseByTerm() {
        try {
            String search = searchBar.getText();
            if (!search.equals("")) {
                Boolean found = false;
                for (int i = 0; i < termAltNames.length && !found; i++) {
                    if (termAltNames[i].contains(search)) {
                        List courses = manager.listCourses(i + 1);
                        listView.getItems().clear();
                        listView.getItems().addAll(courses);
                        found = true;
                    }
                }
                if (!found) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "There are no matches " +
                            "for the current input.");
                    alert.setHeaderText("Unknown Input");
                    alert.showAndWait();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void showPrerequisites() {
        Object itemSelected = listView.getSelectionModel().getSelectedItem();
        String courseName = "";
        if (itemSelected instanceof Course) {
            courseName = ((Course) itemSelected).getName();
        } else if (itemSelected instanceof String) {
            courseName = ((String) itemSelected).substring(0, ((String) itemSelected).indexOf(" "));
        }
        detailView.getItems().clear();
        List<String> prereqs = manager.showPrerequisites(courseName);
        if (prereqs == null) {
            detailView.getItems().add("Class Entered is Invalid");
        } else if (prereqs.size() == 0) {
            detailView.getItems().add("No prerequisites found");
        } else {
            detailView.getItems().addAll(prereqs);
        }
    }

    public void importTranscript() {
        FileChooser loadChooser = new FileChooser();
        loadChooser.setInitialDirectory(new File("./"));
        loadChooser.setTitle("Open Transcript File");
        loadChooser.getExtensionFilters().add(new FileChooser.
                ExtensionFilter("PDF Files", "*.pdf"));
        try {
            transcriptFile = loadChooser.showOpenDialog(null);
            if (transcriptFile != null) {
                pdfManager.importTranscript(transcriptFile);
                listView.getItems().clear();
                listView.getItems().add("Hello " + manager.getMajor() + " student. Import is complete!");
                feature2Button.setDisable(false);
                feature3Button.setDisable(false);
                feature4Button.setDisable(false);
                recommendButton.setDisable(false);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportTranscript() {
        FileChooser saveChooser = new FileChooser();
        saveChooser.setInitialDirectory(new File("./"));
        saveChooser.setTitle("Save Transcirpt File");
        saveChooser.getExtensionFilters().add(new FileChooser.
                ExtensionFilter("PDF Files", "*.pdf"));
        try {
            transcriptFile = saveChooser.showSaveDialog(null);
            if (transcriptFile != null) {
                pdfManager.exportTranscript(transcriptFile.toPath());
                listView.getItems().clear();
                listView.getItems().add("Export successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

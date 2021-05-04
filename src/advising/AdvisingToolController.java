/**
 * Course: SE 2800
 * Section 41
 * Dr. Jonathon Magana
 * Advising Tool
 * Created by: Derek Gauger, Kian Dettlaff, Roberto Garcia, and Tyler Faulkner
 * March 18th, 2021
 */

package advising;

import advising.courseGraph.CourseGraph;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private CourseGraph courseGraph;
    private PDFManager pdfManager;

    @FXML
    ListView listView, detailView, currentCourses, failedRecommend, gradPlanFailed, failedViewer;

    @FXML
    Button recommendButton, feature2Button, feature3Button, failedButton;

    @FXML
    MenuButton optionBox;

    @FXML
    ColorPicker colorPicker;

    @FXML
    TextField searchBar;

    @FXML
    Canvas singleCourse;

    @FXML
    ScrollPane nodeGraph, failedCourseScreen;

    @FXML
    CheckBox preReqTail, completedMark;

    @FXML
    ContextMenu courseMenu;

    /**
     * Method that creates the instance of course manager,
     * actually creates the context menu, and adds the courses by
     * term to the button.
     */
    @FXML
    private void initialize() {
        manager = new CourseManager();
        pdfManager = new PDFManager(manager);
        //comboBox.getItems().addAll(features);
        courseGraph = new CourseGraph(manager.getCatalog());
        // Create the context menu and have a menuitem that calls show prerequisites
        populateContextMenu();
        //Creating and setting each new feature/menu item
        //Course By Term
        MenuItem courseByTerm = new MenuItem("Courses By Term");
        courseByTerm.setOnAction(actionEvent -> showCourseByTerm());
        buttonFeatures.add(courseByTerm);

        MenuItem prereqGraph = new MenuItem("Show PreReq Graph");
        prereqGraph.setOnAction(actionEvent -> drawPreReq());
        buttonFeatures.add(prereqGraph);


        //Attach to menuButton
        optionBox.getItems().addAll(buttonFeatures);
    }

    /**
     * Method for listing all CS courses in the catalog
     */
    @FXML
    private void listCSCourses() {
        hideGraph();
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCSTrack());
    }

    /**
     * Method for listing all SE courses in the catalog
     */
    @FXML
    private void listSECourses() {
        hideGraph();
        listView.getItems().clear();
        listView.getItems().addAll(manager.getSETrack());
    }

    /**
     * Method that lists all courses present on unofficial transcript
     */
    @FXML
    private void listCourseToDate() {
        hideGraph();
        hideFailed();
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCoursesToDate());
    }

    private void hideGraph() {
        searchBar.setOnAction(null);
        nodeGraph.setDisable(true);
        nodeGraph.setVisible(false);
    }

    private void hideFailed() {
        failedCourseScreen.setVisible(false);
        failedCourseScreen.setDisable(true);
        manager.resetVirtual();
    }

    @FXML
    private void openFailedImpact() {
        failedCourseScreen.setVisible(true);
        failedCourseScreen.setDisable(false);
        updateFailed();
    }

    @FXML
    private void markCompleted() {
        Course course = (Course) currentCourses.getSelectionModel().getSelectedItem();
        manager.removeFailedVirtual(course);
        updateFailed();
    }

    private void updateFailed() {
        List<Course> currCourses = manager.getCurrentCourses();
        currentCourses.getItems().setAll(currCourses);
        failedRecommend.getItems().setAll(manager.recommendCourses());
        gradPlanFailed.getItems().setAll(manager.graduationPlan());
        failedViewer.getItems().setAll(getFailedStatus(currCourses));
    }

    private List<Boolean> getFailedStatus(List<Course> courses) {
        List<Boolean> failedStatus = new ArrayList<>();
        for (Course course : courses) {
            failedStatus.add(!course.isCompleted());
        }
        return failedStatus;
    }

    @FXML
    private void markFailed() {
        Course course = (Course) currentCourses.getSelectionModel().getSelectedItem();
        manager.setFailedVirtual(course);
        updateFailed();
    }

    @FXML
    private void drawPreReq() {
        if (!graphIsVisible()) {
            nodeGraph.setDisable(false);
            nodeGraph.setVisible(true);
            singleCourse.getGraphicsContext2D().clearRect(0, 0,
                    singleCourse.getWidth(), singleCourse.getHeight());
            //searchBar.setText("");
            //searchBar.setPromptText("Enter Course Code");
        }
        searchBar.setOnAction(actionEvent -> drawPreReq());
        if (!searchBar.getText().equals("")) {
            try {
                courseGraph.draw(searchBar.getText().toUpperCase().replaceAll(" ", ""),
                        singleCourse.getGraphicsContext2D(), preReqTail.isSelected(),
                        completedMark.isSelected(), colorPicker.getValue());
            } catch (CourseGraph.UnknownCourseException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "There are no matches " +
                        "for the inputted course.");
                alert.setHeaderText("Class Not Found");
                alert.showAndWait();
            }
        }
    }

    /**
     * Shows the courses that the user should take next term.
     */
    public void recommendCourses() {
        hideGraph();
        hideFailed();
        if (transcriptFile != null) {
            listView.getItems().clear();
            listView.getItems().addAll(manager.recommendCourses());
        }
    }

    /**
     * Presents the graduation plan of a given student based on transcript
     */
    @FXML
    private void listGraduationPlan() {
        hideGraph();
        hideFailed();
        listView.getItems().clear();
        listView.getItems().addAll(manager.graduationPlan());
    }

    /**
     * Method to prompt for showing prerequisites and to set the onAction
     */
    private void populateContextMenu() {
        MenuItem prereqs = new MenuItem("Show Prerequisites");
        prereqs.setOnAction(actionEvent -> showPrerequisites());
        courseMenu.getItems().add(prereqs);
    }

    /**
     * Method called when transcript is imported and the course by term button is clicked
     */
    private void showCourseByTerm() {
        if (graphIsVisible()) {
            hideGraph();
            // listView.getItems().clear();
            //searchBar.setText("");
            //searchBar.setPromptText("Please input a Term (1, 2, or 3)");
        }
        searchBar.setOnAction(actionEvent -> showCourseByTerm());
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
        } else {
            searchBar.setPromptText("Please input a Term (1, 2, or 3)");
        }
    }

    private boolean graphIsVisible() {
        return nodeGraph.isVisible();
    }

    /**
     * Method called using context menu that presents prerequisites in detail window
     */
    private void showPrerequisites() {
        Object itemSelected = listView.getSelectionModel().getSelectedItem();
        String courseName = "";
        if (itemSelected instanceof Course) {
            courseName = ((Course) itemSelected).getName();
        } else if (itemSelected instanceof String) {
            String[] strings = ((String) itemSelected).split(" ");
            courseName = strings[0];
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

    /**
     * Method that prompts user for their unofficial transcript
     */
    @FXML
    private void importTranscript() {
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
                listView.getItems().add("Hello " + manager.getMajor()
                        + " student. Import is complete!");
                feature2Button.setDisable(false);
                feature3Button.setDisable(false);
                recommendButton.setDisable(false);
                completedMark.setDisable(false);
                colorPicker.setDisable(false);
                failedButton.setDisable(false);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Critical Error");
            alert.setContentText("The system had a critical error while loading the file.");
            alert.showAndWait();
        }
    }

    /**
     * Exports the user's transcript
     */
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
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Critical Error");
            alert.setContentText("The system had a critical error while loading the file.");
            alert.showAndWait();
        }
    }
}

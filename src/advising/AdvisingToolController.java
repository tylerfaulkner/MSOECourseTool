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
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the Advising Tool GUI using FXML
 */
public class AdvisingToolController {
    private static final String FEATURE_1 = "Courses by Term";
    private static final String FEATURE_2 = "Show Prerequisites";
    private static final String FEATURE_4 = "Recommended Courses for next term";


    /**
     * Alternate names for terms that could be used when a user searches
     */
    private List[] termAltNames = {
            Arrays.asList("first", "fall", "first quarter", "fall quarter", "1", "quarter 1"),
            Arrays.asList("second", "second quarter", "winter", "winter quarter", "2", "quarter 2"),
            Arrays.asList("third", "third quarter", "3", "spring", "spring quarter", "quarter 3")
    };

    private List features = Arrays.asList(FEATURE_1, FEATURE_2, FEATURE_4);
    private CourseManager manager;
    private File transcriptFile;
    private CourseGraph courseGraph;

    @FXML
    ListView listView;

    @FXML
    ComboBox comboBox;

    @FXML
    TextField searchBar;

    @FXML
    Canvas canvas;

    @FXML
    ScrollPane canvasScroll;


    @FXML
    private void initialize() {
        manager = new CourseManager();
        comboBox.getItems().addAll(features);
        courseGraph = new CourseGraph(manager.getCatalog());
    }

    @FXML
    private void listCSCourses(){
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCSTrack());
    }

    @FXML
    private void listSECourses(){
        listView.getItems().clear();
        listView.getItems().addAll(manager.getSETrack());
    }

    @FXML
    private void listCourseToDate() {
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCoursesToDate());
    }

    @FXML
    private void showPreReqGraph(){
        canvasScroll.setDisable(false);
        canvasScroll.setVisible(true);
        courseGraph.draw(canvas.getGraphicsContext2D());
    }

    @FXML
    private void search() {
        try {
            String searchType = (String) comboBox.getValue();
            if (searchType.equals(FEATURE_1)) {
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
            } else if (searchType.equals(FEATURE_4)) {
                if (transcriptFile != null) {
                    listView.getItems().clear();
                    listView.getItems().addAll(manager.recommendCourses());
                }
            } else if (searchType.equals(FEATURE_2)) {
                String search = searchBar.getText();
                listView.getItems().clear();
                List<String> prereqs = manager.showPrerequisites(search);
                if(prereqs == null){
                    listView.getItems().add("Class Entered is Invalid");
                } else {
                    listView.getItems().addAll(prereqs);
                }
            }
        } catch (NullPointerException e) {
            //Used to throwaway search call if there is no input
            e.printStackTrace();
        }
    }

    public void importTranscript() {
        FileChooser loadChooser = new FileChooser();
        loadChooser.setInitialDirectory(new File("./"));
        loadChooser.setTitle("Open Dot File");
        loadChooser.getExtensionFilters().add(new FileChooser.
                ExtensionFilter("PDF Files", "*.pdf"));
        try {
            transcriptFile = loadChooser.showOpenDialog(null);
            if (transcriptFile != null) {
                manager.importTranscript(transcriptFile);
                listView.getItems().clear();
                listView.getItems().add("Hello " + manager.getMajor() + " student. Import is complete!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

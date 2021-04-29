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

    /**
     * Method that creates the instance of course manager, actually creates the context menu, and adds the courses by
     * term to the button.
     */
    @FXML
    private void initialize() {
        File prerequisites = new File("src/Data/prerequisites_updated.csv");
        File offerings = new File("src/Data/offerings.csv");
        File curriculum = new File("src/Data/curriculum.csv");
        manager = new CourseManager(prerequisites, offerings, curriculum);
        // Create the context menu and have a menuitem that calls show prerequisites
        populateContextMenu();
        //Creating and setting each new feature/menu item
        //Course By Term
        MenuItem courseByTerm = new MenuItem("Courses By Term");
        courseByTerm.setOnAction(actionEvent -> showCourseByTerm());
        buttonFeatures.add(courseByTerm);


        //Attach to menuButton
        optionBox.getItems().addAll(buttonFeatures);
    }

    /**
     * Method for listing all CS courses in the catalog
     */
    @FXML
    private void listCSCourses(){
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCSTrack());
    }

    /**
     * Method for listing all SE courses in the catalog
     */
    @FXML
    private void listSECourses(){
        listView.getItems().clear();
        listView.getItems().addAll(manager.getSETrack());
    }

    /**
     * Method that lists all courses present on unofficial transcript
     */
    @FXML
    private void listCourseToDate() {
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCoursesToDate());
    }

    /**
     * Method for recommending courses to take the next term based on unofficial transcript
     */
    @FXML
    public void recommendCourses(){
        try {
            if (transcriptFile != null) {
                listView.getItems().clear();
                listView.getItems().addAll(manager.recommendCourses());
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * Presents the graduation plan of a given student based on transcript
     */
    @FXML
    private void listGraduationPlan() {
        listView.getItems().clear();
        listView.getItems().addAll(manager.graduationPlan());
    }

    /**
     * Method to prompt for showing prerequisites and to set the onAction
     */
    private void populateContextMenu(){
        MenuItem prereqs = new MenuItem("Show Prerequisites");
        prereqs.setOnAction(actionEvent -> showPrerequisites());
        courseMenu.getItems().add(prereqs);
    }

    /**
     * Method called when transcript is imported and the course by term button is clicked
     */
    private void showCourseByTerm(){
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
            } else{
                searchBar.setPromptText("Please input a Term (1, 2, or 3)");
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * Method called using context menu that presents prerequisites in detail window
     */
    private void showPrerequisites(){
        Object itemSelected = listView.getSelectionModel().getSelectedItem();
        String courseName = "";
        if(itemSelected instanceof Course){
            courseName = ((Course) itemSelected).getName();
        } else if(itemSelected instanceof String){
            courseName = ((String) itemSelected).substring(0, ((String) itemSelected).indexOf(" "));
        }
        detailView.getItems().clear();
        List<String> prereqs = manager.showPrerequisites(courseName);
        if(prereqs == null){
            detailView.getItems().add("Class Entered is Invalid");
        } else if (prereqs.size() == 0){
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
        loadChooser.setTitle("Open Dot File");
        loadChooser.getExtensionFilters().add(new FileChooser.
                ExtensionFilter("PDF Files", "*.pdf"));
        try {
            transcriptFile = loadChooser.showOpenDialog(null);
            if (transcriptFile != null) {
                manager.importTranscript(transcriptFile);
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
}

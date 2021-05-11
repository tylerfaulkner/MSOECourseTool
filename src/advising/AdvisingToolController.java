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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

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
    ListView listView, detailView;

    @FXML
    Button recommendButton, feature2Button, feature3Button, feature7Button;

    @FXML
    MenuButton optionBox;

    @FXML
    TextField searchBar;

    @FXML
    Canvas singleCourse;

    @FXML
    ScrollPane nodeGraph;

    @FXML
    CheckBox preReqTail;

    @FXML
    ContextMenu courseMenu;
    /**
     * Method that creates the instance of course manager, actually creates the context menu, and adds the courses by
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
    private void listCSCourses(){
        hideGraph();
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCSTrack());
    }

    /**
     * Method for listing all SE courses in the catalog
     */
    @FXML
    private void listSECourses(){
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
        listView.getItems().clear();
        listView.getItems().addAll(manager.getCoursesToDate());
    }

    private void hideGraph(){
        searchBar.setOnAction(null);
        nodeGraph.setDisable(true);
        nodeGraph.setVisible(false);
    }

    @FXML
    private void drawPreReq(){
        if(!graphIsVisible()) {
            nodeGraph.setDisable(false);
            nodeGraph.setVisible(true);
            singleCourse.getGraphicsContext2D().clearRect(0, 0, singleCourse.getWidth(), singleCourse.getHeight());
            //searchBar.setText("");
            //searchBar.setPromptText("Enter Course Code");
        }
        searchBar.setOnAction(actionEvent -> drawPreReq());
        if (!searchBar.getText().equals("")) {
            try {
                courseGraph.draw(searchBar.getText().toUpperCase().replaceAll(" ", ""),
                        singleCourse.getGraphicsContext2D(), preReqTail.isSelected());
            } catch (CourseGraph.UnknownCourseException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "There are no matches " +
                        "for the inputted course.");
                alert.setHeaderText("Class Not Found");
                alert.showAndWait();
            }
        }
    }

    //@FXML
    //private void search() {

    public void recommendCourses(){
        hideGraph();
        try {
            if (transcriptFile != null) {
                listView.getItems().clear();
                List<Course> rec = manager.recommendCourses();
                rec.remove(0);
                listView.getItems().add("Recommended Courses for next trimester:");
                listView.getItems().add("  ");
                listView.getItems().addAll(rec);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Presents the graduation plan of a given student based on transcript
     */
    @FXML
    private void listGraduationPlan() {
        hideGraph();
        listView.getItems().clear();
        listView.getItems().add("List of classes needed to graduate:");
        List<Course> termCourses = new ArrayList<>();
        List<Course> gradCourses = manager.graduationPlan();
        gradCourses.remove(0);
        Iterator<Course> it = gradCourses.listIterator();
        int termNumber = 1;
        while (it.hasNext()) {
            termCourses.clear();
            String termDescription = "";
            switch (termNumber) {
                case 1:
                    termDescription = "2021-2022 Fall Quarter";
                    break;
                case 2:
                    termDescription = "2021-2022 Winter Quarter";
                    break;
                case 3:
                    termDescription = "2021-2022 Spring Quarter";
                    break;
                case 4:
                    termDescription = "2022-2023 Fall Quarter";
                    break;
                case 5:
                    termDescription = "2022-2023 Winter Quarter";
                    break;
                case 6:
                    termDescription = "2022-2023 Spring Quarter";
                    break;
                case 7:
                    termDescription = "2023-2024 Fall Quarter";
                    break;
                case 8:
                    termDescription = "2023-2024 Winter Quarter";
                    break;
                case 9:
                    termDescription = "2023-2024 Spring Quarter";
                    break;
            }
            listView.getItems().add(termDescription);
            listView.getItems().add("  ");
            while (manager.getTotalCredits(termCourses) < 15 && it.hasNext()) {
                termCourses.add(it.next());
            }
            listView.getItems().addAll(termCourses);
            listView.getItems().add("  ");
            termNumber++;
        }
    }

    @FXML
    private void exportGraduationPlan() {
        FileChooser saveChooser = new FileChooser();
        saveChooser.setInitialDirectory(new File("./"));
        saveChooser.setTitle("Save Graduation Plan File");
        saveChooser.getExtensionFilters().add(new FileChooser.
                ExtensionFilter("Text Files", "*.txt"));
        File gradPlanExp = saveChooser.showSaveDialog(null);
        try {
            if (gradPlanExp != null) {
                PrintWriter pw = new PrintWriter(gradPlanExp);
                List<Course> termCourses = new ArrayList<>();
                List<Course> gradCourses = manager.graduationPlan();
                gradCourses.remove(0);
                Iterator<Course> it = gradCourses.listIterator();
                int termNumber = 1;
                while (it.hasNext()) {
                    termCourses.clear();
                    String termDescription = "";
                    switch (termNumber) {
                        case 1:
                            termDescription = "2021-2022 Fall Quarter";
                            break;
                        case 2:
                            termDescription = "2021-2022 Winter Quarter";
                            break;
                        case 3:
                            termDescription = "2021-2022 Spring Quarter";
                            break;
                        case 4:
                            termDescription = "2022-2023 Fall Quarter";
                            break;
                        case 5:
                            termDescription = "2022-2023 Winter Quarter";
                            break;
                        case 6:
                            termDescription = "2022-2023 Spring Quarter";
                            break;
                        case 7:
                            termDescription = "2023-2024 Fall Quarter";
                            break;
                        case 8:
                            termDescription = "2023-2024 Winter Quarter";
                            break;
                        case 9:
                            termDescription = "2023-2024 Spring Quarter";
                            break;
                    }
                    pw.println(termDescription);
                    while (manager.getTotalCredits(termCourses) < 15 && it.hasNext()) {
                        termCourses.add(it.next());
                    }
                    pw.println(termCourses);
                    pw.println("  ");
                    termNumber++;
                }
                pw.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        if(graphIsVisible()) {
            hideGraph();
            // listView.getItems().clear();
            //searchBar.setText("");
            //searchBar.setPromptText("Please input a Term (1, 2, or 3)");
        }
        searchBar.setOnAction(actionEvent -> showCourseByTerm());
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
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean graphIsVisible(){
        return nodeGraph.isVisible();
    }

    /**
     * Method called using context menu that presents prerequisites in detail window
     */
    private void showPrerequisites(){
        Object itemSelected = listView.getSelectionModel().getSelectedItem();
        String courseName = "";
        if (itemSelected instanceof Course) {
            courseName = ((Course) itemSelected).getName();
        } else if (itemSelected instanceof String) {
            String[] strings = ((String) itemSelected).split(" ") ;
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
                listView.getItems().add("Hello " + manager.getMajor() + " student. Import is complete!");
                feature2Button.setDisable(false);
                feature3Button.setDisable(false);
                feature7Button.setDisable(false);
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

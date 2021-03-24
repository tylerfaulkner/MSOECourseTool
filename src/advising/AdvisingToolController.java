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
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for the Advising Tool GUI using FXML
 */
public class AdvisingToolController {
    private static final String FEATURE_1 = "Courses by Term";

    /**
     * Alternate names for terms that could be used when a user searches
     */
    private List[] termAltNames = {
            Arrays.asList("first", "fall", "first quarter", "fall quarter", "1", "quarter 1"),
            Arrays.asList("second", "second quarter", "winter", "winter quarter", "2", "quarter 2"),
            Arrays.asList("third", "third quarter", "3", "spring", "spring quarter", "quarter 3")
    };

    private List features = Arrays.asList(FEATURE_1);
    private CourseManager manager;

    @FXML
    ListView listView;

    @FXML
    ComboBox comboBox;

    @FXML
    TextField searchBar;


    @FXML
    private void initialize() {
        manager = new CourseManager();
        comboBox.getItems().addAll(features);
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
                            manager.listCourses(listView, i + 1);
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
            }
        } catch (NullPointerException e) {
            //Used to throwaway search call if there is no input
        }
    }
}

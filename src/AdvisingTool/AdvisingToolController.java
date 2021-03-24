package AdvisingTool;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AdvisingToolController {
    private static final String feature1 = "Courses by Term";

    private List[] termAltNames = {Arrays.asList("first", "fall", "first quarter", "fall quarter", "1"),
            Arrays.asList("second", "second quarter", "winter", "winter quarter", "2"),
            Arrays.asList("third", "third quarter", "3", "spring", "spring quarter")};
    private List features = Arrays.asList(feature1);
    private CourseManager manager;

    @FXML
    ListView listView;

    @FXML
    ComboBox comboBox;

    @FXML
    TextField searchBar;


    @FXML
    public void initialize() {
        manager = new CourseManager();
        comboBox.getItems().addAll(features);
    }

    @FXML
    public void search() {
        try {
            String searchType = (String) comboBox.getValue();
            if (searchType.equals(feature1)) {
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
        } catch (NullPointerException e){

        }
    }
}

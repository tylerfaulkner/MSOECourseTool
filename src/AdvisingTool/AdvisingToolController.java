package AdvisingTool;


import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class AdvisingToolController {
    private CourseManager manager;

    @FXML ListView listView;

    @FXML
    public void initialize(){
        manager = new CourseManager();
        manager.listCourses(listView, 1);
    }
}

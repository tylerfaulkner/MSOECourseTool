package AdvisingTool;


import javafx.fxml.FXML;

public class AdvisingToolController {
    private CourseManager manager;

    @FXML
    public void initialize(){
        manager = new CourseManager();
    }
}

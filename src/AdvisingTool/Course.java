package AdvisingTool;


public class Course {
    private String name;
    private int credits;
    private String prerequisites;
    private String description;

    public Course(String name, int credits, String prerequisites, String description ){
        this.name = name;
        this.credits = credits;
        this.prerequisites = prerequisites;
        this.description = description;
    }
}

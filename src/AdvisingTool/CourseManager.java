package AdvisingTool;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class CourseManager {

    HashMap<String, Course> catalog = new HashMap<>();

    public CourseManager(){
        try {
            Scanner prereqs = new Scanner(new File("src/Data/prerequisites_updated.csv"));
            prereqs.nextLine();
            while (prereqs.hasNextLine()){
                Scanner line = new Scanner(prereqs.nextLine()).useDelimiter(",");
                String name = line.next();
                int credits = line.nextInt();
                String prerequisites = line.next();
                System.out.println(prerequisites);
                String desc = line.next();
                Course newCourse = new Course(name, credits, prerequisites, desc);
                catalog.put(name, newCourse);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

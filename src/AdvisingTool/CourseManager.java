package AdvisingTool;


import javafx.scene.control.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import org.apache.pdfbox.*;

public class CourseManager {

    private HashMap<String, Course> catalog = new HashMap<>();

    public CourseManager() {
        try {
            Scanner prereqs = new Scanner(new File("src/Data/prerequisites_updated.csv"));
            prereqs.nextLine();
            while (prereqs.hasNextLine()) {
                Scanner line = new Scanner(prereqs.nextLine()).useDelimiter(",");
                String name = line.next();
                int credits = line.nextInt();
                String prerequisites = line.next();
                String desc = line.next();
                Course newCourse = new Course(name, credits, prerequisites, desc);
                catalog.put(name, newCourse);
            }

            Scanner offerings = new Scanner(new File("src/Data/offerings.csv"));
            String[] header = offerings.nextLine().split(",");
            while (offerings.hasNextLine()) {
                String[] line = offerings.nextLine().split(",");
                String courseName = line[0];
                Course course = catalog.get(courseName);
                for (int i = 1; i < line.length; i++) {
                    if (!line[i].equals("") && course != null) {
                        int term = Integer.valueOf(line[i]);
                        course.addTerm(term, header[i]);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void listCourses(ListView listView, int term) {
        listView.getItems().clear();
        Iterator iter = catalog.keySet().iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            Course course = catalog.get(name);
            System.out.println(course.toString());
            ArrayList<String> majors = course.getTerm(term);
            if (majors.size() != 0) {
                listView.getItems().add(course.toString());
            }
        }
    }
}

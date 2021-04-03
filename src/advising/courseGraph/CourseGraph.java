package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CourseGraph {
    private static final int NODEDISTANCE = 20;
    ArrayList<CourseNode> nodes = new ArrayList<>();

    public CourseGraph(HashMap<String, Course> catalog){
        Collection<String> keys = catalog.keySet();
        for(String key : keys){
            Course course = catalog.get(key);
            CourseNode node = new CourseNode(course);
            if(!nodes.contains(node)){
                nodes.add(node);
            } else {
                nodes.get(nodes.indexOf(node)).addPreReqFor();
            }
        }
    }

    public void draw(GraphicsContext gc){
        for (int i = 0; i < nodes.size(); i++){
            nodes.get(i).draw(gc, NODEDISTANCE, NODEDISTANCE * i);
        }
    }
}

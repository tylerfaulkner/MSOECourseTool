package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CourseGraph {
    private static final int NODEDISTANCE = 20;

    private int highestPreReqCount = 0;
    private ArrayList<CourseNode> nodes = new ArrayList<>();
    private HashMap<String, Course> catalog;

    public CourseGraph(HashMap<String, Course> catalog){
        this.catalog = catalog;
        Collection<String> keys = catalog.keySet();
        for(String key : keys){
            addKeytoNodes(key, nodes);
        }
        addPreReqs();
        getHighestPreReqCount();
    }

    public void draw(GraphicsContext gc){
        for (int i = 0; i < nodes.size(); i++){
            CourseNode node = nodes.get(i);
            int offset = (highestPreReqCount + 1) - node.getPreReqCount();
            node.draw(gc, NODEDISTANCE * offset, NODEDISTANCE * i);
        }
    }

    private void addPreReqs(){
        ArrayList newNodes = (ArrayList) nodes.clone();
        for (int i = 0; i<nodes.size(); ++i){
            CourseNode node = nodes.get(i);
            String preReqsString = node.getCourse().getPrerequisites();
            preReqsString = preReqsString.replace("|", " ");
            String[] preReqs = preReqsString.split(" ");
            for(String preReq : preReqs){
                addKeytoNodes(preReq, newNodes);
            }
        }
        nodes = newNodes;
    }

    private void addKeytoNodes(String key, ArrayList<CourseNode> nodes){
        if(!(key.length() < 3)) {
            Course course = catalog.get(key);
            if (course == null) {
                course = new Course(key, 0, "", "");
                System.out.println("Course: " + course.getName());
            }
            CourseNode newNode = new CourseNode(course);
            if (!nodes.contains(newNode)) {
                nodes.add(newNode);
            } else {
                nodes.get(nodes.indexOf(newNode)).addPreReqFor();
            }
        }
    }

    private void getHighestPreReqCount(){
        for(CourseNode node : nodes){
            int count = node.getPreReqCount();
            if(count > highestPreReqCount){
                highestPreReqCount = count;
            }
        }
    }
}

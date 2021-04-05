package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class CourseGraph {
    private static final int LINE_OFFSET = 30;
    private static final int NODES_PER_COL = 25;
    private static final int COL_OFFSET = 300;
    private static final int NODE_DISTANCE = 120;
    private static final int PADDING = 40;
    private static final int Y_PADDING = 60;

    private int highestPreReqCount = 0;
    private ArrayList<CourseNode> nodes = new ArrayList<>();
    private HashMap<String, Course> catalog;

    public CourseGraph(HashMap<String, Course> catalog){
        this.catalog = catalog;
        Collection<String> keys = catalog.keySet();
        for(String key : keys){
            addKeytoNodes(key, nodes);
        }
        //addPreReqs();
        getHighestPreReqCount();
    }

    public void draw(GraphicsContext gc){
        int offset = 0;
        int previousNodeY = 0;
        for (int i = 0; i < nodes.size(); i++){
            int col = i/NODES_PER_COL;
            if (i%NODES_PER_COL == 0){
                gc.strokeLine((COL_OFFSET*col)-LINE_OFFSET, 0, (COL_OFFSET*col) - LINE_OFFSET, 3000);
                offset = 0;
                previousNodeY = 0;
            }
            CourseNode node = nodes.get(i);
            previousNodeY = offset + previousNodeY + (i > 0 ? NODE_DISTANCE : Y_PADDING);
            int x = PADDING + (col>=1 ? COL_OFFSET*col : 0);
            offset = node.draw(gc, x, previousNodeY);
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

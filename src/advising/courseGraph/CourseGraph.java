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
            offset = node.draw(gc, x, previousNodeY, false);
        }
    }

    public void drawCourse(String name, GraphicsContext gc, boolean trailingPreReqs){
        gc.clearRect(0,0,gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        CourseNode node = findCourseNode(name);
        if(node != null) {
            int x = 150;
            int y = 100;
            if(trailingPreReqs) {
                getPreReqNodes(node);
                x=300;
                y=125;
            }
            node.draw(gc, x, y, trailingPreReqs);
        }
    }

    private void getPreReqNodes(CourseNode node){
        if(node.getCourse().getName() != "") {
            String[] preReqs = node.getCourse().getPrerequisites().split(" ");
            for (String preReq : preReqs) {
                if(preReq != null) {
                    CourseNode preReqNode = findCourseNode(preReq);
                    if (preReqNode != null) {
                        getPreReqNodes(preReqNode);
                        node.addPreReqNode(preReqNode);
                    } else {
                        node.addPreReqNode(new CourseNode(new Course(preReq, 0, "", "")));
                    }
                }
            }
        }
    }

    private CourseNode findCourseNode(String name){
        for(int i = 0; i < nodes.size(); ++i){
            CourseNode node = nodes.get(i);
            if(node.getCourse().getName().equals(name)){
                return node;
            }
        }
        System.out.println("Course Not Found" + name);
        return null;
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

/**
 * Majority Author: Tyler Faulkner
 */
package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Represents the entire drawn graph of pre-requisites
 */
public class CourseGraph {
    private static final double SEARCH_Y = 250;
    private static final double SEARCH_X = 600;

    private ArrayList<CourseNode> nodes = new ArrayList<>();
    private HashMap<String, Course> catalog;

    public CourseGraph(HashMap<String, Course> catalog){
        this.catalog = catalog;
        Collection<String> keys = catalog.keySet();
        for(String key : keys){
            addKeytoNodes(key, nodes);
        }
    }

    /**
     * Draws the graph on the given canvas
     * @param name course to draw
     * @param gc GraphicsContext to draw on
     * @param trailingPreReqs determines if prereqs of prereqs should be shown
     * @throws UnknownCourseException if the course is not found
     */
    public void draw(String name, GraphicsContext gc, boolean trailingPreReqs) throws UnknownCourseException {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        CourseNode node = findCourseNode(name);
        if(node != null) {
            if(trailingPreReqs) {
                getPreReqNodes(node);
            }
            node.draw(gc, SEARCH_X, SEARCH_Y, trailingPreReqs, "brown");
        } else {
            throw new UnknownCourseException("Unknown Course: " + name);
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
        return null;
    }

    private void addKeytoNodes(String key, ArrayList<CourseNode> nodes){
        if(!(key.length() < 3)) {
            Course course = catalog.get(key);
            if (course == null) {
                course = new Course(key, 0, "", "");
            }
            CourseNode newNode = new CourseNode(course);
            if (!nodes.contains(newNode)) {
                nodes.add(newNode);
            }
        }
    }

    public class UnknownCourseException extends Exception{
        public UnknownCourseException(String errorMessage){
            super(errorMessage);
        }
    }
}

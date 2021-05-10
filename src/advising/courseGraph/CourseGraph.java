/**
 * Course: SE 2800
 * Section 41
 * Dr. Jonathon Magana
 * Advising Tool
 * Created by: Tyler Faulkner
 * March 18th, 2021
 */

package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Represents the entire drawn graph of pre-requisites
 */
public class CourseGraph {

    /**
     * The y location to draw the first node
     */
    private static final double SEARCH_Y = 250;

    /**
     * the x location to draw the first node
     */
    private static final double SEARCH_X = 600;

    private ArrayList<CourseNode> nodes = new ArrayList<>();
    private HashMap<String, Course> catalog;

    /**
     * Takes the catalog from course manager in order to operate correctly
     *
     * @param catalog catalog from the course manager
     */
    public CourseGraph(HashMap<String, Course> catalog) {
        this.catalog = catalog;
        Collection<String> keys = catalog.keySet();
        for (String key : keys) {
            addKeyToNodes(key, nodes);
        }
    }

    /**
     * Draws the graph on the given canvas
     *
     * @param name            course to draw
     * @param gc              GraphicsContext to draw on
     * @param trailingPreReqs determines if prereqs of prereqs should be shown
     * @param markCompleted   if completed courses should be labeled
     * @param completedColor  the color to label completed coursers as
     * @throws UnknownCourseException if the course is not found
     */
    public void draw(String name, GraphicsContext gc, boolean trailingPreReqs,
                     boolean markCompleted, Color completedColor) throws UnknownCourseException {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        CourseNode node = findCourseNode(name);
        if (node != null) {
            if (trailingPreReqs) {
                addPreReqNodes(node);
            }
            node.draw(gc, SEARCH_X, SEARCH_Y, trailingPreReqs,
                    "brown", markCompleted, completedColor);
        } else {
            throw new UnknownCourseException("Unknown Course: " + name);
        }
    }

    /**
     * Gets the pre-requisites as nodes for the given node.
     *
     * @param node the node to generate pre-req nodes for
     */
    private void addPreReqNodes(CourseNode node) {
        if (!node.getCourse().getName().equals("")) {
            String[] preReqs = node.getCourse().getPrerequisites().split(" ");
            for (String preReq : preReqs) {
                if (preReq != null) {
                    CourseNode preReqNode = findCourseNode(preReq);
                    if (preReqNode != null) {
                        //calls the mehtod on the pre-req node
                        addPreReqNodes(preReqNode);
                        node.addPreReqNode(preReqNode);
                    } else {
                        node.addPreReqNode(new CourseNode(new Course(preReq, 0, "", ""), catalog));
                    }
                }
            }
        }
    }

    /**
     * Finds a course node with the given name
     *
     * @param name the name to match
     * @return the node that matches the name or null if not found
     */
    private CourseNode findCourseNode(String name) {
        for (CourseNode node : nodes) {
            if (node.getCourse().getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Takes a course name creates a node of it and adds it to nodes arraylist
     *
     * @param key   the course to generate
     * @param nodes the arraylist to add the new node to
     */
    private void addKeyToNodes(String key, ArrayList<CourseNode> nodes) {
        if (!(key.length() < 3)) {
            Course course = catalog.get(key);
            if (course == null) {
                course = new Course(key, 0, "", "");
            }
            CourseNode newNode = new CourseNode(course, catalog);
            if (!nodes.contains(newNode)) {
                nodes.add(newNode);
            }
        }
    }

    /**
     * Used for when an unknown course is given.
     */
    public static class UnknownCourseException extends Exception {
        /**
         * sets the message fore the error message
         *
         * @param errorMessage the message
         */
        public UnknownCourseException(String errorMessage) {
            super(errorMessage);
        }
    }
}

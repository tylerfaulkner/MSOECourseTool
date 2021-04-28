/**
 * Majority Author: Tyler Faulkner
 */

package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that represents a single node within the visualized pre-req graph
 */
public class CourseNode {
    /**
     * The Max Width for the text field
     */
    private static final double MAX_WIDTH = 50;

    /**
     * The offset for the x coordinate given to draw on
     */
    private static final int X_OFFSET = 25;

    /**
     * The offset for Or pre-reqs
     */
    private static final int OR_OFFSET = 20;

    /**
     * The offset for the stroke between nodes
     */
    private static final int STROKE_OFFSET = 67;

    /**
     * Radius of the drawn node
     */
    private static final int NODE_RADIUS = 100;

    /**
     * Spacing between node y coordinates
     */
    private static final int Y_SPACING = 120;

    /**
     * The x spacing between nodes
     */
    private static final int X_SPACING = 150;

    private ArrayList<CourseNode> preReqNodes = new ArrayList<>();
    private Course course;
    private HashMap<String, Course> catalog;

    public CourseNode(Course course, HashMap<String, Course> catalog) {
        this.course = course;
        this.catalog = catalog;
    }

    public Course getCourse() {
        return course;
    }

    @Override
    public boolean equals(Object course) {
        if (course instanceof CourseNode) {
            CourseNode otherCourseNode = (CourseNode) course;
            String name = otherCourseNode.getCourse().getName();
            if (this.course.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a node that is a pre-requisite
     *
     * @param node pre-req node
     */
    public void addPreReqNode(CourseNode node) {
        if (preReqNodes.size() < course.getPrerequisites().split(" ").length) {
            preReqNodes.add(node);
        }
    }

    /**
     * Draw the node on the canvas
     *
     * @param gc              the GraphicsContext of the canvas
     * @param x               x-coordinate
     * @param y               y-coordinate
     * @param trailingPreReqs if prereqs of prereqs should be shown
     * @param color           color of head node
     * @param markCompleted   if completed courses should be marked
     * @param completedColor  the color that completed courses should be
     */
    public void draw(GraphicsContext gc, double x, double y, boolean trailingPreReqs, String color, boolean markCompleted, Color completedColor) {
        if (!trailingPreReqs || preReqNodes.isEmpty()) {
            if (!course.getPrerequisites().equals("")) {
                drawNoTrail(gc, x, y, markCompleted, completedColor);
            } else {
                if (!course.getName().equals("")) {
                    gc.setFill(Paint.valueOf("orange"));
                    String preReq = course.getName();
                    if (markCompleted) {
                        String[] preReqs = preReq.split("\\|");
                        for (int i = 0; i < preReqs.length; ++i) {
                            Course pre = catalog.get(preReqs[i]);
                            if (pre != null && pre.isCompleted()) {
                                gc.setFill(completedColor);
                            }
                        }
                    }
                    gc.fillOval(x, y, NODE_RADIUS, NODE_RADIUS);
                    gc.setFill(Paint.valueOf("black"));
                    preReq = preReq.replaceAll("\\|", " or\n");
                    gc.fillText(preReq, x + OR_OFFSET, y + (Y_SPACING / 3), MAX_WIDTH);
                }
            }
        } else {
            drawNodes(gc, x, y, color, markCompleted, completedColor);
        }
    }

    /**
     * Draws node based on course pre-reqs string
     *
     * @param gc             the GraphicsContext to draw on
     * @param xCord          the x location to draw the head node
     * @param yCord          the y location to draw the head node
     * @param markCompleted  if completed courses should be marked
     * @param completedColor the color to mark completed course
     */
    private void drawNoTrail(GraphicsContext gc, double xCord, double yCord,
                             boolean markCompleted, Color completedColor) {
        String[] preReqs = course.getPrerequisites().split(" ");
        ArrayList<double[]> cords = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < preReqs.length; ++i) {
            String preReq = preReqs[i];
            if (preReq.length() > 3) {
                double yCordAlt = yCord + (Y_SPACING * i);
                double ovalX = xCord - (NODE_RADIUS / 3) - X_SPACING;
                double ovalY = yCordAlt - (NODE_RADIUS / 2);
                gc.setFill(Paint.valueOf("green"));
                if (markCompleted && catalog.containsKey(preReq) && catalog.get(preReq).isCompleted()) {
                    gc.setFill(completedColor);
                }
                gc.fillOval(ovalX, ovalY, NODE_RADIUS, NODE_RADIUS);
                gc.setFill(Paint.valueOf("black"));
                preReq = preReq.replaceAll("\\|", " or\n");
                gc.fillText(preReq, xCord - X_SPACING, yCordAlt, MAX_WIDTH);
                cords.add(new double[]{xCord - X_SPACING, yCordAlt});
                if (i >= 1) {
                    offset += Y_SPACING;
                }
            }
        }
        double xCordAlt = xCord;
        double yCordAlt = yCord + (offset / 2);
        if (cords.size() == 1) {
            yCordAlt = cords.get(0)[1];
        }
        for (double[] cord : cords) {
            double xPreReq = cord[0];
            double yPreReq = cord[1];
            gc.strokeLine(xPreReq + STROKE_OFFSET, yPreReq, xCordAlt, yCordAlt);
        }
        gc.setFill(Paint.valueOf("brown"));
        if (markCompleted && course.isCompleted()) {
            gc.setFill(completedColor);
        }
        gc.fillOval(xCord, yCordAlt - (NODE_RADIUS / 2), NODE_RADIUS, NODE_RADIUS);
        gc.setFill(Paint.valueOf("black"));
        gc.fillText(course.getName(), xCordAlt + MAX_WIDTH / 2, yCordAlt, MAX_WIDTH);
    }

    /**
     * Draws node with pre-req nodes
     *
     * @param gc               Graphics context to draw on
     * @param xCord            x coordinate to draw node
     * @param yCord            y coordinate to draw node
     * @param noncompleteColor the color if the course isn't completed
     * @param markCompleted    if nodes should be marked as completed or not
     * @param completedColor   the color to mark completed courses
     */
    private void drawNodes(GraphicsContext gc, double xCord, double yCord, String noncompleteColor, boolean markCompleted, Color completedColor) {
        if (!course.getName().equals("")) {
            gc.setFill(Paint.valueOf(noncompleteColor));
            if (markCompleted && course.isCompleted()) {
                gc.setFill(completedColor);
            }
            gc.fillOval(xCord, yCord, NODE_RADIUS, NODE_RADIUS);
            gc.setFill(Paint.valueOf("black"));
            gc.fillText(course.getName(), xCord + (Y_SPACING / 4), yCord + (NODE_RADIUS / 2), MAX_WIDTH);
            if (preReqNodes.size() == 2) {
                gc.strokeLine(xCord, yCord + NODE_RADIUS / 2, xCord - X_OFFSET, yCord + NODE_RADIUS + X_OFFSET);
                preReqNodes.get(0).draw(gc, xCord - NODE_RADIUS, yCord + NODE_RADIUS, true,
                        "green", markCompleted, completedColor);
                gc.strokeLine(xCord, yCord + NODE_RADIUS / 2, xCord - X_OFFSET, yCord - NODE_RADIUS / 2);
                preReqNodes.get(1).draw(gc, xCord - NODE_RADIUS, yCord - NODE_RADIUS, true,
                        "green", markCompleted, completedColor);
            } else if (preReqNodes.size() == 1) {
                CourseNode preReq = preReqNodes.get(0);
                if (!preReq.course.getName().equals("")) {
                    gc.strokeLine(xCord, yCord + NODE_RADIUS / 2, xCord - X_OFFSET, yCord + NODE_RADIUS / 2);
                    preReq.draw(gc, xCord - (NODE_RADIUS + X_OFFSET), yCord, true,
                            "cyan", markCompleted, completedColor);
                }
            }
        }
    }
}

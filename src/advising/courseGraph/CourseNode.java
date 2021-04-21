/**
 * Majority Author: Tyler Faulkner
 */

package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import java.util.ArrayList;

/**
 * A class that represents a single node within the visualized pre-req graph
 */
public class CourseNode {
    private static final double MAX_WIDTH = 50;

    private static final int X_OFFSET = 25;
    private static final int OR_OFFSET = 20;
    private static final int STROKE_OFFSET = 67;

    private static final int NODE_RADIUS = 100;
    private static final int Y_SPACING = 120;
    private static final int X_SPACING = 150;

    private ArrayList<CourseNode> preReqNodes = new ArrayList<>();

    private Course course;

    public CourseNode(Course course) {
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }

    @Override
    public boolean equals(Object course) {
        if (course instanceof CourseNode) {
            CourseNode otherCourseNode = (CourseNode) course;
            String name = otherCourseNode.getCourse().getName();
            if (this.course.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a node that is a pre-requisite
     * @param node pre-req node
     */
    public void addPreReqNode(CourseNode node){
        if(preReqNodes.size() < course.getPrerequisites().split(" ").length) {
            preReqNodes.add(node);
        }
    }

    /**
     * Draw the node on the canvas
     * @param gc the GraphicsContext of the canvas
     * @param x x-coordinate
     * @param y y-coordinate
     * @param trailingPreReqs if prereqs of prereqs should be shown
     * @param color color of head node
     * @return
     */
    public int draw(GraphicsContext gc, double x, double y, boolean trailingPreReqs, String color) {
        if(!trailingPreReqs || preReqNodes.isEmpty()) {
            if(!course.getPrerequisites().equals("")) {
                String[] preReqs = course.getPrerequisites().split(" ");
                ArrayList<double[]> cords = new ArrayList<>();
                int offset = 0;
                for (int i = 0; i < preReqs.length; ++i) {
                    String preReq = preReqs[i];
                    if (preReq.length() > 3) {
                        double yCord = y + (Y_SPACING * i);
                        double ovalX = x - (NODE_RADIUS / 3) - X_SPACING;
                        double ovalY = yCord - (NODE_RADIUS / 2);
                        gc.setFill(Paint.valueOf("green"));
                        gc.fillOval(ovalX, ovalY, NODE_RADIUS, NODE_RADIUS);
                        gc.setFill(Paint.valueOf("black"));
                        preReq = preReq.replaceAll("\\|", " or\n");
                        gc.fillText(preReq, x - X_SPACING, yCord, MAX_WIDTH);
                        cords.add(new double[] {x - X_SPACING, yCord});
                        if (i >= 1) {
                            offset += Y_SPACING;
                        }
                    }
                }
                double xCord = x;
                double yCord = y + (offset / 2);
                if (cords.size() == 1) {
                    yCord = cords.get(0)[1];
                }
                for (double[] cord : cords) {
                    double xPreReq = cord[0];
                    double yPreReq = cord[1];
                    gc.strokeLine(xPreReq + STROKE_OFFSET, yPreReq, xCord, yCord);
                }
                //non-trailing head color
                gc.setFill(Paint.valueOf(color));
                gc.fillOval(x, yCord - (NODE_RADIUS / 2), NODE_RADIUS, NODE_RADIUS);
                gc.setFill(Paint.valueOf("black"));
                gc.fillText(course.getName(), xCord + MAX_WIDTH/2, yCord, MAX_WIDTH);
                return offset;
            } else {
                if(!course.getName().equals("")) {
                    gc.setFill(Paint.valueOf("orange"));
                    gc.fillOval(x, y, NODE_RADIUS, NODE_RADIUS);
                    gc.setFill(Paint.valueOf("black"));
                    String preReq = course.getName();
                    preReq = preReq.replaceAll("\\|", " or\n");
                    gc.fillText(preReq, x + OR_OFFSET, y + (Y_SPACING / 3), MAX_WIDTH);
                }
            }
        } else {
            drawNodes(gc, x, y, color);
        }
        return 0;
    }

    private void drawNodes(GraphicsContext gc, double x, double y, String color){
        if(!course.getName().equals("")) {
            gc.setFill(Paint.valueOf(color));
            gc.fillOval(x, y, NODE_RADIUS, NODE_RADIUS);
            gc.setFill(Paint.valueOf("black"));
            gc.fillText(course.getName(), x + (Y_SPACING / 4), y + (NODE_RADIUS / 2), MAX_WIDTH);
            if (preReqNodes.size() == 2) {
                gc.strokeLine(x, y + NODE_RADIUS / 2, x - X_OFFSET, y + NODE_RADIUS + X_OFFSET);
                preReqNodes.get(0).draw(gc, x - NODE_RADIUS, y + NODE_RADIUS, true, "green");
                gc.strokeLine(x, y + NODE_RADIUS / 2, x - X_OFFSET, y - NODE_RADIUS / 2);
                preReqNodes.get(1).draw(gc, x - NODE_RADIUS, y - NODE_RADIUS, true, "green");
            } else if (preReqNodes.size() == 1) {
                CourseNode preReq = preReqNodes.get(0);
                if(!preReq.course.getName().equals("")) {
                    gc.strokeLine(x, y + NODE_RADIUS / 2, x - X_OFFSET, y + NODE_RADIUS / 2);
                    preReq.draw(gc, x - (NODE_RADIUS + X_OFFSET), y, true, "cyan");
                }
            }
        }
    }
}

package advising.courseGraph;

import advising.Course;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;

import java.util.ArrayList;


/**
 * A class that represents a single node within the visualized pre-req graph
 */
public class CourseNode {
    private static final int OUTLINE_RADIUS = 110;
    private static final int NODE_RADIUS = 100;
    private static final int Y_SPACING = 120;
    private static final int X_SPACING = 150;

    private ArrayList<CourseNode> preReqNodes = new ArrayList<>();

    private Course course;
    private int preReqForCount = 0;

    public CourseNode(Course course) {
        this.course = course;
        preReqForCount++;
    }

    public void addPreReqFor() {
        preReqForCount++;
    }

    public int getPreReqCount() {
        return preReqForCount;
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

    public void addPreReqNode(CourseNode node){
        if(preReqNodes.size() < course.getPrerequisites().split(" ").length) {
            preReqNodes.add(node);
        }
    }

    public int draw(GraphicsContext gc, int x, int y, boolean trailingPreReqs) {
        System.out.println("drawing");
        if(!trailingPreReqs || preReqNodes.isEmpty()) {
            if(course.getPrerequisites() != "") {
                String[] preReqs = course.getPrerequisites().split(" ");
                ArrayList<int[]> cords = new ArrayList<>();
                int offset = 0;
                for (int i = 0; i < preReqs.length; ++i) {
                    String preReq = preReqs[i];
                    if (preReq.length() > 3) {
                        int yCord = y + (Y_SPACING * i);
                        int ovalX = x - (NODE_RADIUS / 3);
                        int ovalY = yCord - (NODE_RADIUS / 2);
                        gc.fillOval(ovalX, ovalY, OUTLINE_RADIUS, OUTLINE_RADIUS);
                        gc.setFill(Paint.valueOf("green"));
                        gc.fillOval(ovalX, ovalY, NODE_RADIUS, NODE_RADIUS);
                        gc.setFill(Paint.valueOf("black"));
                        preReq = preReq.replaceAll("\\|", " or\n");
                        gc.fillText(preReq, x, yCord, 50);
                        cords.add(new int[]{x, yCord});
                        if (i >= 1) {
                            offset += Y_SPACING;
                        }
                    }
                }
                int xCord = x + X_SPACING;
                int yCord = y + (offset / 2);
                if (cords.size() == 1) {
                    yCord = cords.get(0)[1];
                }
                for (int[] cord : cords) {
                    int xPreReq = cord[0];
                    int yPreReq = cord[1];
                    gc.strokeLine(xPreReq + (NODE_RADIUS / 1.5), yPreReq, xCord, yCord);
                }
                gc.setFill(Paint.valueOf("white"));
                gc.fillOval(xCord - 25, yCord - (NODE_RADIUS / 2), NODE_RADIUS, NODE_RADIUS);
                gc.setFill(Paint.valueOf("black"));
                gc.fillText(course.getName(), xCord, yCord);
                return offset;
            } else {
                gc.setFill(Paint.valueOf("white"));
                gc.fillOval(x, y, NODE_RADIUS, NODE_RADIUS);
                gc.setFill(Paint.valueOf("black"));
                gc.fillText(course.getName(), x+10, y+50);
            }
        } else {
            drawNodes(gc, x, y);
        }
        return 0;
    }

    private void drawNodes(GraphicsContext gc, int x ,int y){
        gc.setFill(Paint.valueOf("white"));
        gc.fillOval(x, y, NODE_RADIUS, NODE_RADIUS);
        gc.setFill(Paint.valueOf("black"));
        gc.fillText(course.getName(), x+40, y+50);
        System.out.println(preReqNodes.size());
        if (preReqNodes.size() == 2){
            preReqNodes.get(0).draw(gc, x-NODE_RADIUS, y+NODE_RADIUS, true);
            preReqNodes.get(1).draw(gc, x-NODE_RADIUS, y-NODE_RADIUS, true);
        } else if (preReqNodes.size() == 1) {
            gc.strokeLine(x, y+NODE_RADIUS/2, x-25, y+NODE_RADIUS/2);
            preReqNodes.get(0).draw(gc, x-125, y, true);
        }
    }
}

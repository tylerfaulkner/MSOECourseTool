package advising.courseGraph;

import advising.Course;
import javafx.scene.canvas.GraphicsContext;


/**
 * A class that represents a single node within the visualized pre-req graph
 */
public class CourseNode {
    private static final int NODE_RADIUS = 20;

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

    public void draw(GraphicsContext gc, int x, int y) {
        //gc.fillOval(x, y, NODE_RADIUS, NODE_RADIUS);
        gc.fillText(course.getName(), x, y);
    }
}

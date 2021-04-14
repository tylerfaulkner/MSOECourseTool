/**
 * Course: SE 2800
 * Section 41
 * Dr. Jonathon Magana
 * Advising Tool
 * Created by: Derek Gauger, Kian Dettlaff, Roberto Garcia, and Tyler Faulkner
 * March 18th, 2021
 */

package advising;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Course class contains data for an instance of a class.
 */
public class Course implements Comparable<Course> {
    private String name;
    private int credits;
    private String prerequisites;
    private String description;
    private HashMap<Integer, ArrayList<String>> terms = new HashMap<>();
    private boolean completed;
    private boolean passed;
    private String completedTerm;

    private boolean elective;


    public Course(String name, int credits, String prerequisites, String description) {
        this.name = name;
        this.credits = credits;
        this.prerequisites = prerequisites;
        this.description = description;
        terms.put(1, new ArrayList<>());
        terms.put(2, new ArrayList<>());
        terms.put(3, new ArrayList<>());
        completed = false;
        completedTerm = "N/A";
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public String getName() {
        return name;
    }

    public ArrayList getTerm(int term) {
        return terms.get(term);
    }

    public void addTerm(int term, String major) {
        terms.get(term).add(major);
    }

    @Override
    public String toString() {
        return name + " " + description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public int getCredits() {
        return credits;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(Course o) {
        int compare = ((Course) o).getCredits();
        return compare - this.credits;
    }

    public String getCompletedTerm() {
        return completedTerm;
    }

    public void setCompletedTerm(String completedTerm) {
        this.completedTerm = completedTerm;
    }

    public boolean isElective() {
        return elective;
    }

    public void setElective(boolean elective) {
        this.elective = elective;
    }
}

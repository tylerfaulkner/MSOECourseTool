package AdvisingTool;


import java.util.ArrayList;
import java.util.HashMap;

public class Course {
    private String name;
    private int credits;
    private String prerequisites;
    private String description;

    private HashMap<Integer, ArrayList<String>> terms = new HashMap<>();

    public Course(String name, int credits, String prerequisites, String description ){
        this.name = name;
        this.credits = credits;
        this.prerequisites = prerequisites;
        this.description = description;
        terms.put(1, new ArrayList<>());
        terms.put(2, new ArrayList<>());
        terms.put(3, new ArrayList<>());
    }

    public ArrayList getTerm(int term){
        return terms.get(term);
    }

    public void addTerm(int term, String major){
        terms.get(term).add(major);
    }

    @Override
    public String toString(){
        return name + " " + description;
    }
}

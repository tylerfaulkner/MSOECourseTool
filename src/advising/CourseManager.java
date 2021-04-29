/**
 * Course: SE 2800
 * Section 41
 * Dr. Jonathon Magana
 * Advising Tool
 * Created by: Derek Gauger, Kian Dettlaff, Roberto Garcia, and Tyler Faulkner
 * March 18th, 2021
 */

package advising;


import javafx.scene.control.Alert;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages all courses available from the prerequisites csv
 */
public class CourseManager {

    private HashMap<String, Course> catalog = new HashMap<>();
    private List<String> csTrack = new ArrayList<>();
    private List<String> seTrack = new ArrayList<>();
    private List<Course> coursesToDate = new ArrayList<>();
    private List<Course> mathScienceElectives = new ArrayList<>();
    private List<Course> businessElectives = new ArrayList<>();
    private List<Course> programElectives = new ArrayList<>();
    private List<Course> scienceElectives = new ArrayList<>();
    private List<Course> freeElectives = new ArrayList<>();
    private List<List<Course>> totalElectives = new ArrayList<>();
    private int electiveCount = 0;

    private String major;

    /**
     * Course Manager reads in course data upon initialization
     */
    public CourseManager(File prerequisitesCSV, File offeringCSV, File curriculumCSV) {
        try {
            Scanner prereqs = new Scanner(prerequisitesCSV);
            prereqs.nextLine();
            while (prereqs.hasNextLine()) {
                Scanner line = new Scanner(prereqs.nextLine()).useDelimiter(",");
                String name = line.next();
                int credits = line.nextInt();
                String prerequisites = line.next();
                String desc = line.next();
                Course newCourse = new Course(name, credits, prerequisites, desc);
                catalog.put(name, newCourse);
            }

            Scanner offerings = new Scanner(offeringCSV);
            String[] header = offerings.nextLine().split(",");
            while (offerings.hasNextLine()) {
                String[] line = offerings.nextLine().split(",");
                String courseName = line[0];
                Course course = catalog.get(courseName);
                if (course == null){
                    course = new Course(courseName, 3, "", "");
                    catalog.put(courseName, course);
                }
                for (int i = 1; i < line.length; i++) {
                    if (!line[i].equals("") && course != null) {
                        int term = Integer.valueOf(line[i]);
                        course.addTerm(term, header[i]);
                    }
                }
            }

            Scanner curiculum = new Scanner(curriculumCSV);
            curiculum.nextLine();
            while (curiculum.hasNextLine()){
                String[] courses = curiculum.nextLine().split(",");
                csTrack.add(courses[0]);
                seTrack.add(courses[1]);
            }


        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The needed csv files were not found in the directory");
            alert.setHeaderText("Critical Error");
            alert.showAndWait();
        }
    }

    /**
     * Adds courses to the given listView item that are available in the given term
     *
     * @param term     the term of courses to add
     */
    public List listCourses(int term) {
        List<String> courses = new ArrayList<>();
        Iterator iter = catalog.keySet().iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            Course course = catalog.get(name);
            ArrayList<String> majors = course.getTerm(term);
            if (majors.size() != 0) {
                courses.add(course.toString());
            }
        }
        courses.sort(new Sort());
        return courses;
    }

    public String getMajor() {
        return major;
    }

    public List getCSTrack(){
        return csTrack;
    }

    public List getSETrack(){
        return seTrack;
    }

    public List getCoursesToDate() {
        return coursesToDate;
    }


    /**
     * Used to sort listed course in list view
     */
    private class Sort implements Comparator<String> {

        public int compare(String a, String b) {
            a = a.split(" ")[0];
            b = b.split(" ")[0];
            String aMajor = a.substring(0, 2);
            String bMajor = b.substring(0, 2);
            int compare = aMajor.compareTo(bMajor);
            if (compare != 0) {
                return compare;
            } else {
                int aNum = Integer.valueOf(a.substring(2));
                int bNum = Integer.valueOf(b.substring(2));
                if (aNum < bNum) {
                    return -1;
                } else if (aNum > bNum) {
                    return 1;
                }
            }
            return 0;
        }
    }

    public void importTranscript(File transcriptFile) throws IOException {
        coursesToDate.clear();
        PDDocument document = PDDocument.load(transcriptFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setSortByPosition(false);
        String transcriptString = pdfStripper.getText(document);
        transcriptString.lines().forEach(this::processPDFLine);
    }

    private void processPDFLine(String line) {
        Pattern coursePattern = Pattern.compile("[A-Z]{2}[0-9]{3,4}");
        Matcher courseMatcher;
        if(line.matches("^[A-Z]{2}[0-9]{3,4}.*[A-Z]$")){
            courseMatcher = coursePattern.matcher(line);
            courseMatcher.find();
            String courseCode = courseMatcher.group();
            processCourses(line);
        } else if (line.startsWith("BS in")){
            if(line.contains("Computer Science")){
                major = "Computer Science";
            } else if (line.contains("Software Engineering")){
                major = "Software Engineering";
                initializeSEElectives();
                System.out.println("");
            } else {
                System.out.println("Unrecognized Major");
            }
        }
    }

    private void countElectives() {
        electiveCount = 0;
        for (Course c : coursesToDate) {
            if (c.isElective()) {
                electiveCount++;
            }
        }
    }

    private void initializeSEElectives() {
        Map<File, List<Course>> files = new HashMap<>();

        files.put(new File("src/Data/SE-business-electives.txt"), businessElectives);
        files.put(new File("src/Data/SE-free-electives.txt"), freeElectives);
        files.put(new File("src/Data/SE-math-science-electives.txt"), mathScienceElectives);
        files.put(new File("src/Data/SE-program-electives.txt"), programElectives);
        files.put(new File("src/Data/SE-science-electives.txt"), scienceElectives);

        Iterator iterator = files.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Scanner scan = null;
            try {
                scan = new Scanner((File) entry.getKey());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] split = line.split(" - ");
                int credits = 3;
                String description = "";
                String code = "";
                if (split[1].contains("(")) {
                    credits = Integer.parseInt(String.valueOf(split[1].charAt(split[1].indexOf("(") + 1)));
                    description = split[1].substring(0, split[1].indexOf("("));
                } else {
                    description = split[1];
                }
                if (split[1].contains("[")) {
                    description = split[1].substring(0, split[1].indexOf("["));
                }
                code = split[0];
                List<Course> list = (List<Course>) entry.getValue();

                list.add(new Course(code, credits, "", description));
            }
            totalElectives.add((List<Course>) entry.getValue());
        }
    }

    private void initializeCSElectives() {
        Map<File, List<Course>> files = new HashMap<>();

        files.put(new File("src/Data/CS-math-science-electives.txt"), mathScienceElectives);
        files.put(new File("src/Data/CS-program-electives.txt"), programElectives);
        files.put(new File("src/Data/CS-science-electives.txt"), scienceElectives);

        Iterator iterator = files.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Scanner scan = null;
            try {
                scan = new Scanner((File) entry.getKey());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] split = line.split(" - ");
                int credits = 3;
                String description = "";
                String code = "";
                if (split[1].contains("(")) {
                    credits = Integer.parseInt(String.valueOf(split[1].charAt(split[1].indexOf("(") + 1)));
                    description = split[1].substring(0, split[1].indexOf("("));
                } else {
                    description = split[1];
                }
                if (split[1].contains("[")) {
                    description = split[1].substring(0, split[1].indexOf("["));
                }
                code = split[0];
                List<Course> list = (List<Course>) entry.getValue();

                list.add(new Course(code, credits, "", description));
            }
            totalElectives.add((List<Course>) entry.getValue());

        }

    }

    public void processCourses (String line) {

        String[] split = line.split(" ");

        if (catalog.containsKey(split[0])) {

            coursesToDate.add(catalog.get(split[0]));

            int firstIndex = split[split.length - 1].indexOf('.') + 3;
            int lastIndex = split[split.length - 1].lastIndexOf('.') + 3;

            String qualPts = split[split.length -1].substring(0,firstIndex);
            String credsEarned = split[split.length - 1].substring(firstIndex,lastIndex);
            String grade = split[split.length - 1].substring(lastIndex);

            if (grade.equals("F") || grade.equals("W")) {
                coursesToDate.get(coursesToDate.size() - 1).setPassed(false);
                coursesToDate.get(coursesToDate.size() -1).setCompleted(true);

            } else if (grade.equals("WIP")) {
                coursesToDate.get(coursesToDate.size() - 1).setCompleted(true);
            } else {
                coursesToDate.get(coursesToDate.size() - 1).setPassed(true);
                coursesToDate.get(coursesToDate.size() - 1).setCompleted(true);

            }
            for (List<Course> list : totalElectives) {
                for (Course course : list) {
                    if (coursesToDate.get(coursesToDate.size() -1).getName().equalsIgnoreCase(course.getName())) {
                        coursesToDate.get(coursesToDate.size() - 1).setElective(true);
                    }
                }
            }
        }
    }
    public List<Course> graduationPlan() {

        List<Course> graduation = new ArrayList<>();

        List<String> courses = new ArrayList<>();
        if (major.equals("Computer Science")) {
            courses = csTrack;
        } else if (major.equals("Software Engineering")) {
            courses = seTrack;
        }

        String electives[] = {"HUSS", "TECHEL", "MASCIEL", "FREE", "BUSEL", "SCIEL"};
        countElectives();
        for (String code : courses) {

            Course course = catalog.get(code);
            if (!Arrays.asList(electives).contains(code) && course != null) {
                if (!coursesToDate.contains(course)) {
                    graduation.add(course);
                }
            } else {
                if (electiveCount <= 0) {
                    graduation.add(new Course(code, 3, "", ""));
                }
                electiveCount--;
            }
        }

        return graduation;
    }

    public List<Course> recommendCourses () {
        List<Course> recommendedCourses = new ArrayList<>();

        String electives[] = {"HUSS", "TECHEL", "MASCIEL", "FREE", "BUSEL", "SCIEL"};

        for (Course c : coursesToDate) {
            if (!c.isPassed() && !c.isCompleted() && recommendedCoursesTotalCredits(recommendedCourses) < 15) {
                recommendedCourses.sort(Course::compareTo);
                recommendedCourses.add(c);
            }
        }

        List<String> courses = new ArrayList<>();
        if (major.equals("Computer Science")) {
            courses = csTrack;
        } else if (major.equals("Software Engineering")) {
            courses = seTrack;
        }

        for (String code : courses) {

            Course course = catalog.get(code);
            if (!recommendedCourses.contains(course) && !coursesToDate.contains(course) && recommendedCoursesTotalCredits(recommendedCourses) < 15 && !Arrays.asList(electives).contains(code)) {
                recommendedCourses.add(course);
            }

        }

        return recommendedCourses;
    }

    public double recommendedCoursesTotalCredits(List<Course> recCourses) {
        double totalCreds = 0;
        for (Course c : recCourses) {
            if (c != null) {
                totalCreds += c.getCredits();
            }
        }
        return totalCreds;
    }

    /**
     * Method to take in a course name and find the course associated and display the perquisites for that course
     * @param courseName The course code/name taken from the user
     * @return Returns a the list of courses that need to be taken before course, or null if course is invalid
     */
    public List<String> showPrerequisites(String courseName) {
        List<String> courses = new ArrayList<>();
        // Allows for spaces and dashes in course entry
        Course course = catalog.get(courseName.replaceAll("-| ", "").toUpperCase());

        // Check if course exists
        if(course == null) {
            return null;
        }

        String prerequisiteString = course.getPrerequisites();
        String[] prerequisites = prerequisiteString.split(" ");

        // Splits prereq string into seperate entries
        for(String prereq : prerequisites){
            if(prereq.contains("|")){
                // If there are classes with options for prereqs, split them and display in a visually pleasing way
                StringBuilder optionsString = new StringBuilder();
                String[] options = prereq.split("\\|");
                for(String current : options){
                    Course preCourse = catalog.get(current);
                    if (preCourse != null) {
                        String name = preCourse.getName();
                        String description = preCourse.getDescription();
                        optionsString.append(name + " (" + description);
                        optionsString.append(") or ");
                    } else {
                        optionsString.append(current);
                        optionsString.append(" or ");
                    }
                }
                optionsString.delete(optionsString.lastIndexOf(" or "), optionsString.length() - 1);
                courses.add(optionsString.toString());
            } else {
                // Otherwise just add the course to the list with description
                Course singlePrereq = catalog.get(prereq);
                if(singlePrereq != null){
                    courses.add(singlePrereq.getName() + " (" + singlePrereq.getDescription() + ")");
                }
            }
        }
        return courses;
    }
}

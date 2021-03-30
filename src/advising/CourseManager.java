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
    private String major;

    /**
     * Course Manager reads in course data upon initialization
     */
    public CourseManager() {
        try {
            Scanner prereqs = new Scanner(new File("src/Data/prerequisites_updated.csv"));
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

            Scanner offerings = new Scanner(new File("src/Data/offerings.csv"));
            String[] header = offerings.nextLine().split(",");
            while (offerings.hasNextLine()) {
                String[] line = offerings.nextLine().split(",");
                String courseName = line[0];
                Course course = catalog.get(courseName);
                for (int i = 1; i < line.length; i++) {
                    if (!line[i].equals("") && course != null) {
                        int term = Integer.valueOf(line[i]);
                        course.addTerm(term, header[i]);
                    }
                }
            }

            Scanner curiculum = new Scanner(new File("src/Data/curriculum.csv"));
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
            } else {
                System.out.println("Unrecognized Major");
            }
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

            } else if (grade.equals("WIP")) {
                coursesToDate.get(coursesToDate.size() - 1).setCompleted(true);
            } else {
                coursesToDate.get(coursesToDate.size() - 1).setPassed(true);
                coursesToDate.get(coursesToDate.size() - 1).setCompleted(true);

            }
            coursesToDate.get(coursesToDate.size() - 1).setGradeReceived(grade);

        }
    }

    public List<Course> recommendCourses () {
        List<Course> recommendedCourses = new ArrayList<>();

        for (Course c : coursesToDate) {
            if (c.getGradeReceived().equals("F") && recommendedCoursesTotalCredits(recommendedCourses) < 15) {
                recommendedCourses.sort(Course::compareTo);
                recommendedCourses.add(c);
            }
        }
        int index = 0;

        List<String> courses = new ArrayList<>();
        if (major.equals("Computer Science")) {
            courses = csTrack;
        } else if (major.equals("Software Engineering")) {
            courses = seTrack;
        }


        for (String s : courses) {
            if (s.equalsIgnoreCase(coursesToDate.get(coursesToDate.size() - 1).getName())) {
                break;
            }
            ++index;
        }
        while (recommendedCoursesTotalCredits(recommendedCourses) < 15) {
            String courseName = courses.get(index+1);
            Course course = catalog.get(courseName);
            if (!coursesToDate.contains(course)) {
                if (course == null){
                    recommendedCourses.add(new Course(courseName, 3, null, "Free"));
                    recommendedCourses.sort(Course::compareTo);
                } else {
                    recommendedCourses.add(catalog.get(courses.get(++index)));
                    recommendedCourses.sort(Course::compareTo);
                }
            } else{
                index++;
            }

        }

        return recommendedCourses;
    }

    public double recommendedCoursesTotalCredits(List<Course> recCourses) {
        double totalCreds = 0;
        for (Course c : recCourses) {
            totalCreds += c.getCredits();
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
                    optionsString.append(catalog.get(current).getName() + " (" + catalog.get(current).getDescription());
                    optionsString.append(") or ");
                }
                optionsString.delete(optionsString.lastIndexOf(" or "), optionsString.length() - 1);
                courses.add(optionsString.toString());
            } else {
                // Otherwise just add the course to the list with description
                Course singlePrereq = catalog.get(prereq);
                courses.add(singlePrereq.getName() + " (" + singlePrereq.getDescription() + ")");
            }
        }
        return courses;
    }
}

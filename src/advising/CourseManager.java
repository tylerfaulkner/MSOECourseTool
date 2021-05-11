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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Map;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages all courses available from the prerequisites csv
 */
public class CourseManager {
    private static final int NEXT_TERM = 1;
    private static final int MIN_COURSES_IN_TERM = 15;

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

    private List<Course> virtualFailed = new ArrayList<>();

    private String major;

    /**
     * Course Manager reads in course data upon initialization
     */
    public CourseManager(File prerequisites, File offerings, File curr) {
        try {

            readPreReqs(prerequisites);
            readOfferings(offerings);
            readCurriculum(curr);


        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The needed csv files were not found in the directory");
            alert.setHeaderText("Critical Error");
            alert.showAndWait();
        }
    }

    private void readPreReqs(File prerequisiteFile) throws FileNotFoundException{
        Scanner prereqs = new Scanner(prerequisiteFile);
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
    }

    private void readOfferings(File offer) throws FileNotFoundException{
        Scanner offerings = new Scanner(offer);
        String[] header = offerings.nextLine().split(",");
        while (offerings.hasNextLine()) {
            String[] line = offerings.nextLine().split(",");
            String courseName = line[0];
            Course course = catalog.get(courseName);
            if (course == null) {
                course = new Course(courseName, 3, "", "");
                catalog.put(courseName, course);
            }
            for (int i = 1; i < line.length; i++) {
                if (!line[i].equals("")) {
                    int term = Integer.parseInt(line[i]);
                    course.addTerm(term, header[i]);
                }
            }
        }
    }

    private void readCurriculum(File curr) throws FileNotFoundException{
        Scanner curiculum = new Scanner(curr);
        curiculum.nextLine();
        while (curiculum.hasNextLine()) {
            String[] courses = curiculum.nextLine().split(",");
            csTrack.add(courses[0]);
            seTrack.add(courses[1]);
        }
    }

    /**
     * Adds courses to the given listView item that are available in the given term
     *
     * @param term the term of courses to add
     * @return list of strings of all courses available next term
     */
    public List<String> listCourses(int term) {
        List<String> courses = new ArrayList<>();
        for (String name : catalog.keySet()) {
            Course course = catalog.get(name);
            List<String> majors = course.getTerm(term);
            if (majors.size() != 0) {
                courses.add(course.toString());
            }
        }
        courses.sort(new Sort());
        return courses;
    }

    /**
     * Method to allow access to the user's major outside of the
     * course manager
     *
     * @return The user's major
     */
    public String getMajor() {
        return major;
    }

    /**
     * Method to allow access to the computer science major track
     *
     * @return The computer science major track
     */
    public List<String> getCSTrack() {
        return csTrack;
    }

    /**
     * Method to allow access to the software engineering major track
     *
     * @return The software engineering major track
     */
    public List<String> getSETrack() {
        return seTrack;
    }

    /**
     * Method to allow access to all of the courses the user has taken
     *
     * @return A list of courses the user has taken
     */
    List<Course> getCoursesToDate() {
        return coursesToDate;
    }

    HashMap<String, Course> getCatalog() {
        return catalog;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    /**
     * Sets a class as failed for checking what would happen if a course was failed.
     *
     * @param course the course to set as failed virtually
     */
    public void setFailedVirtual(Course course) {
        course.setCompleted(false);
        course.setPassed(false);
        coursesToDate.remove(course);
        virtualFailed.add(course);
    }

    /**
     * Resets all course set as virtually failed
     */
    public void resetVirtual() {
        for (Course course : virtualFailed) {
            course.setCompleted(true);
            course.setPassed(true);
            coursesToDate.add(course);
        }
        virtualFailed.clear();
    }

    /**
     * Removes one virtual failed course from the list
     *
     * @param course the course to return to normal
     */
    public void removeFailedVirtual(Course course) {
        course.setCompleted(true);
        course.setPassed(true);
        coursesToDate.add(course);
        virtualFailed.remove(course);
    }

    /**
     * Used to sort listed course in list view
     */
    private static class Sort implements Comparator<String> {

        public int compare(String a, String b) {
            a = a.split(" ")[0];
            b = b.split(" ")[0];
            String aMajor = a.substring(0, 2);
            String bMajor = b.substring(0, 2);
            int compare = aMajor.compareTo(bMajor);
            if (compare != 0) {
                return compare;
            } else {
                int aNum = Integer.parseInt(a.substring(2));
                int bNum = Integer.parseInt(b.substring(2));
                if (aNum < bNum) {
                    return -1;
                } else if (aNum > bNum) {
                    return 1;
                }
            }
            return 0;
        }
    }

    /**
     * Counts the amount of electives the user has taken.
     */
    private void countElectives() {
        electiveCount = 0;
        for (Course c : coursesToDate) {
            if (c.isElective()) {
                electiveCount++;
            }
        }
    }

    /**
     * Reads in all the files that dictate possible electives for SE majors
     */
    public void initializeSEElectives() {
        Map<File, List<Course>> files = new HashMap<>();

        files.put(new File("src/Data/SE-business-electives.txt"), businessElectives);
        files.put(new File("src/Data/SE-free-electives.txt"), freeElectives);
        files.put(new File("src/Data/SE-math-science-electives.txt"), mathScienceElectives);
        files.put(new File("src/Data/SE-program-electives.txt"), programElectives);
        files.put(new File("src/Data/SE-science-electives.txt"), scienceElectives);

        initializeTotalElectives(files);

    }

    /**
     * Reads in all available electives for CS majors.
     */
    public void initializeCSElectives() {
        Map<File, List<Course>> files = new HashMap<>();

        files.put(new File("src/Data/CS-math-science-electives.txt"), mathScienceElectives);
        files.put(new File("src/Data/CS-program-electives.txt"), programElectives);
        files.put(new File("src/Data/CS-science-electives.txt"), scienceElectives);

        initializeTotalElectives(files);

    }

    /**
     * Sets the lists for each elective type determined by major.
     *
     * @param files a map of files with the list to add the courses to
     */
    private void initializeTotalElectives(Map<File, List<Course>> files) {

        //Iterator setup to read through each of the entries in the map
        Iterator<Map.Entry<File, List<Course>>> iterator = files.entrySet().iterator();

        //While there is a next entry in the map
        while (iterator.hasNext()) {
            //Get the next entry in the map
            Map.Entry<File, List<Course>> entry = iterator.next();

            //The method of reading in data from the .txt file
            Scanner scan = null;
            try {
                scan = new Scanner(entry.getKey());
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Needed Elective File Missing");
                String filename = entry.getKey().getName();
                String content = "The file " + filename + " is missing or in the wrong directory";
                alert.setContentText(content);
            }

            //While the file has another line
            while (scan.hasNextLine()) {
                String line = scan.nextLine();

                //Split the line between course code and description of the course
                String[] split = line.split(" - ");

                int credits = 3;
                String description = "";
                String code = "";

                /* This is all formating for the description
                 * Example Line: CS2040 - Description of C++ Class (3 Credits) [Prereq: CS 2039]
                 * The goal is to remove the credits and prereqs from the description
                 */

                //Basically checks if the line contains the amount of credits it is worth
                if (split[1].contains("(")) {
                    //Obtains the index of the # of credits from the string
                    int indexOfNum = split[1].indexOf("(") + 1;

                    //Pulls that number and creates a string out of it
                    String credStr = String.valueOf(split[1].charAt(indexOfNum));

                    //Obtains the credits as a number
                    credits = Integer.parseInt(credStr);

                    //Chops anything after the first '(' in the string
                    description = split[1].substring(0, split[1].indexOf("("));

                } else {
                    //This means that the description doesn't contain credits
                    // so it is the full string
                    description = split[1];
                }

                //Basically checks if the line contains any prereqs
                if (split[1].contains("[")) {
                    //Chops anything after the first '[' in the string
                    description = split[1].substring(0, split[1].indexOf("["));
                }
                code = split[0];

                //Gets the list you want to add the elective to
                List<Course> list = entry.getValue();

                list.add(new Course(code, credits, "", description));
            }

            //Adds all of the electives to a total elective list
            totalElectives.add(entry.getValue());
        }

    }

    /**
     * Returns all courses marked as WIP
     *
     * @return list of all courses the user is currently taking
     */
    public List<Course> getCurrentCourses() {
        Set<String> courseNames = catalog.keySet();
        List<Course> currentCourse = new ArrayList<>();
        for (String course : courseNames) {
            Course c = catalog.get(course);
            if (c.getWIP()) {
                currentCourse.add(c);
            }
        }
        return currentCourse;
    }


    /**
     * This method takes in a line from the pdf and process the course associated with it
     * The method will:
     * 1. Add the course to your total courses
     * 2. Check the grades and determine if you passed and/or completed it
     * 3. Check if it is an elective class
     *
     * @param line The line from the pdf
     * @param term the term to set for the course
     */
    public void processCourses(String line, String term) {

        Pattern coursePattern = Pattern.compile("[A-Z]{2}[0-9]{3,4}");
        Matcher courseMatcher;
        courseMatcher = coursePattern.matcher(line);
        courseMatcher.find();
        String courseCode = courseMatcher.group();

        if (catalog.containsKey(courseCode)) {
            Pattern gradePattern = Pattern.compile("[A-Z]+$");
            Matcher gradeMatcher;
            gradeMatcher = gradePattern.matcher(line);
            gradeMatcher.find();
            String grade = gradeMatcher.group();
            coursesToDate.add(catalog.get(courseCode));

            Course lastCourse = coursesToDate.get(coursesToDate.size() - 1);

            //If you failed or withdrew from the class,
            //you didn't pass but you did complete the class
            if (grade.equals("F") || grade.equals("W")) {
                lastCourse.setPassed(false);
                lastCourse.setCompleted(true);

                //If you are currently working through the class, you completed it
            } else if (grade.equals("WIP")) {
                lastCourse.setCompleted(true);
                lastCourse.setWIP(true);
                //Other than those two cases, you completed and passed it
            } else {
                lastCourse.setPassed(true);
                lastCourse.setCompleted(true);

            }
            lastCourse.setCompletedTerm(term);
            for (List<Course> list : totalElectives) {
                for (Course course : list) {
                    if (lastCourse.getName().equalsIgnoreCase(course.getName())) {
                        lastCourse.setElective(true);
                    }
                }
            }
        }
    }

    /**
     * The method that determines the courses you have to take to graduate
     *
     * @return A full list of course you have to take
     */

    public HashMap<String, Set<Course>> getCoursesByCompleteTerm() {
        HashMap<String, Set<Course>> coursesByTerm = new HashMap<>();
        for (Course course : coursesToDate) {
            if (!coursesByTerm.containsKey(course.getCompletedTerm())) {
                coursesByTerm.put(course.getCompletedTerm(), new HashSet<>());
            }
            coursesByTerm.get(course.getCompletedTerm()).add(course);
        }
        return coursesByTerm;
    }

    /**
     * Generates and returns the user's graduation plan
     *
     * @return list of courses the user should take until graduation
     */
    public List<Course> graduationPlan() {

        List<Course> graduation = new ArrayList<>();

        //Determining which track to use for your major
        List<String> courses = new ArrayList<>();
        if (major.equals("Computer Science")) {
            courses = csTrack;
        } else if (major.equals("Software Engineering")) {
            courses = seTrack;
        }

        String[] electives = {"HUSS", "TECHEL", "MASCIEL", "FREE", "BUSEL", "SCIEL"};
        countElectives();
        for (String code : courses) {

            Course course = catalog.get(code);
            if (!Arrays.asList(electives).contains(code) && course != null) {
                //In the real CS track CH200 is equal to BI102
                boolean ch200Equivalent = code.equals("CH200") && courses.equals(csTrack)
                        && coursesToDate.contains(catalog.get("BI102"));
                if (!coursesToDate.contains(course) && !ch200Equivalent) {
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

    /**
     * The method that determines which courses you are most likely
     * needed to take next trimester
     *
     * @return A list of courses thats credits are < 15
     */
    public List<Course> recommendCourses() {
        List<Course> recommendedCourses = new ArrayList<>();

        String[] electives = {"HUSS", "TECHEL", "MASCIEL", "FREE", "BUSEL", "SCIEL"};

        //Checks to see if any courses were failed, if they were add them to the list.
        for (Course c : coursesToDate) {
            List<String> majors = c.getTerm(NEXT_TERM);
            boolean availableNextTerm = !majors.isEmpty() && majors.contains(major);
            if (!c.isPassed() && !c.isCompleted() && availableNextTerm
                    && getTotalCredits(recommendedCourses) < MIN_COURSES_IN_TERM) {
                recommendedCourses.sort(Course::compareTo);
                recommendedCourses.add(c);
            }
        }

        //Determining which track to use for your major
        List<String> courses = new ArrayList<>();
        String majorCode = "";
        if (major.equals("Computer Science")) {
            courses = csTrack;
            majorCode = "CS";
        } else if (major.equals("Software Engineering")) {
            courses = seTrack;
            majorCode = "SE";
        }

        for (String code : courses) {

            Course course = catalog.get(code);

            boolean electiveCourse = Arrays.asList(electives).contains(code);
            boolean notEnoughCredits = getTotalCredits(recommendedCourses) < MIN_COURSES_IN_TERM;

            /* Checks if:
            1. Recommended courses already contains the course
            2. If you have already taken the course
            3. If the total recommended course credits so far is more than 15
            4. If it is an elective course
            5. Course isn't null
            6. Course is available next Term
             */
            if (course != null && !recommendedCourses.contains(course)
                    && !coursesToDate.contains(course)
                    && notEnoughCredits && !electiveCourse) {
                List<String> majors = course.getTerm(NEXT_TERM);
                boolean availableNextTerm = majors.contains(majorCode);
                if (availableNextTerm) {
                    recommendedCourses.add(course);
                }
            }

        }

        return recommendedCourses;
    }

    /**
     * Counts the total credits from a list of courses
     *
     * @param recCourses The list of courses
     * @return The total credits the list is worth currently
     */
    private double getTotalCredits(List<Course> recCourses) {
        double totalCreds = 0;
        for (Course c : recCourses) {
            if (c != null) {
                totalCreds += c.getCredits();
            }
        }
        return totalCreds;
    }

    /**
     * Method to take in a course name
     * and find the course associated and display the perquisites for that course
     *
     * @param courseName The course code/name taken from the user
     * @return Returns a the list of course that need to be taken before course,
     * or null if course is invalid
     */
    public List<String> showPrerequisites(String courseName) {
        List<String> courses = new ArrayList<>();
        // Allows for spaces and dashes in course entry
        Course course = catalog.get(courseName.replaceAll("-| ", "").toUpperCase());

        // Check if course exists
        if (course == null) {
            return null;
        }

        String prerequisiteString = course.getPrerequisites();
        String[] prerequisites = prerequisiteString.split(" ");

        // Splits prereq string into seperate entries
        for (String prereq : prerequisites) {
            if (prereq.contains("|")) {
                // If there are classes with options for prereqs,
                // split them and display in a visually pleasing way
                courses.add(formatORPreReqs(prereq));
            } else {
                // Otherwise just add the course to the list with description
                Course singlePrereq = catalog.get(prereq);
                if (singlePrereq != null) {
                    courses.add(singlePrereq.getName()
                            + " (" + singlePrereq.getDescription() + ")");
                }
            }
        }
        return courses;
    }

    private String formatORPreReqs(String prereq){
        StringBuilder optionsString = new StringBuilder();
        String[] options = prereq.split("\\|");
        for (String current : options) {
            Course preCourse = catalog.get(current);
            if (preCourse != null) {
                String name = preCourse.getName();
                String description = preCourse.getDescription();
                String toAppend = name + " (" + description;
                optionsString.append(toAppend);
                optionsString.append(") or ");
            } else {
                optionsString.append(current);
                optionsString.append(" or ");
            }
        }
        optionsString.delete(optionsString.lastIndexOf(" or "), optionsString.length() - 1);
        return optionsString.toString();
    }
}

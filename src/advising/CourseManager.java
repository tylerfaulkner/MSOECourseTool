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
import javafx.scene.control.ListView;
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
            try {
                catalog.get(courseCode).setCompleted(true);
            } catch (NullPointerException e) {
                System.out.println("Course not listed in offerings: " + courseCode);
            }
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
}

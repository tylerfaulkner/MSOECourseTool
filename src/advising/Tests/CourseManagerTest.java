/*
 * Course: SE 2800
 * Section 41
 * Dr. Jonathon Magana
 * Test Class
 * Created by: Derek Gauger, Kian Dettlaff, Roberto Garcia, and Tyler Faulkner
 * April 27th, 2021
 */
package advising.Tests;
import advising.CourseManager;
import org.testng.annotations.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.*;

/**
 * Class for testing PBI's 1-3
 */
public class CourseManagerTest {
    File testCurriculum = new File("src/Data/TestCSV/testCurr.txt");
    File testOfferings = new File("src/Data/TestCSV/testOfferings.txt");
    File testPrerequisites = new File("src/Data/TestCSV/testPrereq.txt");
    CourseManager cm;

    /**
     * Data Provider for list courses
     * @return term, expected list of courses
     */
    @DataProvider(name = "listCoursesDP")
    public Object[][] listCoursesDP(){
        List<String> expected1 = new ArrayList<>(Arrays.asList("BA1220 Microeconomics", "BA2220 Foundations of Business Economics", "BA2331 Business Law", "BA2501 Finance I"));
        List<String> expected2 = new ArrayList<>(Arrays.asList("BA1220 Microeconomics", "BA2220 Foundations of Business Economics"));
        List<String> expected3 = new ArrayList<>(Arrays.asList("BA2220 Foundations of Business Economics", "BA2222 Macroeconomics"));
        return new Object[][] {
                {1, expected1},
                {2, expected2},
                {3, expected3}
        };
    }

    /**
     * Method for testing list courses
     * @param term integer for 1, 2, 3
     * @param expected List of expected courses returned
     */
    @Test(dataProvider = "listCoursesDP")
    public void testListCourses(int term, List<String> expected) {
        //Arrange
        File curriculum = new File("src/Data/curriculum.csv");
        File prerequisites = new File("src/Data/prerequisites_updated.csv");
        cm = new CourseManager(prerequisites, testOfferings, curriculum);
        //Act/Assert
        assertEquals(cm.listCourses(term), expected);
    }

    /**
     * Method for testing if the CS track is correctly returned
     */
    @Test
    public void testGetCSTrack() {
        //Arrange
        List<String> expectedCS = new ArrayList<>(Arrays.asList("this", "is", "valid", "CS"));
        File prerequisites = new File("src/Data/prerequisites_updated.csv");
        File offerings = new File("src/Data/offerings.csv");
        cm = new CourseManager(prerequisites, offerings, testCurriculum);
        //Act/Assert
        assertEquals(expectedCS, cm.getCSTrack(), "Did not return correct CS track");
    }

    /**
     * Method for testing if the SE track is correctly returned
     */
    @Test
    public void testGetSETrack() {
        //Arrange
        List<String> expectedSE = new ArrayList<>(Arrays.asList("SE", "valid", "this", "is"));
        File prerequisites = new File("src/Data/prerequisites_updated.csv");
        File offerings = new File("src/Data/offerings.csv");
        cm = new CourseManager(prerequisites, offerings, testCurriculum);
        //Act/Assert
        assertEquals(expectedSE, cm.getSETrack(), "Did not return correct SE track");
    }

    /**
     * Data provider for testing prerequisites getter
     * @return Course name, Expected list of returned courses
     */
    @DataProvider(name = "showPrereqDP")
    public Object[][] showPrereqDP(){
        List<String> expected1 = new ArrayList<>(Arrays.asList("BA2222 (Macroeconomics)"));
        List<String> expected2 = new ArrayList<>(Arrays.asList("OR or Test "));
        return new Object[][] {
                {"BA2220", expected1},
                {"BA 2220", expected1},
                {"BA-2220", expected1},
                {"BA2501", expected2},
                {"BA 2501", expected2},
                {"BA-2501", expected2}
        };
    }

    /**
     * Test for testing prerequisites
     * @param course The course string passed in
     * @param prereqs The list of expected prerequisites
     */
    @Test(dataProvider = "showPrereqDP")
    public void testShowPrerequisites(String course, List<String> prereqs) {
        //Arrange
        cm = new CourseManager(testPrerequisites, testOfferings, testCurriculum);
        //Act
        List<String> prereqList = cm.showPrerequisites(course);
        //Assert
        assertEquals(prereqList, prereqs, "Didn't return correct list of prerequisites");
    }
}
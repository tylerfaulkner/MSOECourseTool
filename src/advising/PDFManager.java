/**
 * Course: SE 2800
 * Section 41
 * Dr. Jonathon Magana
 * Advising Tool
 * Created by: Kian Dettlaff
 * March 18th, 2021
 */
package advising;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

/**
 * Manages all PDF export and import functionality
 */
public class PDFManager {

    private CourseManager courseManager;
    private static final PDFont STANDARD_FONT = PDType1Font.HELVETICA;
    private static final PDFont BOLD_FONT = PDType1Font.HELVETICA_BOLD;
    private static final PDFont OBLIQUE_FONT = PDType1Font.HELVETICA_OBLIQUE;
    private static final int TEXT_SIZE = 10;
    private static final float LEADING_FACTOR = 1.2f;
    private static final int TOP_MARGIN = 80;
    private static final int TITLE_SIZE = 16;
    private static final int LEFT_MARGIN = 80;
    private static final int BOTTOM_MARGIN = 80;
    private static final int THOUSAND = 1000;
    private static final int X_OFFSET = 8;
    private static final int COL_OFFSET = 5;

    private float x;
    private float y;

    private String major = "External Institution";

    /**
     * Initializes PDF manager
     *
     * @param courseManager the course manager from the controller
     */
    public PDFManager(CourseManager courseManager) {
        this.courseManager = courseManager;
    }

    /**
     * Attempts to import a transcript
     *
     * @param transcriptFile the file of the transcript
     * @throws IOException if a file error occurs
     */
    public void importTranscript(File transcriptFile) throws IOException {
        courseManager.getCoursesToDate().clear();
        PDDocument document = PDDocument.load(transcriptFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setSortByPosition(false);
        String transcriptString = pdfStripper.getText(document);
        transcriptString.lines().forEach(this::processPDFLine);
    }

    private void processPDFLine(String line) {
        if (line.contains("Quarter")) {
            major = line;
        }
        if (line.matches("^[A-Z]{2}[0-9]{3,4}.*[A-Z]$")) {
            courseManager.processCourses(line, major);
        } else if (line.startsWith("BS in")) {
            if (line.contains("Computer Science")) {
                courseManager.setMajor("Computer Science");
                courseManager.initializeCSElectives();
            } else if (line.contains("Software Engineering")) {
                courseManager.setMajor("Software Engineering");
                courseManager.initializeSEElectives();
            } else {
                System.out.println("Unrecognized Major");
            }
        }
    }

    /**
     * Attempts to export the current transcript
     *
     * @param path the path to save the exported transcript
     * @throws IOException if a file error occurs
     */
    public void exportTranscript(Path path) throws IOException {
        PDDocument transcript = new PDDocument();

        HashMap<String, Set<Course>> coursesByTerm = courseManager.getCoursesByCompleteTerm();
        while (!coursesByTerm.isEmpty()) {
            PDPage page = new PDPage(
                    new PDRectangle(PDRectangle.LETTER.getHeight(),
                    PDRectangle.LETTER.getWidth()));
            transcript.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(transcript, page);

            writeHeader(stream, page);
            writeCourses(coursesByTerm, stream, page);

            stream.close();
        }
        transcript.save(path.toString());
        transcript.close();
    }

    private void writeHeader(PDPageContentStream stream, PDPage page) throws IOException {
        stream.setFont(PDFManager.OBLIQUE_FONT, TITLE_SIZE);
        stream.setLeading(TITLE_SIZE * PDFManager.LEADING_FACTOR);
        float width = PDFManager.OBLIQUE_FONT.getStringWidth(
                "Milwaukee School of Engineering") / THOUSAND * TITLE_SIZE;
        x = (page.getMediaBox().getWidth() - width) / 2;
        y = page.getMediaBox().getHeight() - TOP_MARGIN;
        stream.beginText();
        stream.newLineAtOffset(x, y);
        stream.showText("Milwaukee School of Engineering");
        stream.newLine();
        stream.showText("Unofficial Transcript");
        stream.endText();

        stream.setFont(PDFManager.BOLD_FONT, TEXT_SIZE);
        stream.setLeading(TEXT_SIZE * PDFManager.LEADING_FACTOR);
        stream.beginText();
        x = page.getMediaBox().getWidth() / 4;
        y = y - 2 * TITLE_SIZE;
        stream.newLineAtOffset(x, y);
        stream.showText("DEGREE SOUGHT:");
        stream.newLine();
        stream.showText("BS in " + courseManager.getMajor());
        stream.newLineAtOffset(x, TEXT_SIZE);
        stream.showText("DATE DEGREE GRANTED");
        stream.newLine();
        stream.showText("Incomplete");
        stream.endText();

        stream.beginText();
        stream.setFont(PDFManager.STANDARD_FONT, TEXT_SIZE);
        x = page.getMediaBox().getWidth() / X_OFFSET;
        y = y - 4 * TEXT_SIZE;
        stream.newLineAtOffset(x, y);
        stream.showText("ID: 101010");
        stream.newLine();
        stream.showText("NAME: Case, Edge T");
        stream.newLine();
        stream.showText("SSN: xxx-xx-0101");
        stream.newLine();
        stream.showText("Date Printed: MM/DD/YYYY");
        stream.endText();
    }

    private void writeCourses(
            HashMap<String, Set<Course>> coursesByTerm, PDPageContentStream stream, PDPage page) throws IOException {
        boolean firstColumnFull = false;
        boolean secondColumnFull = false;

        boolean result = false;
        startColumnOne(stream, page);
        String[] terms = {};
        terms = coursesByTerm.keySet().toArray(terms);
        for (String term : terms) {
            if (!secondColumnFull) {
                stream.setFont(BOLD_FONT, TEXT_SIZE);
                stream.setLeading(TEXT_SIZE * LEADING_FACTOR);

                newColumnLine(stream, page, term);

                Course[] termCourses = coursesByTerm.get(term).toArray(Course[]::new);
                for (Course course : termCourses) {
                    stream.setFont(STANDARD_FONT, TEXT_SIZE);

                    if (!secondColumnFull) {
                        if (course.isPassed()) {
                            result = newColumnLine(stream, page,
                                    course.getName() + " " + course.getDescription() + "   P");
                        } else if (course.isCompleted()) {
                            result = newColumnLine(stream, page,
                                    course.getName() + " " + course.getDescription() + "   WIP");
                        } else {
                            result = newColumnLine(stream, page,
                                    course.getName() + " " + course.getDescription() + "   F");
                        }
                        coursesByTerm.get(term).remove(course);
                        if (result && firstColumnFull) {
                            secondColumnFull = true;
                        } else if (result) {
                            firstColumnFull = true;
                            stream.endText();
                            startColumnTwo(stream, page);
                        }
                    }
                }
                if (coursesByTerm.get(term).isEmpty()) {
                    coursesByTerm.remove(term);
                }
            }
        }
        stream.endText();
    }

    private boolean newColumnLine(
            PDPageContentStream stream, PDPage page, String contents) throws IOException {
        stream.showText(contents);
        stream.newLine();
        y -= TEXT_SIZE * LEADING_FACTOR;
        return y < BOTTOM_MARGIN;
    }

    private void startColumnOne(PDPageContentStream stream, PDPage page) throws IOException {
        float columnTop = page.getMediaBox().getHeight() * 3 / COL_OFFSET;
        x = LEFT_MARGIN;
        y = columnTop;
        stream.beginText();
        stream.newLineAtOffset(x, y);
    }

    private void startColumnTwo(PDPageContentStream stream, PDPage page) throws IOException {
        float columnTop = page.getMediaBox().getHeight() * 3 / COL_OFFSET;
        x = LEFT_MARGIN + page.getMediaBox().getWidth() / 2;
        y = columnTop;
        stream.beginText();
        stream.newLineAtOffset(x, y);
    }
}

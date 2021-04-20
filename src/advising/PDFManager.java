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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFManager {

    CourseManager courseManager;
    private static final PDFont STANDARD_FONT = PDType1Font.HELVETICA;
    private static final PDFont BOLD_FONT = PDType1Font.HELVETICA_BOLD;
    private static final PDFont OBLIQUE_FONT = PDType1Font.HELVETICA_OBLIQUE;
    private static final int TEXT_SIZE = 8;
    private static final float LEADING_FACTOR = 1.2f;
    private static final int TOP_MARGIN = 80;
    private static final int TITLE_SIZE = 16;

    public PDFManager(CourseManager courseManager){
        this.courseManager = courseManager;
    }

    public void importTranscript(File transcriptFile) throws IOException {
        courseManager.getCoursesToDate().clear();
        PDDocument document = PDDocument.load(transcriptFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setSortByPosition(false);
        String transcriptString = pdfStripper.getText(document);
        transcriptString.lines().forEach(this::processPDFLine);
    }

    private void processPDFLine(String line) {
        Pattern coursePattern = Pattern.compile("[A-Z]{2}[0-9]{3,4}");
        Matcher courseMatcher;
        if (line.matches("^[A-Z]{2}[0-9]{3,4}.*[A-Z]$")) {
            courseMatcher = coursePattern.matcher(line);
            courseMatcher.find();
            String courseCode = courseMatcher.group();
            courseManager.processCourses(line);
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

    public void exportTranscript(Path path) throws IOException {
        PDDocument transcript = new PDDocument();

        HashMap<String, Set<Course>> coursesByTerm = courseManager.getCoursesByCompleteTerm();

        while (!coursesByTerm.isEmpty()) {
            PDPage page = new PDPage(new PDRectangle(PDRectangle.LETTER.getHeight(), PDRectangle.LETTER.getWidth()));
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
        float width = PDFManager.OBLIQUE_FONT.getStringWidth("Milwaukee School of Engineering") / 1000 * TITLE_SIZE;
        float x = (page.getMediaBox().getWidth() - width) / 2;
        float y = page.getMediaBox().getHeight() - TOP_MARGIN;
        stream.beginText();
        stream.newLineAtOffset(x, y);
        stream.showText("Milwaukee School of Engineering");
        stream.newLine();
        stream.showText("Unofficial Transcript");
        stream.endText();

        int textSize = 8;
        stream.setFont(PDFManager.BOLD_FONT, textSize);
        stream.setLeading(textSize * PDFManager.LEADING_FACTOR);
        stream.beginText();
        x = page.getMediaBox().getWidth() / 4;
        y = y - 2 * TITLE_SIZE;
        stream.newLineAtOffset(x, y);
        stream.showText("DEGREE SOUGHT:");
        stream.newLine();
        stream.showText("BS in " + courseManager.getMajor());
        stream.newLineAtOffset(x, textSize);
        stream.showText("DATE DEGREE GRANTED");
        stream.newLine();
        stream.showText("Incomplete");
        stream.endText();

        stream.beginText();
        stream.setFont(PDFManager.STANDARD_FONT, textSize);
        x = page.getMediaBox().getWidth() / 8;
        y = y - 4 * textSize;
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

    private void writeCourses(HashMap<String, Set<Course>> coursesByTerm, PDPageContentStream stream, PDPage page) throws IOException {
        boolean firstColumn = true;
        float columnTop = page.getMediaBox().getHeight() * 3 / 5;

        for (String term : coursesByTerm.keySet()) {
            stream.setFont(OBLIQUE_FONT, TEXT_SIZE);
            stream.setLeading(TEXT_SIZE * LEADING_FACTOR);
            Course[] termCourses = coursesByTerm.get(term).toArray(Course[]::new);
            for (Course course : termCourses) {
                //print out course details in first column until space runs out
                //print out course details in second column until space runs out
                //return
                System.out.println(course.getName());
                coursesByTerm.get(term).remove(course);
            }
            coursesByTerm.remove(term);
        }
    }
}

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;


public class Main {

    static List<Suggest> previouslyAdded = new ArrayList<>();
    static List<Suggest> suggestForRemoval = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        // Создаём конфиг.
        LinksSuggester linksSuggester = new LinksSuggester(new File("data/config"));

        var dir = new File("data/pdfs");

        // Перебираем пдфки в data/pdfs.
        for (var fileIn : dir.listFiles()) {

            // Для каждой пдфки создаём новую в data/converted.
            var fileOut = new File("data/converted/" + "converted_" + fileIn.getName());
            var doc = new PdfDocument(new PdfReader(fileIn), new PdfWriter(fileOut));
            int totalPages = doc.getNumberOfPages();

            // Перебираем страницы pdf.
            for (int i = 1; i <= totalPages; i++) {

                PdfPage pdfPage = doc.getPage(i);
                String content = PdfTextExtractor.getTextFromPage(pdfPage);
                //System.out.println(content);

                List<Suggest> reference = linksSuggester.suggest(content);
                if (reference.isEmpty()) {
                    continue;
                }

                // Проверить наличие ранее вставленных ссылок в документе.
                recommendationsAlreadyAdded(reference);

                if (reference.isEmpty()) {
                    // Не добавляю страницы, если ранее была добавлена ссылка на suggest.
                    continue;
                }

                // Если в странице есть неиспользованные ключевые слова, создаём новую страницу за ней.
                PdfPage newPage = doc.addNewPage(i + 1);

                Rectangle rect = new Rectangle(newPage.getPageSize()).moveRight(10).moveDown(10);
                Canvas canvas = new Canvas(newPage, rect);
                Paragraph paragraph = new Paragraph("Suggestions:\n");
                paragraph.setFontSize(25);

                for (Suggest suggest : reference) {
                    // Вставляем туда рекомендуемые ссылки из конфига.
                    addSuggestToThePage(suggest, rect, paragraph);
                }

                canvas.add(paragraph);
                i += 1;

            }

            doc.close();
            previouslyAdded.clear();
            suggestForRemoval.clear();
        }

    }

    public static List<Suggest> recommendationsAlreadyAdded(List<Suggest> sug) {

        for (Suggest suggest : sug) {
            if (previouslyAdded.indexOf(suggest) == -1) {
                previouslyAdded.add(suggest);
            } else {
                suggestForRemoval.add(suggest);
            }
        }
        for (Suggest suggest : suggestForRemoval) {
            sug.remove(suggest);
        }

        return sug;
    }

    public static void addSuggestToThePage(Suggest suggest, Rectangle rect, Paragraph paragraph) {

        PdfLinkAnnotation annotation = new PdfLinkAnnotation(rect);
        PdfAction action = PdfAction.createURI(suggest.getUrl());

        annotation.setAction(action);

        Link link = new Link(suggest.getTitle(), annotation);

        paragraph.add(link.setUnderline());
        paragraph.add("\n");

        previouslyAdded.add(suggest);

    }

}

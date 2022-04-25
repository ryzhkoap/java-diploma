import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LinksSuggester {

    List<Suggest> list = new ArrayList();

    public LinksSuggester(File file) throws IOException, WrongLinksFormatException {

        // Прочитать входящий файл.
        try (FileReader reader = new FileReader(file)) {
            char[] buf = new char[256];
            int c;
            while ((c = reader.read(buf)) > 0) {
                if (c < 256) {
                    buf = Arrays.copyOf(buf, c);
                }
            }

            // Файл прочитан в массив. Вытащим из массива данные.
            creatSuggestObj(buf);

        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public List<Suggest> suggest(String text) {

        List<Suggest> result = new ArrayList<>();

        if (text.contains("Suggestions:\n")) {
            return result;
        }

        for (Suggest suggest : list) {
            String keyWord = suggest.getKeyWord();
            var contained = text.toLowerCase(Locale.ROOT).contains(keyWord.toLowerCase(Locale.ROOT));

            if (contained) {
                result.add(suggest);
            } else {
                //System.out.println("Не найдено ключесвое слово " + keyWord);
                continue;
            }
        }
        return result;
    }

    public void creatSuggestObj(char[] buf) {

        String str = "";

        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == '\r') {
                String[] strSplit = str.split("\t");
                if (strSplit.length != 3)
                    throw new WrongLinksFormatException("Строка должна состоять из 3-х частей. Проверьте файл config.");

                list.add(new Suggest(strSplit[0], strSplit[1], strSplit[2]));
                str = "";
            } else {
                if (buf[i] != '\n') {
                    str += buf[i];
                }
            }
        }
    }

}

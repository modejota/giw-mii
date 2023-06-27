package org.modejota.utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author José Alberto Gómez García
 * @since 20-Feb-2023
 */
public class StopWordsReader {

    /**
     * Método para leer las palabras vacías de un idioma dado un fichero especificado en la configuración.
     * @return Lista de palabras vacías a tener en cuenta en buscador e indexador.
     * @throws IOException
     */
    public static List<String> readStopWords() throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(Configuration.STOPWORDS_FILE));
        List<String> stopWords = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            stopWords.add(line);
        }
        reader.close();
        return stopWords;
    }

}

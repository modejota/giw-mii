package org.modejota.buscador;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.modejota.utilities.Configuration;
import org.modejota.utilities.StopWordsReader;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author José Alberto Gómez García
 * @since 27-Feb-2023
 */
public class Buscador {

    private IndexSearcher indexSearcher;
    private QueryParser queryParser;
    private Query query;

    /**
     * Constructor del objeto buscador. Sobre él se realizarán las búsquedas
     * @throws IOException
     */
    public Buscador() throws IOException {
        // Obtener directorio del índice del sistema de archivos
        Directory indexDirectory = FSDirectory.open(Paths.get(Configuration.INDEX_DIR));
        try {
            IndexReader indexReader = DirectoryReader.open(indexDirectory);
            indexSearcher = new IndexSearcher(indexReader);

            // El fichero de las palabras vacías viene de la configuración (ya sea del fichero o del usuario)
            List<String> words = StopWordsReader.readStopWords();
            CharArraySet stopWords = StopFilter.makeStopSet(words, true);

            Analyzer analyzer;  // Especializaremos en función del idioma
            if (Configuration.LANGUAGE.equalsIgnoreCase("ES"))
                analyzer = new SpanishAnalyzer(stopWords);
            else if (Configuration.LANGUAGE.equalsIgnoreCase("EN"))
                analyzer = new EnglishAnalyzer(stopWords);
            else
                analyzer = new StandardAnalyzer(stopWords);

            queryParser = new QueryParser("contents", analyzer);

        } catch (IOException e) {
            System.out.println("Error en la creación del buscador. \n" + e.getMessage());
        }
    }

    /**
     * Método para realizar una búsqueda
     * @param searchQuery Término a buscar en la colección documental
     * @return Documentos que contienen el término den la colección. El número lo marca la configuración.
     * @throws ParseException
     * @throws IOException
     */
    public TopDocs search(String searchQuery) throws ParseException, IOException {
        query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, Configuration.MAX_SEARCH_RESULTS);
    }

    /**
     * Método para obtener un documento concreto.
     * @param scoreDoc Objeto devuelto por el buscador, que representa un documento encontrado.
     * @return Documento de la colección
     * @throws IOException
     */
    public Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

}

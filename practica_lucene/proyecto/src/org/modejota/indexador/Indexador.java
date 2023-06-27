package org.modejota.indexador;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.modejota.utilities.Configuration;
import org.modejota.utilities.StopWordsReader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.modejota.utilities.DeleteDirectory.borrarDirectorio;

/**
 * @author José Alberto Gómez García
 * @since 20-Feb-2023
 */
public class Indexador {

    private final IndexWriter writer;

    /**
     * Constructor del indexador. Crea el objeto básico necesario para luego crear el índice y realizar la indexación
     * @throws IOException
     */
    public Indexador() throws IOException {
        // Palabras vacías vienen de la configuración
        List<String> words = StopWordsReader.readStopWords();
        CharArraySet stopWords = StopFilter.makeStopSet(words, true);
        Analyzer analyzer;  // Especializado en función del idioma
        if (Configuration.LANGUAGE.equalsIgnoreCase("ES"))
            analyzer = new SpanishAnalyzer(stopWords);
        else if (Configuration.LANGUAGE.equalsIgnoreCase("EN"))
            analyzer = new EnglishAnalyzer(stopWords);
        else
            analyzer = new StandardAnalyzer(stopWords);

        // Gestión del directorio del índice. De existir se sobreescribe, si no se crea.
        File directorio = new File(Configuration.INDEX_DIR);
        if (directorio.exists() && directorio.isDirectory()) {
            System.out.println("Borrando directorio de índice existente: " + Configuration.INDEX_DIR);
            borrarDirectorio(directorio);
        }
        if (!directorio.exists()) {
            directorio.mkdir();
            System.out.println("Directorio para índice creado: " + Configuration.INDEX_DIR);
        }
        Directory indexDirectory = FSDirectory.open(Paths.get(Configuration.INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexDirectory, config);
    }

    /**
     * A partir de la ruta del fichero, realizar el procesamiento para convertirlo en documento de Lucene
     * @param file Ruta al fichero a procesar
     * @return Documento en el sistema de objetos de Lucene
     * @throws IOException
     */
    private Document getDocumentToIndex(File file) throws IOException {
        Document document = new Document();
        TextField contentField = new TextField("contents", new FileReader(file));
        TextField fileNameField = new TextField("filename", file.getName(), TextField.Store.YES);
        TextField filePathField = new TextField("filepath", file.getCanonicalPath(), TextField.Store.YES);
        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);
        return document;
    }

    /**
     * Método para indexar un fichero dada una ruta
     * @param file Ruta al fichero de la colección documental a indexar
     * @throws IOException
     */
    private void indexFile(File file) throws IOException {
        System.out.println("Indexando " + file.getCanonicalPath());
        try {
            Document document = getDocumentToIndex(file);
            writer.addDocument(document);
        } catch (IOException e) {
            String errorWhere = "Error durante la indexación " + file.getCanonicalPath() + "\n";
            System.out.println(errorWhere + e.getMessage());
        }
    }

    /**
     * Método para indexar los documentos de una colección documental (especificada en la configuración).
     * @param filter Objeto que especifica un filtro para tener en cuenta ciertos tipos de documentos (txt en nuestro caso)
     * @return Número de documentos indexados correctamente
     * @throws IOException
     */
    public Long doIndexing(FileFilter filter) throws IOException {
        File[] files = new File(Configuration.DOCUMENTS_DIR).listFiles();
        assert files != null;
        for (File file : files) {
            if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file))
                indexFile(file);
            else
                System.out.println("No se ha indexado " + file.getCanonicalPath());
        }
        return writer.getMaxCompletedSequenceNumber();
    }

    /**
     * Método para cerrar el indexador
     * @throws IOException
     */
    public void close() throws IOException {
        writer.close();
    }

}

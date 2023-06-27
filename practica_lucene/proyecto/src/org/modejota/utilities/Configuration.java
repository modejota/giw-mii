package org.modejota.utilities;

// Las rutas relativas están pensadas para importar a IntelliJ la carpeta de la entrega completa, la cual
// tendrá "documentacion" y "proyecto" como subcarpetas. En caso contrario, se deberán modificar las rutas manualmente en
// este archivo, o limitarse a usar la interfaz de texto.

/**
 * @author José Alberto Gómez García
 * @since 20-Feb-2023
 */
public class Configuration {
    public static String INDEX_DIR = "./proyecto/index";
    public static String DOCUMENTS_DIR = "./proyecto/collection";
    public static String LANGUAGE = "EN";   // EN or ES
    public static String STOPWORDS_FILE = "./proyecto/data/stopwords_en.txt";   // ./proyecto/data/stopwords_es.txt
    public static int MAX_SEARCH_RESULTS = 15;
    public static String SEARCH_BY = "filename";    // filename or filepath
    public static Boolean USE_THRESHOLD = false;
    public static Float THRESHOLD = 2.0f;  // [0; 100]
    // Cuanto más alto, más relevante debe ser el documento (para los términos dados) para aparecer en los resultados.
    // En función del término los valores pueden no ser muy altos.

}

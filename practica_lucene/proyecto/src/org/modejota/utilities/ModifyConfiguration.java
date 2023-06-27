package org.modejota.utilities;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * @author José Alberto Gómez García
 * @since 27-Feb-2023
 */
public class ModifyConfiguration {

    private final Scanner scanner = new Scanner(System.in);
    private final List<String> options = Arrays.asList("n", "N", "s", "S");

    /**
     * Método para configurar los parámetros que usará el buscador.
     * Puede mantenerse los utilizados por defecto en fichero de configuración o especificar otros.
     */
    public void configurarBuscador() {

        System.out.println("¿Desea utilizar la configuración por defecto? (S/N)");
        String respuesta = scanner.nextLine();

        while (!options.contains(respuesta)) {
            System.out.println("Respuesta no válida. Por favor, introduzca S o N");
            respuesta = scanner.nextLine();
        }

        if (respuesta.equals("n") || respuesta.equals(("N"))) {
            System.out.println("Configuración de los parámetros de la búsqueda: \n");
            configurarDirectorios("obtener_indice");
            configurarDirectorios("documentos");
            configurarIdioma();
            configurarFicheroStopWords();
            configurarNumResultadosBusqueda();
            configurarFormatoSalidaBusqueda();
            configurarFiltroSimilitud();
        }
    }

    /**
     * Método para configurar los parámetros que usará el indexador.
     * Puede mantenerse los utilizados por defecto en fichero de configuración o especificar otros.
     */
    public void configurarIndexador() {
        System.out.println("¿Desea utilizar la configuración por defecto? (S/N)");
        String respuesta = scanner.nextLine();

        while (!options.contains(respuesta)) {
            System.out.println("Respuesta no válida. Por favor, introduzca S o N");
            respuesta = scanner.nextLine();
        }

        if (respuesta.equals("n") || respuesta.equals(("N"))) {
            System.out.println("Configuración de los parámetros del indexador: \n");
            configurarDirectorios("crear_indice");
            configurarDirectorios("documentos");
            configurarIdioma();
            configurarFicheroStopWords();
        }
    }

    /**
     * Método para configurar las rutas a los directorios de las que dependerán indexador y buscador
     * @param mode String que indique si se pide directorio del que obtener documentos,
     *             el directorio donde crear el índice o el directorio de donde leer el índice (debe existir)
     */
    private void configurarDirectorios(String mode) {
        switch (mode) {
            case "documentos" -> System.out.println("Introduzca el directorio con los documentos: ");
            case "crear_indice" -> System.out.println("Introduzca el directorio donde crear el índice: ");
            case "obtener_indice" -> System.out.println("Introduzca el directorio del índice: ");
        }

        File directorio = new File(scanner.nextLine());
        switch (mode) {
            case "documentos" -> {
                while (!directorio.exists() || !directorio.isDirectory()) {
                    System.out.println("El directorio introducido no existe o no es un directorio. Introduzca un directorio válido:");
                    directorio = new File(scanner.nextLine());
                }
                Configuration.DOCUMENTS_DIR = Paths.get(directorio.getAbsolutePath()).toString();
            }
            case "obtener_indice" -> {
                while (!directorio.exists() || !directorio.isDirectory()) {
                    System.out.println("El directorio introducido no existe o no es un directorio. Introduzca un directorio válido:");
                    directorio = new File(scanner.nextLine());
                }
            }
            case "crear_indice" -> {
                directorio = new File(directorio.getAbsolutePath());
                if (!directorio.exists()) {
                    boolean res = directorio.mkdir();
                    if (!res) {
                        System.out.println("No se ha podido crear el directorio del índice. Introduzca otro directorio:");
                        directorio = new File(scanner.nextLine());
                    }
                }
                Configuration.INDEX_DIR = Paths.get(directorio.getAbsolutePath()).toString();
            }
        }
    }

    /**
     * Método para configurar el idioma. Por el momento se admite EN o ES.
     * Para especificar otros idiomas, usar fichero de configuración.
     */
    private void configurarIdioma() {
        System.out.println("Introduzca el idioma de los documentos (EN o ES):");
        String idioma = scanner.nextLine();
        while (!idioma.equals("EN") && !idioma.equals("ES")) {
            System.out.println("El idioma introducido no es válido. Introduzca un idioma válido (EN o ES):");
            idioma = scanner.nextLine();
        }
        Configuration.LANGUAGE = idioma;
    }

    /**
     * Método para configurar la ruta en la que se encuentra el fichero del que obtener las palabras
     * vacías a tener en cuenta por buscador e indexador.
     */
    private void configurarFicheroStopWords() {
        // Podría comprobarse que acaba en en_txt o en es_txt, y que casa con el idioma, pero no se hará para dar flexibilidad
        System.out.println("Introduzca el fichero de stopwords (txt):");
        File ficheroStopwords = new File(scanner.nextLine());
        while (!ficheroStopwords.exists() || !ficheroStopwords.isFile() || !ficheroStopwords.getAbsolutePath().endsWith(".txt")) {
            System.out.println("El fichero introducido no existe o no es un fichero. Introduzca un fichero válido (txt):");
            ficheroStopwords = new File(scanner.nextLine());
        }
        Configuration.STOPWORDS_FILE = Paths.get(ficheroStopwords.getAbsolutePath()).toString();
    }

    /**
     * Método para configurar el número máximo de resultados que se muestran tras realizar una búsqueda.
     */
    private void configurarNumResultadosBusqueda() {
        System.out.println("Introduzca el número máximo de resultados a mostrar:");
        String numResultados = scanner.nextLine();
        while (!numResultados.matches("[0-9]+")) {
            System.out.println("El número introducido no es válido. Introduzca un número válido:");
            numResultados = scanner.nextLine();
        }
        Configuration.MAX_SEARCH_RESULTS = Integer.parseInt(numResultados);
    }

    /**
     * Método para configurar el formato en que se muestran los resultados de la búsqueda.
     * Se puede elegir entre nombre del fichero o ruta completa del fichero en el sistema.
     */
    private void configurarFormatoSalidaBusqueda() {
        System.out.println("¿Desea buscar por nombre de fichero o por ruta de fichero? (filename/filepath)");
        String respuesta = scanner.nextLine();
        while (!respuesta.equals("filename") && !respuesta.equals("filepath")) {
            System.out.println("La respuesta introducida no es válida. Introduzca una respuesta válida (filename/filepath):");
            respuesta = scanner.nextLine();
        }
        Configuration.SEARCH_BY = respuesta;
    }

    /**
     * Método para configurar si se desea utilizar el filtrado de resultados en la búsqueda dado un nivel mínimo de similitud.
     */
    private void configurarFiltroSimilitud() {
        System.out.println("¿Desea utilizar un umbral de similitud? (S/N)");
        String respuesta = scanner.nextLine();
        if (respuesta.equals("s") || respuesta.equals(("S"))) {
            System.out.println("Introduzca el umbral de similitud [0-100]:");
            String umbralSimilitud = scanner.nextLine();
            while (!umbralSimilitud.matches("^\\d*\\.\\d+|\\d+\\.\\d*$") || Float.parseFloat(umbralSimilitud) < 0 || Float.parseFloat(umbralSimilitud) > 100) {
                System.out.println("El umbral introducido no es válido. Introduzca un umbral válido [0-100]:");
                umbralSimilitud = scanner.nextLine();
            }
            Configuration.USE_THRESHOLD = true;
            Configuration.THRESHOLD = Float.valueOf(umbralSimilitud);
        } else {
            Configuration.USE_THRESHOLD = false;
        }
    }

}

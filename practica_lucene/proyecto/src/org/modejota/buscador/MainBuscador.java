package org.modejota.buscador;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.modejota.utilities.Configuration;
import org.modejota.utilities.ModifyConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * @author José Alberto Gómez García
 * @since 27-Feb-2023
 */
public class MainBuscador {

    private static final Buscador buscador;

    static {
        try {
            buscador = new Buscador();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método interactivo para realizar búsquedas sobre la colección documental dado un índice y otros parámetros.
     * @param args
     */
    public static void main(String[] args) {

        ModifyConfiguration configuracion = new ModifyConfiguration();
        configuracion.configurarBuscador();

        boolean salir = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduzca una consulta para realizar la búsqueda: ");
        String input = scanner.nextLine();

        try {
            search(input);
        } catch (IOException e) {
            System.out.println("Error en la búsqueda. \n" + e.getMessage());
        }

        while (!salir) {
            System.out.println("\n¿Desea realizar otra búsqueda? (S/N)");
            String respuesta = scanner.nextLine();
            if (respuesta.equals("S") || respuesta.equals("s")) {
                System.out.println("Introduce el término a buscar: ");
                input = scanner.nextLine();
                try {
                    search(input);
                } catch (IOException e) {
                    System.out.println("Error en la búsqueda. \n" + e.getMessage());
                }
            } else if (respuesta.equals("N") || respuesta.equals("n")) {
                salir = true;
                System.out.println("Gracias por usar el buscador. \n");
            } else {
                System.out.println("Respuesta no válida");
            }
        }
        scanner.close();
    }

    /**
     * Método para realizar una búsqueda, llamará al método de busqueda del bsucador
     * @param searchQuery Término/consulta a realizar
     * @throws IOException
     */
    public static void search(String searchQuery) throws IOException {
        long startTime = System.currentTimeMillis();
        TopDocs hits = null;
        try {
            hits = buscador.search(searchQuery);
        } catch (IOException | ParseException e) {
            System.out.println("Error en la búsqueda. \n" + e.getMessage());
        }
        long endTime = System.currentTimeMillis();
        System.out.println("\nTiempo de búsqueda: " + (endTime - startTime) + " milisegundos");

        assert hits != null;

        ArrayList<ScoreDoc> scoreDocs = new ArrayList<>(Arrays.stream(hits.scoreDocs).toList());
        if (Configuration.USE_THRESHOLD)    // Aplicar filtrado en función del umbral de ser necesario
            scoreDocs.removeIf(scoreDoc -> scoreDoc.score < Configuration.THRESHOLD);
        System.out.println("Número de resultados: " + scoreDocs.size() + "\n");

        for (ScoreDoc scoreDoc : scoreDocs) {   // Mostrar el nombre del fichero o la ruta completa es personalizable.
            System.out.println("Documento: " + buscador.getDocument(scoreDoc).get(Configuration.SEARCH_BY) + "  --> Score: " + scoreDoc.score);
        }
    }

}

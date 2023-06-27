package org.modejota.indexador;

import org.modejota.utilities.ModifyConfiguration;
import org.modejota.utilities.TextFileFilter;

import java.io.IOException;

/**
 * @author José Alberto Gómez García
 * @since 20-Feb-2023
 */
public class MainIndexador {

    /**
     * Método interactivo para realizar el indexado de una colección documental.
     * Genera una estructura de directorios propia de Apache Lucene.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        ModifyConfiguration configuracion = new ModifyConfiguration();
        configuracion.configurarIndexador();

        Indexador indexador = new Indexador();
        Long numIndexed = 0L;

        long startTime = System.currentTimeMillis();
        try {
            numIndexed = indexador.doIndexing(new TextFileFilter());
        } catch (IOException e) {
            System.out.println("Error en la creación del índice. \n" + e.getMessage());
        } finally {
            indexador.close();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Tiempo de indexación de " + numIndexed + " documentos: " + (endTime - startTime) + " milisegundos");
    }
}
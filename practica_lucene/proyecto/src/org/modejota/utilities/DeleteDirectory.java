package org.modejota.utilities;

import java.io.File;

/**
 * @author José Alberto Gómez García
 * @since 27-Feb-2023
 */
public class DeleteDirectory {

    /**
     * Método para eliminar un directorio y sus contenidos de forma recursiva
     * @param directorio File que represente el directorio a tratar
     */
    public static void borrarDirectorio(File directorio) {
        File[] archivos = directorio.listFiles();

        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    borrarDirectorio(archivo);
                } else {
                    archivo.delete();
                }
            }
        }
        directorio.delete();
    }
}

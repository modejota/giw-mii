package org.modejota.utilities;

import java.io.FileFilter;

/**
 * @author José Alberto Gómez García
 * @since 20-Feb-2023
 */
public class TextFileFilter implements FileFilter {
    @Override
    public boolean accept(java.io.File pathname) {
        return pathname.getName().toLowerCase().endsWith(".txt");
    }
}

package org.example.util;

import java.nio.file.Path;

public final class FileTypeUtils {

    private FileTypeUtils() {
    }

    public static boolean isTxtFile(Path path) {
        if (path == null || path.getFileName() == null) {
            return false;
        }
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".txt");
    }
}

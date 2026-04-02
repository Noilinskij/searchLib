package org.example.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexStorageImplTest {

    @TempDir
    Path tempDir;

    @Test
    void addWordAndGetFilesByWordReturnsExpectedFiles() throws Exception {
        IndexStorageImpl storage = new IndexStorageImpl();
        Path file1 = Files.createFile(tempDir.resolve("a.txt"));
        Path file2 = Files.createFile(tempDir.resolve("b.txt"));

        storage.addWord("java", file1);
        storage.addWord("java", file2);

        Set<Path> files = storage.getFilesByWord("java");

        assertEquals(2, files.size());
        assertTrue(files.contains(file1));
        assertTrue(files.contains(file2));
    }

    @Test
    void removeFileRemovesFileFromAllWords() throws Exception {
        IndexStorageImpl storage = new IndexStorageImpl();
        Path file = Files.createFile(tempDir.resolve("a.txt"));

        storage.addWord("java", file);
        storage.addWord("kotlin", file);

        storage.removeFile(file);

        assertTrue(storage.getFilesByWord("java").isEmpty());
        assertTrue(storage.getFilesByWord("kotlin").isEmpty());
    }

    @Test
    void getFilesByWordReturnsUnmodifiableSet() throws Exception {
        IndexStorageImpl storage = new IndexStorageImpl();
        Path file = Files.createFile(tempDir.resolve("a.txt"));
        storage.addWord("java", file);

        Set<Path> files = storage.getFilesByWord("java");

        assertThrows(UnsupportedOperationException.class, () -> files.add(file));
    }
}

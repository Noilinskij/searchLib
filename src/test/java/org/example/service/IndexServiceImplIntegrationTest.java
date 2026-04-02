package org.example.service;

import org.example.storage.IndexStorageImpl;
import org.example.token.SimpleWhiteSpaceTokenizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexServiceImplIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void addFileToIndexIndexesWordsFromSingleFile() throws Exception {
        IndexServiceImpl service = new IndexServiceImpl(new SimpleWhiteSpaceTokenizer(), new IndexStorageImpl());
        Path file = Files.createFile(tempDir.resolve("single.txt")).toAbsolutePath().normalize();
        Files.writeString(file, "Hello Java");

        service.addFileToIndex(file);

        assertTrue(service.getFilesByWord("hello").contains(file));
        assertTrue(service.getFilesByWord("java").contains(file));
    }

    @Test
    void addFileToIndexIndexesAllFilesFromDirectory() throws Exception {
        IndexServiceImpl service = new IndexServiceImpl(new SimpleWhiteSpaceTokenizer(), new IndexStorageImpl());
        Path root = Files.createDirectory(tempDir.resolve("docs")).toAbsolutePath().normalize();
        Path file1 = Files.createFile(root.resolve("a.txt")).toAbsolutePath().normalize();
        Path file2 = Files.createFile(root.resolve("b.txt")).toAbsolutePath().normalize();

        Files.writeString(file1, "cat");
        Files.writeString(file2, "cat");

        service.addFileToIndex(root);

        Set<Path> files = service.getFilesByWord("cat");
        assertTrue(files.contains(file1));
        assertTrue(files.contains(file2));
    }

    @Test
    void removeFileFromIndexRemovesFileFromSearchResults() throws Exception {
        IndexServiceImpl service = new IndexServiceImpl(new SimpleWhiteSpaceTokenizer(), new IndexStorageImpl());
        Path file = Files.createFile(tempDir.resolve("single.txt")).toAbsolutePath().normalize();
        Files.writeString(file, "hello");

        service.addFileToIndex(file);
        service.removeFileFromIndex(file);

        assertTrue(service.getFilesByWord("hello").isEmpty());
    }

    @Test
    void getFilesByWordNormalizesCaseAndSpaces() throws Exception {
        IndexServiceImpl service = new IndexServiceImpl(new SimpleWhiteSpaceTokenizer(), new IndexStorageImpl());
        Path file = Files.createFile(tempDir.resolve("single.txt")).toAbsolutePath().normalize();
        Files.writeString(file, "Java");

        service.addFileToIndex(file);

        assertTrue(service.getFilesByWord("   JAVA  ").contains(file));
    }

    @Test
    void watcherUpdateIndex() throws Exception {
        IndexServiceImpl service = new IndexServiceImpl(new SimpleWhiteSpaceTokenizer(), new IndexStorageImpl());
        Path file = Files.createFile(tempDir.resolve("single.txt")).toAbsolutePath().normalize();

        Files.writeString(file, "apple");
        service.entryCreate(file);
        assertTrue(service.getFilesByWord("apple").contains(file));

        Files.writeString(file, "banana");
        service.entryModify(file);
        assertTrue(service.getFilesByWord("apple").isEmpty());
        assertTrue(service.getFilesByWord("banana").contains(file));

        service.entryDelete(file);
        assertTrue(service.getFilesByWord("banana").isEmpty());
    }
}

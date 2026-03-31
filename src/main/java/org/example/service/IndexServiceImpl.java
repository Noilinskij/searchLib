package org.example.service;

import org.example.storage.IndexStorage;
import org.example.token.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public class IndexServiceImpl implements IndexService {
    public final Tokenizer tokenizer;
    public final IndexStorage indexStorage;

    public IndexServiceImpl(Tokenizer tokenizer, IndexStorage indexStorage) {
        this.tokenizer = tokenizer;
        this.indexStorage = indexStorage;
    }

    @Override
    public void addFileToIndex(Path path) {
        if (path == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .forEach(filePath -> {

                        indexStorage.removeFile(filePath);

                            try (Stream<String> lines = Files.lines(filePath)) {
                                lines.forEach(line -> {
                                    tokenizer.tokenize(line)
                                            .forEach(word -> indexStorage.addWord(word, filePath));
                                });
                            } catch (IOException e) {
                            System.err.println("Ошибка при чтении файла " + filePath + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Ошибка доступа к пути " + path + ": " + e.getMessage());
        }
    }

    @Override
    public void removeFileFromIndex(Path filePath) {
        if (filePath == null) {
            return;
        }
        indexStorage.removeFile(filePath);

    }

    @Override
    public Set<Path> getFilesByWord(String word) {
        if (word == null || word.isBlank()) {
            return Collections.emptySet();
        }
        return indexStorage.getFilesByWord(word.trim().toLowerCase());
    }
}

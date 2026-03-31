package org.example.storage;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IndexStorageImpl implements IndexStorage {

    private final Map<String, Set<Path>> map = new ConcurrentHashMap<>();

    @Override
    public void addWord(String word, Path filePath) {
        if (word == null || filePath == null || word.isBlank()) {
            return;
        }
        map.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet())
                .add(filePath);
    }

    @Override
    public void removeFile(Path filePath) {
        if (filePath == null) {
            return;
        }

        map.values().forEach(set -> set.remove(filePath));
    }

    @Override
    public Set<Path> getFilesByWord(String word) {
        if (word == null || word.isBlank()) {
            return Collections.emptySet();
        }

        Set<Path> set = map.get(word);

        if (set == null) {
            return Collections.emptySet();
        }
        else {
            return Collections.unmodifiableSet(set);
        }
    }

    @Override
    public void cleanup() {
        map.entrySet()
                .removeIf(entry -> entry
                        .getValue()
                        .isEmpty());
    }
}

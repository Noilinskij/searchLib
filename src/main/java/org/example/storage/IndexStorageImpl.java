package org.example.storage;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IndexStorageImpl implements IndexStorage {

    private final Map<String, Set<Path>> map = new ConcurrentHashMap<>();

    @Override
    public void addWord(String word, Path filePath) {
        if (map.containsKey(word)) {
            map.get(word).add(filePath);
        }
        else {
            Set<Path> set = new HashSet<>();
            set.add(filePath);
            map.put(word, set);
        }
    }

    @Override
    public void removeFile(Path filePath) {
        map.values().forEach(set -> set.remove(filePath));
    }

    @Override
    public Set<Path> getFilesByWord(String word) {
        return map.getOrDefault(word, Collections.emptySet());
    }
}

package org.example.storage;

import java.nio.file.Path;
import java.util.Set;

public interface IndexStorage {

    // добавили файл и для каждого слова добавляем файл
    public void addWord(String word, Path filePath);

    // для каждого слова в map удаляем файл
    public void removeFile(Path filePath);

    // получить все файлы для слова
    public Set<Path> getFilesByWord(String word);

    // очистка пустых entries
    public void cleanup();
}

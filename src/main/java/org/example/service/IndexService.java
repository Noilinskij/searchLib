package org.example.service;

import java.nio.file.Path;
import java.util.Set;

public interface IndexService {

    public void addFileToIndex(Path filePath);

    public void removeFileFromIndex(Path filePath);

    public Set<Path> getFilesByWord(String word);
}

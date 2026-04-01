package org.example.watcher;

import java.nio.file.Path;

public interface FileWatcherListener {
    public void entryCreate(Path path);
    public void entryModify(Path path);
    public void entryDelete(Path path);
}

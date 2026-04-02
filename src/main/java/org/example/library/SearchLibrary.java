package org.example.library;

import org.example.service.IndexServiceImpl;
import org.example.storage.IndexStorageImpl;
import org.example.token.SimpleWhiteSpaceTokenizer;
import org.example.token.Tokenizer;
import org.example.util.FileTypeUtils;
import org.example.watcher.FileWatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class SearchLibrary implements AutoCloseable {

    private final IndexServiceImpl indexService;
    private final FileWatcher fileWatcher;
    private Thread watcherThread;

    public SearchLibrary() throws IOException {
        this(new SimpleWhiteSpaceTokenizer());
    }

    public SearchLibrary(Tokenizer tokenizer) throws IOException {
        this.indexService = new IndexServiceImpl(tokenizer, new IndexStorageImpl());
        this.fileWatcher = new FileWatcher(indexService);
        startWatching();
    }

    public boolean addPath(Path path) throws IOException {
        if (path == null) {
            return false;
        }

        Path absPath = path.toAbsolutePath().normalize();
        if (!Files.exists(absPath)) {
            return false;
        }

        if (Files.isRegularFile(absPath) && !FileTypeUtils.isTxtFile(absPath)) {
            return false;
        }

        fileWatcher.addPath(path);
        return true;
    }

    public boolean removePath(Path path) throws IOException {
        if (path == null) {
            return false;
        }

        Path absPath = path.toAbsolutePath().normalize();
        if (!Files.exists(absPath)) {
            return false;
        }

        if (Files.isRegularFile(absPath) && !FileTypeUtils.isTxtFile(absPath)) {
            return false;
        }

        fileWatcher.removePath(path);
        return true;
    }

    public Set<Path> search(String word) {
        return indexService.getFilesByWord(word);
    }

    private synchronized void startWatching() {
        if (watcherThread != null && watcherThread.isAlive()) {
            return;
        }

        watcherThread = new Thread(fileWatcher, "search-library-watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private synchronized void stopWatching() {
        if (watcherThread != null) {
            watcherThread.interrupt();
            watcherThread = null;
        }
    }

    @Override
    public void close() {
        stopWatching();
    }
}

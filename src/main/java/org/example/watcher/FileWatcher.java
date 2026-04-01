package org.example.watcher;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final WatchService watchService;
    private final FileWatcherListener fileWatcherListener;

    private final Map<Path, WatchKey> pathToKey = new ConcurrentHashMap<>();
    private final Map<WatchKey, Path> keyToPath = new ConcurrentHashMap<>();

    private final Set<Path> supervisedPaths = ConcurrentHashMap.newKeySet();

    public FileWatcher(FileWatcherListener fileWatcherListener) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.fileWatcherListener = fileWatcherListener;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                Path dir = keyToPath.get(key);

                if (dir == null) {
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    Path name = (Path) event.context();
                    Path child = dir.resolve(name).toAbsolutePath();

                    if (isAllowed(child) || isAllowed(dir)) {
                        try {
                            handleEvent(kind, child);
                        } catch (IOException e) {
                            System.err.println("Ошибка при обработке пути " + child + ": " + e.getMessage());
                        }

                    }

                }

                boolean valid = key.reset();
                if (!valid) {
                    Path removedPath = keyToPath.remove(key);
                    if (removedPath != null) {
                        pathToKey.remove(removedPath);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleEvent(WatchEvent.Kind<?> kind, Path child) throws IOException {
        if (kind == ENTRY_CREATE) {
            addPath(child);
        } else if (kind == ENTRY_MODIFY){
            fileWatcherListener.entryModify(child);
        } else if (kind == ENTRY_DELETE) {
            fileWatcherListener.entryDelete(child);
        }
    }

    private boolean isAllowed(Path path) {
        return supervisedPaths.contains(path) || pathToKey.containsKey(path);
    }

    public void addPath(Path path) throws IOException {
        if (path == null) {
            return;
        }
        Path absPath = path.toAbsolutePath().normalize();
        if (!Files.exists(absPath)) {
            return;
        }

        if (Files.isRegularFile(absPath)) {
            registerDirectory(absPath.getParent());
            supervisedPaths.add(absPath);
            fileWatcherListener.entryCreate(absPath);
            return;
        }

        try (Stream<Path> stream = Files.walk(absPath)) {
            stream.forEach(p -> {
                try {
                    if (Files.isDirectory(p)) {
                        registerDirectory(p);
                    } else if (Files.isRegularFile(p)) {
                        fileWatcherListener.entryCreate(p);
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при обработке пути " + p + ": " + e.getMessage());
                }
            });
        }
    }

    private synchronized void registerDirectory(Path dir) throws IOException {
        if (!pathToKey.containsKey(dir)) {
            WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            pathToKey.put(dir, key);
            keyToPath.put(key, dir);
        }
    }

    public void removePath(Path path) throws IOException {
        if (path == null) {
            return;
        }

        Path absPath = path.toAbsolutePath().normalize();

        if (supervisedPaths.remove(absPath)) {
            boolean needed = supervisedPaths.stream()
                    .anyMatch(p -> p.startsWith(absPath.getParent()));

            if (!needed) {
                unregisterDirectory(absPath.getParent());
            }

            fileWatcherListener.entryDelete(absPath);
            return;
        }

        if (pathToKey.containsKey(absPath)) {
            supervisedPaths.removeIf(p -> p.startsWith(absPath));

            pathToKey.keySet().stream()
                    .filter(p -> p.startsWith(absPath))
                    .toList()
                    .forEach(this::unregisterDirectory);
            return;
        }

        if (absPath.getParent() != null && pathToKey.containsKey(absPath.getParent())) {
            fileWatcherListener.entryDelete(absPath);
        }
    }

    private synchronized void unregisterDirectory(Path dir) {
        WatchKey key = pathToKey.remove(dir);
        if (key != null) {
            key.cancel();
            keyToPath.remove(key);
            pathToKey.remove(dir);
        }
    }
}

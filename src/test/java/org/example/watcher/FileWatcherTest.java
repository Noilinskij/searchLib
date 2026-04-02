package org.example.watcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileWatcherTest {

    @TempDir
    Path tempDir;

    @Test
    void addPathForFileRegistersParentAndSendsCreateEvent() throws Exception {
        RecordingListener listener = new RecordingListener();
        FileWatcher watcher = new FileWatcher(listener);

        Path file = Files.createFile(tempDir.resolve("single.txt")).toAbsolutePath().normalize();

        watcher.addPath(file);

        assertEquals(1, listener.created().size());
        assertTrue(listener.created().contains(file));
    }

    @Test
    void addPathForDirectoryIndexesAllFilesAndRegistersNestedDirectories() throws Exception {
        RecordingListener listener = new RecordingListener();
        FileWatcher watcher = new FileWatcher(listener);

        Path root = Files.createDirectory(tempDir.resolve("root")).toAbsolutePath().normalize();
        Path nested = Files.createDirectory(root.resolve("nested")).toAbsolutePath().normalize();
        Path rootFile = Files.createFile(root.resolve("a.txt")).toAbsolutePath().normalize();
        Path nestedFile = Files.createFile(nested.resolve("b.txt")).toAbsolutePath().normalize();

        watcher.addPath(root);

        assertTrue(listener.created().contains(rootFile));
        assertTrue(listener.created().contains(nestedFile));
    }

    @Test
    void removeDirectorySendsDeleteEventsForAllNestedFiles() throws Exception {
        RecordingListener listener = new RecordingListener();
        FileWatcher watcher = new FileWatcher(listener);

        Path root = Files.createDirectory(tempDir.resolve("to-remove")).toAbsolutePath().normalize();
        Path nested = Files.createDirectory(root.resolve("nested")).toAbsolutePath().normalize();
        Path nested2 = Files.createDirectory(nested.resolve("deep")).toAbsolutePath().normalize();
        Path file1 = Files.createFile(root.resolve("a.txt")).toAbsolutePath().normalize();
        Path file2 = Files.createFile(nested.resolve("b.txt")).toAbsolutePath().normalize();
        Path file3 = Files.createFile(nested2.resolve("c.txt")).toAbsolutePath().normalize();

        watcher.addPath(root);
        listener.clear();

        watcher.removePath(root);

        assertTrue(listener.deleted().contains(file1));
        assertTrue(listener.deleted().contains(file2));
        assertTrue(listener.deleted().contains(file3));
    }

    @Test
    void removeFileSendsDeleteEventForThatFileOnly() throws Exception {
        RecordingListener listener = new RecordingListener();
        FileWatcher watcher = new FileWatcher(listener);

        Path root = Files.createDirectory(tempDir.resolve("root")).toAbsolutePath().normalize();
        Path file1 = Files.createFile(root.resolve("a.txt")).toAbsolutePath().normalize();
        Path file2 = Files.createFile(root.resolve("b.txt")).toAbsolutePath().normalize();

        watcher.addPath(root);
        listener.clear();

        watcher.removePath(file1);

        assertTrue(listener.deleted().contains(file1));
        assertEquals(1, listener.deleted().size());
        assertTrue(!listener.deleted().contains(file2));
    }

    @Test
    void modifyPathForFileSendsModifyEvent() throws Exception {
        RecordingListener listener = new RecordingListener();
        FileWatcher watcher = new FileWatcher(listener);

        Path file = Files.createFile(tempDir.resolve("modifiable.txt")).toAbsolutePath().normalize();
        watcher.modifyPath(file);

        assertEquals(1, listener.modified().size());
        assertTrue(listener.modified().contains(file));
    }

    @Test
    void internalCollectionsStayConsistentAfterAddAndRemoveDirectory() throws Exception {
        RecordingListener listener = new RecordingListener();
        FileWatcher watcher = new FileWatcher(listener);

        Path root = Files.createDirectory(tempDir.resolve("inspect")).toAbsolutePath().normalize();
        Path nested = Files.createDirectory(root.resolve("nested")).toAbsolutePath().normalize();
        Path file = Files.createFile(nested.resolve("a.txt")).toAbsolutePath().normalize();

        watcher.addPath(root);

        Set<Path> supervised = getSupervisedPaths(watcher);
        Map<Path, WatchKey> pathToKey = getPathToKey(watcher);
        Map<WatchKey, Path> keyToPath = getKeyToPath(watcher);

        assertTrue(supervised.contains(root));
        assertTrue(supervised.contains(nested));
        assertTrue(supervised.contains(file));

        assertTrue(pathToKey.containsKey(root));
        assertTrue(pathToKey.containsKey(nested));
        assertEquals(pathToKey.size(), keyToPath.size());

        for (Map.Entry<Path, WatchKey> entry : pathToKey.entrySet()) {
            assertEquals(entry.getKey(), keyToPath.get(entry.getValue()));
        }

        watcher.removePath(root);

        assertTrue(getSupervisedPaths(watcher).stream().noneMatch(p -> p.startsWith(root)));
        assertTrue(getPathToKey(watcher).keySet().stream().noneMatch(p -> p.startsWith(root)));
        assertTrue(getKeyToPath(watcher).values().stream().noneMatch(p -> p.startsWith(root)));
    }

    @SuppressWarnings("unchecked")
    private Set<Path> getSupervisedPaths(FileWatcher watcher) throws Exception {
        return (Set<Path>) getField(watcher, "supervisedPaths");
    }

    @SuppressWarnings("unchecked")
    private Map<Path, WatchKey> getPathToKey(FileWatcher watcher) throws Exception {
        return (Map<Path, WatchKey>) getField(watcher, "pathToKey");
    }

    @SuppressWarnings("unchecked")
    private Map<WatchKey, Path> getKeyToPath(FileWatcher watcher) throws Exception {
        return (Map<WatchKey, Path>) getField(watcher, "keyToPath");
    }

    private Object getField(FileWatcher watcher, String fieldName) throws Exception {
        Field field = FileWatcher.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(watcher);
    }

    private static final class RecordingListener implements FileWatcherListener {
        private final Set<Path> created = ConcurrentHashMap.newKeySet();
        private final Set<Path> modified = ConcurrentHashMap.newKeySet();
        private final Set<Path> deleted = ConcurrentHashMap.newKeySet();

        @Override
        public void entryCreate(Path path) {
            created.add(path);
        }

        @Override
        public void entryModify(Path path) {
            modified.add(path);
        }

        @Override
        public void entryDelete(Path path) {
            deleted.add(path);
        }

        Set<Path> created() {
            return Collections.unmodifiableSet(created);
        }

        Set<Path> modified() {
            return Collections.unmodifiableSet(modified);
        }

        Set<Path> deleted() {
            return Collections.unmodifiableSet(deleted);
        }

        void clear() {
            created.clear();
            modified.clear();
            deleted.clear();
        }
    }
}

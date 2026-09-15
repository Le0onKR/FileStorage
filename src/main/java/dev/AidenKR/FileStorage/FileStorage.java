package dev.AidenKR.FileStorage;

import dev.AidenKR.FileStorage.data.File;
import dev.AidenKR.FileStorage.data.Folder;
import dev.AidenKR.FileStorage.serializer.FileSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class FileStorage {

    private final Path root;
    private final FileSerializer serializer;

    public FileStorage(Path root) {
        this(root, null);
    }

    public FileStorage(Path root, FileSerializer serializer) {
        this.root = root.toAbsolutePath().normalize();
        this.serializer = serializer;
    }

    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    public static boolean isFile(Path path) {
        return Files.isRegularFile(path);
    }

    public static boolean isFolder(Path path) {
        return Files.isDirectory(path);
    }

    public static void delete(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    public static void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    public static void deleteDirectory(Path directory) throws IOException {
        if (!isFolder(directory)) {
            throw new IllegalArgumentException("Directory does not exist : " + directory);
        }

        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();

            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean classExists(String... classNames) {
        for (String className : classNames) {
            if (!classExists(className)) {
                return false;
            }
        }
        return true;
    }

    public static Path resolve(Path root, String name, FileType type) {
        Path resolved;

        if (type == FileType.DETECT) {
            resolved = root.resolve(name);
        } else {
            String extension = FileType.extension(type);
            resolved = name.endsWith(extension) ? root.resolve(name) : root.resolve(name + extension);
        }

        Path normalizedRoot = root.normalize();
        Path normalizedResolved = resolved.normalize();

        if (!normalizedResolved.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("Resolved path escapes the storage root: " + name);
        }
        return normalizedResolved;
    }

    public Path getRoot() {
        return root;
    }

    public void write(String name, FileType type, String data) throws IOException {
        Path path = resolve(root, name, type);
        createParent(path);
        Files.writeString(path, data, StandardCharsets.UTF_8);
    }

    public String read(String name, FileType type) throws IOException {
        return Files.readString(resolve(root, name, type), StandardCharsets.UTF_8);
    }

    public boolean exists(String name, FileType type) {
        return exists(resolve(root, name, type));
    }

    public void delete(String name, FileType type) throws IOException {
        delete(resolve(root, name, type));
    }

    public File getFile(String name, FileType type) {
        Path path = resolve(root, name, type);

        if (!isFile(path)) {
            return null;
        }
        return new File(path);
    }

    public List<File> getFiles() throws IOException {
        return getFiles(root);
    }

    public List<File> getFiles(String directory) throws IOException {
        return getFiles(resolveDirectory(directory));
    }

    public List<File> getFiles(Path directory) throws IOException {
        List<File> result = new ArrayList<>();

        if (!isFolder(directory)) {
            return result;
        }

        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile).map(File::new).forEach(result::add);
        }
        return result;
    }

    public List<Folder> getFolders() throws IOException {
        return getFolders(root);
    }

    public List<Folder> getFolders(String directory) throws IOException {
        return getFolders(resolveDirectory(directory));
    }

    public List<Folder> getFolders(Path directory) throws IOException {
        List<Folder> result = new ArrayList<>();

        if (!isFolder(directory)) {
            return result;
        }

        try (Stream<Path> stream = Files.walk(directory)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(directory))
                    .map(Folder::new)
                    .forEach(result::add);
        }
        return result;
    }

    private Path resolveDirectory(String directory) {
        Path resolved = root.resolve(directory).normalize();

        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException(
                    "Resolved path escapes the storage root: " + directory);
        }
        return resolved;
    }

    public void save(String name, FileType type, Object object) throws IOException {
        requireSerializer();
        String data = serializer.serialize(object);
        write(name, type, data);
    }

    public <T> T load(String name, FileType type, Class<T> objectType) throws IOException {
        requireSerializer();
        String data = read(name, type);
        return serializer.deserialize(data, objectType);
    }

    private void createParent(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            createDirectories(parent);
        }
    }

    private void requireSerializer() {
        if (serializer == null) {
            throw new IllegalStateException("No serializer has been configured");
        }
    }
}
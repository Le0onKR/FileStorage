package dev.AidenKR.FileStorage.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Folder {

    private final Path path;

    public Folder(Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    public Path getParent() {
        return path.getParent();
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path.toString();
    }

    public String getName() {
        return path.getFileName().toString();
    }

    public boolean exists() {
        return Files.exists(path);
    }

    public long size() throws IOException {
        return Files.size(path);
    }

    public boolean isDirectory() {
        return Files.isDirectory(path);
    }
}

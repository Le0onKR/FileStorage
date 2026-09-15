package dev.AidenKR.FileStorage.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class File {

    private final Path path;

    public File(Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    public Path getPath() {
        return path;
    }

    public Path getParent() {
        return path.getParent();
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

    public boolean isFile() {
        return Files.isRegularFile(path);
    }

    public long size() throws IOException {
        return Files.size(path);
    }
}

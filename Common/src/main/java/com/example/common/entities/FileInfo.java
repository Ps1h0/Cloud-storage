package com.example.common.entities;

import com.example.common.messages.AbstractMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * Класс с информацией о файле
 */
@Getter
public class FileInfo extends AbstractMessage {

    @AllArgsConstructor
    @Getter
    public enum FileType {
        FILE("File"),
        DIRECTORY("Dir");
        private final String name;
    }

    private final String filename;
    private final FileType type;
    private final long size;
    private byte[] fileContent;

    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.type = FileType.DIRECTORY;
                this.size = -1L;
            } else {
                this.type = FileType.FILE;
                this.size = Files.size(path);
                this.fileContent = Files.readAllBytes(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Некорректный файл");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileInfo)) return false;
        FileInfo fileInfo = (FileInfo) o;
        return size == fileInfo.size && Objects.equals(filename, fileInfo.filename) && type == fileInfo.type && Arrays.equals(fileContent, fileInfo.fileContent);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(filename, type, size);
        result = 31 * result + Arrays.hashCode(fileContent);
        return result;
    }
}

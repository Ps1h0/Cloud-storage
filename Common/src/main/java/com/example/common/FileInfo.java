package com.example.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

//Класс с информацией о файле
public class FileInfo extends AbstractMessage {
    //Перечисление типов файлов (файл или директория)
    public enum FileType{
        FILE("File"), DIRECTORY("Dir");

        private final String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String filename;
    private FileType type;
    private long size;
    private byte[] fileContent;


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public FileInfo() {
    }

    public FileInfo(Path path){
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            } else this.fileContent = Files.readAllBytes(path);
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

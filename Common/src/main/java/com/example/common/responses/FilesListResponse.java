package com.example.common.responses;

import com.example.common.messages.AbstractMessage;
import com.example.common.entities.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


/**
 * Ответ на запрос списка файлов. Хранит в себе список с информацией о файлах
 */
@AllArgsConstructor
@Getter
public class FilesListResponse extends AbstractMessage {
    private List<FileInfo> files;
}

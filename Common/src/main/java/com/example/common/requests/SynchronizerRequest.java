package com.example.common.requests;

import com.example.common.messages.AbstractMessage;
import com.example.common.entities.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Запрос серверу на синхронизацию
 */
@AllArgsConstructor
@Getter
public class SynchronizerRequest extends AbstractMessage {
    private List<FileInfo> files;
}

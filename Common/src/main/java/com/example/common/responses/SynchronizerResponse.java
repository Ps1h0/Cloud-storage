package com.example.common.responses;

import com.example.common.AbstractMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Ответ сервера на запрос синхронизации
 */
@AllArgsConstructor
@Getter
public class SynchronizerResponse extends AbstractMessage {
    private FilesListResponse files;
}

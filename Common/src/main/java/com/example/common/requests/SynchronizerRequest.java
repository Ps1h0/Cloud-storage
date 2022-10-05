package com.example.common.requests;

import com.example.common.AbstractMessage;
import com.example.common.FileInfo;
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

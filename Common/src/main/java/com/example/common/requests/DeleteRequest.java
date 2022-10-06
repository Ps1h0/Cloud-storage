package com.example.common.requests;

import com.example.common.messages.AbstractMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeleteRequest extends AbstractMessage {
    private String delPath;
}

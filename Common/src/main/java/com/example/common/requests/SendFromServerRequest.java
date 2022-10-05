package com.example.common.requests;

import com.example.common.AbstractMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendFromServerRequest extends AbstractMessage {
    private String path;
}

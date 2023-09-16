package com.glackfag.shortybot.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Report {
    private String alias;
    private long reporterId;
}

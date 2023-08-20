package com.glackfag.shortybot.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Association {
    private String destination;
    private String alias;
    private LocalDate lastUsage;
    private Integer usages;
    private Long creatorId;
}

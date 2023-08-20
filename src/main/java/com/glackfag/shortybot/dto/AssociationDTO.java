package com.glackfag.shortybot.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssociationDTO {
    private String destination;
    private String alias;
    private LocalDate lastUsage;
    private Integer usages;
    private Long creatorId;
}

package com.glackfag.shortybot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glackfag.shortybot.dto.AssociationDTO;
import com.glackfag.shortybot.models.Association;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Component
@Slf4j
public class RestClient {
    @Value("${shorty.api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RestClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<Association> createAuto(@NotNull Association association) {
        String url = apiUrl + "/create/auto";
        AssociationDTO dto = objectMapper.convertValue(association, AssociationDTO.class);

        ResponseEntity<AssociationDTO> response = restTemplate.postForEntity(url, dto, AssociationDTO.class);
        association = objectMapper.convertValue(response.getBody(), Association.class);

        return Optional.ofNullable(association);
    }

    public Association readByAlias(@NotNull @NotEmpty String alias) {
        String url = UriComponentsBuilder.fromUriString(apiUrl+ "read")
                .queryParam("data", Data.ENTITY)
                .queryParam("alias", alias)
                .toUriString();

        ResponseEntity<AssociationDTO> response = restTemplate.getForEntity(url, AssociationDTO.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.debug(String.format("Error: %d. Attempted to request association by alias: '%s'", response.getStatusCode().value(), alias));
        }

        return objectMapper.convertValue(response.getBody(), Association.class);
    }

    public List<Association> readAllAssociationsByCreatorId(long creatorId) {
        String url = UriComponentsBuilder.fromUriString(apiUrl+ "read")
                .queryParam("data", Data.ENTITIES)
                .queryParam("cid", creatorId)
                .toUriString();

        ResponseEntity<AssociationDTO[]> response = restTemplate.getForEntity(url, AssociationDTO[].class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.debug(String.format("Error: %d. Attempted to request association by creatorId: '%d'", response.getStatusCode().value(), creatorId));
        }

        return Arrays.stream(Objects.requireNonNull(response.getBody())).map(x -> objectMapper.convertValue(x, Association.class)).toList();
    }

    public List<String> readFieldsByCreatorId(long creatorId, @NotNull Field field) {
        String url = UriComponentsBuilder.fromUriString(apiUrl+ "read")
                .queryParam("data", Data.FIELD)
                .queryParam("field", field.toString())
                .toUriString();

        ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.debug(String.format("Error: %d. Attempted to request aliases by creatorId: '%d'", response.getStatusCode().value(), creatorId));
            return new ArrayList<>(0);
        }

        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(response.getBody())));
    }

    public enum Field {
        DESTINATION,
        ALIAS;
    }

    private interface Data {
        String FIELD = "field";
        String ENTITY = "entity";
        String ENTITIES = "entities";
    }
}

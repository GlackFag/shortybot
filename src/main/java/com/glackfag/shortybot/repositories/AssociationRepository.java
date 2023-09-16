package com.glackfag.shortybot.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glackfag.shortybot.dto.AssociationDTO;
import com.glackfag.shortybot.dto.ReportDTO;
import com.glackfag.shortybot.models.Association;
import com.glackfag.shortybot.models.Report;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Repository
@Slf4j
public class AssociationRepository {
    @Value("${shorty.api.url}")
    private String apiUrl;
    @Value("${shorty.auth}")
    private String authHeaderValue;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public AssociationRepository(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Association createAuto(@NotNull Association association) {
        String url = apiUrl + "/create/auto";
        AssociationDTO dto = objectMapper.convertValue(association, AssociationDTO.class);

        ResponseEntity<AssociationDTO> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(dto, authHeader()), AssociationDTO.class);

        if (response.getStatusCode().is2xxSuccessful())
            log.debug(String.format("Success: %d. Attempted to create association by alias: '%s' creatorId: '%d'",
                    response.getStatusCode().value(), association.getAlias(), association.getCreatorId()));
        else
            log.debug(String.format("Error: %d. Attempted to create association by alias: '%s' creatorId: '%d'",
                    response.getStatusCode().value(), association.getAlias(), association.getCreatorId()));

        return objectMapper.convertValue(response.getBody(), Association.class);
    }

    public Association readByAlias(@NotNull @NotEmpty String alias) {
        String url = UriComponentsBuilder.fromUriString(apiUrl + "/read")
                .queryParam("data", Data.ENTITY)
                .queryParam("alias", alias)
                .toUriString();

        ResponseEntity<AssociationDTO> response = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(authHeader()), AssociationDTO.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.debug(String.format("Error: %d. Attempted to request association by alias: '%s'", response.getStatusCode().value(), alias));
        }

        return objectMapper.convertValue(response.getBody(), Association.class);
    }

    public List<Association> readAllAssociationsByCreatorId(long creatorId) {
        String url = UriComponentsBuilder.fromUriString(apiUrl + "/read")
                .queryParam("data", Data.ENTITIES)
                .queryParam("cid", creatorId)
                .toUriString();

        ResponseEntity<AssociationDTO[]> response = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(authHeader()), AssociationDTO[].class);

        if (response.getStatusCode().is2xxSuccessful())
            log.debug(String.format("Success: %d. Attempted to request association by creatorId: '%d'", response.getStatusCode().value(), creatorId));
        else {
            log.debug(String.format("Error: %d. Attempted to request association by creatorId: '%d'", response.getStatusCode().value(), creatorId));
            return Collections.emptyList();
        }

        return Arrays.stream(Objects.requireNonNull(response.getBody())).map(x -> objectMapper.convertValue(x, Association.class)).toList();
    }

    public List<String> readFieldByCreatorId(long creatorId, @NotNull Field field) {
        String url = UriComponentsBuilder.fromUriString(apiUrl + "/read")
                .queryParam("data", Data.FIELD)
                .queryParam("field", field.toString())
                .toUriString();

        ResponseEntity<String[]> response = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(authHeader()), String[].class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.debug(String.format("Error: %d. Attempted to request aliases by creatorId: '%d'", response.getStatusCode().value(), creatorId));
            return Collections.emptyList();
        }

        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(response.getBody())));
    }

    public Pair<HttpStatusCode, Association> report(Report report) {
        String url = apiUrl + "/report";

        ReportDTO dto = objectMapper.convertValue(report, ReportDTO.class);

        ResponseEntity<AssociationDTO> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(dto, authHeader()), AssociationDTO.class);

        return responseEntity.getBody() == null ? Pair.of(responseEntity.getStatusCode(), null) :
                Pair.of(responseEntity.getStatusCode(), objectMapper.convertValue(responseEntity.getBody(), Association.class));
    }

    HttpHeaders authHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeaderValue);

        return headers;
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

package com.glackfag.shortybot.services;

import com.glackfag.shortybot.bot.Bot;
import com.glackfag.shortybot.models.Association;
import com.glackfag.shortybot.models.Report;
import com.glackfag.shortybot.repositories.AssociationRepository;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssociationService {
    private final AssociationRepository repository;
    private final Bot bot;

    @Autowired
    public AssociationService(AssociationRepository repository, @Lazy Bot bot) {
        this.repository = repository;
        this.bot = bot;
    }

    public Association createAuto(@NotNull Association association) {
        return repository.createAuto(association);
    }

    public Association readByAlias(@NotNull @NotEmpty String alias) {
        return repository.readByAlias(alias);
    }


    public List<Association> readAllAssociationsByCreatorId(long creatorId) {
        return repository.readAllAssociationsByCreatorId(creatorId);
    }

    public List<String> readFieldByCreatorId(long creatorId, @NotNull AssociationRepository.Field field) {
        return repository.readFieldByCreatorId(creatorId, field);
    }

    public boolean report(Report report){
        Pair<HttpStatusCode, Association> result = repository.report(report);

        if(result.getRight() != null)
            bot.notifyBannedShorteningCreator(result.getRight());

        return result.getLeft().is2xxSuccessful();
    }
}
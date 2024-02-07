package org.digit.program.validator;

import lombok.extern.slf4j.Slf4j;
import org.digit.program.models.program.Program;
import org.digit.program.models.program.ProgramSearch;
import org.digit.program.models.sanction.Sanction;
import org.digit.program.models.sanction.SanctionSearch;
import org.digit.program.repository.ProgramRepository;
import org.digit.program.repository.SanctionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SanctionValidator {

    private final SanctionRepository sanctionRepository;
    private final ProgramRepository programRepository;

    public SanctionValidator(SanctionRepository sanctionRepository, ProgramRepository programRepository) {
        this.sanctionRepository = sanctionRepository;
        this.programRepository = programRepository;
    }

    public void validateSanction(List<Sanction> sanctions, Boolean isCreate) {
        log.info("validating Sanction");
        List<Program> programs = programRepository.searchProgram(ProgramSearch.builder()
                .programCode(sanctions.get(0).getProgramCode()).build());
        if (CollectionUtils.isEmpty(programs)) {
            throw new IllegalArgumentException("No Programs exists for program code: " + sanctions.get(0).getProgramCode());
        }

        if (!isCreate) {
            Set<String> sanctionIds = sanctions.stream().map(Sanction::getId).collect(Collectors.toSet());
            List<Sanction> sanctionFromSearch = sanctionRepository.searchSanction(SanctionSearch.builder()
                    .ids(new ArrayList<>(sanctionIds)).build());
            if (sanctionFromSearch.size() != sanctionIds.size()) {
                sanctionIds.removeAll(sanctionFromSearch.stream().map(Sanction::getId).collect(Collectors.toSet()));
                throw new IllegalArgumentException("No sanction found for id(s): " + sanctionIds);
            }
        }
    }
}

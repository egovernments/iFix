package org.digit.program.utils;

import lombok.extern.slf4j.Slf4j;
import org.digit.program.constants.AllocationType;
import org.digit.program.constants.Status;
import org.digit.program.models.allocation.Allocation;
import org.digit.program.models.disburse.DisburseSearch;
import org.digit.program.models.disburse.Disbursement;
import org.digit.program.models.sanction.Sanction;
import org.digit.program.models.sanction.SanctionSearch;
import org.digit.program.repository.DisburseRepository;
import org.digit.program.repository.SanctionRepository;
import org.digit.program.service.EnrichmentService;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.digit.program.constants.Error.NO_SANCTION_AVAILABLE_FOR_AMOUNT;
import static org.digit.program.constants.Error.NO_SANCTION_AVAILABLE_FOR_AMOUNT_MSG;

@Component
@Slf4j
public class CalculationUtil {
    private final SanctionRepository sanctionRepository;
    private final DisburseRepository disburseRepository;
    private final EnrichmentService enrichmentService;

    public CalculationUtil(SanctionRepository sanctionRepository, DisburseRepository disburseRepository, EnrichmentService enrichmentService) {
        this.sanctionRepository = sanctionRepository;
        this.disburseRepository = disburseRepository;
        this.enrichmentService = enrichmentService;
    }

    /**
     * If status of disburse reply is failed then add the disbursement amount to sanction and return the sanction
     * @param disbursement
     * @param senderId
     * @return
     */
    public Sanction calculateAndReturnSanctionForOnDisburse(Disbursement disbursement, String senderId) {
        if (disbursement.getStatus().getStatusCode().equals(Status.FAILED)  ||
                disbursement.getStatus().getStatusCode().equals(Status.ERROR)) {
            log.info("Calculating Sanction for on disburse");
            List<Disbursement> disbursementsFromDB = disburseRepository.searchDisbursements(DisburseSearch.builder()
                    .targetId(disbursement.getTargetId()).build());
            List<Status> statuses = disbursementsFromDB.stream().map(disbursement1 -> disbursement1.getStatus()
                    .getStatusCode()).collect(Collectors.toList());
            if (statuses.contains(Status.PARTIAL)) {
                return null;
            }
            SanctionSearch sanctionSearch = SanctionSearch.builder().ids(Collections.singletonList(disbursement.getSanctionId())).build();
            Sanction sanction = sanctionRepository.searchSanction(sanctionSearch, false).get(0);
            sanction.setAvailableAmount(sanction.getAvailableAmount() + disbursement.getGrossAmount());
            enrichmentService.getAuditDetails(senderId, disbursement.getAuditDetails());
            log.info("Sanctioned amount increased for Failed or Error disbursement with id: {}", disbursement.getId());
            return sanction;
        } else {
            return null;
        }
    }

    /**
     * Searches the db for disbursement on target id and if any previous disbursement is partial then return null
     * If sanction id is present, decrease the available amount and return the sanction else find a sanction with
     * available amount more than disbursement amount and decrease the available amount and return the sanction.
     *
     * @param disbursement
     * @param senderId
     * @return
     */
    public Sanction calculateAndReturnSanctionForDisburse(Disbursement disbursement, String senderId) {
        Sanction sanction = null;
        //Search disburse and return null if targetId is already present in db
        List<Disbursement> disbursementsFromDB = disburseRepository.searchDisbursements(DisburseSearch.builder()
                .targetId(disbursement.getTargetId()).build());

        if (!disbursementsFromDB.isEmpty()) {
            Optional<Disbursement> optionalDisbursement = disbursementsFromDB.stream()
                    .filter(disbursementFromDB -> disbursementFromDB.getStatus().getStatusCode().equals(Status.PARTIAL))
                    .findFirst();
            if (optionalDisbursement.isPresent()) {
                Disbursement disbursementWithPartialStatus = optionalDisbursement.get();
                disbursement.setSanctionId(disbursementWithPartialStatus.getSanctionId());
                for (Disbursement childDisbursement : disbursement.getDisbursements())
                    childDisbursement.setSanctionId(disbursementWithPartialStatus.getSanctionId());
                return sanction;
            }
        }
        log.info("Calculating Sanction for disburse");
        if (disbursement.getSanctionId() != null) {
            SanctionSearch sanctionSearch = SanctionSearch.builder().ids(Collections.singletonList(disbursement.getSanctionId())).build();
            sanction = sanctionRepository.searchSanction(sanctionSearch, false).get(0);
        } else {
            SanctionSearch sanctionSearch = SanctionSearch.builder().locationCode(disbursement.getLocationCode())
                    .programCode(disbursement.getProgramCode()).build();
            List<Sanction> sanctions = sanctionRepository.searchSanction(sanctionSearch, false);

            for (Sanction sanctionFromDB : sanctions) {
                if (sanctionFromDB.getAvailableAmount().compareTo(disbursement.getGrossAmount()) >= 0) {
                    sanction = sanctionFromDB;
                    break;
                }
            }

            if (sanction == null) {
                throw new CustomException(NO_SANCTION_AVAILABLE_FOR_AMOUNT, NO_SANCTION_AVAILABLE_FOR_AMOUNT_MSG +
                        disbursement.getGrossAmount());
            }

            disbursement.setSanctionId(sanction.getId());
            for (Disbursement childDisbursement : disbursement.getDisbursements())
                childDisbursement.setSanctionId(sanction.getId());
        }

        sanction.setAvailableAmount(sanction.getAvailableAmount() - disbursement.getGrossAmount());

        sanction.setAuditDetails(enrichmentService.getAuditDetails(senderId, sanction.getAuditDetails()));
        log.info("Sanction calculated for disburse");
        return sanction;
    }

    /**
     * For list of allocations fetch the sanctions and calculate sanctioned or deducted amount and update the
     * sanctioned amount and available amount and return list of sanctions.
     * @param allocations
     * @return
     */
    public List<Sanction> calculateAndReturnSanctionForAllocation(List<Allocation> allocations) {
        log.info("Calculating Sanction for allocation");
        Set<String> sanctionIds = allocations.stream().map(Allocation::getSanctionId).collect(Collectors.toSet());
        Map<String, Sanction> sanctionIdVsSanction = sanctionRepository.searchSanction(SanctionSearch.builder()
                .ids(new ArrayList<>(sanctionIds)).build(), false).stream().collect(Collectors.toMap(Sanction::getId, sanction -> sanction));
        for (Allocation allocation : allocations) {
            if (allocation.getAllocationType().equals(AllocationType.ALLOCATION)) {
                sanctionIdVsSanction.get(allocation.getSanctionId()).setAllocatedAmount(sanctionIdVsSanction.get(allocation.getSanctionId()).getAllocatedAmount() + allocation.getGrossAmount());
                sanctionIdVsSanction.get(allocation.getSanctionId()).setAvailableAmount(sanctionIdVsSanction.get(allocation.getSanctionId()).getAvailableAmount() + allocation.getGrossAmount());
            } else {
                sanctionIdVsSanction.get(allocation.getSanctionId()).setAllocatedAmount(sanctionIdVsSanction.get(allocation.getSanctionId()).getAllocatedAmount() - allocation.getGrossAmount());
                sanctionIdVsSanction.get(allocation.getSanctionId()).setAvailableAmount(sanctionIdVsSanction.get(allocation.getSanctionId()).getAvailableAmount() - allocation.getGrossAmount());
            }
        }
        log.info("Sanction calculated for allocation");
        return new ArrayList<>(sanctionIdVsSanction.values());
    }
}

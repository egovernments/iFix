package org.digit.program.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.digit.program.configuration.ProgramConfiguration;
import org.digit.program.constants.Status;
import org.digit.program.models.RequestHeader;
import org.digit.program.models.allocation.Allocation;
import org.digit.program.models.disburse.Disbursement;
import org.digit.program.models.program.Program;
import org.digit.program.models.sanction.Sanction;
import org.digit.program.utils.CommonUtil;
import org.digit.program.utils.IdGenUtil;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrichmentService {

    private final IdGenUtil idGenUtil;
    private final ProgramConfiguration configs;
    private final CommonUtil commonUtil;

    public EnrichmentService(IdGenUtil idGenUtil, ProgramConfiguration configs, CommonUtil commonUtil) {
        this.idGenUtil = idGenUtil;
        this.configs = configs;
        this.commonUtil = commonUtil;
    }

    /**
     * Enriches data required for create program if not already present
     * @param header
     * @param program
     */
    public void enrichProgramForCreate(RequestHeader header, Program program) {
        log.info("Enriching Program for Create");
        if (commonUtil.isSameDomain(header.getReceiverId(), configs.getDomain())) {
            program.setProgramCode(idGenUtil.getIdList(RequestInfo.builder().build(), program.getLocationCode(),
                    configs.getIdName(), "", 1).get(0));
            program.setStatus(org.digit.program.models.Status.builder().statusCode(Status.APPROVED).build());
        } else {
            if (program.getId() == null || StringUtils.isEmpty(program.getId()))
                program.setId(UUID.randomUUID().toString());
            program.setClientHostUrl(commonUtil.extractHostUrlFromURL(configs.getDomain()));
            program.setStatus(org.digit.program.models.Status.builder().statusCode(Status.INITIATED).build());
        }
        program.setStatus(org.digit.program.models.Status.builder().statusCode(Status.ACTIVE).build());
        program.setAuditDetails(getAuditDetails(header.getSenderId(), null));
        log.debug("Enrichment for create completed for id: {}", program.getId());
    }

    /**
     * Enriches audit details for program update and reply
     * @param program
     * @param senderId
     */
    public void enrichProgramForUpdateOrOnProgram(Program program, String senderId) {
        log.info("Enrich Program for Update/OnProgram");
        program.setAuditDetails(getAuditDetails(senderId, program.getAuditDetails()));
        log.debug("Enrichment for update/on-program completed for id: {}", program.getId());
    }

    /**
     * Set audit details and id if not present for sanction create
     * @param sanctions
     * @param header
     */
    public void enrichSanctionCreate(List<Sanction> sanctions, RequestHeader header) {
        log.info("Enrich sanction create");
        if (!commonUtil.isSameDomain(header.getReceiverId(), configs.getDomain())) {
            for (Sanction sanction : sanctions) {
                if (sanction.getId() == null || StringUtils.isEmpty(sanction.getId()))
                    sanction.setId(UUID.randomUUID().toString());
                sanction.setAuditDetails(getAuditDetails(header.getSenderId(), null));
            }
        }
    }

    /**
     * Setting audit details for sanction update
     * @param sanctions
     * @param senderId
     */
    public void enrichSanctionUpdate(List<Sanction> sanctions, String senderId) {
        log.info("Enrich sanction update");
        for (Sanction sanction : sanctions) {
            sanction.setAuditDetails(getAuditDetails(senderId, sanction.getAuditDetails()));
        }
    }

    /**
     * Setting audit details and id if not present for allocation create
     * @param allocations
     * @param senderId
     */
    public void enrichAllocationCreate(List<Allocation> allocations, String senderId) {
        log.info("Enrich allocation create");
        for (Allocation allocation : allocations) {
            if (allocation.getId() == null || StringUtils.isEmpty(allocation.getId()))
                allocation.setId(UUID.randomUUID().toString());
            allocation.setAuditDetails(getAuditDetails(senderId, null));
        }
    }

    /**
     * Setting audit details for allocation update
     * @param allocations
     * @param senderId
     */
    public void enrichAllocationUpdate(List<Allocation> allocations, String senderId) {
        log.info("Enrich allocation update");
        for (Allocation allocation : allocations) {
            allocation.setAuditDetails(getAuditDetails(senderId, allocation.getAuditDetails()));
        }
    }

    /**
     * Setting id(if not present) and audit details for disbursement and child disbursement and status for create
     * @param disbursement
     * @param senderId
     */
    public void enrichDisburseCreate(Disbursement disbursement, String senderId) {
        log.info("Enrich disburse create");
        if (disbursement.getId() == null || StringUtils.isEmpty(disbursement.getId()))
            disbursement.setId(UUID.randomUUID().toString());
        AuditDetails auditDetails = getAuditDetails(senderId, null);
        disbursement.setAuditDetails(auditDetails);
        disbursement.setStatus(org.digit.program.models.Status.builder().statusCode(Status.INITIATED).build());
        for (Disbursement childDisbursement : disbursement.getDisbursements()) {
            if (childDisbursement.getId() == null || StringUtils.isEmpty(childDisbursement.getId()))
                childDisbursement.setId(UUID.randomUUID().toString());
            childDisbursement.setAuditDetails(auditDetails);
            childDisbursement.setStatus(org.digit.program.models.Status.builder().statusCode(Status.INITIATED).build());
        }
    }

    /**
     * Setting audit details for disbursement and child disbursement and status for update
     * @param disbursement
     * @param senderId
     * @param isReply
     */
    public void enrichDisburseUpdate(Disbursement disbursement, String senderId, Boolean isReply) {
        log.info("Enrich disburse update");
        AuditDetails auditDetails = getAuditDetails(senderId, disbursement.getAuditDetails());
        disbursement.setAuditDetails(auditDetails);
        for (Disbursement childDisbursement : disbursement.getDisbursements()) {
            childDisbursement.setAuditDetails(auditDetails);
        }
        if (isReply) {
            List<Status> statuses = disbursement.getDisbursements().stream()
                    .map(disbursement1 -> disbursement1.getStatus().getStatusCode()).distinct().collect(Collectors.toList());
            if (statuses.contains(Status.FAILED) && statuses.contains(Status.SUCCESSFUL)) {
                disbursement.getStatus().setStatusCode(Status.PARTIAL);
            }
        }
    }

    /**
     * Setting audit details
     * @param senderId
     * @param prevAuditDetails
     * @return
     */
    public AuditDetails getAuditDetails(String senderId, AuditDetails prevAuditDetails) {
        Long time = System.currentTimeMillis();
        if (prevAuditDetails == null) {
            return AuditDetails.builder()
                    .createdBy(senderId)
                    .createdTime(time)
                    .lastModifiedBy(senderId)
                    .lastModifiedTime(time).build();
        } else {
            prevAuditDetails.setLastModifiedTime(time);
            prevAuditDetails.setLastModifiedBy(senderId);
            return prevAuditDetails;
        }

    }

}

package org.egov.ifixespipeline.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestHeader;
import org.egov.ifixespipeline.models.*;
import org.egov.ifixespipeline.producer.Producer;
import org.egov.ifixespipeline.repository.ServiceRequestRepository;
import org.egov.ifixespipeline.service.FiscalDataEnrichmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class IfixElasticSearchPipelineListener {

    @Autowired
    private FiscalDataEnrichmentService fiscalDataEnrichmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private Producer producer;

    @Value("${fiscal.event.index.topic}")
    private String indexFiscalEventsTopic;

    @Value("${fiscal.event.kafka.push.topic}")
    private String fiscalEventsNewRecordsTopic;

    @Value("${ifix.master.data.service.host}")
    private String ifixMasterDataServiceHost;

    @Value("${ifix.coa.search.endpoint}")
    private String ifixCoaSearchEndpoint;

    @Value("${coa.electricity.head.name}")
    private String electricityCoaHeadName;

    @Value("${coa.operations.head.name}")
    private String operationsCoaHeadName;

    @Value("${coa.salary.head.name}")
    private String salaryCoaHeadName;

    private Map<String, Map<String, HashSet<String>>> tenantIdVsExpenditureTypeVsUuidsMap = new HashMap<>();

    /**
     * Kafka consumer
     *
     * @param record
     * @param topic
     */
    @KafkaListener(topics = { "${fiscal.event.kafka.push.topic}" })
    public void listen(HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            FiscalEventBulkRequest fiscalEventBulkRequest = objectMapper.convertValue(record, FiscalEventBulkRequest.class);

            for(FiscalEvent fiscalEvent : fiscalEventBulkRequest.getFiscalEvent()) {
                log.info("Fiscal Event request got "+fiscalEvent);
                log.info("tenant id:"+fiscalEvent.getTenantId());
                // Enrich hierarchy map according to the tenantid encountered by this pipeline to avoid redundant network calls
                if(!tenantIdVsExpenditureTypeVsUuidsMap.containsKey(fiscalEvent.getTenantId()))
                    tenantIdVsExpenditureTypeVsUuidsMap.put(fiscalEvent.getTenantId(),
                            loadExpenditureTypeVsUuidMap(fiscalEvent.getTenantId()));

                fiscalDataEnrichmentService.enrichFiscalData(fiscalEvent);
                fiscalDataEnrichmentService.enrichComputedFields(fiscalEvent,
                        tenantIdVsExpenditureTypeVsUuidsMap.get(fiscalEvent.getTenantId()));
                log.info("Fiscal evennt before sending to index:"+fiscalEvent);
                producer.push(indexFiscalEventsTopic, FiscalEventRequest.builder().fiscalEvent(fiscalEvent).build());
            }
        }catch(Exception e) {
            log.error("Exception while reading from the queue: ", e);
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, HashSet<String>> loadExpenditureTypeVsUuidMap(String tenantId){
        COASearchCriteria criteria = COASearchCriteria.builder().tenantId(tenantId).build();
        COASearchRequest request = COASearchRequest.builder().requestHeader(new RequestHeader()).criteria(criteria).build();
        Object result = serviceRequestRepository.fetchResult(getIfixMasterDataUri(), request);
        COAResponse response = objectMapper.convertValue(result, COAResponse.class);
        HashMap<String, HashSet<String>> expenditureTypeVsUuidsMap = new HashMap<>();
        expenditureTypeVsUuidsMap.put("Others", new HashSet<>());
        expenditureTypeVsUuidsMap.put(electricityCoaHeadName, new HashSet<>());
        expenditureTypeVsUuidsMap.put(operationsCoaHeadName, new HashSet<>());
        log.info("expenditureTypeVsUuidsMap:" +expenditureTypeVsUuidsMap);
        response.getChartOfAccounts().forEach(chartOfAccount -> {
            log.info("COA ID: "+chartOfAccount.getId()+" COA CODE:"+chartOfAccount.getCoaCode());
            if(expenditureTypeVsUuidsMap.containsKey(electricityCoaHeadName)) {
                log.info("Inside electricity Coa head");
                expenditureTypeVsUuidsMap.get(electricityCoaHeadName).add(chartOfAccount.getId());
            }
            else if(expenditureTypeVsUuidsMap.containsKey(operationsCoaHeadName)) {
                log.info("Inside Operation Coa head");
                expenditureTypeVsUuidsMap.get(operationsCoaHeadName).add(chartOfAccount.getId());
            }
            else if(expenditureTypeVsUuidsMap.containsKey(salaryCoaHeadName)) {
                log.info("Inside salary Coa head");
                expenditureTypeVsUuidsMap.get(salaryCoaHeadName).add(chartOfAccount.getId());
            }
            else {
                log.info("Inside Other Coa head");
                expenditureTypeVsUuidsMap.get("Others").add(chartOfAccount.getId());
            }

        });
        return expenditureTypeVsUuidsMap;
    }

    private StringBuilder getIfixMasterDataUri() {
        StringBuilder uri = new StringBuilder(ifixMasterDataServiceHost);
        uri.append(ifixCoaSearchEndpoint);
        return uri;
    }
}

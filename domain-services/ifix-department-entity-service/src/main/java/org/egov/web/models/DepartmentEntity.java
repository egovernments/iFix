package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.egov.common.contract.AuditDetails;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This object captures the information for department entity
 */
@ApiModel(description = "This object captures the information for department entity")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2021-08-23T11:51:49.710+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentEntity {
    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("departmentId")
    private String departmentId = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("hierarchyLevel")
    private Integer hierarchyLevel = null;

    @JsonProperty("children")
    @Valid
    private List<String> children = new ArrayList<>();

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;


    public DepartmentEntity addChildrenItem(String childrenItem) {
        this.children.add(childrenItem);
        return this;
    }

}


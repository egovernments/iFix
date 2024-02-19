package org.digit.program.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.digit.program.configuration.ProgramConfiguration;
import org.digit.program.constants.SortOrder;
import org.digit.program.models.Pagination;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CommonUtil {

    private final ProgramConfiguration configs;
    private final ObjectMapper mapper;

    public CommonUtil(ProgramConfiguration configs, ObjectMapper mapper) {
        this.configs = configs;
        this.mapper = mapper;
    }

    public Pagination enrichSearch(Pagination pagination) {
        if (pagination == null)
            pagination = Pagination.builder().build();

        if (pagination.getLimit() == null) {
            pagination.setLimit(configs.getSearchDefaultLimit());
        } else if (pagination.getLimit() > configs.getSearchMaxLimit()) {
            pagination.setLimit(configs.getSearchMaxLimit());
        }
        if (pagination.getOffset() == null) {
            pagination.setOffset(0);
        }
        if (StringUtils.isEmpty(pagination.getSortBy())) {
            pagination.setSortBy("last_modified_time");
        }
        if (pagination.getSortOrder() == null) {
            pagination.setSortOrder(SortOrder.DESC);
        }
        return pagination;
    }

    public JsonNode getJsonNode(ResultSet rs, String key) throws SQLException {
        JsonNode jsonNode = null;

        try {

            PGobject obj = (PGobject) rs.getObject(key);
            if (obj != null) {
                jsonNode = mapper.readTree(obj.getValue());
            }

        } catch (IOException e) {
            throw new CustomException("PARSING_ERROR", "Error while parsing");
        }

        if(jsonNode== null || jsonNode.isEmpty())
            jsonNode = null;

        return jsonNode;
    }

    public PGobject getPGObject(Object jsonObject) {

        String value = null;
        try {
            value = mapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new CustomException();
        }

        PGobject json = new PGobject();
        json.setType("jsonb");
        try {
            json.setValue(value);
        } catch (SQLException e) {
            throw new CustomException("", "");
        }
        return json;
    }

}
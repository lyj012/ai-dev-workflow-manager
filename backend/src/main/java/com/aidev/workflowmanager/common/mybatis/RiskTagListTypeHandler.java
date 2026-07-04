package com.aidev.workflowmanager.common.mybatis;

import com.aidev.workflowmanager.common.enums.RiskTag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class RiskTagListTypeHandler extends BaseTypeHandler<List<RiskTag>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<RiskTag>> RISK_TAG_LIST = new TypeReference<List<RiskTag>>() {
    };

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<RiskTag> parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter == null ? Collections.emptyList() : parameter));
        } catch (JsonProcessingException ex) {
            throw new SQLException("Failed to serialize riskTags", ex);
        }
    }

    @Override
    public List<RiskTag> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<RiskTag> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<RiskTag> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private List<RiskTag> parse(String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<RiskTag>();
        }
        try {
            List<RiskTag> parsed = OBJECT_MAPPER.readValue(value, RISK_TAG_LIST);
            return parsed == null ? new ArrayList<RiskTag>() : parsed;
        } catch (IOException | IllegalArgumentException ex) {
            throw new SQLException("Failed to deserialize riskTags", ex);
        }
    }
}

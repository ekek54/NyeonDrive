package com.example.nyeondrive.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum FileOrderField {
    ID("id"),
    NAME("name"),
    EXTENSION("extension"),
    CONTENT_TYPE("contentType"),
    SIZE("size"),
    TRASHED("isTrashed"),
    CREATED_AT("createdAt");

    private final String fieldName;
    private static final Map<String, FileOrderField> fieldMap = Arrays.stream(values())
            .collect(Collectors.toMap(
                    FileOrderField::getFieldName,
                    fileOrderField -> fileOrderField
            ));

    FileOrderField(String fieldName) {
        this.fieldName = fieldName;
    }

    public static FileOrderField of(String fieldName) {
        log.info("orderBy field name: {}", fieldName);
        validateFieldName(fieldName);
        return fieldMap.get(fieldName);
    }

    private static void validateFieldName(String fieldName) {
        if (!fieldMap.containsKey(fieldName)) {
            throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }
    }
}

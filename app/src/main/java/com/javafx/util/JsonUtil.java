package com.javafx.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return MAPPER.readValue(json, clazz);
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) throws Exception {
        return MAPPER.readValue(json,
                MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static String toJson(Object obj) throws Exception {
        return MAPPER.writeValueAsString(obj);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}

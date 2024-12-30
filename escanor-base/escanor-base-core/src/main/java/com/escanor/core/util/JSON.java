/*
 * Copyright (c) 2024 Weimeng Liu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.escanor.core.util;

import com.escanor.core.exception.ResponseException;
import com.escanor.core.factory.ObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

public class JSON {
    private static final Log log = LogFactory.getLog(JSON.class);
    private static final ObjectMapper mapper = ObjectMapperFactory.getDefaultObjectMapper();

    static {
        // 忽略空值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private JSON() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    public static String toJSONString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("toJSONString error", e);
            throw new ResponseException("转换为JSON字符串异常", e);
        }
    }

    /**
     * 将对象转换为JSON字节数组
     *
     * @param object 对象
     * @return JSON字节数组
     */
    public static byte[] toJSONBytes(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (Exception e) {
            log.error("toJSONBytes error", e);
            throw new ResponseException("转换为JSON字节数组异常", e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json  JSON字符串
     * @param clazz 对象类型
     * @param <T>   对象类型
     * @return 对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("parseObject error", e);
            throw new ResponseException("Json字符串转换为实体异常", e);
        }
    }

    /**
     * 将JSON字符串转换为对象, 主要针对List, Map等复杂类型
     * <p>
     * <b>使用示例：</b> List&lt;String&gt; list = JSON.parseObject(json, new TypeReference&lt;List&lt;String&gt;&gt;() {});
     * </p>
     *
     * @param json          JSON字符串
     * @param typeReference 对象类型
     * @param <T>           对象类型
     * @return 对象
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("parseObject error", e);
            throw new ResponseException("Json字符串转换为实体异常", e);
        }
    }

    /**
     * 将JSON字节数组转换为对象
     *
     * @param json  JSON字节数组
     * @param clazz 对象类型
     * @param <T>   对象类型
     * @return 对象
     */
    public static <T> T parseObject(byte[] json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("parseObject error", e);
            throw new ResponseException("Json字符串转换为实体异常", e);
        }
    }

    /**
     * 将JSON字节数组转换为对象
     *
     * @param input json输入流
     * @param clazz 对象类型
     * @param <T>   对象类型
     * @return 对象
     */
    public static <T> T parseObject(InputStream input, Class<T> clazz) {
        try {
            return mapper.readValue(input, clazz);
        } catch (Exception e) {
            log.error("parseObject error", e);
            throw new ResponseException("Json字符串转换为实体异常", e);
        }
    }

    /**
     * 将JSON字符串转换为JsonNode
     *
     * @param content JSON字符串
     * @return JsonNode
     */
    public static JsonNode readTree(String content) {
        try {
            return mapper.readTree(content);
        } catch (Exception e) {
            log.error("readTree error", e);
            throw new ResponseException("Json字符串转换为JsonNode异常", e);
        }
    }

    /**
     * 提取简单类型（如 String, Integer 等）, 通过 Class<T> 指定简单类型，如 String.class, Integer.class 等。
     * <p>
     * <b>注意：</b> 由于 Java 的泛型擦除机制，无法直接使用 Class<T> 类型来转换复杂类型，需要使用 TypeReference<T> 类型，见方法 {@link #extractComplexValue(String, String, TypeReference)}。
     * </p>
     * <p>
     * <b>使用示例：</b> String name = extractSimpleValue(json, "$.name", String.class);。
     * </p>
     *
     * @param json      JSON 字符串
     * @param jsonPath  JSON Path 表达式
     * @param valueType 值类型
     * @param <T>       值类型
     * @return 值
     */
    public static <T> T extractSimpleValue(String json, String jsonPath, Class<T> valueType) {
        try {
            // 使用 Jackson 解析 JSON 字符串为 JsonNode
            Object document = mapper.readTree(json);

            // 使用 JsonPath 获取值
            Object result = JsonPath.read(document, jsonPath);

            // 将 JsonPath 返回的结果转换为指定的类型
            return mapper.convertValue(result, valueType);
        } catch (Exception e) {
            log.error("extractSimpleValue error", e);
            throw new ResponseException("根据jsonPath提取数据异常", e);
        }
    }

    /**
     * 提取复杂类型（如 List, Map 等）, 通过 TypeReference 指定复杂类型，如 List&lt;String&gt;, Map&lt;String, Object&gt; 等。
     * <p>
     * <b>使用示例：</b> List&lt;String&gt; list = extractComplexValue(json, "$.data", new TypeReference&lt;List&lt;String&gt;&gt;() {});。
     * </p>
     *
     * @param json          JSON 字符串
     * @param jsonPath      JSON Path 表达式
     * @param typeReference 复杂类型引用
     * @param <T>           复杂类型
     * @return 复杂类型
     */
    public static <T> T extractComplexValue(String json, String jsonPath, TypeReference<T> typeReference) {
        try {
            // 使用 Jackson 解析 JSON 字符串为 JsonNode
            Object document = mapper.readTree(json);

            // 使用 JsonPath 获取值
            Object result = JsonPath.read(document, jsonPath);

            // 将 JsonPath 返回的结果转换为指定的复杂类型
            return mapper.convertValue(result, typeReference);
        } catch (Exception e) {
            log.error("extractComplexValue error", e);
            throw new ResponseException("根据jsonPath提取数据异常", e);
        }
    }
}

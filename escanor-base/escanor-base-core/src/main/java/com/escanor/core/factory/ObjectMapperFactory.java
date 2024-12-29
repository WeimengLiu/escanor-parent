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

package com.escanor.core.factory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class ObjectMapperFactory {

    private ObjectMapperFactory() {
        // private constructor to prevent instantiation
    }

    /**
     * 该ObjectMapper用于通用JSON串的解析，不带字段类型定义</br>
     * Sample如下：
     * <pre>
     * {
     *     "id": "8471D7417AFF506AE0530100007F9787",
     *     "versionNo": 0,
     *     "createUser": "IMP",
     *     "createDate": "2019-03-19 15:58:00.000",
     *     "updateUser": null,
     *     "updateDate": "2019-03-19 15:58:00.000",
     *     "catelogNo": "INTFL",
     * }
     * </pre>
     */
    public static ObjectMapper getDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return objectMapper;
    }

    /**
     * 该ObjectMapper用于POJO的序列化，序列化后JSON中带有字段类型定义，不能用于通用JSON串的解析</br>
     * Sample如下：
     * <pre>
     * [
     *     "java.util.ArrayList",
     *     [
     *         [
     *             "com.escanor.core.entity.Demo",
     *             {
     *                 "id": "8471D7417AFF506AE0530100007F9787",
     *                 "versionNo": 0,
     *                 "createUser": "IMP",
     *                 "createDate": [
     *                     "java.sql.Timestamp",
     *                     1552995080000
     *                 ],
     *                 "updateUser": null,
     *                 "updateDate": [
     *                     "java.sql.Timestamp",
     *                     1552995080000
     *                 ],
     *                 "catelogNo": "INTFL",
     *             }
     *         ]
     *     ]
     * ]
     * </pre>
     */
    public static ObjectMapper getObjectMapperWithType() {
        ObjectMapper objectMapperWithType = new ObjectMapper();
        objectMapperWithType.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapperWithType.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        return objectMapperWithType;
    }
}

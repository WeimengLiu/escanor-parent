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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * XML工具类
 *
 * @author wmliu
 * @date 2024-12-30
 */
public class XMLUtils {

    private static final XmlMapper.Builder builder = XmlMapper.xmlBuilder();

    private static final Log log = LogFactory.getLog(XMLUtils.class);


    private XMLUtils() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * 将对象转换为XML字符串
     *
     * @param <T>        对象类型
     * @param bean       对象
     * @param encoding   字符集
     * @param ifFormat   是否格式化
     * @param ignoreHead 是否忽略头部
     * @return XML字符串
     */
    public static <T> String toXMLString(T bean, String encoding, boolean ifFormat, boolean ignoreHead) {
        //设定XML格式化输出
        if (ifFormat) {
            builder.enable(SerializationFeature.INDENT_OUTPUT);
        }

        //忽略未知属性
        builder.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        XmlMapper xmlMapper = builder.build();
        try {
            String xml = xmlMapper.writeValueAsString(bean);
            if (ignoreHead) {
                return xml;
            } else {
                return "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>" + xml;
            }
        } catch (Exception e) {
            log.error("toXML error", e);
            throw new ResponseException("转换为XML异常", e);
        }
    }

    public static <T> byte[] toXMLBytes(T bean, String encoding, boolean ifFormat, boolean ignoreHead) {
        try {
            String xml = toXMLString(bean, encoding, ifFormat, ignoreHead);
            return xml.getBytes(encoding);
        } catch (Exception e) {
            log.error("toXML error", e);
            throw new ResponseException("转换为XML异常", e);
        }
    }


    /**
     * 将对象转换为XML字符串
     *
     * @param bean 对象
     * @return XML字符串
     */
    public static String toXMLString(Object bean) {
        try {
            return toXMLString(bean, Charset.defaultCharset().toString(), false, true);
        } catch (Exception e) {
            log.error("toXML error", e);
            throw new ResponseException("转换为XML异常", e);
        }
    }


    /**
     * 将对象转换为XML字节数组
     *
     * @param bean     对象
     * @param encoding 字符集
     * @return XML字节数组
     */
    public static byte[] toXMLBytes(Object bean, String encoding) {
        try {
            return toXMLBytes(bean, encoding, false, true);
        } catch (Exception e) {
            log.error("toXML error", e);
            throw new ResponseException("转换为XML异常", e);
        }
    }

    /**
     * 将对象转换为XML字节数组
     *
     * @param bean    对象
     * @param charset 字符集
     * @return XML字节数组
     */
    public static byte[] toXMLBytes(Object bean, Charset charset) {
        try {
            return toXMLBytes(bean, charset.toString());
        } catch (Exception e) {
            log.error("toXML error", e);
            throw new ResponseException("转换为XML异常", e);
        }
    }

    /**
     * 将对象转换为XML字节数组
     *
     * @param bean 对象
     * @return XML字节数组
     */
    public static byte[] toXMLBytes(Object bean) {
        try {
            return toXMLBytes(bean, Charset.defaultCharset());
        } catch (Exception e) {
            log.error("toXML error", e);
            throw new ResponseException("转换为XML异常", e);
        }
    }

    /**
     * 将XML字符串转换为对象
     *
     * @param xml   XML字符串
     * @param clazz 对象类型
     * @param <T>   对象类型
     * @return 对象
     */
    public static <T> T toBean(String xml, Class<T> clazz) {
        try {
            XmlMapper xmlMapper = builder.build();
            return xmlMapper.readValue(xml, clazz);
        } catch (Exception e) {
            log.error("toBean error", e);
            throw new ResponseException("转换为Bean异常", e);
        }
    }

    /**
     * 将XML字节数组转换为对象
     *
     * @param <T>      对象类型
     * @param bytes    XML字节数组
     * @param encoding 字符集
     * @param clazz    对象类型
     * @return 对象
     */
    public static <T> T toBean(byte[] bytes, String encoding, Class<T> clazz) {
        try {
            return toBean(new String(bytes, encoding), clazz);
        } catch (Exception e) {
            log.error("toBean error", e);
            throw new ResponseException("转换为Bean异常", e);
        }
    }

    /**
     * 将XML字节数组转换为对象
     *
     * @param <T>   对象类型
     * @param bytes XML字节数组
     * @param clazz 对象类型
     * @return 对象
     */
    public static <T> T toBean(byte[] bytes, Class<T> clazz) {
        try {
            return toBean(bytes, Charset.defaultCharset().toString(), clazz);
        } catch (Exception e) {
            log.error("toBean error", e);
            throw new ResponseException("转换为Bean异常", e);
        }
    }

    /**
     * 将XML输入流转换为对象
     *
     * @param <T>      对象类型
     * @param stream   XML输入流
     * @param encoding 字符集
     * @param clazz    对象类型
     * @return 对象
     */
    public static <T> T toBean(InputStream stream, String encoding, Class<T> clazz) {
        try {
            String xml = StreamUtils.copyToString(stream, Charset.forName(encoding));
            return toBean(xml, clazz);
        } catch (Exception e) {
            log.error("toBean error", e);
            throw new ResponseException("转换为Bean异常", e);
        }
    }

    /**
     * 将XML输入流转换为对象
     *
     * @param <T>    对象类型
     * @param stream XML输入流
     * @param clazz  对象类型
     * @return 对象
     */
    public static <T> T toBean(InputStream stream, Class<T> clazz) {
        try {
            return toBean(stream, Charset.defaultCharset().toString(), clazz);
        } catch (Exception e) {
            log.error("toBean error", e);
            throw new ResponseException("转换为Bean异常", e);
        }
    }
}

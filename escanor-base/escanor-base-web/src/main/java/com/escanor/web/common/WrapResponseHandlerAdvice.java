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

package com.escanor.web.common;

import com.escanor.core.annotation.IgnoreWrapResponse;
import com.escanor.core.common.CommonHttpHeader;
import com.escanor.core.common.Response;
import com.escanor.core.exception.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.EMPTY_LIST;

@ControllerAdvice
public class WrapResponseHandlerAdvice implements ResponseBodyAdvice<Object> {

    final ObjectMapper objectMapper;

    public WrapResponseHandlerAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (returnType.hasMethodAnnotation(IgnoreWrapResponse.class)) {
            return body;
        }

        if (body == null) {
            Method method = returnType.getMethod();
            if (null != method) {
                if (Collection.class.isAssignableFrom(method.getReturnType())) {
                    body = EMPTY_LIST;
                    return Response.ok(body);
                } else if (Page.class.isAssignableFrom(method.getReturnType())) {
                    body = new PageImpl<>(Collections.emptyList());
                    return Response.ok(body);
                } else if (String.class.isAssignableFrom(method.getReturnType())) {
                    return null;
                }
            }
            return Response.ok();
        }

        if (body instanceof Response) {
            return body;
        }
        //http header中有忽略包装响应标志，直接返回
        HttpHeaders httpHeaders = request.getHeaders();
        if (!httpHeaders.isEmpty()) {
            String value = httpHeaders.getFirst(CommonHttpHeader.IGNORE_WRAP_RESPONSE.header());
            if (StringUtils.equals(value, CommonHttpHeader.IGNORE_WRAP_RESPONSE.value())) {
                return body;
            }
        }


        Response<?> data = Response.ok(body);
        if (body instanceof String) {
            try {
                return objectMapper.writeValueAsString(data);
            } catch (JsonProcessingException e) {
                throw new ResponseException(e);
            }
        }

        return data;
    }
}

package com.escanor.web.feign;

import com.escanor.core.exception.ResponseException;
import com.escanor.core.util.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

/**
 * Cloud内部调用对于FeignException统一处理
 *
 * @author : Accelerator
 * @date : 2019/3/27 9:52
 */
@Configuration
public class ExceptionErrorDecoder implements ErrorDecoder {

    private final Log logger = LogFactory.getLog(ExceptionErrorDecoder.class);

    @Override
    public Exception decode(String s, Response response) {
        try {
            if (response.body() != null) {
                String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                logger.error(body);
                if (null != body && body.startsWith("{") && body.endsWith("}")) {
                    JsonNode jsonNode = JSON.readTree(body);
                    if (StringUtils.isNotBlank(jsonNode.get("msg").asText())) {
                        return new ResponseException(jsonNode.get("msg").asText() + ":" + jsonNode.get("data").asText());
                    }
                }
            }
        } catch (Exception var4) {
            logger.error(var4.getMessage());
//            return new ResponseException("服务器内部错误:" + var4.getMessage());
        }
        return errorStatus(s, response);
    }

    private ResponseException errorStatus(String methodKey, Response response) {
        String message = format("status %s reading %s", response.status(), methodKey);
        try {
            if (response.body() != null) {
                String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                message += "; content:\n" + body;
            }
        } catch (IOException ignored) { // NOPMD
        }
        return new ResponseException("服务器内部错误:" + message);
    }

}

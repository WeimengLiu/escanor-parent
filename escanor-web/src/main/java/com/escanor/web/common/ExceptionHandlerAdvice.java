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

import com.escanor.core.common.Response;
import com.escanor.core.exception.ResponseException;
import com.escanor.core.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ControllerAdvice
@ResponseBody
public class ExceptionHandlerAdvice {

    protected final Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ResponseException.class)
    @ResponseBody
    Response<String> handleBadRequest(HttpServletRequest req, ResponseException ce) {
        printRequestUriAndException(req, ce, null);
        return Response.fail(ce.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    Response<String> handleBadRequest(HttpServletRequest req, MethodArgumentNotValidException ex) {
        printRequestUriAndException(req, ex, "数据校验异常");
        StringBuilder sb = new StringBuilder();
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        if (CollectionUtils.isNotEmpty(errors)) {
            errors.forEach(error -> sb.append("[").append(error.getDefaultMessage()).append("] "));
        }
        return Response.fail(sb.toString());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    Response<String> handleBadRequest(HttpServletRequest req, MissingServletRequestParameterException ex) {
        printRequestUriAndException(req, ex, "请求缺少关键参数");
        return Response.fail("请求缺少关键参数：[" + ex.getParameterName() + "]");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    Response<String> handleBadRequest(HttpServletRequest req, Throwable ex) {
        printRequestUriAndException(req, ex, null);
        String message = "后台程序异常";
        if (ex.getCause() instanceof ResponseException) {
            message = ex.getMessage();
        }
        for (Throwable se : ex.getSuppressed()) {
            if (se instanceof ResponseException) {
                message = se.getMessage();
            }
        }
        return Response.fail(message);
    }

    /**
     * HttpMessageNotReadableException
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    Response<String> handleBadRequest(HttpServletRequest req, HttpMessageNotReadableException ex) {
        printRequestUriAndException(req, ex, "报文格式错误，无法解析");
        if (logger.isDebugEnabled()) {
            try {
                ContentCachingRequestWrapper requestWrapper = WebUtils.getNativeRequest(req, ContentCachingRequestWrapper.class);
                String body = "";
                if (null != requestWrapper) {
                    body = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                }
                logger.debug("请求路径为：{}， 请求报文为：{}", req.getRequestURL(), body);
            } catch (Exception e) {
                logger.error("error:", e);
            }
        }
        return Response.fail("报文格式错误，无法解析");
    }

    /**
     * ObjectOptimisticLockingFailureException
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseBody
    Response<String> handleBadRequest(HttpServletRequest req, ObjectOptimisticLockingFailureException ex) {
        printRequestUriAndException(req, ex, "乐观锁异常");
        logger.error("后台程序异常：", ex);
        return Response.fail("数据已被更新或处理,请刷新页面");
    }

    private void printRequestUriAndException(HttpServletRequest request, Throwable e, String message) {
        logger.error("接口处理发生异常，接口路径为：{}", request.getRequestURI());
        if (StringUtils.hasText(message)) {
            logger.error(message, e);
        } else {
            logger.error("后台程序异常：", e);
        }
    }
}

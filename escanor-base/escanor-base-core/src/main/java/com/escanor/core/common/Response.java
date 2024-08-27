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

package com.escanor.core.common;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder
public class Response<T> {

    public static final String OK = "OK";

    public static final String FAIL = "FAIL";


    private int status;

    private String message;

    private T data;

    public Response(ResponseCode responseCode) {
        this.status = responseCode.getCode();
        this.message = responseCode.getMessage();
    }

    public Response(ResponseCode responseCode, T data) {
        this.status = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.data = data;
    }

    public Response(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Response<String> ok() {
        return new Response<>(ResponseCode.OK, OK);
    }

    public static Response<String> fail() {
        return new Response<>(ResponseCode.FAIL, FAIL);
    }

    public static <T> Response<T> ok(T data, String message) {
        return new Response<>(ResponseCode.OK.getCode(), message, data);
    }

    public static <T> Response<T> ok(T data) {
        return new Response<>(ResponseCode.OK, data);
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(ResponseCode.FAIL.getCode(), message, null);
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<>(code, message, null);
    }
}

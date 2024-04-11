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

package com.escanor.shiro.util;

import com.escanor.core.common.Response;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public abstract class HttpResponseHelper {
    public static void writeResponse(ServletResponse servletResponse, Response<?> response, int status) throws IOException {
        OutputStream outputStream = servletResponse.getOutputStream();
        outputStream.write(Json.toJsonBytes(response));
        if (WebUtils.isHttp(servletResponse)) {
            HttpServletResponse httpServletResponse = WebUtils.toHttp(servletResponse);
            httpServletResponse.setStatus(status);
        }
        //outputStream.flush();
    }

    public static void writeScOkResponse(ServletResponse servletResponse, Response<?> response) throws IOException {
        writeResponse(servletResponse, response,HttpServletResponse.SC_OK);
        //outputStream.flush();
    }

    public static void writeUnauthorizedResponse(ServletResponse servletResponse, Response<?> response) throws IOException {
        writeResponse(servletResponse, response,HttpServletResponse.SC_UNAUTHORIZED);
        //outputStream.flush();
    }
}

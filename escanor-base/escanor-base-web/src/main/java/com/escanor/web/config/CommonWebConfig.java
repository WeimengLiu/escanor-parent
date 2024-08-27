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

package com.escanor.web.config;

import com.escanor.web.common.IgnoreWrapResponseUrlMatcher;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletContext;
import java.util.TimeZone;

@Configuration
@EnableConfigurationProperties({BaseWebProperties.class})
@EnableWebMvc
public class CommonWebConfig {
    @Bean
    Jackson2ObjectMapperBuilderCustomizer customizer() {
        return objectMapperBuilder -> objectMapperBuilder.simpleDateFormat("yyyy-MM-dd hh:mm:ss").timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @Bean
    @RefreshScope
    IgnoreWrapResponseUrlMatcher ignoreWrapResponseUrlMatcher(BaseWebProperties webProperties, ServletContext servletContext) {
        return new IgnoreWrapResponseUrlMatcher(servletContext.getContextPath(), webProperties.getIgnoreWrapResponseUrls());
    }
}

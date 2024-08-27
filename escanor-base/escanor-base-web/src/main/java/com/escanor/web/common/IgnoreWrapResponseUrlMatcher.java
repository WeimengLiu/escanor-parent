package com.escanor.web.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.escanor.web.common.CommonConstants.ACTUATOR_ENDPOINT_PATTERN;

public class IgnoreWrapResponseUrlMatcher {

    private final String[] urls;

    public IgnoreWrapResponseUrlMatcher(String contextPath,String[] urls) {
        this.urls = deduplication(StringUtils.defaultString(contextPath, StringUtils.EMPTY), urls);
    }

    private String[] deduplication(String contextPath,String[] urls) {
        return Stream.of(urls).map(url -> contextPath + url).distinct().toArray(String[]::new);
    }

    public boolean match(String url) {
        return PatternMatchUtils.simpleMatch(this.urls, url);
    }
}

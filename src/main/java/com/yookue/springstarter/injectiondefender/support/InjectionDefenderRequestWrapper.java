/*
 * Copyright (c) 2022 Yookue Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.injectiondefender.support;


import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.web.util.ContentCachingRequestWrapper;
import com.yookue.commonplexus.javaseutil.util.MapPlainWraps;
import lombok.Getter;
import lombok.Setter;


/**
 * {@link org.springframework.web.util.ContentCachingRequestWrapper} for injection defender
 *
 * @author David Hsing
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class InjectionDefenderRequestWrapper extends ContentCachingRequestWrapper {
    private boolean xssClean = true;
    private Safelist xssWhitelist;

    public InjectionDefenderRequestWrapper(@Nonnull HttpServletRequest request) {
        super(request);
    }

    public InjectionDefenderRequestWrapper(@Nonnull HttpServletRequest request, int contentCacheLimit) {
        super(request, contentCacheLimit);
    }

    public InjectionDefenderRequestWrapper(@Nonnull HttpServletRequest request, boolean xssClean, @Nullable Safelist xssWhitelist) {
        super(request);
        this.xssClean = xssClean;
        this.xssWhitelist = xssWhitelist;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String getParameter(@Nullable String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String result = super.getParameter(name);
        return (StringUtils.isBlank(result) || !xssClean || xssWhitelist == null) ? result : Jsoup.clean(result, xssWhitelist);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> origin = super.getParameterMap();
        if (MapPlainWraps.isEmpty(origin) || !xssClean || xssWhitelist == null) {
            return origin;
        }
        Map<String, String[]> result = new LinkedHashMap<>(origin.size());
        for (Map.Entry<String, String[]> entry : origin.entrySet()) {
            String[] values;
            if (ArrayUtils.isEmpty(entry.getValue()) || !xssClean || xssWhitelist == null) {
                values = entry.getValue();
            } else {
                values = Arrays.stream(entry.getValue()).map(element -> StringUtils.isBlank(element) ? element : Jsoup.clean(element, xssWhitelist)).toArray(String[]::new);
            }
            result.put(entry.getKey(), values);
        }
        return result;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String[] getParameterValues(@Nullable String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String[] result = super.getParameterValues(name);
        if (ArrayUtils.isEmpty(result) || !xssClean || xssWhitelist == null) {
            return result;
        }
        return Arrays.stream(result).map(element -> StringUtils.isBlank(element) ? element : Jsoup.clean(element, xssWhitelist)).toArray(String[]::new);
    }

    @Override
    public Cookie[] getCookies() {
        Cookie[] result = super.getCookies();
        if (ArrayUtils.isNotEmpty(result)) {
            for (Cookie cookie : result) {
                String value = (StringUtils.isBlank(cookie.getValue()) || !xssClean || xssWhitelist == null) ? cookie.getValue() : Jsoup.clean(cookie.getValue(), xssWhitelist);
                cookie.setValue(value);
            }
        }
        return result;
    }

    @Override
    public String getHeader(@Nullable String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String result = super.getHeader(name);
        return (StringUtils.isBlank(result) || !xssClean || xssWhitelist == null) ? result : Jsoup.clean(result, xssWhitelist);
    }

    public String getRawParameter(@Nullable String name) {
        return StringUtils.isBlank(name) ? null : super.getParameter(name);
    }

    public Map<String, String[]> getRawParameterMap() {
        return super.getParameterMap();
    }

    public String[] getRawParameterValues(@Nullable String name) {
        return StringUtils.isBlank(name) ? null : super.getParameterValues(name);
    }

    public Cookie[] getRawCookies() {
        return super.getCookies();
    }

    public String getRawHeader(@Nullable String name) {
        return StringUtils.isBlank(name) ? null : super.getHeader(name);
    }
}

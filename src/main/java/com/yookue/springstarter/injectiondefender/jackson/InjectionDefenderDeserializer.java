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

package com.yookue.springstarter.injectiondefender.jackson;


import java.io.IOException;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.yookue.commonplexus.javaseutil.constant.CharVariantConst;
import com.yookue.commonplexus.javaseutil.util.BooleanUtilsWraps;
import com.yookue.commonplexus.javaseutil.util.JsoupParserWraps;
import com.yookue.commonplexus.springutil.util.AntPathWraps;
import com.yookue.commonplexus.springutil.util.UriUtilsWraps;
import com.yookue.commonplexus.springutil.util.WebUtilsWraps;
import com.yookue.springstarter.injectiondefender.event.SQLInjectionEvent;
import com.yookue.springstarter.injectiondefender.event.XSSInjectionEvent;
import com.yookue.springstarter.injectiondefender.exception.SQLInjectionException;
import com.yookue.springstarter.injectiondefender.exception.XSSInjectionException;
import com.yookue.springstarter.injectiondefender.property.InjectionDefenderProperties;
import com.yookue.springstarter.injectiondefender.util.InjectionDefenderUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;


/**
 * {@link com.fasterxml.jackson.databind.JsonDeserializer} for injection defender
 *
 * @author David Hsing
 */
@RequiredArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class InjectionDefenderDeserializer extends JsonDeserializer<String> {
    private final InjectionDefenderProperties properties;
    private ApplicationEventPublisher publisher;

    @Override
    public String deserialize(@Nullable JsonParser parser, @Nullable DeserializationContext context) throws IOException {
        if (parser == null) {
            return null;
        }
        String fieldValue = parser.getText();
        if (StringUtils.isBlank(fieldValue)) {
            return trimIfPossible(fieldValue);
        }
        HttpServletRequest request = WebUtilsWraps.getContextServletRequest();
        InjectionDefenderProperties.DefenderFilter filterProps = properties.getDefenderFilter();
        InjectionDefenderProperties.SqlProtection sqlProps = properties.getSqlProtection();
        InjectionDefenderProperties.XssProtection xssProps = properties.getXssProtection();
        if (request == null || BooleanUtilsWraps.allNotTrue(sqlProps.getEnabled(), xssProps.getEnabled())) {
            return trimIfPossible(fieldValue);
        }
        String servletPath = UriUtilsWraps.getServletPath(request);
        boolean sqlValidate = BooleanUtils.isTrue(sqlProps.getEnabled()) && (CollectionUtils.isEmpty(filterProps.getExcludePaths()) || !AntPathWraps.matchAnyPatterns(servletPath, filterProps.getExcludePaths()));
        boolean xssValidate = BooleanUtils.isTrue(xssProps.getEnabled()) && (CollectionUtils.isEmpty(filterProps.getExcludePaths()) || !AntPathWraps.matchAnyPatterns(servletPath, filterProps.getExcludePaths()));
        if (!sqlValidate && !xssValidate) {
            return trimIfPossible(fieldValue);
        }
        String fieldName = parser.getCurrentName();
        if (sqlValidate && InjectionDefenderUtils.maybeSqlInjection(fieldValue)) {
            if (publisher != null) {
                publisher.publishEvent(new SQLInjectionEvent(request, fieldName, fieldValue));
            }
            if (BooleanUtils.isTrue(sqlProps.getThrowsException())) {
                throw new SQLInjectionException("Request may be a malicious access", fieldName, fieldValue);
            }
        }
        if (xssValidate) {
            if (InjectionDefenderUtils.maybeXssInjection(fieldValue)) {
                if (publisher != null) {
                    publisher.publishEvent(new XSSInjectionEvent(request, fieldName, fieldValue));
                }
                if (BooleanUtils.isTrue(xssProps.getThrowsException())) {
                    throw new XSSInjectionException("Request may be a malicious access", fieldName, fieldValue);
                }
            }
            if (BooleanUtils.isTrue(xssProps.getCleanParams()) && StringUtils.containsAny(fieldValue, CharVariantConst.ANGLE_BRACKET_LEFT, CharVariantConst.ANGLE_BRACKET_RIGHT)) {
                fieldValue = JsoupParserWraps.cleanWith(fieldValue, xssProps.getWhitelistType());
            }
        }
        return trimIfPossible(fieldValue);
    }

    private String trimIfPossible(@Nullable String text) {
        return BooleanUtils.isTrue(properties.getTrimParams()) ? (BooleanUtils.isTrue(properties.getTrimToNull()) ? StringUtils.trimToNull(text) : StringUtils.trim(text)) : text;
    }
}

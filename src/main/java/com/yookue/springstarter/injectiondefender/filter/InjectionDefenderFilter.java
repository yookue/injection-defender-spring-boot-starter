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

package com.yookue.springstarter.injectiondefender.filter;


import java.io.IOException;
import java.util.Map;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.yookue.commonplexus.javaseutil.util.BooleanUtilsWraps;
import com.yookue.commonplexus.javaseutil.util.JsoupParserWraps;
import com.yookue.commonplexus.springutil.util.AntPathWraps;
import com.yookue.commonplexus.springutil.util.UriUtilsWraps;
import com.yookue.springstarter.injectiondefender.event.MaliciousSqlEvent;
import com.yookue.springstarter.injectiondefender.event.MaliciousXssEvent;
import com.yookue.springstarter.injectiondefender.exception.MaliciousSqlException;
import com.yookue.springstarter.injectiondefender.exception.MaliciousXssException;
import com.yookue.springstarter.injectiondefender.property.InjectionDefenderProperties;
import com.yookue.springstarter.injectiondefender.support.InjectionDefenderRequestWrapper;
import com.yookue.springstarter.injectiondefender.util.InjectionDefenderUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


/**
 * {@link jakarta.servlet.Filter} for injection defender
 *
 * @author David Hsing
 * @see org.springframework.web.filter.OncePerRequestFilter
 */
@RequiredArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class InjectionDefenderFilter extends OncePerRequestFilter implements ApplicationEventPublisherAware {
    private final InjectionDefenderProperties properties;

    @Setter
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain chain) throws ServletException, IOException {
        InjectionDefenderProperties.SqlProtection sqlProps = properties.getSqlProtection();
        InjectionDefenderProperties.XssProtection xssProps = properties.getXssProtection();
        boolean sqlValidate = BooleanUtils.isTrue(sqlProps.getEnabled()), xssValidate = BooleanUtils.isTrue(xssProps.getEnabled());
        Map<String, String[]> parameters = request.getParameterMap();
        if (!CollectionUtils.isEmpty(parameters)) {
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                if (sqlValidate && InjectionDefenderUtils.maybeSqlInjection(entry.getValue())) {
                    if (applicationEventPublisher != null) {
                        applicationEventPublisher.publishEvent(new MaliciousSqlEvent(request, entry.getKey(), entry.getValue()));
                    }
                    if (BooleanUtils.isTrue(sqlProps.getThrowsException())) {
                        throw new MaliciousSqlException("Request may be a malicious access", entry.getKey(), entry.getValue());
                    }
                }
                if (xssValidate && InjectionDefenderUtils.maybeXssInjection(entry.getValue())) {
                    if (applicationEventPublisher != null) {
                        applicationEventPublisher.publishEvent(new MaliciousXssEvent(request, entry.getKey(), entry.getValue()));
                    }
                    if (BooleanUtils.isTrue(xssProps.getThrowsException())) {
                        throw new MaliciousXssException("Request may be a malicious access", entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        if (xssValidate && BooleanUtils.isTrue(xssProps.getCleanParams())) {
            InjectionDefenderRequestWrapper wrapper = new InjectionDefenderRequestWrapper(request, true, JsoupParserWraps.getSafelist(xssProps.getWhitelistType()));
            chain.doFilter(wrapper, response);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) throws ServletException {
        InjectionDefenderProperties.DefenderFilter filterProps = properties.getDefenderFilter();
        if (!CollectionUtils.isEmpty(filterProps.getExcludePaths())) {
            String servletPath = UriUtilsWraps.getServletPath(request);
            return AntPathWraps.matchAnyPatterns(servletPath, filterProps.getExcludePaths());
        }
        InjectionDefenderProperties.SqlProtection sqlProps = properties.getSqlProtection();
        InjectionDefenderProperties.XssProtection xssProps = properties.getXssProtection();
        if (BooleanUtilsWraps.allNotTrue(sqlProps.getEnabled(), xssProps.getEnabled())) {
            return true;
        }
        return super.shouldNotFilter(request);
    }
}

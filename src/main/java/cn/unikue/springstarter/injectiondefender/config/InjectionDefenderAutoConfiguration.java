/*
 * Copyright (c) 2022 Unikue Ltd. All rights reserved.
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

package cn.unikue.springstarter.injectiondefender.config;


import java.util.Optional;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.unikue.commonplexus.javaseutil.util.CollectionPlainWraps;
import cn.unikue.commonplexus.javaseutil.util.MapPlainWraps;
import cn.unikue.commonplexus.springcondition.annotation.ConditionalOnAnyBooleanProperties;
import cn.unikue.commonplexus.springutil.jackson.deserializer.StringTrimmerDeserializer;
import cn.unikue.springstarter.injectiondefender.advice.StringTrimmerEmptyAdvice;
import cn.unikue.springstarter.injectiondefender.advice.StringTrimmerNullAdvice;
import cn.unikue.springstarter.injectiondefender.filter.InjectionDefenderFilter;
import cn.unikue.springstarter.injectiondefender.jackson.InjectionDefenderDeserializer;
import cn.unikue.springstarter.injectiondefender.property.InjectionDefenderProperties;


/**
 * Configuration of injection defender
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = InjectionDefenderAutoConfiguration.PROPERTIES_PREFIX, name = "enabled", matchIfMissing = true)
@ConditionalOnWebApplication
@EnableConfigurationProperties(value = InjectionDefenderProperties.class)
public class InjectionDefenderAutoConfiguration implements WebMvcConfigurer {
    public static final String PROPERTIES_PREFIX = "spring.injection-defender";    // $NON-NLS-1$
    public static final String STRING_TRIMMER_EMPTY_ADVICE = "stringTrimmerEmptyAdvice";    // $NON-NLS-1$
    public static final String STRING_TRIMMER_NULL_ADVICE = "stringTrimmerNullAdvice";    // $NON-NLS-1$
    public static final String JACKSON_STRING_TRIMMER_CUSTOMIZER = "jacksonStringTrimmerCustomizer";    // $NON-NLS-1$
    public static final String JACKSON_INJECTION_DEFENDER_CUSTOMIZER = "jacksonInjectionDefenderCustomizer";    // $NON-NLS-1$

    @Bean
    @ConditionalOnMissingBean(value = InjectionDefenderFilter.class, parameterizedContainer = FilterRegistrationBean.class)
    public FilterRegistrationBean<InjectionDefenderFilter> injectionDefenderFilterRegistration(@Nonnull InjectionDefenderProperties properties, @Nonnull ApplicationEventPublisher publisher) {
        InjectionDefenderFilter filter = new InjectionDefenderFilter(properties, publisher);
        FilterRegistrationBean<InjectionDefenderFilter> result = new FilterRegistrationBean<>(filter);
        InjectionDefenderProperties.DefenderFilter props = properties.getDefenderFilter();
        Optional.ofNullable(props.getFilerOrder()).ifPresent(result::setOrder);
        CollectionPlainWraps.ifNotEmpty(props.getFilterPaths(), result::setUrlPatterns);
        MapPlainWraps.ifNotEmpty(props.getFilterParams(), result::setInitParameters);
        return result;
    }

    @Bean(name = STRING_TRIMMER_EMPTY_ADVICE)
    @ConditionalOnBooleanProperty(prefix = InjectionDefenderAutoConfiguration.PROPERTIES_PREFIX, name = "trim-params", havingValue = false, matchIfMissing = true)
    @ConditionalOnMissingBean(name = STRING_TRIMMER_EMPTY_ADVICE)
    public StringTrimmerEmptyAdvice stringTrimmerEmptyAdvice() {
        return new StringTrimmerEmptyAdvice();
    }

    @Bean(name = STRING_TRIMMER_NULL_ADVICE)
    @ConditionalOnBooleanProperty(prefix = InjectionDefenderAutoConfiguration.PROPERTIES_PREFIX, name = "trim-params")
    @ConditionalOnMissingBean(name = STRING_TRIMMER_NULL_ADVICE)
    public StringTrimmerNullAdvice stringTrimmerNullAdvice() {
        return new StringTrimmerNullAdvice();
    }

    @Bean(name = JACKSON_STRING_TRIMMER_CUSTOMIZER)
    @ConditionalOnBooleanProperty(prefix = InjectionDefenderAutoConfiguration.PROPERTIES_PREFIX, name = "trim-params")
    @ConditionalOnClass(value = ObjectMapper.class)
    @ConditionalOnMissingBean(name = JACKSON_STRING_TRIMMER_CUSTOMIZER)
    @Order(value = 200)
    public Jackson2ObjectMapperBuilderCustomizer jacksonStringTrimmerCustomizer(@Nonnull InjectionDefenderProperties properties) {
        StringTrimmerDeserializer deserializer = new StringTrimmerDeserializer(BooleanUtils.isTrue(properties.getTrimToNull()));
        return builder -> builder.deserializerByType(String.class, deserializer);
    }

    @Bean(name = JACKSON_INJECTION_DEFENDER_CUSTOMIZER)
    @ConditionalOnAnyBooleanProperties(value = {
        @ConditionalOnBooleanProperty(prefix = InjectionDefenderAutoConfiguration.PROPERTIES_PREFIX + ".sql-protection", name = "enabled", matchIfMissing = true),
        @ConditionalOnBooleanProperty(prefix = InjectionDefenderAutoConfiguration.PROPERTIES_PREFIX + ".xss-protection", name = "enabled", matchIfMissing = true)
    })
    @ConditionalOnClass(value = ObjectMapper.class)
    @ConditionalOnMissingBean(name = JACKSON_INJECTION_DEFENDER_CUSTOMIZER)
    @Order(value = 300)
    public Jackson2ObjectMapperBuilderCustomizer jacksonInjectionDefenderCustomizer(@Nonnull InjectionDefenderProperties properties, @Nonnull ApplicationEventPublisher publisher) {
        InjectionDefenderDeserializer deserializer = new InjectionDefenderDeserializer(properties, publisher);
        return builder -> builder.deserializerByType(String.class, deserializer);
    }
}

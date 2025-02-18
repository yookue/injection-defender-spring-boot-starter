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

package com.yookue.springstarter.injectiondefender.property;


import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import com.yookue.commonplexus.javaseutil.enumeration.JsoupWhitelistType;
import com.yookue.springstarter.injectiondefender.config.InjectionDefenderAutoConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * Properties for injection defender
 *
 * @author David Hsing
 */
@ConfigurationProperties(prefix = InjectionDefenderAutoConfiguration.PROPERTIES_PREFIX)
@Getter
@Setter
@ToString
public class InjectionDefenderProperties implements Serializable {
    /**
     * Indicates whether to enable this starter or not
     * <p>
     * Default is {@code true}
     */
    private Boolean enabled = true;

    /**
     * Indicates whether to trim request parameters or not
     */
    private Boolean trimParams;

    /**
     * Indicates whether to trim empty request parameters to null or not
     */
    private Boolean trimToNull;

    /**
     * Defender filter attributes
     */
    private final DefenderFilter defenderFilter = new DefenderFilter();

    /**
     * Sql protection attributes
     */
    private final SqlProtection sqlProtection = new SqlProtection();

    /**
     * Xss protection attributes
     */
    private final XssProtection xssProtection = new XssProtection();


    /**
     * Properties for defender filter
     *
     * @author David Hsing
     * @see com.yookue.springstarter.injectiondefender.filter.InjectionDefenderFilter
     */
    @Getter
    @Setter
    @ToString
    public static class DefenderFilter implements Serializable {
        /**
         * The priority order of the filter
         * <p>
         * Default is {@code Ordered.HIGHEST_PRECEDENCE + 100}
         */
        private Integer filerOrder = Ordered.HIGHEST_PRECEDENCE + 100;

        /**
         * The url patterns of the filter
         */
        private Set<String> filterPaths;

        /**
         * The init parameters of the filter
         */
        private Map<String, String> filterParams;

        /**
         * The url patterns that ignored by the filter
         */
        private Set<String> excludePaths;
    }


    /**
     * Properties for sql protection
     *
     * @author David Hsing
     */
    @Getter
    @Setter
    @ToString
    public static class SqlProtection implements Serializable {
        /**
         * Indicates whether to enable sql protection or not
         * <p>
         * Default is {@code true}
         */
        private Boolean enabled = true;

        /**
         * Indicates whether to throw a {@link com.yookue.springstarter.injectiondefender.exception.MaliciousSqlException} when injection occurred or not
         * <p>
         * Default is {@code true}
         */
        private Boolean throwsException = true;
    }


    /**
     * Properties for xss protection
     *
     * @author David Hsing
     */
    @Getter
    @Setter
    @ToString
    public static class XssProtection implements Serializable {
        /**
         * Indicates whether to enable xss protection or not
         * <p>
         * Default is {@code true}
         */
        private Boolean enabled = true;

        /**
         * Indicates whether to clean injection or not
         * <p>
         * Default is {@code true}
         */
        private Boolean cleanParams = true;

        /**
         * Indicates whether to throw a {@link com.yookue.springstarter.injectiondefender.exception.MaliciousXssException} when injection occurred or not
         */
        private Boolean throwsException;

        /**
         * The jsoup {@link org.jsoup.safety.Safelist} for cleaning xss
         */
        private JsoupWhitelistType whitelistType = JsoupWhitelistType.RELAXED;
    }
}

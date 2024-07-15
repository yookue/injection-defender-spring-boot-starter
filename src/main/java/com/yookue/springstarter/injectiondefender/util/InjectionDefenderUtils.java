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

package com.yookue.springstarter.injectiondefender.util;


import java.util.Collection;
import java.util.regex.Pattern;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import com.yookue.commonplexus.javaseutil.util.ArrayUtilsWraps;
import com.yookue.commonplexus.javaseutil.util.RegexUtilsWraps;


/**
 * Utilities for injection defender
 *
 * @author David Hsing
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public abstract class InjectionDefenderUtils {
    private static final Pattern SQL_QUOTE = Pattern.compile("^\\s*\\S*(['\"])+\\)*\\s*(and\\b|or\\b|union\\b|select\\b|insert\\b|update\\b|delete\\b|create\\b|drop\\b|truncate\\b|grant\\b|alter\\b|execute\\b|exec\\b|declare\\b|order\\b|show\\b|#|--|}\\)|/\\*)+", Pattern.CASE_INSENSITIVE);    // $NON-NLS-1$
    private static final Pattern XSS_BRACKET = Pattern.compile("<(iframe|script)\\b", Pattern.CASE_INSENSITIVE);    // $NON-NLS-1$

    public static boolean maybeSqlInjection(@Nullable CharSequence sequence) {
        return StringUtils.isNotBlank(sequence) && RegexUtilsWraps.find(sequence, SQL_QUOTE);
    }

    public static boolean maybeSqlInjection(@Nullable CharSequence... sequences) {
        return maybeSqlInjection(ArrayUtilsWraps.asList(sequences));
    }

    public static boolean maybeSqlInjection(@Nullable Collection<? extends CharSequence> sequences) {
        return !CollectionUtils.isEmpty(sequences) && sequences.stream().anyMatch(InjectionDefenderUtils::maybeSqlInjection);
    }

    public static boolean maybeXssInjection(@Nullable CharSequence sequence) {
        return StringUtils.isNotBlank(sequence) && RegexUtilsWraps.find(sequence, XSS_BRACKET);
    }

    public static boolean maybeXssInjection(@Nullable CharSequence... sequences) {
        return maybeXssInjection(ArrayUtilsWraps.asList(sequences));
    }

    public static boolean maybeXssInjection(@Nullable Collection<? extends CharSequence> sequences) {
        return !CollectionUtils.isEmpty(sequences) && sequences.stream().anyMatch(InjectionDefenderUtils::maybeXssInjection);
    }
}

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


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.yookue.commonplexus.javaseutil.util.StackTraceWraps;
import lombok.extern.slf4j.Slf4j;


/**
 * Tests for {@link com.yookue.springstarter.injectiondefender.util.InjectionDefenderUtils}
 *
 * @author David Hsing
 */
@Slf4j
class InjectionDefenderUtilsTest {
    @Test
    void startWithNegative() {
        String injection = "-1' union select 1,2,3 --+";
        boolean result = InjectionDefenderUtils.maybeSqlInjection(injection);
        log.info("{} = {}", StackTraceWraps.getExecutingMethodName(), result);
        Assertions.assertTrue(result);
    }

    @Test
    void startWithPositive() {
        String injection = "1' or show database();";
        boolean result = InjectionDefenderUtils.maybeSqlInjection(injection);
        log.info("{} = {}", StackTraceWraps.getExecutingMethodName(), result);
        Assertions.assertTrue(result);
    }

    @Test
    void startWithAlphabetic() {
        String injection = "admin' or create table --";
        boolean result = InjectionDefenderUtils.maybeSqlInjection(injection);
        log.info("{} = {}", StackTraceWraps.getExecutingMethodName(), result);
        Assertions.assertTrue(result);
    }

    @Test
    void startWithAlphanumeric() {
        String injection = "admin-777' or drop table --";
        boolean result = InjectionDefenderUtils.maybeSqlInjection(injection);
        log.info("{} = {}", StackTraceWraps.getExecutingMethodName(), result);
        Assertions.assertTrue(result);
    }
}

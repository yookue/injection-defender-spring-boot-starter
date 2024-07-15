/*
 * Copyright (c) 2022-2023 Yookue Ltd. All rights reserved.
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

package com.yookue.springstarter.injectiondefender.exception;


import jakarta.annotation.Nullable;
import com.yookue.commonplexus.javaseutil.exception.MaliciousAccessException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.StandardException;


/**
 * Checked exception thrown that may be a sql injection
 *
 * @author David Hsing
 */
@NoArgsConstructor
@Getter
@StandardException
@SuppressWarnings("unused")
public class SQLInjectionException extends MaliciousAccessException {
    private String paramName;
    private Object paramValue;

    public SQLInjectionException(@Nullable String message, @Nullable String paramName, @Nullable Object paramValue) {
        super(message);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public SQLInjectionException(@Nullable String message, @Nullable Throwable cause, @Nullable String paramName, @Nullable Object paramValue) {
        super(message, cause);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public SQLInjectionException(@Nullable Throwable cause, @Nullable String paramName, @Nullable Object paramValue) {
        super(cause);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }
}

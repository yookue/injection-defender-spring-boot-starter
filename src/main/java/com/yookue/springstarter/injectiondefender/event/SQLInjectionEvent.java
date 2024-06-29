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

package com.yookue.springstarter.injectiondefender.event;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.context.ApplicationEvent;
import lombok.Getter;


/**
 * Event when sql injection be intercepted
 *
 * @author David Hsing
 * @see org.springframework.web.context.support.ServletRequestHandledEvent
 * @see com.yookue.springstarter.injectiondefender.exception.SQLInjectionException
 */
@Getter
@SuppressWarnings("unused")
public class SQLInjectionEvent extends ApplicationEvent {
    private String paramName;
    private Object paramValue;

    /**
     * Constructs the event with the object which initially occurred
     * <p>
     * If thrown by filter, this may be a {@link javax.servlet.http.HttpServletRequest}
     * <br>
     * If thrown by jackson, this may be a {@link com.fasterxml.jackson.core.JsonParser}
     */
    public SQLInjectionEvent(@Nonnull Object source) {
        super(source);
    }

    public SQLInjectionEvent(@Nonnull Object source, @Nullable String paramName, @Nullable Object paramValue) {
        super(source);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }
}
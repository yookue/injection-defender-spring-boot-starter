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

package com.yookue.springstarter.injectiondefender.advice;


import jakarta.annotation.Nonnull;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebBindingInitializer;
import lombok.AllArgsConstructor;


/**
 * Binds {@link org.springframework.beans.propertyeditors.StringTrimmerEditor} to {@link org.springframework.web.bind.WebDataBinder}
 *
 * @author David Hsing
 * @reference "https://blog.csdn.net/u012165930/article/details/78942938"
 */
@AllArgsConstructor
@ControllerAdvice
@RestControllerAdvice
@SuppressWarnings({"unused", "JavadocDeclaration", "JavadocLinkAsPlainText"})
public class StringTrimmerEmptyAdvice implements WebBindingInitializer {
    @InitBinder
    public void initBinder(@Nonnull WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }
}

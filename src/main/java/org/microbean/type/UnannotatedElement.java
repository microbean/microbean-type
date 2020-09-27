/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.type;

import java.lang.annotation.Annotation;

import java.lang.reflect.AnnotatedElement;

/**
 * A deliberately perverse {@link AnnotatedElement} implementation
 * that reports no annotations by default.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public class UnannotatedElement implements AnnotatedElement {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link UnannotatedElement}.
   */
  public UnannotatedElement() {
    super();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns an empty {@link Annotation} array.
   *
   * @return an emtpy {@link Annotation} array
   *
   * @nullability This method does not and its overrides must not
   * return {@code null}.
   *
   * @threadsafety This method is and its overrides must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is and its overrides must be idempotent
   * and deterministic.
   */
  @Override
  public Annotation[] getAnnotations() {
    return AbstractType.EMPTY_ANNOTATION_ARRAY.clone();
  }

  /**
   * Returns {@code null} when invoked with any argument.
   *
   * @return {@code null}
   *
   * @nullability This method does and its overrides can return {@code
   * null}.
   *
   * @threadsafety This method is and its overrides must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is and its overrides must be idempotent
   * and deterministic.
   */
  @Override
  public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
    return null;
  }

  /**
   * Returns an empty {@link Annotation} array.
   *
   * @return an emtpy {@link Annotation} array
   *
   * @nullability This method does not and its overrides must not
   * return {@code null}.
   *
   * @threadsafety This method is and its overrides must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is and its overrides must be idempotent
   * and deterministic.
   */
  @Override
  public Annotation[] getDeclaredAnnotations() {
    return AbstractType.EMPTY_ANNOTATION_ARRAY.clone();
  }

}

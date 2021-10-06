/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020–2021 microBean™.
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

import java.io.Serializable;

import java.lang.annotation.Annotation;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * A common abstract superclass for {@link Type} implementations.
 *
 * <p>All {@link AbstractType} implementations are at least lossily
 * {@linkplain Serializable serializable}.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class AbstractType implements Serializable, Type {


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization purposes}.
   */
  private static final long serialVersionUID = 1L;

  /**
   * A zero-length array of {@link AnnotatedType} instances.
   *
   * @nullability This field is never {@code null}.
   */
  protected static final AnnotatedType[] EMPTY_ANNOTATED_TYPE_ARRAY = new AnnotatedType[0];

  /**
   * A zero-length array of {@link Annotation} instances.
   *
   * @nullability This field is never {@code null}.
   */
  protected static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

  /**
   * A zero-length array of {@link TypeVariable} instances.
   *
   * @nullability This field is never {@code null}.
   */
  protected static final TypeVariable<?>[] EMPTY_TYPE_VARIABLE_ARRAY = new TypeVariable<?>[0];

  /**
   * A zero-length array of {@link Type} instances.
   *
   * @nullability This field is never {@code null}.
   */
  public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractType}.
   */
  protected AbstractType() {
    super();
  }

}

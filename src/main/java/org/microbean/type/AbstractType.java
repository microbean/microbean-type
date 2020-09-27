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
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class AbstractType implements Type, Serializable {

  private static final long serialVersionUID = 1L;
  
  protected static final AnnotatedType[] EMPTY_ANNOTATED_TYPE_ARRAY = new AnnotatedType[0];

  protected static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

  protected static final TypeVariable<?>[] EMPTY_TYPE_VARIABLE_ARRAY = new TypeVariable<?>[0];

  protected static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

  protected static final Type[] OBJECT = new Type[] { Object.class };

  protected AbstractType() {
    super();
  }

  protected static final Serializable toSerializableType(final Type type) {
    final Serializable returnValue;
    if (type == null || type instanceof Serializable) {
      returnValue = (Serializable)type;
    } else if (type instanceof ParameterizedType) {
      returnValue = new DefaultParameterizedType((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      returnValue = DefaultGenericArrayType.valueOf((GenericArrayType)type);
    } else if (type instanceof TypeVariable) {
      returnValue = new PartiallyImplementedTypeVariable((TypeVariable<?>)type);
    } else if (type instanceof WildcardType) {
      returnValue = new AbstractWildcardType((WildcardType)type);
    } else {
      throw new IllegalArgumentException("Unexpected type: " + type);
    }
    return returnValue;
  }

}

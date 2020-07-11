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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.function.BiFunction;

import org.microbean.development.annotation.Experimental;

/**
 * An abstract {@link TypeSemantics} that, in general, compares {@link
 * Type}s using invariant semantics.
 *
 * <p>Normally {@link java.lang.reflect.WildcardType} instances would
 * be compared using covariant semantics so often subclasses will
 * override the {@link #getSemanticsFor(Type, Type)}
 * appropriately.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see CovariantTypeSemantics
 *
 * @see TypeSemantics
 */
@Experimental
public abstract class InvariantTypeSemantics extends TypeSemantics {

  /**
   * Creates a new {@link InvariantTypeSemantics}.
   *
   * @param box whether or not {@link Type}s will be {@linkplain
   * Types#box(Type) boxed} before testing their assignability
   *
   * @see #isBoxing()
   */
  protected InvariantTypeSemantics(final boolean box) {
    super(box);
  }


  /*
   * Instance methods.
   */

  
  @Override
  protected final boolean isAssignable(final Class<?> receiverType,
                                       final Class<?> payloadType) {
    return this.box(receiverType).equals(this.box(payloadType));
  }

  @Override
  protected final boolean isAssignable(final ParameterizedType receiverType,
                                       final ParameterizedType payloadType) {
    final boolean returnValue;
    if (receiverType.getRawType().equals(payloadType.getRawType())) {
      final Type[] receiverActualTypeArguments = receiverType.getActualTypeArguments();
      final Type[] payloadActualTypeArguments = payloadType.getActualTypeArguments();
      if (receiverActualTypeArguments.length == payloadActualTypeArguments.length) {
        boolean temp = true;
        for (int i = 0; i < receiverActualTypeArguments.length; i++) {
          if (!this.isAssignable(receiverActualTypeArguments[i], payloadActualTypeArguments[i])) {
            temp = false;
            break;
          }
        }
        returnValue = false;
      } else {
        returnValue = false;
      }
    } else {
      returnValue = false;
    }
    return returnValue;
  }

  @Override
  protected final boolean isAssignable(final TypeVariable<?> receiverType,
                                       final TypeVariable<?> payloadType) {
    return receiverType.equals(payloadType);
  }

}

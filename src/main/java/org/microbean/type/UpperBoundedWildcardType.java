/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Optional;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

import static org.microbean.type.ConstantDescs.CD_Type;
import static org.microbean.type.ConstantDescs.CD_UpperBoundedWildcardType;

/**
 * A {@link WildcardType} implementation that has only {@linkplain
 * WildcardType#getUpperBounds() upper bounds}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see WildcardType
 *
 * @see WildcardType#getUpperBounds()
 */
public final class UpperBoundedWildcardType extends AbstractWildcardType implements Constable {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link UpperBoundedWildcardType}.
   *
   * @param upperBound the sole {@linkplain
   * WildcardType#getUpperBounds() upper bound}; may be {@code null}
   * in which case an array consisting solely of {@link Object
   * Object.class} will be used instead
   */
  public UpperBoundedWildcardType(final Type upperBound) {
    super(upperBound == null ? (Type[])null : new Type[] { upperBound }, null);
  }

  /**
   * Creates a new {@link UpperBoundedWildcardType}.
   *
   * @param upperBounds the {@linkplain
   * WildcardType#getUpperBounds() upper bounds};
   * may be {@code null}
   */
  public UpperBoundedWildcardType(final Type[] upperBounds) {
    super(upperBounds, null);
  }

  /**
   * Creates a new {@link UpperBoundedWildcardType}.
   *
   * @param wildcardType the {@link WildcardType} whose {@link
   * WildcardType#getUpperBounds() upper bounds} will be effectively
   * copied; may be {@code null}
   * in which case an array consisting solely of {@link Object
   * Object.class} will be used instead
   */
  public UpperBoundedWildcardType(final WildcardType wildcardType) {
    super(wildcardType == null ? new Type[] { Object.class } : wildcardType.getUpperBounds(), null);
  }

  @Override
  public final Optional<? extends ConstantDesc> describeConstable() {
    final Type[] upperBounds = this.getUpperBounds();
    // Upper bounded (extends).
    final int bsmInvokeArgumentsLength = upperBounds.length + 1;
    final ConstantDesc[] bsmInvokeArguments = new ConstantDesc[bsmInvokeArgumentsLength];
    bsmInvokeArguments[0] = MethodHandleDesc.ofConstructor(CD_UpperBoundedWildcardType, CD_Type.arrayType());
    for (int i = 1; i < bsmInvokeArgumentsLength; i++) {
      final Optional<? extends ConstantDesc> arg = JavaTypes.describeConstable(upperBounds[i - 1]);
      if (arg.isEmpty()) {
        return Optional.empty();
      }
      bsmInvokeArguments[i] = arg.orElseThrow();
    }
    return Optional.of(DynamicConstantDesc.of(BSM_INVOKE, bsmInvokeArguments));
  }
  

  /*
   * Static methods.
   */


  /**
   * If the supplied {@link WildcardType} is a {@link
   * UpperBoundedWildcardType}, returns it; otherwise creates a new
   * {@link UpperBoundedWildcardType} with the supplied {@link
   * WildcardType}'s {@linkplain WildcardType#getUpperBounds() upper
   * bounds} and returns it.
   *
   * @param wildcardType the {@link WildcardType} to effectively copy
   * (or return); may be {@code null} in which case upper bounds
   * consisting of an array consisting solely of {@link Object
   * Object.class} will be used instead
   *
   * @return a non-{@code null} {@link UpperBoundedWildcardType}
   *
   * @exception NullPointerException if {@code wildcardType} is {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link UpperBoundedWildcardType} with
   * each invocation).
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final UpperBoundedWildcardType of(final WildcardType wildcardType) {
    if (wildcardType instanceof UpperBoundedWildcardType w) {
      return w;
    } else {
      return new UpperBoundedWildcardType(wildcardType);
    }
  }


}

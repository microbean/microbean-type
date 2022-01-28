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

import static org.microbean.type.ConstantDescs.CD_LowerBoundedWildcardType;
import static org.microbean.type.ConstantDescs.CD_Type;

/**
 * A {@link java.lang.reflect.WildcardType} implementation that has
 * only {@linkplain java.lang.reflect.WildcardType#getLowerBounds()
 * lower bounds}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see java.lang.reflect.WildcardType
 *
 * @see java.lang.reflect.WildcardType#getLowerBounds()
 */
public final class LowerBoundedWildcardType extends AbstractWildcardType implements Constable {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link LowerBoundedWildcardType}.
   *
   * @param lowerBound the sole {@linkplain
   * java.lang.reflect.WildcardType#getLowerBounds() lower bound}; may
   * be {@code null}
   */
  public LowerBoundedWildcardType(final Type lowerBound) {
    super(null, lowerBound == null ? (Type[])null : new Type[] { lowerBound });
  }

  /**
   * Creates a new {@link LowerBoundedWildcardType}.
   *
   * @param lowerBounds the {@linkplain
   * java.lang.reflect.WildcardType#getLowerBounds() lower bounds};
   * may be {@code null}
   */
  // It is critical that lowerBounds remain a varargs parameter, not
  // an array type. See JavaTypes::describeConstable(WildcardType).
  public LowerBoundedWildcardType(final Type... lowerBounds) {
    super(null, lowerBounds);
  }

  /**
   * Creates a new {@link LowerBoundedWildcardType}.
   *
   * @param wildcardType the {@link WildcardType} whose {@linkplain
   * WildcardType#getLowerBounds() lower bounds} will be effectively
   * copied; must not be {@code null}
   *
   * @exception NullPointerException if {@code wildcardType} is {@code
   * null}
   *
   * @exception IllegalArgumentException if {@code wildcardType} has
   * no lower bounds
   *
   * @see WildcardType#getLowerBounds()
   */
  public LowerBoundedWildcardType(final WildcardType wildcardType) {
    super(null, wildcardType.getLowerBounds());
    if (this.getLowerBound() == null) {
      throw new IllegalArgumentException("wildcardType: " + wildcardType);
    }
  }


  /*
   * Instance methods.
   */


  @Override // Constable
  public final Optional<? extends ConstantDesc> describeConstable() {
    final Type[] lowerBounds = this.getLowerBounds();
    final int bsmInvokeArgumentsLength = lowerBounds.length + 1;
    final ConstantDesc[] bsmInvokeArguments = new ConstantDesc[bsmInvokeArgumentsLength];
    bsmInvokeArguments[0] = MethodHandleDesc.ofConstructor(CD_LowerBoundedWildcardType, CD_Type.arrayType());
    for (int i = 1; i < bsmInvokeArgumentsLength; i++) {
      final Optional<? extends ConstantDesc> arg = JavaTypes.describeConstable(lowerBounds[i - 1]);
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
   * LowerBoundedWildcardType}, returns it; otherwise creates a new {@link
   * LowerBoundedWildcardType} with the supplied {@link WildcardType}'s
   * {@linkplain WildcardType#getUpperBounds() upper bounds} and
   * {@linkplain WildcardType#getLowerBounds() lower bounds} and
   * returns it.
   *
   * @param wildcardType the {@link WildcardType} to effectively copy
   * (or return); must not be {@code null}
   *
   * @return a non-{@code null} {@link LowerBoundedWildcardType}
   *
   * @exception NullPointerException if {@code wildcardType} is {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link LowerBoundedWildcardType} with
   * each invocation).
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final LowerBoundedWildcardType of(final WildcardType wildcardType) {
    if (wildcardType instanceof LowerBoundedWildcardType w) {
      return w;
    } else {
      return new LowerBoundedWildcardType(wildcardType);
    }
  }

}

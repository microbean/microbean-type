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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.StringJoiner;

import org.microbean.development.annotation.Convenience;

class AbstractWildcardType implements WildcardType {


  /*
   * Instance fields.
   */


  private final Type[] upperBounds;

  private final Type[] lowerBounds;

  private final int hashCode;


  /*
   * Constructors.
   */


  AbstractWildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
    super();
    if (upperBounds == null || upperBounds.length <= 0) {
      this.upperBounds = new Type[] { Object.class };
    } else if (upperBounds.length == 1) {
      this.upperBounds = upperBounds.clone();
    } else {
      throw new IllegalArgumentException("upperBounds.length > 1: " + Arrays.asList(upperBounds));
    }
    if (lowerBounds == null || lowerBounds.length <= 0) {
      this.lowerBounds = JavaTypes.emptyTypeArray();
    } else if (lowerBounds.length == 1) {
      this.lowerBounds = lowerBounds.clone();
    } else {
      throw new IllegalArgumentException("lowerBounds.length > 1: " + Arrays.asList(lowerBounds));
    }
    // Wildcards can't have primitives or other wildcards
    // (non-reference types) anywhere in their bounds.
    for (final Type upperBound : this.upperBounds) {
      if (!JavaTypes.isReferenceType(upperBound)) {
        throw new IllegalArgumentException("upperBounds contains non-reference type: " + Arrays.asList(this.upperBounds));
      }
    }
    for (final Type lowerBound : this.lowerBounds) {
      if (!JavaTypes.isReferenceType(lowerBound)) {
        throw new IllegalArgumentException("lowerBounds contains non-reference type: " + Arrays.asList(this.lowerBounds));
      }
    }
    this.hashCode = this.computeHashCode();
  }

  AbstractWildcardType(final WildcardType other) {
    this(other.getUpperBounds(), other.getLowerBounds());
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the sole upper bound of this {@link
   * AbstractWildcardType}.
   *
   * <p>Wildcard types as defined in the <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1"
   * target="_parent">Java Language Specification</a> may have exactly
   * one upper bound.  {@link WildcardType} permits {@linkplain
   * WildcardType#getUpperBounds() permits many}, for no good reason.
   * This method makes it easier to get the sole upper bound of a
   * wildcard type.</p>
   *
   * @return the sole upper bound of this {@link
   * AbstractWildcardType}; never {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see WildcardType#getUpperBounds()
   */
  @Convenience
  public final Type getUpperBound() {
    return this.upperBounds[0];
  }

  /**
   * Returns the sole lower bound of this {@link
   * AbstractWildcardType}, if there is one, or {@code null}.
   *
   * <p>Wildcard types as defined in the <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5.1"
   * target="_parent">Java Language Specification</a> may have zero or
   * one lower bound.  {@link WildcardType} permits {@linkplain
   * WildcardType#getLowerBounds() permits many}, for no good reason.
   * This method makes it easier to get the sole lower bound of a
   * wildcard type.</p>
   *
   * @return the sole lower bound of this {@link
   * AbstractWildcardType}, if there is one, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see WildcardType#getLowerBounds()
   */
  @Convenience
  public final Type getLowerBound() {
    return this.lowerBounds.length <= 0 ? null : this.lowerBounds[0];
  }

  @Override // WildcardType
  public final Type[] getUpperBounds() {
    return this.upperBounds.clone();
  }

  @Override // WildcardType
  public final Type[] getLowerBounds() {
    return this.lowerBounds.clone();
  }

  @Override // Object
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    return JavaTypes.hashCode(this);
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof WildcardType w) {
      return JavaTypes.equals(this, w);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return JavaTypes.toString(this);
  }


  /*
   * Static methods.
   */


  /**
   * If the supplied {@link WildcardType} is an {@link
   * AbstractWildcardType}, returns it; otherwise creates a new {@link
   * AbstractWildcardType} with the supplied {@link WildcardType}'s
   * {@linkplain WildcardType#getUpperBounds() upper bounds} and
   * {@linkplain WildcardType#getLowerBounds() lower bounds} and
   * returns it.
   *
   * @param wildcardType the {@link WildcardType} to effectively copy
   * (or return); must not be {@code null}
   *
   * @return a non-{@code null} {@link AbstractWildcardType}
   *
   * @exception NullPointerException if {@code wildcardType} is {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link AbstractWildcardType} with each
   * invocation).
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final AbstractWildcardType of(final WildcardType wildcardType) {
    if (wildcardType instanceof AbstractWildcardType) {
      return (AbstractWildcardType)wildcardType;
    } else {
      return new AbstractWildcardType(wildcardType);
    }
  }

}

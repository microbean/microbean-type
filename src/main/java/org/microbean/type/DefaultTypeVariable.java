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

import java.lang.annotation.Annotation;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.Objects;

import java.util.function.Predicate;

/**
 * A {@link TypeVariable} implementation.
 *
 * @param <T> the kind of {@link GenericDeclaration} that this {@link
 * DefaultTypeVariable} was declared by
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class DefaultTypeVariable<T extends GenericDeclaration> implements TypeVariable<T> {


  /*
   * Instance fields.
   */


  private final int hashCode;

  private final TypeVariable<? extends T> delegate;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link DefaultTypeVariable}.
   *
   * @param other a {@link TypeVariable} providing important locating
   * information; must not be {@code null}
   *
   * @exception NullPointerException if {@code other} is {@code null}
   */
  public DefaultTypeVariable(final TypeVariable<? extends T> other) {
    this(other.getGenericDeclaration(), tv -> tv.getName().equals(other.getName()));
  }

  /**
   * Creates a new {@link DefaultTypeVariable}.
   *
   * @param genericDeclaration a {@link GenericDeclaration} whose
   * {@linkplain GenericDeclaration#getTypeParameters() type
   * variables} will be inspected; must not be {@code null}
   *
   * @param name the value that the return value of an invocation of
   * {@link TypeVariable#getName()} must have when invoked on a member
   * of the array returned by an invocation of {@link
   * GenericDeclaration#getTypeParameters()} on the supplied {@code
   * genericDeclaration}; must not be {@code null}
   *
   * @exception NullPointerException if {@code genericDeclaration} is
   * {@code null}
   *
   * @exception IllegalArgumentException if the supplied {@code name}
   * identifies no {@link TypeVariable}s
   */
  public DefaultTypeVariable(final GenericDeclaration genericDeclaration, final String name) {
    this(genericDeclaration, tv -> tv.getName().equals(name));
  }

  /**
   * Creates a new {@link DefaultTypeVariable}.
   *
   * @param genericDeclaration a {@link GenericDeclaration} whose
   * {@linkplain GenericDeclaration#getTypeParameters() type
   * variables} will be inspected; must not be {@code null}
   *
   * @param predicate the {@link Predicate} that will identify the
   * {@link TypeVariable} that this {@link DefaultTypeVariable} will
   * wrap; must not be {@code null}
   *
   * @exception NullPointerException if either {@code
   * genericDeclaration} or {@code predicate} is {@code null}
   *
   * @exception IllegalArgumentException if the supplied {@link
   * Predicate} identifies no {@link TypeVariable}s
   */
  @SuppressWarnings("unchecked")
  public DefaultTypeVariable(final GenericDeclaration genericDeclaration, final Predicate<? super TypeVariable<?>> predicate) {
    super();
    TypeVariable<? extends T> delegate = null;
    for (final TypeVariable<?> tv : genericDeclaration.getTypeParameters()) {
      if (predicate.test(tv)) {
        delegate = (TypeVariable<? extends T>)tv;
        break;
      }
    }
    if (delegate == null) {
      throw new IllegalArgumentException("genericDeclaration: " + genericDeclaration + "; predicate: " + predicate);
    }
    this.delegate = delegate;
    this.hashCode = this.computeHashCode();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link TypeVariable} this {@link DefaultTypeVariable} wraps.
   *
   * @return the {@link TypeVariable} this {@link DefaultTypeVariable}
   * wraps; never {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public final TypeVariable<? extends T> getDelegate() {
    return this.delegate;
  }

  @Override
  public final String getName() {
    return this.delegate.getName();
  }

  @Override
  public final T getGenericDeclaration() {
    return this.delegate.getGenericDeclaration();
  }

  @Override
  public final Type[] getBounds() {
    return this.delegate.getBounds();
  }

  @Override
  public final AnnotatedType[] getAnnotatedBounds() {
    return this.delegate.getAnnotatedBounds();
  }

  @Override
  public final <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
    return this.delegate.getAnnotation(annotationClass);
  }

  @Override
  public final Annotation[] getAnnotations() {
    return this.delegate.getAnnotations();
  }

  @Override
  public final <T extends Annotation> T[] getAnnotationsByType(final Class<T> annotationClass) {
    return this.delegate.getAnnotationsByType(annotationClass);
  }

  @Override
  public final Annotation[] getDeclaredAnnotations() {
    return this.delegate.getDeclaredAnnotations();
  }

  @Override
  public final <T extends Annotation> T[] getDeclaredAnnotationsByType(final Class<T> annotationClass) {
    return this.delegate.getDeclaredAnnotationsByType(annotationClass);
  }

  @Override
  public final <T extends Annotation> T getDeclaredAnnotation(final Class<T> annotationClass) {
    return this.delegate.getDeclaredAnnotation(annotationClass);
  }

  @Override
  public final boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
    return this.delegate.isAnnotationPresent(annotationClass);
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    return JavaTypes.hashCode(this.delegate);
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof TypeVariable<?> tv) {
      return JavaTypes.equals(this, tv);
    } else {
      return false;
    }
  }

  @Override
  public final String toString() {
    return JavaTypes.toString(this);
  }

  /**
   * Returns a {@link DefaultTypeVariable} representing the supplied
   * {@link TypeVariable}.
   *
   * <p>If the supplied {@link TypeVariable} is:</p>
   *
   * <ul>
   *
   * <li>{@code null}: {@code null} is returned</li>
   *
   * <li>an instance of {@link DefaultTypeVariable}: the supplied
   * {@code type} is returned</li>
   *
   * <li>anything else: a new {@link DefaultTypeVariable} is returned</li>
   *
   * </ul>
   *
   * @param <T> the type of {@link GenericDeclaration} that declares
   * the type variable
   *
   * @param type the {@link TypeVariable} in question; may be {@code
   * null} in which case {@code null} will be returned
   *
   * @return a {@link DefaultTypeVariable}, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final <T extends GenericDeclaration> DefaultTypeVariable<T> valueOf(final TypeVariable<T> type) {
    if (type == null) {
      return null;
    } else if (type instanceof DefaultTypeVariable<T> tv) {
      return tv;
    } else {
      return new DefaultTypeVariable<>(type);
    }
  }

}

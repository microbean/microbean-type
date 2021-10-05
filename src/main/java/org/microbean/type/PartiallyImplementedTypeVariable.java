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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.annotation.Annotation;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.Objects;

/**
 * A deliberately partial implementation of the {@link TypeVariable}
 * interface suitable only for use cases that involve checking
 * {@linkplain #getBounds()} and no other.
 *
 * <p>Specifically, instances of this class return a {@link
 * NonexistentGenericDeclaration} instance from the {@link
 * #getGenericDeclaration()} method, and contain no annotation
 * information.</p>
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see NonexistentGenericDeclaration
 */
public final class PartiallyImplementedTypeVariable extends AbstractType implements TypeVariable<NonexistentGenericDeclaration> {


  /*
   * Static fields.
   */


  private static final long serialVersionUID = 1L;


  /*
   * Instance fields.
   */


  private String name;

  private transient Type[] bounds;

  private transient int hashCode;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link PartiallyImplementedTypeVariable}.
   *
   * @param name the name of this {@link
   * PartiallyImplementedTypeVariable}; must not be {@code null}
   *
   * @param bounds the bounds of this {@link
   * PartiallyImplementedTypeVariable}; may be {@code null} or
   * zero-length in which case a new {@link Type} array consisting of
   * a single {@link Object Object.class} element will be used instead
   *
   * @exception NullPointerException if {@code name} is {@code null}
   */
  public PartiallyImplementedTypeVariable(final String name,
                                          final Type[] bounds) {
    super();
    this.name = Objects.requireNonNull(name, "name");
    if (bounds == null || bounds.length <= 0) {
      this.bounds = new Type[] { Object.class };
    } else {
      this.bounds = bounds.clone();
    }
    this.hashCode = this.computeHashCode();
  }

  PartiallyImplementedTypeVariable(final TypeVariable<? extends GenericDeclaration> other) {
    this(other.getGenericDeclaration(), other.getName());
  }

  PartiallyImplementedTypeVariable(final GenericDeclaration genericDeclaration,
                                   final String name) {
    super();
    this.name = name;
    TypeVariable<?> realVariable = null;
    for (final TypeVariable<?> tv : genericDeclaration.getTypeParameters()) {
      if (tv.getName().equals(name)) {
        realVariable = tv;
        break;
      }
    }
    this.bounds = realVariable.getBounds();
    this.hashCode = this.computeHashCode();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the name of this {@link PartiallyImplementedTypeVariable}.
   *
   * @return the name of this {@link PartiallyImplementedTypeVariable}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final String getName() {
    return this.name;
  }

  /**
   * Returns a {@link NonexistentGenericDeclaration} instance when
   * invoked.
   *
   * @return a {@link NonexistentGenericDeclaration} instance when
   * invoked
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final NonexistentGenericDeclaration getGenericDeclaration() {
    return NonexistentGenericDeclaration.INSTANCE;
  }

  /**
   * Returns the bounds of this {@link PartiallyImplementedTypeVariable}.
   *
   * <p>The bounds returned may be modified by the caller.</p>
   *
   * @return the bounds of this {@link PartiallyImplementedTypeVariable}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final Type[] getBounds() {
    return this.bounds.clone();
  }

  /**
   * Returns a copy of {@link AbstractType#EMPTY_ANNOTATED_TYPE_ARRAY} when invoked.
   *
   * @return a copy of {@link AbstractType#EMPTY_ANNOTATED_TYPE_ARRAY}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final AnnotatedType[] getAnnotatedBounds() {
    return AbstractType.EMPTY_ANNOTATED_TYPE_ARRAY.clone();
  }

  /**
   * Returns {@code null} when invoked.
   *
   * @param annotationClass ignored
   *
   * @return {@code null} in all cases
   *
   * @nullability This method always returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
    return null;
  }

  /**
   * Returns a copy of {@link AbstractType#EMPTY_ANNOTATION_ARRAY} when invoked.
   *
   * @return a copy of {@link AbstractType#EMPTY_ANNOTATION_ARRAY}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final Annotation[] getAnnotations() {
    return EMPTY_ANNOTATION_ARRAY.clone();
  }

  /**
   * Returns a copy of {@link AbstractType#EMPTY_ANNOTATION_ARRAY} when invoked.
   *
   * @return a copy of {@link AbstractType#EMPTY_ANNOTATION_ARRAY}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final Annotation[] getDeclaredAnnotations() {
    return EMPTY_ANNOTATION_ARRAY.clone();
  }

  /**
   * Returns a hashcode for this {@link
   * PartiallyImplementedTypeVariable}.
   *
   * @return a hashcode for this {@link
   * PartiallyImplementedTypeVariable}
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  private final int computeHashCode() {
    return Objects.hash(this.name, this.bounds);
  }
  
  /**
   * Returns {@code true} if the supplied {@link Object} is equal to
   * this {@link PartiallyImplementedTypeVariable}; {@code false}
   * otherwise.
   *
   * <p>An arbitrary {@link Object} is equal to this {@link
   * PartiallyImplementedTypeVariable} if it is an instance of {@link
   * TypeVariable} and its {@linkplain TypeVariable#getName() name}
   * {@linkplain String#equals(Object) is equal to} this {@link
   * PartiallyImplementedTypeVariable}'s {@linkplain #getName() name}
   * and if its {@linkplain TypeVariable#getBounds() bounds}
   * {@linkplain Arrays#equals(Object[], Object[]) are equal to} this
   * {@link PartiallyImplementedTypeVariable}'s {@linkplain
   * #getBounds() bounds}.  <strong>Note that this definition of
   * equality is deliberately different from that honored by the
   * default JDK-supplied implementation of {@link
   * TypeVariable}.</strong></p>
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Object} is equal to
   * this {@link PartiallyImplementedTypeVariable}; {@code false}
   * otherwise
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof TypeVariable<?>) {
      final TypeVariable<?> her = (TypeVariable<?>)other;
      return
        Objects.equals(this.name, her.getName()) &&
        Arrays.equals(this.bounds, her.getBounds());
    } else {
      return false;
    }
  }

  /**
   * Returns the return value of invoking the {@link #getName()}
   * method.
   *
   * @return the return value of invoking the {@link #getName()}
   * method
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @Override
  public final String toString() {
    return this.getName();
  }

  private final void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
    stream.defaultReadObject();
    final Serializable[] serializableBounds = (Serializable[])stream.readObject();
    if (serializableBounds == null || serializableBounds.length <= 0) {
      this.bounds = new Type[] { Object.class };
    } else if (serializableBounds.length == 1) {
      final Object bound = serializableBounds[0];
      if (bound == null) {
        this.bounds = new Type[] { Object.class };
      } else {
        this.bounds = new Type[] { (Type)bound };
      }
    } else {
      this.bounds = new Type[serializableBounds.length];
      System.arraycopy(serializableBounds, 0, this.bounds, 0, serializableBounds.length);
    }
    this.hashCode = this.computeHashCode();
  }

  private final void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    final Type[] originalBounds = this.bounds;
    if (originalBounds.length <= 0) {
      stream.writeObject(new Serializable[0]);
    } else {
      final Serializable[] newBounds = new Serializable[originalBounds.length];
      for (int i = 0; i < newBounds.length; i++) {
        newBounds[i] = Types.toSerializableType(originalBounds[i]);
      }
      stream.writeObject(newBounds);
    }
  }

  public static final PartiallyImplementedTypeVariable valueOf(final TypeVariable<? extends GenericDeclaration> type) {
    if (type == null) {
      return null;
    } else if (type instanceof PartiallyImplementedTypeVariable) {
      return (PartiallyImplementedTypeVariable)type;
    } else {
      return new PartiallyImplementedTypeVariable(type);
    }
  }

}

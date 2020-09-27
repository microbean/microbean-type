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


  public PartiallyImplementedTypeVariable(final String name,
                                          final Type[] bounds) {
    super();
    this.name = Objects.requireNonNull(name, "name");
    this.bounds = bounds == null || bounds.length <= 0 ? OBJECT : bounds;
    this.hashCode =
      Objects.hash(this.getName(),
                   this.getBounds());
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
    this.hashCode =
      Objects.hash(this.getName(),
                   this.getBounds());
  }


  /*
   * Instance methods.
   */


  @Override
  public final String getName() {
    return this.name;
  }

  @Override
  public final NonexistentGenericDeclaration getGenericDeclaration() {
    return NonexistentGenericDeclaration.INSTANCE;
  }

  @Override
  public final Type[] getBounds() {
    return this.bounds.clone();
  }

  @Override
  public final AnnotatedType[] getAnnotatedBounds() {
    return AbstractType.EMPTY_ANNOTATED_TYPE_ARRAY.clone();
  }

  @Override
  public final <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
    return null;
  }

  @Override
  public final Annotation[] getAnnotations() {
    return EMPTY_ANNOTATION_ARRAY.clone();
  }

  @Override
  public final Annotation[] getDeclaredAnnotations() {
    return EMPTY_ANNOTATION_ARRAY.clone();
  }

  @Override
  public final int hashCode() {
    return this.hashCode;
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof TypeVariable<?>) {
      final TypeVariable<?> her = (TypeVariable<?>)other;
      return
        Objects.equals(this.getName(), her.getName()) &&
        Arrays.equals(this.getBounds(), her.getBounds());
    } else {
      return false;
    }
  }

  @Override
  public final String toString() {
    return this.getName();
  }

  private final void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
    stream.defaultReadObject();
    final Object bounds = stream.readObject();
    assert bounds instanceof Serializable[];
    final Serializable[] serializableBounds = (Serializable[])bounds;
    if (serializableBounds == null || serializableBounds.length <= 0) {
      this.bounds = OBJECT;
    } else if (serializableBounds.length == 1) {
      final Object bound = serializableBounds[0];
      if (bound == null || Object.class.equals(bound)) {
        this.bounds = OBJECT;
      } else {
        assert bound instanceof Type;
        this.bounds = new Type[] { (Type)bound };
      }
    } else {
      this.bounds = new Type[serializableBounds.length];
      System.arraycopy(serializableBounds, 0, this.bounds, 0, serializableBounds.length);
    }
    this.hashCode =
      Objects.hash(this.getName(),
                   this.getBounds());
  }
  
  private final void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    final Type[] originalBounds = this.getBounds();
    if (originalBounds.length <= 0) {
      stream.writeObject(new Serializable[0]);
    } else {
      final Serializable[] newBounds = new Serializable[originalBounds.length];
      for (int i = 0; i < newBounds.length; i++) {
        newBounds[i] = AbstractType.toSerializableType(originalBounds[i]);
      }
      stream.writeObject(newBounds);
    }
  }

}

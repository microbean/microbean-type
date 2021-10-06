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
 * A {@link TypeVariable} implementation that is also {@link
 * Serializable}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
final class DefaultTypeVariable<T extends GenericDeclaration> extends AbstractType implements TypeVariable<T> {


  /*
   * Static fields.
   */


  private static final long serialVersionUID = 1L;


  /*
   * Instance fields.
   */


  private transient int hashCode;

  private transient TypeVariable<? extends T> delegate;


  /*
   * Constructors.
   */


  DefaultTypeVariable(final TypeVariable<? extends T> other) {
    this(other.getGenericDeclaration(), tv -> tv.getName().equals(other.getName()));
  }

  DefaultTypeVariable(final T genericDeclaration, final String name) {
    this(genericDeclaration, tv -> tv.getName().equals(name));
  }

  @SuppressWarnings("unchecked")
  DefaultTypeVariable(final T genericDeclaration, final Predicate<? super TypeVariable<?>> predicate) {
    super();
    for (final TypeVariable<?> tv : genericDeclaration.getTypeParameters()) {
      if (predicate.test(tv)) {
        this.delegate = (TypeVariable<? extends T>)tv;
        break;
      }
    }
    if (this.delegate == null) {
      throw new IllegalArgumentException("genericDeclaration: " + genericDeclaration + "; predicate: " + predicate);
    }
    this.hashCode = this.computeHashCode();
  }


  /*
   * Instance methods.
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
    return Types.hashCode(this.delegate);
  }

  @Override
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof TypeVariable<?>) {
      return Types.equals(this, (TypeVariable<?>)other);
    } else {
      return false;
    }
  }

  @Override
  public final String toString() {
    return this.delegate.toString();
  }

  @SuppressWarnings("unchecked")
  private final void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
    stream.defaultReadObject();
    final T gd;
    final Object o = stream.readObject();
    if (o instanceof Class) {
      gd = (T)o;
    } else if (o instanceof String) {
      final String name = (String)o;
      final Class<?>[] parameterTypes = (Class<?>[])stream.readObject();
      final Class<?> declaringClass = (Class<?>)stream.readObject();
      T temp = null;
      try {
        if ("<init>".equals(name)) {
          temp = (T)declaringClass.getDeclaredConstructor(parameterTypes);
        } else {
          temp = (T)declaringClass.getDeclaredMethod(name, parameterTypes);
        }
      } catch (final ReflectiveOperationException reflectiveOperationException) {
        throw new IOException(reflectiveOperationException.getMessage(), reflectiveOperationException);
      } finally {
        gd = temp;
      }
    } else {
      throw new IllegalStateException();
    }
    final String name = (String)stream.readObject();
    for (final TypeVariable<?> tv : gd.getTypeParameters()) {
      if (tv.getName().equals(name)) {
        this.delegate = (TypeVariable<? extends T>)tv;
        break;
      }
    }
    if (this.delegate == null) {
      throw new IllegalStateException();
    }
    this.hashCode = this.computeHashCode();
  }

  private final void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    final GenericDeclaration gd = this.getGenericDeclaration();
    if (gd instanceof Class) {
      stream.writeObject(gd);
    } else if (gd instanceof Executable) {
      final Executable e = (Executable)gd;
      stream.writeObject(e instanceof Constructor ? "<init>" : ((Method)e).getName());
      stream.writeObject(e.getParameterTypes());
      stream.writeObject(e.getDeclaringClass());
    } else {
      throw new AssertionError();
    }
    stream.writeObject(this.delegate.getName());
  }

  public static final <T extends GenericDeclaration> DefaultTypeVariable<T> valueOf(final TypeVariable<T> type) {
    if (type == null) {
      return null;
    } else if (type instanceof DefaultTypeVariable) {
      return (DefaultTypeVariable<T>)type;
    } else {
      return new DefaultTypeVariable<>(type);
    }
  }

}

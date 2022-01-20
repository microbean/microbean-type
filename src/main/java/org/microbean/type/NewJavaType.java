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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.microbean.development.annotation.Experimental;

@Experimental
public class NewJavaType extends NewType<Type> {

  public static final Map<Type, Class<?>> wrapperTypes =
    Map.of(boolean.class, Boolean.class,
           byte.class, Byte.class,
           char.class, Character.class,
           double.class, Double.class,
           float.class, Float.class,
           int.class, Integer.class,
           long.class, Long.class,
           short.class, Short.class,
           void.class, Void.class);

  public NewJavaType(final JavaType.Token<?> type) {
    this(type.type());
  }
  
  public NewJavaType(final Type type) {
    super(type);
  }

  @Override
  public boolean named() {
    final Type type = this.object();
    return type instanceof Class || type instanceof TypeVariable;
  }

  public String name() {
    final Type type = this.object();
    if (type instanceof Class<?> c) {
      return c.getName();
    } else if (type instanceof TypeVariable<?> tv) {
      return tv.getName();
    }
    return null;
  }

  @Override
  public boolean represents(final NewType<?> type) {
    if (super.represents(type)) {
      return true;
    }
    // TODO: other stuff
    return false;
  }

  @Override
  public final boolean top() {
    return this.object() == Object.class;
  }
  
  @Override
  public NewJavaType box() {
    final Type type = this.object();
    if (type == void.class) {
      return of(Void.class);
    } else if (type instanceof Class<?> c && c.isPrimitive()) {
      return of(wrapperTypes.get(c));
    } else {
      return this;
    }
  }

  @Override
  public Collection<NewJavaType> directSupertypes() {
    final Collection<Type> directSupertypes = JavaTypes.directSupertypes(this.object());
    if (!directSupertypes.isEmpty()) {
      final Collection<NewJavaType> c = new ArrayList<>(directSupertypes.size());
      for (final Type type : directSupertypes) {
        c.add(of(type));
      }
      return Collections.unmodifiableCollection(c);
    }
    return List.of();
  }

  @Override
  public NewJavaType type() {
    final Type type = this.object();
    final Type newType = type(type);
    return newType == type ? this : of(newType);
  }

  @Override
  public boolean hasTypeParameters() {
    return this.object() instanceof Class<?> c && c.getTypeParameters().length > 0;
  }

  @Override
  public boolean hasTypeArguments() {
    return this.object() instanceof ParameterizedType;
  }

  @Override
  public List<NewJavaType> typeArguments() {
    final Type type = this.object();
    if (type instanceof ParameterizedType p) {
      final Type[] typeArguments = p.getActualTypeArguments();
      final List<NewJavaType> typeArgumentsList = new ArrayList<>(typeArguments.length);
      for (final Type typeArgument : typeArguments) {
        typeArgumentsList.add(of(typeArgument));
      }
      return Collections.unmodifiableList(typeArgumentsList);
    }
    return List.of();
  }

  public List<NewJavaType> typeParameters() {
    final Type type = this.object();
    if (type instanceof Class<?> c) {
      final Type[] typeParameters = c.getTypeParameters();
      if (typeParameters.length > 0) {
        final List<NewJavaType> typeParametersList = new ArrayList<>(typeParameters.length);
        for (final Type typeParameter : typeParameters) {
          typeParametersList.add(of(typeParameter));
        }
        return Collections.unmodifiableList(typeParametersList);
      }
    }
    return List.of();
  }
  
  @Override
  public NewJavaType componentType() {
    final Type newType;
    final Type type = this.object();
    if (type instanceof Class<?> c) {
      newType = c.getComponentType();
    } else if (type instanceof GenericArrayType g) {
      newType = g.getGenericComponentType();
    } else {
      newType = null;
    }
    if (newType == null) {
      return null;
    }
    return of(newType);
  }

  @Override
  public boolean upperBounded() {
    final Type type = this.object();
    return type instanceof WildcardType || type instanceof TypeVariable;
  }

  @Override
  public boolean lowerBounded() {
    final Type type = this.object();
    return type instanceof WildcardType w && w.getLowerBounds().length > 0;
  }

  @Override
  public List<NewJavaType> lowerBounds() {
    final Type type = this.object();
    if (type instanceof WildcardType w) {
      final Type[] lowerBounds = w.getLowerBounds();
      if (lowerBounds.length > 0) {
        final List<NewJavaType> lowerBoundsList = new ArrayList<>(lowerBounds.length);
        for (final Type lowerBound : lowerBounds) {
          lowerBoundsList.add(of(lowerBound));
        }
        return Collections.unmodifiableList(lowerBoundsList);
      }
    }
    return List.of();
  }

  @Override
  public List<NewJavaType> upperBounds() {
    final Type type = this.object();
    final Type[] upperBounds;
    if (type instanceof TypeVariable<?> t) {
      upperBounds = t.getBounds();
    } else if (type instanceof WildcardType w) {
      upperBounds = w.getUpperBounds();
    } else {
      upperBounds = null;
    }
    if (upperBounds != null && upperBounds.length > 0) {
      final List<NewJavaType> upperBoundsList = new ArrayList<>(upperBounds.length);
      for (final Type upperBound : upperBounds) {
        upperBoundsList.add(of(upperBound));
      }
      return Collections.unmodifiableList(upperBoundsList);
    }
    return List.of();
  }

  

  public static final NewJavaType of(final JavaType.Token<?> type) {
    return of(type.type());
  }

  public static final NewJavaType of(final Type type) {
    return new NewJavaType(type);
  }

  private static final Type type(final Type type) {
    if (type instanceof ParameterizedType p) {
      return type(p.getRawType());
    } else if (type instanceof GenericArrayType g) {
      return type(g.getGenericComponentType());
    } else {
      return type;
    }
  }
  
}

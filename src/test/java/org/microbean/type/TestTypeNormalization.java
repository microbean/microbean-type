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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestTypeNormalization {

  private TestTypeNormalization() {
    super();
  }

  @Test
  final void testIsRawType() {
    assertFalse(Types.isRawType(Integer.class));
    assertTrue(Types.isRawType(List.class));
    assertFalse(Types.isRawType(new TypeLiteral<List<String>>() {}.getType()));
    assertTrue(Types.isRawType(Outer.class)); // see below
    assertTrue(Types.isRawType(Outer.class.getDeclaredClasses()[0]));
    assertTrue(Types.isRawType(Outer.Inner.class));
  }

  @Test
  final void testGenericArray() {
    final GenericArrayType gat = (GenericArrayType)new TypeLiteral<List<String>[]>() {}.getType();
    assertSame(List.class, ((ParameterizedType)gat.getGenericComponentType()).getRawType());
    assertSame(gat, Types.normalize(gat));
    @SuppressWarnings("rawtypes")
    final Class<?> c = (Class<?>)new TypeLiteral<List[]>() {}.getType();
    assertTrue(c.isArray());
  }

  @Test
  final void testTypeNormalization() {
    // A straight up ordinary class should just be returned.
    Type arg = Integer.class;
    Type type = Types.normalize(arg);
    assertSame(arg, type);

    // A ParameterizedType should just be returned.
    arg = new DefaultParameterizedType(Set.class, String.class);
    type = Types.normalize(arg);
    assertSame(arg, type);

    // A raw type should be turned into a parameterized type.
    arg = List.class;
    type = Types.normalize(arg);
    assertNotSame(arg, type);
    assertTrue(type instanceof ParameterizedType);
    final ParameterizedType parameterizedType = (ParameterizedType)type;
    assertEquals(List.class, parameterizedType.getRawType());
    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
    assertNotNull(actualTypeArguments);
    assertEquals(1, actualTypeArguments.length);
    Type firstActualTypeArgument = actualTypeArguments[0];
    assertTrue(firstActualTypeArgument instanceof TypeVariable);
    TypeVariable<?> typeVariable = (TypeVariable<?>)firstActualTypeArgument;
    Type[] bounds = typeVariable.getBounds();
    assertNotNull(bounds);
    assertEquals(1, bounds.length);
    assertEquals(Object.class, bounds[0]);

    // An array type whose component type is a raw type should be
    // turned into a GenericArrayType.
    arg = List[].class;
    type = Types.normalize(arg);
    assertNotSame(arg, type);
    assertTrue(type instanceof GenericArrayType);
    final GenericArrayType genericArrayType = (GenericArrayType)type;
    final Type genericComponentType = genericArrayType.getGenericComponentType();
    assertTrue(genericComponentType instanceof ParameterizedType);
    final ParameterizedType genericComponentParameterizedType = (ParameterizedType)genericComponentType;
    assertEquals(List.class, genericComponentParameterizedType.getRawType());
    actualTypeArguments = genericComponentParameterizedType.getActualTypeArguments();
    assertNotNull(actualTypeArguments);
    assertEquals(1, actualTypeArguments.length);
    firstActualTypeArgument = actualTypeArguments[0];
    assertTrue(firstActualTypeArgument instanceof TypeVariable);
    typeVariable = (TypeVariable<?>)firstActualTypeArgument;
    bounds = typeVariable.getBounds();
    assertNotNull(bounds);
    assertEquals(1, bounds.length);
    assertEquals(Object.class, bounds[0]);

  }

  private static final class Outer<T> {

    private T t;

    private Outer() {
      super();
    }

    final class Inner {

      final T setOuterT(final T t1) {
        t = t1;
        return t;
      }

    }
  }

}

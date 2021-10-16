/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2021 microBean™.
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

import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Test;

import org.microbean.type.Types.FreshTypeVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TestSubstitute {

  private TestSubstitute() {
    super();
  }

  @Test
  final void testSubstituteNullTo() {
    // You can't specify the null type as a valid substitution.
    assertThrows(NullPointerException.class,
                 () -> Types.substitute(new TypeLiteral<List<CharSequence>>() {}.getType(), Number.class, null));
  }

  @Test
  final void testSubstituteNullIn() {
    final Type result = Types.substitute(null, Number.class, String.class);
    // The null type equals no other non-null type so the substitution
    // never happens.
    assertNull(result);
  }

  @Test
  final void testSubstituteNullInNullFrom() {
    final Type result = Types.substitute(null, null, String.class);
    // null == null so the substitution of String.class is applied and
    // returned
    assertSame(String.class, result);
  }

  @Test
  final void testSubstituteAllNulls() {
    // to is null so this throws a NullPointerException.
    assertThrows(NullPointerException.class,
                 () -> Types.substitute(null, null, null));
  }

  @Test
  final void testSubstituteInEqualsFrom() {
    final Type result = Types.substitute(Object.class, Object.class, String.class);
    assertSame(String.class, result);
  }

  @Test
  final void testSubstituteAllSame() {
    final Type result = Types.substitute(Object.class, Object.class, Object.class);
    assertSame(Object.class, result);
  }

  @Test
  final void testSubstituteFromEqualsTo() {
    final Type result = Types.substitute(Object.class, String.class, String.class);
    assertSame(Object.class, result);
  }

  @Test
  final void testSubstituteTypeArgument() {
    final DefaultParameterizedType result =
      (DefaultParameterizedType)Types.substitute(new TypeLiteral<List<CharSequence>>() {}.getType(),
                                                 CharSequence.class,
                                                 String.class);
    assertNotNull(result);
    assertSame(List.class, result.getRawType());
    assertSame(String.class, result.getActualTypeArguments()[0]);
  }

  @Test
  final void testOwnerTypeAndRawTypeAreNeverSubstituted() {
    final ParameterizedType in = (ParameterizedType)new TypeLiteral<List<CharSequence>>() {}.getType();
    assertNull(in.getOwnerType());
    final ParameterizedType result = (ParameterizedType)Types.substitute(in, List.class, Class.class);
    assertSame(in, result);
  }

  @Test
  final void testSubstituteInIsolation() throws ReflectiveOperationException {
    // This test just tests the raw mechanics of the following
    // indiscriminate Types method:
    //
    // static final Type substitute(final Type in,
    //                              final Type from,
    //                              final Type to)
    //
    // It can be used to make absolutely nonsensical substitutions and
    // is designed for internal use only.
    final TypeVariable<?>[] typeParameters = C.class.getTypeParameters();
    assertEquals(2, typeParameters.length);
    final TypeVariable<?> V = typeParameters[0];
    assertEquals("V", V.getName());
    assertSame(Object.class, V.getBounds()[0]);
    final TypeVariable<?> W = typeParameters[1];
    assertEquals("W", W.getName());
    assertSame(List.class, ((ParameterizedType)W.getBounds()[0]).getRawType());
    assertSame(V, ((ParameterizedType)W.getBounds()[0]).getActualTypeArguments()[0]);

    // The owner type and the raw type don't actually matter here.
    // Remember, we're testing substitution in isolation, not the
    // capture conversion algorithm.
    final ParameterizedType p =
      new DefaultParameterizedType(C.class.getDeclaringClass(),
                                   C.class,
                                   Number.class,
                                   UnboundedWildcardType.INSTANCE);
    final FreshTypeVariable s2 = new FreshTypeVariable(W.getBounds()[0]);
    assertEquals(List.class, ((ParameterizedType)s2.getUpperBound()).getRawType());
    assertSame(V, ((ParameterizedType)s2.getUpperBound()).getActualTypeArguments()[0]);
    final Type result = Types.substitute(p, UnboundedWildcardType.INSTANCE, s2);
    assertSame(s2, ((ParameterizedType)result).getActualTypeArguments()[1]);
  }

  // https://stackoverflow.com/questions/46050311/second-order-generics-seem-to-behave-differently-than-first-order-generics/46061947#46061947
  private static final class C<V, W extends List<V>> {

    private C() {
      super();
    }

  }

}

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
import java.lang.reflect.WildcardType;

import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestSubstitute {

  private TestSubstitute() {
    super();
  }

  @Test
  final void testSubstitute() {
    // https://stackoverflow.com/questions/46050311/second-order-generics-seem-to-behave-differently-than-first-order-generics/46061947#46061947

    // Let G name a generic type declaration...
    final Class<?> G = C.class;
    
    // ...with n type parameters A1...An...
    final TypeVariable<?>[] A = (TypeVariable<?>[])G.getTypeParameters();
    assertNotNull(A);
    assertEquals(2, A.length);
    
    final TypeVariable<?> A1 = A[0];
    assertEquals("V", A1.getName());

    final TypeVariable<?> A2 = A[1];
    assertEquals("W", A2.getName());

    // ...with corresponding bounds U1...Un.
    final Class<?> U1 = (Class<?>)A1.getBounds()[0];
    assertSame(Object.class, U1);

    final ParameterizedType U2 = (ParameterizedType)A2.getBounds()[0];
    assertEquals(List.class, U2.getRawType());
    assertSame(A1 /* "V" */, U2.getActualTypeArguments()[0]);

    // There exists a capture conversion from a parameterized type G<T1,...,Tn>...
    final ParameterizedType GT = (ParameterizedType)new TypeLiteral<C<Number, ?>>() {}.getType();
    assertSame(G, GT.getRawType());

    final Type[] T = GT.getActualTypeArguments();
    assertNotNull(T);
    assertEquals(2, T.length);

    final Class<?> T1 = (Class<?>)T[0];
    assertSame(Number.class, T1);

    final WildcardType T2 = (WildcardType)T[1];
    assertNotNull(T2);
    assertEquals(0, T2.getLowerBounds().length);
    assertSame(Object.class, T2.getUpperBounds()[0]);
    
    // (Test substitution here.  Don't read into whether we're doing
    // capture conversion correctly or not.  We're just testing the
    // substitution on one thing and it's arbitrary.)
    final ParameterizedType result = (ParameterizedType)Types.substitute(GT /* in */, T2 /* from */, U2 /* to */);
    assertNotNull(result);
    assertSame(G, result.getRawType());
    final Type[] S = result.getActualTypeArguments();
    assertNotNull(S);
    assertEquals(2, S.length);
    final Class<?> S1 = (Class<?>)S[0];
    assertSame(T1, S1);
    final ParameterizedType S2 = (ParameterizedType)S[1];
    assertNotNull(S2);
    assertEquals(List.class, S2.getRawType());

    // Remember, for true capture conversion this isn't right, but for substitution, it is.
    assertSame(A1 /* "V" */, S2.getActualTypeArguments()[0]);
    
  }

  @Test
  final void testSubstituteInIsolation() {
    final ParameterizedType A = (DefaultParameterizedType)Types.normalize(A.class);
    final Type[] args = A.getActualTypeArguments();
    assertEquals(3, args.length);
    final TypeVariable<?> B = (TypeVariable<?>)args[0];
    assertEquals("B", B.getName());
    assertSame(A.class, B.getGenericDeclaration());
    final TypeVariable<?> C = (TypeVariable<?>)args[1];
    assertNotNull(C);
    assertEquals("C", C.getName());
    final Type[] cBounds = C.getBounds();
    assertEquals(1, cBounds.length);
    final ParameterizedType cBound0 = (ParameterizedType)cBounds[0];
    assertSame(List.class, cBound0.getRawType());
    final Type[] cBound0Args = cBound0.getActualTypeArguments();
    assertEquals(1, cBound0Args.length);
    assertSame(B, cBound0Args[0]);

    final Type result = Types.substitute(A, B, Number.class);
    System.out.println(Types.toString(result));
  }

  // https://stackoverflow.com/questions/46050311/second-order-generics-seem-to-behave-differently-than-first-order-generics/46061947#46061947
  private static final class C<V, W extends List<V>> {

    private C() {
      super();
    }
    
  }

  private static class A<B, C extends List<B>, D extends List<List<C>>> {

    private A() {
    }
    
  }
  
}

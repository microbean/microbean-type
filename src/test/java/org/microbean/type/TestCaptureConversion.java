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

import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Test;

import org.microbean.type.Types.FreshTypeVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

final class TestCaptureConversion {

  private TestCaptureConversion() {
    super();
  }

  @Test
  final void testCaptureConversion() {
    // https://stackoverflow.com/questions/46050311/second-order-generics-seem-to-behave-differently-than-first-order-generics/46061947#46061947
    final ParameterizedType GT = (ParameterizedType)new TypeLiteral<C<Number, ?>>() {}.getType();
    final ParameterizedType GS = Types.applyCaptureConversion(GT);
    assertNotNull(GS);
    assertNotSame(GT, GS);
    assertSame(GT.getOwnerType(), GS.getOwnerType());
    assertSame(GT.getRawType(), GS.getRawType());
    final Type[] S = GS.getActualTypeArguments();
    assertNotNull(S);
    assertEquals(2, S.length);
    assertSame(Number.class, S[0]);
    final FreshTypeVariable S1 = (FreshTypeVariable)S[1];
    assertNull(S1.getLowerBound());
    final ParameterizedType S1UB = (ParameterizedType)S1.getUpperBound();
    assertNull(S1UB.getOwnerType());
    assertSame(List.class, S1UB.getRawType());
    final Object[] S1UBA = S1UB.getActualTypeArguments();
    assertEquals(1, S1UBA.length);
    assertSame(Number.class, S1UBA[0]);
  }

  // https://stackoverflow.com/questions/46050311/second-order-generics-seem-to-behave-differently-than-first-order-generics/46061947#46061947
  private static final class C<V, W extends List<V>> {

    private C() {
      super();
    }

  }

}

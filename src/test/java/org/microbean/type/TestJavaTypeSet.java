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

import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestJavaTypeSet {

  private TestJavaTypeSet() {
    super();
  }

  @Test
  final void testJavaTypeSetOfIntegerHasIntegerAsItsMostSpecializedNonInterfaceType() {
    final NewJavaTypeSet jts = NewJavaTypeSet.of(Integer.class);
    assertEquals(1, jts.size());
    assertSame(Integer.class, jts.mostSpecializedNonInterfaceType());
    assertNull(jts.mostSpecializedInterfaceType());
  }

  @Test
  final void testJavaTypeSetOfIntegerAndNumberHasIntegerAsItsMostSpecializedNonInterfaceType() {
    final NewJavaTypeSet jts = NewJavaTypeSet.of(Number.class, Integer.class);
    assertEquals(2, jts.size());
    assertSame(Integer.class, jts.mostSpecializedNonInterfaceType());
    assertNull(jts.mostSpecializedInterfaceType());
  }

  @Test
  final void testJavaTypeSetOfIntegerAndComparableHasIntegerAsItsMostSpecializedNonInterfaceType() {
    final NewJavaTypeSet jts = NewJavaTypeSet.of(Comparable.class, Integer.class);
    assertEquals(2, jts.size());
    assertSame(Integer.class, jts.mostSpecializedNonInterfaceType());
    assertSame(Comparable.class, jts.mostSpecializedInterfaceType());
  }

  @Test
  final void testJavaTypeSetOfIntegerAndStringHasEitherStringOrIntegerAsItsMostSpecializedNonInterfaceType() {
    final NewJavaTypeSet jts = NewJavaTypeSet.of(Integer.class, String.class);
    assertEquals(2, jts.size());
    final java.lang.reflect.Type mssc = jts.mostSpecializedNonInterfaceType();
    assertTrue(mssc == String.class || mssc == Integer.class);
    assertNull(jts.mostSpecializedInterfaceType());
  }

  @Test
  final void testOrdering() {
    final NewJavaTypeSet jts = NewJavaTypeSet.of(List.of(Integer.class, String.class, Number.class));
    for (int i = 0; i < 10; i++) {
      final Iterator<java.lang.reflect.Type> iterator = jts.iterator();
      for (int j = 0; j < jts.size(); j++) {
        switch (j) {
        case 0:
          assertSame(Integer.class, iterator.next());
          break;
        case 1:
          assertSame(String.class, iterator.next());
          break;
        case 2:
          assertSame(Number.class, iterator.next());
          break;
        default:
          throw new AssertionError();
        }
      }
    }
  }


}

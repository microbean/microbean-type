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
  final void testJavaTypeSetOfIntegerHasIntegerAsItsMostSpecializedClass() {
    final JavaTypeSet jts = JavaTypeSet.of(Integer.class);
    assertEquals(1, jts.size());
    assertSame(Integer.class, jts.mostSpecializedClass());
    assertNull(jts.mostSpecializedInterface());
  }

  @Test
  final void testJavaTypeSetOfIntegerAndNumberHasIntegerAsItsMostSpecializedClass() {
    final JavaTypeSet jts = JavaTypeSet.of(Number.class, Integer.class);
    assertEquals(2, jts.size());
    assertSame(Integer.class, jts.mostSpecializedClass());
    assertNull(jts.mostSpecializedInterface());
  }

  @Test
  final void testJavaTypeSetOfIntegerAndComparableHasIntegerAsItsMostSpecializedClass() {
    final JavaTypeSet jts = JavaTypeSet.of(Comparable.class, Integer.class);
    assertEquals(2, jts.size());
    assertSame(Integer.class, jts.mostSpecializedClass());
    assertSame(Comparable.class, jts.mostSpecializedInterface());
  }

  @Test
  final void testJavaTypeSetOfIntegerAndStringHasEitherStringOrIntegerAsItsMostSpecializedClass() {
    final JavaTypeSet jts = JavaTypeSet.of(Integer.class, String.class);
    assertEquals(2, jts.size());
    final java.lang.reflect.Type mssc = jts.mostSpecializedClass();
    assertTrue(mssc == String.class || mssc == Integer.class);
    assertNull(jts.mostSpecializedInterface());
  }


}

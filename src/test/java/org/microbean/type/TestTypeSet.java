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

import java.io.Closeable;
import java.io.Serializable;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestTypeSet {

  private TestTypeSet() {
    super();
  }

  @Test
  final void testEmptyTypeSetContainsWorks() {
    final TypeSet typeSet = new TypeSet();
    assertFalse(typeSet.contains(Object.class));
  }

  @Test
  final void testSetEquality() {
    final TypeSet typeSet1 = Types.toTypes(StringBuilder.class);
    final TypeSet typeSet2 = Types.toTypes(StringBuilder.class);
    assertEquals(typeSet1, typeSet2);
    assertEquals(typeSet1.hashCode(), typeSet2.hashCode());    
  }

  @Test
  final void testGetInterfaces() {
    final TypeSet typeSet = Types.toTypes(StringBuilder.class);
    final Set<Class<?>> interfaces = typeSet.getInterfaces();
    assertNotNull(interfaces);
    assertEquals(4, interfaces.size());
    assertTrue(interfaces.contains(CharSequence.class));
    assertTrue(interfaces.contains(Comparable.class));
    assertTrue(interfaces.contains(Serializable.class));
    assertTrue(interfaces.contains(Appendable.class));
    assertSame(interfaces, typeSet.getInterfaces());
    assertThrows(UnsupportedOperationException.class, () -> interfaces.clear());
  }

  @Test
  final void testGetMostSpecializedInterface() {
    final TypeSet typeSet = Types.toTypes(new Closeable() { @Override public void close() {} }.getClass());
    assertEquals(Closeable.class, typeSet.getMostSpecializedInterface());
  }

  @Test
  final void testGetClasses() {
    final TypeSet typeSet = Types.toTypes(Integer.class);
    final Set<Class<?>> classes = typeSet.getClasses();
    assertNotNull(classes);
    assertEquals(3, classes.size());
    assertTrue(classes.contains(Object.class));
    assertTrue(classes.contains(Number.class));
    assertTrue(classes.contains(Integer.class));
    assertSame(classes, typeSet.getClasses());
    assertThrows(UnsupportedOperationException.class, () -> classes.clear());
  }

  @Test
  final void testGetMostSpecializedClass() {
    final TypeSet typeSet = Types.toTypes(Integer.class);
    assertEquals(Integer.class, typeSet.getMostSpecializedClass());
  }
  
}

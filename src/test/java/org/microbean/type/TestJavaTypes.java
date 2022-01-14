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

import java.io.Serializable;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestJavaTypes {

  private TestJavaTypes() {
    super();
  }

  @Test
  final void testClassToString() {
    assertEquals("java.lang.Object", JavaTypes.toString(Object.class));
    class Foo {};
    // Huh.
    assertEquals("org.microbean.type.TestJavaTypes$1Foo", JavaTypes.toString(Foo.class));
    assertEquals("java.lang.Object[]", JavaTypes.toString(Object[].class));
    assertEquals("java.lang.Object[][][]", JavaTypes.toString(Object[][][].class));
    assertEquals("int", JavaTypes.toString(int.class));
  }

  @Test
  final void testEmptyTypeArray() {
    final java.lang.reflect.Type[] t = JavaTypes.emptyTypeArray();
    assertEquals(0, t.length);
  }
  
  @Test
  final void testParameterizedTypeToString() {
    assertEquals("java.lang.ClassValue<X>", JavaTypes.toString(DummyClassValue.class.getGenericSuperclass()));
  }

  @Test
  final void testDirectSupertypesOfInteger() {
    final Collection<Type> directSupertypes = JavaTypes.directSupertypes(Integer.class);
    assertEquals(4, directSupertypes.size());
    final Iterator<Type> iterator = directSupertypes.iterator();
    assertTrue(iterator.hasNext());
    for (int i = 0; i < directSupertypes.size(); i++) {
      switch (i) {
      case 0:
        assertSame(Number.class, iterator.next());
        break;
      case 1:
        assertEquals(new DefaultParameterizedType(null, Comparable.class, Integer.class), iterator.next());
        break;
      case 2:
        assertSame(Constable.class, iterator.next());
        break;
      case 3:
        assertSame(ConstantDesc.class, iterator.next());
        break;
      default:
        throw new AssertionError("i: " + i + "; " + iterator.next());
      }
    }
    assertFalse(iterator.hasNext());
  }

  @Test
  final void testDirectSupertypesOfInt() {
    final Collection<Type> directSupertypes = JavaTypes.directSupertypes(int.class);
    assertTrue(directSupertypes.isEmpty());
  }

  @Test
  final void testDirectSupertypesOfIntArray() {
    final Collection<Type> directSupertypes = JavaTypes.directSupertypes(int[].class);
    assertEquals(3, directSupertypes.size());
    final Iterator<Type> iterator = directSupertypes.iterator();
    assertTrue(iterator.hasNext());
    for (int i = 0; i < directSupertypes.size(); i++) {
      switch (i) {
      case 0:
        assertSame(Object.class, iterator.next());
        break;
      case 1:
        assertSame(Cloneable.class, iterator.next());
        break;
      case 2:
        assertSame(Serializable.class, iterator.next());
        break;
      default:
        throw new AssertionError("i: " + i + "; " + iterator.next());
      }
    }
    assertFalse(iterator.hasNext());
  }

  @Test
  final void testDirectSupertypesOfIntegerArray() {
    final Collection<Type> directSupertypes = JavaTypes.directSupertypes(Integer[].class);
    assertEquals(5, directSupertypes.size(), directSupertypes.toString());
    final Iterator<Type> iterator = directSupertypes.iterator();
    assertTrue(iterator.hasNext());
    for (int i = 0; i < directSupertypes.size(); i++) {
      switch (i) {
      case 0:
        assertSame(Number[].class, iterator.next());
        break;
      case 1:
        assertEquals(new DefaultGenericArrayType(new DefaultParameterizedType(null, Comparable.class, Integer.class)), iterator.next());
        break;
      case 2:
        assertSame(Comparable[].class, iterator.next());
        break;
      case 3:
        assertSame(Constable[].class, iterator.next());
        break;
      case 4:
        assertSame(ConstantDesc[].class, iterator.next());
        break;
      default:
        throw new AssertionError("i: " + i + "; " + iterator.next());
      }
    }
    assertFalse(iterator.hasNext());
  }

  @Test
  final void testDirectSupertypesOfComparable() {
    final Collection<Type> directSupertypes = JavaTypes.directSupertypes(Comparable.class); // (generic class)
    assertEquals(1, directSupertypes.size());
    assertSame(Object.class, directSupertypes.iterator().next());
  }

  @Test
  final void testSupertypesOfInteger() {
    System.out.println(JavaTypes.supertypes(Integer.class));
  }

  @Test
  final void testNumberIsSupertypeOfInteger() {
    assertTrue(JavaTypes.supertype(Number.class, Integer.class));
    assertFalse(JavaTypes.supertype(Integer.class, Number.class));
  }

  @Test
  final void testComparableStringIsNotSupertypeOfInteger() {
    final ParameterizedType p = new DefaultParameterizedType(null, Comparable.class, String.class);
    assertFalse(JavaTypes.supertype(p, Integer.class));
  }

  // @Test
  final void testCompilation() {
    final Object m = new HashMap<String, String>();
    final ParameterizedType p = (ParameterizedType)m.getClass().getGenericSuperclass();
    final Type[] tas = p.getActualTypeArguments();
    assertEquals(2, tas.length);
    assertEquals(String.class, tas[0]); // will fail
    
  }
  
  private static final Comparable<? super Number> comparableSuperNumber() {
    return x -> 0;
  }
  
  private static final class DummyClassValue<X> extends ClassValue<X> {

    @Override
    protected final X computeValue(final Class<?> c) {
      return null;
    }
    
  }
}

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

import java.io.Serializable;

import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.Collection;

import jakarta.enterprise.util.TypeLiteral;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestGetDirectSupertypes {

  private TestGetDirectSupertypes() {
    super();
  }

  @Test
  final void testClassDirectSupertypes() {
    // Object (limiting case)
    Type[] ds = Types.getDirectSupertypes(Object.class);
    assertEquals(0, ds.length);

    // Primitive scalar (another limiting case)
    ds = Types.getDirectSupertypes(int.class);
    assertEquals(1, ds.length);
    assertSame(long.class, ds[0]);
    // TODO: all the others
    
    // Primitive array
    ds = Types.getDirectSupertypes(int[].class);
    assertEquals(3, ds.length);
    Collection<Type> dsc = Arrays.asList(ds);
    assertTrue(dsc.contains(Serializable.class));
    assertTrue(dsc.contains(Cloneable.class));
    assertTrue(dsc.contains(Object.class));

    // Object[]
    ds = Types.getDirectSupertypes(Object[].class);
    assertEquals(3, ds.length);
    dsc = Arrays.asList(ds);
    assertTrue(dsc.contains(Cloneable.class));
    assertTrue(dsc.contains(Object.class));
    assertTrue(dsc.contains(Serializable.class));

    // String[] (non-generic type array)
    ds = Types.getDirectSupertypes(String[].class);
    dsc = Arrays.asList(ds);
    assertEquals(4, ds.length, "" + dsc);

    // First is the direct superclass…
    assertSame(Object[].class, ds[0]);

    // …then the direct interfaces in no partiuclar order.
    assertTrue(dsc.contains(CharSequence[].class));
    assertTrue(dsc.contains(Serializable[].class));
    assertTrue(dsc.contains(new TypeLiteral<Comparable<String>[]>() {}.getType()));

    // See the interesting footnote to
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-10.html#jls-10.1
    // which says:
    //
    // "The supertype relation for array types is not the same as the
    // superclass relation. The direct supertype of Integer[] is
    // Number[] according to §4.10.3, but the direct superclass of
    // Integer[] is Object according to the Class object for Integer[]
    // (§10.8). This does not matter in practice, because Object is
    // also a supertype of all array types."
    //
    // (As it happens, the direct supertypes of Integer[] include
    // Number[] and Comparable<Integer>[].)
    ds = Types.getDirectSupertypes(Integer[].class);
    dsc = Arrays.asList(ds);
    assertEquals(2, ds.length, "" + dsc);
    assertSame(Number[].class, ds[0]);
    assertEquals(new TypeLiteral<Comparable<Integer>[]>() {}.getType(), ds[1]);
    
    // Comparable[] (Comparable is a raw type)
    ds = Types.getDirectSupertypes(Comparable[].class);
    assertEquals(1, ds.length);
    assertSame(Object[].class, ds[0]);
  }
  
}

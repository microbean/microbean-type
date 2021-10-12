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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
  final void testCompilationThings() {
    // Does not work, obviously.
    // final ArrayList<Object> l = new ArrayList<String>();

    // Obvious.
    final ArrayList<String> l0 = new ArrayList<String>();

    final ArrayList<?> l1 = l0;
    final ArrayList<? extends String> l2 = l0;
    final ArrayList<? super String> l3 = l0;
    // final ArrayList<CharSequence> l4 = l0; // nope
    // final ArrayList<? extends Integer> = l0; // nope
    final ArrayList<? extends CharSequence> l4 = l0;
    // final ArrayList<? super CharSequence> l5 = l0; // nope
    

  }

  private static abstract class Goober implements Collection<List<String>[]> {

  }

  private static final Type[] generateContainingTypes(final Type type) {
    Objects.requireNonNull(type);
    if (type == Object.class) {
      return new Type[] { UnboundedWildcardType.INSTANCE, new LowerBoundedWildcardType(Object.class) };
    } else {
      final Collection<Type> types = new ArrayList<>();
      generateContainingTypes(type, types);
      types.add(UnboundedWildcardType.INSTANCE);
      return types.toArray(new Type[types.size()]);
    }
  }

  private static final void generateContainingTypes(final Type type, final Collection<Type> types) {
    Objects.requireNonNull(type);
    // T <= T
    types.add(type);
    if (type instanceof WildcardType) {
      final WildcardType wct = (WildcardType)type;
      final Type[] lowerBounds = wct.getLowerBounds();
      if (lowerBounds.length <= 0) {
        final Type[] upperBounds = wct.getUpperBounds();
        assert upperBounds.length == 1;
        final Type upperBound = upperBounds[0];
        if (upperBound != Object.class) {
          for (final Type s : Types.getDirectSupertypes(upperBound)) {
            assert s != null;
            assert !Types.equals(upperBound, s);
            if (s != Object.class) {
              generateContainingTypes(new UpperBoundedWildcardType(s), types);
            }
          }
        }
      }
    } else {
      generateContainingTypes(new UpperBoundedWildcardType(type), types);
      types.add(new LowerBoundedWildcardType(type));
    }
  }

  @Test
  final void testGetContainingTypeArguments() {
    final Type[] containingTypes = Types.getContainingTypeArguments(String.class);
    System.out.println("Containing types:");
    for (final Type t : containingTypes) {
      System.out.println("  " + Types.toString(t));
    }
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

    // …then the direct superinterfaces in no partiuclar order.
    assertTrue(dsc.contains(CharSequence[].class));
    assertTrue(dsc.contains(Serializable[].class));
    // assertTrue(dsc.contains(new TypeLiteral<Comparable<String>[]>() {}.getType()), "" + dsc);
    // Hmm; maybe this instead?
    assertTrue(dsc.contains(Comparable[].class));

    // See the interesting footnote to
    // https://docs.oracle.com/javase/specs/jls/se11/html/jls-10.html#jls-10.1
    // which says:
    //
    // "The supertype relation for array types is not the same as the
    // superclass relation. The direct supertype of Integer[] is
    // Number[] according to §4.10.3, but the direct superclass of
    // Integer[] is Object according to the Class object for Integer[]
    // (§10.8). This does not matter in practice, because Object is
    // also a [non-direct] supertype of all array types."
    //
    // (As it happens, the direct supertypes of Integer[] include
    // Number[] and Comparable<Integer>[].)
    ds = Types.getDirectSupertypes(Integer[].class);
    assertEquals(2, ds.length);
    assertSame(Number[].class, ds[0]); // we always add the direct supertype first
    // assertEquals(new TypeLiteral<Comparable<Integer>[]>() {}.getType(), ds[1]); // then the direct superinterfaces
    // Hmm; maybe this instead?
    assertSame(Comparable[].class, ds[1]);
    
    // Comparable[] (Comparable is a raw type)
    ds = Types.getDirectSupertypes(Comparable[].class);
    assertEquals(1, ds.length);
    assertSame(Object[].class, ds[0]);
  }

  @SuppressWarnings("rawtypes")
  @Test
  final void testGenericArrayTypeDirectSupertypes() {
    final GenericArrayType type = (GenericArrayType)new TypeLiteral<List<String>[]>() {}.getType();
    assertEquals(new TypeLiteral<List<String>>() {}.getType(), type.getGenericComponentType());
    final Type[] dss = Types.getDirectSupertypes(type);
    Collection<Type> dsc = Arrays.asList(dss);
    assertTrue(dss.length > 2, "" + dsc); // TODO: this will change when we add wildcard handling
    assertTrue(dsc.contains(List[].class), "" + dsc);
    assertTrue(dsc.contains(new TypeLiteral<Collection<String>[]>() {}.getType()), "" + dsc);
  }

  @Test
  final void testParameterizedTypeDirectSupertypes() {
    final Type[] dss = Types.getDirectSupertypes(new TypeLiteral<List<String>>() {}.getType());
    Collection<Type> dsc = Arrays.asList(dss);
    assertTrue(dss.length > 2, "" + dsc); // TODO: this will change when we add wildcard handling
    assertTrue(dsc.contains(List.class), "" + dsc);
    assertTrue(dsc.contains(new TypeLiteral<Collection<String>>() {}.getType()), "" + dsc);
  }
}

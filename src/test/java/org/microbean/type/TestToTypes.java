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

import java.io.Serializable;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.ArrayList;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import java.util.function.Predicate;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.util.reflection.HierarchyDiscovery;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.microbean.development.annotation.Issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestToTypes {

  private TestToTypes() {
    super();
  }

  @Test
  final void testToTypes() throws ReflectiveOperationException {
    Set<Type> allTypes = Types.toTypes(ArrayListInteger.class);
    assertNotNull(allTypes);
    assertEquals(11, allTypes.size(), allTypes.toString());
    assertTrue(allTypes.contains(Object.class));
    assertTrue(allTypes.contains(Cloneable.class));
    assertTrue(allTypes.contains(Serializable.class));
    assertTrue(allTypes.contains(RandomAccess.class));
    assertTrue(allTypes.contains(ArrayListInteger.class));
    assertTrue(allTypes.contains(new DefaultParameterizedType(null, Collection.class, Integer.class)));
    assertTrue(allTypes.contains(new DefaultParameterizedType(null, Iterable.class, Integer.class)));
    assertTrue(allTypes.contains(new DefaultParameterizedType(null, List.class, Integer.class)));
    assertTrue(allTypes.contains(new DefaultParameterizedType(null, AbstractCollection.class, Integer.class)));
    assertTrue(allTypes.contains(new DefaultParameterizedType(null, AbstractList.class, Integer.class)));
    assertTrue(allTypes.contains(new DefaultParameterizedType(null, ArrayList.class, Integer.class)));

    allTypes = Types.toTypes(int.class);
    assertNotNull(allTypes);
    assertEquals(1, allTypes.size());
    assertTrue(allTypes.contains(int.class));
  }

  private static final void interestingCases() {
    // Note: none of these is a compilation error.
    final ArrayList<Long>[] arrayListLongArray = blork();
    final Object object = arrayListLongArray;
    final Cloneable cloneable = arrayListLongArray;
    final Serializable serializable = arrayListLongArray;
    final Object[] objectArray = arrayListLongArray;
    final Cloneable[] cloneableArray = arrayListLongArray;
    final Serializable[] serializableArray = arrayListLongArray;
    final RandomAccess[] randomAccessArray = arrayListLongArray;
    final Collection<Long>[] collectionLongArray = arrayListLongArray;
    final Iterable<Long>[] iterableLongArray = arrayListLongArray;
    final List<Long>[] listLongArray = arrayListLongArray;
    final AbstractCollection<Long>[] abstractCollectionLongArray = arrayListLongArray;
    final AbstractList<Long>[] abstractListLongArray = arrayListLongArray;
  }


  @Test
  final void testWackyArrayClassBehaviors() {
    Class<?> c = Long[].class;
    // Interestingly, no Number[].class:
    assertEquals(Object.class, c.getSuperclass());
    assertEquals(Object.class, c.getGenericSuperclass());
    Class<?>[] interfaces = c.getInterfaces();
    assertNotNull(interfaces);
    assertEquals(2, interfaces.length);
    assertEquals(Cloneable.class, interfaces[0]);
    assertEquals(Serializable.class, interfaces[1]);
    // Because the JVM is magic (note that Number[].class does not
    // appear in the type hierarchy)?:
    assertTrue(Number[].class.isAssignableFrom(Long[].class));
    final Long[] longObjects = new Long[] { Long.valueOf(1L) };
    // Note: no compilation errors:
    Number[] numbers = longObjects;
    assertTrue(longObjects instanceof Number[]);

    // Now let's try some primitives:
    c = int[].class;
    assertEquals(Object.class, c.getSuperclass());
    assertEquals(Object.class, c.getGenericSuperclass());
    interfaces = c.getInterfaces();
    assertNotNull(interfaces);
    assertEquals(2, interfaces.length);
    assertEquals(Cloneable.class, interfaces[0]);
    assertEquals(Serializable.class, interfaces[1]);
    final int[] ints = new int[] { 1 };
    // Compilation errors as expected (maybe :-)):
    // final long[] longs = ints;
    // numbers = ints
  }

  @Test
  final void testRawClass() {
    final Type type = Predicate.class;
    final Set<Type> weldTypes = new HierarchyDiscovery(type).getTypeClosure();
    final TypeSet typeSet = Types.toTypes(type);
    assertNotNull(typeSet);
    assertEquals(weldTypes, typeSet);
    assertTrue(typeSet.contains(type));
  }

  @Test
  final void testStraightforwardGenericArrayType() {
    final Type type = new TypeLiteral<List<Integer>[]>() {}.getType();
    assertTrue(type instanceof GenericArrayType);
    final Set<Type> weldTypes = new HierarchyDiscovery(type).getTypeClosure();
    final TypeSet typeSet = Types.toTypes(type);
    assertNotNull(typeSet);
    assertEquals(weldTypes, typeSet);
    assertTrue(typeSet.contains(type));
  }

  @Test
  final void testRawGenericArrayType() {
    @SuppressWarnings("rawtypes")
    final Type type = new TypeLiteral<List[]>() {}.getType();
    assertFalse(type instanceof GenericArrayType);
    assertTrue(type instanceof Class);
    assertTrue(((Class<?>)type).getComponentType() instanceof Class);
    final Set<Type> weldTypes = new HierarchyDiscovery(type).getTypeClosure();
    final TypeSet typeSet = Types.toTypes(type);
    assertNotNull(typeSet);
    assertEquals(weldTypes, typeSet);
    assertTrue(typeSet.contains(type));
  }

  @Test
  final void testToTypesNull() {
    final TypeSet typeSet = Types.toTypes(null);
    assertNotNull(typeSet);
    assertTrue(typeSet.isEmpty());
  }

  @Test
  final <T> void testTypeVariableGenericArrayType() {
    final Type type = new TypeLiteral<T[]>() {}.getType();
    assertTrue(type instanceof GenericArrayType);
    final Set<Type> weldTypes = new HierarchyDiscovery(type).getTypeClosure();
    final TypeSet typeSet = Types.toTypes(type);
    assertNotNull(typeSet);
    assertEquals(weldTypes, typeSet);
    assertTrue(typeSet.contains(type));
  }

  @Test
  final void testBaz() {
    Set<Type> allTypes = Types.toTypes(Baz.class);
    assertNotNull(allTypes);
    assertEquals(4, allTypes.size(), allTypes.toString());
    assertTrue(allTypes.contains(Object.class));
    assertTrue(allTypes.contains(Baz.class));
    assertTrue(allTypes.contains(new DefaultParameterizedType(TestToTypes.class, Bar.class, Integer.class)));
    assertTrue(allTypes.contains(new DefaultParameterizedType(TestToTypes.class, Foo.class, Integer.class)));
  }

  @Test
  @Issue(uri = "https://github.com/microbean/microbean-type/issues/4")
  final void testBar() {
    final Type barType = Types.normalize(Bar.class);
    assertTrue(barType instanceof ParameterizedType);
    final Type[] actualTypeArguments = ((ParameterizedType)barType).getActualTypeArguments();
    assertNotNull(actualTypeArguments);
    assertEquals(1, actualTypeArguments.length);
    assertTrue(actualTypeArguments[0] instanceof TypeVariable);
    final TypeVariable<?> b = (TypeVariable<?>)actualTypeArguments[0];
    
    Set<Type> allTypes = Types.toTypes(barType);
    System.out.println("allTypes: " + allTypes);
  }
  
  @Test
  final void testBravo() {
    Set<Type> allTypes = Types.toTypes(Bravo.class);
    assertNotNull(allTypes);
    assertEquals(3, allTypes.size(), allTypes.toString());
    assertTrue(allTypes.contains(Object.class));
    assertTrue(allTypes.contains(Bravo.class));
    final Class<?> me = TestToTypes.class;
    assertTrue(allTypes.contains(t(me, Alpha.class,
                                   t(me, Alpha.class,
                                     t(me, Alpha.class,
                                       t(null, Map.class,
                                         t(me, Alpha.class,
                                           String.class),
                                         t(null, List.class,
                                           t(null, Set.class,
                                             t(null, Comparable.class,
                                               Serializable.class)
                                             )
                                           )
                                         )
                                       )
                                     )
                                   )
                                 )
               );
  }

  private static final <T extends ArrayList<Long>> T[] blork() {
    return null;
  }

  private static final Type t(final Class<?> ownerType, final Class<?> rawType, final Type... actualTypeArguments) {
    return new DefaultParameterizedType(ownerType, rawType, actualTypeArguments);
  }

  private static final class ArrayListInteger extends ArrayList<Integer> {
    private static final long serialVersionUID = 1L;
  }

  private static class Foo<T> {

  }

  private static class Bar<B> extends Foo<B> {

  }

  private static class Baz extends Bar<Integer> {

  }

  private static class Alpha<T> {

  }

  private static class Bravo extends Alpha<Alpha<Alpha<Map<Alpha<String>, List<Set<Comparable<Serializable>>>>>>> {

  }

}

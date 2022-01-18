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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.concurrent.FutureTask;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.microbean.type.Type.Semantics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TestCovariantTypeSemantics {

  private final Semantics<Type> covariantTypeSemantics = new JavaType.Semantics(true);

  private TestCovariantTypeSemantics() {
    super();
  }

  @Test
  final <T, U extends TypeSet> void testLegalBeanTypeScenario2() {
    final Type receiverType = new JavaType.Token<Function<? super Contextual<?>, ? extends TypeSet>>() {}.type();
    final Type payloadType = new JavaType.Token<Function<Contextual<T>, U>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testParameterizedReceiverTypeRawPayloadType() {
    final Type receiverType = new org.microbean.type.JavaType.Token<Predicate<Contextual<?>>>() {}.type();
    final Type payloadType = Predicate.class;
    assertTrue(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testRawReceiverTypeParameterizedPayloadType() {
    final Type receiverType = Predicate.class;
    final Type payloadType = new JavaType.Token<Predicate<Contextual<?>>>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testLowerBoundedWildcardCase() {
    final Type receiverType = new JavaType.Token<Predicate<? super Contextual<?>>>() {}.type();
    final Type payloadType = new JavaType.Token<Predicate<Contextual<?>>>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testResolvedWildcardCase() {
    final Type receiverType = new JavaType.Token<Predicate<Object>>() {}.type();
    final Type payloadType = new JavaType.Token<Predicate<Contextual<Object>>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final <T extends String> void testStringAssignableFromTypeVariableThatExtendsString() {
    final Type receiverType = String.class;
    final Type payloadType = new JavaType.Token<T>() {}.type();
    assertTrue(payloadType instanceof TypeVariable);
    assertEquals(String.class, ((TypeVariable<?>)payloadType).getBounds()[0]);
  }

  @Test
  final void testArrayImpliedInterfaces() {
    // See https://issues.redhat.com/browse/WELD-2614
    assertTrue(Cloneable.class.isAssignableFrom(Integer[].class));
    assertTrue(Serializable.class.isAssignableFrom(Integer[].class));
    final Collection<Class<?>> interfaces = Arrays.asList(Integer[].class.getInterfaces());
    assertTrue(interfaces.contains(Cloneable.class));
    assertTrue(interfaces.contains(Serializable.class));
  }


  /*
   * COVARIANT semantics (for the most part).
   */


  @Test
  final void testRawTypeAssignableFromRawType() {
    assertTrue(this.covariantTypeSemantics.assignable(Number.class, Integer.class));
    assertTrue(this.covariantTypeSemantics.assignable(Number.class, Number.class));
    assertTrue(this.covariantTypeSemantics.assignable(int.class, int.class));
    assertTrue(this.covariantTypeSemantics.assignable(Object.class, int.class));
    assertFalse(this.covariantTypeSemantics.assignable(Integer.class, Number.class));
  }

  @Test
  final void testRawArrayAssignableFromRawArray() {
    final Type numbers = new Number[0].getClass();
    final Type numbersOfNumbers = new Number[0][].getClass();
    final Type integers = new Integer[0].getClass();
    final Type integersOfIntegers = new Integer[0][].getClass();
    final Type ints = new int[0].getClass();
    assertTrue(this.covariantTypeSemantics.assignable(Object.class, numbers));
    assertTrue(this.covariantTypeSemantics.assignable(Object.class, integers));
    assertTrue(this.covariantTypeSemantics.assignable(Object.class, ints));
    assertTrue(this.covariantTypeSemantics.assignable(numbers, numbers));
    assertTrue(this.covariantTypeSemantics.assignable(numbers, integers));
    assertFalse(this.covariantTypeSemantics.assignable(numbers, ints));
    assertTrue(this.covariantTypeSemantics.assignable(integers, integers));
    assertFalse(this.covariantTypeSemantics.assignable(integers, ints));
    assertFalse(this.covariantTypeSemantics.assignable(numbers, Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(integers, Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(ints, Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(integers, numbers));
    assertFalse(this.covariantTypeSemantics.assignable(ints, numbers));
    assertTrue(this.covariantTypeSemantics.assignable(numbersOfNumbers, numbersOfNumbers));
    assertTrue(this.covariantTypeSemantics.assignable(numbersOfNumbers, new Number[0][].getClass()));
    assertTrue(this.covariantTypeSemantics.assignable(numbersOfNumbers, integersOfIntegers));
    assertFalse(this.covariantTypeSemantics.assignable(integersOfIntegers, numbersOfNumbers));
  }

  @Test
  final void testRawTypeAssignableFromParameterizedType() {
    assertTrue(this.covariantTypeSemantics.assignable(Map.class,
                                                      new DefaultParameterizedType(null,
                                                                                   Map.class,
                                                                                   String.class,
                                                                                   Integer.class)));
    assertTrue(this.covariantTypeSemantics.assignable(Map.class,
                                                      new DefaultParameterizedType(null,
                                                                                   Map.class,
                                                                                   Object.class,
                                                                                   Object.class)));
    assertTrue(this.covariantTypeSemantics.assignable(Map.class,
                                                      new DefaultParameterizedType(null,
                                                                                   HashMap.class,
                                                                                   Object.class,
                                                                                   Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(Map.class,
                                                       new DefaultParameterizedType(null,
                                                                                    List.class,
                                                                                    Object.class)));
  }

  @Test
  final <A, B extends Number, C extends Runnable & CharSequence> void testRawTypeAssignableFromTypeVariable() {
    final Type a = new JavaType.Token<A>() {}.type();
    final Type b = new JavaType.Token<B>() {}.type();
    final Type c = new JavaType.Token<C>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(Object.class, a));
    assertFalse(this.covariantTypeSemantics.assignable(Number.class, a));
    assertFalse(this.covariantTypeSemantics.assignable(Runnable.class, a));
    assertTrue(this.covariantTypeSemantics.assignable(Object.class, b));
    assertTrue(this.covariantTypeSemantics.assignable(Number.class, b));
    assertFalse(this.covariantTypeSemantics.assignable(Integer.class, b));
    assertFalse(this.covariantTypeSemantics.assignable(Runnable.class, b));
    assertTrue(this.covariantTypeSemantics.assignable(Object.class, c));
    assertTrue(this.covariantTypeSemantics.assignable(Runnable.class, c));
    assertTrue(this.covariantTypeSemantics.assignable(CharSequence.class, c));
    assertFalse(this.covariantTypeSemantics.assignable(String.class, c));
    assertFalse(this.covariantTypeSemantics.assignable(Integer.class, c));
  }

  @Test
  final void testRawTypeAssignableFromWildcardType() {
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    Integer.class),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    UnboundedWildcardType.INSTANCE)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    Integer.class),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new UpperBoundedWildcardType(Number.class))));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    Integer.class),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new UpperBoundedWildcardType(Integer.class))));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    Integer.class),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new LowerBoundedWildcardType(Integer.class))));
  }

  @Test
  final void testRawTypeAssignableFromGenericArrayType() {
    @SuppressWarnings("rawtypes")
      final Class<?> rawListArrayClass = new List[0].getClass();
    @SuppressWarnings("rawtypes")
      final Class<?> rawArrayListArrayClass = new ArrayList[0].getClass();
    assertTrue(this.covariantTypeSemantics.assignable(rawListArrayClass,
                                                      new DefaultGenericArrayType(List.class, Object.class)));
    assertTrue(this.covariantTypeSemantics.assignable(rawListArrayClass,
                                                      new DefaultGenericArrayType(List.class, Integer.class)));
    assertTrue(this.covariantTypeSemantics.assignable(rawListArrayClass,
                                                      new DefaultGenericArrayType(ArrayList.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(rawArrayListArrayClass,
                                                       new DefaultGenericArrayType(List.class, Integer.class)));
  }

  @Test
  final void testParameterizedTypeAssignableFromRawType() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Map.class, Object.class, Object.class),
                                                      Map.class));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Map.class, Runnable.class, Exception.class),
                                                      Map.class));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Map.class, Object.class, Object.class),
                                                      HashMap.class));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Map.class, Runnable.class, Exception.class),
                                                      HashMap.class));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, String.class),
                                                       Collection.class));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Comparable.class, Integer.class),
                                                       Double.class));
  }

  @Test
  final void testParameterizedTypeAssignableFromParameterizedType() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Map.class, String.class, Integer.class),
                                                      new DefaultParameterizedType(null, Map.class, String.class, Integer.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Runnable.class),
                                                      new DefaultParameterizedType(null, List.class, Runnable.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                      new DefaultParameterizedType(null, List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                       new DefaultParameterizedType(null, List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                       new DefaultParameterizedType(null, List.class, Runnable.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Number.class),
                                                       new DefaultParameterizedType(null, List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Number.class),
                                                       new DefaultParameterizedType(null, List.class, Runnable.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Integer.class),
                                                       new DefaultParameterizedType(null, List.class, Number.class)));
  }

  @Test
  final void testArrayListRunnableCanBeAssignedToListRunnable() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Runnable.class),
                                                      new DefaultParameterizedType(null, ArrayList.class, Runnable.class)));
  }

  @Test
  final void testArrayListObjectCanBeAssignedToListObject() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Object.class),
                                                      new DefaultParameterizedType(null, ArrayList.class, Object.class)));
  }

  @Test
  final void testHashMapStringIntegerCanBeAssignedToMapStringInteger() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Map.class, String.class, Integer.class),
                                                      new DefaultParameterizedType(null, HashMap.class, String.class, Integer.class)));
  }

  @Test
  final
    <A extends Collection<Number>,
               B extends List<Runnable> & Comparable<CharSequence>,
                         C extends B>
    void testParameterizedTypeAssignableFromTypeVariable() {
    final Type a = new JavaType.Token<A>() {}.type();
    final Type b = new JavaType.Token<B>() {}.type();
    final Type c = new JavaType.Token<C>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Collection.class, Number.class), a));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Iterable.class, Number.class), a));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Number.class), a));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Iterable.class, Object.class), a));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Iterable.class, Integer.class), a));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Iterable.class, Runnable.class), b));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Collection.class, Runnable.class), b));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Runnable.class), b));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Comparable.class, CharSequence.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, ArrayList.class, Runnable.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Object.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, FutureTask.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Comparable.class, String.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Comparable.class, Object.class), b));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Iterable.class, Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Collection.class, Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Comparable.class, CharSequence.class), c));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, ArrayList.class, Runnable.class), c));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Object.class), c));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, FutureTask.class), c));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Comparable.class, String.class), c));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, Comparable.class, Object.class), c));
  }

  @Test
  final void testParameterizedTypeAssignableFromParameterizedWildcardType() {
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new DefaultParameterizedType(null,
                                                                                                                 Collection.class,
                                                                                                                 Integer.class)),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    UnboundedWildcardType.INSTANCE)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new DefaultParameterizedType(null,
                                                                                                                 Collection.class,
                                                                                                                 Integer.class)),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new UpperBoundedWildcardType(Iterable.class))));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new DefaultParameterizedType(null,
                                                                                                                 Collection.class,
                                                                                                                 Integer.class)),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new UpperBoundedWildcardType(Collection.class))));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new DefaultParameterizedType(null,
                                                                                                                 Collection.class,
                                                                                                                 Integer.class)),
                                                       new DefaultParameterizedType(null,
                                                                                    Collection.class,
                                                                                    new LowerBoundedWildcardType(new DefaultParameterizedType(null,
                                                                                                                                              Collection.class,
                                                                                                                                              Integer.class)))));
  }

  @Test
  final void testParameterizedTypeAssignableFromGenericArrayType() {
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Integer.class),
                                                       new DefaultGenericArrayType(List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultParameterizedType(null, List.class, Integer.class),
                                                       new DefaultGenericArrayType(ArrayList.class, Integer.class)));
  }

  @Test
  final <T, S extends Number> void testTypeVariableAssignableFromRawType() {
    final Type t = new JavaType.Token<T>() {}.type();
    final Type s = new JavaType.Token<S>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(t, Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(t, List.class));
    assertFalse(this.covariantTypeSemantics.assignable(s, Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(s, Number.class));
    assertFalse(this.covariantTypeSemantics.assignable(s, Long.class));
  }

  @Test
  final <T, S extends List<Number>> void testTypeVariableAssignableFromParameterizedType() {
    final Type t = new JavaType.Token<T>() {}.type();
    final Type s = new JavaType.Token<S>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(t,
                                                       new DefaultParameterizedType(null, List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(t,
                                                       new DefaultParameterizedType(null, List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(s,
                                                       new DefaultParameterizedType(null, List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(s,
                                                       new DefaultParameterizedType(null, List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(s,
                                                       new DefaultParameterizedType(null, List.class, Long.class)));
  }

  @Test
  final <A, B, C extends Number, D extends Integer> void testTypeVariableAssignableFromTypeVariable() {
    final int count = 5;
    final Type[] typeVariables = new Type[count];
    typeVariables[0] = new JavaType.Token<A>() {}.type();
    typeVariables[1] = new JavaType.Token<B>() {}.type();
    typeVariables[2] = new JavaType.Token<C>() {}.type();
    typeVariables[3] = new JavaType.Token<D>() {}.type();
    typeVariables[4] = new JavaType.Token<D[]>() {}.type();
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < count; j++) {
        if (i == j) {
          assertTrue(this.covariantTypeSemantics.assignable(typeVariables[i], typeVariables[j]));
        } else {
          assertFalse(this.covariantTypeSemantics.assignable(typeVariables[i], typeVariables[j]));
        }
      }
    }
  }

  @Test
  final <A, B extends A, C extends A, D extends C, E extends D> void testTypeVariableAssignableFromTypeVariable2() {
    final int count = 5;
    final Type[] typeVariables = new Type[count];
    typeVariables[0] = new JavaType.Token<A>() {}.type();
    typeVariables[1] = new JavaType.Token<B>() {}.type();
    typeVariables[2] = new JavaType.Token<C>() {}.type();
    typeVariables[3] = new JavaType.Token<D>() {}.type();
    typeVariables[4] = new JavaType.Token<E>() {}.type();

    for (int i = 0; i < count; i++) {
      for (int j = 0; j < count; j++) {
        if (i == j || i == 0 || (i < j && i != 1)) {
          assertTrue(this.covariantTypeSemantics.assignable(typeVariables[i], typeVariables[j]));
        } else {
          assertFalse(this.covariantTypeSemantics.assignable(typeVariables[i], typeVariables[j]));
        }
      }
    }
  }

  @Test
  final <A, B extends Number> void testTypeVariableAssignableFromWildcard() {
    final Type a = new JavaType.Token<A>() {}.type();
    final Type b = new JavaType.Token<B>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(a, UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.assignable(a, new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(a, new LowerBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.assignable(b, new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new LowerBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new UpperBoundedWildcardType(Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new LowerBoundedWildcardType(Integer.class)));
  }

  @Test
  final <A, B extends List<Integer>> void testTypeVariableAssignableFromGenericArrayType() {
    final Type a = new JavaType.Token<A>() {}.type();
    final Type b = new JavaType.Token<B>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(a, new DefaultGenericArrayType(List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(a, new DefaultGenericArrayType(List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(a, new DefaultGenericArrayType(List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new DefaultGenericArrayType(List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new DefaultGenericArrayType(List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new DefaultGenericArrayType(List.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new DefaultGenericArrayType(ArrayList.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new DefaultGenericArrayType(ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(b, new DefaultGenericArrayType(ArrayList.class, Integer.class)));
  }

  @Test
  final void testWildcardAssignableFromRawType() {
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE, Number.class));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Number.class), Number.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Integer.class), Number.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Number.class), Number.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Number.class), Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Number.class), Integer.class));
  }

  @Test
  final <A, B extends Number, C extends B, D extends Number & Serializable> void testWildcardWithTypeVariableAssignableFromRawType() {
    final Type a = new JavaType.Token<A>() {}.type();
    final Type b = new JavaType.Token<B>() {}.type();
    final Type c = new JavaType.Token<C>() {}.type();
    final Type d = new JavaType.Token<D>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(a), Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(b), Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(b), Number.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(b), Integer.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(c), Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(c), Number.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(c), Integer.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(d), Number.class));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(d), Serializable.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(a), Object.class));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(a), Number.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(b), Object.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(b), Number.class));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(b), Integer.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(c), Object.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(c), Number.class));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(c), Integer.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d), Object.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d), Number.class));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d), Serializable.class));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d), Integer.class));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d), Runnable.class));
  }

  @Test
  final void testWildcardAssignableFromParameterizedType() {
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                      new DefaultParameterizedType(null, Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                      new DefaultParameterizedType(null, List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                      new DefaultParameterizedType(null, ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                       new DefaultParameterizedType(null, Collection.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                       new DefaultParameterizedType(null, Collection.class, Object.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                      new DefaultParameterizedType(null, Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                      new DefaultParameterizedType(null, Iterable.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                       new DefaultParameterizedType(null, List.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                       new DefaultParameterizedType(null, ArrayList.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                       new DefaultParameterizedType(null, Collection.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class)),
                                                       new DefaultParameterizedType(null, Collection.class, Object.class)));

  }

  @Test
  final void testCollectionNumberIsAssignableToUnboundedWildcard() {
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE,
                                                      new DefaultParameterizedType(null, Collection.class, Number.class)));
  }

  @Test
  final <A, B extends Number, C extends Runnable & Appendable> void testWildcardAssignableFromTypeVariable() {
    final Type a = new JavaType.Token<A>() {}.type();
    final Type b = new JavaType.Token<B>() {}.type();
    final Type c = new JavaType.Token<C>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE, a));
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE, b));
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE, c));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Number.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Integer.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Number.class), b));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Integer.class), b));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Runnable.class), c));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Appendable.class), c));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Runnable.class), c));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Appendable.class), c));
  }

  @Test
  final
    <A,
    B extends A,
              C extends A,
                        D extends C,
                                  E extends D>
    void testWildcardWithTypeVariableAssignableFromTypeVariable() {
    final int count = 5;
    final Type[] typeVariables = new Type[count];
    typeVariables[0] = new JavaType.Token<A>() {}.type();
    typeVariables[1] = new JavaType.Token<B>() {}.type();
    typeVariables[2] = new JavaType.Token<C>() {}.type();
    typeVariables[3] = new JavaType.Token<D>() {}.type();
    typeVariables[4] = new JavaType.Token<E>() {}.type();
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < count; j++) {
        if (i == j || i == 0 || (i < j && i != 1)) {
          assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(typeVariables[i]), typeVariables[j]));
          assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(typeVariables[j]), typeVariables[i]));
        } else {
          assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(typeVariables[i]), typeVariables[j]));
          assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(typeVariables[j]), typeVariables[i]));
        }
      }
    }
  }

  @Test
  final void testWildcardAssignableFromWildcard() {
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE, UnboundedWildcardType.INSTANCE));
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE, new UpperBoundedWildcardType(Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Number.class), new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Number.class), UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Integer.class), UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Integer.class), new UpperBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Integer.class), new UpperBoundedWildcardType(Integer.class)));
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE, new LowerBoundedWildcardType(Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Number.class), new LowerBoundedWildcardType(Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Integer.class), new LowerBoundedWildcardType(Integer.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Number.class), new LowerBoundedWildcardType(Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Integer.class), new LowerBoundedWildcardType(Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Number.class), new LowerBoundedWildcardType(Integer.class)));
  }

  @Test
  final <A extends Throwable, B extends A, C extends B, D extends Exception> void testWildcardAssignableFromWildcard2() {
    final int count = 4;
    Type[] typeVariables = new Type[count];
    final Type a = typeVariables[0] = new JavaType.Token<A>() {}.type();
    final Type b = typeVariables[1] = new JavaType.Token<B>() {}.type();
    final Type c = typeVariables[2] = new JavaType.Token<C>() {}.type();
    final Type d = typeVariables[3] = new JavaType.Token<D>() {}.type();
    for (int i = 0; i < count; i++) {
      assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE,
                                                        new UpperBoundedWildcardType(typeVariables[i])));
      assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE,
                                                        new LowerBoundedWildcardType(typeVariables[i])));
      for (int j = 0; j < count; j++) {
        assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(typeVariables[i]),
                                                           new LowerBoundedWildcardType(typeVariables[j])));
        assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(typeVariables[i]),
                                                           new UpperBoundedWildcardType(typeVariables[j])));
        if (i == j || (i < j && j != 3)) {
          assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(typeVariables[i]),
                                                            new UpperBoundedWildcardType(typeVariables[j])));
          assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(typeVariables[j]),
                                                            new LowerBoundedWildcardType(typeVariables[i])));
        } else {
          assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(typeVariables[i]),
                                                             new UpperBoundedWildcardType(typeVariables[j])));
          assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(typeVariables[j]),
                                                             new LowerBoundedWildcardType(typeVariables[i])));
        }
      }
    }
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Exception.class),
                                                      new UpperBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(RuntimeException.class),
                                                       new UpperBoundedWildcardType(d)));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Serializable.class),
                                                      new UpperBoundedWildcardType(b)));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Throwable.class),
                                                      new UpperBoundedWildcardType(b)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Exception.class),
                                                       new UpperBoundedWildcardType(b)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(a),
                                                      new LowerBoundedWildcardType(Throwable.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(c),
                                                      new LowerBoundedWildcardType(Throwable.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(a),
                                                       new LowerBoundedWildcardType(Exception.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(c),
                                                       new LowerBoundedWildcardType(Exception.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d),
                                                      new LowerBoundedWildcardType(Throwable.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d),
                                                      new LowerBoundedWildcardType(Exception.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d),
                                                       new LowerBoundedWildcardType(RuntimeException.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(RuntimeException.class),
                                                       new LowerBoundedWildcardType(a)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Throwable.class),
                                                       new LowerBoundedWildcardType(a)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(RuntimeException.class),
                                                       new LowerBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Throwable.class),
                                                       new LowerBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(Exception.class),
                                                       new LowerBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(Exception.class),
                                                       new UpperBoundedWildcardType(d)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(d),
                                                       new UpperBoundedWildcardType(Exception.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(d),
                                                       new LowerBoundedWildcardType(Exception.class)));
  }

  @Test
  final void testWildcardAssignableFromGenericArrayType() {
    assertTrue(this.covariantTypeSemantics.assignable(UnboundedWildcardType.INSTANCE,
                                                      new DefaultGenericArrayType(List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                      new DefaultGenericArrayType(List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                      new DefaultGenericArrayType(ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new UpperBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                       new DefaultGenericArrayType(Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                      new DefaultGenericArrayType(List.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                      new DefaultGenericArrayType(Collection.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new LowerBoundedWildcardType(new DefaultGenericArrayType(List.class, Number.class)),
                                                       new DefaultGenericArrayType(ArrayList.class, Number.class)));
  }

  @Test
  final void testGenericArrayTypeAssignableFromRawType() {
    @SuppressWarnings("rawtypes")
      final Class<?> rawArrayListArrayClass = new ArrayList[0].getClass();
    @SuppressWarnings("rawtypes")
      final Class<?> rawListArrayClass = new List[0].getClass();
    @SuppressWarnings("rawtypes")
      final Class<?> rawObjectArrayClass = new Object[0].getClass();
    @SuppressWarnings("rawtypes")
      final Class<?> rawSetArrayClass = new Set[0].getClass();
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class),
                                                      rawListArrayClass));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class),
                                                      rawArrayListArrayClass));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class),
                                                       rawObjectArrayClass));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class),
                                                       rawSetArrayClass));
  }

  @Test
  final void testGenericArrayTypeAssignableFromParameterizedType() {
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class),
                                                       new DefaultParameterizedType(null, List.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class),
                                                       new DefaultParameterizedType(null, List.class, Number.class)));
  }

  @Test
  final <A, B extends List<Number>> void testGenericArrayTypeAssignableFromTypeVariable() {
    final Type a = new JavaType.Token<A>() {}.type();
    final Type b = new JavaType.Token<B>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class), a));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(List.class, Number.class), b));
  }

  @Test
  final <A, B extends Number> void testGenericArrayTypeAssignableFromWildcard() {
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       UnboundedWildcardType.INSTANCE));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class))));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Number.class))));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new UpperBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Integer.class))));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new LowerBoundedWildcardType(new DefaultParameterizedType(null, Collection.class, Integer.class))));
  }

  @Test
  final void testGenericArrayTypeAssignableFromGenericArrayType() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                      new DefaultGenericArrayType(Collection.class, Number.class)));
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, UnboundedWildcardType.INSTANCE),
                                                      new DefaultGenericArrayType(ArrayList.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new DefaultGenericArrayType(Iterable.class, Number.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new DefaultGenericArrayType(Collection.class, Object.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new DefaultGenericArrayType(Collection.class, Integer.class)));
    assertFalse(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                       new DefaultGenericArrayType(Collection.class, Double.class)));

  }

  @Test
  final void testArrayListNumberArrayCanBeAssignedToCollectionNumberArray() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                      new DefaultGenericArrayType(ArrayList.class, Number.class)));
  }

  @Test
  final void testListNumberArrayCanBeAssignedToCollectionNumberArray() {
    assertTrue(this.covariantTypeSemantics.assignable(new DefaultGenericArrayType(Collection.class, Number.class),
                                                      new DefaultGenericArrayType(List.class, Number.class)));
  }

  @Test
  final void testDifferentTypeParametersDoNotMatch() {
    final Type receiverType = new DefaultParameterizedType(null, Foo.class, Integer.class);
    final Type payloadType = new DefaultParameterizedType(null, Foo.class, String.class);
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testArrayAndComponentTypeIncompatibility() {
    final Type receiverType = new JavaType.Token<Foo<String>[]>() {}.type();
    final Type payloadType = new JavaType.Token<Foo<String>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testIncompatibleParameterizedTypeArrays() {
    final Type receiverType = new JavaType.Token<Foo<Integer>[]>() {}.type();
    final Type payloadType = new JavaType.Token<Foo<String>[]>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testRealTypeParameterAssignableToWildcard() {
    final Type receiverType = new JavaType.Token<Foo<?>>() {}.type();
    final Type payloadType = new JavaType.Token<Foo<String>>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testArrayOfRealParameterizedTypeAssignableToArrayOfWildcardParameterizedType() {
    final Type receiverType = new JavaType.Token<Foo<?>[]>() {}.type();
    final Type payloadType = new JavaType.Token<Foo<String>[]>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testFooQuestionMarkArrayIsNotAssignableToFooStringArray() {
    final Type receiverType = new JavaType.Token<Foo<String>[]>() {}.type();
    final Type payloadType = new JavaType.Token<Foo<?>[]>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testArrayBoxNotAllowed() {
    assertFalse(this.covariantTypeSemantics.assignable(int[].class, Integer[].class));
  }

  @Test
  final void testMultidimensionalArrayTypeParameterCase() {
    final Type receiverType = new JavaType.Token<List<Number[][]>>() {}.type();
    final Type payloadType = new JavaType.Token<List<Integer[][]>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final <T> void testFooTAssignableToFooStringWithBeanTypeSemantics() {
    final Type receiverType = new JavaType.Token<Foo<String>>() {}.type();
    final Type payloadType = new JavaType.Token<Foo<T>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(receiverType, payloadType));
  }

  @Test
  final void testFooObjectAssignableToFoo() {
    final Type payloadType = new JavaType.Token<Foo<Object>>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(Foo.class, payloadType));
  }

  @Test
  final <T> void testFooTAssignableToFoo() {
    final Type payloadType = new JavaType.Token<Foo<T>>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(Foo.class, payloadType));
  }

  @Test
  final <N extends Number> void testFooNExtendsNumberAssignableToFoo() {
    final Type payloadType = new JavaType.Token<Foo<N>>() {}.type();
    assertTrue(this.covariantTypeSemantics.assignable(Foo.class, payloadType));
  }

  @Test
  final <T1 extends Number, T2 extends T1> void testListRunnableIsNotAssignableFromListT2() {
    final Type listT2 = new JavaType.Token<List<T2>>() {}.type();
    final Type listRunnable = new JavaType.Token<List<Runnable>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(listRunnable, listT2));
    assertFalse(this.covariantTypeSemantics.assignable(listT2, listRunnable));
  }

  @Test
  final <T1 extends Number, T2 extends T1> void testListT1ExtendsNumberIsNotAssignableFromListT2ExtendsT1ExtendsNumber() {
    // This doesn't compile:
    // final List<T2> lt2 = null;
    // final List<T1> lt1 = lt2;
    final Type listT1 = new JavaType.Token<List<T1>>() {}.type();
    final Type listT2 = new JavaType.Token<List<T2>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(listT1, listT2));
  }

  @Test
  final <T1 extends Number, T2 extends T1> void testListNumberIsNotAssignableFromListT2ExtendsT1ExtendsNumber() {
    final Type n1 = new JavaType.Token<List<Number>>() {}.type();
    final Type payloadType = new JavaType.Token<List<T2>>() {}.type();
    assertFalse(this.covariantTypeSemantics.assignable(n1, payloadType));
  }

  @Test
  final void testArrayCovariance1() {
    // Prove the JDK lets us compile this.
    final Number[] numbers = new Integer[] { Integer.valueOf(0) };
    final Type type1 = new Number[0].getClass();
    final Type type2 = new Integer[0].getClass();
    assertTrue(this.covariantTypeSemantics.assignable(type1, type2));
  }


  /*
   * Inner and nested classes.
   */


  private static final class TypeSet {

  }

  private static final class Contextual<T> {

  }

  private static final class Foo<T> {
  }

  private static final class Bar implements Function<Contextual<?>, TypeSet> {

    @Override
    public final TypeSet apply(final Contextual<?> c) {
      return new TypeSet();
    }

  }

}

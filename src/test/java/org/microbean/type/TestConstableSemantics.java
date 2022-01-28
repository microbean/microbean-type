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

import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import java.lang.invoke.MethodHandles;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.lang.constant.ConstantDescs.BSM_GET_STATIC_FINAL;
import static java.lang.constant.ConstantDescs.CD_Class;
import static java.lang.constant.ConstantDescs.CD_String;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.type.ConstantDescs.CD_Method;
import static org.microbean.type.ConstantDescs.CD_UnboundedWildcardType;
import static org.microbean.type.ConstantDescs.CD_WildcardType;

import static org.microbean.type.JavaType.Token;

final class TestConstableSemantics {

  private TestConstableSemantics() {
    super();
  }

  @Test
  final void testConstableParameterizedType() throws ReflectiveOperationException {
    final ParameterizedType p0 = (ParameterizedType)new Token<List<String>>() {}.type();
    assertNotNull(p0);
    assertFalse(p0 instanceof DefaultParameterizedType);
    final ParameterizedType p1 = (ParameterizedType)JavaTypes.describeConstable(p0).orElseThrow().resolveConstantDesc(MethodHandles.lookup());
    assertTrue(p1 instanceof DefaultParameterizedType);
    assertNotSame(p0, p1);
    assertEquals(p0, p1);
    assertEquals(p1, p0);
  }

  @Test
  final void testConstableGenericArrayType() throws ReflectiveOperationException {
    final GenericArrayType g0 = (GenericArrayType)new Token<List<String>[]>() {}.type();
    assertNotNull(g0);
    assertFalse(g0 instanceof DefaultGenericArrayType);
    final GenericArrayType g1 = (GenericArrayType)JavaTypes.describeConstable(g0).orElseThrow().resolveConstantDesc(MethodHandles.lookup());
    assertTrue(g1 instanceof DefaultGenericArrayType);
    assertNotSame(g0, g1);
    assertEquals(g0, g1);
    assertEquals(g1, g0);
  }

  @Test
  final <T> void testConstableTypeVariable() throws ReflectiveOperationException {
    final TypeVariable<?> t0 = (TypeVariable<?>)new Token<T>() {}.type();
    assertNotNull(t0);
    assertFalse(t0 instanceof DefaultTypeVariable);
    final TypeVariable<?> t1 = (TypeVariable<?>)JavaTypes.describeConstable(t0).orElseThrow().resolveConstantDesc(MethodHandles.lookup());
    assertSame(t0.getClass(), t1.getClass());
    assertNotSame(t0, t1);
    assertEquals(t0, t1);
    assertEquals(t1, t0);
  }

  @Test
  final <T> void testConstableDefaultTypeVariable() throws ReflectiveOperationException {
    final DefaultTypeVariable<Method> dtv =
      new DefaultTypeVariable<>(this.getClass().getDeclaredMethod("testConstableDefaultTypeVariable"), "T");
    final TypeVariable<?> jdkTv = (TypeVariable<?>)JavaTypes.describeConstable(dtv).orElseThrow().resolveConstantDesc(MethodHandles.lookup());
    assertNotSame(dtv, jdkTv);
    final TypeVariable<?> dtvDelegate = dtv.getDelegate();
    assertNotNull(dtvDelegate);
    assertNotSame(dtvDelegate, jdkTv);
    assertEquals(dtv, jdkTv);
    assertEquals(dtvDelegate, jdkTv);
    assertEquals(jdkTv, dtvDelegate);
    // Note:
    // https://github.com/openjdk/jdk/blob/jdk-17+35/src/java.base/share/classes/sun/reflect/generics/reflectiveObjects/TypeVariableImpl.java#L163
    // There's a cls == TypeVariableImpl.class comparison in there.
    assertNotEquals(jdkTv, dtv);
    assertTrue(JavaTypes.equals(jdkTv, dtv));
    assertTrue(JavaTypes.equals(dtv, jdkTv));
  }

  @Test
  final void testConstableLowerBoundedWildcardType() throws ReflectiveOperationException {
    final WildcardType w0 = (WildcardType)((ParameterizedType)new Token<List<? super String>>() {}.type()).getActualTypeArguments()[0];
    assertNotNull(w0);
    assertFalse(w0 instanceof LowerBoundedWildcardType);
    final WildcardType w1 = (WildcardType)JavaTypes.describeConstable(w0).orElseThrow().resolveConstantDesc(MethodHandles.lookup());
    assertTrue(w1 instanceof LowerBoundedWildcardType);
    assertNotSame(w0, w1);
    assertEquals(w0, w1);
    assertEquals(w1, w0);
  }

  @Test
  final void testGetStaticFinal() throws ReflectiveOperationException {
    DynamicConstantDesc.ofNamed(BSM_GET_STATIC_FINAL, "INSTANCE", CD_WildcardType, CD_UnboundedWildcardType)
      .resolveConstantDesc(MethodHandles.lookup());
  }

  @Test
  final void testConstableUnboundedWildcardType() throws ReflectiveOperationException {
    final WildcardType w0 = (WildcardType)((ParameterizedType)new Token<List<?>>() {}.type()).getActualTypeArguments()[0];
    assertNotNull(w0);
    assertFalse(w0 instanceof UnboundedWildcardType);
    final WildcardType w1 = (WildcardType)JavaTypes.describeConstable(w0).orElseThrow().resolveConstantDesc(MethodHandles.lookup());
    assertTrue(w1 instanceof UnboundedWildcardType);
    assertNotSame(w0, w1);
    assertEquals(w0, w1);
    assertEquals(w1, w0);
  }

  @Test
  final void testConstableMethod() throws ReflectiveOperationException {
    final Method m = this.getClass().getDeclaredMethod("testConstableMethod");
    assertNotNull(m);
    final Optional<? extends ConstantDesc> constable = JavaTypes.describeConstable(m);
    assertNotNull(constable);
    assertTrue(constable.isPresent());
    final Method m1 = (Method)constable.orElseThrow().resolveConstantDesc(MethodHandles.lookup());
    assertNotNull(m1);
    assertEquals(m, m1);
  }

}

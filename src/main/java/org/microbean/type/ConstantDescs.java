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

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;

import static java.lang.constant.ConstantDescs.CD_Class;
import static java.lang.constant.ConstantDescs.CD_MethodHandle;
import static java.lang.constant.ConstantDescs.CD_MethodHandles;
import static java.lang.constant.DirectMethodHandleDesc.Kind.STATIC;

/**
 * A utility class containing useful {@link
 * java.lang.constant.ConstantDesc}s (primarily {@link ClassDesc}s).
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class ConstantDescs {


  /*
   * Static fields.
   */


  /**
   * A {@link ClassDesc} representing {@link java.lang.reflect.Type
   * java.lang.reflect.Type}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_Type = ClassDesc.of("java.lang.reflect.Type");

  /**
   * A {@link ClassDesc} representing {@link
   * java.lang.reflect.Constructor java.lang.reflect.Constructor}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_Constructor = ClassDesc.of("java.lang.reflect.Constructor");

  /**
   * A {@link ClassDesc} representing {@link DefaultGenericArrayType
   * org.microbean.type.DefaultGenericArrayType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_DefaultGenericArrayType = ClassDesc.of(DefaultGenericArrayType.class.getName());

  /**
   * A {@link ClassDesc} representing {@link DefaultParameterizedType
   * org.microbean.type.DefaultParameterizedType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_DefaultParameterizedType = ClassDesc.of(DefaultParameterizedType.class.getName());

  /**
   * A {@link ClassDesc} representing {@link
   * java.lang.reflect.GenericArrayType
   * java.lang.reflect.GenericArrayType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_GenericArrayType = ClassDesc.of("java.lang.reflect.GenericArrayType");
  
  /**
   * A {@link ClassDesc} representing {@link
   * java.lang.reflect.GenericDeclaration
   * java.lang.reflect.GenericDeclaration}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_GenericDeclaration = ClassDesc.of("java.lang.reflect.GenericDeclaration");

  /**
   * A {@link ClassDesc} representing {@link LowerBoundedWildcardType
   * org.microbean.type.LowerBoundedWildcardType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_LowerBoundedWildcardType = ClassDesc.of(LowerBoundedWildcardType.class.getName());

  /**
   * A {@link ClassDesc} representing {@link java.lang.reflect.Member
   * java.lang.reflect.Member}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_Member = ClassDesc.of("java.lang.reflect.Member");

  /**
   * A {@link ClassDesc} representing {@link java.lang.reflect.Method
   * java.lang.reflect.Method}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_Method = ClassDesc.of("java.lang.reflect.Method");

  /**
   * A {@link ClassDesc} representing {@link
   * java.lang.reflect.ParameterizedType
   * java.lang.reflect.ParameterizedType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_ParameterizedType = ClassDesc.of("java.lang.reflect.ParameterizedType");

  
  /**
   * A {@link ClassDesc} representing {@link
   * java.lang.reflect.TypeVariable java.lang.reflect.TypeVariable}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_TypeVariable = ClassDesc.of("java.lang.reflect.TypeVariable");

  /**
   * A {@link ClassDesc} representing {@link UnboundedWildcardType
   * org.microbean.type.UnboundedWildcardType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_UnboundedWildcardType = ClassDesc.of(UnboundedWildcardType.class.getName());

  /**
   * A {@link ClassDesc} representing {@link UpperBoundedWildcardType
   * org.microbean.type.UpperBoundedWildcardType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_UpperBoundedWildcardType = ClassDesc.of(UpperBoundedWildcardType.class.getName());

  /**
   * A {@link ClassDesc} representing {@link UpperBoundedWildcardType
   * org.microbean.type.UpperBoundedWildcardType}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_WildcardType = ClassDesc.of("java.lang.reflect.WildcardType");

  /**
   * A {@link DirectMethodHandleDesc} representing {@link
   * java.lang.invoke.MethodHandles#reflectAs(Class, MethodHandle)}.
   *
   * @nullability This field is never {@code null}.
   */
  public static final DirectMethodHandleDesc DMHD_REFLECT_AS =
    MethodHandleDesc.ofMethod(STATIC, CD_MethodHandles, "reflectAs", MethodTypeDesc.of(CD_Member, CD_Class, CD_MethodHandle));


  /*
   * Constructors.
   */


  private ConstantDescs() {
    super();
  }

}

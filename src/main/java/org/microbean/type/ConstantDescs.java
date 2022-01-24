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

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

final class ConstantDescs {

  static final ClassDesc CD_Type = Type.class.describeConstable().orElseThrow();

  static final ClassDesc CD_Constructor = Constructor.class.describeConstable().orElseThrow();

  static final ClassDesc CD_DefaultGenericArrayType = DefaultGenericArrayType.class.describeConstable().orElseThrow();

  static final ClassDesc CD_DefaultParameterizedType = DefaultParameterizedType.class.describeConstable().orElseThrow();

  static final ClassDesc CD_GenericDeclaration = GenericDeclaration.class.describeConstable().orElseThrow();

  static final ClassDesc CD_LowerBoundedWildcardType = LowerBoundedWildcardType.class.describeConstable().orElseThrow();

  static final ClassDesc CD_Method = Method.class.describeConstable().orElseThrow();

  static final ClassDesc CD_TypeVariable = TypeVariable.class.describeConstable().orElseThrow();

  static final ClassDesc CD_UnboundedWildcardType = UnboundedWildcardType.class.describeConstable().orElseThrow();

  static final ClassDesc CD_UpperBoundedWildcardType = UpperBoundedWildcardType.class.describeConstable().orElseThrow();
    
  private ConstantDescs() {
    super();
  }
    
}

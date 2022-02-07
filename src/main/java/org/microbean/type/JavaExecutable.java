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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.List;

final class JavaExecutable implements Owner<Type> {

  private final Executable e;
  
  private final List<? extends JavaType> typeParameters;

  private final JavaType returnType;

  private final List<? extends JavaType> parameters;

  JavaExecutable(final Executable e, final boolean box) {
    super();
    this.e = e;

    final Type[] genericParameterTypes = e.getGenericParameterTypes();
    if (genericParameterTypes.length > 0) {
      final List<JavaType> parameters = new ArrayList<>(genericParameterTypes.length);
      for (final Type genericParameterType : genericParameterTypes) {
        parameters.add(JavaType.of(genericParameterType, box));
      }
      this.parameters = Collections.unmodifiableList(parameters);
    } else {
      this.parameters = List.of();
    }

    final TypeVariable<?>[] typeParameters = e.getTypeParameters();
    if (typeParameters.length > 0) {
      final List<JavaType> javaTypeParameters = new ArrayList<>(typeParameters.length);
      for (final TypeVariable<?> typeParameter : typeParameters) {
        javaTypeParameters.add(JavaType.of(typeParameter, box));
      }
      this.typeParameters = Collections.unmodifiableList(javaTypeParameters);
    } else {
      this.typeParameters = List.of();
    }
    
    if (e instanceof Constructor<?> constructor) {
      this.returnType = JavaType.of(void.class, box);
    } else if (e instanceof Method m) {
      this.returnType = JavaType.of(m.getGenericReturnType(), box);
    } else {
      throw new AssertionError("e: " + e);
    }
  }

  @Override // Owner<Type>
  public final Executable object() {
    return this.e;
  }

  @Override // Owner<Type>
  public final JavaType owner() {
    return JavaType.of(this.e.getDeclaringClass());
  }

  @Override // Owner<Type>
  public final String name() {
    return this.e.getName();
  }

  @Override // Owner<Type>
  public final JavaType type() {
    return this.returnType;
  }

  @Override // Owner<Type>
  public final List<? extends JavaType> parameters() {
    return this.parameters;
  }

  @Override // Owner<Type>
  public final List<? extends JavaType> typeParameters() {
    return this.typeParameters;
  }

  @Override // Object
  public final int hashCode() {
    int hashCode = 17;
    Object value = this.owner();
    int c = value == null ? 0 : value.hashCode();
    hashCode = 37 * hashCode + c;
    value = this.name();
    c = value == null ? 0 : value.hashCode();
    hashCode = 37 * hashCode + c;
    value = this.type();
    c = value == null ? 0 : value.hashCode();
    hashCode = 37 * hashCode + c;
    value = this.typeParameters();
    c = value == null ? 0 : value.hashCode();
    hashCode = 37 * hashCode + c;
    value = this.parameters();
    c = value == null ? 0 : value.hashCode();
    hashCode = 37 * hashCode + c;
    return hashCode;
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      final JavaExecutable her = (JavaExecutable)other;
      return
        Objects.equals(this.owner(), her.owner()) &&
        Objects.equals(this.name(), her.name()) &&
        Objects.equals(this.type(), her.type()) &&
        Objects.equals(this.typeParameters(), her.typeParameters()) &&
        Objects.equals(this.parameters(), her.parameters());        
    } else {
      return false;
    }
  }
  
}

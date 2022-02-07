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

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.List;

final class JavaExecutable implements Owner<Type> {

  private final JavaType owner;
  
  private final String name;

  private final List<? extends JavaType> typeParameters;

  private final JavaType returnType;

  private final List<? extends JavaType> parameters;

  JavaExecutable(final JavaType owner,
                 final String name,
                 final List<? extends JavaType> typeParameters,
                 final JavaType returnType,
                 final List<? extends JavaType> parameters,
                 final boolean box) {
    super();
    this.owner = owner;
    this.name = Objects.requireNonNull(name, "name");
    if (box) {
      this.returnType = returnType == null ? null : returnType.box();
      if (typeParameters == null || typeParameters.isEmpty()) {
        this.typeParameters = List.of();
        if (parameters == null || parameters.isEmpty()) {
          this.parameters = List.of();
        } else {
          final List<JavaType> list = new ArrayList<>(parameters.size());
          for (final JavaType parameter : parameters) {
            list.add(parameter.box());
          }
          this.parameters = Collections.unmodifiableList(list);
        }
      } else {
        List<JavaType> list = new ArrayList<>(typeParameters.size());
        for (final JavaType typeParameter : typeParameters) {
          list.add(typeParameter.box());
        }
        this.typeParameters = Collections.unmodifiableList(list);
        if (parameters == null || parameters.isEmpty()) {
          this.parameters = List.of();
        } else {
          list = new ArrayList<>(parameters.size());
          for (final JavaType parameter : parameters) {
            list.add(parameter.box());
          }
          this.parameters = Collections.unmodifiableList(list);
        }
      }
    } else {
      this.returnType = returnType == null ? null : returnType;
      if (typeParameters == null || typeParameters.isEmpty()) {
        this.typeParameters = List.of();
        if (parameters == null || parameters.isEmpty()) {
          this.parameters = List.of();
        } else {
          this.parameters = List.copyOf(parameters);
        }
      } else {
        this.typeParameters = List.copyOf(typeParameters);
        if (parameters == null || parameters.isEmpty()) {
          this.parameters = List.of();
        } else {
          this.parameters = List.copyOf(parameters);
        }
      }
    }
  }

  @Override // Owner<Type>
  public final JavaType owner() {
    return this.owner;
  }

  @Override // Owner<Type>
  public final String name() {
    return this.name();
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

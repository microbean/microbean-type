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

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;

/**
 * Bootstrap methods for invokedynamic situations.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class Bootstraps {


  /*
   * Static fields.
   */


  /**
   * The {@link ClassDesc} representing this class.
   *
   * @nullability This field is never {@code null}.
   */
  public static final ClassDesc CD_Bootstraps = Bootstraps.class.describeConstable().orElseThrow();


  /*
   * Constructors.
   */


  private Bootstraps() {
    super();
  }


  /*
   * Static methods.
   */


  /**
   * Returns a {@link TypeVariable} with a {@linkplain
   * TypeVariable#getName() name} {@linkplain String#equals(Object)
   * equal to} the supplied {@code name} and that is declared by the
   * supplied {@link GenericDeclaration}, or {@code null} if there is
   * no such {@link TypeVariable}.
   *
   * @param gd the {@link GenericDeclaration} in question; may be
   * {@code null} in which case {@code null} will be returned
   *
   * @param name the name in question; may be {@code null} in which
   * case {@code null} will be returned
   *
   * @return a {@link TypeVariable} with a {@linkplain
   * TypeVariable#getName() name} {@linkplain String#equals(Object)
   * equal to} the supplied {@code name} and that is declared by the
   * supplied {@link GenericDeclaration}, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final TypeVariable<?> getTypeVariable(final GenericDeclaration gd, final String name) {
    if (gd != null) {
      for (final TypeVariable<?> typeVariable : gd.getTypeParameters()) {
        if (typeVariable.getName().equals(name)) {
          return typeVariable;
        }
      }
    }
    return null;
  }

}

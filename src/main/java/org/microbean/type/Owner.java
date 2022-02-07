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

import java.util.List;

import org.microbean.development.annotation.OverridingEncouraged;

public interface Owner<T> {

  public Owner<T> owner();

  /**
   * Returns {@code true} if and only if this {@link Owner} represents
   * either a Java type that has a name, or an {@linkplain
   * java.lang.reflect.Executable executable}.
   *
   * <p>In the Java reflective type system, only {@link Class}, {@link
   * java.lang.reflect.TypeVariable}, and {@link
   * java.lang.reflect.Executable} instances have names.</p>
   *
   * @return {@code true} if and only if this {@link Owner} represents
   * either a Java type that has a name, or an {@linkplain
   * java.lang.reflect.Executable executable}
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @see #name()
   *
   * @see Class#getName()
   *
   * @see java.lang.reflect.TypeVariable#getName()
   */
  @OverridingEncouraged
  public default boolean named() {
    return this.name() != null;
  }
  
  public String name();

  public Type<T> type();

  @OverridingEncouraged
  public default boolean hasParameters() {
    return !this.parameters().isEmpty();
  }
  
  public List<? extends Type<T>> parameters();

  @OverridingEncouraged
  public default boolean hasTypeParameters() {
    return !this.typeParameters().isEmpty();
  }
    
  public List<? extends Type<T>> typeParameters();    
    
}

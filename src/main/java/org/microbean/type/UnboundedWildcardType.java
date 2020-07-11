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

import java.lang.reflect.WildcardType;

/**
 * A {@link java.lang.reflect.WildcardType} implementation that has no
 * bounds, i.e. has only a single {@linkplain
 * java.lang.reflect.WildcardType#getUpperBounds() upper bound} of
 * {@link Object Object.class}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see java.lang.reflect.WildcardType
 *
 * @see java.lang.reflect.WildcardType#getUpperBounds()
 */
public final class UnboundedWildcardType extends AbstractWildcardType {

  /**
   * The sole instance of this class.
   */
  public static final WildcardType INSTANCE = new UnboundedWildcardType();
  
  private UnboundedWildcardType() {
    super(null, null);
  }

}

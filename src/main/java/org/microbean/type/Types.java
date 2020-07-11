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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A hub for Java {@link Type}-related operations.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #toTypes(Type)
 *
 * @threadsafety Instances of this class are safe for concurrent use
 * by multiple threads.
 */
public final class Types {


  /*
   * Static fields.
   */

  
  static final Comparator<Type> typeNameComparator = new TypeNameComparator();

  private static final Map<Type, Type> wrapperTypes;

  static {
    final Map<Type, Type> map = new HashMap<>();
    map.put(boolean.class, Boolean.class);
    map.put(byte.class, Byte.class);
    map.put(char.class, Character.class);
    map.put(double.class, Double.class);
    map.put(float.class, Float.class);
    map.put(int.class, Integer.class);
    map.put(long.class, Long.class);
    map.put(short.class, Short.class);
    map.put(void.class, Void.class);
    wrapperTypes = Collections.unmodifiableMap(map);
  }


  /*
   * Constructors.
   */

  
  /**
   * Creates a new {@link Types}.
   */
  private Types() {
    super();
  }

  
  /*
   * Methods.
   */


  /**
   * For the vast majority of cases, returns the supplied {@link Type}
   * unchanged, but in the case where a raw {@link Class} is supplied,
   * returns an equivalent {@link ParameterizedType} whose {@linkplain
   * ParameterizedType#getActualTypeArguments() actual type arguments}
   * are not resolved.
   *
   * @param type the {@link Type} to normalize; may be {@code null} in
   * which case {@code null} will be returned; will often be returned
   * unchanged
   *
   * @return the normalized {@link Type}; {@code null} if {@code type}
   * was {@code null}
   *
   * @nullability This method may return {@code null} only if the
   * supplied {@link Type} is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static final Type normalize(final Type type) {
    final Type returnValue;
    if (type instanceof Class<?> cls) {
      final Type[] typeParameters = cls.getTypeParameters();
      if (typeParameters == null || typeParameters.length <= 0) {
        final Type componentType = cls.getComponentType();
        if (componentType == null) {
          returnValue = type;
        } else {
          final Type normalizedComponentType = normalize(componentType); // XXX recursive
          if (componentType == normalizedComponentType) {
            returnValue = type;
          } else {
            returnValue = new DefaultGenericArrayType(normalizedComponentType);
          }
        }
      } else {
        returnValue = new DefaultParameterizedType(cls.getDeclaringClass(), cls, typeParameters);
      }
    } else {
      returnValue = type;
    }
    return returnValue;
  }

  /*
   * Bookmark: resolve(Type)
   */

  /**
   * Resolves the supplied {@link Type} and returns the result.
   *
   * @param type the {@link Type} to resolve; may be {@code null}
   *
   * @param typeResolver the {@link Function} whose {@link
   * Function#apply(Object)} method will be called on, among other
   * things, {@link ParameterizedType} {@linkplain
   * ParameterizedType#getActualTypeArguments() type arguments}; must
   * not be {@code null}
   *
   * @return the resolved {@link Type}, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final Type resolve(final Type type, final Function<? super Type, ? extends Type> typeResolver) {
    final Type returnValue;
    if (type instanceof Class<?> cls) {
      returnValue = resolve(cls, typeResolver);
    } else if (type instanceof ParameterizedType parameterizedType) {
      returnValue = resolve(parameterizedType, typeResolver);
    } else if (type instanceof GenericArrayType genericArrayType) {
      returnValue = resolve(genericArrayType, typeResolver);
    } else if (type instanceof TypeVariable<?> typeVariable) {
      returnValue = resolve(typeVariable, typeResolver);
    } else if (type instanceof WildcardType wildcardType) {
      // (No-op.)
      returnValue = resolve(wildcardType, typeResolver);
    } else {
      // (Unknown type, so we don't know how to resolve it.)
      returnValue = type;
    }
    return returnValue;
  }

  private static final Type resolve(final Class<?> type, final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }

  private static final Type resolve(final ParameterizedType type, final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    if (candidate == null) {
      final Type[] actualTypeArguments = type.getActualTypeArguments();
      final int length = actualTypeArguments.length;
      final Type[] resolvedActualTypeArguments = new Type[length];
      boolean createNewType = false;
      for (int i = 0; i < length; i++) {
        final Type actualTypeArgument = actualTypeArguments[i];
        final Type resolvedActualTypeArgument = resolve(actualTypeArgument, typeResolver);
        resolvedActualTypeArguments[i] = resolvedActualTypeArgument;
        if (actualTypeArgument != resolvedActualTypeArgument) {
          // If they're not the same object reference, then resolution
          // did something, so we have to return a new type, not the
          // one we were handed.
          createNewType = true;
        }
      }
      return createNewType ? new DefaultParameterizedType(type.getOwnerType(), type.getRawType(), resolvedActualTypeArguments) : type;
    } else {
      return candidate;
    }

  }

  private static final Type resolve(final GenericArrayType type, final Function<? super Type, ? extends Type> typeResolver) {
    final Type returnValue;
    final Type candidate = typeResolver.apply(type);
    if (candidate == null) {
      final Type genericComponentType = type.getGenericComponentType();
      final Type resolvedComponentType = resolve(genericComponentType, typeResolver);
      assert resolvedComponentType != null;
      if (resolvedComponentType == genericComponentType) {
        // Identity means that basically resolution was a no-op, so the
        // genericComponentType was whatever it was, and so we are going
        // to be a no-op as well.
        returnValue = type;
      } else if (resolvedComponentType instanceof Class<?> componentType) {
        // This might happen when genericComponentType was a
        // TypeVariable.  In this case, it might get resolved to a
        // simple scalar (e.g. Integer.class).  Now we have,
        // effectively, a GenericArrayType whose genericComponentType is
        // just a plain class.  That's actually just an ordinary Java
        // array, so "resolve" this type by returning its array
        // equivalent.
        returnValue = Array.newInstance(componentType, 0).getClass();
      } else {
        // If we get here, we know that resolution actually did
        // something, so return a new GenericArrayType implementation
        // whose component type is the resolved version of the
        // original's.
        returnValue = new DefaultGenericArrayType(resolvedComponentType);
      }
    } else {
      returnValue = candidate;
    }
    return returnValue;
  }

  private static final Type resolve(final TypeVariable<?> type, final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }

  private static final Type resolve(final WildcardType type, final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }


  /*
   * Bookmark: toTypes(Type)
   */


  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable <code>Set</code>}
   * of {@link Type}s, each element of which is a {@link Type} which
   * any instance bearing the supplied {@link Type} will implement.
   *
   * <p>The {@link Set} that is returned is guaranteed to not contain
   * {@link WildcardType} or {@link TypeVariable} instances.</p>
   *
   * @param type the {@link Type} whose type closure should be
   * computed; may be {@code null}
   *
   * @return a non-{@code null} {@link TypeSet} <code>Set</code>} of
   * {@link Type}s; the supplied {@link Type} will be one of its
   * elements unless it is a {@link TypeVariable} or a {@link
   * WildcardType}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.
   *
   * @see #toTypes(Type, Predicate)
   */
  public static final TypeSet toTypes(final Type type) {
    return toTypes(type, null);
  }

  /**
   * Returns a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable <code>Set</code>}
   * of {@link Type}s, each element of which is a {@link Type} which
   * any instance bearing the supplied {@link Type} will implement.
   *
   * <p>The {@link Set} that is returned is guaranteed to not contain
   * {@link WildcardType} or {@link TypeVariable} instances.</p>
   *
   * <p>If the supplied {@link Predicate} returns {@code true} from an
   * invocation of its {@link Predicate#test(Object)} method, then the
   * {@link Type} that was passed to it will not be contained by the
   * returned {@link TypeSet}.</p>
   *
   * @param type the {@link Type} whose type closure should be
   * computed; may be {@code null}
   *
   * @param removalPredicate a {@link Predicate} used to selectively
   * remove some {@link Type}s from the computed {@link TypeSet}; may
   * be {@code null}
   *
   * @return a non-{@code null} {@link TypeSet} <code>Set</code>} of
   * {@link Type}s, filtered by the supplied {@link Predicate}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.
   */
  public static final TypeSet toTypes(final Type type, Predicate<? super Type> removalPredicate) {
    final Map<Type, Type> resolvedTypes = new HashMap<>();
    toTypes(type, isRawClass(type), resolvedTypes);
    resolvedTypes.keySet()
      .removeIf(k -> !(k instanceof Class) || removalPredicate != null && removalPredicate.test(resolvedTypes.get(k)));
    return new TypeSet(resolvedTypes.values());
  }

  private static final void toTypes(final Type type,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    if (type == null) {
      // Do nothing on purpose
    } else if (type instanceof Class<?> cls) {
      toTypes(cls, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof ParameterizedType parameterizedType) {
      toTypes(parameterizedType, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof GenericArrayType genericArrayType) {
      toTypes(genericArrayType, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof TypeVariable<?> typeVariable) {
      // Do nothing on purpose
    } else if (type instanceof WildcardType wildcardType) {
      // Do nothing on purpose
    } else {
      throw new IllegalArgumentException("Unexpected type: " + type);
    }
  }

  private static final void toTypes(final Class<?> type,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    resolvedTypes.put(type, resolve(type, resolvedTypes::get));
    final Type superclass = noParameterizedTypes ? type.getSuperclass() : type.getGenericSuperclass();
    if (superclass != null) {
      toTypes(superclass, noParameterizedTypes, resolvedTypes);
    }
    final Type[] interfaces = noParameterizedTypes ? type.getInterfaces() : type.getGenericInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      toTypes(interfaces[i], noParameterizedTypes, resolvedTypes);
    }
  }

  private static final void toTypes(final ParameterizedType type,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    final Class<?> rawType = toRawType(type);
    if (rawType != null) {
      final TypeVariable<?>[] typeVariables = rawType.getTypeParameters();
      final Type[] typeVariableValues = type.getActualTypeArguments();
      assert typeVariables.length == typeVariableValues.length;
      for (int i = 0; i < typeVariables.length; i++) {
        resolvedTypes.put(typeVariables[i], resolve(typeVariableValues[i], resolvedTypes::get));
      }
      resolvedTypes.put(rawType, resolve(type, resolvedTypes::get));
      toTypes(rawType, noParameterizedTypes, resolvedTypes);
    }
  }

  private static final void toTypes(final GenericArrayType type,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    final Class<?> rawType = toRawType(type);
    if (rawType != null) {
      final Class<?> arrayType = Array.newInstance(rawType, 0).getClass();
      resolvedTypes.put(arrayType, type);
      toTypes(arrayType, noParameterizedTypes, resolvedTypes);
    }
  }

  /**
   * Returns the result of <a
   * href="https://docs.oracle.com/javase/specs/jls/se13/html/jls-5.html#jls-5.1.7">boxing
   * conversion</a> on the supplied {@link Type}.
   *
   * <p>In most cases this means the supplied {@link Type} is returned
   * unchanged.</p>
   *
   * <p>This method calls {@link #isPrimitive(Type)} as part of its
   * execution.</p>
   *
   * @param <T> the kind of {@link Type} in question
   *
   * @param type the {@link Type} to test
   *
   * @return the result of <a
   * href="https://docs.oracle.com/javase/specs/jls/se13/html/jls-5.html#jls-5.1.7">boxing
   * conversion</a> on the supplied {@link Type}
   *
   * @nullability This method will return {@code null} if {@code type}
   * is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.
   *
   * @see #isPrimitive(Type)
   */
  @SuppressWarnings("unchecked")
  public static final <T extends Type> T box(final T type) {
    // We don't just blindly look up in wrapperTypes.  We call
    // isPrimitive() first to potentially avoid the Map lookup.
    return isPrimitive(type) ? (T)wrapperTypes.getOrDefault(type, type) : type;
  }

  /**
   * Returns {@code true} if and only if {@code type} is a {@link
   * Class} and {@linkplain Class#isPrimitive() is primitive}.
   *
   * @param type the {@link Type} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if and only if {@code type} is a {@link
   * Class} and {@linkplain Class#isPrimitive() is primitive}
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.
   */
  public static final boolean isPrimitive(final Type type) {
    return type instanceof Class<?> cls && cls.isPrimitive();
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Type} is
   * a raw {@link Class}.
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if if and only if the supplied {@link Type}
   * is a raw {@link Class}
   */
  public static final boolean isRawClass(final Type type) {
    return type instanceof Class<?> cls && (isRawClass(cls.getComponentType()) || cls.getTypeParameters().length > 0);
  }

  /**
   * Does what is necessary to extract a {@link Class} from the
   * supplied {@link Type} if at all possible.
   *
   * <ul>
   *
   * <li>If a {@link Class} is supplied, the {@link Class} is simply
   * returned.</li>
   *
   * <li>If a {@link ParameterizedType} is supplied, the result of
   * invoking {@link #toRawType(Type)} on its {@linkplain
   * ParameterizedType#getRawType() raw type} is returned.</li>
   *
   * <li>If a {@link GenericArrayType} is supplied, the result of
   * invoking {@link #toRawType(Type)} on its {@linkplain
   * GenericArrayType#getGenericComponentType() generic component
   * type} is returned.</li>
   *
   * <li>If a {@link TypeVariable} is supplied, the result of invoking
   * {@link #toRawType(Type)} on its {@linkplain
   * TypeVariable#getBounds() first bound} is returned (if it has one)
   * or {@link Object Object.class} if it does not.</li>
   *
   * <li>If a {@link WildcardType} is supplied, the result of invoking
   * {@link #toRawType(Type)} on its {@linkplain
   * WildcardType#getUpperBounds() first upper bound} is
   * returned.</li>
   *
   * </ul>
   *
   * @param type the {@link Type} for which a {@link Class} is to be
   * returned; may be {@code null} in which case {@code null} will be
   * returned
   *
   * @return a {@link Class}, or {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by mutltiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final Class<?> toRawType(final Type type) {
    final Class<?> returnValue;
    if (type == null) {
      returnValue = null;
    } else if (type instanceof Class<?> cls) {
      returnValue = toRawType(cls);
    } else if (type instanceof ParameterizedType parameterizedType) {
      returnValue = toRawType(parameterizedType);
    } else if (type instanceof GenericArrayType genericArrayType) {
      returnValue = toRawType(genericArrayType);
    } else if (type instanceof TypeVariable<?> typeVariable) {
      returnValue = toRawType(typeVariable);
    } else if (type instanceof WildcardType wildcardType) {
      returnValue = toRawType(wildcardType);
    } else {
      returnValue = null;
    }
    return returnValue;
  }

  private static final Class<?> toRawType(final Class<?> type) {
    return type;
  }

  private static final Class<?> toRawType(final ParameterizedType type) {
    return type == null ? null : toRawType(type.getRawType());
  }

  private static final Class<?> toRawType(final GenericArrayType type) {
    final Class<?> candidate = type == null ? null : toRawType(type.getGenericComponentType());
    return candidate == null ? null : (Class<?>)Array.newInstance(candidate, 0).getClass();
  }

  private static final Class<?> toRawType(final TypeVariable<?> type) {
    final Type[] bounds = type.getBounds();
    return hasBound(bounds) ? toRawType(bounds[0]) : Object.class;
  }

  private static final Class<?> toRawType(final WildcardType type) {
    final Type[] bounds = type.getUpperBounds();
    return hasBound(bounds) ? toRawType(bounds[0]) : Object.class;
  }

  private static final boolean hasBound(final Type[] bounds) {
    return bounds != null && bounds.length > 0;
  }


  /*
   * Inner and nested classes.
   */


  private static final class TypeNameComparator implements Comparator<Type> {

    @Override
    public final int compare(final Type t1, final Type t2) {
      return t1.equals(t2) ? 0 : t1.getTypeName().compareTo(t2.getTypeName());
    }

  }

}

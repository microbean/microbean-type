/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2020–2021 microBean™.
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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

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


  static final Comparator<Type> typeComparator = new TypeComparator();

  private static final Map<Class<?>, Class<?>> wrapperTypes;

  static {
    final Map<Class<?>, Class<?>> map = new IdentityHashMap<>(9);
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
    if (type instanceof Class) {
      final Class<?> cls = (Class<?>)type;
      final Type[] typeParameters = cls.getTypeParameters();
      if (typeParameters.length <= 0) {
        final Type componentType = cls.getComponentType();
        if (componentType == null) {
          return type;
        } else {
          final Type normalizedComponentType = normalize(componentType); // XXX recursive
          return componentType == normalizedComponentType ? type : new DefaultGenericArrayType(normalizedComponentType);
        }
      } else {
        return new DefaultParameterizedType(cls.getDeclaringClass(), cls, typeParameters);
      }
    } else {
      return type;
    }
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Type} is
   * a <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.8"
   * target="_parent">raw type</a>.
   *
   * <p>This method returns {@code true} if:</p>
   *
   * <ul>
   *
   * <li>The supplied {@link Type} is an instance of {@link Class},
   * <strong>and any</strong> of the following conditions is true:
   *
   * <ul>
   *
   * <li>The return value of invoking this method on the {@linkplain
   * Class class}'s {@linkplain Class#getComponentType() component
   * type} is {@code true}</li>
   *
   * <li>The {@linkplain Class class} {@linkplain
   * Class#getTypeParameters() has type parameters}</li>
   *
   * </ul>
   *
   * </li>
   *
   * </ul>
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if and only if the supplied {@link Type} is
   * a raw {@link Class}
   */
  public static final boolean isRawType(final Type type) {
    return type instanceof Class && isRawType((Class<?>)type);
  }

  private static final boolean isRawType(final Class<?> c) {
    if (c == null) {
      return false;
    } else if (isGeneric(c) || isRawType(c.getComponentType())) {
      return true;
    } else {
      // See https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.8
      final Class<?> dc = c.getDeclaringClass();
      // Don't recurse, because it's only the immediate parent that
      // we're supposed to test
      return dc != null && dc.getTypeParameters().length > 0;
    }
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Class} is
   * <em>generic</em> according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.2"
   * target="_parent">the rules of the Java Language
   * Specification</a>.
   *
   * @param cls the {@link Class} in question; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Class} is non-{@code
   * null} and returns an array with more than zero elements from an
   * invocation of its {@link Class#getTypeParameters()} method;
   * {@code false} otherwise
   *
   * @see Class#getTypeParameters()
   *
   * @see <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.2"
   * target="_parent">The Java Language Specification Section
   * 8.1.2</a>
   */
  // https://docs.oracle.com/javase/specs/jls/se11/html/jls-8.html#jls-8.1.2
  // "A class is generic if the class declaration declares one or more
  // type variables (§4.4)."
  public static final boolean isGeneric(final Class<?> cls) {
    return cls != null && cls.getTypeParameters().length > 0;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Type}
   * represents a <em>reference type</em> according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.3"
   * target="_parent">the rules of the Java Language
   * Specification</a>.
   *
   * @param type the {@link Type} in question; may be {@code null} in which case
   * {@code false} will be returned
   *
   * @return {@code true} if and only if the supplied {@link Type}
   * represents a <em>reference type</em> according to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.3"
   * target="_parent">the rules of the Java Language
   * Specification</a>
   *
   * @see <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.3"
   * target="_parent">The Java Language Specification Section 4.3</a>
   */
  public static final boolean isReferenceType(final Type type) {
    return
      (type instanceof Class && !((Class<?>)type).isPrimitive()) ||
      type instanceof ParameterizedType ||
      type instanceof GenericArrayType ||
      type instanceof TypeVariable ||
      type instanceof WildcardType; // wildcard types in the JLS are unsound type variables
  }


  /*
   * Bookmark: resolve(Type)
   */


  /**
   * Resolves the supplied {@link Type} and returns the result, or
   * the {@link Type} itself if the {@link Type} could not be resolved.
   *
   * <p>Type resolution is delegated to the supplied {@link Function}
   * which is most commonly a method reference to the {@link
   * Map#get(Object)} method of a {@link Map Map&lt;Type, Type&gt;}.
   * Generally speaking, however, the type resolver may take any
   * side-effect-free action it wishes.</p>
   *
   * @param type the {@link Type} to resolve; may be {@code null}
   *
   * @param typeResolver the {@link Function} whose {@link
   * Function#apply(Object)} method will be called on, among other
   * things, {@link ParameterizedType} {@linkplain
   * ParameterizedType#getActualTypeArguments() type arguments}; must
   * not be {@code null}; may return {@code null}; will never be
   * supplied with a {@code null} {@link Type}
   *
   * @return the resolved {@link Type}, or {@code null}
   *
   * @exception NullPointerException if {@code typeResolver} is {@code
   * null}
   *
   * @nullability This method will return {@code null} only when the
   * supplied {@link Type} is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final Type resolve(Type type,
                                   final Function<? super Type, ? extends Type> typeResolver) {
    if (type == null) {
      return null;
    } else if (type instanceof Class) {
      return resolve((Class<?>)type, typeResolver);
    } else if (type instanceof ParameterizedType) {
      return resolve((ParameterizedType)type, typeResolver);
    } else if (type instanceof GenericArrayType) {
      return resolve((GenericArrayType)type, typeResolver);
    } else if (type instanceof TypeVariable) {
      return resolve((TypeVariable<?>)type, typeResolver);
    } else if (type instanceof WildcardType) {
      return resolve((WildcardType)type, typeResolver);
    } else {
      // (Unknown type, so we don't know how to resolve it.)
      return type;
    }
  }

  private static final Type resolve(final Class<?> type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }

  private static final Type resolve(final ParameterizedType type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    if (candidate == null) {
      final Type[] actualTypeArguments = type.getActualTypeArguments();
      final int length = actualTypeArguments.length;
      final Type[] resolvedActualTypeArguments = new Type[length];
      boolean createNewType = false;
      for (int i = 0; i < length; i++) {
        final Type actualTypeArgument = actualTypeArguments[i];
        final Type resolvedActualTypeArgument = resolve(actualTypeArgument, typeResolver); // XXX recursive
        resolvedActualTypeArguments[i] = resolvedActualTypeArgument;
        if (!createNewType && actualTypeArgument != resolvedActualTypeArgument) {
          // If they're not the same object reference, then resolution
          // did something, so we have to return a new type, not the
          // one we were handed.
          createNewType = true;
        }
      }
      return
        createNewType ? new DefaultParameterizedType(type.getOwnerType(), type.getRawType(), resolvedActualTypeArguments) : type;
    } else {
      return candidate;
    }

  }

  private static final Type resolve(final GenericArrayType type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    if (candidate == null) {
      final Type genericComponentType = type.getGenericComponentType();
      final Type resolvedComponentType = resolve(genericComponentType, typeResolver); // XXX recursive
      if (resolvedComponentType == null || resolvedComponentType == genericComponentType) {
        // Identity means that basically resolution was a no-op, so the
        // genericComponentType was whatever it was, and so we are going
        // to be a no-op as well.
        return type;
      } else if (resolvedComponentType instanceof Class) {
        // This might happen when genericComponentType was a
        // TypeVariable.  In this case, it might get resolved to a
        // simple scalar (e.g. Integer.class).  Now we have,
        // effectively, a GenericArrayType whose genericComponentType is
        // just a plain class.  That's actually just an ordinary Java
        // array, so "resolve" this type by returning its array
        // equivalent.
        return Array.newInstance((Class<?>)resolvedComponentType, 0).getClass();
      } else {
        // If we get here, we know that resolution actually did
        // something, so return a new GenericArrayType implementation
        // whose component type is the resolved version of the
        // original's.
        return new DefaultGenericArrayType(resolvedComponentType);
      }
    } else {
      return candidate;
    }
  }

  private static final Type resolve(final TypeVariable<?> type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
    final Type candidate = typeResolver.apply(type);
    return candidate == null ? type : candidate;
  }

  private static final Type resolve(final WildcardType type,
                                    final Function<? super Type, ? extends Type> typeResolver) {
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
   * @return a non-{@code null} {@link TypeSet}; the supplied {@link
   * Type} will be one of its elements unless it is a {@link
   * TypeVariable} or a {@link WildcardType}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent.
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
   * @return a non-{@code null} {@link TypeSet}, filtered by the
   * supplied {@link Predicate}
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
    toTypes(type, isRawType(type), resolvedTypes);
    resolvedTypes.keySet().removeIf(removalPredicate == null ?
                                    Predicate.not(Types::isClass) :
                                    k -> !isClass(k) || removalPredicate.test(resolvedTypes.get(k)));
    return resolvedTypes.isEmpty() ? TypeSet.EMPTY_TYPESET : new TypeSet(resolvedTypes.values());
  }

  private static final void toTypes(final Type type,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    if (type == null) {
      // Do nothing on purpose
    } else if (type instanceof Class) {
      toTypes((Class<?>)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof ParameterizedType) {
      toTypes((ParameterizedType)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof GenericArrayType) {
      toTypes((GenericArrayType)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof TypeVariable) {
      toTypes((TypeVariable<?>)type, noParameterizedTypes, resolvedTypes);
    } else if (type instanceof WildcardType) {
      toTypes((WildcardType)type, noParameterizedTypes, resolvedTypes);
    } else {
      throw new IllegalArgumentException("Unexpected type: " + type);
    }
  }

  private static final void toTypes(final Class<?> cls,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    resolvedTypes.put(cls, resolve(cls, resolvedTypes::get));
    final Type superclass = noParameterizedTypes ? cls.getSuperclass() : cls.getGenericSuperclass();
    if (superclass != null) {
      toTypes(superclass, noParameterizedTypes || isRawType(superclass), resolvedTypes);
    }
    final Type[] interfaces = noParameterizedTypes ? cls.getInterfaces() : cls.getGenericInterfaces();
    for (final Type iface : interfaces) {
      toTypes(iface, noParameterizedTypes || isRawType(iface), resolvedTypes);
    }
  }

  private static final void toTypes(final ParameterizedType parameterizedType,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    final Class<?> rawType = erase(parameterizedType);
    if (rawType != null) {
      if (!noParameterizedTypes) {
        final TypeVariable<?>[] typeVariables = rawType.getTypeParameters();
        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        assert typeVariables.length == typeArguments.length;
        for (int i = 0; i < typeVariables.length; i++) {
          resolvedTypes.put(typeVariables[i], resolve(typeArguments[i], resolvedTypes::get));
        }
      }
      resolvedTypes.put(rawType, resolve(parameterizedType, resolvedTypes::get));
      toTypes(rawType, noParameterizedTypes, resolvedTypes);
    }
  }

  private static final void toTypes(final GenericArrayType genericArrayType,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    final Class<?> rawType = erase(genericArrayType);
    if (rawType != null) {
      assert rawType.isArray() : "Not an array type; check erase(GenericArrayType): " + rawType;
      resolvedTypes.put(rawType, genericArrayType);
      toTypes(rawType, noParameterizedTypes, resolvedTypes);
    }
  }

  private static final void toTypes(final TypeVariable<?> typeVariable,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    // Do nothing on purpose.
  }

  private static final void toTypes(final WildcardType wildcardType,
                                    final boolean noParameterizedTypes,
                                    final Map<Type, Type> resolvedTypes) {
    // Do nothing on purpose.
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
   * @idempotency This method is idempotent and deterministic.
   *
   * @see #isPrimitive(Type)
   */
  @SuppressWarnings("unchecked")
  public static final <T extends Type> T box(final T type) {
    return
      type instanceof Class && ((Class<?>)type).isPrimitive() ?
      (T)wrapperTypes.getOrDefault(type, (Class<?>)type) :
      type;
  }

  /**
   * Returns {@code true} if and only if {@code type} is a {@link
   * Class} and {@linkplain Class#isPrimitive() is primitive}.
   *
   * <h2>Design Notes</h2>
   *
   * <p>This prosaic method exists because various {@link Predicate}s
   * need to exist that test this very thing, and making it {@code
   * public} does no harm.</p>
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
   * @idempotency This method is idempotent and deterministic.
   */
  public static final boolean isPrimitive(final Type type) {
    return type instanceof Class && ((Class<?>)type).isPrimitive();
  }

  /**
   * Returns {@code true} if the supplied {@link Type} is an instance
   * of {@link Class}.
   *
   * <h2>Design Notes</h2>
   *
   * <p>This prosaic method exists because various {@link Predicate}s
   * need to exist that test this very thing, and making it {@code
   * public} does no harm.</p>
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if and only if {@code type} is an instance
   * of {@link Class}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @since 0.0.6
   */
  public static final boolean isClass(final Type type) {
    return type instanceof Class;
  }

  /**
   * Returns the type erasure for the supplied {@link Type} according
   * to <a
   * href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-4.html#jls-4.6"
   * target="_parent">the rules of the Java Language
   * Specification</a>.
   *
   * <ul>
   *
   * <li>If {@code null} is supplied, {@code null} is returned.</li>
   *
   * <li>If a {@link Class} is supplied, the {@link Class} is returned.</li>
   *
   * <li>If a {@link ParameterizedType} is supplied, the result of
   * invoking {@link #erase(Type)} on its {@linkplain
   * ParameterizedType#getRawType() raw type} is returned.</li>
   *
   * <li>If a {@link GenericArrayType} is supplied, the result of
   * invoking {@link Object#getClass()} on an invocation of {@link
   * Array#newInstance(Class, int)} with the return value of an
   * invocation of {@link #erase(Type)} on its {@linkplain
   * GenericArrayType#getGenericComponentType() generic component
   * type} and {@code 0} as its arguments is returned.</li>
   *
   * <li>If a {@link TypeVariable} is supplied, the result of invoking
   * {@link #erase(Type)} <strong>on its {@linkplain
   * TypeVariable#getBounds() first (leftmost) bound}</strong> is
   * returned (if it has one) or {@link Object Object.class} if it
   * does not.  <strong>Any other bounds are ignored.</strong></li>
   *
   * <li>If a {@link WildcardType} is supplied, the result of invoking
   * {@link #erase(Type)} <strong>on its {@linkplain
   * WildcardType#getUpperBounds() first upper bound}</strong> is
   * returned.  <strong>Any other bounds are ignored.</strong></li>
   *
   * </ul>
   *
   * @param type the {@link Type} for which the corresponding type
   * erasure is to be returned; may be {@code null} in which case
   * {@code null} will be returned
   *
   * @return a {@link Class}, or {@code null} if a suitable type
   * erasure could not be determined
   *
   * @nullability This method may return {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by mutltiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  // Trying to implement
  // https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.6.
  public static final Class<?> erase(final Type type) {
    final Class<?> returnValue;
    if (type == null) {
      return null;
    } else if (type instanceof Class) {
      return erase((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      return erase((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return erase((GenericArrayType)type);
    } else if (type instanceof TypeVariable) {
      return erase((TypeVariable<?>)type);
    } else if (type instanceof WildcardType) {
      return erase((WildcardType)type);
    } else {
      return null;
    }
  }

  private static final Class<?> erase(final Class<?> type) {
    return type;
  }

  private static final Class<?> erase(final ParameterizedType type) {
    return type == null ? null : erase(type.getRawType());
  }

  private static final Class<?> erase(final GenericArrayType type) {
    final Class<?> candidate = type == null ? null : erase(type.getGenericComponentType());
    return candidate == null ? null : (Class<?>)Array.newInstance(candidate, 0).getClass();
  }

  private static final Class<?> erase(final TypeVariable<?> type) {
    final Type[] bounds = type == null ? null : type.getBounds();
    return bounds != null && bounds.length > 0 ? erase(bounds[0]) : Object.class;
  }

  private static final Class<?> erase(final WildcardType type) {
    final Type[] bounds = type == null ? null : type.getUpperBounds();
    return bounds != null && bounds.length > 0 ? erase(bounds[0]) : Object.class;
  }

  /**
   * Returns the <em>effective bounds</em> of the supplied {@link
   * TypeVariable}.
   *
   * <p>The effective bounds of a {@link TypeVariable} are one of the
   * following:</p>
   *
   * <ul>
   *
   * <li>The (recursive) effective bounds of its sole {@link
   * TypeVariable}-typed bound</li>
   *
   * <li>Its non-{@link TypeVariable}-typed bounds</li>
   *
   * </ul>
   *
   * @param type the {@link TypeVariable} in question; must not be {@code null}
   *
   * @return a non-{@code null} array of {@link Type}s representing
   * the supplied {@link TypeVariable}'s effective bounds with one or
   * more elements
   *
   * @exception NullPointerException if {@code type} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final Type[] getEffectiveBounds(final TypeVariable<?> type) {
    final Type[] bounds = type.getBounds();
    if (bounds == null || bounds.length <= 0) {
      return new Type[] { Object.class };
    } else if (bounds.length == 1) {
      final Type soleBound = bounds[0];
      if (soleBound instanceof TypeVariable) {
        return getEffectiveBounds((TypeVariable<?>)soleBound);
      } else {
        return new Type[] { soleBound };
      }
    } else {
      return bounds.clone();
    }
  }

  /**
   * Returns a {@link Serializable} {@link Type} that represents the
   * supplied {@link Type}.
   *
   * @param <T> a {@link Serializable} {@link Type}
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code null} will be returned
   *
   * @return a {@link Serializable} version of the supplied {@link
   * Type}
   *
   * @exception IllegalArgumentException if {@code type} is non-{@code
   * null} and is not an instance of {@link Serializable}, {@link
   * ParameterizedType}, {@link GenericArrayType}, {@link
   * TypeVariable} or {@link WildcardType}
   *
   * @nullability This method will return {@code null} only when the
   * supplied {@link Type} is {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  @SuppressWarnings("unchecked")
  public static final <T extends Serializable & Type> T toSerializableType(final Type type) {
    if (type == null || type instanceof Serializable) {
      return (T)type;
    } else if (type instanceof ParameterizedType) {
      return (T)DefaultParameterizedType.valueOf((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return (T)DefaultGenericArrayType.valueOf((GenericArrayType)type);
    } else if (type instanceof TypeVariable) {
      return (T)DefaultTypeVariable.valueOf((TypeVariable<? extends GenericDeclaration>)type);
    } else if (type instanceof WildcardType) {
      return (T)AbstractWildcardType.valueOf((WildcardType)type);
    } else {
      throw new IllegalArgumentException("Unexpected type: " + type);
    }
  }

  /**
   * Computes and returns a hashcode for the supplied {@link Type} independent of
   * its implementation.
   *
   * @param type the {@link Type} in question; may be {@code null} in
   * which case {@code 0} will be returned
   *
   * @return a hashcode for the supplied {@link Type}
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final int hashCode(final Type type) {
    if (type == null) {
      return 0;
    } else if (type instanceof Class<?>) {
      return hashCode((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      return hashCode((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return hashCode((GenericArrayType)type);
    } else if (type instanceof TypeVariable) {
      return hashCode((TypeVariable<?>)type);
    } else if (type instanceof WildcardType) {
      return hashCode((WildcardType)type);
    } else {
      return type.hashCode();
    }
  }

  private static final int hashCode(final Class<?> type) {
    return type == null ? 0 : type.hashCode();
  }

  private static final int hashCode(final ParameterizedType type) {
    if (type == null) {
      return 0;
    } else {
      return Arrays.hashCode(type.getActualTypeArguments()) ^ hashCode(type.getOwnerType()) ^ hashCode(type.getRawType());
    }
  }

  private static final int hashCode(final GenericArrayType type) {
    return type == null ? 0 : Objects.hashCode(type.getGenericComponentType());
  }

  private static final int hashCode(final TypeVariable<?> type) {
    return type == null ? 0 : type.hashCode();
  }

  private static final int hashCode(final WildcardType type) {
    return type == null ? 0 : Arrays.hashCode(type.getUpperBounds()) ^ Arrays.hashCode(type.getLowerBounds());
  }

  /**
   * Tests two {@link Type}s for equality in a manner that is
   * independent of their implementations.
   *
   * @param type0 the first {@link Type}; may be {@code null}
   *
   * @param type1 the second {@link Type}; may be {@code null}
   *
   * @return {@code true} if the two {@link Type}s are equal; {@code
   * false} otherwise
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final boolean equals(final Type type0, final Type type1) {
    if (type0 == null) {
      return type1 == null;
    } else if (type1 == null) {
      return false;
    } else if (type0 == type1) {
      return true;
    } else if (type0 instanceof Class) {
      return type1 instanceof Class && equals((Class<?>)type0, (Class<?>)type1);
    } else if (type0 instanceof ParameterizedType) {
      return type1 instanceof ParameterizedType && equals((ParameterizedType)type0, (ParameterizedType)type1);
    } else if (type0 instanceof GenericArrayType) {
      return type1 instanceof GenericArrayType && equals((GenericArrayType)type0, (GenericArrayType)type1);
    } else if (type0 instanceof TypeVariable) {
      return type1 instanceof TypeVariable && equals((TypeVariable<?>)type0, (TypeVariable<?>)type1);
    } else if (type0 instanceof WildcardType) {
      return type1 instanceof WildcardType && equals((WildcardType)type0, (WildcardType)type1);
    } else {
      return Objects.equals(type0, type1);
    }
  }

  private static final boolean equals(final Type[] ts0, final Type[] ts1) {
    if (ts0 == null) {
      return ts1 == null;
    } else if (ts1 == null) {
      return false;
    } else if (ts0 == ts1) {
      return true;
    } else if (ts0.length == ts1.length) {
      for (final Type t0 : ts0) {
        for (final Type t1 : ts1) {
          if (!equals(t0, t1)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private static final boolean equals(final Class<?> c0, final Class<?> c1) {
    return Objects.equals(c0, c1);
  }

  private static final boolean equals(final ParameterizedType pt0, final ParameterizedType pt1) {
    if (pt0 == null) {
      return pt1 == null;
    } else if (pt1 == null) {
      return false;
    } else if (pt0 == pt1) {
      return true;
    } else {
      return
        equals(pt0.getOwnerType(), pt1.getOwnerType()) &&
        equals(pt0.getRawType(), pt1.getRawType()) &&
        equals(pt0.getActualTypeArguments(), pt1.getActualTypeArguments());
    }
  }

  private static final boolean equals(final GenericArrayType ga0, final GenericArrayType ga1) {
    if (ga0 == null) {
      return ga1 == null;
    } else if (ga1 == null) {
      return false;
    } else if (ga0 == ga1) {
      return true;
    } else {
      return equals(ga0.getGenericComponentType(), ga1.getGenericComponentType());
    }
  }

  private static final boolean equals(final TypeVariable<?> tv0, final TypeVariable<?> tv1) {
    if (tv0 == null) {
      return tv1 == null;
    } else if (tv1 == null) {
      return false;
    } else if (tv0 == tv1) {
      return true;
    } else {
      return
        equals(tv0.getGenericDeclaration(), tv1.getGenericDeclaration()) &&
        Objects.equals(tv0.getName(), tv1.getName());
    }
  }

  private static final boolean equals(final GenericDeclaration gd0, final GenericDeclaration gd1) {
    if (gd0 == null) {
      return gd1 == null;
    } else if (gd1 == null) {
      return false;
    } else if (gd0 == gd1) {
      return true;
    } else if (gd0 instanceof Class) {
      return gd1 instanceof Class && equals((Class<?>)gd0, (Class<?>)gd1);
    } else if (gd0 instanceof Executable) {
      return gd1 instanceof Executable && equals((Executable)gd0, (Executable)gd1);
    } else {
      return Objects.equals(gd0, gd1);
    }
  }

  private static final boolean equals(final Executable e0, final Executable e1) {
    if (e0 == null) {
      return e1 == null;
    } else if (e1 == null) {
      return false;
    } else if (e0 == e1) {
      return true;
    } else if (e0 instanceof Constructor) {
      return e1 instanceof Constructor && equals((Constructor<?>)e0, (Constructor<?>)e1);
    } else if (e0 instanceof Method) {
      return e1 instanceof Method && equals((Method)e0, (Method)e1);
    } else {
      return Objects.equals(e0, e1);
    }
  }

  private static final boolean equals(final Constructor<?> c0, final Constructor<?> c1) {
    return Objects.equals(c0, c1);
  }

  private static final boolean equals(final Method m0, final Method m1) {
    return Objects.equals(m0, m1);
  }

  private static final boolean equals(final WildcardType w0, final WildcardType w1) {
    if (w0 == null) {
      return w1 == null;
    } else if (w1 == null) {
      return false;
    } else if (w0 == w1) {
      return true;
    } else {
      return
        equals(w0.getLowerBounds(), w1.getLowerBounds()) &&
        equals(w1.getUpperBounds(), w1.getUpperBounds());
    }
  }

  /**
   * Returns a {@link String} representation of the supplied {@link
   * Type} that is independent of its implementation.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param type the {@link Type} in question; may be {@code null} in which
   * case "{@code null}" will be returned
   *
   * @return a non-{@code null} {@link String} representation of the
   * supplied {@link Type}
   *
   * @nullability This method never returns {@code null}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @idempotency This method is idempotent and deterministic.
   */
  public static final String toString(final Type type) {
    if (type == null) {
      return "null";
    } else if (type instanceof Class) {
      return toString((Class<?>)type);
    } else if (type instanceof ParameterizedType) {
      return toString((ParameterizedType)type);
    } else if (type instanceof GenericArrayType) {
      return toString((GenericArrayType)type);
    } else if (type instanceof TypeVariable) {
      return toString((TypeVariable<?>)type);
    } else if (type instanceof WildcardType) {
      return toString((WildcardType)type);
    } else {
      return type.getTypeName();
    }
  }

  private static final String toString(final Class<?> c) {
    return c == null ? "null" : c.getName();
  }

  private static final String toString(final ParameterizedType ptype) {
    if (ptype == null) {
      return "null";
    } else {
      final StringBuilder sb = new StringBuilder();
      final Type ownerType = ptype.getOwnerType();
      if (ownerType == null) {
        sb.append(toString(ptype.getRawType()));
      } else {
        sb.append(toString(ownerType)).append("$");
        final Type rawType = ptype.getRawType();
        if (ownerType instanceof ParameterizedType) {
          sb.append(toString(rawType).replace(toString(((ParameterizedType)ownerType).getRawType()) + "$", ""));
        } else if (rawType instanceof Class) {
          sb.append(((Class<?>)rawType).getSimpleName());
        } else {
          sb.append(toString(rawType));
        }
      }
      final Type[] actualTypeArguments = ptype.getActualTypeArguments();
      if (actualTypeArguments != null && actualTypeArguments.length > 0) {
        final StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
        stringJoiner.setEmptyValue("");
        for (final Type actualTypeArgument : actualTypeArguments) {
          stringJoiner.add(toString(actualTypeArgument));
        }
        sb.append(stringJoiner.toString());
      }
      return sb.toString();
    }
  }

  private static final String toString(final GenericArrayType gatype) {
    if (gatype == null) {
      return "null";
    } else {
      return toString(gatype.getGenericComponentType()) + "[]";
    }
  }

  private static final String toString(final TypeVariable<?> tv) {
    return tv == null ? "null" : tv.getName();
  }

  private static final String toString(final WildcardType wc) {
    if (wc == null) {
      return "null";
    } else {
      final StringBuilder sb = new StringBuilder("?");
      Type[] bounds = wc.getLowerBounds();
      if (bounds == null || bounds.length <= 0) {
        // Upper bounds only.
        bounds = wc.getUpperBounds();
        if (bounds == null || bounds.length <= 0 || Object.class.equals(bounds[0])) {
          bounds = null;
        } else {
          sb.append(" extends ");
        }
      } else {
        // Lower bounds only.
        sb.append(" super ");
      }
      if (bounds != null) {
        assert bounds.length > 0;
        final StringJoiner sj = new StringJoiner(" & ");
        for (final Type bound: bounds) {
          sj.add(String.valueOf(toString(bound)));
        }
        sb.append(sj.toString());
      }
      return sb.toString();
    }
  }


  /*
   * Inner and nested classes.
   */


  private static final class TypeComparator implements Comparator<Type> {

    @Override
    public final int compare(final Type t1, final Type t2) {
      if (t1 == null) {
        return t2 == null ? 0 : 1; // nulls go to the right
      } else if (t2 == null) {
        return -1; // nulls go to the right
      } else if (t1 == t2 || Types.equals(t1, t2)) {
        return 0;
      } else {
        return Types.toString(t1).compareTo(Types.toString(t2));
      }
    }

  }


}

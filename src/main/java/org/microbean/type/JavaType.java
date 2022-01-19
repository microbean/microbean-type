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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import java.util.function.UnaryOperator;

import org.microbean.development.annotation.Convenience;

import static org.microbean.type.JavaTypes.erase;

public final class JavaType implements org.microbean.type.Type {


  /*
   * Static fields.
   */


  public static final Map<Type, Class<?>> wrapperTypes =
    Map.of(boolean.class, Boolean.class,
           byte.class, Byte.class,
           char.class, Character.class,
           double.class, Double.class,
           float.class, Float.class,
           int.class, Integer.class,
           long.class, Long.class,
           short.class, Short.class,
           void.class, Void.class);

  private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];


  /*
   * Instance fields.
   */


  private final Type type;


  /*
   * Constructors.
   */


  private JavaType(final Type type) {
    super();
    this.type = Objects.requireNonNull(type, "type");
  }


  /*
   * Instance methods.
   */


  public final Type type() {
    return this.type;
  }

  @Override // Type
  public final int hashCode() {
    return JavaTypes.hashCode(this.type);
  }

  @Override // Type
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && this.getClass() == other.getClass()) {
      return JavaTypes.equals(this.type, ((JavaType)other).type);
    } else {
      return false;
    }
  }


  /*
   * Static methods.
   */


  public static final JavaType of(final Type type) {
    return new JavaType(type);
  }


  /*
   * Methods for use by reference by Semantics.
   */


  private static final Type box(final Type type) {
    return type == void.class || type instanceof Class<?> c && c.isPrimitive() ? wrapperTypes.get(type) : type;
  }

  private static final Type type(final Type type) {
    if (type instanceof ParameterizedType p) {
      return type(p.getRawType());
    } else if (type instanceof GenericArrayType g) {
      return type(g.getGenericComponentType());
    } else {
      return type;
    }
  }

  private static final Type componentType(final Type type) {
    if (type instanceof Class<?> c) {
      return c.getComponentType();
    } else if (type instanceof GenericArrayType g) {
      return g.getGenericComponentType();
    } else {
      return null;
    }
  }

  private static final Type[] upperBounds(final Type type) {
    if (type instanceof TypeVariable<?> tv) {
      return tv.getBounds();
    } else if (type instanceof WildcardType w) {
      return w.getUpperBounds();
    } else {
      return EMPTY_TYPE_ARRAY;
    }
  }

  private static final boolean upperBounded(final Type type) {
    if (type instanceof TypeVariable) {
      return true;
    } else if (type instanceof WildcardType w) {
      return w.getLowerBounds().length <= 0;
    } else {
      return false;
    }
  }

  private static final Type lowerBound(final Type type) {
    if (type instanceof WildcardType w) {
      final Type[] lowerBounds = w.getLowerBounds();
      return lowerBounds.length > 0 ? lowerBounds[0] : null;
    } else {
      return null;
    }
  }

  private static final boolean lowerBounded(final Type type) {
    return type instanceof WildcardType w && w.getLowerBounds().length > 0;
  }

  private static final boolean named(final Type type) {
    return (type instanceof Class) || (type instanceof TypeVariable);
  }

  private static final boolean hasTypeArguments(final Type type) {
    return type instanceof ParameterizedType;
  }

  private static final Type[] typeArguments(final Type type) {
    return type instanceof ParameterizedType p ? p.getActualTypeArguments() : EMPTY_TYPE_ARRAY;
  }

  private static final boolean hasTypeParameters(final Type type) {
    return type instanceof Class<?> c && c.getTypeParameters().length > 0;
  }


  /*
   * Inner and nested classes.
   */


  public static final class Semantics extends org.microbean.type.Type.CovariantSemantics<java.lang.reflect.Type> {

    public Semantics(final boolean box) {
      super(JavaType::named,
            JavaTypes::equals,
            box ? JavaType::box : UnaryOperator.identity(),
            JavaType::of,
            JavaTypes::directSupertypes,
            JavaType::type,
            JavaType::hasTypeParameters,
            JavaType::hasTypeArguments,
            JavaType::typeArguments,
            JavaType::componentType,
            JavaType::upperBounded,
            JavaType::upperBounds,
            JavaType::lowerBounded,
            JavaType::lowerBound);
    }

  }

  /**
   * A holder of a {@link Type} that embodies <a
   * href="http://gafter.blogspot.com/2006/12/super-type-tokens.html"
   * target="_parent">Gafter's gadget</a>.
   *
   * <p>To use this class, create a new instance of an anonymous
   * subclass of it, and then call {@link #type() type()} on it:</p>
   *
   * <blockquote><pre>
   * // type will be a {@link ParameterizedType} whose {@link ParameterizedType#getRawType() rawType} is {@link java.util.List List.class} and
   * // whose {@linkplain ParameterizedType#getActualTypeArguments() sole type argument} is {@link String String.class}
   * Type type = new Token&lt;List&lt;String&gt;&gt;() {}.type();</pre></blockquote>
   *
   * @param <T> the modeled type; often parameterized
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see #type()
   */
  public static abstract class Token<T> implements AutoCloseable {


    /*
     * Static fields.
     */


    private static final ActualTypeArgumentExtractor actualTypeArgumentExtractor = new ActualTypeArgumentExtractor(Token.class, 0);


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link Token}.
     */
    protected Token() {
      super();
    }


    /*
     * Instance methods.
     */


    /**
     * Returns the {@link Type} modeled by this {@link Token}.
     *
     * @return the {@link Type} modeled by this {@link Token}; never
     * {@code null}
     *
     * @nullability This method never returns {@code null}.
     *
     * @threadsafety This method is safe for concurrent use by multiple
     * threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    public final Type type() {
      return actualTypeArgumentExtractor.get(this.getClass());
    }

    /**
     * A covenience method that clears any caches used by this {@link
     * Token}.
     *
     * <p>This method does not need to be called.</p>
     *
     * @threadsafety This method is safe for concurrent use by multiple
     * threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    @Override // AutoCloseable
    public final void close() {
      actualTypeArgumentExtractor.remove(this.getClass());
    }

    /**
     * Returns the {@linkplain #erase(Type) type erasure} of this {@link
     * Token}'s {@linkplain #type() modeled <code>Type</code>}, or
     * {@code null} if erasing the {@link Type} would result in a
     * non-{@link Class} erasure (in which case the erasure is simply
     * the {@link Type} itself), or if an erasure cannot be determined.
     *
     * @return the {@linkplain #erase(Type) type erasure} of this {@link
     * Token}'s {@linkplain #type() modeled <code>Type</code>}, or
     * {@code null} if erasing the {@link Type} would result in a
     * non-{@link Class} erasure, or if an erasure cannot be determined
     *
     * @nullability This method never returns {@code null}.
     *
     * @threadsafety This method is safe for concurrent use by multiple
     * threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    public final Class<?> erase() {
      return erase(this.type());
    }

    /**
     * Returns a hashcode for this {@link Token} computed from the
     * {@link Type} it {@linkplain #type() models}.
     *
     * @return a hashcode for this {@link Token}
     *
     * @threadsafety This method is, and its overrides must be, safe for
     * concurrent use by multiple threads.
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @see #equals(Object)
     */
    @Override // Object
    public int hashCode() {
      final Type type = this.type();
      return type == null ? 0 : type.hashCode();
    }

    /**
     * Returns {@code true} if the supplied {@link Object} is equal to
     * this {@link Token}.
     *
     * <p>This method returns {@code true} if the supplied {@link
     * Object}'s {@linkplain Object#getClass() class} is this {@link
     * Token}'s class and if its {@linkplain #type() modeled
     * <code>Type</code>} is equal to this {@link Token}'s
     * {@linkplain #type() modeled <code>Type</code>}.</p>
     *
     * @param other the {@link Object} to test; may be {@code null} in
     * which case {@code false} will be returned
     *
     * @return {@code true} if the supplied {@link Object} is equal to
     * this {@link Token}; {@code false} otherwise
     *
     * @threadsafety This method is, and its overrides must be, safe for
     * concurrent use by multiple threads.
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @see #hashCode()
     */
    @Override // Object
    public boolean equals(final Object other) {
      if (other == this) {
        return true;
      } else if (other instanceof Token<?> tt) {
        return Objects.equals(this.type(), tt.type());
      } else {
        return false;
      }
    }

    /**
     * Returns a {@link String} representation of this {@link
     * Token}.
     *
     * <p>This method returns a value equal to that returned by {@link
     * Type#getTypeName() this.type().getTypeName()}.</p>
     *
     * @return a {@link String} representation of this {@link
     * Token}; never {@code null}
     *
     * @nullability This method does not, and its overrides must not,
     * return {@code null}.
     *
     * @threadsafety This method is, and its overrides must be, safe for
     * concurrent use by multiple threads.
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     */
    @Override // Object
    public String toString() {
      final Type type = this.type();
      return type == null ? "null" : type.getTypeName();
    }


    /*
     * Static methods.
     */


    /**
     * Returns the <a
     * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.6"
     * target="_parent">type erasure</a> of the supplied {@link Type} as
     * a {@link Class}, or {@code null} if erasing the supplied {@link
     * Type} would result in a non-{@link Class} erasure (in which case
     * the erasure is simply the supplied {@link Type} itself), or if an
     * erasure cannot be determined.
     *
     * <p>If the supplied {@link Type} is an instance of {@link
     * Class}, {@link ParameterizedType}, {@link GenericArrayType},
     * {@link TypeVariable} or {@link WildcardType}, then the return
     * value of this method will be non-{@code null}.</p>
     *
     * @param type the {@link Type} to erase; may be {@code null} in
     * which case {@code null} will be returned
     *
     * @return the type erasure of the supplied {@link Type} as a {@link
     * Class}, or {@code null} if erasing the supplied {@link Type}
     * would result in a non-{@link Class} erasure, or if an erasure
     * cannot be determined
     *
     * @nullability This method may return {@code null}
     *
     * @threadsafety This method is safe for concurrent use by multiple
     * threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    @Convenience
    public static final Class<?> erase(final Type type) {
      return JavaTypes.erase(type);
    }

  }

  private static final class ActualTypeArgumentExtractor extends ClassValue<Type> {


    /*
     * Instance fields.
     */


    /**
     * The parameterized {@link Class} for which a type argument will
     * be supplied.
     *
     * @nullability This field is never {@code null}.
     */
    private final Class<?> stopClass;

    /**
     * The zero-based index of the type parameter for which a type
     * argument will be extracted.
     *
     * <p>This field will never be negative.</p>
     */
    private final int index;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link ActualTypeArgumentExtractor}.
     */
    private ActualTypeArgumentExtractor() {
      super();
      throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@link ActualTypeArgumentExtractor}.
     *
     * @param stopClass the parameterized {@link Class} for which a
     * type argument will be supplied; must not be {@code null}; must
     * be {@linkplain Modifier#isAbstract(int) abstract}; must not be
     * an {@linkplain Class#isArray() array} or an {@linkplain
     * Class#isInterface() interface}
     *
     * @param index the zero-based index of the type parameter in
     * {@code stopClass} for which an argument should be extracted;
     * must be greater than or equal to {@code 0} and less than the
     * number of {@linkplain Class#getTypeParameters() type parameters
     * in <code>stopClass</code>}
     *
     * @exception NullPointerException if {@code stopClass} is {@code
     * null}
     *
     * @exception IndexOutOfBoundsException if {@code index} is not
     * valid
     *
     * @exception IllegalArgumentException if {@code stopClass} is an
     * {@linkplain Class#isArray() array}, an {@linkplain
     * Class#isInterface() interface}, or not {@linkplain
     * Modifier#isAbstract(int) abstract}
     */
    private ActualTypeArgumentExtractor(final Class<?> stopClass, final int index) {
      super();
      if (index < 0 || index >= stopClass.getTypeParameters().length) {
        throw new IndexOutOfBoundsException(index);
      } else if (stopClass.isInterface() || stopClass.isArray() || !Modifier.isAbstract(stopClass.getModifiers())) {
        throw new IllegalArgumentException("stopClass: " + stopClass.getName());
      }
      this.stopClass = stopClass;
      this.index = index;
    }


    /*
     * Instance methods.
     */


    /**
     * Returns the type argument, as specified at {@linkplain
     * #ActualTypeArgumentExtractor(Class, int) construction time},
     * supplied to the supplied {@link Class} as a {@link Type}, or
     * {@code null} if such a type argument cannot be computed for any
     * reason.
     *
     * @param c the {@link Class} ultimately supplying a type
     * argument; may be {@code null} in which case {@code null} will
     * be returned
     *
     * @return the type argument, as specified at {@linkplain
     * #ActualTypeArgumentExtractor(Class, int) construction time},
     * supplied to the supplied {@link Class} as a {@link Type}, or
     * {@code null} if such a type argument cannot be computed for any
     * reason
     *
     * @nullability This method may return {@code null}.
     *
     * @threadsafety This method is safe for concurrent use by
     * multiple threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    @Override // ClassValue<Type>
    protected final Type computeValue(final Class<?> c) {
      final ParameterizedType p = this.mostSpecializedParameterizedSuperclass(c);
      return p == null ? null : p.getActualTypeArguments()[this.index];
    }

    private final ParameterizedType mostSpecializedParameterizedSuperclass(final Type type) {
      if (type == null || type == Object.class || type == this.stopClass) {
        return null;
      } else {
        final Class<?> erasure = erase(type);
        if (erasure == null || erasure == Object.class || !(this.stopClass.isAssignableFrom(erasure))) {
          return null;
        } else {
          return type instanceof ParameterizedType p ? p : this.mostSpecializedParameterizedSuperclass(erasure.getGenericSuperclass());
        }
      }
    }

  }

}

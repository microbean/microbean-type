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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.function.BiPredicate;

import org.microbean.development.annotation.Experimental;

/**
 * A {@link org.microbean.type.Type} that models a {@link
 * java.lang.reflect.Type java.lang.reflect.Type} for use primarily by
 * a {@link org.microbean.type.Type.Semantics} instance.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see org.microbean.type.Type.Semantics
 */
@Experimental
public class JavaType extends org.microbean.type.Type<Type> {


  /*
   * Static fields.
   */


  /**
   * An immutable {@link Map} of Java wrapper {@linkplain Class
   * classes} indexed by their primitive equivalents.
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


  /*
   * Instance fields.
   */


  private final boolean box;

  private final BiPredicate<? super Type, ? super Type> supertypeAcceptancePredicate;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link JavaType}.
   *
   * @param type the Java {@link Type} being modeled; must not be {@code null}
   *
   * @param box whether boxing of primitive types will be in effect;
   * even if {@code true} boxing only happens to primitive types
   *
   * @param supertypeAcceptancePredicate a {@link BiPredicate} gating
   * a {@link Type}'s membership in the represented type's collection
   * of {@linkplain #supertypes() supertypes} ({@linkplain
   * #directSupertypes() direct} and {@linkplain #supertypes()
   * otherwise}); may be {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}
   */
  protected JavaType(final Type type, final boolean box, final BiPredicate<? super Type, ? super Type> supertypeAcceptancePredicate) {
    super(type);
    this.box = box;
    this.supertypeAcceptancePredicate = Objects.requireNonNull(supertypeAcceptancePredicate, "supertypeAcceptancePredicate");
  }


  /*
   * Instance methods.
   */

  /**
   * Returns {@code true} if and only if this {@link JavaType}
   * represents a Java type that has a name.
   *
   * <p>In the Java reflective type system, only {@link Class} and
   * {@link TypeVariable} instances have names.</p>
   *
   * @return {@code true} if and only if this {@link JavaType} represents
   * a Java type that has a name
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #name()
   *
   * @see Class#getName()
   *
   * @see TypeVariable#getName()
   */
  @Override // org.microbean.type.Type<Type>
  public boolean named() {
    final Type type = this.object();
    return type instanceof Class || type instanceof TypeVariable;
  }

  /**
   * Returns the name of this {@link JavaType} if it has one
   * <strong>or {@code null} if it does not</strong>.
   *
   * <p>Only classes and type variables in the Java reflective type
   * system have names.</p>
   *
   * @return the name of this {@link JavaType}, or {@code null}
   *
   * @nullability This method and its overrides may, and often will,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be, idempotent
   * and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   */
  @Override // org.microbean.type.Type<Type>
  public String name() {
    final Type type = this.object();
    if (type instanceof Class<?> c) {
      return c.getName();
    } else if (type instanceof TypeVariable<?> tv) {
      return tv.getName();
    }
    return null;
  }

  /**
   * Returns {@code true} if this {@link JavaType} represents the same
   * type as that represented by the supplied {@link Owner}.
   *
   * <p>Type representation is not the same thing as equality.
   * Specifically, a {@link org.microbean.type.Type} may represent
   * another {@link org.microbean.type.Type} exactly, but may not be
   * {@linkplain org.microbean.type.Type#equals(Object) equal to}
   * it.</p>
   *
   * <p>Equality is <em>subordinate</em> to type representation.  That
   * is, in determining whether a given {@link
   * org.microbean.type.Type} represents another {@link
   * org.microbean.type.Type}, their {@link
   * org.microbean.type.Type#equals(Object) equals(Object)} methods
   * may be called, but a {@link org.microbean.type.Type}'s {@link
   * org.microbean.type.Type#equals(Object) equals(Object)} method
   * must not call {@link
   * org.microbean.type.Type#represents(Owner)}.</p>
   *
   * @param <X> the type representation type used by the supplied
   * {@link Owner}
   *
   * @param other the {@link Owner} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if this {@link Type} represents the same
   * thing as that represented by the supplied {@link Owner}
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   */
  @Override // org.microbean.type.Type<Type>
  public <X> boolean represents(final Owner<X> other) {
    if (super.represents(other)) {
      return true;
    }
    // TODO: other stuff; could use the equivalent of JavaTypes.toString() here
    return false;
  }

  /**
   * Returns {@code true} if and only if the return value of {@link
   * #object()} is identical to {@link Object Object.class}.
   *
   * @return {@code true} if and only if the return value of {@link
   * #object()} is identical to {@link Object Object.class}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // org.microbean.type.Type<Type>
  public final boolean top() {
    return this.object() == Object.class;
  }

  /**
   * If this {@link JavaType} represents a primitive type, and if and
   * only if boxing was enabled in the {@linkplain #JavaType(Type,
   * boolean, Predicate) constructor}, returns a {@link JavaType}
   * representing the corresponding "wrapper type", or this {@link
   * JavaType} itself in all other cases.
   *
   * @return a {@link JavaType} representing the corresponding "wrapper
   * type" where appropriate, or {@code this}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see org.microbean.type.Type#box()
   */
  @Override // org.microbean.type.Type<Type>
  public JavaType box() {
    if (this.box) {
      final Type type = this.object();
      if (type == void.class) {
        return of(Void.class, true, this.supertypeAcceptancePredicate);
      } else if (type == int.class) {
        // This is such a ridiculously common case we avoid the map lookup
        return of(Integer.class, true, this.supertypeAcceptancePredicate);
      } else if (type instanceof Class<?> c && c.isPrimitive()) {
        return of(wrapperTypes.get(c), true, this.supertypeAcceptancePredicate);
      }
    }
    return this;
  }

  /**
   * Returns an {@linkplain
   * Collections#unmodifiableCollection(Collection) unmodifiable and
   * immutable <code>Collection</code>} of the <em>direct
   * supertypes</em> of this {@link JavaType}, or an {@linkplain
   * Collection#isEmpty() empty <code>Collection</code>} if there are
   * no direct supertypes.
   *
   * <p>This implementation uses the {@link
   * JavaTypes#directSupertypes(Type)} method.</p>
   *
   * @return an {@linkplain
   * Collections#unmodifiableCollection(Collection) unmodifiable and
   * immutable <code>Collection</code>} of the <em>direct
   * supertypes</em> of this {@link JavaType}; never {@code null}
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
  @Override // org.microbean.type.Type<Type>
  public Collection<? extends JavaType> directSupertypes() {
    final ArrayList<JavaType> c = new ArrayList<>(11);
    for (final Type type : JavaTypes.directSupertypes(this.object())) {
      c.add(of(type, this.box, this.supertypeAcceptancePredicate));
    }
    c.trimToSize();
    return Collections.unmodifiableCollection(c);
  }

  /**
   * If this {@link JavaType} represents a {@link ParameterizedType}
   * or a {@link GenericArrayType} returns a {@link JavaType}
   * representing its {@linkplain ParameterizedType#getRawType() raw
   * type} or {@linkplain GenericArrayType#getGenericComponentType()
   * generic component type}, or, if this {@link JavaType} does not
   * represent a {@link ParameterizedType} or a {@link
   * GenericArrayType}, returns {@code this}.
   *
   * @return a suitable {@link JavaType}; never {@code null}; often {@code
   * this}
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
  @Override // org.microbean.type.Type<Type>
  public JavaType type() {
    final Type type = this.object();
    if (type instanceof ParameterizedType p) {
      return of(p.getRawType(), this.box, this.supertypeAcceptancePredicate);
    } else if (type instanceof GenericArrayType g) {
      return of(g.getGenericComponentType(), this.box, this.supertypeAcceptancePredicate);
    } else {
      return this;
    }
  }

  /**
   * Returns the owner of this {@link JavaType} as an {@link Object},
   * suitable only for equality comparisons, or {@code null} if this
   * {@link JavaType} does not represent either a {@link Class}, a
   * {@link ParameterizedType} or a {@link TypeVariable}.
   *
   * @return the owner of this {@link JavaType}, or {@code null}
   *
   * @nullability Implementations of this method must not return
   * {@code null}.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   */
  @Experimental
  @Override // org.microbean.type.Type<Type>
  public Owner<Type> owner() {
    final Type type = this.object();
    if (type instanceof Class<?> c) {
      return of(c.getEnclosingClass(), this.box, this.supertypeAcceptancePredicate);
    } else if (type instanceof ParameterizedType p) {
      return of(p.getOwnerType(), this.box, this.supertypeAcceptancePredicate);
    } else if (type instanceof TypeVariable<?> tv) {
      final GenericDeclaration gd = tv.getGenericDeclaration();
      if (gd instanceof Class<?> c) {
        return of(c, this.box, this.supertypeAcceptancePredicate);
      } else if (gd instanceof Executable e) {
        return new JavaExecutable(e, this.box);
      } else {
        throw new AssertionError("gd: " + gd);
      }
    } else {
      return null;
    }
  }

  /**
   * Returns {@code true} if and only if this {@link JavaType}
   * represents a generic {@link Class} by virtue of having
   * {@linkplain Class#getTypeParameters() type parameters}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is an instance of {@link Class} and, if so, if
   * that {@link Class} {@linkplain Class#getTypeParameters() has any
   * type parameters}.
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a generic class by virtue of having {@linkplain
   * Class#getTypeParameters() type parameters}
   *
   * @see #object()
   *
   * @see #typeParameters()
   */
  @Override // org.microbean.type.Type<Type>
  public boolean hasTypeParameters() {
    return this.object() instanceof Class<?> c && c.getTypeParameters().length > 0;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a {@link ParameterizedType} by virtue of {@linkplain
   * ParameterizedType#getActualTypeArguments() having type
   * arguments}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is a {@link ParameterizedType}.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a parameterized type by virtue of having {@linkplain
   * ParameterizedType#getActualTypeArguments() type arguments}
   *
   * @see #object()
   *
   * @see #typeArguments()
   */
  @Override // org.microbean.type.Type<Type>
  public boolean hasTypeArguments() {
    return this.object() instanceof ParameterizedType;
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s type
   * arguments.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link JavaType} models a
   * {@link ParameterizedType}.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s
   * {@linkplain ParameterizedType#getActualTypeArguments() type
   * arguments}; never {@code null}
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
  @Override // org.microbean.type.Type<Type>
  public List<? extends JavaType> typeArguments() {
    if (this.object() instanceof ParameterizedType p) {
      final Type[] typeArguments = p.getActualTypeArguments();
      final List<JavaType> typeArgumentsList = new ArrayList<>(typeArguments.length);
      for (final Type typeArgument : typeArguments) {
        typeArgumentsList.add(of(typeArgument, this.box, this.supertypeAcceptancePredicate));
      }
      return Collections.unmodifiableList(typeArgumentsList);
    }
    return List.of();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s type
   * parameters.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link JavaType} models a generic
   * {@link Class}.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s
   * {@linkplain Class#getTypeParameters() type parameters}; never
   * {@code null}
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
  @Override // org.microbean.type.Type<Type>
  public List<? extends JavaType> typeParameters() {
    if (this.object() instanceof Class<?> c) {
      final Type[] typeParameters = c.getTypeParameters();
      if (typeParameters.length > 0) {
        final List<JavaType> typeParametersList = new ArrayList<>(typeParameters.length);
        for (final Type typeParameter : typeParameters) {
          typeParametersList.add(of(typeParameter, this.box, this.supertypeAcceptancePredicate));
        }
        return Collections.unmodifiableList(typeParametersList);
      }
    }
    return List.of();
  }

  /**
   * Returns the {@linkplain Class#getComponentType() component type}
   * of this {@link JavaType}, if there is one, <strong>or {@code
   * null} if there is not</strong>.
   *
   * <p>This method returns a non-{@code null} result only when this
   * {@link JavaType} represents a {@link Type} that is either
   * {@linkplain Class#isArray() an array} or a {@link
   * GenericArrayType}.</p>
   *
   * @return the {@linkplain Class#getComponentType() component type}
   * of this {@link JavaType}, if there is one, or {@code null} if
   * there is not
   *
   * @nullability This method and its overrides may return null.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see Class#getComponentType()
   *
   * @see GenericArrayType#getGenericComponentType()
   */
  @Override // org.microbean.type.Type<Type>
  public JavaType componentType() {
    final Type newType;
    final Type type = this.object();
    if (type instanceof Class<?> c) {
      newType = c.getComponentType();
    } else if (type instanceof GenericArrayType g) {
      newType = g.getGenericComponentType();
    } else {
      newType = null;
    }
    if (newType == null) {
      return null;
    }
    return of(newType, this.box, this.supertypeAcceptancePredicate);
  }

  /**
   * Returns {@code true} if and only if this {@link JavaType}
   * represents either a {@link TypeVariable} or a {@link
   * WildcardType}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is either a {@link TypeVariable} or a {@link
   * WildcardType}.</p>
   *
   * @return {@code true} if and only if this {@link JavaType}
   * represents either a {@link TypeVariable} or a {@link
   * WildcardType}
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #upperBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public boolean upperBounded() {
    final Type type = this.object();
    return type instanceof WildcardType || type instanceof TypeVariable;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a {@link WildcardType} {@linkplain WildcardType#getLowerBounds()
   * with a lower bound}.
   *
   * <p>This implementation checks to see if the return value of
   * {@link #object()} is a {@link WildcardType}, and, if so, if that
   * {@link WildcardType} {@linkplain WildcardType#getLowerBounds()
   * has a lower bound}.</p>
   *
   * @return {@code true} if and only if this {@link JavaType}
   * represents a {@link WildcardType} {@linkplain
   * WildcardType#getLowerBounds() with a lower bound}
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #lowerBounds()
   *
   * @see WildcardType#getLowerBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public boolean lowerBounded() {
    final Type type = this.object();
    return type instanceof WildcardType w && w.getLowerBounds().length > 0;
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s lower
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models a {@link
   * WildcardType} {@linkplain WildcardType#getLowerBounds() with a
   * lower bound}.</p>
   *
   * <p>The returned {@link List} will contain at most one
   * element.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s lower
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see WildcardType#getLowerBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public List<? extends JavaType> lowerBounds() {
    final Type type = this.object();
    if (type instanceof WildcardType w) {
      final Type[] lowerBounds = w.getLowerBounds();
      if (lowerBounds.length > 0) {
        final List<JavaType> lowerBoundsList = new ArrayList<>(lowerBounds.length);
        for (final Type lowerBound : lowerBounds) {
          lowerBoundsList.add(of(lowerBound, this.box, this.supertypeAcceptancePredicate));
        }
        return Collections.unmodifiableList(lowerBoundsList);
      }
    }
    return List.of();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s upper
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models either a {@link
   * WildcardType} or a {@link TypeVariable}.
   *
   * <p>The returned {@link List} will contain at most one
   * element.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link JavaType}'s lower
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @see TypeVariable#getBounds()
   *
   * @see WildcardType#getUpperBounds()
   */
  @Override // org.microbean.type.Type<Type>
  public List<? extends JavaType> upperBounds() {
    final Type type = this.object();
    if (type instanceof WildcardType w) {
      return List.of(of(w.getUpperBounds()[0], this.box, this.supertypeAcceptancePredicate));
    } else if (type instanceof TypeVariable<?> t) {
      final Type[] upperBounds = t.getBounds();
      switch (upperBounds.length) {
      case 0:
        throw new AssertionError();
      case 1:
        return List.of(of(upperBounds[0], this.box, this.supertypeAcceptancePredicate));
      default:
        final List<JavaType> upperBoundsList = new ArrayList<>(upperBounds.length);
        for (final Type upperBound : upperBounds) {
          upperBoundsList.add(of(upperBound, this.box, this.supertypeAcceptancePredicate));
        }
        return Collections.unmodifiableList(upperBoundsList);
      }
    } else {
      return List.of();
    }
  }

  /**
   * Returns all the supertypes of this {@link JavaType} (which
   * includes this {@link JavaType}).
   *
   * <p>This method reflexively and transitively applies the direct
   * supertype relation (represented by the {@link
   * #directSupertypes()} method) to this {@link JavaType} and returns
   * an {@linkplain Collections#unmodifiableCollection(Collection)
   * unmodifiable <code>Collection</code>} containing the result.</p>
   *
   * @return an {@linkplain
   * Collections#unmodifiableCollection(Collection) unmodifiable
   * <code>Collection</code>} containing all the supertypes of this
   * {@link Type} (which includes this {@link JavaType}); never {@code
   * null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #directSupertypes()
   */
  @SuppressWarnings("unchecked")
  public Collection<? extends JavaType> supertypes() {
    return (Collection<? extends JavaType>)super.supertypes();
  }

  @Override
  protected boolean acceptSupertype(final org.microbean.type.Type<? extends Type> subtype, final org.microbean.type.Type<? extends Type> supertype) {
    return this.supertypeAcceptancePredicate.test(subtype.object(), supertype.object());
  }

  @Override // Object
  public final int hashCode() {
    return JavaTypes.hashCode(this.object());
  }

  @Override // Object
  public final boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && this.getClass() == other.getClass()) {
      return JavaTypes.equals(this.object(), ((JavaType)other).object());
    } else {
      return false;
    }
  }


  /*
   * Static methods.
   */


  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @return a new {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Token, boolean, Predicate)
   */
  public static JavaType of(final Token<?> type) {
    return of(type, false, null);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @param box whether autoboxing is enabled
   *
   * @return a new {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Token, boolean, Predicate)
   */
  public static JavaType of(final Token<?> type, final boolean box) {
    return of(type, box, null);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type a {@link Token} representing the type to model; must
   * not be {@code null}
   *
   * @param box whether autoboxing is enabled
   *
   * @param supertypeAcceptancePredicate a {@link BiPredicate} gating
   * a {@link Type}'s membership in the represented type's collection
   * of {@linkplain #supertypes() supertypes} ({@linkplain
   * #directSupertypes() direct} and {@linkplain #supertypes()
   * otherwise}); may be {@code null}
   *
   * @return a new {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Type, boolean, Predicate)
   */
  public static JavaType of(final Token<?> type, final boolean box, final BiPredicate<? super Type, ? super Type> supertypeAcceptancePredicate) {
    return of(type.type(), box, supertypeAcceptancePredicate);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @return a new {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Type, boolean, Predicate)
   */
  public static JavaType of(final Type type) {
    return of(type, false, null);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @param box whether autoboxing is enabled
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Type, boolean, Predicate)
   */
  public static JavaType of(final Type type, final boolean box) {
    return of(type, box, null);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the {@link Type} that will be modeled; must not be
   * {@code null}
   *
   * @param box whether autoboxing is enabled
   *
   * @param supertypeAcceptancePredicate a {@link BiPredicate} gating a
   * {@link Type}'s membership in the represented type's collection of
   * {@linkplain #supertypes() supertypes} ({@linkplain
   * #directSupertypes() direct} and {@linkplain #supertypes()
   * otherwise}); may be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static JavaType of(final Type type, final boolean box, final BiPredicate<? super Type, ? super Type> supertypeAcceptancePredicate) {
    return new JavaType(type, box, supertypeAcceptancePredicate == null ? JavaTypes::acceptAll : supertypeAcceptancePredicate);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the {@link org.microbean.type.Type} whose {@linkplain
   * org.microbean.type.Type#object() represented <code>Type</code>}
   * will be modeled; must not be {@code null}
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(org.microbean.type.Type, boolean)
   */
  public static JavaType of(final org.microbean.type.Type<? extends Type> type) {
    return of(type, false);
  }

  /**
   * Returns a {@link JavaType} suitable for the supplied arguments.
   *
   * @param type the {@link org.microbean.type.Type} whose {@linkplain
   * org.microbean.type.Type#object() represented <code>Type</code>}
   * will be modeled; must not be {@code null}
   *
   * @param box whether autoboxing is enabled
   *
   * @return a {@link JavaType}; never {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}.
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic (in
   * that it may return a new {@link JavaType} with each invocation).
   * However, any {@link JavaType} returned from this method is
   * guaranteed to {@linkplain #equals(Object) equal} any other {@link
   * JavaType} returned from this method, provided the inputs to all
   * invocations are equal.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Type, boolean)
   */
  public static JavaType of(final org.microbean.type.Type<? extends Type> type, final boolean box) {
    if (type instanceof JavaType jt && box == jt.box) {
      return jt;
    }
    return of(type.object(), box);
  }


  /*
   * Inner and nested classes.
   */


  /**
   * A holder of a {@link Type} that embodies <a
   * href="http://gafter.blogspot.com/2006/12/super-type-tokens.html"
   * target="_parent">Gafter's gadget</a>.
   *
   * <p>To use this class, create a new instance of an anonymous
   * subclass of it, and then call {@link #type() type()} on it.  For
   * example:</p>
   *
   * <blockquote><pre>
   * // type will be a {@link ParameterizedType} whose {@link ParameterizedType#getRawType() rawType} is {@link java.util.List List.class} and
   * // whose {@linkplain ParameterizedType#getActualTypeArguments() sole type argument} is {@link String String.class}
   * {@link Type} type = new {@link Token Token}&lt;{@link java.util.List List}&lt;{@link String}&gt;&gt;() {}.{@link #type() type()};</pre></blockquote>
   *
   * @param <T> the modeled type; often parameterized
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see #type()
   */
  public static abstract class Token<T> {


    /*
     * Instance fields.
     */


    private final Type type;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link Token}.
     */
    protected Token() {
      super();
      this.type = mostSpecializedParameterizedSuperclass(this.getClass()).getActualTypeArguments()[0];
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
      return this.type;
    }

    /**
     * Returns the {@linkplain JavaTypes#erase(Type) type erasure} of
     * this {@link Token}'s {@linkplain #type() modeled
     * <code>Type</code>}, or {@code null} if erasing the {@link Type}
     * would result in a non-{@link Class} erasure (in which case the
     * erasure is simply the {@link Type} itself), or if an erasure
     * cannot be determined.
     *
     * @return the {@linkplain JavaTypes#erase(Type) type erasure} of
     * this {@link Token}'s {@linkplain #type() modeled
     * <code>Type</code>}, or {@code null} if erasing the {@link Type}
     * would result in a non-{@link Class} erasure, or if an erasure
     * cannot be determined
     *
     * @nullability This method never returns {@code null}.
     *
     * @threadsafety This method is safe for concurrent use by multiple
     * threads.
     *
     * @idempotency This method is idempotent and deterministic.
     */
    public final Class<?> erase() {
      return JavaTypes.erase(this.type());
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


    private static final ParameterizedType mostSpecializedParameterizedSuperclass(final Type type) {
      if (type == null || type == Object.class || type == Token.class) {
        return null;
      } else {
        final Class<?> erasure = JavaTypes.erase(type);
        if (erasure == null || erasure == Object.class || !(Token.class.isAssignableFrom(erasure))) {
          return null;
        } else {
          return type instanceof ParameterizedType p ? p : mostSpecializedParameterizedSuperclass(erasure.getGenericSuperclass());
        }
      }
    }


  }

}

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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import java.util.function.Function;
import java.util.function.Predicate;

import org.microbean.development.annotation.Convenience;
import org.microbean.development.annotation.EntryPoint;
import org.microbean.development.annotation.Experimental;
import org.microbean.development.annotation.OverridingDiscouraged;
import org.microbean.development.annotation.OverridingEncouraged;

/**
 * A value-like object representing a (Java-like) type for purposes of
 * testing assignability.
 *
 * <p>Concisely: {@link Type}s represent {@link java.lang.reflect.Type
 * java.lang.reflect.Type}s (or other frameworks' similar
 * representations) without any dependence on {@link
 * java.lang.reflect.Type java.lang.reflect.Type} itself.</p>
 *
 * <p>This class is used directly primarily by the {@link Semantics}
 * class and its subclasses.</p>
 *
 * @param <T> the type of the thing representing a Java type that is
 * being adapted; often a {@link java.lang.reflect.Type
 * java.lang.reflect.Type} or some other framework's representation of
 * one
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see Semantics
 */
@Experimental
public abstract class Type<T> implements Owner<T> {


  /*
   * Static fields.
   */


  private static final VarHandle SUPERTYPES;

  static {
    final Lookup lookup = MethodHandles.lookup();
    try {
      SUPERTYPES = lookup.findVarHandle(Type.class, "supertypes", List.class);
    } catch (final NoSuchFieldException | IllegalAccessException reflectiveOperationException) {
      throw (Error)new ExceptionInInitializerError(reflectiveOperationException.getMessage()).initCause(reflectiveOperationException);
    }
  }


  /*
   * Instance fields.
   */


  private final T type;

  private volatile List<? extends Type<T>> supertypes;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Type}.
   *
   * @param type the type to represent; must not be {@code null}
   *
   * @exception NullPointerException if {@code type} is {@code null}
   */
  protected Type(final T type) {
    super();
    this.type = Objects.requireNonNull(type, "type");
  }

  /**
   * Creates a new {@link Type} that models custom supertypes.
   *
   * <p>Among other things, this means that the {@link #object()}
   * method will return {@code null}.</p>
   *
   * @param supertypes the custom supertypes; must not be {@code null}
   *
   * @exception NullPointerException if {@code supertypes} is {@code
   * null}
   */
  protected Type(final List<? extends Type<T>> supertypes) {
    super();
    this.type = null;
    this.supertypes = List.copyOf(supertypes);
  }


  /*
   * Instance methods.
   */


  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a Java type that has a name.
   *
   * <p>In the Java reflective type system, only {@link Class} and
   * {@link java.lang.reflect.TypeVariable} instances have names.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a Java type that has a name
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
  @Override // Owner<T>
  @OverridingEncouraged
  public boolean named() {
    return Owner.super.named();
  }

  /**
   * Returns the name of this {@link Type} if it has one <strong>or
   * {@code null} if it does not</strong>.
   *
   * <p>Only classes (and interfaces) and type variables in the Java
   * reflective type system have names.</p>
   *
   * @return the name of this {@link Type}, or {@code null}
   *
   * @nullability Implementations of this method may, and often will,
   * return {@code null}.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   */
  @Override // Owner<T>
  public abstract String name();

  /**
   * Returns the object supplied at construction time that is the type
   * this {@link Type} is representing, or {@code null} if this
   * information is not available, or if this {@link Type} {@linkplain
   * #customSupertyped() represents a collection of custom
   * supertypes}.
   *
   * @return the object supplied at construction time that is the type
   * this {@link Type} is representing
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Owner<T>
  public final T object() {
    return this.type;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * the uppermost reference type in the type system.
   *
   * <p>In all practical cases, if an implementation of this method
   * returns {@code true}, it means this {@link Type} represents
   * {@link Object Object.class}.</p>
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * the uppermost reference type in the type system, e.g. {@link
   * Object Object.class}
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method must be idempotent
   * and deterministic.
   */
  public abstract boolean top();

  /**
   * Returns a (possibly cached) {@link List} of this {@link Type}'s
   * <em>direct supertypes</em>, defined to be its immediate
   * superclass or supertype, if any, and any interfaces it directly
   * implements.
   *
   * @return a (possibly cached) {@link List} of this {@link Type}'s
   * direct supertypes
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #computeDirectSupertypes()
   */
  public final List<? extends Type<T>> directSupertypes() {
    return List.copyOf(this.computeDirectSupertypes());
  }

  /**
   * Returns an {@linkplain
   * Collections#unmodifiableList(List) unmodifiable and
   * immutable <code>List</code>} of the <em>direct
   * supertypes</em> of this {@link Type}, or an {@linkplain
   * List#isEmpty() empty <code>List</code>} if there are
   * no direct supertypes, or if this {@link Type} implementation does
   * not distinguish between direct and non-direct supertypes.
   *
   * <p>The direct supertypes of a type are defined to be those
   * analogous to those described by the <a
   * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.10.2"
   * target="_parent">Java Language Specification, section 4.10.2</a>,
   * minus any types featuring wildcards.</p>
   *
   * <p>The {@link List} returned must not contain {@code this}
   * and must have no {@code null} or duplicate elements.</p>
   *
   * <p>The ordering within the returned {@link List} is not
   * specified, <strong>but must be deterministic between
   * invocations</strong>.</p>
   *
   * <p>The size of the {@link List} returned must not change
   * between calls.</p>
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * <p><strong>The default implementation of this method returns an
   * empty {@link List}.</strong> Many subclasses may wish to
   * override this behavior.</p>
   *
   * @return an {@linkplain
   * Collections#unmodifiableList(List) unmodifiable and
   * immutable <code>List</code>} of the <em>direct
   * supertypes</em> of this {@link Type}; never {@code null}
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
   * @see JavaTypes#directSupertypes(java.lang.reflect.Type)
   *
   * @see #directSupertypes()
   *
   * @see #supertypes()
   *
   * @see #computeSupertypes()
   */
  protected List<? extends Type<T>> computeDirectSupertypes() {
    return List.of();
  }

  /**
   * If this {@link Type} represents a parameterized type or a generic
   * array type, returns a {@link Type} representing its raw type or
   * generic component type, or, if this {@link Type} does not
   * represent a parameterized type or a generic array type, returns
   * {@code this}.
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return a suitable {@link Type}; never {@code null}; often {@code
   * this}
   *
   * @nullability Overrides of this method must not return {@code
   * null}.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Overrides of this method must be idempotent and
   * deterministic.
   */
  @Override // Owner<T>
  public abstract Type<T> type();

  /**
   * Returns the owner of this {@link Type} as an {@link Owner
   * Owner&lt;T&gt;}, suitable only for equality comparisons, or
   * {@code null} if this {@link Type} is not owned.
   *
   * @return the owner of this {@link Type}, or {@code null}
   *
   * @nullability Overrides of this method must not return
   * {@code null}.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Overrides of this method must be idempotent
   * and deterministic.
   */
  @Override // Owner<T>
  public abstract Owner<T> owner();

  /**
   * Returns a {@link Type}, <strong>usually new</strong>, whose
   * {@link #object() object()} method will return the supplied {@code
   * object}.
   *
   * @param object the modeled type; must not be {@code null}
   *
   * @return a {@link Type}
   *
   * @exception NullPointerException if {@code object} is {@code null}
   *
   * @nullability Overrides of this method must not return {@code
   * null}.
   *
   * @idempotency Overrides of this method must be deterministic but
   * not idempotent.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   */
  public abstract Type<T> withObject(final T object);

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a generic class by virtue of having type parameters.
   *
   * <p>This implementation calls {@link Owner#hasTypeParameters()}
   * and returns the result. Subclasses are encouraged to provide a
   * faster implementation.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a generic class by virtue of having type parameters
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see Owner#hasTypeParameters()
   *
   * @see #typeParameters()
   */
  @Override // Owner<T>
  @OverridingEncouraged
  public boolean hasTypeParameters() {
    return Owner.super.hasTypeParameters();
  }

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a parameterized type by virtue of having type arguments.
   *
   * <p>This implementation calls the {@link #typeArguments()} method
   * and returns {@code true} if the resulting {@link List}
   * {@linkplain List#isEmpty() is not empty}.  Subclasses are
   * encouraged to provide a faster implementation.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a parameterized type by virtue of having type arguments
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #typeArguments()
   */
  @OverridingEncouraged
  public boolean hasTypeArguments() {
    final Collection<?> tas = this.typeArguments();
    return tas != null && !tas.isEmpty();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s type
   * parameters.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models a generic
   * class.</p>
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s type
   * parameters; never {@code null}
   *
   * @nullability Overrides of this method must not return
   * {@code null}.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Overrides of this method must be idempotent
   * and deterministic.
   */
  @Override // Owner<T>
  public abstract List<? extends Type<T>> typeParameters();

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s type
   * arguments.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models a
   * parameterized type.</p>
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s type
   * parameters; never {@code null}
   *
   * @nullability Overrides of this method must not return
   * {@code null}.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Overrides of this method must be idempotent
   * and deterministic.
   */
  public abstract List<? extends Type<T>> typeArguments();

  /**
   * Returns {@code false} when invoked since {@link Type}s don't have
   * parameters (unlike {@link Owner}s representing {@link
   * java.lang.reflect.Executable executables}).
   *
   * @return {@code false} when invoked
   *
   * @nullability This method always returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Owner<T>
  public final boolean hasParameters() {
    return false;
  }

  /**
   * Returns {@code null} when invoked since {@link Type}s don't have
   * parameters (unlike {@link Owner}s representing {@link
   * java.lang.reflect.Executable executables}).
   *
   * @return {@code null} when invoked
   *
   * @nullability This method always returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Owner<T>
  public final List<? extends Type<T>> parameters() {
    return null;
  }

  /**
   * Returns the <em>component type</em> of this {@link Type}, if
   * there is one, <strong>or {@code null} if there is not</strong>.
   *
   * <p>Overrides of this method must return a non-{@code null}
   * result only when this {@link Type} represents a type that is
   * either an array or a generic array type.</p>
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return the <em>component type</em> of this {@link Type}, if
   * there is one, or {@code null} if there is not
   *
   * @nullability Overrides of this method may return {@code
   * null}.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Overrides of this method must be idempotent
   * and deterministic.
   */
  public abstract Type<T> componentType();

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * either a type variable or a wildcard type.
   *
   * <p>This implementation calls the {@link #upperBounds()} method
   * and returns {@code true} if the resulting {@link List}
   * {@linkplain List#isEmpty() is not empty}.  Subclasses are
   * encouraged to provide a faster implementation.</p>
   *
   * <p>Undefined behavior will result if an override does not
   * meet these requirements.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * either a type variable or a wildcard type
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #upperBounds()
   */
  @OverridingEncouraged
  public boolean upperBounded() {
    return !this.upperBounds().isEmpty();
  }

  /**
   * Returns {@code true} if and only if this {@link Type} represents
   * a wildcard type with a lower bound.
   *
   * <p>This implementation calls the {@link #lowerBounds()} method
   * and returns {@code true} if the resulting {@link List}
   * {@linkplain List#isEmpty() is not empty}.  Subclasses are
   * encouraged to provide a faster implementation.</p>
   *
   * <p>Undefined behavior will result if an override does not
   * meet these requirements.</p>
   *
   * @return {@code true} if and only if this {@link Type} represents
   * a wildcard type with lower bounds
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #lowerBounds()
   */
  @OverridingEncouraged
  public boolean lowerBounded() {
    return !this.lowerBounds().isEmpty();
  }

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s lower
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models a
   * lower-bounded wildcard type.</p>
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s lower
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability Overrides of this method must not return
   * {@code null}.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Overrides of this method must be idempotent
   * and deterministic.
   */
  public abstract List<? extends Type<T>> lowerBounds();

  /**
   * Returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s upper
   * bounds.
   *
   * <p>The returned {@link List} will be {@linkplain List#isEmpty()
   * non-empty} if and only if this {@link Type} models either a type
   * variable, an unbounded wildcard type, or an upper-bounded
   * wildcard type.
   *
   * <p>Undefined behavior will result if an implementation does not
   * meet these requirements.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} of this {@link Type}'s upper
   * bounds; never {@code null}; often {@linkplain List#isEmpty()
   * empty}
   *
   * @nullability Overrides of this method must not return
   * {@code null}.
   *
   * @threadsafety Overrides of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Overrides of this method must be idempotent
   * and deterministic.
   */
  public abstract List<? extends Type<T>> upperBounds();

  /**
   * Returns {@code true} if and only if this {@link Type} models a
   * custom {@linkplain #supertypes() list of supertypes} and nothing
   * else.
   *
   * <p>This method returns {@code true} when and only when all of the
   * following conditions are met:</p>
   *
   * <ul>
   *
   * <li>The {@link #object()} method returns {@code null}</li>
   *
   * <li>The {@link #hasTypeArguments()} method returns {@code
   * false}</li>
   *
   * <li>The {@link #arrayType()} method returns {@code false}</li>
   *
   * <li>The {@link #lowerBounded()} method returns {@code false}</li>
   *
   * <li>The {@link #upperBounded()} method returns {@code false}</li>
   *
   * <li>The {@link #named()} method returns {@code false}</li>
   *
   * </ul>
   *
   * @return {@code true} if and only if this {@link type} models a
   * custom {@linkplain #supertypes() list of supertypes} and nothing
   * else
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #supertypes()
   *
   * @see #Type(List)
   */
  public final boolean customSupertyped() {
    return
      this.object() == null &&
      !this.named() &&
      !this.hasTypeArguments() &&
      !this.arrayType() &&
      !this.lowerBounded() &&
      !this.upperBounded();
  }
  
  /**
   * Returns {@code true} if and only if this {@link Type} models a
   * class or interface.
   *
   * <p>This method returns {@code true} if and only if {@link
   * #named()} returns {@code true} and {@link #upperBounded()}
   * returns {@code false}.</p>
   *
   * @return {@code true} if and only if this {@link Type} models a
   * class
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #named()
   *
   * @see #upperBounded()
   */
  public final boolean classOrInterface() {
    return this.named() && !this.upperBounded();
  }

  /**
   * Returns {@code true} if and only if this {@link Type} models an
   * array type (which may be a {@linkplain #genericArrayType()
   * generic array type}).
   *
   * <p>This method returns {@code true} if and only if {@link
   * #componentType()} returns a non-{@code null} value.</p>
   *
   * @return {@code true} if and only if this {@link Type} models an
   * array type (which may be a {@linkplain #genericArrayType()
   * generic array type})
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #componentType()
   */
  public final boolean arrayType() {
    return this.componentType() != null;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} models a
   * generic array type.
   *
   * <p>This method returns {@code true} if and only if {@link
   * #componentType()} returns a non-{@code null} value and {@link
   * #type()} returns a {@link Type} that is not {@code this}.</p>
   *
   * @return {@code true} if and only if this {@link Type} models a
   * generic array type
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #componentType()
   *
   * @see #type()
   */
  public final boolean genericArrayType() {
    return this.componentType() != null && this.type() != this;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} models a
   * non-generic array type.
   *
   * <p>This method returns {@code true} if and only if {@link
   * #componentType()} returns a non-{@code null} value and {@link
   * #type()} returns a {@link Type} that is {@code this}.</p>
   *
   * @return {@code true} if and only if this {@link Type} models a
   * non-generic array type
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #componentType()
   *
   * @see #type()
   */
  public final boolean nonGenericArrayType() {
    return this.componentType() != null && this.type() == this;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} models a
   * parameterized type.
   *
   * <p>This method returns {@code true} if and only if {@link
   * #hasTypeArguments()} returns {@code true}.</p>
   *
   * @return {@code true} if and only if this {@link Type} models a
   * parameterized type
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #hasTypeArguments()
   */
  public final boolean parameterizedType() {
    return this.hasTypeArguments();
  }

  /**
   * Returns {@code true} if and only if this {@link Type} models a
   * type variable.
   *
   * <p>This method returns {@code true} if and only if {@link
   * #named()} returns {@code true} and {@link #upperBounded()}
   * returns {@code true}.</p>
   *
   * @return {@code true} if and only if this {@link Type} models a
   * type variable
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #named()
   *
   * @see #upperBounded()
   */
  public final boolean typeVariable() {
    return this.named() && this.upperBounded();
  }

  /**
   * Returns {@code true} if and only if this {@link Type} models a
   * wildcard type.
   *
   * <p>This method returns {@code true} if and only if {@link
   * #named()} returns {@code true} and one of {@link #upperBounded()}
   * or {@link #lowerBounded()} returns {@code true}.</p>
   *
   * @return {@code true} if and only if this {@link Type} models a
   * wildcard type
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #named()
   *
   * @see #upperBounded()
   *
   * @see #lowerBounded()
   */
  public final boolean wildcard() {
    return !this.named() && (this.upperBounded() || this.lowerBounded());
  }

  /**
   * Returns all the supertypes of this {@link Type} (which normally
   * includes this {@link Type}).
   *
   * <p>This method returns a cached list of supertypes, or, if
   * such a list does not yet exist, returns the result of
   * invoking the {@link #computeSupertypes()} method.</p>
   *
   * @return an {@linkplain
   * Collections#unmodifiableList(List) unmodifiable
   * <code>List</code>} containing all the supertypes of this
   * {@link Type} (which normally includes this {@link Type}); never
   * {@code null}; never {@linkplain Collection#isEmpty() empty}
   *
   * @nullability This method does not return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #computeSupertypes()
   */
  public final List<? extends Type<T>> supertypes() {
    List<? extends Type<T>> c = this.supertypes; // volatile read
    if (c == null) {
      c = List.copyOf(this.computeSupertypes());
      if (!SUPERTYPES.compareAndSet(this, null, c)) { // volatile write
        return this.supertypes; // volatile read
      }
    }
    return c;
  }

  /**
   * Returns all the supertypes of this {@link Type} (which normally
   * includes this {@link Type}).
   *
   * <p>The returned {@link List} must not be {@code null} and
   * must not be {@linkplain Collection#isEmpty() empty}.  Undefined
   * behavior will result if overrides do not adhere to this
   * requirement.</p>
   *
   * <p>The order of elements within the {@link List} returned
   * by any invocation of this method must be deterministic.
   * Undefined behavior will result if overrides do not adhere to this
   * requirement.</p>
   *
   * <p>The default implementation of this method reflexively and
   * transitively applies the direct supertype relation (represented
   * by the {@link #directSupertypes()} method) to this {@link Type}
   * and returns an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>List</code>} containing the result.</p>
   *
   * @return an {@linkplain Collections#unmodifiableList(List)
   * unmodifiable <code>Collection</code>} containing all the
   * supertypes of this {@link Type} (which includes this {@link
   * Type}); never {@code null}; never {@linkplain
   * Collection#isEmpty() empty}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #supertypes()
   *
   * @see #directSupertypes()
   *
   * @see #computeDirectSupertypes()
   */
  protected List<? extends Type<T>> computeSupertypes() {
    return computeSupertypesBreadthFirst(this);
  }

  /**
   * Returns {@code true} if and only if this {@link Type} is a
   * supertype of the supplied {@link Type}.
   *
   * <p>This method uses a combination of the {@link #supertypes()}
   * method and the {@link #equals(Type, Type)} method in its
   * implementation.</p>
   *
   * @param sub the purported subtype; must not be {@code null}
   *
   * @return {@code true} if and only if this {@link Type} is a
   * supertype of the supplied {@link Type}
   *
   * @exception NullPointerException if {@code sub} is {@code null}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #supertypes()
   *
   * @see #equals(Type, Type)
   */
  @Convenience
  public final boolean supertypeOf(final Type<?> sub) {
    // Does this represent a supertype of sub?  Remember that the
    // supertype relation is reflexive.
    for (final Type<?> supertype : sub.supertypes()) {
      if (equals(supertype, this)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if and only if this {@link Type} is a
   * subtype of the supplied {@link Type}.
   *
   * <p>This method uses a combination of the {@link #supertypes()}
   * method and the {@link #equals(Type, Type)} method in its
   * implementation.</p>
   *
   * @param sup the purported supertype; must not be {@code null}
   *
   * @return {@code true} if and only if this {@link Type} is a
   * subtype of the supplied {@link Type}
   *
   * @exception NullPointerException if {@code sup} is {@code null}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #supertypes()
   *
   * @see #equals(Type, Type)
   */
  @Convenience
  public final boolean subtypeOf(final Type<?> sup) {
    for (final Type<?> supertype : this.supertypes()) {
      if (equals(sup, supertype)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a {@link Type}, selected from this {@link Type}'s
   * {@linkplain #supertypes() supertypes}, that is the most
   * specialized of those {@linkplain #supertypes() supertypes} and
   * that passes the test represented by the supplied {@link
   * Predicate}.
   *
   * <p>Often this method will return this {@link Type} itself, since
   * normally the set of supertypes of a type include the type
   * itself.</p>
   *
   * @param p the {@link Predicate} imposing additional restructions;
   * must not be {@code null}
   *
   * @return the most specialized {@link Type} meeting the conditions
   * above; often this {@link Type} itself
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic provided
   * that the supplied {@link Predicate} is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #supertypes()
   *
   * @see #customSupertyped()
   */
  public final Type<T> mostSpecialized(final Predicate<? super Type<T>> p) {
    Type<T> candidate = null;
    for (final Type<T> supertype : this.supertypes()) {
      if (supertype != candidate && p.test(supertype) && candidate == null || candidate.supertypeOf(supertype)) {
        candidate = supertype;
      }
    }
    return candidate;
  }
  
  @Override // Object
  public int hashCode() {
    return hashCode(this);
  }
  
  @Override // Owner
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof Type<?> type) { // instanceof is deliberate
      return equals(this, type);
    } else {
      return false;
    }
  }


  /*
   * Static methods.
   */


  /**
   * Returns a hashcode for the supplied {@link Type}, independent of
   * its particular implementation of the {@link Object#hashCode()}
   * method.
   *
   * @param type the {@link Type}; may be {@code null}
   *
   * @return a hashcode for the supplied {@link Type}
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #equals(Type, Type)
   */
  public static final int hashCode(final Type<?> type) {
    if (type == null) {
      return 0;
    }

    int hashCode = 1;

    final List<? extends Type<?>> typeArguments = type.typeArguments();
    if (typeArguments == null || typeArguments.isEmpty()) {
      hashCode = 31 * hashCode;
    } else {
      for (final Type<?> arg : typeArguments) {
        hashCode = 31 * hashCode + hashCode(arg);
      }
    }

    final Type<?> componentType = type.componentType();
    hashCode = 31 * hashCode + (componentType == null ? 0 : hashCode(componentType));

    final List<? extends Type<?>> lowerBounds = type.lowerBounds();
    if (lowerBounds == null || lowerBounds.isEmpty()) {
      hashCode = 31 * hashCode;
    } else {
      for (final Type<?> b : lowerBounds) {
        hashCode = 31 * hashCode + hashCode(b);
      }
    }

    final List<? extends Type<?>> upperBounds = type.upperBounds();
    if (upperBounds == null || upperBounds.isEmpty()) {
      hashCode = 31 * hashCode;
    } else {
      for (final Type<?> b : upperBounds) {
        hashCode = 31 * hashCode + hashCode(b);
      }
    }

    final String name = type.name();
    if (name == null) {
      if (typeArguments == null && componentType == null && lowerBounds == null && upperBounds == null) {
        // Just a supertype container
        final List<? extends Type<?>> supertypes = type.supertypes();
        if (supertypes == null) {
          hashCode = 31 * hashCode;
        } else {
          for (final Type<?> supertype : supertypes) {
            hashCode = 31 * hashCode + hashCode(supertype);
          }
        }
      } else {
        hashCode = 31 * hashCode;
      }
    } else {
      hashCode = 31 * hashCode + name.hashCode();
    }

    final Type<?> typeType = type.type();
    if (typeType != type) {
      hashCode = 31 * hashCode + (typeType == null ? 0 : hashCode(typeType));
    }

    final Owner<?> owner = type.owner();
    if (owner == null) {
      hashCode = 31 * hashCode;
    } else if (owner instanceof Type<?> ownerType) {
      hashCode = 31 * hashCode + hashCode(ownerType);
    } else {
      for (final Type<?> p : owner.parameters()) {
        hashCode = 31 * hashCode + hashCode(p);
      }
      hashCode = 31 * hashCode + hashCode(owner.type()); // return type
      hashCode = 31 * hashCode + hashCode((Type<?>)owner.owner()); // declaring class
    }

    return hashCode;
  }

  /**
   * Returns {@code true} if and only if the two supplied {@link
   * Type}s are equal, based solely upon the properties publicly
   * exposed by the {@link Type} class.
   *
   * @param t1 the first {@link Type}; may be {@code null}
   *
   * @param t2 the second {@link Type}; may be {@code null}
   *
   * @return {@code true} if and only if the two supplied {@link
   * Type}s are equal, based solely upon the properties publicly
   * exposed by the {@link Type} abstract class
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #hashCode(Type)
   */
  public static final boolean equals(final Type<?> t1, final Type<?> t2) {
    if (t1 == t2) {
      return true;
    } else if (t1 == null || t2 == null) {
      return false;
    }

    final Object t1Object = t1.object();
    if (t1Object == null) {
      if (t2.object() != null) {
        return false;
      }
    } else {
      return t1.objectEquals(t2.object());
    }

    if (t1.hasTypeArguments()) {
      if (!t2.hasTypeArguments()) {
        return false;
      }
      return parameterizedTypeEqualsParameterizedType(t1, t2);
    } else if (t2.hasTypeArguments()) {
      return false;
    }

    final Type<?> t1ComponentType = t1.componentType();
    final Type<?> t2ComponentType = t2.componentType();
    if (t1ComponentType == null) {
      if (t2ComponentType != null) {
        return false;
      } else if (t1.lowerBounded()) {
        return lowerBoundedWildcardTypeEqualsType(t1, t2);
      } else if (t1.upperBounded()) {
        return upperBoundedTypeEqualsType(t1, t2);
      } else if (t2.lowerBounded() || t2.upperBounded()) {
        return false;
      }

      // Not parameterized type.
      // Not array type.
      // Not generic array type.
      // Not type variable.
      // Not wildcard.
      final String t1Name = t1.name();
      final String t2Name = t2.name();
      if (t1Name == null) {
        // The only way this can happen is if t1 is actually just a
        // placeholder for its supertypes.
        if (t2Name != null) {
          return false;
        }
        return supertypesEqual(t1, t2);
      } else {
        return
          t1Name.equals(t2Name) &&
          equals((Type<?>)t1.owner(), (Type<?>)t2.owner());
      }
    } else if (!equals(t1ComponentType, t2ComponentType)) {
      return false;
    }

    // Non-null component types are equal; these are arrays
    final Type<?> t1Type = t1.type();
    final Type<?> t2Type = t2.type();
    if (t1Type == t1) {
      return t2Type == t2;
    } else if (t2Type == t2) {
      return false;
    }
    // Generic array types.
    return equals(t1Type, t2Type);
  }

  private static final boolean supertypesEqual(final Type<?> t1, final Type<?> t2) {
    if (t1 == t2) {
      return true;
    } else if (t1 == null || t2 == null) {
      return false;
    }
    final List<? extends Type<?>> s1 = t1.supertypes();
    final List<? extends Type<?>> s2 = t2.supertypes();
    final int size = s1.size();
    if (s2.size() != size) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      final Type<?> supertype1 = s1.get(i);
      if (supertype1 != s1 && supertype1 != s2) {
        final Type<?> supertype2 = s2.get(i);
        if (supertype2 != t1 && supertype2 != t2 && !equals(supertype1, supertype2)) {
          return false;
        }
      }
    }
    return true;
  }

  private static final boolean parameterizedTypeEqualsParameterizedType(final Type<?> t1, final Type<?> t2) {
    assert t1 != t2;
    assert t1.hasTypeArguments();
    assert t2.hasTypeArguments();

    final List<? extends Type<?>> t1Args = t1.typeArguments();
    final List<? extends Type<?>> t2Args = t2.typeArguments();
    int size = t1Args.size();
    if (t2Args.size() != size) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (!equals(t1Args.get(i), t2Args.get(i))) {
        return false;
      }
    }
    assert t1.type() != t1;
    assert t2.type() != t2;
    return equals(t1.type(), t2.type());
  }

  private static final boolean lowerBoundedWildcardTypeEqualsType(final Type<?> t1, final Type<?> t2) {
    assert t1 != t2;
    assert t1.lowerBounded();

    final List<? extends Type<?>> t1LowerBounds = t1.lowerBounds();
    final List<? extends Type<?>> t2LowerBounds = t2.lowerBounds();
    final int size = t1LowerBounds.size();
    if (t2LowerBounds.size() != size) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (!equals(t1LowerBounds.get(i), t2LowerBounds.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static final boolean upperBoundedTypeEqualsType(final Type<?> t1, final Type<?> t2) {
    assert t1 != t2;
    assert t1.upperBounded();

    if (!t2.upperBounded()) {
      return false;
    }

    final String t1Name = t1.name();
    if (!Objects.equals(t1Name, t2.name())) {
      return false;
    }

    final List<? extends Type<?>> t1UpperBounds = t1.upperBounds();
    final List<? extends Type<?>> t2UpperBounds = t2.upperBounds();
    final int size = t1UpperBounds.size();
    if (t2UpperBounds.size() != size) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (!equals(t1UpperBounds.get(i), t2UpperBounds.get(i))) {
        return false;
      }
    }

    if (t1Name != null) {
      // Two identically-named but not identical type variables.
      final Owner<?> t1Owner = t1.owner();
      final Owner<?> t2Owner = t2.owner();
      if (t1Owner == null || t2Owner == null) {
        // Type variables have to be declared by *some* owner, whether
        // it be a class or an executable.
        throw new IllegalArgumentException();
      } else if (t1Owner instanceof Type<?> t1OwnerType) {
        return t2Owner instanceof Type<?> t2OwnerType && equals(t1OwnerType, t2OwnerType);
      } else {
        return executableEqualsExecutable(t1Owner, t2Owner);
      }
    }

    // Two upper bounded (nameless) wildcards with the same bounds;
    // they're equal.
    return true;
  }

  private static final boolean executableEqualsExecutable(final Owner<?> o1, final Owner<?> o2) {
    assert o1 != o2;
    assert o1.hasTypeParameters();
    assert o2.hasTypeParameters();
    assert o1.named();
    assert o2.named();
    assert o1.hasParameters();
    assert o2.hasParameters();
    assert !(o1 instanceof Type);
    assert !(o2 instanceof Type);

    if (!Objects.equals(o1.name(), o2.name())) {
      return false;
    }
    final List<? extends Type<?>> o1Parameters = o1.parameters();
    final List<? extends Type<?>> o2Parameters = o2.parameters();
    final int size = o1Parameters.size();
    if (o2Parameters.size() != size) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (!equals(o1Parameters.get(i), o2Parameters.get(i))) {
        return false;
      }
    }
    return
      !equals(o1.type(), o2.type()) ||
      !equals((Type<?>)o1.owner(), (Type<?>)o2.owner());
  }

  /**
   * A utility method that maps the supplied {@link Collection} into
   * an unmodifiable {@link List} using the supplied mapping
   * function.
   *
   * @param <T> the type of the incoming elements
   *
   * @param <U> the type of the returned {@link Collection}'s elements
   *
   * @param c the incoming {@link Collection}; may be {@code null}
   *
   * @param f the mapping {@link Function}
   *
   * @return an unmodifiable mapped {@link List}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.  It does not synchronize on the supplied {@link
   * Collection} when iterating over it.
   */
  static final <T, U> List<U> map(final Collection<? extends T> c, final Function<? super T, ? extends U> f) {
    if (c == null || c.isEmpty()) {
      return List.of();
    }
    final Iterator<? extends T> i = c.iterator();
    switch (c.size()) {
    case 0:
      throw new AssertionError();
    case 1:
      return List.of(f.apply(i.next()));
    case 2:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()));
    case 3:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    case 4:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    case 5:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    case 6:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    case 7:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    case 8:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    case 9:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    case 10:
      return List.of(f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()),
                     f.apply(i.next()));
    default:
      final List<U> rv = new ArrayList<>(c.size());
      while (i.hasNext()) {
        rv.add(f.apply(i.next()));
      }
      return Collections.unmodifiableList(rv);
    }
  }

  /**
   * A utility method that maps the supplied array into an
   * unmodifiable {@link List} using the supplied mapping function.
   *
   * @param <T> the type of the incoming elements
   *
   * @param <U> the type of the returned {@link List}'s elements
   *
   * @param c the incoming array; may be {@code null}
   *
   * @param f the mapping {@link Function}
   *
   * @return an unmodifiable mapped {@link List}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.  It does not synchronize on the supplied array when
   * accessing its elements.
   */
  static final <T, U> List<U> map(final T[] c, final Function<? super T, ? extends U> f) {
    if (c == null) {
      return List.of();
    }
    switch (c.length) {
    case 0:
      return List.of();
    case 1:
      return List.of(f.apply(c[0]));
    case 2:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]));
    case 3:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]));
    case 4:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]),
                     f.apply(c[3]));
    case 5:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]),
                     f.apply(c[3]),
                     f.apply(c[4]));
    case 6:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]),
                     f.apply(c[3]),
                     f.apply(c[4]),
                     f.apply(c[5]));
    case 7:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]),
                     f.apply(c[3]),
                     f.apply(c[4]),
                     f.apply(c[5]),
                     f.apply(c[6]));
    case 8:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]),
                     f.apply(c[3]),
                     f.apply(c[4]),
                     f.apply(c[5]),
                     f.apply(c[6]),
                     f.apply(c[7]));
    case 9:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]),
                     f.apply(c[3]),
                     f.apply(c[4]),
                     f.apply(c[5]),
                     f.apply(c[6]),
                     f.apply(c[7]),
                     f.apply(c[8]));
    case 10:
      return List.of(f.apply(c[0]),
                     f.apply(c[1]),
                     f.apply(c[2]),
                     f.apply(c[3]),
                     f.apply(c[4]),
                     f.apply(c[5]),
                     f.apply(c[6]),
                     f.apply(c[7]),
                     f.apply(c[8]),
                     f.apply(c[9]));
    default:
      final List<U> rv = new ArrayList<>(c.length);
      for (int i = 0; i < c.length; i++) {
        rv.add(f.apply(c[i]));
      }
      return Collections.unmodifiableList(rv);
    }
  }

  private static final <X> List<? extends Type<X>> computeSupertypesBreadthFirst(final Type<X> type) {
    final ArrayList<Type<X>> supertypes = new ArrayList<>(16);
    final Queue<Type<X>> q = new ArrayDeque<>(8);
    q.add(type);
    while (!q.isEmpty()) {
      final Type<X> head = q.remove();
      supertypes.add(head);
      q.addAll(head.directSupertypes());
    }
    supertypes.trimToSize();
    return Collections.unmodifiableList(supertypes);
  }


  /*
   * Inner and nested classes.
   */


  /**
   * An abstract embodiment of {@link Type} {@linkplain
   * #assignable(Type, Type) assignability rules}.
   *
   * <p>By default, no {@link Type} {@linkplain #assignable(Type,
   * Type) is assignable} to any other {@link Type} except itself.
   * Subclasses will obviously want to override this behavior in some,
   * if not all, cases.</p>
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see VariantSemantics
   *
   * @see InvariantSemantics
   *
   * @see CovariantSemantics
   *
   * @see CdiSemantics
   */
  @Experimental
  public static abstract class Semantics {


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link Semantics}.
     */
    protected Semantics() {
      super();
    }


    /*
     * Instance methods.
     */


    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadType} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverType}, according to the assignability rules modeled by
     * this {@link Semantics} instance, and using no autoboxing.
     *
     * <p>This is a convenience method that {@linkplain
     * JavaType#of(boolean, java.lang.reflect.Type) creates
     * <code>JavaType</code>s} to represent the supplied {@link
     * java.lang.reflect.Type}s before calling the (canonical) {@link
     * #assignable(Type, Type)} method.</p>
     *
     * @param receiverType the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadType the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadType} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverType}, according to the assignability rules modeled by
     * this {@link Semantics} instance, and using no autoboxing;
     * {@code false} otherwise
     *
     * @exception NullPointerException if either {@code receiverType}
     * or {@code payloadType} is {@code null}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     *
     * @see JavaType#of(boolean, java.lang.reflect.Type)
     */
    @Convenience
    public boolean assignable(final java.lang.reflect.Type receiverType,
                              final java.lang.reflect.Type payloadType) {
      return this.assignable(receiverType, payloadType, false);
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadType} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverType}, according to the assignability rules modeled by
     * this {@link Semantics} instance, and using the specified
     * autoboxing semantics.
     *
     * <p>This is a convenience method that {@linkplain
     * JavaType#of(boolean, java.lang.reflect.Type) creates
     * <code>JavaType</code>s} to represent the supplied {@link
     * java.lang.reflect.Type}s before calling the (canonical) {@link
     * #assignable(Type, Type)} method.</p>
     *
     * @param receiverType the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadType the payload type as described above; must
     * not be {@code null}
     *
     * @param box whether autoboxing is enabled
     *
     * @return {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadType} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverType}, according to the assignability rules modeled by
     * this {@link Semantics} instance, and using the specified
     * autoboxing semantics; {@code false} otherwise
     *
     * @exception NullPointerException if either {@code receiverType}
     * or {@code payloadType} is {@code null}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     *
     * @see JavaType#of(boolean, java.lang.reflect.Type)
     */
    @Convenience
    public boolean assignable(final java.lang.reflect.Type receiverType,
                              final java.lang.reflect.Type payloadType,
                              final boolean box) {
      return this.assignable(JavaType.of(box, receiverType), JavaType.of(box, payloadType));
    }

    /**
     * Returns {@code true} if and only if a reference bearing at
     * least one of the types modeled by the supplied {@code
     * payloadTypes} is assignable to a reference bearing the type
     * modeled by the supplied {@code receiverType}, according to the
     * assignability rules modeled by this {@link Semantics} instance.
     *
     * <p>No autoboxing of types takes place.</p>
     *
     * @param receiverType the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadTypes the payload types as described above; must
     * not be {@code null}
     *
     * @return {@code true} if and only if a reference bearing at
     * least one of the types modeled by the supplied {@code
     * payloadTypes} is assignable to a reference bearing the type
     * modeled by the supplied {@code receiverType}, according to the
     * assignability rules modeled by this {@link Semantics} instance;
     * {@code false} otherwise
     *
     * @exception NullPointerException if either {@code receiverType}
     * or {@code payloadTypes} is {@code null}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #anyAssignable(java.lang.reflect.Type, Collection,
     * boolean)
     */
    @Convenience
    public final boolean anyAssignable(final java.lang.reflect.Type receiverType,
                                       final Collection<? extends java.lang.reflect.Type> payloadTypes) {
      return this.anyAssignable(receiverType, payloadTypes, false);
    }

    /**
     * Returns {@code true} if and only if a reference bearing at
     * least one of the types modeled by the supplied {@code
     * payloadTypes} is assignable to a reference bearing the type
     * modeled by the supplied {@code receiverType}, according to the
     * assignability rules modeled by this {@link Semantics} instance.
     *
     * @param receiverType the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadTypes the payload types as described above; must
     * not be {@code null}
     *
     * @param box whether autoboxing is enabled
     *
     * @return {@code true} if and only if a reference bearing at
     * least one of the types modeled by the supplied {@code
     * payloadTypes} is assignable to a reference bearing the type
     * modeled by the supplied {@code receiverType}, according to the
     * assignability rules modeled by this {@link Semantics} instance;
     * {@code false} otherwise
     *
     * @exception NullPointerException if either {@code receiverType}
     * or {@code payloadTypes} is {@code null}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     */
    @Convenience
    public final boolean anyAssignable(final java.lang.reflect.Type receiverType,
                                       final Collection<? extends java.lang.reflect.Type> payloadTypes,
                                       final boolean box) {
      final JavaType receiverJavaType = JavaType.of(box, receiverType);
      for (final java.lang.reflect.Type payloadType : payloadTypes) {
        if (this.assignable(receiverJavaType, JavaType.of(box, payloadType))) {
          return true;
        }
      }
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing at
     * least one of the types modeled by the supplied {@code
     * payloadTypes} is assignable to a reference bearing the type
     * modeled by the supplied {@code receiverType}, according to the
     * assignability rules modeled by this {@link Semantics} instance.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by a {@code payloadTypes}
     * element; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverType the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadTypes the payload types as described above; must
     * not be {@code null}
     *
     * @return {@code true} if and only if a reference bearing at
     * least one of the types modeled by the supplied {@code
     * payloadTypes} is assignable to a reference bearing the type
     * modeled by the supplied {@code receiverType}, according to the
     * assignability rules modeled by this {@link Semantics} instance;
     * {@code false} otherwise
     *
     * @exception NullPointerException if either {@code receiverType}
     * or {@code payloadTypes} is {@code null}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     */
    @Convenience
    public final <X, Y> boolean anyAssignable(final Type<X> receiverType,
                                              final Collection<? extends Type<Y>> payloadTypes) {
      for (final Type<Y> payloadType : payloadTypes) {
        if (this.assignable(receiverType, payloadType)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadType} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverType}, according to the assignability rules modeled by
     * this {@link Semantics} instance.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadType};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverType the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadType the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadType} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverType}, according to the assignability rules modeled by
     * this {@link Semantics} instance; {@code false} otherwise
     *
     * @exception NullPointerException if either {@code receiverType}
     * or {@code payloadType} is {@code null}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     */
    @EntryPoint
    @OverridingDiscouraged
    public <X, Y> boolean assignable(final Type<X> receiverType, final Type<Y> payloadType) {
      if (receiverType == Objects.requireNonNull(payloadType, "payloadType")) {
        return true;
      } else if (receiverType.hasTypeArguments()) {
        return parameterizedTypeIsAssignableFromType(receiverType, payloadType);
      } else if (payloadType.hasTypeArguments()) {
        return this.nonParameterizedTypeIsAssignableFromParameterizedType(receiverType, payloadType);
      } else if (receiverType.componentType() == null) {
        if (receiverType.lowerBounded()) {
          return this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, true, payloadType);
        } else if (receiverType.upperBounded()) {
          if (receiverType.named()) {
            return this.typeVariableIsAssignableFromNonParameterizedType(receiverType, payloadType);
          } else {
            return this.wildcardTypeIsAssignableFromNonParameterizedType(receiverType, false, payloadType);
          }
        } else {
          return this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
        }
      } else if (receiverType.type() == receiverType) {
        return this.classIsAssignableFromNonParameterizedType(receiverType, payloadType);
      } else {
        return this.genericArrayTypeIsAssignableFromNonParameterizedType(receiverType, payloadType);
      }
    }

    private final <X, Y> boolean nonParameterizedTypeIsAssignableFromParameterizedType(final Type<X> receiverNonParameterizedType,
                                                                                       final Type<Y> payloadParameterizedType) {
      if (receiverNonParameterizedType.componentType() == null) {
        if (receiverNonParameterizedType.lowerBounded()) {
          return this.wildcardTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, true /* lower bounded */, payloadParameterizedType);
        } else if (receiverNonParameterizedType.upperBounded()) {
          if (receiverNonParameterizedType.named()) {
            return this.typeVariableIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
          } else {
            return this.wildcardTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, false /* upper bounded */, payloadParameterizedType);
          }
        } else {
          return this.classIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
        }
      } else if (receiverNonParameterizedType.type() == receiverNonParameterizedType) {
        return this.classIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
      } else {
        return this.genericArrayTypeIsAssignableFromParameterizedType(receiverNonParameterizedType, payloadParameterizedType);
      }
    }

    private final <X, Y> boolean classIsAssignableFromNonParameterizedType(final Type<X> receiverClass,
                                                                           final Type<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.classIsAssignableFromWildcardType(receiverClass, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.classIsAssignableFromTypeVariable(receiverClass, payloadNonParameterizedType);
          } else {
            return this.classIsAssignableFromWildcardType(receiverClass, payloadNonParameterizedType, false);
          }
        } else {
          return this.classIsAssignableFromClass(receiverClass, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.classIsAssignableFromClass(receiverClass, payloadNonParameterizedType);
      } else {
        return this.classIsAssignableFromGenericArrayType(receiverClass, payloadNonParameterizedType);
      }
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadClass} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverClass}, according to the assignability rules modeled by
     * this {@link Semantics} instance, and if and only if {@code
     * payloadClass} models a {@linkplain Class Java class} and not
     * any other type, and if and only if {@code receiverClass} models
     * a {@linkplain Class Java class} and not any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadClass};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverClass the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadClass the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean classIsAssignableFromClass(final Type<X> receiverClass, final Type<Y> payloadClass) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadClass} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverClass}, according to the assignability rules modeled by
     * this {@link Semantics} instance, and if and only if {@code
     * payloadParameterizedType} models a Java {@linkplain
     * java.lang.reflect.ParameterizedType parameterized type} and not
     * any other type, and if and only if {@code receiverClass} models
     * a {@linkplain Class Java class} and not any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverClass the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadParameterizedType the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean classIsAssignableFromParameterizedType(final Type<X> receiverClass, final Type<Y> payloadParameterizedType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadGenericArrayType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverClass}, according to the assignability
     * rules modeled by this {@link Semantics} instance, and if and
     * only if {@code payloadGenericArrayType} models a Java
     * {@linkplain java.lang.reflect.GenericArrayType generic array
     * type} and not any other type, and if and only if {@code
     * receiverClass} models a {@linkplain Class Java class} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadGenericArrayType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverClass the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadGenericArrayType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean classIsAssignableFromGenericArrayType(final Type<X> receiverClass, final Type<Y> payloadGenericArrayType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadTypeVariable} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverClass}, according to the assignability
     * rules modeled by this {@link Semantics} instance, and if and
     * only if {@code payloadTypeVariable} models a Java {@linkplain
     * java.lang.reflect.TypeVariable type variable} and not any other
     * type, and if and only if {@code receiverClass} models a
     * {@linkplain Class Java class} and not any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadTypeVariable};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverClass the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadTypeVariable the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean classIsAssignableFromTypeVariable(final Type<X> receiverClass, final Type<Y> payloadTypeVariable) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadWildcardType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverClass}, according to the assignability
     * rules modeled by this {@link Semantics} instance, and if and
     * only if {@code payloadWildcardType} models a Java {@linkplain
     * java.lang.reflect.WildcardType wildcard type} and not any other
     * type, and if and only if {@code receiverClass} models a
     * {@linkplain Class Java class} and not any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadWildcardType};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverClass the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadWildcardType the payload type as described above; must
     * not be {@code null}
     *
     * @param lowerBounded whether {@code payloadWildcardType} is a
     * lower-bounded wildcard type
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean classIsAssignableFromWildcardType(final Type<X> receiverClass, final Type<Y> payloadWildcardType, final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean parameterizedTypeIsAssignableFromType(final Type<X> receiverParameterizedType, final Type<Y> payloadType) {
      if (payloadType.hasTypeArguments()) {
        return this.parameterizedTypeIsAssignableFromParameterizedType(receiverParameterizedType, payloadType);
      } else if (payloadType.componentType() == null) {
        if (payloadType.lowerBounded()) {
          return this.parameterizedTypeIsAssignableFromWildcardType(receiverParameterizedType, payloadType, true);
        } else if (payloadType.upperBounded()) {
          if (payloadType.named()) {
            return this.parameterizedTypeIsAssignableFromTypeVariable(receiverParameterizedType, payloadType);
          } else {
            return this.parameterizedTypeIsAssignableFromWildcardType(receiverParameterizedType, payloadType, false);
          }
        } else {
          return this.parameterizedTypeIsAssignableFromClass(receiverParameterizedType, payloadType);
        }
      } else if (payloadType.type() == payloadType) {
        return this.parameterizedTypeIsAssignableFromClass(receiverParameterizedType, payloadType);
      } else {
        return this.parameterizedTypeIsAssignableFromGenericArrayType(receiverParameterizedType, payloadType);
      }
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadClass} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverParameterizedType}, according to the assignability
     * rules modeled by this {@link Semantics} instance, and if and
     * only if {@code payloadClass} models a Java {@linkplain Class
     * class} and not any other type, and if and only if {@code
     * receiverParameterizedType} models a {@linkplain
     * java.lang.reflect.ParameterizedType parameterized type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverParameterizedType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadClass the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean parameterizedTypeIsAssignableFromClass(final Type<X> receiverParameterizedType,
                                                                    final Type<Y> payloadClass) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadParameterizedType}
     * is assignable to a reference bearing the type modeled by the
     * supplied {@code receiverParameterizedType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadParameterizedType} models a
     * Java {@linkplain java.lang.reflect.ParameterizedType
     * parameterized type} and not any other type, and if and only if
     * {@code receiverParameterizedType} models a {@linkplain
     * java.lang.reflect.ParameterizedType parameterized type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverParameterizedType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadParameterizedType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean parameterizedTypeIsAssignableFromParameterizedType(final Type<X> receiverParameterizedType,
                                                                                final Type<Y> payloadParameterizedType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadGenericArrayType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverParameterizedType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadGenericArrayType} models a
     * Java {@linkplain java.lang.reflect.GenericArrayType generic
     * array type} and not any other type, and if and only if {@code
     * receiverParameterizedType} models a {@linkplain
     * java.lang.reflect.ParameterizedType parameterized type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadGenericArrayType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverParameterizedType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadGenericArrayType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean parameterizedTypeIsAssignableFromGenericArrayType(final Type<X> receiverParameterizedType, final Type<Y> payloadGenericArrayType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadTypeVariable} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverParameterizedType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadTypeVariable} models a Java
     * {@linkplain java.lang.reflect.TypeVariable type variable} and
     * not any other type, and if and only if {@code
     * receiverParameterizedType} models a {@linkplain
     * java.lang.reflect.ParameterizedType parameterized type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadTypeVariable}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverParameterizedType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadTypeVariable the payload type as described above;
     * must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean parameterizedTypeIsAssignableFromTypeVariable(final Type<X> receiverParameterizedType, final Type<Y> payloadTypeVariable) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadWildcardType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverParameterizedType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadWildcardType} models a Java
     * {@linkplain java.lang.reflect.WildcardType wildcard type} and
     * not any other type, and if and only if {@code
     * receiverParameterizedType} models a {@linkplain
     * java.lang.reflect.ParameterizedType parameterized type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadWildcardType};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverParameterizedType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadWildcardType the payload type as described above; must
     * not be {@code null}
     *
     * @param lowerBounded whether {@code payloadWildcardType} is a
     * lower-bounded wildcard type
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean parameterizedTypeIsAssignableFromWildcardType(final Type<X> receiverParameterizedType,
                                                                           final Type<Y> payloadWildcardType,
                                                                           final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean genericArrayTypeIsAssignableFromNonParameterizedType(final Type<X> receiverGenericArrayType,
                                                                                      final Type<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.genericArrayTypeIsAssignableFromWildcardType(receiverGenericArrayType, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.genericArrayTypeIsAssignableFromTypeVariable(receiverGenericArrayType, payloadNonParameterizedType);
          } else {
            return this.genericArrayTypeIsAssignableFromWildcardType(receiverGenericArrayType, payloadNonParameterizedType, false);
          }
        } else {
          return this.genericArrayTypeIsAssignableFromClass(receiverGenericArrayType, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.genericArrayTypeIsAssignableFromClass(receiverGenericArrayType, payloadNonParameterizedType);
      } else {
        return this.genericArrayTypeIsAssignableFromGenericArrayType(receiverGenericArrayType, payloadNonParameterizedType);
      }
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadClass} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverGenericArrayType}, according to the assignability rules
     * modeled by this {@link Semantics} instance, and if and only if
     * {@code payloadClass} models a Java {@linkplain Class class} and
     * not any other type, and if and only if {@code
     * receiverGenericArrayType} models a {@linkplain
     * java.lang.reflect.GenericArrayType generic array type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadClass};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverGenericArrayType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadClass the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean genericArrayTypeIsAssignableFromClass(final Type<X> receiverGenericArrayType, final Type<Y> payloadClass) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadParameterizedType}
     * is assignable to a reference bearing the type modeled by the
     * supplied {@code receiverGenericArrayType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadParameterizedType} models a
     * Java {@linkplain java.lang.reflect.ParameterizedType
     * parameterized type} and not any other type, and if and only if
     * {@code receiverGenericArrayType} models a {@linkplain
     * java.lang.reflect.GenericArrayType generic array type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverGenericArrayType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadParameterizedType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean genericArrayTypeIsAssignableFromParameterizedType(final Type<X> receiverGenericArrayType, final Type<Y> payloadParameterizedType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadGenericArrayType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverGenericArrayType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadGenericArrayType} models a
     * Java {@linkplain java.lang.reflect.GenericArrayType generic
     * array type} and not any other type, and if and only if {@code
     * receiverGenericArrayType} models a {@linkplain
     * java.lang.reflect.GenericArrayType generic array type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadGenericArrayType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverGenericArrayType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadGenericArrayType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean genericArrayTypeIsAssignableFromGenericArrayType(final Type<X> receiverGenericArrayType, final Type<Y> payloadGenericArrayType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadTypeVariable} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverGenericArrayType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadTypeVariable} models a Java
     * {@linkplain java.lang.reflect.TypeVariable type variable} and
     * not any other type, and if and only if {@code
     * receiverGenericArrayType} models a {@linkplain
     * java.lang.reflect.GenericArrayType generic array type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadTypeVariable}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverGenericArrayType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadTypeVariable the payload type as described above;
     * must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean genericArrayTypeIsAssignableFromTypeVariable(final Type<X> receiverGenericArrayType, final Type<Y> payloadTypeVariable) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadWildcardType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverGenericArrayType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadWildcardType} models a Java
     * {@linkplain java.lang.reflect.WildcardType wildcard type} and
     * not any other type, and if and only if {@code
     * receiverGenericArrayType} models a {@linkplain
     * java.lang.reflect.GenericArrayType generic array type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverGenericArrayType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadWildcardType};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverGenericArrayType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadWildcardType the payload type as described above; must
     * not be {@code null}
     *
     * @param lowerBounded whether {@code payloadWildcardType} is a
     * lower-bounded wildcard type
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean genericArrayTypeIsAssignableFromWildcardType(final Type<X> receiverGenericArrayType,
                                                                          final Type<Y> payloadWildcardType,
                                                                          final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean typeVariableIsAssignableFromNonParameterizedType(final Type<X> receiverTypeVariable,
                                                                                  final Type<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.typeVariableIsAssignableFromWildcardType(receiverTypeVariable, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.typeVariableIsAssignableFromTypeVariable(receiverTypeVariable, payloadNonParameterizedType);
          } else {
            return this.typeVariableIsAssignableFromWildcardType(receiverTypeVariable, payloadNonParameterizedType, false);
          }
        } else {
          return this.typeVariableIsAssignableFromClass(receiverTypeVariable, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.typeVariableIsAssignableFromClass(receiverTypeVariable, payloadNonParameterizedType);
      } else {
        return this.typeVariableIsAssignableFromGenericArrayType(receiverTypeVariable, payloadNonParameterizedType);
      }
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadClass} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverTypeVariable}, according to the assignability rules
     * modeled by this {@link Semantics} instance, and if and only if
     * {@code payloadClass} models a Java {@linkplain Class class} and
     * not any other type, and if and only if {@code
     * receiverTypeVariable} models a {@linkplain
     * java.lang.reflect.TypeVariable type variable} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverTypeVariable the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadClass the payload type as described above;
     * must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean typeVariableIsAssignableFromClass(final Type<X> receiverTypeVariable, final Type<Y> payloadClass) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadParameterizedType}
     * is assignable to a reference bearing the type modeled by the
     * supplied {@code receiverTypeVariable}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadParameterizedType} models a
     * Java {@linkplain java.lang.reflect.ParameterizedType
     * parameterized type} and not any other type, and if and only if
     * {@code receiverTypeVariable} models a {@linkplain
     * java.lang.reflect.TypeVariable type variable} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadParameterizedType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverTypeVariable the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadParameterizedType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean typeVariableIsAssignableFromParameterizedType(final Type<X> receiverTypeVariable, final Type<Y> payloadParameterizedType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadGenericArrayType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverTypeVariable}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadGenericArrayType} models a
     * Java {@linkplain java.lang.reflect.GenericArrayType generic
     * array type} and not any other type, and if and only if {@code
     * receiverTypeVariable} models a {@linkplain
     * java.lang.reflect.TypeVariable type variable} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadGenericArrayType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverTypeVariable the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadGenericArrayType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean typeVariableIsAssignableFromGenericArrayType(final Type<X> receiverTypeVariable, final Type<Y> payloadGenericArrayType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadTypeVariable} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverTypeVariable}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadTypeVariable} models a Java
     * {@linkplain java.lang.reflect.TypeVariable type variable} and
     * not any other type, and if and only if {@code
     * receiverTypeVariable} models a {@linkplain
     * java.lang.reflect.TypeVariable type variable} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadTypeVariable}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverTypeVariable the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadTypeVariable the payload type as described above;
     * must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean typeVariableIsAssignableFromTypeVariable(final Type<X> receiverTypeVariable, final Type<Y> payloadTypeVariable) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadWildcardType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverTypeVariable}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadWildcardType} models a Java
     * {@linkplain java.lang.reflect.WildcardType wildcard type} and
     * not any other type, and if and only if {@code
     * receiverTypeVariable} models a {@linkplain
     * java.lang.reflect.TypeVariable type variable} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverTypeVariable}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadWildcardType};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverTypeVariable the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadWildcardType the payload type as described above; must
     * not be {@code null}
     *
     * @param lowerBounded whether {@code payloadWildcardType} is a
     * lower-bounded wildcard type
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean typeVariableIsAssignableFromWildcardType(final Type<X> receiverTypeVariable,
                                                                      final Type<Y> payloadWildcardType,
                                                                      final boolean lowerBounded) {
      return false;
    }

    private final <X, Y> boolean wildcardTypeIsAssignableFromNonParameterizedType(final Type<X> receiverWildcardType,
                                                                           final boolean lowerBounded,
                                                                           final Type<Y> payloadNonParameterizedType) {
      if (payloadNonParameterizedType.componentType() == null) {
        if (payloadNonParameterizedType.lowerBounded()) {
          return this.wildcardTypeIsAssignableFromWildcardType(receiverWildcardType, lowerBounded, payloadNonParameterizedType, true);
        } else if (payloadNonParameterizedType.upperBounded()) {
          if (payloadNonParameterizedType.named()) {
            return this.wildcardTypeIsAssignableFromTypeVariable(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
          } else {
            return this.wildcardTypeIsAssignableFromWildcardType(receiverWildcardType, lowerBounded, payloadNonParameterizedType, false);
          }
        } else {
          return this.wildcardTypeIsAssignableFromClass(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
        }
      } else if (payloadNonParameterizedType.type() == payloadNonParameterizedType) {
        return this.wildcardTypeIsAssignableFromClass(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
      } else {
        return this.wildcardTypeIsAssignableFromGenericArrayType(receiverWildcardType, lowerBounded, payloadNonParameterizedType);
      }
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadClass} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverWildcardType}, according to the assignability rules
     * modeled by this {@link Semantics} instance, and if and only if
     * {@code payloadClass} models a Java {@linkplain Class class} and
     * not any other type, and if and only if {@code
     * receiverWildcardType} models a {@linkplain
     * java.lang.reflect.WildcardType wildcard type} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverWildcardType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverWildcardType the receiver type as described
     * above; must not be {@code null}
     *
     * @param lowerBounded whether {@code receiverWildcardType} is a
     * lower-bounded wildcard type
     *
     * @param payloadClass the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean wildcardTypeIsAssignableFromClass(final Type<X> receiverWildcardType,
                                                               final boolean lowerBounded,
                                                               final Type<Y> payloadClass) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadParameterizedType}
     * is assignable to a reference bearing the type modeled by the
     * supplied {@code receiverWildcardType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadParameterizedType} models a
     * Java {@linkplain java.lang.reflect.ParameterizedType
     * parameterized type} and not any other type, and if and only if
     * {@code receiverWildcardType} models a {@linkplain
     * java.lang.reflect.WildcardType wildcard type} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverWildcardType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadParameterizedType}; often a {@link
     * java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverWildcardType the receiver type as described
     * above; must not be {@code null}
     *
     * @param lowerBounded whether {@code receiverWildcardType} is a
     * lower-bounded wildcard type
     *
     * @param payloadParameterizedType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean wildcardTypeIsAssignableFromParameterizedType(final Type<X> receiverWildcardType,
                                                                           final boolean lowerBounded,
                                                                           final Type<Y> payloadParameterizedType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadGenericArrayType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverWildcardType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadGenericArrayType} models a
     * Java {@linkplain java.lang.reflect.GenericArrayType generic
     * array type} and not any other type, and if and only if {@code
     * receiverWildcardType} models a {@linkplain
     * java.lang.reflect.WildcardType wildcard type} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverWildcardType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code
     * payloadGenericArrayType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param receiverWildcardType the receiver type as described
     * above; must not be {@code null}
     *
     * @param lowerBounded whether {@code receiverWildcardType} is a
     * lower-bounded wildcard type
     *
     * @param payloadGenericArrayType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean wildcardTypeIsAssignableFromGenericArrayType(final Type<X> receiverWildcardType,
                                                                          final boolean lowerBounded,
                                                                          final Type<Y> payloadGenericArrayType) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadTypeVariable} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverWildcardType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadTypeVariable} models a Java
     * {@linkplain java.lang.reflect.TypeVariable type variable} and
     * not any other type, and if and only if {@code
     * receiverWildcardType} models a {@linkplain
     * java.lang.reflect.WildcardType wildcard type} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverWildcardType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadTypeVariable};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverWildcardType the receiver type as described
     * above; must not be {@code null}
     *
     * @param lowerBounded whether {@code receiverWildcardType} is a
     * lower-bounded wildcard type
     *
     * @param payloadTypeVariable the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean wildcardTypeIsAssignableFromTypeVariable(final Type<X> receiverWildcardType,
                                                                      final boolean lowerBounded,
                                                                      final Type<Y> payloadTypeVariable) {
      return false;
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadWildcardType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverWildcardType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadWildcardType} models a Java
     * {@linkplain java.lang.reflect.WildcardType wildcard type} and
     * not any other type, and if and only if {@code
     * receiverWildcardType} models a {@linkplain
     * java.lang.reflect.WildcardType wildcard type} and not any other
     * type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code false} in all cases.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverWildcardType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadWildcardType};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverWildcardType the receiver type as described
     * above; must not be {@code null}
     *
     * @param receiverWildcardTypeLowerBounded whether {@code
     * receiverWildcardType} is a lower-bounded wildcard type
     *
     * @param payloadWildcardType the payload type as described above; must
     * not be {@code null}
     *
     * @param payloadWildcardTypeLowerBounded whether {@code
     * payloadWildcardType} is a lower-bounded wildcard type
     *
     * @return {@code false} in all cases
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @OverridingEncouraged
    protected <X, Y> boolean wildcardTypeIsAssignableFromWildcardType(final Type<X> receiverWildcardType,
                                                                      final boolean receiverWildcardTypeLowerBounded,
                                                                      final Type<Y> payloadWildcardType,
                                                                      final boolean payloadWildcardTypeLowerBounded) {
      return false;
    }

  }

  /**
   * An abstract partial {@link Semantics} that permits subtyping.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  @Experimental
  public static abstract class VariantSemantics extends Semantics {


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link VariantSemantics}.
     */
    protected VariantSemantics() {
      super();
    }


    /*
     * Instance methods.
     */


    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadClass} is assignable
     * to a reference bearing the type modeled by the supplied {@code
     * receiverClass}, according to the assignability rules modeled by
     * this {@link Semantics} instance, and if and only if {@code
     * payloadClass} models a {@linkplain Class Java class} and not
     * any other type, and if and only if {@code receiverClass} models
     * a {@linkplain Class Java class} and not any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code true} if and only if
     * {@link Type#supertypeOf(Type)
     * receiverClass.supertype(payloadClass)} returns {@code true}.
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverClass}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadClass};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverClass the receiver type as described above; must
     * not be {@code null}
     *
     * @param payloadClass the payload type as described above; must
     * not be {@code null}
     *
     * @return {@code true} if and only if {@link
     * Type#supertypeOf(Type) receiverClass.supertypeOf(payloadClass)}
     * returns {@code true}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @Override
    protected <X, Y> boolean classIsAssignableFromClass(final Type<X> receiverClass, final Type<Y> payloadClass) {
      return receiverClass.supertypeOf(payloadClass);
    }

    /**
     * Returns {@code true} if and only if a reference bearing the
     * type modeled by the supplied {@code payloadGenericArrayType} is
     * assignable to a reference bearing the type modeled by the
     * supplied {@code receiverGenericArrayType}, according to the
     * assignability rules modeled by this {@link Semantics} instance,
     * and if and only if {@code payloadGenericArrayType} models a
     * {@linkplain java.lang.reflect.GenericArrayType generic array
     * type} and not any other type, and if and only if {@code
     * receiverGenericArrayType} models a {@linkplain
     * java.lang.reflect.GenericArrayType generic array type} and not
     * any other type.
     *
     * <p>This method is called by the {@link #assignable(Type, Type)}
     * method.</p>
     *
     * <p>This implementation returns {@code true} if and only if the
     * {@linkplain Type#componentType() component type} of the {@code
     * receiverGenericArrayType} {@linkplain #assignable(Type, Type)
     * is assignable from} the {@linkplain Type#componentType()
     * component type} of the {@code payloadGenericArrayType}.</p>
     *
     * @param <X> the kind of type modeled by the {@code
     * receiverGenericArrayType}; often a {@link java.lang.reflect.Type
     * java.lang.reflect.Type}
     *
     * @param <Y> the kind of type modeled by the {@code payloadClass};
     * often a {@link java.lang.reflect.Type java.lang.reflect.Type}
     *
     * @param receiverGenericArrayType the receiver type as described
     * above; must not be {@code null}
     *
     * @param payloadGenericArrayType the payload type as described
     * above; must not be {@code null}
     *
     * @return {@code true} if and only if the {@linkplain
     * Type#componentType() component type} of the {@code
     * receiverGenericArrayType} {@linkplain #assignable(Type, Type)
     * is assignable from} the {@linkplain Type#componentType()
     * component type} of the {@code payloadGenericArrayType}
     *
     * @idempotency This method is, and its overrides must be,
     * idempotent and deterministic.
     *
     * @threadsafety This method is, and its overrides must be, safe
     * for concurrent use by multiple threads.
     *
     * @see #assignable(Type, Type)
     */
    @Override
    protected final <X, Y> boolean genericArrayTypeIsAssignableFromGenericArrayType(final Type<X> receiverGenericArrayType,
                                                                                    final Type<Y> payloadGenericArrayType) {
      return this.assignable(receiverGenericArrayType.componentType(), payloadGenericArrayType.componentType());
    }

    final <X> List<? extends Type<X>> condense(final List<? extends Type<X>> bounds) {
      if (!bounds.isEmpty()) {
        final Type<X> firstBound = bounds.get(0);
        if (firstBound.named() && firstBound.upperBounded()) {
          // it's a type variable
          return this.condense(firstBound.upperBounds());
        }
      }
      return bounds;
    }

  }


  /**
   * A {@link VariantSemantics} implementation that implements Java
   * type assignability semantics, which are covariant.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  @Experimental
  public static class CovariantSemantics extends VariantSemantics {


    /*
     * Static fields.
     */


    /**
     * A convenient instance of {@link CovariantSemantics}.
     *
     * @nullability This field is never {@code null}.
     */
    public static final CovariantSemantics INSTANCE = new CovariantSemantics();


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link CovariantSemantics}.
     */
    public CovariantSemantics() {
      super();
    }


    /*
     * Instance methods.
     */


    @Override
    protected <X, Y> boolean classIsAssignableFromParameterizedType(final Type<X> receiverClass, final Type<Y> payloadParameterizedType) {
      return this.assignable(receiverClass, payloadParameterizedType.type());
    }

    @Override
    protected final <X, Y> boolean classIsAssignableFromGenericArrayType(final Type<X> receiverClass, final Type<Y> payloadGenericArrayType) {
      final Type<X> receiverComponentType = receiverClass.componentType();
      return
        this.assignable(receiverComponentType == null ? receiverClass : receiverComponentType,
                        payloadGenericArrayType.componentType());
    }

    @Override
    protected final <X, Y> boolean classIsAssignableFromTypeVariable(final Type<X> receiverType, final Type<Y> payloadType) {
      for (final Type<Y> bound : payloadType.upperBounds()) {
        if (this.assignable(receiverType, bound)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected <X, Y> boolean parameterizedTypeIsAssignableFromClass(final Type<X> receiverParameterizedType, final Type<Y> payloadClass) {
      // Raw types are assignable and either the payload is a generic
      // class (and therefore a raw class) or the payload is a class
      // whose parameterized supertypes need to be evaluated.
      return
        this.assignable(receiverParameterizedType.type(), payloadClass) &&
        (payloadClass.hasTypeParameters() ||
         this.parameterizedTypeIsAssignableFromAnyType(receiverParameterizedType, payloadClass.supertypes()));
    }

    @Override
    protected <X, Y> boolean parameterizedTypeIsAssignableFromParameterizedType(final Type<X> receiverParameterizedType,
                                                                                final Type<Y> payloadParameterizedType) {
      return this.parameterizedTypeIsAssignableFromAnyType(receiverParameterizedType, payloadParameterizedType.supertypes());
    }

    private final boolean parameterizedTypeIsAssignableFromAnyType(final Type<?> receiverParameterizedType,
                                                                   final Iterable<? extends Type<?>> payloadTypes) {
      for (final Type<?> payloadType : payloadTypes) {
        if (payloadType.hasTypeArguments() &&
            this.parameterizedTypeIsAssignableFromParameterizedType0(receiverParameterizedType, payloadType)) {
          return true;
        }
      }
      return false;
    }

    private final boolean parameterizedTypeIsAssignableFromParameterizedType0(final Type<?> receiverParameterizedType,
                                                                              final Type<?> payloadParameterizedType) {
      if (Type.equals(receiverParameterizedType.type(), payloadParameterizedType.type())) {
        final List<? extends Type<?>> receiverTypeTypeArguments = receiverParameterizedType.typeArguments();
        final List<? extends Type<?>> payloadTypeTypeArguments = payloadParameterizedType.typeArguments();
        if (receiverTypeTypeArguments.size() == payloadTypeTypeArguments.size()) {
          for (int i = 0; i < receiverTypeTypeArguments.size(); i++) {
            final Type<?> receiverTypeTypeArgument = receiverTypeTypeArguments.get(i);
            final Type<?> payloadTypeTypeArgument = payloadTypeTypeArguments.get(i);
            if (receiverTypeTypeArgument.wildcard() || payloadTypeTypeArgument.wildcard()) {
              if (!this.assignable(receiverTypeTypeArgument, payloadTypeTypeArgument)) {
                return false;
              }
            } else if (!Type.equals(receiverTypeTypeArgument, payloadTypeTypeArgument)) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }

    @Override
    protected final <X, Y> boolean parameterizedTypeIsAssignableFromTypeVariable(final Type<X> receiverParameterizedType,
                                                                                 final Type<Y> payloadTypeVariable) {
      for (final Type<Y> bound : payloadTypeVariable.upperBounds()) {
        if (this.assignable(receiverParameterizedType, bound)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected final <X, Y> boolean genericArrayTypeIsAssignableFromClass(final Type<X> receiverGenericArrayType,
                                                                         final Type<Y> payloadClass) {
      return this.assignable(receiverGenericArrayType.componentType(), payloadClass.componentType());
    }


    @Override
    protected final <X, Y> boolean typeVariableIsAssignableFromTypeVariable(final Type<X> receiverTypeVariable,
                                                                            final Type<Y> payloadTypeVariable) {
      return receiverTypeVariable.supertypeOf(payloadTypeVariable);
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromClass(final Type<X> receiverWildcardType,
                                                                     final boolean lowerBounded,
                                                                     final Type<Y> payloadClass) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadClass) &&
        (!lowerBounded || this.assignable(payloadClass, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromParameterizedType(final Type<X> receiverWildcardType,
                                                                                 final boolean lowerBounded,
                                                                                 final Type<Y> payloadParameterizedType) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadParameterizedType) &&
        (!lowerBounded || this.assignable(payloadParameterizedType, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromGenericArrayType(final Type<X> receiverWildcardType,
                                                                                final boolean lowerBounded,
                                                                                final Type<Y> payloadGenericArrayType) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadGenericArrayType) &&
        (!lowerBounded || this.assignable(payloadGenericArrayType, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromTypeVariable(final Type<X> receiverWildcardType,
                                                                            final boolean lowerBounded,
                                                                            final Type<Y> payloadTypeVariable) {
      return
        this.assignable(receiverWildcardType.upperBounds().get(0), payloadTypeVariable) &&
        (!lowerBounded || this.assignable(payloadTypeVariable, receiverWildcardType.lowerBounds().get(0)));
    }

    @Override
    protected final <X, Y> boolean wildcardTypeIsAssignableFromWildcardType(final Type<X> receiverWildcardType,
                                                                            final boolean receiverWildcardTypeLowerBounded,
                                                                            final Type<Y> payloadWildcardType,
                                                                            final boolean payloadWildcardTypeLowerBounded) {
      final Type<X> receiverWildcardTypeUpperBound = receiverWildcardType.upperBounds().get(0);
      if (this.assignable(receiverWildcardTypeUpperBound, payloadWildcardType.upperBounds().get(0))) {
        if (receiverWildcardTypeLowerBounded) {
          return
            payloadWildcardTypeLowerBounded &&
            this.assignable(payloadWildcardType.lowerBounds().get(0), receiverWildcardType.lowerBounds().get(0));
        }
        return
          !payloadWildcardTypeLowerBounded ||
          this.assignable(receiverWildcardTypeUpperBound, payloadWildcardType.lowerBounds().get(0));
      }
      return false;
    }


  }

  /**
   * A {@link VariantSemantics} implementation that does not permit
   * subtyping, but that compares wildcard types covariantly.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  @Experimental
  public static class InvariantSemantics extends VariantSemantics {


    /*
     * Static fields.
     */


    /**
     * An instance of {@link InvariantSemantics}.
     *
     * @nullability This field is never {@code null}.
     */
    public static final InvariantSemantics INSTANCE = new InvariantSemantics();


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link InvariantSemantics}.
     */
    public InvariantSemantics() {
      super();
    }


    /*
     * Instance methods.
     */


    @Override // VariantSemantics
    public <X, Y> boolean assignable(final Type<X> receiverType, final Type<Y> payloadType) {
      if (receiverType.wildcard() || payloadType.wildcard()) {
        return CovariantSemantics.INSTANCE.assignable(receiverType, payloadType);
      }
      return Type.equals(receiverType, payloadType);
    }

  }

  /**
   * A {@link VariantSemantics} implementation that implements the
   * rules of <a
   * href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#typesafe_resolution"
   * target="_parent"><em>typesafe resolution</em></a>.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   */
  @Experimental
  public static class CdiSemantics extends VariantSemantics {


    /*
     * Static fields.
     */


    /**
     * An instance of {@link CdiSemantics}.
     *
     * @nullability This field is never {@code null}.
     */
    public static final CdiSemantics INSTANCE = new CdiSemantics();


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link CdiSemantics}.
     */
    public CdiSemantics() {
      super();
    }


    /*
     * Instance methods.
     */


    /**
     * Returns {@code true} if and only if the supplied {@link Type}
     * represents an <em>actual type</em>, which the <a
     * href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#assignable_parameters"
     * target="_parent">CDI specification</a> implies, but does not
     * explicitly state, is either a class, a parameterized type or a
     * generic array type.
     *
     * <p>Actual types, though not defined, are <a
     * href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#typesafe_resolution"
     * target="_parent">critically important to CDI's concept of
     * <em>typesafe resolution</em></a>.</p>
     *
     * <p>The concept of an actual type is not mentioned in the <a
     * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.1"
     * target="_parent">Java Language Specification</a>.
     *
     * @param type the {@link Type} in question; must not be {@code
     * null}
     *
     * @return {@code true} if and only if the supplied {@link Type}
     * returns {@code false} from invocations of both its {@link
     * #upperBounded()} and {@link #lowerBounded()} methods; {@code
     * false} otherwise
     *
     * @exception NullPointerException if {@code type} is {@code null}
     *
     * @idempotency This method is idempotent and deterministic.
     *
     * @threadsafety This method is safe for concurrent use by
     * multiple threads.
     */
    public final boolean actualType(final Type<?> type) {
      return !type.upperBounded() && !type.lowerBounded();
    }

    @Convenience
    @Override
    public final boolean assignable(final java.lang.reflect.Type receiverType,
                                    final java.lang.reflect.Type payloadType,
                                    final boolean ignoredBox) {
      // Boxing is always required in CDI.
      return this.assignable(JavaType.of(true, receiverType), JavaType.of(true, payloadType));
    }

    @Override
    protected final <X, Y> boolean classIsAssignableFromParameterizedType(final Type<X> receiverClass,
                                                                          final Type<Y> payloadParameterizedType) {
      if (Type.equals(receiverClass, payloadParameterizedType.type())) {
        for (final Type<Y> payloadTypeTypeArgument : payloadParameterizedType.typeArguments()) {
          if (payloadTypeTypeArgument.top()) {
            // OK; it's Object.class or similar
          } else if (payloadTypeTypeArgument.typeVariable()) {
            final List<? extends Type<Y>> bounds = payloadTypeTypeArgument.upperBounds();
            switch (bounds.size()) {
            case 0:
              // OK; it's effectively "extends Object"
              break;
            case 1:
              if (!bounds.get(0).top()) {
                return false;
              }
              break;
            default:
              return false;
            }
          } else {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    @Override
    protected final <X, Y> boolean parameterizedTypeIsAssignableFromClass(final Type<X> receiverParameterizedType,
                                                                          final Type<Y> payloadClass) {
      if (Type.equals(receiverParameterizedType.type(), payloadClass)) {
        for (final Type<X> receiverTypeTypeArgument : receiverParameterizedType.typeArguments()) {
          if (receiverTypeTypeArgument.top()) {
            // OK
          } else if (receiverTypeTypeArgument.typeVariable()) {
            final List<? extends Type<X>> bounds = receiverTypeTypeArgument.upperBounds();
            switch (bounds.size()) {
            case 0:
              // OK
              break;
            case 1:
              if (!bounds.get(0).top()) {
                return false;
              }
              break;
            default:
              return false;
            }
          } else {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    @Override
    protected final <X, Y> boolean parameterizedTypeIsAssignableFromParameterizedType(final Type<X> receiverParameterizedType,
                                                                                      final Type<Y> payloadParameterizedType) {
      if (Type.equals(receiverParameterizedType.type(), payloadParameterizedType.type())) {
        final List<? extends Type<X>> receiverTypeTypeArguments = receiverParameterizedType.typeArguments();
        final List<? extends Type<Y>> payloadTypeTypeArguments = payloadParameterizedType.typeArguments();
        if (receiverTypeTypeArguments.size() == payloadTypeTypeArguments.size()) {
          for (int i = 0; i < receiverTypeTypeArguments.size(); i++) {
            if (!CdiTypeArgumentSemantics.INSTANCE.assignable(receiverTypeTypeArguments.get(i), payloadTypeTypeArguments.get(i))) {
              return false;
            }
          }
          return true;
        }
      }
      return false;
    }


    /*
     * Inner and nested classes.
     */


    @Experimental
    private static class CdiTypeArgumentSemantics extends VariantSemantics {

      private static final CdiTypeArgumentSemantics INSTANCE = new CdiTypeArgumentSemantics();

      private CdiTypeArgumentSemantics() {
        super();
      }

      @Override
      protected final <X, Y> boolean classIsAssignableFromTypeVariable(final Type<X> receiverClass,
                                                                       final Type<Y> payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverClass, payloadTypeVariable);
      }

      @Override
      protected final <X, Y> boolean parameterizedTypeIsAssignableFromTypeVariable(final Type<X> receiverParameterizedType,
                                                                                   final Type<Y> payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverParameterizedType, payloadTypeVariable);
      }

      @Override
      protected final <X, Y> boolean genericArrayTypeIsAssignableFromTypeVariable(final Type<X> receiverGenericArrayType,
                                                                                  final Type<Y> payloadTypeVariable) {
        return this.actualTypeIsAssignableFromTypeVariable(receiverGenericArrayType, payloadTypeVariable);
      }

      private final <X, Y> boolean actualTypeIsAssignableFromTypeVariable(final Type<X> receiverActualType,
                                                                          final Type<Y> payloadTypeVariable) {
        // Section 5.2.4 of the CDI specification is mealy-mouthed but
        // interpretable:
        //
        // "…the required type parameter [receiverActualType] is an
        // actual type [never defined, but implied by other sentences
        // scattered throughout the specification to be either a
        // class, parameterized type or generic array type], the bean
        // type parameter is a type variable [payloadTypeVariable] and
        // the actual type [receiverActualType] is assignable to the
        // upper bound[s], if any [there will always be one], of the
        // type variable [payloadTypeVariable], or…"
        //
        // So perhaps somewhat counterintuitively check all the bounds
        // of the *payload* and see if the *receiver* is assignable to
        // all of them.
        for (final Type<Y> bound : this.condense(payloadTypeVariable.upperBounds())) {
          // Note that this is somewhat backwards to what you may expect.
          if (!CovariantSemantics.INSTANCE.assignable(bound, receiverActualType)) {
            return false;
          }
        }
        return true;
      }

      @Override
      protected final <X, Y> boolean typeVariableIsAssignableFromTypeVariable(final Type<X> receiverTypeVariable,
                                                                              final Type<Y> payloadTypeVariable) {
        // Section 5.2.4 again:
        //
        // "…the required type parameter and the bean type parameter are
        // both type variables and the upper bound of the required [receiver]
        // type parameter is assignable to the upper bound, if any, of
        // the bean [payload] type parameter."
        //
        // The first of many problems with this:
        //
        // This is of course ambiguous.  See
        // https://issues.redhat.com/browse/CDI-440 which was
        // unceremoniously closed for no good reason.
        //
        // "For example, the bullet
        //
        // * the required type parameter and the bean type parameter
        //   are both type variables and the upper bound of the
        //   required type parameter is assignable to the upper bound,
        //   if any, of the bean type parameter.
        //
        // should have meaning similar to:
        //
        // for each upper bound T of the bean type parameter, there is
        // an (at least one) upper bound of the required type which is
        // assignable to T."
        //
        // This is not normal Java/covariant semantics.  In Java, if
        // you have two type variables, A extends Object and B extends
        // Object, you cannot assign B to A or vice versa.  Here, the
        // CDI specification is essentially performing a kind of type
        // erasure in this one case.
        //
        // In CDI, when a type variable is a type argument (by
        // definition of a parameterized type), the platform basically
        // erases it.  In CDI you cannot have a type-variable-typed
        // injection point, because of course you can't predict what
        // "value" that injection point's type will have at
        // application analysis time.  So it stands to reason that if
        // there is a type variable "anywhere in" the injection point
        // (as a type argument, say) it will effectively be erased to
        // its bounds.
        return this.match(payloadTypeVariable.upperBounds(), receiverTypeVariable.upperBounds());
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromClass(final Type<X> receiverWildcardType,
                                                                       final boolean lowerBounded,
                                                                       final Type<Y> payloadClass) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadClass);
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromParameterizedType(final Type<X> receiverWildcardType,
                                                                                   final boolean lowerBounded,
                                                                                   final Type<Y> payloadParameterizedType) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadParameterizedType);
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromGenericArrayType(final Type<X> receiverWildcardType,
                                                                                  final boolean lowerBounded,
                                                                                  final Type<Y> payloadGenericArrayType) {
        return this.wildcardTypeIsAssignableFromActualType(receiverWildcardType, lowerBounded, payloadGenericArrayType);
      }

      private final <X, Y> boolean wildcardTypeIsAssignableFromActualType(final Type<X> receiverWildcardType,
                                                                          final boolean lowerBounded,
                                                                          final Type<Y> payloadActualType) {
        return
          (!lowerBounded || this.match(payloadActualType, receiverWildcardType.lowerBounds())) &&
          this.match(receiverWildcardType.upperBounds(), payloadActualType);
      }

      @Override
      protected final <X, Y> boolean wildcardTypeIsAssignableFromTypeVariable(final Type<X> receiverWildcardType,
                                                                              final boolean lowerBounded,
                                                                              final Type<Y> payloadTypeVariable) {
        final List<? extends Type<Y>> condensedPayloadTypeVariableBounds = this.condense(payloadTypeVariable.upperBounds());
        if (!lowerBounded || this.match(condensedPayloadTypeVariableBounds, false, receiverWildcardType.lowerBounds(), true)) {
          final List<? extends Type<X>> condensedReceiverWildcardUpperBounds = this.condense(receiverWildcardType.upperBounds());
          return
            this.match(condensedReceiverWildcardUpperBounds, false, condensedPayloadTypeVariableBounds, false) ||
            this.match(condensedPayloadTypeVariableBounds, false, condensedReceiverWildcardUpperBounds, false);
        }
        return false;
      }

      private final <Y> boolean match(final Type<?> bound0, final List<? extends Type<Y>> bounds1) {
        return this.match(bound0, bounds1, true);
      }

      private final <Y> boolean match(final Type<?> bound0, List<? extends Type<Y>> bounds1, final boolean condense1) {
        if (!bounds1.isEmpty()) {
          if (condense1) {
            bounds1 = this.condense(bounds1);
          }
          boolean match = false;
          for (final Type<?> bound1 : bounds1) {
            if (CovariantSemantics.INSTANCE.assignable(bound0, bound1)) {
              match = true;
              break;
            }
          }
          if (!match) {
            return false;
          }
        }
        return true;
      }

      private final <X> boolean match(final List<? extends Type<X>> bounds0, final Type<?> bound1) {
        return this.match(bounds0, true, bound1);
      }

      private final <X> boolean match(List<? extends Type<X>> bounds0, final boolean condense0, final Type<?> bound1) {
        if (!bounds0.isEmpty()) {
          if (condense0) {
            bounds0 = this.condense(bounds0);
          }
          boolean match = false;
          for (final Type<X> bound0 : bounds0) {
            if (CovariantSemantics.INSTANCE.assignable(bound0, bound1)) {
              match = true;
              break;
            }
          }
          if (!match) {
            return false;
          }
        }
        return true;
      }

      private final <X, Y> boolean match(final List<? extends Type<X>> bounds0, final List<? extends Type<Y>> bounds1) {
        return this.match(bounds0, true, bounds1, true);
      }

      private final <X, Y> boolean match(List<? extends Type<X>> bounds0,
                                         final boolean condense0,
                                         List<? extends Type<Y>> bounds1,
                                         final boolean condense1) {
        if (!bounds0.isEmpty() && !bounds1.isEmpty()) {
          for (final Type<X> bound0 : condense0 ? this.condense(bounds0) : bounds0) {
            boolean match = false;
            for (final Type<Y> bound1 : condense1 ? this.condense(bounds1) : bounds1) {
              if (CovariantSemantics.INSTANCE.assignable(bound0, bound1)) {
                match = true;
                break;
              }
            }
            if (!match) {
              return false;
            }
          }
        }
        return true;
      }

    }
  }
}

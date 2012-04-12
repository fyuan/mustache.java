package com.github.mustachejava.reflect;

import com.github.mustachejava.MustacheException;
import com.github.mustachejava.util.GuardException;
import com.github.mustachejava.util.Wrapper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Used for evaluating values at a callsite
 */
public class ReflectionWrapper extends GuardedWrapper {
  // Context
  protected int scopeIndex;
  protected Wrapper[] wrappers;
  protected boolean[] mapGuard;

  // Dispatch
  protected final Method method;
  protected final Field field;
  protected final Object[] arguments;

  public ReflectionWrapper(String name, int scopeIndex, Wrapper[] wrappers, Class[] guard, boolean[] mapGuard,
                           AccessibleObject method, Object[] arguments) {
    super(name, scopeIndex, guard, mapGuard);
    this.wrappers = wrappers;
    if (method instanceof Field) {
      this.method = null;
      this.field = (Field) method;
    } else {
      this.method = (Method) method;
      this.field = null;
    }
    this.arguments = arguments;
  }

  @Override
  public Object call(Object[] scopes) throws GuardException {
    try {
      guardCall(scopes);
      Object scope = unwrap(scopes);
      if (scope == null) return null;
      if (method == null) {
        return field.get(scope);
      } else {
        return method.invoke(scope, arguments);
      }
    } catch (InvocationTargetException e) {
      throw new MustacheException("Failed to execute method: " + method, e.getTargetException());
    } catch (IllegalAccessException e) {
      throw new MustacheException("Failed to execute method: " + method, e);
    }
  }

  protected Object unwrap(Object[] scopes) throws GuardException {
    Object scope = scopes[scopeIndex];
    // The value may be buried by . notation
    if (wrappers != null) {
      for (Wrapper wrapper : wrappers) {
        scope = wrapper.call(new Object[]{scope});
      }
    }
    return scope;
  }

  public Method getMethod() {
    return method;
  }

  public Field getField() {
    return field;
  }

  public Object[] getArguments() {
    return arguments;
  }
}

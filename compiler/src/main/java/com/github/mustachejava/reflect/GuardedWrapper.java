package com.github.mustachejava.reflect;

import java.util.Map;

import com.github.mustachejava.util.GuardException;
import com.github.mustachejava.util.Wrapper;

/**
 * Wrapper that guards.
 */
public class GuardedWrapper implements Wrapper {
  protected final String name;
  protected final int scopeIndex;
  protected final Class[] guard;
  protected final boolean[] mapGuard;

  public GuardedWrapper(String name, int scopeIndex, Class[] guard, boolean[] mapGuard) {
    this.name = name;
    this.scopeIndex = scopeIndex;
    this.guard = guard;
    this.mapGuard = mapGuard;
  }

  @Override
  public Object call(Object[] scopes) throws GuardException {
    guardCall(scopes);
    return null;
  }

  protected void guardCall(Object[] scopes) throws GuardException {
    int length = scopes.length;
    if (guard.length != length) {
      throw new GuardException();
    }
    for (int j = 0; j < length; j++) {
      Class guardClass = guard[j];
      if (guardClass != null && !guardClass.isInstance(scopes[j])) {
        throw new GuardException();
      }
      if (scopes[j] instanceof Map || (guardClass != null && guardClass.isAssignableFrom(Map.class))) {
        Map map = (Map) scopes[j];
        if (mapGuard[j]) {
          if (!map.containsKey(name)) {
            throw new GuardException();
          }
        } else {
          if (map.containsKey(name)) {
            throw new GuardException();
          }
        }
      }
    }
  }
}

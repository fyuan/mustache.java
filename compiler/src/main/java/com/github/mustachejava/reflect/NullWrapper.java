package com.github.mustachejava.reflect;

public class NullWrapper extends GuardedWrapper {
  public NullWrapper(String name, Object[] scopes) {
    super(name, 0, null, new Class[scopes.length], new boolean[scopes.length]);
    int length = scopes.length;
    for (int i = 0; i < length; i++) {
      Object scope = scopes[i];
      if (scope != null) {
        guard[i] = scope.getClass();
      }
    }
  }
}

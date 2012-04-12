package com.github.mustachejava.reflect;

import java.io.Writer;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.mustachejava.Iteration;
import com.github.mustachejava.ObjectHandler;
import com.github.mustachejava.util.GuardException;
import com.github.mustachejava.util.Wrapper;

/**
 * Lookup objects using reflection and execute them the same way.
 * <p/>
 * User: sam
 * Date: 7/24/11
 * Time: 3:02 PM
 */
public class ReflectionObjectHandler implements ObjectHandler {

  protected static final Method MAP_METHOD;

  static {
    try {
      MAP_METHOD = Map.class.getMethod("get", Object.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Wrapper find(String name, Object[] scopes) {
    Wrapper wrapper = null;
    int length = scopes.length;
    Class[] guard = createGuard(scopes);
    boolean[] mapGuard = new boolean[length];
    NEXT:
    for (int i = length - 1; i >= 0; i--) {
      Object scope = scopes[i];
      if (scope == null) continue;
      List<Wrapper> wrappers = null;
      int dotIndex;
      String subname = name;
      while ((dotIndex = subname.indexOf('.')) != -1) {
        String lookup = subname.substring(0, dotIndex);
        subname = subname.substring(dotIndex + 1);
        wrapper = findWrapper(0, null, new Class[]{scope.getClass()}, mapGuard, scope, lookup);
        if (wrapper != null) {
          if (wrappers == null) wrappers = new ArrayList<Wrapper>();
          wrappers.add(wrapper);
          try {
            scope = wrapper.call(new Object[]{scope});
          } catch (GuardException e) {
            throw new AssertionError(e);
          }
        } else {
          continue NEXT;
        }
        if (scope == null) return null;
      }
      Wrapper[] foundWrappers = wrappers == null ? null : wrappers.toArray(
              new Wrapper[wrappers.size()]);
      Wrapper foundWrapper = findWrapper(i, foundWrappers, guard, mapGuard, scope, subname);
      if (foundWrapper != null) {
        wrapper = foundWrapper;
        break;
      }
    }
    return wrapper;
  }

  @Override
  public Object coerce(Object object) {
    return object;
  }

  protected Class[] createGuard(Object[] scopes) {
    int length = scopes.length;
    Class[] guard = new Class[length];
    for (int i = 0; i < length; i++) {
      Object scope = scopes[i];
      if (scope != null) {
        guard[i] = scope.getClass();
      }
    }
    return guard;
  }

  protected Wrapper findWrapper(int scopeIndex, Wrapper[] wrappers, Class[] guard, boolean[] mapGuard, Object scope, String name) {
    if (scope == null) return null;
    if (scope instanceof Map) {
      Map map = (Map) scope;
      if (map.get(name) == null) {
        return null;
      } else {
        mapGuard[scopeIndex] = true;
        return new ReflectionWrapper(name, scopeIndex, wrappers, guard, mapGuard, MAP_METHOD, new Object[]{name});
      }
    }
    Class aClass = scope.getClass();
    // Don't overload methods in your contexts
    Wrapper member = null;
    try {
      member = getField(name, aClass, scopeIndex, guard, mapGuard, wrappers);
    } catch (NoSuchFieldException e) {
      // Not set
    }
    if (member == null) {
      try {
        member = getMethod(name, aClass, scopeIndex, guard, mapGuard, wrappers);
      } catch (NoSuchMethodException e) {
        try {
          member = getMethod(name, aClass, scopeIndex, guard, mapGuard, wrappers, List.class);
        } catch (NoSuchMethodException e1) {
          String propertyname = name.substring(0, 1).toUpperCase() +
                  (name.length() > 1 ? name.substring(1) : "");
          try {
            member = getMethod("get" + propertyname, aClass, scopeIndex, guard, mapGuard, wrappers);
          } catch (NoSuchMethodException e2) {
            try {
              member = getMethod("is" + propertyname, aClass, scopeIndex, guard, mapGuard, wrappers);
            } catch (NoSuchMethodException e3) {
              // Nothing to be done
            }
          }
        }
      }
    }
    return member;
  }

  protected Wrapper getMethod(String name, Class aClass, int scopeIndex, Class[] guard, boolean[] mapGuard, Wrapper[] wrappers, Class... params) throws NoSuchMethodException {
    Method member;
    try {
      member = aClass.getDeclaredMethod(name, params);
    } catch (NoSuchMethodException nsme) {
      Class superclass = aClass.getSuperclass();
      if (superclass != null && superclass != Object.class) {
        return getMethod(name, superclass, scopeIndex, guard, mapGuard, wrappers, params);
      }
      throw nsme;
    }
    checkMethod(member);
    member.setAccessible(true);
    return createWrapper(name, scopeIndex, wrappers, guard, mapGuard, member, null);
  }

  protected void checkMethod(Method member) throws NoSuchMethodException {
    if ((member.getModifiers() & Modifier.PRIVATE) == Modifier.PRIVATE) {
      throw new NoSuchMethodException("Only public, protected and package members allowed");
    }
  }

  protected Wrapper getField(String name, Class aClass, int scopeIndex, Class[] guard, boolean[] mapGuard, Wrapper[] wrappers) throws NoSuchFieldException {
    Field member;
    try {
      member = aClass.getDeclaredField(name);
    } catch (NoSuchFieldException nsfe) {
      Class superclass = aClass.getSuperclass();
      if (superclass != null && superclass != Object.class) {
        return getField(name, superclass, scopeIndex, guard, mapGuard, wrappers);
      }
      throw nsfe;
    }
    checkField(member);
    member.setAccessible(true);
    return createWrapper(name, scopeIndex, wrappers, guard, mapGuard, member, null);
  }

  protected void checkField(Field member) throws NoSuchFieldException {
    if ((member.getModifiers() & Modifier.PRIVATE) == Modifier.PRIVATE) {
      throw new NoSuchFieldException("Only public, protected and package members allowed");
    }
  }

  protected Wrapper createWrapper(String name, int scopeIndex, Wrapper[] wrappers, Class[] guard, boolean[] mapGuard, AccessibleObject member, Object[] arguments) {
    return new ReflectionWrapper(name, scopeIndex, wrappers, guard, mapGuard, member, arguments);
  }

  @Override
  public Writer falsey(Iteration iteration, Writer writer, Object object, Object[] scopes) {
    if (object != null) {
      if (object instanceof Boolean) {
        if ((Boolean) object) {
          return writer;
        }
      } else if (object instanceof String) {
        if (!object.toString().equals("")) {
          return writer;
        }
      } else if (object instanceof List) {
        List list = (List) object;
        int length = list.size();
        if (length > 0) return writer;
      } else if (object instanceof Iterable) {
        Iterable iterable = (Iterable) object;
        if (iterable.iterator().hasNext()) return writer;
      } else if (object instanceof Iterator) {
        Iterator iterator = (Iterator) object;
        if (iterator.hasNext()) return writer;
      } else if (object instanceof Object[]) {
        Object[] array = (Object[]) object;
        int length = array.length;
        if (length > 0) return writer;
      } else {
        // All other objects are truthy
        return writer;
      }
    }
    return iteration.next(writer, object, scopes);
  }

  public Writer iterate(Iteration iteration, Writer writer, Object object, Object[] scopes) {
    if (object == null) return writer;
    if (object instanceof Boolean) {
      if (!(Boolean) object) {
        return writer;
      }
    }
    if (object instanceof String) {
      if (object.toString().equals("")) {
        return writer;
      }
    }
    if (object instanceof Iterable) {
      for (Object next : ((Iterable) object)) {
        writer = iteration.next(writer, coerce(next), scopes);
      }
    } else if (object instanceof Iterator) {
      Iterator iterator = (Iterator) object;
      while (iterator.hasNext()) {
        writer = iteration.next(writer, coerce(iterator.next()), scopes);
      }
    } else if (object instanceof Object[]) {
      Object[] array = (Object[]) object;
      for (Object o : array) {
        writer = iteration.next(writer, coerce(o), scopes);
      }
    } else {
      writer = iteration.next(writer, object, scopes);
    }
    return writer;
  }

}

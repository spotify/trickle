/*
 * Copyright (c) 2013 Spotify AB
 */

package com.spotify.trickle.transform;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.trickle.Trickle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.util.concurrent.Futures.*;

class NoChecksMethodTransformer<T> implements Transformer<T> {
  final ImmutableList<Trickle.Dep<?>> inputs;
  final Object obj;

  final Method method;

  NoChecksMethodTransformer(
      final List<Trickle.Dep<?>> inputs,
      final Object obj) {
    this.inputs = ImmutableList.copyOf(inputs);
    this.obj = obj;

    // validate number of methods
    final Class<?> aClass = obj.getClass();
    final Method[] methods = aClass.getDeclaredMethods();
    checkArgument(methods.length == 1,
        "Must contain exactly one method");

    method = methods[0];

    // validate parameters count
    final Class<?>[] parameterTypes = method.getParameterTypes();
    checkArgument(parameterTypes.length == inputs.size(),
        "Must have same number of arguments as inputs, expected %s, found %s",
        inputs.size(), parameterTypes.length);

    method.setAccessible(true);
  }

  @Override
  public ListenableFuture<T> createTransform(
      final ImmutableList<Object> values,
      final ListenableFuture<?> doneSignal) {

    final Class<?> returnType = method.getReturnType();
    if (ListenableFuture.class.isAssignableFrom(returnType)) {
      return transform(doneSignal, new AsyncFunction<Object, T>() {
        @Override
        public ListenableFuture<T> apply(Object _) throws Exception {
          return (ListenableFuture<T>) invokeObject(values);
        }
      });
    } else {
      return transform(doneSignal, new AsyncFunction<Object, T>() {
        @Override
        public ListenableFuture<T> apply(Object _) throws Exception {
          return immediateFuture((T) invokeObject(values));
        }
      });
    }
  }

  private Object invokeObject(final ImmutableList<Object> values)
      throws IllegalAccessException, InvocationTargetException {

    final Object[] args = new Object[values.size()];
    for (int i = 0; i < values.size(); i++) {
      final Trickle.Dep<?> dep = inputs.get(i);
      final Object valueObject = values.get(i);

      Object arg;
      if (valueObject instanceof ListenableFuture &&
                !(dep instanceof Trickle.BindingDep)) {
        arg = getUnchecked((ListenableFuture) valueObject);
      } else {
        arg = valueObject;
      }

      args[i] = arg;
    }

    return method.invoke(obj, args);
  }
}

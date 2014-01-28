/*
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.trickle;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;

import static com.google.common.util.concurrent.Futures.immediateFuture;

/**
 * Contains utility methods for dealing with fallbacks.
 */
public final class Fallbacks {
  private Fallbacks() {
    // prevent instantiation
  }

  public static <T> AsyncFunction<Throwable, T> always(@Nullable final T value) {
    return new AsyncFunction<Throwable, T>() {
      @Nullable
      @Override
      public ListenableFuture<T> apply(@Nullable Throwable input) {
        return immediateFuture(value);
      }
    };
  }
}

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

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Code that has a inputs of type A, B and C, and returns a value of type R. Side-effects such as
 * writing to a database are permitted, but authors are encouraged to keep implementations free
 * of side-effects if at all possible.
 */
public interface Func3<A, B, C, R> extends Func<R> {
  ListenableFuture<R> run(A arg1, B arg2, C arg3);
}

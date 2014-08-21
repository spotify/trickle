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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO: document!
 */
public class NoopExceptionWrapper implements ExceptionWrapper {

  @Override
  public Throwable wrapException(Throwable t,
                                 TraverseState.FutureCallInformation currentCall,
                                 TraverseState traverseState) {
    checkNotNull(t);
    checkNotNull(currentCall);
    checkNotNull(traverseState);

    return t;
  }
}

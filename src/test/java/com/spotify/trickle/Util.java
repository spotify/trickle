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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class Util {

  /**
   * Returns a matcher that verifies that a throwable has the expected Throwable as a parent
   * somewhere in the hierarchy.
   *
   * @param expected the expected ancestor
   * @return true or false
   */
  static Matcher<Throwable> hasAncestor(final Throwable expected) {
    return new TypeSafeMatcher<Throwable>() {
      @Override
      protected boolean matchesSafely(Throwable item) {
        for (Throwable cause = item ; cause != null ; cause = cause.getCause()) {
          if (cause.equals(expected)) {
            return true;
          }
        }

        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("with parent cause " + expected);
      }
    };
  }
}

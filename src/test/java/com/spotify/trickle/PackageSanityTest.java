package com.spotify.trickle;

import com.google.common.testing.AbstractPackageSanityTests;
import org.junit.Before;

public class PackageSanityTest extends AbstractPackageSanityTests {

  @Before
  @Override
  public void setUp() throws Exception {
    setDefault(Name.class, Name.named("hi", Object.class));
    super.setUp();
  }
}

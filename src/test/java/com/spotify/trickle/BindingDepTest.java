package com.spotify.trickle;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class BindingDepTest {
  @Test
  public void shouldGenerateEqualNodeInfoForSameName() throws Exception {
    // this is important for understanding how nodes relate to each other when inspecting a call
    // graph - if the same input name is used in more than one place, different BindingDep instances
    // will be created, but they should be considered equal since they refer to the same input.
    Name<String> name2 = Name.named("ho");

    BindingDep<String> dep1 = new BindingDep<String>(Name.<String>named("hi"));
    BindingDep<String> dep2 = new BindingDep<String>(Name.<String>named("hi"));

    new EqualsTester()
        .addEqualityGroup(dep1.getNodeInfo(), dep2.getNodeInfo())
        .addEqualityGroup(new BindingDep<String>(name2).getNodeInfo())
        .testEquals();
  }
}

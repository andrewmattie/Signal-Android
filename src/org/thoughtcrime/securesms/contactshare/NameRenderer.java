package org.thoughtcrime.securesms.contactshare;

import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.contactshare.model.Name;

public class NameRenderer {

  public static String getDisplayString(@NonNull Name name) {
    // TODO: Do this correctly
    return name.getGivenName() + " " + name.getFamilyName();
  }
}

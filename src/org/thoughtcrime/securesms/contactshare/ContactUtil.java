package org.thoughtcrime.securesms.contactshare;

import android.net.Uri;
import android.support.annotation.NonNull;

public final class ContactUtil {


  public static long getContactIdFromUri(@NonNull Uri uri) {
    try {
      return Long.parseLong(uri.getLastPathSegment());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}

package org.thoughtcrime.securesms.contactshare.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.thoughtcrime.securesms.attachments.Attachment;
import org.thoughtcrime.securesms.mms.PartAuthority;

import java.io.IOException;
import java.io.InputStream;

/**
 * A model that can retrieve a fully-filled {@link Contact} model asynchronously.
 */
public class AsyncContact {

  private static final String TAG = AsyncContact.class.getSimpleName();

  private final Context    context;
  private final Attachment avatar;
  private final Attachment contact;

  public AsyncContact(@NonNull  Context    context,
                      @NonNull  Attachment contact,
                      @Nullable Attachment avatar)
  {
    this.context  = context.getApplicationContext();
    this.avatar   = avatar;
    this.contact  = contact;
  }

  @WorkerThread
  public @Nullable Contact getContact() {
    if (contact.getDataUri() == null) {
      Log.w(TAG, "Provided contact attachment has no URI");
      return null;
    }


    try (InputStream stream = PartAuthority.getAttachmentStream(context, contact.getDataUri())) {
      return null;
    } catch (IOException e) {
      Log.e(TAG, "Error while reading contact attachment file", e);
      return null;
    }
  }
}

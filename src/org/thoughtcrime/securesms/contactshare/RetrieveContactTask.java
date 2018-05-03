package org.thoughtcrime.securesms.contactshare;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.ContactRetriever;
import org.thoughtcrime.securesms.database.model.MessageRecord;

public class RetrieveContactTask extends AsyncTask<Void, Void, Contact> {

  private final ContactRetriever         retriever;
  private final MessageRecord            forMessage;
  private final ContactRetrievedListener listener;

  public RetrieveContactTask(@NonNull ContactRetriever         retriever,
                      @NonNull MessageRecord            forMessage,
                      @NonNull ContactRetrievedListener listener)
  {
    this.retriever     = retriever;
    this.forMessage    = forMessage;
    this.listener      = listener;
  }

  @Override
  protected Contact doInBackground(Void... voids) {
    return retriever.getContact();
  }

  @Override
  protected void onPostExecute(Contact contact) {
    listener.onContactRetrieved(contact, forMessage);
  }

  public interface ContactRetrievedListener {
    void onContactRetrieved(@Nullable Contact contact, @NonNull MessageRecord forMessage);
  }
}



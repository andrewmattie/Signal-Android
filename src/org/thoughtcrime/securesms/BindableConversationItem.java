package org.thoughtcrime.securesms;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.crypto.MasterSecret;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.database.model.MmsMessageRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.recipients.Recipient;

import java.util.Locale;
import java.util.Set;

public interface BindableConversationItem extends Unbindable {
  void bind(@NonNull MessageRecord      messageRecord,
            @NonNull GlideRequests      glideRequests,
            @NonNull Locale             locale,
            @NonNull Set<MessageRecord> batchSelected,
            @NonNull Recipient          recipients,
                     boolean            pulseHighlight);

  MessageRecord getMessageRecord();

  void setEventListener(@Nullable EventListener listener);

  interface EventListener {
    void onQuoteClicked(MmsMessageRecord messageRecord);
    void onSharedContactDetailsClicked(@NonNull Contact sharedContact, @NonNull View avatarTransitionView);
    void onAddToContactsClicked(@NonNull Contact sharedContact);
  }
}

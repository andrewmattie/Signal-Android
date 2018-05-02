package org.thoughtcrime.securesms.contactshare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.components.AvatarImageView;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.recipients.Recipient;

import java.util.ArrayList;
import java.util.List;

public class ContactShareEditAdapter extends RecyclerView.Adapter<ContactShareEditAdapter.ContactEditViewHolder> {

  private final GlideRequests glideRequests;
  private final List<Contact> contacts;

  ContactShareEditAdapter(@NonNull GlideRequests glideRequests) {
    this.glideRequests = glideRequests;
    this.contacts      = new ArrayList<>();
  }

  @Override
  public ContactEditViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ContactEditViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_editable_contact, parent, false));
  }

  @Override
  public void onBindViewHolder(ContactEditViewHolder holder, int position) {
    holder.bind(contacts.get(position), glideRequests);
  }

  @Override
  public int getItemCount() {
    return contacts.size();
  }

  void setContacts(@Nullable List<Contact> contacts) {
    this.contacts.clear();

    if (contacts != null) {
      this.contacts.addAll(contacts);
    }

    notifyDataSetChanged();
  }

  static class ContactEditViewHolder extends RecyclerView.ViewHolder {

    private final AvatarImageView     avatar;
    private final TextView            name;
    private final ContactFieldAdapter fieldAdapter;

    ContactEditViewHolder(View itemView) {
      super(itemView);
      avatar       = itemView.findViewById(R.id.editable_contact_avatar);
      name         = itemView.findViewById(R.id.editable_contact_name);
      fieldAdapter = new ContactFieldAdapter();

      RecyclerView fields = itemView.findViewById(R.id.editable_contact_fields);
      fields.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
      fields.getLayoutManager().setAutoMeasureEnabled(true);
      fields.setAdapter(fieldAdapter);
    }

    void bind(@NonNull Contact contact, @NonNull GlideRequests glideRequests) {
      Context   context   = itemView.getContext();
//      Recipient recipient = Recipient.from(context, contact.getAvatar(), true);
      if (contact.getAvatar() != null) {
        // TODO: Set image
//        avatar.setAvatar(glideRequests, recipient, false);
      }
      name.setText(NameRenderer.getDisplayString(contact.getName()));
      fieldAdapter.setFields(context, contact.getPhoneNumbers(), contact.getEmails(), contact.getPostalAddresses());
    }
  }
}

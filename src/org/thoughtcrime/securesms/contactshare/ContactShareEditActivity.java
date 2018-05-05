package org.thoughtcrime.securesms.contactshare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.providers.PersistentBlobProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

// TODO: Extend password activity thingy?
public class ContactShareEditActivity extends AppCompatActivity {

  public  static final String KEY_CONTACTS  = "contacts";
  private static final String KEY_CONTACT_IDS = "ids";

  private ContactShareEditViewModel viewModel;
  private ContactShareEditAdapter   contactAdapter;
  private boolean                   photosUsed;

  public static Intent getIntent(@NonNull Context context, @NonNull List<Long> contactIds) {
    ArrayList<String> serializedIds = new ArrayList<>(Stream.of(contactIds).map(String::valueOf).toList());

    Intent intent = new Intent(context, ContactShareEditActivity.class);
    intent.putStringArrayListExtra(KEY_CONTACT_IDS, serializedIds);
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contact_share_edit);

    if (getIntent() == null) {
      throw new IllegalStateException("You must supply extras to this activity. Please use the #getIntent() method.");
    }

    List<String> serializedIds = getIntent().getStringArrayListExtra(KEY_CONTACT_IDS);
    if (serializedIds == null) {
      throw new IllegalStateException("You must supply contact ID's to this activity. Please use the #getIntent() method.");
    }

    List<Long> contactIds = Stream.of(serializedIds).map(Long::parseLong).toList();

    View sendButton = findViewById(R.id.contact_share_edit_send);
    sendButton.setOnClickListener(v -> onSendClicked(viewModel.getTrimmedContacts()));

    RecyclerView contactList = findViewById(R.id.contact_share_edit_list);
    contactList.setLayoutManager(new LinearLayoutManager(this));
    contactList.getLayoutManager().setAutoMeasureEnabled(true);

    contactAdapter = new ContactShareEditAdapter(GlideApp.with(this));
    contactList.setAdapter(contactAdapter);

    // TODO: Unify executors in some class
    ContactRepository contactRepository = new ContactRepository(this,
                                                                Executors.newSingleThreadExecutor(),
                                                                DatabaseFactory.getContactsDatabase(this));

    viewModel = ViewModelProviders.of(this, new ContactShareEditViewModel.Factory(contactIds, contactRepository)).get(ContactShareEditViewModel.class);
    viewModel.getContacts().observe(this, contactAdapter::setContacts);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (!photosUsed && viewModel.getContacts().getValue() != null) {
      deleteContactPhotos(viewModel.getContacts().getValue());
    }
  }

  private void onSendClicked(List<Contact> contacts) {
    photosUsed = true;

    Intent intent = new Intent();

    ArrayList<Contact> contactArrayList = new ArrayList<>(contacts.size());
    contactArrayList.addAll(contacts);
    intent.putExtra(KEY_CONTACTS, contactArrayList);

    setResult(Activity.RESULT_OK, intent);

    finish();
  }

  @SuppressLint("StaticFieldLeak")
  private void deleteContactPhotos(List<Contact> contacts) {
    final Context context = this;

    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... voids) {
        for (Contact contact : contacts) {
          if (contact.getAvatar() != null && contact.getAvatar().getImage().getDataUri() != null) {
            PersistentBlobProvider.getInstance(context)
                                  .delete(context, contact.getAvatar().getImage().getDataUri());
          }
        }
        return null;
      }
    }.execute();
  }
}

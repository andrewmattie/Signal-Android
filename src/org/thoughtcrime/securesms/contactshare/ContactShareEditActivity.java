package org.thoughtcrime.securesms.contactshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.database.Address;

import java.util.ArrayList;
import java.util.List;

// TODO: Extend password activity thingy?
public class ContactShareEditActivity extends AppCompatActivity
                                      implements ContactShareEditFragment.EventListener {

  public  static final String KEY_CONTACTS  = "contacts";
  private static final String KEY_ADDRESSES = "addresses";

  public static Intent getIntent(@NonNull Context context, @NonNull ArrayList<Address> addresses) {
    Intent intent = new Intent(context, ContactShareEditActivity.class);
    intent.putParcelableArrayListExtra(KEY_ADDRESSES, addresses);
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contact_share_select);

    if (savedInstanceState == null) {
      if (getIntent() == null) {
        throw new IllegalStateException("You must supply arguments to this fragment. Please use the #newInstance() method.");
      }

      ArrayList<Address> addresses = getIntent().getParcelableArrayListExtra(KEY_ADDRESSES);
      if (addresses == null) {
        throw new IllegalStateException("You must supply addresses to this fragment. Please use the #newInstance() method.");
      }

      ContactShareEditFragment fragment = ContactShareEditFragment.newInstance(addresses);
      getSupportFragmentManager().beginTransaction()
                                 .add(R.id.fragment_container, fragment)
                                 .commit();
    }
  }

  @Override
  public void onSendClicked(List<Contact> contacts) {
    Intent intent = new Intent();

    ArrayList<Contact> contactArrayList = new ArrayList<>(contacts.size());
    contactArrayList.addAll(contacts);
    intent.putExtra(KEY_CONTACTS, contactArrayList);

    setResult(Activity.RESULT_OK, intent);

    finish();
  }
}

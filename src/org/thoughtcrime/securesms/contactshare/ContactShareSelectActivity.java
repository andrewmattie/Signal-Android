package org.thoughtcrime.securesms.contactshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.thoughtcrime.securesms.ContactSelectionActivity;
import org.thoughtcrime.securesms.ContactSelectionListFragment;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.database.Address;

import java.util.ArrayList;

/**
 * Handles the selection of contacts to be shared.
 */
public class ContactShareSelectActivity extends ContactSelectionActivity {

  private static final int REQUEST_CODE = 24601;

  @Override
  protected void onCreate(Bundle icicle, boolean ready) {
    getIntent().putExtra(ContactSelectionListFragment.MULTI_SELECT, false);
    super.onCreate(icicle, ready);

    contactsFragment.setOnContactSelectedListener(new ContactSelectionListFragment.OnContactSelectedListener() {
      @Override
      public void onContactSelected(String number) {
        ArrayList<Address> addresses = new ArrayList<>();
        addresses.add(Address.fromExternal(ContactShareSelectActivity.this, number));

        Intent intent = ContactShareEditActivity.getIntent(ContactShareSelectActivity.this, addresses);
        startActivityForResult(intent, REQUEST_CODE);
      }

      @Override
      public void onContactDeselected(String number) { }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    setResult(resultCode, data);
    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
      finish();
    }
  }
}

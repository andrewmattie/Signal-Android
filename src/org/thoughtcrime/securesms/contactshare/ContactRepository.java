package org.thoughtcrime.securesms.contactshare;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.thoughtcrime.securesms.contacts.ContactsDatabase;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Email;
import org.thoughtcrime.securesms.contactshare.model.Name;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.contactshare.model.PostalAddress;
import org.thoughtcrime.securesms.database.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

class ContactRepository {

  private static final String TAG = ContactRepository.class.getSimpleName();

  private final Context          context;
  private final Executor         executor;
  private final ContactsDatabase contactsDatabase;

  ContactRepository(@NonNull Context          context,
                    @NonNull Executor         executor,
                    @NonNull ContactsDatabase contactsDatabase)
  {
    this.context          = context.getApplicationContext();
    this.executor         = executor;
    this.contactsDatabase = contactsDatabase;
  }

  // TODO: Move from addresses to contactId as identifier?
  void getContacts(@NonNull List<Address> addresses, @NonNull Callback callback) {
    executor.execute(() -> {
      List<Contact> contacts = new ArrayList<>(addresses.size());
      for (Address address : addresses) {
        Contact contact = getContact(address);
        if (contact != null) {
          contacts.add(contact);
        }
      }
      callback.onComplete(contacts);
    });
  }

  @WorkerThread
  private @Nullable Contact getContact(@NonNull Address address) {
    long contactId = contactsDatabase.getContactIdFromAddress(address);
    if (contactId < 0) {
      return null;
    }

    Name name = getName(contactId);
    if (name == null) {
      Log.w(TAG, "Found a lookup key, but couldn't find a name associated with it. Very strange.");
      return null;
    }

    return new Contact(name,
                       getPhoneNumbers(contactId),
                       getEmails(contactId),
                       getPostalAddresses(contactId),
                       address);
  }

  @WorkerThread
  private @Nullable Name getName(long contactId) {
    try (Cursor cursor = contactsDatabase.getNameDetails(contactId)) {
      if (cursor != null && cursor.moveToFirst()) {
        return new Name(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
      }
    }

    return null;
  }

  @WorkerThread
  private @NonNull List<Phone> getPhoneNumbers(long contactId) {
    Map<String, Phone> numberMap = new HashMap<>();
    try (Cursor cursor = contactsDatabase.getPhoneDetails(contactId)) {
      while (cursor != null && cursor.moveToNext()) {
        // TODO: Use column constants
        String number    = cursor.getString(0);
        Phone  existing  = numberMap.get(number);
        Phone  candidate = new Phone(number, phoneTypeFromContactType(cursor.getInt(1)), cursor.getString(2));

        if (existing == null || (existing.getType() == Phone.Type.CUSTOM && existing.getLabel() == null)) {
          numberMap.put(number, candidate);
        }
      }
    }
    List<Phone> numbers = new ArrayList<>(numberMap.size());
    numbers.addAll(numberMap.values());
    return numbers;
  }

  private @NonNull List<Email> getEmails(long contactId) {
    List<Email> emails = new LinkedList<>();

    try (Cursor cursor = contactsDatabase.getEmailDetails(contactId)) {
      while (cursor != null && cursor.moveToNext()) {
        // TODO: Use column constants
        emails.add(new Email(cursor.getString(0), emailTypeFromContactType(cursor.getInt(1)), cursor.getString(2)));
      }
    }

    return emails;
  }

  private @NonNull List<PostalAddress> getPostalAddresses(long contactId) {
    List<PostalAddress> postalAddresses = new LinkedList<>();

    try (Cursor cursor = contactsDatabase.getPostalAddressDetails(contactId)) {
      while (cursor != null && cursor.moveToNext()) {
        // TODO: Use column constants
        postalAddresses.add(new PostalAddress(postalAddressTypeFromContactType(cursor.getInt(0)),
                                              cursor.getString(1),
                                              cursor.getString(2),
                                              cursor.getString(3),
                                              cursor.getString(4),
                                              cursor.getString(5),
                                              cursor.getString(6),
                                              cursor.getString(7),
                                              cursor.getString(8)));
      }
    }

    return postalAddresses;
  }

  private Phone.Type phoneTypeFromContactType(int type) {
    switch (type) {
      case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
        return Phone.Type.HOME;
      case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
        return Phone.Type.MOBILE;
      case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
        return Phone.Type.WORK;
    }
    return Phone.Type.CUSTOM;
  }

  private Email.Type emailTypeFromContactType(int type) {
    switch (type) {
      case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
        return Email.Type.HOME;
      case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
        return Email.Type.MOBILE;
      case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
        return Email.Type.WORK;
    }
    return Email.Type.CUSTOM;
  }

  private PostalAddress.Type postalAddressTypeFromContactType(int type) {
    switch (type) {
      case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
        return PostalAddress.Type.HOME;
      case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
        return PostalAddress.Type.WORK;
    }
    return PostalAddress.Type.CUSTOM;
  }

  interface Callback {
    void onComplete(@NonNull List<Contact> contacts);
  }
}

package org.thoughtcrime.securesms.contactshare.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.thoughtcrime.securesms.database.Address;

import java.util.Collections;
import java.util.List;

public class Contact implements Parcelable {

  private final Name                name;
  private final List<Phone>         phoneNumbers;
  private final List<Email>         emails;
  private final List<PostalAddress> postalAddresses;
  private final Address             avatarAddress;

  public Contact(@NonNull  Name                name,
                 @NonNull  List<Phone>         phoneNumbers,
                 @NonNull  List<Email>         emails,
                 @NonNull  List<PostalAddress> postalAddresses,
                 @NonNull  Address             avatarAddress)
  {
    this.name            = name;
    this.phoneNumbers    = Collections.unmodifiableList(phoneNumbers);
    this.emails          = Collections.unmodifiableList(emails);
    this.postalAddresses = Collections.unmodifiableList(postalAddresses);
    this.avatarAddress   = avatarAddress;
  }

  private Contact(Parcel in) {
    this(in.readParcelable(Name.class.getClassLoader()),
         in.createTypedArrayList(Phone.CREATOR),
         in.createTypedArrayList(Email.CREATOR),
         in.createTypedArrayList(PostalAddress.CREATOR),
         in.readParcelable(Address.class.getClassLoader()));
  }

  public static final Creator<Contact> CREATOR = new Creator<Contact>() {
    @Override
    public Contact createFromParcel(Parcel in) {
      return new Contact(in);
    }

    @Override
    public Contact[] newArray(int size) {
      return new Contact[size];
    }
  };

  public @NonNull Name getName() {
    return name;
  }

  public @NonNull List<Phone> getPhoneNumbers() {
    return phoneNumbers;
  }

  public @NonNull List<Email> getEmails() {
    return emails;
  }

  public @NonNull List<PostalAddress> getPostalAddresses() {
    return postalAddresses;
  }

  public @NonNull Address getAvatarAddress() {
    return avatarAddress;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(name, flags);
    dest.writeTypedList(phoneNumbers);
    dest.writeTypedList(emails);
    dest.writeTypedList(postalAddresses);
    dest.writeParcelable(avatarAddress, flags);
  }
}

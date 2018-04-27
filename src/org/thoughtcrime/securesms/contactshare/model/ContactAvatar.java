package org.thoughtcrime.securesms.contactshare.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class ContactAvatar implements Parcelable {

  private final Uri     imageUri;
  private final boolean isProfile;

  public ContactAvatar(@NonNull Uri imageUri, boolean isProfile) {
    this.imageUri  = imageUri;
    this.isProfile = isProfile;
  }

  private ContactAvatar(Parcel in) {
    this(in.readParcelable(Uri.class.getClassLoader()), in.readByte() != 0);
  }

  public @NonNull Uri getImageUri() {
    return imageUri;
  }

  public boolean isProfile() {
    return isProfile;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(imageUri, flags);
    dest.writeByte((byte) (isProfile ? 1 : 0));
  }

  public static final Creator<ContactAvatar> CREATOR = new Creator<ContactAvatar>() {
    @Override
    public ContactAvatar createFromParcel(Parcel in) {
      return new ContactAvatar(in);
    }

    @Override
    public ContactAvatar[] newArray(int size) {
      return new ContactAvatar[size];
    }
  };
}

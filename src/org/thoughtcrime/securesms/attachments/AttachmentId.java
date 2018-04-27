package org.thoughtcrime.securesms.attachments;

import android.os.Parcel;
import android.os.Parcelable;

import org.thoughtcrime.securesms.util.Util;

public class AttachmentId implements Parcelable {

  private final long rowId;
  private final long uniqueId;

  public AttachmentId(long rowId, long uniqueId) {
    this.rowId    = rowId;
    this.uniqueId = uniqueId;
  }

  protected AttachmentId(Parcel in) {
    this(in.readLong(), in.readLong());
  }

  public long getRowId() {
    return rowId;
  }

  public long getUniqueId() {
    return uniqueId;
  }

  public String[] toStrings() {
    return new String[] {String.valueOf(rowId), String.valueOf(uniqueId)};
  }

  public String toString() {
    return "(row id: " + rowId + ", unique ID: " + uniqueId + ")";
  }

  public boolean isValid() {
    return rowId >= 0 && uniqueId >= 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AttachmentId attachmentId = (AttachmentId)o;

    if (rowId != attachmentId.rowId) return false;
    return uniqueId == attachmentId.uniqueId;
  }

  @Override
  public int hashCode() {
    return Util.hashCode(rowId, uniqueId);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(rowId);
    dest.writeLong(uniqueId);
  }

  public static final Creator<AttachmentId> CREATOR = new Creator<AttachmentId>() {
    @Override
    public AttachmentId createFromParcel(Parcel in) {
      return new AttachmentId(in);
    }

    @Override
    public AttachmentId[] newArray(int size) {
      return new AttachmentId[size];
    }
  };
}

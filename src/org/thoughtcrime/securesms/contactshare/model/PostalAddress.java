package org.thoughtcrime.securesms.contactshare.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PostalAddress implements Selectable, Parcelable {

  private final Type   type;
  private final String label;
  private final String street;
  private final String poBox;
  private final String neighborhood;
  private final String city;
  private final String region;
  private final String postalCode;
  private final String country;

  private boolean selected;

  public PostalAddress(@NonNull  Type   type,
                       @Nullable String label,
                       @Nullable String street,
                       @Nullable String poBox,
                       @Nullable String neighborhood,
                       @Nullable String city,
                       @Nullable String region,
                       @Nullable String postalCode,
                       @Nullable String country)
  {
    this.type         = type;
    this.label        = label;
    this.street       = street;
    this.poBox        = poBox;
    this.neighborhood = neighborhood;
    this.city         = city;
    this.region       = region;
    this.postalCode   = postalCode;
    this.country      = country;
    this.selected     = true;
  }

  private PostalAddress(Parcel in) {
    this(Type.valueOf(in.readString()),
         in.readString(),
         in.readString(),
         in.readString(),
         in.readString(),
         in.readString(),
         in.readString(),
         in.readString(),
         in.readString());
  }

  public Type getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  public String getStreet() {
    return street;
  }

  public String getPoBox() {
    return poBox;
  }

  public String getNeighborhood() {
    return neighborhood;
  }

  public String getCity() {
    return city;
  }

  public String getRegion() {
    return region;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getCountry() {
    return country;
  }

  @Override
  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override
  public boolean isSelected() {
    return selected;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(type.name());
    dest.writeString(label);
    dest.writeString(street);
    dest.writeString(poBox);
    dest.writeString(neighborhood);
    dest.writeString(city);
    dest.writeString(region);
    dest.writeString(postalCode);
    dest.writeString(country);
  }

  public static final Creator<PostalAddress> CREATOR = new Creator<PostalAddress>() {
    @Override
    public PostalAddress createFromParcel(Parcel in) {
      return new PostalAddress(in);
    }

    @Override
    public PostalAddress[] newArray(int size) {
      return new PostalAddress[size];
    }
  };

  public enum Type {
    HOME, WORK, CUSTOM
  }
}

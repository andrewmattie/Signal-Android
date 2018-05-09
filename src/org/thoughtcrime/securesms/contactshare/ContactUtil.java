package org.thoughtcrime.securesms.contactshare;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Stream;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.database.Address;

import java.util.List;
import java.util.Locale;

public final class ContactUtil {


  public static long getContactIdFromUri(@NonNull Uri uri) {
    try {
      return Long.parseLong(uri.getLastPathSegment());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public static @Nullable Phone getDisplayNumber(@NonNull Contact contact) {
    return getDisplayNumber(new ContactRepository.ContactInfo(contact));
  }

  public static @Nullable Phone getDisplayNumber(@NonNull ContactRepository.ContactInfo contactInfo) {
    Contact contact = contactInfo.getContact();

    if (contact.getPhoneNumbers().size() == 0) {
      return null;
    }

    List<Phone> signalNumbers = Stream.of(contact.getPhoneNumbers()).filter(contactInfo::isPush).toList();
    if (signalNumbers.size() > 0) {
      return signalNumbers.get(0);
    }

    List<Phone> mobileNumbers = Stream.of(contact.getPhoneNumbers()).filter(number -> number.getType() == Phone.Type.MOBILE).toList();
    if (mobileNumbers.size() > 0) {
      return mobileNumbers.get(0);
    }

    return contact.getPhoneNumbers().get(0);
  }

  public static @NonNull String getPrettyPhoneNumber(@NonNull Phone phoneNumber, @NonNull Locale fallbackLocale) {
    PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    try {
      PhoneNumber parsed = util.parse(phoneNumber.getNumber(), fallbackLocale.getISO3Country());
      return util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    } catch (NumberParseException e) {
      return phoneNumber.getNumber();
    }
  }

  public static @NonNull String getNormalizedPhoneNumber(@NonNull Context context, @NonNull String number) {
    Address address = Address.fromExternal(context, number);
    return address.serialize();
  }

  public static @NonNull String getLocalPhoneNumber(@NonNull String number, @NonNull Locale fallbackLocale) {
    PhoneNumberUtil util = PhoneNumberUtil.getInstance();
    try {
      PhoneNumber parsed = util.parse(number, fallbackLocale.getISO3Country());
      return String.valueOf(parsed.getNationalNumber());
    } catch (NumberParseException e) {
      return number;
    }
  }
}

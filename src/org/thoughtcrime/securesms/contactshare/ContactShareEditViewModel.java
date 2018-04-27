package org.thoughtcrime.securesms.contactshare;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Selectable;
import org.thoughtcrime.securesms.database.Address;

import java.util.ArrayList;
import java.util.List;

class ContactShareEditViewModel extends ViewModel {

  private static final String TAG = ContactShareEditViewModel.class.getSimpleName();

  private final MutableLiveData<List<Contact>> contacts;

  ContactShareEditViewModel(@NonNull List<Address> addresses,
                            @NonNull ContactRepository contactRepository)
  {
    contacts = new MutableLiveData<>();
    contactRepository.getContacts(addresses, contacts::postValue);
  }

  @NonNull LiveData<List<Contact>> getContacts() {
    return contacts;
  }

  @NonNull List<Contact> getTrimmedContacts() {
    List<Contact> currentContacts = getCurrentContacts();
    List<Contact> newContacts     = new ArrayList<>(currentContacts.size());

    for (Contact contact : currentContacts) {
      newContacts.add(new Contact(contact.getName(),
                                  trimSelectables(contact.getPhoneNumbers()),
                                  trimSelectables(contact.getEmails()),
                                  trimSelectables(contact.getPostalAddresses()),
                                  contact.getAvatarAddress()));
    }

    return newContacts;
  }

  private <E extends Selectable> List<E> trimSelectables(List<E> selectables) {
    return Stream.of(selectables).filter(Selectable::isSelected).toList();
  }

  @NonNull
  private List<Contact> getCurrentContacts() {
    List<Contact> currentContacts = contacts.getValue();
    return currentContacts != null ? currentContacts : new ArrayList<>();
  }

  static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final List<Address>     addresses;
    private final ContactRepository contactRepository;

    Factory(@NonNull List<Address> addresses, @NonNull ContactRepository contactRepository) {
      this.addresses         = addresses;
      this.contactRepository = contactRepository;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return modelClass.cast(new ContactShareEditViewModel(addresses, contactRepository));
    }
  }
}

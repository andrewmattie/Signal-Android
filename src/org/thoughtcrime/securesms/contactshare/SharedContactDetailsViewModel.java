package org.thoughtcrime.securesms.contactshare;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.util.SingleLiveEvent;

class SharedContactDetailsViewModel extends ViewModel {

  private final ContactRepository repo;

  private final SingleLiveEvent<Event>          event;
  private final MutableLiveData<ContactDetails> contactDetails;

  SharedContactDetailsViewModel(@NonNull Contact contact, @NonNull ContactRepository repo) {
    this.repo           = repo;
    this.event          = new SingleLiveEvent<>();
    this.contactDetails = new MutableLiveData<>();

    this.contactDetails.postValue(new ContactDetails(contact, ContactState.NEW));
  }

  LiveData<Event> getEvent() {
    return event;
  }

  LiveData<ContactDetails> getContactDetails() {
    return contactDetails;
  }

  void saveAsNewContact() {
    event.postValue(Event.NEW_CONTACT_COMPLETE);
  }

  void saveDetailsToExistingContact(long contactId) {
    event.postValue(Event.EDIT_CONTACT_COMPLETE);
  }

  static class ContactDetails {

    private final Contact      contact;
    private final ContactState state;

    ContactDetails(@NonNull Contact contact, @NonNull ContactState state) {
      this.contact = contact;
      this.state   = state;
    }

    public @NonNull Contact getContact() {
      return contact;
    }

    public @NonNull ContactState getState() {
      return state;
    }
  }

  enum ContactState {
    NEW, SYSTEM_CONTACT, PUSH_CONTACT;
  }

  enum Event {
    NEW_CONTACT_COMPLETE, EDIT_CONTACT_COMPLETE
  }

  static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final Contact           contact;
    private final ContactRepository repo;

    Factory(@NonNull Contact contact, @NonNull ContactRepository repo) {
      this.contact = contact;
      this.repo    = repo;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return modelClass.cast(new SharedContactDetailsViewModel(contact, repo));
    }
  }
}

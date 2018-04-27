package org.thoughtcrime.securesms.contactshare;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.mms.GlideApp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Given a list of contacts, allows you to edit which fields you want to send before you share them.
 */
public class ContactShareEditFragment extends Fragment {

  private static final String KEY_ADDRESSES = "addresses";

  private ContactShareEditViewModel viewModel;
  private ContactShareEditAdapter   contactAdapter;
  private View                      sendButton;
  private EventListener             eventListener;

  static ContactShareEditFragment newInstance(@NonNull ArrayList<Address> contactIds) {
    Bundle args = new Bundle();
    args.putParcelableArrayList(KEY_ADDRESSES, contactIds);

    ContactShareEditFragment fragment = new ContactShareEditFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args == null) {
      throw new IllegalStateException("You must supply arguments to this fragment. Please use the #newInstance() method.");
    }

    List<Address> addresses = args.getParcelableArrayList(KEY_ADDRESSES);
    if (addresses == null) {
      throw new IllegalStateException("You must supply addresses to this fragment. Please use the #newInstance() method.");
    }

    // TODO: Unify executors in some class
    ContactRepository contactRepository = new ContactRepository(getContext(),
                                                                Executors.newSingleThreadExecutor(),
                                                                DatabaseFactory.getContactsDatabase(getContext()));

    viewModel = ViewModelProviders.of(this, new ContactShareEditViewModel.Factory(addresses, contactRepository)).get(ContactShareEditViewModel.class);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      eventListener = (EventListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement EventListener");
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_contact_share_edit, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    sendButton = view.findViewById(R.id.contact_share_edit_send);

    RecyclerView contactList = view.findViewById(R.id.contact_share_edit_list);
    contactList.setLayoutManager(new LinearLayoutManager(getContext()));
    contactList.getLayoutManager().setAutoMeasureEnabled(true);

    contactAdapter = new ContactShareEditAdapter(GlideApp.with(this));
    contactList.setAdapter(contactAdapter);
  }

  @Override
  public void onStart() {
    super.onStart();

    viewModel.getContacts().observe(this, contactAdapter::setContacts);

    sendButton.setOnClickListener(view -> eventListener.onSendClicked(viewModel.getTrimmedContacts()));
  }

  interface EventListener {
    void onSendClicked(List<Contact> contacts);
  }
}

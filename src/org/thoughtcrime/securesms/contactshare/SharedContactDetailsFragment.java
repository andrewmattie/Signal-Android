package org.thoughtcrime.securesms.contactshare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.model.Contact;

public class SharedContactDetailsFragment extends Fragment {

  public static final String KEY_CONTACT = "contact";

  private Contact contact;

  public static SharedContactDetailsFragment newInstance(@NonNull Contact contact) {
    Bundle args = new Bundle();
    args.putParcelable(KEY_CONTACT, contact);

    SharedContactDetailsFragment fragment = new SharedContactDetailsFragment();
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

    contact = args.getParcelable(KEY_CONTACT);
    if (contact == null) {
      throw new IllegalStateException("You must supply a contact to this fragment. Please use the #newInstance() method.");
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_shared_contact_details, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    TextView name = view.findViewById(R.id.contact_details_name);
    name.setText(contact.getName().getDisplayName());
  }
}

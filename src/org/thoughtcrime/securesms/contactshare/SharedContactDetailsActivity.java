package org.thoughtcrime.securesms.contactshare;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.ContactRepository.ContactInfo;
import org.thoughtcrime.securesms.contactshare.SharedContactDetailsViewModel.ContactViewDetails;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.database.Address;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.mms.AttachmentManager;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.util.CommunicationActions;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

// TODO: Extend password activity thing?
public class SharedContactDetailsActivity extends AppCompatActivity {

  private static final String TAG               = SharedContactDetailsActivity.class.getSimpleName();
  private static final int    CODE_PICK_CONTACT = 2323;

  public static final String KEY_CONTACT = "contact";

  private ContactFieldAdapter contactFieldAdapter;
  private ViewGroup           rootView;
  private TextView            nameView;
  private TextView            numberView;
  private ImageView           avatarView;
  private View                addButtonView;
  private View                inviteButtonView;
  private ViewGroup           engageContainerView;
  private View                messageButtonView;
  private View                callButtonView;

  private GlideRequests                 glideRequests;
  private SharedContactDetailsViewModel viewModel;


  public static Intent getIntent(@NonNull Context context, @NonNull Contact contact) {
    Intent intent = new Intent(context, SharedContactDetailsActivity.class);
    intent.putExtra(KEY_CONTACT, contact);
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_shared_contact_details);

    if (getIntent() == null) {
      throw new IllegalStateException("You must supply arguments to this activity. Please use the #newInstance() method.");
    }

    Contact contact = getIntent().getParcelableExtra(KEY_CONTACT);
    if (contact == null) {
      throw new IllegalStateException("You must supply addresses to this fragment. Please use the #newInstance() method.");
    }

    initToolbar();
    initViews();
    initViewModel(contact);
  }

  private void initToolbar() {
    Toolbar toolbar = ViewUtil.findById(this, R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setLogo(null);
    getSupportActionBar().setTitle("");
    toolbar.setNavigationOnClickListener(v -> onBackPressed());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(getResources().getColor(R.color.grey_400));
    }
  }

  private void initViews() {
    rootView            = findViewById(R.id.root);
    nameView            = findViewById(R.id.contact_details_name);
    numberView          = findViewById(R.id.contact_details_number);
    avatarView          = findViewById(R.id.contact_details_avatar);
    addButtonView       = findViewById(R.id.contact_details_add_button);
    inviteButtonView    = findViewById(R.id.contact_details_invite_button);
    engageContainerView = findViewById(R.id.contact_details_engage_container);
    messageButtonView   = findViewById(R.id.contact_details_message_button);
    callButtonView      = findViewById(R.id.contact_details_call_button);

    contactFieldAdapter = new ContactFieldAdapter(false);

    RecyclerView list = findViewById(R.id.contact_details_fields);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(contactFieldAdapter);

    glideRequests = GlideApp.with(this);
  }

  private void initViewModel(@NonNull Contact contact) {
    ContactRepository contactRepository = new ContactRepository(this,
                                                                Executors.newSingleThreadExecutor(),
                                                                DatabaseFactory.getContactsDatabase(this),
                                                                DatabaseFactory.getThreadDatabase(this));

    viewModel = ViewModelProviders.of(this, new SharedContactDetailsViewModel.Factory(contact, contactRepository))
                                  .get(SharedContactDetailsViewModel.class);

    viewModel.getEvent().observe(this, this::displayEvent);
    viewModel.getContactDetails().observe(this, this::displayContactDetails);
  }

  private void displayEvent(@Nullable SharedContactDetailsViewModel.Event event) {
    if (event == null) {
      return;
    }

    switch (event) {
      case NEW_CONTACT_SUCCESS:
        Snackbar.make(rootView, "Contact added.", Snackbar.LENGTH_LONG).show();
        break;
      case NEW_CONTACT_ERROR:
        Toast.makeText(this, "Error while adding contact", Toast.LENGTH_SHORT).show();
        break;
      case EDIT_CONTACT_SUCCESS:
        Snackbar.make(rootView, "Contact updated.", Snackbar.LENGTH_LONG).show();
        break;
      case EDIT_CONTACT_ERROR:
        Toast.makeText(this, "Error while editing contact", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private void displayContactDetails(@Nullable ContactViewDetails contactDetails) {
    if (contactDetails == null) {
      return;
    }

    ContactInfo contactInfo = contactDetails.getContactInfo();
    Contact     contact     = contactInfo.getContact();

    nameView.setText(contact.getName().getDisplayName());

    Phone displayNumber = ContactUtil.getDisplayNumber(contactInfo);
    if (displayNumber != null) {
      numberView.setVisibility(View.VISIBLE);
      // TODO: Use the custom locale thingy?
      numberView.setText(ContactUtil.getPrettyPhoneNumber(displayNumber, Locale.getDefault()));
    } else {
      numberView.setVisibility(View.GONE);
    }

    if (contact.getAvatar() != null && contact.getAvatar().getImage().getDataUri() != null) {
      glideRequests.load(new DecryptableUri(contact.getAvatar().getImage().getDataUri()))
          .fallback(R.drawable.ic_contact_picture)
          .circleCrop()
          .into(avatarView);
    } else {
      glideRequests.load(R.drawable.ic_contact_picture)
          .circleCrop()
          .into(avatarView);
    }

    contactFieldAdapter.setFields(this, contact.getPhoneNumbers(), contact.getEmails(), contact.getPostalAddresses());

    switch (contactDetails.getState()) {
      case NEW:
        displayNewContactBar();
        break;
      case ADDED:
        if (displayNumber != null) {
          displayAddedContactBar(contactInfo, displayNumber, contactInfo.isPush(displayNumber));
        }
        break;
      case LOADING:
        displayLoadingContactBar();
        break;
    }
  }

  private void displayNewContactBar() {
    addButtonView.setVisibility(View.VISIBLE);
    inviteButtonView.setVisibility(View.GONE);
    engageContainerView.setVisibility(View.GONE);

    addButtonView.setOnClickListener(v -> {
      new AlertDialog.Builder(this)
          // TODO: Strings
                     .setItems(new CharSequence[]{"Add as a new contact", "Add to existing contact"}, (dialog, which) -> {
                       if (which == 0) {
                         viewModel.saveAsNewContact();
                       } else {
                         AttachmentManager.selectContactInfo(this, CODE_PICK_CONTACT);
                       }
                     })
                    .create()
                    .show();
    });
  }

  private void displayAddedContactBar(@NonNull ContactInfo contactInfo, @NonNull Phone phoneNumber, boolean isPush) {
    if (!isPush) {
      inviteButtonView.setVisibility(View.VISIBLE);
      addButtonView.setVisibility(View.GONE);
      engageContainerView.setVisibility(View.GONE);

      List<Phone> systemNumbers = Stream.of(contactInfo.getContact().getPhoneNumbers()).filter(phone -> !contactInfo.isPush(phone)).toList();
      addButtonView.setOnClickListener(v -> selectNumber(systemNumbers, this::sendInvite));
    } else {
      engageContainerView.setVisibility(View.VISIBLE);
      addButtonView.setVisibility(View.GONE);
      inviteButtonView.setVisibility(View.GONE);

      messageButtonView.setOnClickListener(v -> {
        List<Phone> pushNumbers = Stream.of(contactInfo.getContact().getPhoneNumbers()).filter(contactInfo::isPush).toList();

        selectNumber(pushNumbers, phone -> {
          Address address = Address.fromExternal(this, phone.getNumber());
          viewModel.getThreadId(address).observe(this, threadId -> {
            if (threadId == null) {
              Log.e(TAG, "Got a null threadId. This should never happen.");
              return;
            }

            CommunicationActions.startConversation(this, address, threadId, null);
          });
        });
      });

      callButtonView.setOnClickListener(v -> {
        List<Phone> pushNumbers = Stream.of(contactInfo.getContact().getPhoneNumbers()).filter(contactInfo::isPush).toList();

        selectNumber(pushNumbers, phone -> {
          viewModel.getResolvedRecipient(phone).observe(this, recipient -> {
            if (recipient == null) {
              Log.e(TAG, "Got a null recipient. This should never happen.");
              return;
            }

            CommunicationActions.startVoiceCall(this, recipient);
          });
        });
      });
    }
  }

  private void displayLoadingContactBar() {
    // TODO: Spinner or something
    addButtonView.setVisibility(View.GONE);
    inviteButtonView.setVisibility(View.GONE);
    engageContainerView.setVisibility(View.GONE);
  }

  private void selectNumber(@NonNull List<Phone> phoneNumbers, @NonNull PhoneSelectedCallback callback) {
    if (phoneNumbers.size() > 1) {
      CharSequence[] values = new CharSequence[phoneNumbers.size()];

      for (int i = 0; i < values.length; i++) {
        values[i] = phoneNumbers.get(i).getNumber();
      }

      new AlertDialog.Builder(this)
          .setItems(values, ((dialog, which) -> callback.onSelected(phoneNumbers.get(which))))
          .create()
          .show();
    } else {
      callback.onSelected(phoneNumbers.get(0));
    }
  }

  private void sendInvite(@NonNull Phone phoneNumber) {
    Address address = Address.fromExternal(this, phoneNumber.getNumber());

    viewModel.getThreadId(address).observe(this, threadId -> {
      if (threadId == null) {
        Log.e(TAG, "Got a null threadId. This should never happen.");
        return;
      }

      CommunicationActions.startConversation(this, address, threadId, getString(R.string.InviteActivity_lets_switch_to_signal, "https://sgnl.link/1KpeYmF"));
    });
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (data == null || data.getData() == null || resultCode != RESULT_OK) {
      return;
    }

    if (requestCode == CODE_PICK_CONTACT) {
      long contactId = ContactUtil.getContactIdFromUri(data.getData());
      viewModel.saveDetailsToExistingContact(contactId);
    }
  }

  private interface PhoneSelectedCallback {
    void onSelected(@NonNull Phone phone);
  }
}

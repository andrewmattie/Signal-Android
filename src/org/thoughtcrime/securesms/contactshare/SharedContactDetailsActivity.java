package org.thoughtcrime.securesms.contactshare;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.SharedContactDetailsViewModel.ContactDetails;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.mms.AttachmentManager;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.util.ViewUtil;

import java.util.concurrent.Executors;

// TODO: Extend password activity thing?
public class SharedContactDetailsActivity extends AppCompatActivity {

  private static final int CODE_PICK_CONTACT = 2323;

  public static final String KEY_CONTACT = "contact";

  private ContactFieldAdapter contactFieldAdapter;
  private TextView            nameView;
  private TextView            numberView;
  private ImageView           avatarView;
  private View                addButtonView;

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
    nameView      = findViewById(R.id.contact_details_name);
    numberView    = findViewById(R.id.contact_details_number);
    avatarView    = findViewById(R.id.contact_details_avatar);
    addButtonView = findViewById(R.id.contact_details_add_button);

    contactFieldAdapter = new ContactFieldAdapter(false);

    RecyclerView list = findViewById(R.id.contact_details_fields);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(contactFieldAdapter);

    glideRequests = GlideApp.with(this);
  }

  private void initViewModel(@NonNull Contact contact) {
    ContactRepository contactRepository = new ContactRepository(this, Executors.newSingleThreadExecutor(), DatabaseFactory.getContactsDatabase(this));

    viewModel = ViewModelProviders.of(this, new SharedContactDetailsViewModel.Factory(contact, contactRepository))
                                  .get(SharedContactDetailsViewModel.class);

    viewModel.getEvent().observe(this, this::handleEvent);
    viewModel.getContactDetails().observe(this, this::handleContactDetails);
  }

  private void handleEvent(@Nullable SharedContactDetailsViewModel.Event event) {
    if (event == null) {
      return;
    }

    switch (event) {
      case NEW_CONTACT_COMPLETE:
        // TODO: Snackbar
        Toast.makeText(this, "Contact added!", Toast.LENGTH_SHORT).show();
        break;
      case EDIT_CONTACT_COMPLETE:
        // TODO: Snackbar
        Toast.makeText(this, "Contact updated!", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private void handleContactDetails(@Nullable ContactDetails contactDetails) {
    if (contactDetails == null) {
      return;
    }

    Contact contact = contactDetails.getContact();

    nameView.setText(contact.getName().getDisplayName());

    if (contact.getPhoneNumbers().size() > 0) {
      // TODO: Whenever we display a number, we should probably prioritize mobile -- in conversation as well
      numberView.setText(contact.getPhoneNumbers().get(0).getNumber());
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
      case NEW:            displayNewContactBar();    break;
      case SYSTEM_CONTACT: displaySystemContactBar(); break;
      case PUSH_CONTACT:   displayPushContactBar();   break;
    }
  }

  private void displayNewContactBar() {
    addButtonView.setOnClickListener(v -> {
      AttachmentManager.selectContactInfo(this, CODE_PICK_CONTACT);
    });
  }

  private void displaySystemContactBar() {

  }

  private void displayPushContactBar() {

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
}

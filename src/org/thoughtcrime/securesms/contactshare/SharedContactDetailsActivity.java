package org.thoughtcrime.securesms.contactshare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.util.ViewUtil;

// TODO: Extend password activity thing?
public class SharedContactDetailsActivity extends AppCompatActivity {

  public static final String KEY_CONTACT = "contact";

  private Contact contact;

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

    contact = getIntent().getParcelableExtra(KEY_CONTACT);
    if (contact == null) {
      throw new IllegalStateException("You must supply addresses to this fragment. Please use the #newInstance() method.");
    }

    initToolbar();
    initHeader();
    initFields();
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

  private void initHeader() {
    TextView  name   = findViewById(R.id.contact_details_name);
    TextView  number = findViewById(R.id.contact_details_number);
    ImageView avatar = findViewById(R.id.contact_details_avatar);

    name.setText(contact.getName().getDisplayName());

    if (contact.getPhoneNumbers().size() > 0) {
      // TODO: Whenever we display a number, we should probably prioritize mobile -- in conversation as well
      number.setText(contact.getPhoneNumbers().get(0).getNumber());
    }

    GlideRequests glideRequests = GlideApp.with(this);

    if (contact.getAvatar() != null && contact.getAvatar().getImage().getDataUri() != null) {
      glideRequests.load(new DecryptableUri(contact.getAvatar().getImage().getDataUri()))
          .fallback(R.drawable.ic_contact_picture)
          .circleCrop()
          .into(avatar);
    } else {
      glideRequests.load(R.drawable.ic_contact_picture)
          .circleCrop()
          .into(avatar);
    }
  }

  private void initFields() {
    ContactFieldAdapter adapter = new ContactFieldAdapter(false);
    adapter.setFields(this, contact.getPhoneNumbers(), contact.getEmails(), contact.getPostalAddresses());

    RecyclerView list = findViewById(R.id.contact_details_fields);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setAdapter(adapter);
  }

}

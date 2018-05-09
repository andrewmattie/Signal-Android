package org.thoughtcrime.securesms.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contactshare.ContactUtil;
import org.thoughtcrime.securesms.contactshare.model.Contact;
import org.thoughtcrime.securesms.contactshare.model.Phone;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader;
import org.thoughtcrime.securesms.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.thoughtcrime.securesms.mms.GlideRequests;

import java.util.Locale;

public class SharedContactView extends LinearLayout {


  private ImageView avatarView;
  private TextView  nameView;
  private TextView  numberView;
  private TextView  actionButton;

  private AddToContactsClickedListener addToContactsClickedListener;

  public SharedContactView(Context context) {
    super(context);
    initialize();
  }

  public SharedContactView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }

  public SharedContactView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public SharedContactView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize();
  }

  private void initialize() {
    inflate(getContext(), R.layout.shared_contact_view, this);

    avatarView   = findViewById(R.id.contact_avatar);
    nameView     = findViewById(R.id.contact_name);
    numberView   = findViewById(R.id.contact_number);
    actionButton = findViewById(R.id.contact_action_button);
  }

  public void setContact(@NonNull GlideRequests glideRequests, @NonNull Contact contact) {
    if (contact.getAvatar() != null && contact.getAvatar().getImage().getDataUri() != null) {
      glideRequests.load(new DecryptableUri(contact.getAvatar().getImage().getDataUri()))
          .fallback(R.drawable.ic_contact_picture)
                   .circleCrop()
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .into(avatarView);
    } else {
      glideRequests.load(R.drawable.ic_contact_picture)
                   .circleCrop()
                   .diskCacheStrategy(DiskCacheStrategy.ALL)
                   .into(avatarView);
    }

    nameView.setText(contact.getName().getDisplayName());

    Phone displayNumber = ContactUtil.getDisplayNumber(contact);
    if (displayNumber != null) {
      // TODO: Use locale
      numberView.setText(ContactUtil.getPrettyPhoneNumber(displayNumber, Locale.getDefault()));
    } else {
      numberView.setText("");
    }

    // TODO: String
    actionButton.setText("Add to Contacts");
    actionButton.setOnClickListener(v -> {
      if (addToContactsClickedListener != null) {
        addToContactsClickedListener.onAddToContactsClicked(contact);
      }
    });
  }

  public View getAvatarView() {
    return avatarView;
  }

  public void setOnAddToContactsClickedListener(AddToContactsClickedListener listener) {
    this.addToContactsClickedListener = listener;
  }

  public interface AddToContactsClickedListener {
    void onAddToContactsClicked(@NonNull Contact contact);
  }
}

package de.nulide.findmydevice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.utils.SMS;

public class LockScreenMessage extends AppCompatActivity {

    public static final String SENDER = "sender";
    private String sender;

    private TextView tvLockScreenMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen_message);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        Bundle bundle = getIntent().getExtras();
        sender = bundle.getString(SENDER);
        Settings settings;
        IO.context = this;
        settings = IO.read(Settings.class, IO.settingsFileName);

        tvLockScreenMessage = findViewById(R.id.textViewLockScreenMessage);
        tvLockScreenMessage.setText(settings.getLockScreenMessage());
    }

    @Override
    protected void onPause() {
        SMS.sendMessage(sender, "LockScreenMessage: Usage detected.");
        super.onPause();
    }

}
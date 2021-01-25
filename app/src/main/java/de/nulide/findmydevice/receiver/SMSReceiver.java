package de.nulide.findmydevice.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.WhiteList;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONSettings;
import de.nulide.findmydevice.data.io.json.JSONWhiteList;
import de.nulide.findmydevice.utils.ExpiredTempWhiteListedTask;
import de.nulide.findmydevice.utils.MessageHandler;
import de.nulide.findmydevice.utils.Notifications;
import de.nulide.findmydevice.utils.Permission;
import de.nulide.findmydevice.utils.SaveTimerTask;

public class SMSReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    public static final String RELOAD_DATA = "de.nulide.reload.data";

    private WhiteList whiteList;
    private String tempContact;
    private Settings settings;
    private Date timeUntilNextUsage;

    public SMSReceiver() {
        timeUntilNextUsage = Calendar.getInstance().getTime();
    }

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        if(whiteList == null){
            init(context);
        }
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Date time = Calendar.getInstance().getTime();
            if (time.getTime() > timeUntilNextUsage.getTime()) {
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String format = bundle.getString("format");
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    boolean isVersionM =
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        if (isVersionM) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                        String receiver = msgs[i].getOriginatingAddress();
                        receiver.replace(" ", "");
                        boolean inWhitelist = false;
                        for (int iwl = 0; iwl < whiteList.size(); iwl++) {
                            if (receiver.equals(whiteList.get(iwl).getNumber())) {
                                MessageHandler.handle(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody(), context);
                                inWhitelist = true;
                            }
                        }
                        if((Boolean) settings.get(Settings.SET_ACCESS_VIA_PIN)) {
                            if (!inWhitelist && tempContact != null && tempContact.equals(receiver)) {
                                MessageHandler.handle(receiver, msgs[i].getMessageBody(), context);
                                inWhitelist = true;
                            }
                            if (!inWhitelist && MessageHandler.checkForPin(msgs[i].getMessageBody())) {
                                Timer tempContactExpireTimer = new Timer();
                                tempContactExpireTimer.schedule(new ExpiredTempWhiteListedTask(receiver, this), 10000);
                                tempContact = receiver;
                                MessageHandler.handle(receiver, msgs[i].getMessageBody(), context);
                            }
                        }
                    }
                }

                Calendar now = Calendar.getInstance();
                now.add(Calendar.SECOND, 1);
                timeUntilNextUsage = now.getTime();
            }
        }else if(intent.getAction().equals(BOOT_COMPLETED)){
            init(context);
            Notifications.notify(context, "AfterBootTest", "Service running", Notifications.CHANNEL_LIFE);
        }else if(intent.getAction().equals(RELOAD_DATA)){
            init(context);
        }
    }

    private void init(Context context){
        IO.context = context;
        whiteList = JSONFactory.convertJSONWhiteList(IO.read(JSONWhiteList.class, IO.whiteListFileName));
        settings = JSONFactory.convertJSONSettings(IO.read(JSONSettings.class, IO.settingsFileName));
        Permission.initValues(context);
        MessageHandler.init(settings);
    }

    public void removeTemp(){
       // tempContact = null;
    }

}

package com.offlineprogrammer.braintrainer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazon.device.messaging.ADMConstants;
import com.amazon.device.messaging.ADMMessageHandlerBase;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationClient;
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationDetails;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ADMMessageHandler extends ADMMessageHandlerBase {

    private final static String TAG = ADMMessageHandler.class.getName();
    private static PinpointManager pinpointManager;

    protected ADMMessageHandler(String className) {
        super(className);
    }

    public ADMMessageHandler() {
        super(ADMMessageHandler.class.getName());
    }

    public static PinpointManager getPinpointManager(final Context applicationContext) {
        if (pinpointManager == null) {
            final AWSConfiguration awsConfig = new AWSConfiguration(applicationContext);
            AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    Log.i("INIT", userStateDetails.getUserState().toString());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("INIT", "Initialization error.", e);
                }
            });

            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    applicationContext,
                    AWSMobileClient.getInstance(),
                    awsConfig);

            pinpointManager = new PinpointManager(pinpointConfig);


        }
        return pinpointManager;
    }


    @Override
    protected void onMessage(Intent intent) {
        Log.i(TAG, "SampleADMMessageHandler:onMessage");

        /* String to access message field from data JSON. */
        final String msgKey = getString(R.string.json_data_msg_key);

        /* String to access timeStamp field from data JSON. */
        final String timeKey = getString(R.string.json_data_time_key);

        /* Intent action that will be triggered in onMessage() callback. */
        final String intentAction = getString(R.string.intent_msg_action);

        /* Extras that were included in the intent. */
        final Bundle extras = intent.getExtras();

        verifyMD5Checksum(extras);

        /* Extract message from the extras in the intent. */
        final String msg = extras.getString(msgKey);
        final String time = extras.getString(timeKey);

        if (msg == null || time == null)
        {
            Log.w(TAG, "SampleADMMessageHandler:onMessage Unable to extract message data." +
                    "Make sure that msgKey and timeKey values match data elements of your JSON message");
        }

        /* Create a notification with message data. */
        /* This is required to test cases where the app or device may be off. */
        ADMHelper.createADMNotification(this, msgKey, timeKey, intentAction, msg, time);

        /* Intent category that will be triggered in onMessage() callback. */
        final String msgCategory = getString(R.string.intent_msg_category);

        /* Broadcast an intent to update the app UI with the message. */
        /* The broadcast receiver will only catch this intent if the app is within the onResume state of its lifecycle. */
        /* User will see a notification otherwise. */
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(intentAction);
        broadcastIntent.addCategory(msgCategory);
        broadcastIntent.putExtra(msgKey, msg);
        broadcastIntent.putExtra(timeKey, time);
        this.sendBroadcast(broadcastIntent);



        NotificationDetails details = NotificationDetails.builder()
                .intent(intent)
                .intentAction(NotificationClient.ADM_INTENT_ACTION)
                .build();

        pinpointManager.getNotificationClient().handleCampaignPush(details);


        

    }

    @Override
    protected void onRegistrationError(String s) {
        Log.e(TAG, "SampleADMMessageHandler:onRegistrationError " + s);

    }

    @Override
    protected void onRegistered(String registrationId) {
        Log.i(TAG, "SampleADMMessageHandler:onRegistered");
        Log.i(TAG, registrationId);

        /* Register the app instance's registration ID with your server. */
        MyServerMsgHandler srv = new MyServerMsgHandler();
        srv.registerAppInstance(getApplicationContext(), registrationId);


        getPinpointManager(getApplicationContext());


        pinpointManager.getNotificationClient().registerDeviceToken(registrationId);
    }

    @Override
    protected void onUnregistered(String registrationId) {
        Log.i(TAG, "SampleADMMessageHandler:onUnregistered");

        /* Unregister the app instance's registration ID with your server. */
        MyServerMsgHandler srv = new MyServerMsgHandler();
        srv.unregisterAppInstance(getApplicationContext(), registrationId);
    }

    private void verifyMD5Checksum(final Bundle extras)
    {
        /* String to access consolidation key field from data JSON. */
        final String consolidationKey = getString(R.string.json_data_consolidation_key);

        final Set<String> extrasKeySet = extras.keySet();
        final Map<String, String> extrasHashMap = new HashMap<String, String>();
        for (String key : extrasKeySet)
        {
            if (!key.equals(ADMConstants.EXTRA_MD5) && !key.equals(consolidationKey))
            {
                extrasHashMap.put(key, extras.getString(key));
            }
        }
        final String md5 = ADMMD5ChecksumCalculator.calculateChecksum(extrasHashMap);
        Log.i(TAG, "SampleADMMessageHandler:onMessage App md5: " + md5);

        /* Extract md5 from the extras in the intent. */
        final String admMd5 = extras.getString(ADMConstants.EXTRA_MD5);
        Log.i(TAG, "SampleADMMessageHandler:onMessage ADM md5: " + admMd5);

        /* Data integrity check. */
        if(!admMd5.trim().equals(md5.trim()))
        {
            Log.w(TAG, "SampleADMMessageHandler:onMessage MD5 checksum verification failure. " +
                    "Message received with errors");
        }
    }
}

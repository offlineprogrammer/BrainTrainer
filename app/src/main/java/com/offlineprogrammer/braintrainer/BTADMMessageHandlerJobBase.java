/*
 * [BTADMMessageHandlerJobBase.java]
 *
 * (c) 2019, Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

package com.offlineprogrammer.braintrainer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazon.device.messaging.ADMConstants;
import com.amazon.device.messaging.ADMMessageHandlerJobBase;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationClient;
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationDetails;
import com.amplifyframework.core.Amplify;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The BTADMMessageHandlerJobBase class receives messages sent by ADM via the BTADMMessageReceiver receiver.
 *
 * @version Revision: 1, Date: 11/20/2019
 */
public class BTADMMessageHandlerJobBase extends ADMMessageHandlerJobBase
{
    /** Tag for logs. */
    private final static String TAG = "ADMSampleJobBase";
    private static PinpointManager pinpointManager;

    /**
     * Class constructor.
     */
    public BTADMMessageHandlerJobBase()
    {
        super();
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

    private void recordEvent(String sEventName)  {
        try {
            Amplify.Analytics.recordEvent(sEventName);
            // Plugin will automatically flush events.
            // You do not have to do this in the app code.
            Amplify.Analytics.flushEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /** {@inheritDoc} */
    @Override
    protected void onMessage(final Context context, final Intent intent)
    {

        NotificationDetails details = NotificationDetails.builder()
                .intent(intent)
                .intentAction(NotificationClient.ADM_INTENT_ACTION)
                .build();

        pinpointManager.getNotificationClient().handleCampaignPush(details);

        recordEvent("Notification " );
        Log.i(TAG, "BTADMMessageHandlerJobBase:onMessage");

        /* String to access message field from data JSON. */
        final String msgKey = context.getString(R.string.json_data_msg_key);

        /* String to access timeStamp field from data JSON. */
        final String timeKey = context.getString(R.string.json_data_time_key);

        /* Intent action that will be triggered in onMessage() callback. */
        final String intentAction = context.getString(R.string.intent_msg_action);

        /* Extras that were included in the intent. */
        final Bundle extras = intent.getExtras();

        verifyMD5Checksum(context, extras);

        /* Extract message from the extras in the intent. */
        final String msg = extras.getString(msgKey);
        final String time = extras.getString(timeKey);

        if (msg == null || time == null)
        {
            Log.w(TAG, "BTADMMessageHandlerJobBase:onMessage Unable to extract message data." +
                    "Make sure that msgKey and timeKey values match data elements of your JSON message");
        }

        /* Create a notification with message data. */
        /* This is required to test cases where the app or device may be off. */
        ADMHelper.createADMNotification(context, msgKey, timeKey, intentAction, msg, time);

        /* Intent category that will be triggered in onMessage() callback. */
        final String msgCategory = context.getString(R.string.intent_msg_category);

        /* Broadcast an intent to update the app UI with the message. */
        /* The broadcast receiver will only catch this intent if the app is within the onResume state of its lifecycle. */
        /* User will see a notification otherwise. */
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(intentAction);
        broadcastIntent.addCategory(msgCategory);
        broadcastIntent.putExtra(msgKey, msg);
        broadcastIntent.putExtra(timeKey, time);
        context.sendBroadcast(broadcastIntent);

    }

    /**
     * This method verifies the MD5 checksum of the ADM message.
     *
     * @param extras Extra that was included with the intent.
     */
    private void verifyMD5Checksum(final Context context, final Bundle extras)
    {
        /* String to access consolidation key field from data JSON. */
        final String consolidationKey = context.getString(R.string.json_data_consolidation_key);

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
        Log.i(TAG, "BTADMMessageHandlerJobBase:onMessage App md5: " + md5);

        /* Extract md5 from the extras in the intent. */
        final String admMd5 = extras.getString(ADMConstants.EXTRA_MD5);
        Log.i(TAG, "BTADMMessageHandlerJobBase:onMessage ADM md5: " + admMd5);

        /* Data integrity check. */
        if(!admMd5.trim().equals(md5.trim()))
        {
            Log.w(TAG, "BTADMMessageHandlerJobBase:onMessage MD5 checksum verification failure. " +
                    "Message received with errors");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onRegistrationError(final Context context, final String string)
    {
        Log.e(TAG, "BTADMMessageHandlerJobBase:onRegistrationError " + string);
    }

    /** {@inheritDoc} */
    @Override
    protected void onRegistered(final Context context, final String registrationId)
    {
        Log.i(TAG, "BTADMMessageHandlerJobBase:onRegistered");
        Log.i(TAG, registrationId);

        getPinpointManager(context.getApplicationContext());


        pinpointManager.getNotificationClient().registerDeviceToken(registrationId);

        /* Register the app instance's registration ID with your server. */
       // MyServerMsgHandler srv = new MyServerMsgHandler();
       // srv.registerAppInstance(context.getApplicationContext(), registrationId);
    }

    /** {@inheritDoc} */
    @Override
    protected void onUnregistered(final Context context, final String registrationId)
    {
        Log.i(TAG, "BTADMMessageHandlerJobBase:onUnregistered");

        /* Unregister the app instance's registration ID with your server. */
       // MyServerMsgHandler srv = new MyServerMsgHandler();
       // srv.unregisterAppInstance(context.getApplicationContext(), registrationId);
    }

}

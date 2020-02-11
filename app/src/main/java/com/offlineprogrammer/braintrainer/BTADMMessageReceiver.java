/*
 * [BTADMMessageReceiver.java]
 *
 * (c) 2019, Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */
package com.offlineprogrammer.braintrainer;

import com.amazon.device.messaging.ADMMessageReceiver;

/**
 * The BTADMMessageReceiver class listens for messages from ADM and forwards them.
 *
 * @version Revision: 1, Date: 11/20/2019
 */
public class BTADMMessageReceiver extends ADMMessageReceiver {
    public BTADMMessageReceiver() {
        super(ADMMessageHandler.class);
        if(ADMHelper.IS_ADM_V2) {
            registerJobServiceClass(BTADMMessageHandlerJobBase.class, ADMHelper.JOB_ID);
        }
    }
}

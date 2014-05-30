//
//  Created by Adalbert Wysocki on 5/27/14.
//  Copyright 2014 Clearbon, Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//

package com.clearbon.cordova.netswipe;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jumio.netswipe.sdk.NetswipeSDK;
import com.jumio.netswipe.sdk.PlatformNotSupportedException;
import com.jumio.netswipe.sdk.core.NetswipeCardInformation;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;


public class NetSwipePlugin extends CordovaPlugin {
    
    public static final String Init = "init";
    public static final String ScanCard = "scanCard";
    
    private static final String LogTag = "NetSwipePlugin";
    
    private CallbackContext callbackContext;
    private NetswipeSDK sdk; 
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        PluginResult result = null;

        if (action.equals(Init)) {
            init(args);
            result = new PluginResult(Status.NO_RESULT);
            result.setKeepCallback(false);
            return true;
        } else if (action.equals(ScanCard)) {
            scanCard(args);
            result = new PluginResult(Status.NO_RESULT);
            result.setKeepCallback(true);
            return true;
        } else {
            result = new PluginResult(Status.INVALID_ACTION);
            callbackContext.error("Invalid Action");
            return false;
        }
    }
        
    /**
     * Init the NetSwipe required auth parameters (apiKey/apiSecret).
     * @param data parameters
     */
    private void init (JSONArray data) {
        if (data.length() < 2) {
            Log.e(LogTag, "Missing required parameters apiToken or/and apiSecret. Not initialized.");
            return;
        }

        if (!android.os.Build.MODEL.equals("sdk") && NetswipeSDK.isRooted()) {
            Log.d(LogTag, "NetSwipe SDK can't run on rooted device. Not initialized.");
            return;
        }
        if (!NetswipeSDK.isSupportedPlatform(cordova.getActivity())) {
            Log.d(LogTag, "Platform not supported. Not initialized.");
            return;
        }
        
        try {
            String apiToken = data.getString(0);
            String apiSecret = data.getString(1);

            Log.d(LogTag, "Initializing NetSwipe SDK with apiToken: " + apiToken + " ,apiSecret: " + apiSecret);
            sdk = new NetswipeSDK(cordova.getActivity(), apiToken, apiSecret);

            Log.i(LogTag, "Initialized NetSwipe SDK (version: " + NetswipeSDK.getVersion() + ")");
            
        } catch (JSONException e) {
            Log.e(LogTag, "Invalid parameters apiToken or/and apiSecret. Not initialized.");
            return;
        } catch (PlatformNotSupportedException e) {
            Log.e(LogTag, "Error initializing the NetSwipe SDK. Not initialized.", e);
            return;
        }
    }
    
    /**
     * Scan command to trigger the NetSwipe SDK. 
     * @param data options
     */
    private void scanCard(JSONArray data) {
        if (sdk == null) {
            try {
                JSONObject res = new JSONObject();
                res.put("code", 0);
                res.put("message", "Not initialized. Call init first.");
                callbackContext.error(res);
                return;
            } catch (JSONException e) {
            }
        }
        
        //configuring the sdk
        if (data.length() == 1) {
            try {
                JSONObject options = data.getJSONObject(0);
                sdk.setCardHolderNameRequired(getOption(options, "cardHolderNameRequired", true));
                sdk.setSortCodeAndAccountNumberRequired(getOption(options, "sortCodeAndAccountNumberRequired", false));
                sdk.setManualEntryEnabled(getOption(options, "manualEntryEnabled", true));
                sdk.setExpiryRequired(getOption(options, "expiryRequired", true));
                sdk.setCvvRequired(getOption(options, "cvvRequired", true));
            } catch (JSONException e) {
                Log.e(LogTag, "Error configuring the NetSwipe SDK. Running with partial settings.", e);
            }
        }
        
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    sdk.start();
                } catch (Exception e) {
                    Log.e(LogTag, "Error starting the NetSwipe SDK.", e);
                }
            }
        };

        this.cordova.setActivityResultCallback(this);
        this.cordova.getActivity().runOnUiThread(runnable);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { 
        if (requestCode == NetswipeSDK.REQUEST_CODE) { 
            /*ArrayList<String> scanAttempts = 
                data.getStringArrayListExtra(NetswipeSDK.EXTRA_SCAN_ATTEMPTS);*/ 
            if (resultCode == Activity.RESULT_OK) { 
                NetswipeCardInformation cardInformation = 
                        data.getParcelableExtra(NetswipeSDK.EXTRA_CARD_INFORMATION);
                
                //TODO card type
                //CreditCardType creditCardType = cardInformation.getCardType();

                JSONObject cardInfo = new JSONObject();
                try {
                    cardInfo.put("cardNumber", getStringValue(cardInformation.getCardNumber(), true));
                    cardInfo.put("expiryMonth", getStringValue(cardInformation.getExpiryDateMonth(), false));
                    cardInfo.put("expiryYear", getStringValue(cardInformation.getExpiryDateYear(), false));
                    cardInfo.put("cvv", getStringValue(cardInformation.getCvvCode(), false));
                    cardInfo.put("cardHolderName", getStringValue(cardInformation.getCardHolderName(), false));
                    cardInfo.put("sortCode", getStringValue(cardInformation.getSortCode(), false));
                    cardInfo.put("accountNumber", getStringValue(cardInformation.getAccountNumber(), false));
                    cardInfo.put("cardNumberManuallyEntered", cardInformation.isCardNumberManuallyEntered());
            
                    //TODO custom fields
                    //String zipCode = cardInformation.getCustomField("zip_code");
                    
                    callbackContext.success(cardInfo);
                    
                } catch (JSONException e) {
                    Log.e(LogTag, "Error creating return parameters for success callback.", e);
                } finally {
                    cardInformation.clear();
                    cardInfo.remove("cardNumber");
                    cardInfo.remove("expiryMonth");
                    cardInfo.remove("expiryYear");
                    cardInfo.remove("cvv");
                    cardInfo.remove("cardHolderName");
                    cardInfo.remove("sortCode");
                    cardInfo.remove("accountNumber");
                    cardInfo.remove("cardNumberManuallyEntered");
                    cardInfo = null;
                }
            } else if (resultCode == Activity.RESULT_CANCELED) { 
                try {
                    int errorCode = data.getIntExtra(NetswipeSDK.EXTRA_ERROR_CODE, 0); 
                    String errorMessage = data.getStringExtra(NetswipeSDK.EXTRA_ERROR_MESSAGE);
                    
                    JSONObject cardInfo = new JSONObject();
                    cardInfo.put("code", errorCode);
                    cardInfo.put("message", errorMessage);
                    
                    callbackContext.error(cardInfo);
                } catch (JSONException e) {
                    Log.e(LogTag, "Error creating return parameters for error callback.", e);
                }
            }  
        } 
    } 
    
    private boolean getOption (JSONObject options, String name, boolean defaultValue) {
        boolean res = defaultValue;
        if (options.has(name)) {
            try {
                if (options.getInt(name) == 1) {
                    res = true;
                } else {
                    res = false;
                }
            } catch (JSONException e) {
                Log.e(LogTag, "Invalid value for option " + name + " using default: " + defaultValue);
            }
        }
        return res;
    }
    
    private String getStringValue (char[] value, boolean removeSpaces) {
        String res = "";
        if (value != null && value.length > 0) {
            res = new String(value);
            if (removeSpaces) {
                res = res.replace(" ", "");
            }
        }
        return res;
    }
}

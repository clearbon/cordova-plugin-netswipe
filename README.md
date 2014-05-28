cordova-plugin-netswipe
=======================

Plugin to integrate NetSwipe SDK from Jumio into Cordova/PhoneGap based applications

#Introduction

NetSwipe Cordova plugin allows to integrate the NetSwipe SDK from Jumio into your applications.
NetSwipe enables apps to scan the payments cards (debit/credit) in order to accept payments in your apps. NetSwipe is much more reliable in recognizing card information than similar card.io SDK. It also includes recognition of expiration date, card holder name on card, includes a manual capture with customizable fields and provides server storage of all swipes for transaction verifications/tracking.

NetSwipe is a commerical product and a proper commercial license is required for this plugin to work. 

Contact Jumio sales department for more information (<https://www.jumio.com/netswipe/>).


#Requirements 
The following is required for this plugin to work:

* Cordova v3.0.0+
* NetSwipe SDK for iOS v2.0.0+ & iOS 6.0+ (for iOS integration only) 
* NetSwipe SDK for Android v2.0.0+ & Android 4.0+ (API level 14) (for Android integration only)

#Supported Platforms

* iOS
* Android (coming soon)

#Installation 

After installing the NetSwipe SDK following the documentation, install the plugin:

	cordova plugin add org.apache.cordov.file


#Usage

##Initialization

Init the plugin with NetSwipe app key and secret.

	CardScanner.init("<APP_KEY>","<APP_SECRET>");


##Request card scan


	CardScanner.scanCard(options, successCallback, errorCallback)

* _options_: key/value pairs enabling/disabling features and configuring the look&feel. Supported options:

Name | Values | Description
---- | ------ | -----------
cardHolderNameRequired | 1 (default) / 0 | enbales the capture of the card holder
expiryRequired | 1 (default) / 0 | enbales the capture of the expiration date
cvvRequired | 1 (default) / 0 | enbales the capture of the cvv
manualEntryEnabled | 1 (default) / 0 | enbales the manual entry flow
sortCodeAndAccountNumberRequired | 1 / 0 (default) | enables the identification of the sort code and account number

* _successCallback(cardInfo)_: function called after a successful scan

Name | Type | Description
---- | ------ | -----------
cardNumber | String | card number
expiryMonth | String | card expiration month
expiryYear | String | card expiration year
cvv | String | verification code
cardHolderName | String | card holder name
sortCode | String | sort code
accountNumber | String | account number
cardNumberManuallyEntered | int | whether the card number was entered manually or not

* _errorCallback(error)_: function called after a successful scan

Name | Type | Description
---- | ------ | -----------
code | String | Error code
message | String | Error message

See NetSwipe SDK documentation for the list of error codes and associated messages.  
	
ex:
	
	CardScanner.scanCard({
		manualEntryEnabled: 0,
		sortCodeAndAccountNumberRequired: 1
	}, function (cardInfo) {
       console.log('Success: [Card #:' + cardInfo.cardNumber + ', Exp Date:' + cardInfo.expiryMonth + '/' + cardInfo.expiryYear + ', cvv:' + cardInfo.cvv);               
    }, function (error) {
       console.log('Error [Code:' + error.code + ', Message: ' + error.message);
    });
    
#Limitations

The following features are not yet exposed:

* customize theme
* add custom fields
 

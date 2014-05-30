//
//  NetSwipePlugin.c
//
//  Created by Adalbert Wysocki on 5/23/14.
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

#import "NetSwipePlugin.h"

@interface NetSwipePlugin() {
}

//Util to get boolean options
+ (BOOL) getOption:(NSString*)optionName fromOptions:(NSDictionary *)options withDefault:(BOOL)defaultValue;

@end

@implementation NetSwipePlugin

@synthesize netSwipeController;
@synthesize hasPendingOperation;

- (void)init:(CDVInvokedUrlCommand*)command
{
    if (self.netSwipeController != nil) {
        NSLog(@"Already initialized. Returning.");
        return;
    }
    
    NSUInteger argc = [command.arguments count];
    if (argc < 2) {
        NSLog(@"Missing required parameters apiToken and apiSecret. Not initialized.");
        return;
    }
    
    NSString *merchantApiToken = [command.arguments objectAtIndex:0];
    NSString *merchantApiSecret = [command.arguments objectAtIndex:1];
    
    self.netSwipeController = [[NetswipeViewController alloc]
                               initWithMerchantApiToken: merchantApiToken apiSecret: merchantApiSecret delegate:
                               self];
    
    NSLog(@"Initialized NetSwipe API (version: %@)", [NetswipeViewController sdkVersion]);
}

- (void)scanCard:(CDVInvokedUrlCommand*)command
{
    if (self.netSwipeController == nil) {
        NSString *message = @"Not initialized. Call init first.";
        NSLog(@"%@", message);
        
        NSDictionary *result = [NSDictionary dictionaryWithObjectsAndKeys: @(0), @"code", message, @"message", nil];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsDictionary:result];
        [self writeJavascript:[pluginResult toErrorCallbackString:self.callbackId]];
        return;
    }
    if (self.hasPendingOperation) {
        NSLog(@"Operation already in progress.");
        return;
    }
    self.hasPendingOperation = YES;
    self.callbackId = command.callbackId;
    
    NSDictionary *options = [command.arguments objectAtIndex:0];
    
    //Setting options
    self.netSwipeController.cardHolderNameRequired =
        [NetSwipePlugin getOption:@"cardHolderNameRequired" fromOptions:options withDefault:YES];
    self.netSwipeController.sortCodeAndAccountNumberRequired =
        [NetSwipePlugin getOption:@"sortCodeAndAccountNumberRequired" fromOptions:options withDefault:NO];
    self.netSwipeController.manualEntryEnabled =
        [NetSwipePlugin getOption:@"manualEntryEnabled" fromOptions:options withDefault:YES];
    self.netSwipeController.expiryRequired =
        [NetSwipePlugin getOption:@"expiryRequired" fromOptions:options withDefault:YES];
    self.netSwipeController.cvvRequired =
        [NetSwipePlugin getOption:@"cvvRequired" fromOptions:options withDefault:YES];
    
    [self.viewController presentViewController: self.netSwipeController animated: YES completion: nil];
}

- (void)netswipeViewController:(NetswipeViewController *)controller didFinishScanWithCardInformation:(NetswipeCardInformation *)cardInformation requestReference:(NSString *)requestReference{
    
    //Use the cardInformation...
    NSString *cardNumber = ((cardInformation.cardNumber != nil)?cardInformation.cardNumber:@"");
    NSString *cardNumberGrouped = ((cardInformation.cardNumberGrouped != nil)?cardInformation.cardNumberGrouped:@"");
    NSString *expiryMonth = ((cardInformation.cardExpiryMonth != nil)?cardInformation.cardExpiryMonth:@"");
    NSString *expiryYear = ((cardInformation.cardExpiryYear != nil)?cardInformation.cardExpiryYear:@"");
    NSString *expiryDate = ((cardInformation.cardExpiryDate != nil)?cardInformation.cardExpiryDate:@"");
    NSString *cvv = ((cardInformation.cardCVV != nil)?cardInformation.cardCVV:@"");
    NSString *cardHolderName = ((cardInformation.cardHolderName != nil)?cardInformation.cardHolderName:@"");
    NSString *sortCode = ((cardInformation.cardSortCode != nil)?cardInformation.cardSortCode:@"");
    NSString *accountNumber = ((cardInformation.cardAccountNumber != nil)?cardInformation.cardAccountNumber:@"");
    NSString *cardNumberManuallyEntered = ((cardInformation.cardNumberManuallyEntered)?@"true":@"false");
    
    //Get the value of the additional field
    //NSMutableString *zipCode = [cardInformation valueForFieldID: @"idZipCode"];
    //NSLog(@"Additional field value: %@", zipCode);
    
    //Dismiss the SDK
    [self.viewController dismissViewControllerAnimated: YES completion: nil];
    NSMutableDictionary *result = [NSMutableDictionary dictionaryWithObjectsAndKeys: cardNumber, @"cardNumber", expiryMonth, @"expiryMonth", expiryYear, @"expiryYear", cvv, @"cvv", cardHolderName, @"cardHolderName", sortCode, @"sortCode", accountNumber, @"accountNumber", cardNumberManuallyEntered, @"cardNumberManuallyEntered", nil];

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
													   messageAsDictionary:result];
    
    [self writeJavascript:[pluginResult toSuccessCallbackString:self.callbackId]];

    //Clearing the information
    [cardInformation clear];
    [result removeAllObjects];
    
    self.hasPendingOperation = NO;
}

- (void)netswipeViewController:(NetswipeViewController *)controller didCancelWithError:(NSError *)error{
    NSInteger code = error.code;
    NSString *message = error.localizedDescription;
    NSLog(@"Canceled with error code: %d, message: %@", code, message);
    
    //Dismiss the SDK
    [self.viewController dismissViewControllerAnimated: YES completion: nil];
    
    NSDictionary *result = [NSDictionary dictionaryWithObjectsAndKeys: @(code), @"code", message, @"message", nil];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                  messageAsDictionary:result];
    
    [self writeJavascript:[pluginResult toErrorCallbackString:self.callbackId]];
    self.hasPendingOperation = NO;
}

+ (BOOL) getOption:(NSString*)optionName fromOptions:(NSDictionary *)options withDefault:(BOOL)defaultValue {
    NSObject *value = [options objectForKey:optionName];
    if (value && [value isKindOfClass:[NSNumber class]]) {
        return [((NSNumber *)value) boolValue];
    } else{
        return defaultValue;
    }
}

@end
#import "FirebaseInvitesPlugin.h"

@implementation FirebaseInvitesPlugin {
    NSString *_inviteCallbackId;
    id <FIRInviteBuilder> _inviteDialog;
}

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Invites plugin");

    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    [GIDSignIn sharedInstance].clientID = [FIRApp defaultApp].options.clientID;
    [GIDSignIn sharedInstance].uiDelegate = self;
    [GIDSignIn sharedInstance].delegate = self;
}

- (void)handleOpenURLWithApplicationSourceAndAnnotation:(NSNotification*)notification {
    NSDictionary*  notificationData = [notification object];

    if ([notificationData isKindOfClass: NSDictionary.class]) {
        NSURL* url = notificationData[@"url"];
        id annotation = notificationData[@"annotation"];
        NSString* sourceApplication = notificationData[@"sourceApplication"];

        if ([url isKindOfClass:NSURL.class] && [sourceApplication isKindOfClass:NSString.class]) {
            [[GIDSignIn sharedInstance] handleURL:url
                                sourceApplication:sourceApplication
                                       annotation:annotation];
        }
    }
}

- (void)invite:(CDVInvokedUrlCommand *)command {
    _inviteCallbackId = command.callbackId;

    NSDictionary* options = command.arguments[0];
    // Only title and message properties are mandatory (and checked in JS API)
    NSString *title = options[@"title"];
    NSString *message = options[@"message"];
    NSString *deepLink = options[@"deepLink"];
    NSString *emailAction = options[@"emailAction"];
    NSString *emailImage = options[@"emailImage"];
    NSString *androidClientID = [FIROptions defaultOptions].androidClientID;

    _inviteDialog = [FIRInvites inviteDialog];

    [_inviteDialog setInviteDelegate:self];
    [_inviteDialog setTitle:title];
    [_inviteDialog setMessage:message];
    [_inviteDialog setDeepLink:deepLink];
    [_inviteDialog setCallToActionText:emailAction];
    [_inviteDialog setCustomImage:emailImage];
    // in case an Android app is available:
    if (androidClientID) {
        FIRInvitesTargetApplication *targetApplication = [FIRInvitesTargetApplication new];
        // The Android client ID from the Google API console project (?)
        targetApplication.androidClientID = androidClientID;
        [_inviteDialog setOtherPlatformsTargetApplication:targetApplication];
    }
    // trigger google signin required for invites dialog
    [[GIDSignIn sharedInstance] signIn];
}

#pragma mark FIRInviteDelegate

- (void)inviteFinishedWithInvitations:(NSArray *)invitationIds
                                error:(nullable NSError *)error {
    CDVPluginResult *pluginResult;
    if (error) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:invitationIds];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:_inviteCallbackId];
}

#pragma mark GIDSignInDelegate

- (void)signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user
                                          withError:(NSError *)error {
    if (error == nil) {
        [_inviteDialog open];
    } else {
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_inviteCallbackId];
    }
}

#pragma mark GIDSignInUIDelegate

- (void)signIn:(GIDSignIn *)signIn presentViewController:(UIViewController *)viewController {
    [self.viewController presentViewController:viewController animated:YES completion:nil];
}

- (void)signIn:(GIDSignIn *)signIn dismissViewController:(UIViewController *)viewController {
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}

@end

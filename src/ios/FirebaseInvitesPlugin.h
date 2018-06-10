#import <Cordova/CDV.h>
@import Firebase;
@import GoogleSignIn;

@interface FirebaseInvitesPlugin : CDVPlugin<FIRInviteDelegate, GIDSignInDelegate, GIDSignInUIDelegate>

- (void)invite:(CDVInvokedUrlCommand*)command;

@end

#import <Cordova/CDV.h>
@import Firebase;
@import GoogleSignIn;

@interface FirebaseInvitePlugin : CDVPlugin<FIRInviteDelegate, GIDSignInDelegate, GIDSignInUIDelegate>

- (void)invite:(CDVInvokedUrlCommand*)command;

@end

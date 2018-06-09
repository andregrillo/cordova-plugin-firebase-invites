#import <Cordova/CDV.h>
@import Firebase;

@interface FirebaseInvitePlugin : CDVPlugin<FIRInviteDelegate>

- (void)invite:(CDVInvokedUrlCommand*)command;

@property (nonatomic, copy) NSString *inviteCallbackId;

@end

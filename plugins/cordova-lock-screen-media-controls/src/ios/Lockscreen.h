#import <Cordova/CDV.h>

@interface Lockscreen : CDVPlugin


- (void)init:(CDVInvokedUrlCommand*)command;
- (void)setState:(CDVInvokedUrlCommand*)command;
- (void)setMetadata:(CDVInvokedUrlCommand*)command;
- (void)release:(CDVInvokedUrlCommand*)command;
- (void)listenActions:(CDVInvokedUrlCommand*)command;

- (void)setCurrentTime:(CDVInvokedUrlCommand*)command;
- (void)setSkipInterval:(CDVInvokedUrlCommand*)command;

@end
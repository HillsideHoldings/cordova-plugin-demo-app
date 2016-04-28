#import <Cordova/CDV.h>

@interface Lockscreen : CDVPlugin

- (void)setTitle:(CDVInvokedUrlCommand*)command;
- (void)setSubtitle:(CDVInvokedUrlCommand*)command;
- (void)setTrackLength:(CDVInvokedUrlCommand*)command;
- (void)setCurrentTime:(CDVInvokedUrlCommand*)command;
- (void)setArtworkURL:(CDVInvokedUrlCommand*)command;
- (void)setSkipInterval:(CDVInvokedUrlCommand*)command;
- (void)registerActions:(CDVInvokedUrlCommand*)command;

@end
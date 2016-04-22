#import "Lockscreen.h"
#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MediaPlayer.h>


@interface Lockscreen ()

@property (nonatomic, copy) NSString *trackTitle;
@property (nonatomic, copy) NSString *trackSubtitle;
@property (nonatomic, copy) NSNumber *trackDuration;
@property (nonatomic, copy) NSNumber *currentPosition;
@property (nonatomic, copy) UIImage *artworkImage;
@property (nonatomic, copy) NSString *actionsID;

@end

@implementation Lockscreen

-(void)prepareControls {
    MPRemoteCommandCenter *rcc = [MPRemoteCommandCenter sharedCommandCenter];
    MPSkipIntervalCommand *skipBackwardIntervalCommand = [rcc skipBackwardCommand];
    [skipBackwardIntervalCommand setEnabled:YES];
    [skipBackwardIntervalCommand addTarget:self action:@selector(skipBackwardEvent:)];
    
    MPSkipIntervalCommand *skipForwardIntervalCommand = [rcc skipForwardCommand];
    [skipForwardIntervalCommand setEnabled:YES];
    [skipForwardIntervalCommand addTarget:self action:@selector(skipForwardEvent:)];
    
    MPRemoteCommand *pauseCommand = [rcc pauseCommand];
    [pauseCommand setEnabled:YES];
    [pauseCommand addTarget:self action:@selector(pauseEvent:)];
    
    MPRemoteCommand *playCommand = [rcc playCommand];
    [playCommand setEnabled:YES];
    [playCommand addTarget:self action:@selector(playEvent:)];
    
    MPRemoteCommand *remoteCommand = [rcc togglePlayPauseCommand];
    [remoteCommand setEnabled:YES];
    [remoteCommand addTarget:self action:@selector(toggleEvent:)];
}

- (void)pluginInitialize {
    [self prepareControls];
}



- (void)setPlaybackInfo {
    NSMutableDictionary *episodeInfo = [NSMutableDictionary dictionary];
    episodeInfo[MPMediaItemPropertyArtist] = self.trackSubtitle;
    episodeInfo[MPMediaItemPropertyMediaType] = @(MPMediaTypePodcast);
    episodeInfo[MPMediaItemPropertyTitle] = self.trackTitle;
    episodeInfo[MPMediaItemPropertyPlaybackDuration] = self.trackDuration;
    episodeInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = self.currentPosition;
    episodeInfo[MPNowPlayingInfoPropertyPlaybackRate] = @1;
    
    if(self.artworkImage) {
        MPMediaItemArtwork *artwork = [[MPMediaItemArtwork alloc] initWithImage:self.artworkImage];
        episodeInfo[MPMediaItemPropertyArtwork] = artwork;
    }
    [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:episodeInfo];
}

#pragma mark commands
- (void)setTitle:(CDVInvokedUrlCommand*)command {
    self.trackTitle = [command argumentAtIndex:0];
    [self setPlaybackInfo];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)setSubtitle:(CDVInvokedUrlCommand*)command {
    self.trackSubtitle = [command argumentAtIndex:0];
    [self setPlaybackInfo];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)setTrackLength:(CDVInvokedUrlCommand*)command {
    self.trackDuration = [command argumentAtIndex:0];
    [self setPlaybackInfo];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)setCurrentTime:(CDVInvokedUrlCommand*)command {
    self.currentPosition = [command argumentAtIndex:0];
    [self setPlaybackInfo];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)setArtworkURL:(CDVInvokedUrlCommand*)command {
    NSURL *artworkURL = [NSURL URLWithString:[command argumentAtIndex:0]];
    NSData *imgData = [NSData dataWithContentsOfURL:artworkURL];
    self.artworkImage = [UIImage imageWithData:imgData];
    [self setPlaybackInfo];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)setSkipInterval:(CDVInvokedUrlCommand*)command {
    MPRemoteCommandCenter *rcc = [MPRemoteCommandCenter sharedCommandCenter];
    MPSkipIntervalCommand *skipBackwardIntervalCommand = [rcc skipBackwardCommand];
    skipBackwardIntervalCommand.preferredIntervals = @[[command argumentAtIndex:0]];
    MPSkipIntervalCommand *skipForwardIntervalCommand = [rcc skipForwardCommand];
    skipForwardIntervalCommand.preferredIntervals = @[[command argumentAtIndex:0]];
}

- (void)registerActions:(CDVInvokedUrlCommand*)command {
    self.actionsID = command.callbackId;
}

#pragma mark - Lock screen actions
-(void)playEvent:(MPRemoteCommandEvent *)event {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)pauseEvent:(MPRemoteCommandEvent *)event {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:2];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)toggleEvent:(MPRemoteCommandEvent *)event {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:3];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)skipBackwardEvent:(MPSkipIntervalCommandEvent *)skipEvent {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:4];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)skipForwardEvent:(MPSkipIntervalCommandEvent *)skipEvent {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:5];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

@end
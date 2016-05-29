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

-(void)releaseControls {
    MPRemoteCommandCenter *rcc = [MPRemoteCommandCenter sharedCommandCenter];
    MPSkipIntervalCommand *skipBackwardIntervalCommand = [rcc skipBackwardCommand];
    [skipBackwardIntervalCommand removeTarget:self];
    MPSkipIntervalCommand *skipForwardIntervalCommand = [rcc skipForwardCommand];
    [skipForwardIntervalCommand removeTarget:self];
    MPRemoteCommand *pauseCommand = [rcc pauseCommand];
    [pauseCommand removeTarget:self];
    MPRemoteCommand *playCommand = [rcc playCommand];
    [playCommand removeTarget:self];
    MPRemoteCommand *remoteCommand = [rcc togglePlayPauseCommand];
    [remoteCommand removeTarget:self];
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
    episodeInfo[MPNowPlayingInfoPropertyPlaybackRate] = @1;
    episodeInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = self.currentPosition;
    
    if(self.artworkImage) {
        MPMediaItemArtwork *artwork = [[MPMediaItemArtwork alloc] initWithImage:self.artworkImage];
        episodeInfo[MPMediaItemPropertyArtwork] = artwork;
    }
    [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:episodeInfo];
}

#pragma mark commands
- (void)setCurrentTime:(CDVInvokedUrlCommand*)command {
    self.currentPosition = [command argumentAtIndex:0];
    
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

- (void)init:(CDVInvokedUrlCommand*)command {
    
}

- (void)setState:(CDVInvokedUrlCommand*)command {
    
}

- (void)setMetadata:(CDVInvokedUrlCommand*)command {
    NSDictionary *metadata = [command argumentAtIndex:0];
    
    if(metadata[@"title"])
    self.trackTitle = metadata[@"title"];
    if(metadata[@"subTitle"])
    self.trackSubtitle = metadata[@"subTitle"];
    if(metadata[@"duration"])
    self.trackDuration = metadata[@"duration"];
    if(metadata[@"image"]) {
        NSURL *artworkURL = [NSURL URLWithString:metadata[@"image"]];
        NSData *imgData = [NSData dataWithContentsOfURL:artworkURL];
        self.artworkImage = [UIImage imageWithData:imgData];
    }
    [self setPlaybackInfo];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)release:(CDVInvokedUrlCommand*)command {
    [self releaseControls];
}

- (void)listenActions:(CDVInvokedUrlCommand*)command {
    self.actionsID = command.callbackId;
}

#pragma mark - Lock screen actions
-(void)playEvent:(MPRemoteCommandEvent *)event {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"PLAY"];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)pauseEvent:(MPRemoteCommandEvent *)event {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"PAUSE"];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)toggleEvent:(MPRemoteCommandEvent *)event {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"PLAY_PAUSE"];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)skipBackwardEvent:(MPSkipIntervalCommandEvent *)skipEvent {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"REWIND"];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

-(void)skipForwardEvent:(MPSkipIntervalCommandEvent *)skipEvent {
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"FORWARD"];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.actionsID];
}

@end
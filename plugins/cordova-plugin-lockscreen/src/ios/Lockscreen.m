#import "Lockscreen.h"
#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MediaPlayer.h>


@interface Lockscreen ()

@property (nonatomic, copy) NSString *mediaTitle;

@end

@implementation Lockscreen

-(void)prepareControls {
    MPRemoteCommandCenter *rcc = [MPRemoteCommandCenter sharedCommandCenter];
    MPSkipIntervalCommand *skipBackwardIntervalCommand = [rcc skipBackwardCommand];
    [skipBackwardIntervalCommand setEnabled:YES];
    [skipBackwardIntervalCommand addTarget:self action:@selector(skipBackwardEvent:)];
    //    skipBackwardIntervalCommand.preferredIntervals = @[@(15)];  // Set your own interval
    
    MPSkipIntervalCommand *skipForwardIntervalCommand = [rcc skipForwardCommand];
    //    skipForwardIntervalCommand.preferredIntervals = @[@(15)];  // Max 99
    [skipForwardIntervalCommand setEnabled:YES];
    [skipForwardIntervalCommand addTarget:self action:@selector(skipForwardEvent:)];
    
    MPRemoteCommand *pauseCommand = [rcc pauseCommand];
    [pauseCommand setEnabled:YES];
    [pauseCommand addTarget:self action:@selector(playOrPauseEvent:)];
    //
    MPRemoteCommand *playCommand = [rcc playCommand];
    [playCommand setEnabled:YES];
    [playCommand addTarget:self action:@selector(playOrPauseEvent:)];
}

- (void)pluginInitialize {
    [self prepareControls];
}



- (void)setPlaybackInfo {
    NSMutableDictionary *episodeInfo = [NSMutableDictionary dictionary];
    episodeInfo[MPMediaItemPropertyArtist] = @"Artist";
    episodeInfo[MPMediaItemPropertyPodcastTitle] = @"Podcast Title";
    episodeInfo[MPMediaItemPropertyMediaType] = @(MPMediaTypePodcast);
    episodeInfo[MPMediaItemPropertyTitle] = self.mediaTitle;
    episodeInfo[MPMediaItemPropertyPlaybackDuration] = @(60);
    episodeInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = @(30);
    episodeInfo[MPNowPlayingInfoPropertyPlaybackRate] = @1;
    [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:episodeInfo];
}

- (void)setTitle:(CDVInvokedUrlCommand*)command {
    self.mediaTitle = [command argumentAtIndex:0];
    [self setPlaybackInfo];
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                   messageAsInt:0];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

@end
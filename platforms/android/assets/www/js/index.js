/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
	initialize : function() {
		this.bindEvents();
	},
	bindEvents : function() {
		document.addEventListener('deviceready', this.onDeviceReady, false);
	},

	onDeviceReady : function() {
		// document.addEventListener("resume", onResume, false);
		// document.addEventListener("pause", onPause, false);
		document.addEventListener("backbutton", onBackKey, false);
		document.getElementById("playbtn").addEventListener("click", playAudio);
		initVolumeSlide();
	},

};
app.initialize();

var skipInterval = 30;
var my_media = null;
var isPlaying = false;
var mediaTimer = null;
var lockscreen = null;
var volumeSlider = null;

function initMedia() {
	var url = "http://www.songspk320z.us/songoftheday/[Songs.PK]%20Khaike%20Paan%20Banaraswala%20-%20Don%20(2006).mp3";
	// Play the audio file at url
	my_media = new Media(url,
	// success callback
	function() {
		console.log("playAudio():Audio Success");
	},
	// error callback
	function(err) {
		console.log("playAudio():Audio Error: " + err);
	});

	initLockScreenControls();

}

function initLockScreenControls() {
	lockscreen = new Lockscreen();
	lockscreen.setState(Lockscreen.STATE_STOPPED);
	lockscreen.listenActions(onLockScreenEventReceived);
	// let's tell system that our app should not go in pause state.
	cordova.plugins.backgroundMode.enable();

}

// Play audio
function playAudio() {
	if (my_media == null)
		initMedia();

	if (isPlaying) {
		// Stop audio
		my_media.stop();
		my_media.release();
		isPlaying = false;
		// clearInterval(mediaTimer);
		document.getElementById("playbtn").innerHTML = "Play";
		document.getElementById("buff").innerHTML = "0%";
		if (lockscreen != null) {
			lockscreen.setState(Lockscreen.STATE_PAUSED);
		}
		
	} else {
		// Play audio
		my_media.play();
		isPlaying = true;
		document.getElementById("playbtn").innerHTML = "Stop";

//		 clearInterval(mediaTimer);
//		 Update media position every second
//		 mediaTimer = setInterval(function() {
//		 updateBufferValue();
//		 }, 1000);

		if (lockscreen != null) {

			lockscreen.setState(Lockscreen.STATE_PLAYING);
			lockscreen.setMetadata({
				"title" : "Khai k paan banaras wala",
				"subTitle" : "Don",
				"duration" : 123000,
				"image" : "http://www.songspk320z.us/img/globe.png"
			});

			// lets show this information on notification as well
			cordova.plugins.backgroundMode.setDefaults({
				title : "Playing...",
				ticker : "Playing...",
				text : "Khai k paan banaras wala"
			});
		}
	}

}

function updateBufferValue() {
	// get media position
	my_media
			.getBufferedPercent(
					// success callback
					function(per) {
						if (per > -1) {
							document.getElementById("buff").innerHTML = per
									+ "%";
						}

						var p = eval(per);
						if (per >= 100) {
							clearInterval(mediaTimer);
						}
					},
					// error callback
					function(e) {
						console.log("Error getting buffered percentage = " + e);
						document.getElementById("buff").innerHTML = "Error getting buffered percentage.";
						clearInterval(mediaTimer);
					});
}

function onLockScreenEventReceived(event) {
	if (event.action == Lockscreen.ACTION_PLAY_PAUSE) {
		// toggle playing
		playAudio();

	} else if (event.action == Lockscreen.ACTION_REWIND) {

		my_media.getCurrentPosition(function(p) {
			p -= skipInterval;
			my_media.seekTo(1000 * p);
		}, null);

	} else if (event.action == Lockscreen.ACTION_FORWARD) {

		my_media.getCurrentPosition(function(p) {
			p += skipInterval;
			my_media.seekTo(1000 * p);
		}, null);
	}else if (event.action == Lockscreen.ACTION_HEADSET_SINGLE_CLICK) {
        // toggle playing
        playAudio();

    } else if (event.action == Lockscreen.ACTION_HEADSET_DOUBLE_CLICK) {

        alert("Double click from headset detected");

    } else if (event.action == Lockscreen.ACTION_HEADSET_TRIPPLE_CLICK) {

        alert("Tripple click from headset detected");
    }
}

function initVolumeSlide() {

	volumeSlider = window.plugins.volumeSlider;
	volumeSlider.createVolumeSlider(10, 50, 300, 30); // origin x, origin y,
														// width, height
	volumeSlider.showVolumeSlider();

	// lets give some timeout so that volume slider is fully loaded.
	setTimeout(function() {

		volumeSlider.getVolumeLevel(function(vol) {
			console.log("Getting current volume: " + vol);
		}, function(err) {
			console.log("Error getting volume: " + err);
		});

		// Register for event to get volume when user press volume up or down
		// key. Do not forget to unregister this when application is closed.
		volumeSlider.registerVolumeUpdate(function(vol) {
			console.log("Volume changed: " + vol);
		});

	}, 500);

}

function onBackKey() {
	// lets release the lock screen media now. It is necessary.
	if (lockscreen != null) {
		lockscreen.release();
		cordova.plugins.backgroundMode.disable();
	}

	// unregister volume update to avoid memory leaks.
	if (volumeSlider != null) {
		volumeSlider.unRegisterVolumeUpdate();
	}

	navigator.app.exitApp();
}
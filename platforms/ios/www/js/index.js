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
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');
        
        document.getElementById("playbtn").addEventListener("click", playAudio);
        initMedia();
        initVolumeSlide();
    },

};

app.initialize();


var skipInterval = 25;
var my_media = null;
var isPlaying = false;
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
  // let's tell system that our app should not go in pause state.
//	cordova.plugins.backgroundMode.enable();
}

function initLockScreenControls() {
	lockscreen = new Lockscreen();
	lockscreen.setState(Lockscreen.STATE_STOPPED);
	lockscreen.listenActions(onLockScreenEventReceived);
  lockscreen.setSkipInterval(skipInterval);
}

// Play audio
//
function playAudio() {
	if (isPlaying) {
		// Pause audio
		my_media.pause();
		isPlaying = false;
		document.getElementById("playbtn").innerHTML = "Play";
		lockscreen.setState(Lockscreen.STATE_PAUSED);
	} else {
		// Play audio
		my_media.play();
		isPlaying = true;
		document.getElementById("playbtn").innerHTML = "Pause";

		lockscreen.setState(Lockscreen.STATE_PLAYING);
    lockscreen.setMetadata({
      "title" : "Khai k paan banaras wala",
      "subTitle" : "Don",
      "image" : "http://www.songspk320z.us/img/globe.png"
    });

    setTimeout(function() {
      lockscreen.setMetadata({"duration": my_media.getDuration()} );
      my_media.getCurrentPosition(function(p) {
        lockscreen.setCurrentTime(p);
  		}, null);
    }, 100);

	}
}

function onLockScreenEventReceived(event) {
	if (event == Lockscreen.ACTION_PLAY_PAUSE
      || event == Lockscreen.ACTION_PLAY
      || event == Lockscreen.ACTION_PAUSE) {
		// toggle playing
		playAudio();
	} else if (event == Lockscreen.ACTION_REWIND) {
		my_media.getCurrentPosition(function(p) {
			p -= skipInterval;
			my_media.seekTo(1000 * p);
      lockscreen.setCurrentTime(p);
		}, null);
	} else if (event == Lockscreen.ACTION_FORWARD) {
		my_media.getCurrentPosition(function(p) {
			p += skipInterval;
			my_media.seekTo(1000 * p);
      lockscreen.setCurrentTime(p);
		}, null);
	}
}

function initVolumeSlide() {
	volumeSlider = window.plugins.volumeSlider;
	volumeSlider.createVolumeSlider(10, 450, 300, 30); // origin x, origin y, width, height
	volumeSlider.showVolumeSlider();
	// lets give some timeout so that volume slider is fully loaded.
	setTimeout(function(){

		volumeSlider.getVolumeLevel(function(vol){
			console.log("Getting current volume: "+vol);
		}, function(err){
			console.log("Error getting volume: "+err);
		});

		// Register for event to get volume when user press volume up or down key. Do not forget to unregister this when application is closed.
		volumeSlider.registerVolumeUpdate(function(vol){
			console.log("Volume changed: "+vol);
		});

	}, 500);
}

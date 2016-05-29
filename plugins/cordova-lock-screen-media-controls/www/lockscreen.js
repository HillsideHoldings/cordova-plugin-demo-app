		var Lockscreen = function() {
			cordova.exec(null, null, "Lockscreen", "init", []);
		};

		// Media states
		Lockscreen.STATE_PLAYING = "PLAYING";
		Lockscreen.STATE_PAUSED = "PAUSED";
		Lockscreen.STATE_STOPPED = "STOPPED";
		Lockscreen.ACTION_PLAY_PAUSE = "PLAY_PAUSE";
		Lockscreen.ACTION_PLAY = "PLAY";
		Lockscreen.ACTION_PAUSE = "PAUSE";
		Lockscreen.ACTION_REWIND = "REWIND";
		Lockscreen.ACTION_FORWARD = "FORWARD";
		
		Lockscreen.ACTION_HEADSET_SINGLE_CLICK = "HEADSET_SINGLE_CLICK";
		Lockscreen.ACTION_HEADSET_DOUBLE_CLICK = "HEADSET_DOUBLE_CLICK";
		Lockscreen.ACTION_HEADSET_TRIPPLE_CLICK = "HEADSET_TRIPPLE_CLICK";


		Lockscreen.prototype.setState = function(state) {
			cordova.exec(null, null, "Lockscreen", "setState", [state]);
		};

		Lockscreen.prototype.setMetadata = function(metadata) {
			cordova.exec(null, null, "Lockscreen", "setMetadata", [ metadata ]);
		};

		Lockscreen.prototype.setCurrentTime = function(currentTime) {
			cordova.exec(null, null, "Lockscreen", "setCurrentTime", [currentTime]);
		};

		Lockscreen.prototype.setSkipInterval = function(skipInterval) {
			cordova.exec(null, null, "Lockscreen", "setSkipInterval", [skipInterval]);
		};

		Lockscreen.prototype.release = function() {
			cordova.exec(null, null, "Lockscreen", "release", []);
		};

		Lockscreen.prototype.listenActions = function(onEventReceived) {

			cordova.exec(function(e){
				onEventReceived(e)
			}, null, 'Lockscreen',
					 'listenActions', []);
		};

		module.exports = Lockscreen;

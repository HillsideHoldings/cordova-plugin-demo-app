

	var Lockscreen = function() {
		cordova.exec(null, null, "Lockscreen", "init", []);
	};

	// Media states
	Lockscreen.STATE_PLAYING = "PLAYING";
	Lockscreen.STATE_PAUSED = "PAUSED";
	Lockscreen.STATE_STOPPED = "STOPPED";
	Lockscreen.ACTION_PLAY_PAUSE = "PLAY_PAUSE";
	Lockscreen.ACTION_REWIND = "REWIND";
	Lockscreen.ACTION_FORWARD = "FORWARD";
	

	Lockscreen.prototype.setState = function(state) {
		cordova.exec(null, null, "Lockscreen", "setState", [state]);
	};

	Lockscreen.prototype.setMetadata = function(metadata) {
		cordova.exec(null, null, "Lockscreen", "setMetadata", [ metadata ]);
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

	
	
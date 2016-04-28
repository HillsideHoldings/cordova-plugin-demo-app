cordova.define("cordova-lock-screen-media-controls.Lockscreen",
		function(require, exports, module) {

			//var Lockscreen
	
			module.exports = {
				setTitle : function(title) {
					cordova.exec(null, null, "Lockscreen", "setTitle",
							[ title ]);
				},

				setSubtitle : function(subtitle) {
					cordova.exec(null, null, "Lockscreen", "setSubtitle",
							[ subtitle ]);
				},

				setTrackLength : function(trackLength) {
					cordova.exec(null, null, "Lockscreen", "setTrackLength",
							[ trackLength ]);
				},

				setCurrentTime : function(currentTime) {
					cordova.exec(null, null, "Lockscreen", "setCurrentTime",
							[ currentTime ]);
				},

				setArtworkURL : function(artworkUrl) {
					cordova.exec(null, null, "Lockscreen", "setArtworkURL",
							[ artworkUrl ]);
				},

				setSkipInterval : function(skipInterval) {
					cordova.exec(null, null, "Lockscreen", "setSkipInterval",
							[ skipInterval ]);
				},

				registerActions : function(playAction, pauseAction,
						toggleAction, forwardAction, backwardAction) {
					cordova.exec(function(message) {
						switch (message) {
						case 1:
							playAction();
							break;
						case 2:
							pauseAction();
							break;
						case 3:
							toggleAction();
							break;
						case 4:
							backwardAction();
							break;
						case 5:
							forwardAction();
							break;
						}
					}, null, "Lockscreen", "registerActions", [ playAction,
							pauseAction, toggleAction, forwardAction,
							backwardAction ]);
				}

			};

			

		});

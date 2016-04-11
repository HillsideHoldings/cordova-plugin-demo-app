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
        console.log('Received Event: ' + id);
        document.getElementById("playbtn").addEventListener("click", playAudio);
        //initMedia();
    },
    
    
};

app.initialize();
var my_media = null;
var isPlaying = false;
var mediaTimer = null;

function initMedia(){
	var url = "http://www.songspk320z.us/songoftheday/[Songs.PK]%20Khaike%20Paan%20Banaraswala%20-%20Don%20(2006).mp3";
    // Play the audio file at url
    my_media = new Media(url,
        // success callback
        function () {
            console.log("playAudio():Audio Success");
        },
        // error callback
        function (err) {
            console.log("playAudio():Audio Error: " + err);
        }
    );
	
}

//Play audio
//
function playAudio() {
	if(my_media == null) initMedia();
	
	if(isPlaying){
		 // Stop audio
	    my_media.stop();
	    my_media.release();
	    //my_media = null;
	    isPlaying = false;
	    clearInterval(mediaTimer);
	    document.getElementById("playbtn").innerHTML = "Play";
	    document.getElementById("buff").innerHTML = "0%";
	    
	}else{
		 // Play audio
	    my_media.play();
	    isPlaying = true;
	    document.getElementById("playbtn").innerHTML = "Stop";
	    
	    clearInterval(mediaTimer);
	    // Update media position every second
		mediaTimer = setInterval(function () {
	    	updateBufferValue();
	    }, 1000);
	}
   
	
}

function updateBufferValue (){
	// get media position
    my_media.getBufferedPercent(
        // success callback
        function (per) {
            if (per > -1) {
                document.getElementById("buff").innerHTML = per+"%";
            }
            
            
            var p = eval(per);
            if(per >= 100){
            	clearInterval(mediaTimer);
            }
        },
        // error callback
        function (e) {
            console.log("Error getting buffered percentage = " + e);
            document.getElementById("buff").innerHTML ="Error getting buffered percentage.";
            clearInterval(mediaTimer);
        }
    );
}
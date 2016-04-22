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

        console.log('Received Event: ' + id);
        
        // Start playback
        var my_media = new Media('http://www.songspk320z.us/songoftheday/[Songs.PK]%20Khaike%20Paan%20Banaraswala%20-%20Don%20(2006).mp3',
                                 function () {
                                    console.log("playAudio():Audio Success");
                                 },
                                 // error callback
                                 function (err) {
                                    console.log("playAudio():Audio Error: " + err);
                                 }
                            );
        my_media.play();
        
        // you can change skip interval (in seconds)
        var skipInterval = 30;
        // this value will be shown on fwd/bckwd buttons labels
        Lockscreen.setSkipInterval(skipInterval);
        
        // set track info for lock screen
        Lockscreen.setTitle('Title');
        Lockscreen.setSubtitle('Subtitle');
        // track length is not available right away
        setTimeout(function() { Lockscreen.setTrackLength(my_media.getDuration()); }, 2000);
        Lockscreen.setCurrentTime(0);
        Lockscreen.setArtworkURL('http://www.songspk320z.us/img/globe.png');
    
        // here is call backs for user actions
        Lockscreen.registerActions(function() {
                                        my_media.play();
                                   },
                                   function() {
                                        my_media.pause();
                                   },
                                   function() {
                                        // TODO: toggle logic
                                        my_media.play();
                                   },
                                   function() {
                                        my_media.getCurrentPosition(function(p) {
                                                                        p += skipInterval;
                                                                        my_media.seekTo(1000*p);
                                                                        Lockscreen.setCurrentTime(p);
                                                                    }, null);
                                   },
                                   function() {
                                        my_media.getCurrentPosition(function(p) {
                                                                        p -= skipInterval;
                                                                        my_media.seekTo(1000*p);
                                                                        Lockscreen.setCurrentTime(p);
                                                                    }, null);
                                   });
        
    },

};

app.initialize();


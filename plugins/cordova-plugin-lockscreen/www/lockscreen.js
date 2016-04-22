module.exports = {
setTitle: function(title, successCallback) {
    cordova.exec(successCallback, null, "Lockscreen", "setTitle", [title]);
}
};
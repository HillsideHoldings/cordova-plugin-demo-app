cordova.define("cordova-plugin-lockscreen.Lockscreen", function(require, exports, module) {
module.exports = {
setTitle: function(title, successCallback) {
    cordova.exec(successCallback, null, "Lockscreen", "setTitle", [title]);
}
};
});

var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseInvites";

module.exports = {
    invite: function(config) {
        return new Promise(function(resolve, reject) {
            exec(resolve, reject, PLUGIN_NAME, "invite", [config]);
        });
    }
};

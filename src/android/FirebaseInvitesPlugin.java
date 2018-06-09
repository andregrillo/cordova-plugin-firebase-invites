package by.chemerisuk.cordova.firebase;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.appinvite.AppInviteInvitation;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin.ExecutionThread;

import java.util.Arrays;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.appinvite.AppInviteInvitation.IntentBuilder.PlatformMode.PROJECT_PLATFORM_IOS;


public class FirebaseInvitePlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FirebaseInvitePlugin";
    private static final int REQUEST_INVITE = 4343248;

    private String iosClientId;
    private CallbackContext invitateCallbackContext;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Invite plugin");

        iosClientId = preferences.getString("REVERSED_CLIENT_ID", "").trim();
    }

    @CordovaMethod(ExecutionThread.WORKER)
    protected void invite(JSONObject config, CallbackContext callbackContext) throws JSONException {
        this.invitateCallbackContext = callbackContext;

        String title = config.getString("title");
        String message = config.getString("message");
        AppInviteInvitation.IntentBuilder builder = new AppInviteInvitation.IntentBuilder(title)
            .setMessage(message)
            .setDeepLink(Uri.parse(config.getString("link")));
        if (config.has("emailAction")) {
            builder.setCallToActionText(config.getString("emailAction"));
        }
        if (config.has("emailImage")) {
            builder.setCustomImage(Uri.parse(config.getString("emailImage")));
        }
        if (config.has("emailSubject")) {
            builder.setEmailSubject(config.getString("emailSubject"));
        }
        if (config.has("emailContent")) {
            builder.setEmailHtmlContent(config.getString("emailContent"));
        }
        if (!iosClientId.isEmpty()) {
            builder.setOtherPlatformsTargetApplication(PROJECT_PLATFORM_IOS, iosClientId);
        }
        if (config.has("androidMinimumVersion")) {
            builder.setAndroidMinimumVersionCode(config.getInt("androidMinimumVersion"));
        }

        cordova.startActivityForResult(this, builder.build(), REQUEST_INVITE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (this.invitateCallbackContext != null && requestCode == REQUEST_INVITE) {
            JSONArray invitationIds = new JSONArray();
            if (resultCode == Activity.RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, intent);
                for (String id : ids) {
                    invitationIds.put(id);
                }
            }
            this.invitateCallbackContext.success(invitationIds);
        }
    }
}

package by.chemerisuk.cordova.firebase;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;

import com.google.android.gms.appinvite.AppInviteInvitation;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.appinvite.AppInviteInvitation.IntentBuilder.PlatformMode.PROJECT_PLATFORM_IOS;


public class FirebaseInvitesPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FirebaseInvitePlugin";
    private static final int REQUEST_INVITE = 4343248;

    private String iosClientId;
    private CallbackContext inviteCallbackContext;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Invites plugin");

        iosClientId = preferences.getString("REVERSED_CLIENT_ID", "").trim();

        // REVERSED_CLIENT_ID -> CLIENT_ID for iOS

        if (!iosClientId.isEmpty()) {
            List<String> parts = Arrays.asList(iosClientId.split("\\."));
            Collections.reverse(parts);
            iosClientId = TextUtils.join(".", parts);
        }
    }

    @CordovaMethod
    protected void invite(JSONObject config, CallbackContext callbackContext) throws JSONException {
        this.inviteCallbackContext = callbackContext;

        String title = config.getString("title");
        String message = config.getString("message");
        AppInviteInvitation.IntentBuilder builder = new AppInviteInvitation
            .IntentBuilder(title)
            .setMessage(message)
            .setDeepLink(Uri.parse(config.getString("deepLink")));
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

        if (this.inviteCallbackContext != null && requestCode == REQUEST_INVITE) {
            JSONArray invitationIds = new JSONArray();
            if (resultCode == Activity.RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, intent);
                for (String id : ids) {
                    invitationIds.put(id);
                }
            }
            this.inviteCallbackContext.success(invitationIds);
        }
    }
}

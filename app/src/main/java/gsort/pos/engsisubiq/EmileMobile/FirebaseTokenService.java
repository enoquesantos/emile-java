package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FirebaseTokenService extends FirebaseInstanceIdService implements IRequestListener, IUserProfileChangeListener {

    private UserProfile userProfile;
    private static String TAG = "FirebaseTokenService";
    private LocalStorage localStorage;

    public FirebaseTokenService() {
        localStorage = LocalStorage.getInstance();
        userProfile  = UserProfile.getInstance();
        userProfile.addUserProfileChangeListener(this);
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // save the token to application local storage
        localStorage.saveString("push_notification_token", refreshedToken);

        // submit the token to WebService
        sendRegistrationToServer(refreshedToken);
    }

    @Override
    public void onRequestError(int statusCode, String message, HashMap<String, String> headers) {
        Log.d(TAG, "Error on send the push notification token to WebService!");
    }

    @Override
    public void onRequestFinished() { }

    @Override
    public void onRequestOpened() { }

    @Override
    public void onRequestSuccess(int statusCode, Object response, HashMap<String, String> headers) {
        Log.d(TAG, "The push notification token was sent to WebService!");
        localStorage.saveString("push_notification_token", "");
    }

    @Override
    public void userProfileChanged(UserProfile userProfile) {
        String token = localStorage.getString("push_notification_token");
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        try {
            int userId = userProfile.getId();

            if (userId == 0 || token.equals(""))
                return;

            RequestHttp requestHttp = new RequestHttp(this);

            JSONObject postData = new JSONObject();
            postData.put("push_notification_token", token);
            postData.put("id", userId);

            requestHttp.post("/token_register/", postData);
            Log.d(TAG, "Sending token to WebService...");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
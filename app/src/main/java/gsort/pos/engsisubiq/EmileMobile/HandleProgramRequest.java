package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;
import org.json.JSONObject;
import java.util.HashMap;

class HandleProgramRequest implements IRequestListener {

    private UserProfile userProfile;

    public HandleProgramRequest(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @Override
    public void onRequestError(int statusCode, String message, HashMap<String, String> headers) {
        Log.d("HandleProgramRequest", "Request error!");
        Log.d("HandleProgramRequest", "statusCode: " + statusCode);
        Log.d("HandleProgramRequest", "message: " + message);
    }

    @Override
    public void onRequestFinished() { }

    @Override
    public void onRequestOpened() { }

    @Override
    public void onRequestSuccess(int statusCode, Object response, HashMap<String, String> headers) {
        Log.d("HandleProgramRequest", "RequestSuccess with statusCode: " + statusCode);
        if (statusCode == 200 && response != null) {
            try {
                JSONObject responseObject = (JSONObject) response;

                if (responseObject.has("id"))
                    userProfile.setProgram(responseObject);
            } catch (Exception e) {
                Log.e("HandleProgramRequest", "error on parse program json object!", e);
            }
        }
    }
}

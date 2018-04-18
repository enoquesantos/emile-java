package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserExtraData {

    private int userId;
    private static String TAG = "LoadUserProfileExtraFields";
    private RequestHttp requestHttp1;
    private RequestHttp requestHttp2;

    public UserExtraData(UserProfile userProfile) {
        this.userId = userProfile.getId();

        HandleProgramRequest handleProgramRequest = new HandleProgramRequest(userProfile);
        HandleCourseSectionsRequest handleCourseSectionsRequest = new HandleCourseSectionsRequest(userProfile);

        requestHttp1 = new RequestHttp(handleProgramRequest);
        requestHttp2 = new RequestHttp(handleCourseSectionsRequest);
    }

    public void loadProgram() {
        if (userId > 0) {
            requestHttp1.get("program/" + userId + "/");
            Log.d(TAG, "loading user program2...");
        }
    }

    public void loadCourseSections() {
        if (userId > 0) {
            JSONObject json = new JSONObject();

            try {
                json.put("id", userId);
                requestHttp2.post("course_sections/", json);
                Log.d(TAG, "loading user course sections2...");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

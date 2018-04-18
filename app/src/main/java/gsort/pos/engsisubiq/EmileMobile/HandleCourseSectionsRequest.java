package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;

import org.json.JSONArray;

import java.util.HashMap;

class HandleCourseSectionsRequest implements IRequestListener {

    private UserProfile userProfile;

    public HandleCourseSectionsRequest(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @Override
    public void onRequestError(int statusCode, String message, HashMap<String, String> headers) {
        Log.d("HandleCourseS...Request", "Request error!");
        Log.d("HandleCourseS...Request", "statusCode: " + statusCode);
        Log.d("HandleCourseS...Request", "message: " + message);
    }

    @Override
    public void onRequestFinished() { }

    @Override
    public void onRequestOpened() { }

    @Override
    public void onRequestSuccess(int statusCode, Object response, HashMap<String, String> headers) {
        Log.d("HandleCourseS...Request", "RequestSuccess with statusCode: " + statusCode);
        if (statusCode == 200 && response != null) {
            try {
                JSONArray responseArray = (JSONArray) response;

                if (responseArray.length() > 0)
                    userProfile.setCourseSections(responseArray);
            } catch (Exception e) {
                Log.e("HandleCourseS...Request", "error on parse course sections array!", e);
            }
        }
    }
}

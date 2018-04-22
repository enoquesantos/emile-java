package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;
import org.json.JSONObject;
import java.util.HashMap;

public class WriteMessageRequestHandle implements IRequestListener {

    private RequestHttp requestHttp;
    private IRequestHttpFragment iFragment;
    private static String classTag = "WriteMessageRequestHandle";

    WriteMessageRequestHandle(IRequestHttpFragment fragment) {
        requestHttp = new RequestHttp(this);
        iFragment   = fragment;
    }

    /**
     * Open message request with GET HTTP method
     * @param userId int
     */
    public void request(String path, JSONObject jsonObject) {
        requestHttp.post(path, jsonObject);
    }

    @Override
    public void onRequestError(int statusCode, String message, HashMap<String, String> headers) {
        iFragment.requestError(statusCode, message, headers);
    }

    @Override
    public void onRequestFinished() {
        iFragment.requestFinished();
    }

    @Override
    public void onRequestOpened() {
        iFragment.requestOpened();
    }

    @Override
    public void onRequestSuccess(int statusCode, Object response, HashMap<String, String> headers) {
        try {
            JSONObject result = (JSONObject) response;
            iFragment.requestSuccess(statusCode, result, headers);
        } catch (Exception e) {
            Log.e(classTag, e.getMessage());
        }
    }
}

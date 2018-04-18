package gsort.pos.engsisubiq.EmileMobile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class HandleWriteMessageRequest implements IRequestListener {

    private RequestHttp requestHttp;
    private IRequestHttpFragment ifragment;

    public HandleWriteMessageRequest(IRequestHttpFragment fragment) {
        ifragment = fragment;
        requestHttp = new RequestHttp(this);
    }

    public void submit(JSONObject postData) {
        try {
            requestHttp.post(postData.getString("urlService/"), postData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestError(int statusCode, String message, HashMap<String, String> headers) {

    }

    @Override
    public void onRequestFinished() {

    }

    @Override
    public void onRequestOpened() {

    }

    @Override
    public void onRequestSuccess(int statusCode, Object response, HashMap<String, String> headers) {

    }
}

package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MessagesRequestHandle implements IRequestListener {

    private LocalStorage localStorage;
    private RequestHttp requestHttp;
    private IRequestHttpFragment iFragment;
    private static ArrayList<MessageBean> messages;
    private static String classTag = "MessagesRequestHandle";

    MessagesRequestHandle(IRequestHttpFragment fragment) {
        localStorage        = LocalStorage.getInstance();
        messages            = new ArrayList<>();
        requestHttp         = new RequestHttp(this);
        iFragment           = fragment;
    }

    /**
     * Open message request with GET HTTP method
     * @param userId int
     */
    public void request(int userId) {
        String path = "messages/" + userId;
        requestHttp.get(path);
    }

    public void loadFromLocalStorage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                iFragment.requestOpened();
                if (messages.size() > 0) {
                    Log.d(classTag, "Local messages already loaded!!");
                    iFragment.setViewData(messages);
                } else {
                    String localMessages = localStorage.getString("local_messages");

                    if (!localMessages.equals("")) {
                        try {
                            JSONObject json = new JSONObject(localMessages);
                            loadFromArray(json.getJSONArray("results"));
                            if (messages.size() > 0) iFragment.setViewData(messages);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(classTag, "Local messages is empty!");
                    }
                }
                iFragment.requestFinished();
            }
        }).start();
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
            if (result.length() > 0) {
                loadFromArray(result.getJSONArray("results"));
                saveMessages(result);
                if (messages.size() > 0) iFragment.setViewData(messages);
            } else {
                Log.d(classTag, "message count is zero!!!");
            }
        } catch (Exception e) {
            Log.e(classTag, e.getMessage());
        }
    }

    private void saveMessages(final JSONObject json) {
        localStorage.saveString("local_messages", json.toString());
    }

    private void loadFromArray(JSONArray jsonArray) {
        if (jsonArray != null && jsonArray.length() > 0) {
            int i = 0;
            int size = jsonArray.length();
            while(i < size) {
                try {
                    messages.add(new MessageBean(jsonArray.getJSONObject(i++)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.i(classTag, "json is null!!");
        }
    }
}

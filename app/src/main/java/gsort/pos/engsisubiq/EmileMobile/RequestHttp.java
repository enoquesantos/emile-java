package gsort.pos.engsisubiq.EmileMobile;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.*;

public class RequestHttp {

    private static class RestService {
        static final String baseUrl     = "http://enoque.pythonanywhere.com/";
        static final String tkHeaderKey = "Authorization";
        static final String tkHeaderVal = "Basic " + RequestHttp.getBasicAuthToken("apirest", "1q2w3e4r5t");;
    }

    private IRequestListener listener;
    private OkHttpClient client;
    private static MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
    private static MediaType uploadMediaType = MediaType.parse("multipart/form-data; charset=utf-8");

    public RequestHttp(IRequestListener requestListener) {
        listener = requestListener;
        client = new OkHttpClient();
    }

    public static String getBasicAuthToken(String username, String password) {
        return Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    public void post(String path, JSONObject json) {
        post(path, json, null);
    }

    public void post(String path, JSONObject json, HashMap<String, String> headers) {
        runInBackground(path, true, json.toString(), headers);
    }

    public void get(String path) {
        get(path, null);
    }

    public void get(String path, HashMap<String, String> headers) {
        runInBackground(path, false, null, headers);
    }

    /**
     * Try parse a json string to JSONObject or JSONArray.
     * If the strJson parameter is not a valid json, return the strJson
     * @param strJson String
     * @return Object
     */
    private static Object toJSON(String strJson) {
        Object jsonObject;

        try {
            jsonObject = new JSONObject(strJson);
        } catch (JSONException ex) {
            // e.g. in case JSONArray is valid as well...
            try {
                jsonObject = new JSONArray(strJson);
            } catch (JSONException ex1) {
                return strJson;
            }
        }

        return jsonObject;
    }

    private void runInBackground(String path, boolean isPost, String postData, HashMap<String, String> headers) {
        Request.Builder builder = new Request.Builder();
        builder.url(path.contains("http") ? path : RestService.baseUrl + path);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet())
                builder.addHeader(entry.getKey(), entry.getValue());
        } else {
            builder.addHeader(RestService.tkHeaderKey, RestService.tkHeaderVal);
        }

        Log.d("RequestHttp post", "path: " + path);
        if (isPost) {
            RequestBody body = RequestBody.create(jsonMediaType, postData);
            builder.post(body);
            Log.d("RequestHttp post", "post data: " + postData);
        } else {
            builder.get();
        }

        Request request = builder.build();

        listener.onRequestOpened();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("RequestHttp failure", "onFailure...");
                listener.onRequestFinished();

                String message = e.getMessage();
                if (message == null || message.equals("")) message = "Unknown request error!";

                Log.w("RequestHttp failure", message);
                listener.onRequestError(0, message, null);
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d("RequestHttp", "onResponse...");
                listener.onRequestFinished();

                int statusCode = response.code();
                String responseStr = response.body().string();
                HashMap<String, String> responseHeaders = new HashMap<>();

                Log.d("RequestHttp", "statusCode: " + statusCode);
                Log.d("RequestHttp", "responseMessage: " + responseStr);
                Log.d("RequestHttp", "request url: " + call.request().url().url());

                if (response.isSuccessful()) {
                    try {
                        okhttp3.Headers headers = response.headers();

                        int i = 0, j = headers.size();
                        while (i < j) {
                            responseHeaders.put(headers.name(i), headers.value(i));
                            i++;
                        }

                        Log.d("RequestHttp", "call listener.onRequestSuccess now...");
                        listener.onRequestSuccess(statusCode, toJSON(responseStr), responseHeaders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // listener.onRequestError(statusCode, responseMessage, responseHeaders);
                    }
                } else {
                    listener.onRequestError(statusCode, responseStr, responseHeaders);
                }
            }
        });
    }
}

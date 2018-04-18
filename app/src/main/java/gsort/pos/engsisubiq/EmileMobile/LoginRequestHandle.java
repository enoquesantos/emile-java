package gsort.pos.engsisubiq.EmileMobile;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class LoginRequestHandle implements IRequestListener  {

    private RequestHttp requestHttp;
    private LocalStorage localStorage;
    private IRequestHttpFragment iFragment;

    LoginRequestHandle(IRequestHttpFragment fragment) {
        iFragment    = fragment;
        localStorage = LocalStorage.getInstance();
        requestHttp  = new RequestHttp(this);
    }

    public void post(final String login, final String password) {
        String path = "login/";
        JSONObject json = new JSONObject();

        try {
            json.put("login", login);
            json.put("password", password);
            requestHttp.post(path, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            JSONObject json = new JSONObject(response.toString());

            if (statusCode == 200 && json.has("user")) {

                UserProfile userProfile = UserProfile.getInstance();
                userProfile.initialize(json);

                if (userProfile.isLoggedIn()) {
                    userProfile.saveInLocalStorage();
                    iFragment.requestSuccess(statusCode, "Login efetuado com sucesso!", headers);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

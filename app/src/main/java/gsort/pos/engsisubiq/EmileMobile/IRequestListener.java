package gsort.pos.engsisubiq.EmileMobile;

import java.util.HashMap;

/**
 * Created by enoque on 26/03/18.
 */

public interface IRequestListener {
    void onRequestError(int statusCode, String message, HashMap<String, String> headers);
    void onRequestFinished();
    void onRequestOpened();
    void onRequestSuccess(int statusCode, Object response, HashMap<String, String> headers);
}

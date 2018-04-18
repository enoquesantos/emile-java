package gsort.pos.engsisubiq.EmileMobile;

import java.util.Map;
import org.json.JSONObject;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.FirebaseMessagingService;

public class PushMessageService extends FirebaseMessagingService {

    private static String TAG = "PushMessageService";

    public PushMessageService() { }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String,String> extraData = remoteMessage.getData();

        // whe app is in background or closed, the notification object
        // is null, because all push data become in remoteMessage.getData()
        if (notification != null) {
            String title        = notification.getTitle();
            String message      = notification.getBody();
            String messageData  = "";

            Log.i(TAG, "new push message!");

            if (message == null)
                message = extraData.get("body");

            // this JSONObject is needed to serialize the Map as string
            // to pass to QtApplication as argument when user click in notification
            JSONObject json = null;
            try {
                json = new JSONObject(extraData);
                messageData = json.toString();
            } catch(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            // show system notification
            Notifications sn = Notifications.getInstance();
            sn.notify(title, message, messageData);
        }
    }
}

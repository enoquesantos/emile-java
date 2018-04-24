package gsort.pos.engsisubiq.EmileMobile;

import java.util.Map;
import org.json.JSONObject;
import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.FirebaseMessagingService;

// https://stackoverflow.com/questions/37711082/how-to-handle-notification-when-app-in-background-in-firebase/37845174#37845174
public class PushMessageService extends FirebaseMessagingService {

    private static String TAG = "PushMessageService";

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
            sn.initialize(this);
            sn.notify(title, message, messageData);

            notifyApplicationReceivers(json);
        }
    }

    private void notifyApplicationReceivers(JSONObject json) {
        Intent intent = new Intent("push_message");
        intent.putExtra("message_data", json.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
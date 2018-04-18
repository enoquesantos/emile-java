package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class MessageBean {

    private int id;
    private String date;
    private String message;
    private String sender;
    private int senderId;
    private String time;
    private String title;

    MessageBean(JSONObject objc) {
        String dateTime;
        JSONObject senderObjc;
        try {
            id          = objc.getInt("id");
            title       = objc.getString("title");
            message     = objc.getString("message");
            dateTime    = objc.getString("date");
            senderObjc  = objc.getJSONObject("sender");
            senderId    = senderObjc.getInt("id");
            sender      = senderObjc.getString("name");
            date        = formattedDateFromString("yyyy-MM-dd", "dd/MM/yyyy", dateTime);
            time        = dateTime.substring(11, 19);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    private static String formattedDateFromString(String inputFormat, String outputFormat, String inputDate){
        if (inputFormat.equals("")) // if inputFormat = "", set a default input format.
            inputFormat = "yyyy-MM-dd hh:mm:ss";

        if(outputFormat.equals("")) // if inputFormat = "", set a default output format.
            outputFormat = "EEEE d 'de' MMMM 'del' yyyy";

        Date parsed;
        String outputDate = "";
        Locale locale = java.util.Locale.getDefault();

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, locale);
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, locale);

        // You can set a different Locale, This example set a locale of Country Mexico.
        // SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, new Locale("es", "MX"));
        // SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, new Locale("es", "MX"));

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);
        } catch (Exception e) {
            Log.e("MessageBean", "Exception in formateDateFromstring(): " + e.getMessage());
        }

        return outputDate;
    }
}

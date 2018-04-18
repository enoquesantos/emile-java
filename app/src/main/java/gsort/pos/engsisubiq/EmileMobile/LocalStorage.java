package gsort.pos.engsisubiq.EmileMobile;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {

    private static LocalStorage instance;
    private static SharedPreferences sharedPreferences;

    private LocalStorage() { }

    public static LocalStorage getInstance() {
        if (instance == null)
            instance = new LocalStorage();
        return instance;
    }

    public void initialize(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public void saveBool(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    public void saveString(final String key, final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
                prefsEditor.putString(key, value);
                prefsEditor.apply();
            }
        }).start();
    }

    public boolean getBool(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }
}

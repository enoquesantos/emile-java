package gsort.pos.engsisubiq.EmileMobile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserProfile {

    private         int             id;
    private         String          email;
    private         String          name;
    private         String          permissionName;
    private         String          shortUsername;
    private         JSONArray       courseSections;
    private         JSONArray       messageDestinations;
    private         JSONObject      profile;
    private         JSONObject      program;
    private         boolean         _isLoggedIn;
    private         boolean         canSendMessages;
    private         UserExtraData   userExtraData;
    private         MainActivity    activity;
    private static  UserProfile     instance;
    private static  LocalStorage    localStorage;
    private static  String          TAG;
    private ArrayList<IUserProfileChangeListener> listeners;

    /**
     * private construct : the class is a Singleton
     */
    private UserProfile() {
        id                  = 0;
        email               = "";
        name                = "";
        permissionName      = "";
        shortUsername       = "";
        _isLoggedIn         = false;
        canSendMessages     = false;
        localStorage        = LocalStorage.getInstance();
        profile             = new JSONObject();
        program             = new JSONObject();
        courseSections      = new JSONArray();
        messageDestinations = new JSONArray();
        TAG                 = "UserProfile";
        listeners           = new ArrayList<>();
    }

    /**
     * get the instance for UserProfile
     * @return UserProfile
     */
    public static UserProfile getInstance() {
        if (instance == null)
            instance = new UserProfile();
        return instance;
    }

    public void addUserProfileChangeListener(IUserProfileChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Starts the user profile using a json object
     * and set in all object properties (for available properties)
     * @param data JSONObject
     */
    public void initialize(JSONObject data) {
        if (data == null || !data.has("user"))
            return;

        try {
            profile = data.getJSONObject("user");

            id      = profile.getInt("id");
            email   = profile.getString("email");
            name    = profile.getString("name");

            if (!name.equals("")) {
                String[] names = name.split(" ");
                shortUsername = activity.capitalizeString(names[0]) + " " + activity.capitalizeString(names[names.length-1]);
            }

            // when student, the json (after login) already contains the program object!
            if (data.has("program"))
                program = data.getJSONObject("program");

            JSONObject permission   = profile.getJSONObject("permission");
            permissionName          = permission.getString("description");
            canSendMessages         = !permissionName.equals("student");

            if (id > 0 && !email.equals("")) {
                profile         = data;
                _isLoggedIn     = true;
                userExtraData   = new UserExtraData(this);
                loadExtraFields();
                localStorage.saveBool("is_user_logged_in", true);
            }

            activity.setNavViewUserInformation(this);
            notifyListeners();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveInLocalStorage() {
        localStorage.saveString("user_profile_data", profile.toString());
    }

    public void loadFromLocalStorage() {
        String data = localStorage.getString("user_profile_data");
        if (data == null || data.length() == 0 || !localStorage.getBool("is_user_logged_in"))
            return;

        try {
            String strProgram = localStorage.getString("user_profile_program");
            if (strProgram.length() > 0)
                program = new JSONObject(strProgram);

            // when student, the json (after login) already contains the courseSections!
            // for teacher or coordinator, another request is needed to load the course sections
            String strCourseSections = localStorage.getString("user_profile_course_sections");
            if (strCourseSections.length() > 0)
                courseSections = new JSONArray(strCourseSections);

            String strMessageDestinations = localStorage.getString("user_profile_message_destinations");
            if (strMessageDestinations.length() > 0)
                messageDestinations = new JSONArray(strMessageDestinations);

            initialize(new JSONObject(data));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return _isLoggedIn;
    }

    public void logout() {
        localStorage.saveBool("is_user_logged_in", false);
    }

    /**
     * Get for user profile field if available, returning the request property value or
     * return a null value when 'property' is not available in current user profile
     * @param property String
     * @return Object
     */
    public Object getInfo(String property) {
        if (profile.has(property)) {
            try {
                return profile.get(property);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getShortUsername() {
        return shortUsername;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public JSONObject getProgram() {
        return program;
    }

    public JSONArray getMessageDestinations() {
        return messageDestinations;
    }

    public JSONArray getCourseSections() {
        return courseSections;
    }

    public boolean canSendMessages() {
        return this.canSendMessages;
    }

    public void setCourseSections(JSONArray json) {
        courseSections = json;
        localStorage.saveString("user_profile_course_sections", courseSections.toString());

        try {
            addTeacherMessagesDestinations();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setProgram(JSONObject json) {
        program = json;
        localStorage.saveString("user_profile_program", program.toString());

        try {
            addCoordinatorMessagesDestinations();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    private void addTeacherMessagesDestinations() throws JSONException {
        int i = 0, length = courseSections.length();

        JSONObject option = new JSONObject();
        option.put("sender", id);
        option.put("name", "");
        option.put("post_data_key", "");
        option.put("post_data_val", 0);
        option.put("title", "Todos os meus alunos (todas as turmas)");
        option.put("url_service", "sendMessageToStudentsOfATeacher");
        messageDestinations.put(option);

        while (i < length) {
            JSONObject section = courseSections.getJSONObject(i++);

            option = new JSONObject();
            option.put("sender", id);
            option.put("name", section.getString("name"));
            option.put("post_data_key", "course_section_id");
            option.put("post_data_val", section.getInt("id"));
            option.put("title", "Todos os alunos de " + getPrettySectionName(section.getString("name")));
            option.put("url_service", "sendMessageToStudentsOfACourseSection");

            messageDestinations.put(option);
        }

        Log.i(TAG, "addTeacherMessagesDestinations.messageDestinations:");
        Log.i(TAG, messageDestinations.toString());
        localStorage.saveString("user_profile_message_destinations", messageDestinations.toString());
    }

    private void addCoordinatorMessagesDestinations() throws JSONException {
        // if the user profile is coordinator,
        // add the follow options:
        //  1. send message to all students of the program
        //  2. send message to all teachers of the program
        if (permissionName.equalsIgnoreCase("coordinator")) {
            String programId   = String.valueOf(program.getInt("id"));
            String programAbbr = program.getString("abbreviation");
            String programName = program.getString("name");

            // add option to notify all teachers
            JSONObject option2 = new JSONObject();
            option2.put("sender", id);
            option2.put("name", programName);
            option2.put("post_data_key", "program_id");
            option2.put("post_data_val", programId);
            option2.put("title", "Todos os professores de " + programAbbr);
            option2.put("url_service", "sendMessagesToTeachersOfAProgram");
            messageDestinations.put(option2);

            // add option to notify all students
            JSONObject option3 = new JSONObject();
            option3.put("sender", id);
            option3.put("name", programName);
            option3.put("post_data_key", "program_id");
            option3.put("post_data_val", programId);
            option3.put("title", "Todos os alunos do curso " + programAbbr);
            option3.put("url_service", "sendMessagesToStudentsOfAProgram");
            messageDestinations.put(option3);
        }

        Log.i(TAG, "addCoordinatorMessagesDestinations.messageDestinations:");
        Log.i(TAG, messageDestinations.toString());
        localStorage.saveString("user_profile_message_destinations", messageDestinations.toString());
    }

    private void loadExtraFields() {
        if (courseSections.length() == 0)
            userExtraData.loadCourseSections();

        if (program.length() == 0)
            userExtraData.loadProgram();
    }

    private void notifyListeners() {
        final UserProfile self = instance;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (IUserProfileChangeListener l : listeners)
                        l.userProfileChanged(self);
                } catch (Exception e) {
                    // Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }).start();
    }

    private static String getPrettySectionName(String section) {
        String[] names = section.split("-");
        int length = names.length;
        if (length > 0) {
            String name = names[0];
            if (length > 1) name += " " + names[length-1];
            return name;
        }
        return section;
    }
}
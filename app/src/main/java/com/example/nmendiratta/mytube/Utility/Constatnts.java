package com.example.nmendiratta.mytube.Utility;

import android.util.Log;

public class Constatnts {

    public static final String GET_CODE_URL = "https://accounts.google.com/o/oauth2/auth";
    public static final String CLIENT_ID = "903211392493-avd6qjioo2en1iddn4ju5glu4leap0dc.apps.googleusercontent.com";
    public static final String REDIRECT_URI = "http%3A%2F%2Flocalhost%2Foauth2callback";
    public static final String RESPONSE_TYPE = "code";
    public static final String SCOPE = "https://www.googleapis.com/auth/youtube";
    public static final String ACCESS_TYPE = "offline";
    public static final String CLIENT_SECRET = "";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String ACCESS_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    public static final String RECEIVED_CODE = "4/kyz8bj2_Hdf5DAcOE1V2KyZQfrR-eNURZQ3PyZ4nhHI#";

    public static final String INITIAL_SEARCH_WORD = "Build Android Application";

    public static String requestAccessUrl(){
        String url = GET_CODE_URL+"?client_id="+CLIENT_ID+"&redirect_uri="+REDIRECT_URI+"&scope="+SCOPE+"&response_type="+RESPONSE_TYPE+"&access_type="+ACCESS_TYPE;

        Log.d("MYTUBE", "URL>>>>"+url);
        return  url;
    }

    public static String requestTokenParameters(){
        String params = "code="+RECEIVED_CODE+"&client_id="+CLIENT_ID+"&client_secret="+CLIENT_SECRET+"&redirect_uri="+REDIRECT_URI+"&grant_type="+GRANT_TYPE;
        Log.d("MYTUBE", "ACCESS TOKEN URL>>>>"+params);
        return params;
    }

}

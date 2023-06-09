package de.nulide.findmydevice.net;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.io.IO;
import de.nulide.findmydevice.data.io.JSONFactory;
import de.nulide.findmydevice.data.io.json.JSONMap;
import de.nulide.findmydevice.utils.PatchedVolley;

public class DataHandler {

    private static final String GET_AT = "/requestAccess";
    public static final String COMMAND = "/command";
    public static final String  LOCATION = "/location";
    public static final String PICTURE = "/picture";
    public static final String DEVICE = "/device";
    public static final String PUSH = "/push";

    public static final int DEFAULT_METHOD = Request.Method.PUT;
    public static final int DEFAULT_RESP_METHOD = Request.Method.POST;


    private Context context;
    private Settings settings;
    private String url;
    private RequestQueue queue;

    private JsonObjectRequest request;
    private RespHandler respHandler;

    public DataHandler(Context context) {
        this.context = context;
        IO.context = context;
        settings = JSONFactory.convertJSONSettings(IO.read(JSONMap.class, IO.settingsFileName));
        url = (String)settings.get(Settings.SET_FMDSERVER_URL);
        queue = PatchedVolley.newRequestQueue(context);
    }

    public void run(String com, RespListener listener){
        run(com, getEmptyDataReq(), listener);
    }

    public void run(String com, JSONObject object, RespListener listener){
        prepareWithAT(com, object, listener);
        send();
    }

    public void prepareWithAT(String com, JSONObject object, RespListener listener){
        prepareWithAT(DEFAULT_METHOD, DEFAULT_RESP_METHOD, com, getDefaultATReq(), object, listener);
    }

    public void prepareWithAT(int method, int respMethod, String com, JSONObject req, JSONObject object, RespListener listener){
        respHandler = new RespHandler(this, context, object, respMethod, com, listener);
        prepareSingle(method, GET_AT, req, respHandler);
    }

    public void prepareSingle(int method, String com, JSONObject req, RespHandler respHandler){
        this.respHandler = respHandler;
        request = new JsonObjectRequest(method, url + com,
                req, respHandler,
                Throwable::printStackTrace) {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return req.toString().getBytes(StandardCharsets.UTF_8);
            }
        };
    }

    public RespHandler getRespHandler(){
        return respHandler;
    }

    public void send(){
        queue.add(request);
    }

    public JSONObject getDefaultATReq(){
        JSONObject requestAccessObject = new JSONObject();
        try {
            requestAccessObject.put("IDT", settings.get(Settings.SET_FMDSERVER_ID));
            requestAccessObject.put("Data", settings.get(Settings.SET_FMD_CRYPT_HPW));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestAccessObject;
    }

    public JSONObject getEmptyDataReq(){
        JSONObject requestDataObject = new JSONObject();
        try {
            requestDataObject.put("IDT", "");
            requestDataObject.put("Data", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestDataObject;
    }

}

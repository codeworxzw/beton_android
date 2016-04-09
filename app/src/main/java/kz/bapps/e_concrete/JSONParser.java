package kz.bapps.e_concrete;

/**
 * Created by user on 04.06.15.
 *
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class JSONParser {

//    final static public String USER_AGENT = "EConcrete/1.0";
    final static public String HOST = "dev.beton.bapps.kz";//"192.168.0.180";//
    final static public String PORT = "9999";
    final static public String URL_ROOT = "http://" + HOST + ":" + PORT + "/";

    final static public String METHOD_POST = "POST";
    final static public String METHOD_GET = "GET";
    final static public String METHOD_HEAD = "HEAD";
    final static public String METHOD_PUT = "PUT";
    final static public String METHOD_DELETE = "DELETE";


    final static private String CRLF = "\r\n";
    final static private String TWO_HYPHENS = "--";

    public static CookieManager cookieManager;
    private String json = "";
    private Boolean auth = false;
    private String resource = null;
    private String method = METHOD_GET;
    private ContentValues params = new ContentValues();
    private Context context;
    private SharedPreferences prefs;
    private Map<String,File> files;
    private String boundary =  "----SLhLSENiESJNFLSNf";
    private String queryString = "";
    private JSONObject jsonDataObject;
    private JSONArray jsonDataArray;

    private Map<String,List<String>> headers = new HashMap<>();

    // constructor
    // function get json from resource
    // by making HTTP POST or GET method

    public JSONParser(Context context) {

        this.context = context;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
        }

        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        files = new HashMap<>();

        // Настройки приложения
        prefs = context.getSharedPreferences(EConcrete.appName, Context.MODE_PRIVATE);
    }

    public Boolean execute() {

        HttpURLConnection conn = null;

        try
        {
            //
            Set<Map.Entry<String, Object>> sets = this.getParams().valueSet();
            Iterator itr = sets.iterator();

            while (itr.hasNext()) {
                Map.Entry me = (Map.Entry) itr.next();
                String key = URLEncoder.encode(me.getKey().toString(), Charset.forName("UTF-8").name());
                Object value = URLEncoder.encode((String) me.getValue(), Charset.forName("UTF-8").name());
                queryString += key + "=" + value;
                if (itr.hasNext()) queryString += "&";
            }

            URL url;
            if (this.getMethod().equals(METHOD_GET) && !queryString.isEmpty()) {
                url = new URL(URL_ROOT + this.getResource() + "?" + queryString);
                Log.d("JSON Resource",URL_ROOT + this.getResource() + "?" + queryString);
            } else {
                url = new URL(URL_ROOT + this.getResource());
                Log.d("JSON Resource",URL_ROOT + this.getResource());
            }

            conn = (HttpURLConnection) url.openConnection();

            if(files.isEmpty()) {
                send(conn);
            } else {
                sendMultipart(conn);
            }

            receive(conn);

        } catch (ProtocolException e) {
            e.printStackTrace();
            Log.e("JSON ERROR","Ошибка ProtocolException");
            return Boolean.FALSE;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("JSON ERROR", "Ошибка MalformedURLException");
            return Boolean.FALSE;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("JSON ERROR", "Ошибка IOException");
            return Boolean.FALSE;
        } finally {
            if(conn != null) conn.disconnect();
        }

        return Boolean.TRUE;
    }


    /**
     * ОТПРАВКА ЗАПРОСА
     * @param conn HttpUrlConnection
     * @throws IOException
     */
    public void send(HttpURLConnection conn) throws IOException {

        if(!this.getMethod().equals(METHOD_POST) &&
                !this.getMethod().equals(METHOD_GET)) {
            conn.setRequestMethod(this.getMethod());
        }

        byte[] queryBytes = queryString.getBytes(Charset.forName("UTF-8"));

        if(this.getMethod().equals(METHOD_POST) || this.getMethod().equals(METHOD_PUT)) {
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset="
                    + Charset.forName("UTF-8").name());
            conn.setRequestProperty("Content-Length", Integer.toString(queryBytes.length));
        } else {
            conn.setRequestProperty("Content-Type", "plain/text");
        }

        conn.setInstanceFollowRedirects(false);

        conn.setRequestProperty("Host", HOST + ":" + PORT);
//        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setRequestProperty("X-CSRF-TOKEN", prefs.getString("_token", ""));
//        conn.setRequestProperty("Connection", "keep-alive");
//        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("Accept-Charset", Charset.forName("UTF-8").name());
        conn.setRequestProperty("Cookie", cookieManager.getCookie(URL_ROOT));
        conn.setRequestProperty("Cache-Control", "no-cache");

        if(this.getJsonDataArray() != null) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        }

        for(Map.Entry<String, List<String>> entry: headers.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            for(String value : values) {
                conn.setRequestProperty(key, value);
            }
        }

        if (this.getMethod().equals(METHOD_POST) ||
                this.getMethod().equals(METHOD_PUT)) {

            if(this.getJsonDataObject() != null) {
                OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
                wr.write(this.getJsonDataObject().toString());
                wr.close();
            } else if(this.getJsonDataArray() != null) {
                OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
                wr.write(this.getJsonDataArray().toString());
                wr.close();
            } else {
                DataOutputStream request = new DataOutputStream(conn.getOutputStream());
                request.writeBytes(queryString);
                request.close();
            }
        } else {
            conn.connect();
        }

    }

    /**
     * ОТПРАВКА МУЛЬТИПАРТ ФОРМЫ
     * @param conn HttpUrlConnection
     * @throws IOException
     */
    public void sendMultipart(HttpURLConnection conn) throws IOException {

        DataOutputStream outputStream;

        generateBoundary();

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        conn.setRequestMethod(METHOD_POST);
        conn.setRequestProperty("Host", HOST + ":" + PORT);
//        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setRequestProperty("X-CSRF-TOKEN", prefs.getString("_token", ""));
        conn.setRequestProperty("Accept-Charset", Charset.forName("UTF-8").name());
        conn.setRequestProperty("Cookie", cookieManager.getCookie(URL_ROOT));

        if(!this.getMethod().equals(METHOD_POST)) {
            this.getParams().put("_method",this.getMethod());
        }

        outputStream = new DataOutputStream(conn.getOutputStream());

        for(Map.Entry<String, File> entry : files.entrySet()) {
            outputStream.writeBytes(TWO_HYPHENS + boundary + CRLF);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() +
                    "\"; filename=\"" + entry.getValue().getName() + "\"" + CRLF);
            outputStream.writeBytes("Content-Type: application/octet-stream" + CRLF);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + CRLF);
            outputStream.writeBytes(CRLF);

            InputStream is = new FileInputStream(entry.getValue());
            byte[] data = new byte[1024];
            int blocks;
            while ((blocks = is.read(data, 0, data.length)) != -1)
            {
                outputStream.write(data, 0, blocks);
            }

            outputStream.writeBytes(CRLF);
            outputStream.flush();
        }

        Set<Map.Entry<String, Object>> params = this.getParams().valueSet();

        for (Map.Entry<String, Object> entry : params) {
            outputStream.writeBytes(TWO_HYPHENS + boundary + CRLF);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF);
            outputStream.writeBytes("Content-Type: text/plain" + CRLF);
            outputStream.writeBytes(CRLF);
            outputStream.writeBytes((String)entry.getValue());
            outputStream.writeBytes(CRLF);
            outputStream.flush();
        }

        outputStream.writeBytes(TWO_HYPHENS + boundary + TWO_HYPHENS + CRLF);

        outputStream.close();
    }

    /**
     * ПОЛУЧЕНИЕ ОТВЕТА
     * @param conn HttpUrlConnection
     * @throws IOException
     */
    public void receive(HttpURLConnection conn) throws IOException {

        if(conn.getHeaderFields() != null) {
            List<String> cookieList = conn.getHeaderFields().get("Set-Cookie");

            if (cookieList != null) {
                for (String cookieTemp : cookieList) {
                    cookieManager.setCookie(URL_ROOT, cookieTemp);
                }
            }
        }

        int status = conn.getResponseCode();
        InputStream is;

        Log.d("JSON RESPONSE URL", "==================RESPONSE HEADERS================");
        if(status >= HttpURLConnection.HTTP_BAD_REQUEST) {
            is = conn.getErrorStream();
            Log.d("JSON RESPONSE ERROR", Integer.toString(status) + " - " + conn.getResponseMessage());
        } else {
            is = conn.getInputStream();
            Log.d("JSON RESPONSE MESSAGE", Integer.toString(status) + " - " + conn.getResponseMessage());
        }

        /*if(!conn.getHeaderField("Content-Type").contains("application/json")) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null) {
                Log.e("JSON ANSWER", line);
            }

            throw new ProtocolException();
        }*/

        this.setJson(IOUtils.toString(is, Charset.forName("UTF-8").name()));

        Log.d("JSON JSON STRING", this.getJson());

        is.close();
    }


    /**
     *
     * @return JSONObject
     */
    public JSONObject toJsonObject() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(this.getJson());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data object " + e.toString());
        }
        return jsonObject;
    }

    /**
     *
     * @return JSONArray
     */
    public JSONArray toJsonArray() {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(this.json);

            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d("JSON", jsonArray.getJSONObject(i).toString(1));
            }
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data array " + e.toString());
        }
        return jsonArray;
    }

    /**
     * ПРОСИМ _token и COOKIE
     */
    public void ping() {

        Log.d("Auth JSON", "ПРОСИМ _token");
        Log.d("JSON", "===============BEGIN ASK TOKEN===================");

        this.setMethod(METHOD_GET);
        this.setResource("home");

        if (!this.execute()) return;

        try {
            this.auth = this.toJsonObject().getBoolean("success");
            String token = this.toJsonObject().getString("_token");
            prefs.edit()
                    .putString("_token", token)
                    .apply();
        } catch (JSONException e) {
            Log.e("JSON AUTH", e.getMessage());
        }
        Log.d("JSON", "===============END ASK TOKEN===================");
    }

    /**
     * ОТПРАВЛЯЕМ АВТОРИЗАЦИЮ
     */
    public Boolean makeAuth(String mEmail,String mPassword) {

        String mDeviceToken = prefs.getString("device_token","");

        Log.d("JSON", "===============BEGIN===================");

        this.setResource("restapi/login");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("login",mEmail);
            jsonObject.put("password",mPassword);
            jsonObject.put("system","android");
            jsonObject.put("deviceToken",mDeviceToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.setJsonDataObject(jsonObject);
        this.setMethod(METHOD_POST);

        Log.d("AUTH JSON", mEmail + " - " + mPassword);

        Log.d("JSON", "================END====================");

        return this.execute();
    }

    public void addFile(String filename,File file) {
        files.put(filename, file);
    }

    public void generateBoundary()
    {
        this.boundary = "--------------------" + UUID.randomUUID().toString();
    }

    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return this.resource;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }

    public void setParams(ContentValues params) {
        this.params = params;
    }

    public ContentValues getParams() {
        return this.params;
    }

    public void setHeaders(Map<String,List<String>> headers) {
        this.headers = headers;
    }


    public Boolean isAuth() {
        return auth;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public JSONArray getJsonDataArray() {
        return jsonDataArray;
    }

    public void setJsonDataArray(JSONArray jsonDataArray) {
        this.jsonDataArray = jsonDataArray;
    }

    public JSONObject getJsonDataObject() {
        return jsonDataObject;
    }

    public void setJsonDataObject(JSONObject jsonDataObject) {
        this.jsonDataObject = jsonDataObject;
    }
}
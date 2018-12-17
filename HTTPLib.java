package shubhadeep.com.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HTTPLib {

    /**
     * This is a model with getter and setter which contains the HTTP response information
     */
    public class HTTPResponse {
        // HTTP code of the response received, -1 means network error
        private int responseCode = -1;
        // Response body in plain text
        private String responseBody = null;

        /**
         * returns the Response Code
         */
        public int getResponseCode() {
            return this.responseCode;
        }

        /**
         * sets the Response Code
         */
        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        /**
         * returns the Response Body
         */
        public String getResponseBody() {
            return this.responseBody;
        }

        /**
         * sets the Response Body
         */
        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

    }

    //constant variables
    private static final String LINE_FEED = "\r\n";
    private static final String TAG = "HTTPLib";

    /**
     * This utility class provides an abstraction layer for sending JSON HTTP Post Request
     */
    public abstract class HTTPUtility {
        //define the charset
        private final String charset = "UTF-8";
        //HttpURLConnection object
        private HttpURLConnection httpConn;
        //Header parameters
        private HashMap<String, String> headers = new HashMap<String, String>();
        //Request URL
        private String requestURL = null;
        //Request method
        private String method = null;

        /**
         * This constructor initializes a new HTTP request
         */
        public HTTPUtility(String requestURL, String method) {
            this.requestURL = requestURL;
            this.method = method;
        }

        /**
         * initializes a new HTTP POST request
         */
        private void CreateConnection()
                throws IOException {
            URL url = new URL(requestURL);
            //creates connection
            httpConn = (HttpURLConnection) url.openConnection();
            //sets headers
            Set<Map.Entry<String, String>> set = headers.entrySet();
            for (Map.Entry<String, String> me : set) {
                httpConn.setRequestProperty(me.getKey(), me.getValue());
            }
            //sets method
            httpConn.setRequestMethod(method);
            httpConn.setUseCaches(false);
            if (method == "POST") {
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
            }

        }

        /**
         * Adds a header field to the request header
         */
        public void addHeader(String key, String value) {
            headers.put(key, value);
        }

        /**
         * Adds a JSON field to the request body
         */
        public abstract void addParameter(String key, Object value);

        /**
         * Completes the HTTP Post call and returns an object of the HTTPResponse class
         */
        public abstract HTTPResponse finish();

        /**
         * Reads the response text from HTTP Connection
         */
        private String readResponseBody() {
            try {
                StringBuilder s_buffer = new StringBuilder();
                InputStream is = new BufferedInputStream(httpConn.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    s_buffer.append(inputLine);
                }
                return s_buffer.toString();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

    /**
     * This utility class provides an abstraction layer for sending JSON HTTP Post Request
     */
    public class JSONPostUtility extends HTTPUtility {
        //holds the JSON parameters
        private JSONObject jsonParam = new JSONObject();

        /**
         * This constructor initializes a new HTTP POST request with content type is set to application/json
         */
        public JSONPostUtility(String requestURL)
                throws IOException {
            //call super constructor
            super(requestURL, "POST");
            //set the content type
            super.addHeader("Content-Type", "application/json;charset=" + super.charset);
            super.addHeader("Accept", "application/json");
        }

        /**
         * Adds a JSON field to the request body
         */
        public void addParameter(String key, Object value) {
            try {
                jsonParam.put(key, value);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        /**
         * Completes the HTTP Post call and returns an object of the HTTPResponse class
         */
        public HTTPResponse finish() {
            HTTPResponse response = new HTTPResponse();
            try {
                //create the HTTP connection
                super.CreateConnection();
                //write the JSON data via DataOutputStream
                DataOutputStream dataOutputStream = new DataOutputStream(super.httpConn.getOutputStream());
                dataOutputStream.writeBytes(jsonParam.toString());
                //get the results
                response.setResponseCode(super.httpConn.getResponseCode());
                response.setResponseBody(super.readResponseBody());
                dataOutputStream.flush();
                dataOutputStream.close();
                super.httpConn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return response;
        }
    }

    /**
     * This utility class provides an abstraction layer for sending multipart HTTP Post Request
     */
    public class MultipartPostUtility extends HTTPUtility {
        //constant to define the boundary between every parameter
        private final String boundary = "*****";
        //PrintWriter to write the data
        private PrintWriter writer = null;
        //OutputStream to write the data
        private OutputStream outputStream = null;
        //Form parameters
        private HashMap<String, String> params = new HashMap<String, String>();
        //Files
        HashMap<String, Map.Entry<String, byte[]>> files = new HashMap<String, Map.Entry<String, byte[]>>();

        /**
         * This constructor initializes a new HTTP POST request with content type is set to multipart/form-data
         */
        public MultipartPostUtility(String requestURL) {
            super(requestURL, "POST");
            super.addHeader("Connection", "Keep-Alive");
            super.addHeader("Content-Type", "multipart/form-data;boundary=" + boundary);
        }

        /**
         * Adds a form field to the request
         */
        public void addParameter(String key, Object value) {
            params.put(key, value.toString());
        }

        /**
         * Adds all the form parameters to the writer
         */
        private void addParametersToWriter() {
            Set<Map.Entry<String, String>> set = params.entrySet();
            for (Map.Entry<String, String> me : set) {
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"" + me.getKey() + "\"")
                        .append(LINE_FEED);
                writer.append("Content-Type: text/plain; charset=" + super.charset).append(
                        LINE_FEED);
                writer.append(LINE_FEED);
                writer.append(me.getValue()).append(LINE_FEED);
                writer.flush();
            }
        }

        /**
         * Adds a file to the files section of the request
         */
        public void addFile(String fieldName, String fileName, byte[] buffer) {
            files.put(fieldName, new AbstractMap.SimpleEntry<String, byte[]>(fileName, buffer));
        }

        /**
         * Adds all the files to the writer
         */
        private void addFilesToWriter()
                throws Exception {
            Set<Map.Entry<String, Map.Entry<String, byte[]>>> set = files.entrySet();
            for (Map.Entry<String, Map.Entry<String, byte[]>> me : set) {
                writer.append("--" + boundary).append(LINE_FEED);
                writer.append(
                        "Content-Disposition: form-data; name=\"" + me.getKey()
                                + "\"; filename=\"" + me.getValue().getKey() + "\"")
                        .append(LINE_FEED);
                writer.append(
                        "Content-Type: "
                                + URLConnection.guessContentTypeFromName(me.getValue().getKey()))
                        .append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();

                outputStream.write(me.getValue().getValue(), 0, me.getValue().getValue().length);
                outputStream.flush();

                writer.append(LINE_FEED);
                writer.flush();
            }
        }

        /**
         * Completes the HTTP Post call and returns an object of the HTTPResponse class
         */
        public HTTPResponse finish() {
            HTTPResponse response = new HTTPResponse();
            try {
                super.CreateConnection();
                outputStream = super.httpConn.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, super.charset),
                        true);
                addParametersToWriter();
                addFilesToWriter();
                writer.append(LINE_FEED).flush();
                writer.append("--" + boundary + "--").append(LINE_FEED);
                writer.close();

                response.setResponseCode(super.httpConn.getResponseCode());
                response.setResponseBody(super.readResponseBody());

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            return response;
        }

    }

    /**
     * This utility class provides an abstraction layer for sending JSON HTTP Post Request
     */
    public class HTTPGetUtility extends HTTPUtility {

        /**
         * This constructor initializes a new HTTP POST request with content type is set to application/json
         */
        public HTTPGetUtility(String requestURL) {
            super(requestURL, "GET");
        }

        /**
         * Adds a JSON field to the request body
         */
        public void addParameter(String key, Object value) {
            if (!super.requestURL.contains("?")) {
                super.requestURL = super.requestURL + "?";
            }
            super.requestURL = super.requestURL + key + "=" + value.toString();
        }

        /**
         * Completes the HTTP Post call and returns an object of the HTTPResponse class
         */
        public HTTPResponse finish() {
            HTTPResponse response = new HTTPResponse();
            try {
                super.CreateConnection();
                response.setResponseCode(super.httpConn.getResponseCode());
                response.setResponseBody(super.readResponseBody());
                super.httpConn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return response;
        }
    }
}


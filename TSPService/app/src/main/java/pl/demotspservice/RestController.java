package pl.demotspservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RestController {
    private String baseUrl;
    private String urlResource;
    private String httpMethod;
    private String urlPath;
    private String serverResponse;
    private JSONObject requestData;
    private HashMap<String, String> parameters;
    private Map<String, List<String>> headerFields;
    private HashMap<String, String> headerRequestFields;

    public RestController(String baseUrl, String urlResource,
                          String urlPath, String httpMethod,
                          HashMap<String, String> parameters,
                          HashMap<String, String> headerRequestFields)
    {
        setBaseUrl(baseUrl);
        setUrlResource(urlResource);
        setUrlPath(urlPath);
        setHttpMethod(httpMethod);
        setParameters(parameters);
        setHeaderRequestFields(headerRequestFields);
        serverResponse = "";
        requestData = new JSONObject();
        headerFields = new HashMap<>();
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    // https://demotspservice.pl/
    public RestController setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
            this.baseUrl += "/";
        }
        return this;
    }

    // api/parcels
    public RestController setUrlResource(String urlResource) {
        this.urlResource = urlResource;
        return this;
    }

    // 1/edit
    public final RestController setUrlPath(String urlPath) {
        this.urlPath = urlPath;
        return this;
    }

    public RestController setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public RestController setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public RestController setHeaderRequestFields(HashMap<String, String> headerRequestFields) {
        this.headerRequestFields = headerRequestFields;
        return this;
    }

    public JSONObject getServerResponseAsJsonObject() {
        try {
            return new JSONObject(String.valueOf(serverResponse));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getRequestDataAsJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject execute(){
        String line;
        StringBuilder outputStringBuilder = new StringBuilder();

        try {
            StringBuilder urlString = new StringBuilder(baseUrl + urlResource);

            if(!urlPath.equals("")) {
                urlString.append("/" + urlPath);
            }

            if(parameters.size() > 0 && httpMethod.equals("GET")) {
                requestData = getRequestDataAsJSONObject();
                urlString.append("?" + requestData);
            }

            URL url = new URL(urlString.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod);
            for (Map.Entry<String, String> entry : headerRequestFields.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            connection.setRequestProperty("Accept", "application/json");

            if (httpMethod.equals("POST") || httpMethod.equals("PUT")) {
                requestData = getRequestDataAsJSONObject();

                connection.setDoInput(true);
                connection.setDoOutput(true);

                try {
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(requestData.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    headerFields = connection.getHeaderFields();

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        outputStringBuilder.append(line);
                    }
                } catch (Exception ex){}
                connection.disconnect();
            } else {
                InputStream content = (InputStream) connection.getInputStream();

                headerFields = connection.getHeaderFields();

                BufferedReader in = new BufferedReader(new InputStreamReader(content));

                while ((line = in.readLine()) != null) {
                    outputStringBuilder.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!outputStringBuilder.toString().equals("")) {
            serverResponse = outputStringBuilder.toString();
        } else {
            serverResponse = "{\"message\": \"Call failed!\", \"result\": \"false\"}";
        }

        return getServerResponseAsJsonObject();
    }
}

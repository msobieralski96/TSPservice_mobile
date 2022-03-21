package pl.demotspservice;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ParcelMenuActivity extends AppCompatActivity {

    TextView text_parcel_sscc;
    Button button_show_details;
    Button button_modify_status;
    Button button_confirm_delivery;

    private String token;
    private String username;
    private JSONObject serverOutput;
    private JSONObject TSPdata;
    private String SSCC;
    private String id;

    private String baseUrl;
    private String urlResource;
    private String urlPath;
    private String httpMethod;
    private HashMap<String, String> parameters;
    private HashMap<String, String> headerRequestFields;

    private boolean block_back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcel_menu);

        block_back_pressed = false;

        text_parcel_sscc = (TextView) findViewById(R.id.text_parcel_sscc);
        button_show_details = (Button) findViewById(R.id.button_show_details);
        button_modify_status = (Button) findViewById(R.id.button_modify_status);
        button_confirm_delivery = (Button) findViewById(R.id.button_confirm_delivery);

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        username = extras.getString("username");
        serverOutput = null;
        SSCC = "Przesyłka ";
        id = null;
        try {
            serverOutput = new JSONObject(String.valueOf(extras.getString("serverOutput")));
            SSCC += serverOutput.getJSONObject("parcel").getString("SSCC_number");
            id = serverOutput.getJSONObject("parcel").getString("id");
            TSPdata = new JSONObject(String.valueOf(extras.getString("TSPdata")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        text_parcel_sscc.setText(SSCC);

        button_show_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseUrl = "https://demotspservice.pl";
                urlResource = "api/parcels";
                urlPath = id;
                httpMethod = "GET";
                parameters = new HashMap<String, String>();
                headerRequestFields = new HashMap<String, String>();
                headerRequestFields.put("Authorization", "Bearer " + token);
                try {
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new ParcelMenuActivity.ShowParcelDetailActivityOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });

        button_modify_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseUrl = "https://demotspservice.pl";
                urlResource = "api/parcels";
                urlPath = id;
                httpMethod = "GET";
                parameters = new HashMap<String, String>();
                headerRequestFields = new HashMap<String, String>();
                headerRequestFields.put("Authorization", "Bearer " + token);
                try {
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new ParcelMenuActivity.ShowParcelEditActivityOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });

        button_confirm_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseUrl = "https://demotspservice.pl";
                urlResource = "api/parcels";
                urlPath = id;
                httpMethod = "GET";
                parameters = new HashMap<String, String>();
                headerRequestFields = new HashMap<String, String>();
                headerRequestFields.put("Authorization", "Bearer " + token);
                try {
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new ParcelMenuActivity.ShowConfirmDeliveryActivityOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });
    }

    public class ShowParcelDetailActivityOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;

        public ShowParcelDetailActivityOperation(RestController restController){
            this.restController = restController;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                serverOutput = restController.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serverOutput;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            goToParcelDetailActivity(token, username, serverOutput);
        }
    }

    private void goToParcelDetailActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());
            bundle.putString("TSPdata", TSPdata.toString());

            Intent intent = new Intent(this, ParcelDetailActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    public class ShowParcelEditActivityOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;

        public ShowParcelEditActivityOperation(RestController restController){
            this.restController = restController;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                serverOutput = restController.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serverOutput;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            goToParcelEditActivity(token, username, serverOutput);
        }
    }

    private void goToParcelEditActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());
            bundle.putString("TSPdata", TSPdata.toString());

            Intent intent = new Intent(this, ParcelEditActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    public class ShowConfirmDeliveryActivityOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;

        public ShowConfirmDeliveryActivityOperation(RestController restController){
            this.restController = restController;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                serverOutput = restController.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serverOutput;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            findViewById(R.id.loadingPanel).setVisibility(View.GONE);

            goToConfirmDeliveryActivity(token, username, serverOutput);
        }
    }

    private void goToConfirmDeliveryActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());
            bundle.putString("TSPdata", TSPdata.toString());

            Intent intent = new Intent(this, ConfirmDeliveryActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    public void onBackPressed() {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);

            Intent intent;
            if(TSPdata.toString().equals("{}")){
                intent = new Intent(this, MenuActivity.class);
            } else /*(TSPdata constains some data)*/{
                bundle.putString("serverOutput", TSPdata.toString());
                intent = new Intent(this, TSPResolveActivity.class);
            }

            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}

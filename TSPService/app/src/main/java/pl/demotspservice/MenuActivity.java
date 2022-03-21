package pl.demotspservice;

import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

public class MenuActivity extends AppCompatActivity {

    private TextView text_view_logged;
    private Button button_logout;
    private Button button_show_parcels;
    private Button button_scan_barcode;
    private Button button_resolve_tsp;

    private String token;
    private String username;
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
        setContentView(R.layout.activity_menu);

        block_back_pressed = false;

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        username = extras.getString("username");

        text_view_logged = (TextView) findViewById(R.id.text_view_logged);
        button_logout = (Button) findViewById(R.id.button_logout);
        button_show_parcels = (Button) findViewById(R.id.button_show_parcels);
        button_scan_barcode = (Button) findViewById(R.id.button_scan_barcode);
        button_resolve_tsp = (Button) findViewById(R.id.button_resolve_tsp);

        text_view_logged.setText("Witaj " + username + "!");

        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    baseUrl = "https://demotspservice.pl";
                    urlResource = "api/logout";
                    urlPath = "";
                    httpMethod = "GET";
                    parameters = new HashMap<String, String>();
                    headerRequestFields = new HashMap<String, String>();
                    headerRequestFields.put("Authorization", "Bearer " + token);
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new MenuActivity.LogoutOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });

        button_resolve_tsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*try {
                    baseUrl = "https://demotspservice.pl";
                    urlResource = "api/TSP";
                    urlPath = "";
                    httpMethod = "GET";
                    parameters = new HashMap<String, String>();
                    headerRequestFields = new HashMap<String, String>();
                    headerRequestFields.put("Authorization", "Bearer " + token);
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new MenuActivity.PrepareTSPOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }*/
                goToTSPPrepareActivity(token, username);
            }
        });

        button_show_parcels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*try {
                    baseUrl = "https://demotspservice.pl";
                    urlResource = "api/parcels";
                    urlPath = "";
                    httpMethod = "GET";
                    parameters = new HashMap<String, String>();
                    headerRequestFields = new HashMap<String, String>();
                    headerRequestFields.put("Authorization", "Bearer " + token);
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new MenuActivity.ShowParcelsOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }*/
                goToShowParcelsActivity(token, username);
            }
        });

        button_scan_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToScanBarcodeActivity();
            }
        });

        if(Camera.getNumberOfCameras() < 1){
            button_scan_barcode.setClickable(false);
            button_scan_barcode.setVisibility(View.GONE);
        }
    }

    public class LogoutOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;

        public LogoutOperation(RestController restController){
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

            block_back_pressed = false;
            goToLoginActivity();
            Toast.makeText(getApplicationContext(), "Wylogowano!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void goToLoginActivity() {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

/*    public class ShowParcelsOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;

        public ShowParcelsOperation(RestController restController){
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

            goToShowParcelsActivity(token, username, serverOutput);
        }
    }

    private void goToShowParcelsActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());

            Intent intent = new Intent(this, ParcelIndexActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }*/

    private void goToShowParcelsActivity(String token, String username) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);

            Intent intent = new Intent(this, ParcelIndexActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    private void goToScanBarcodeActivity() {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putInt("camera_facing", 0);

            Intent intent = new Intent(this, ScanBarcodeActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    /*public class PrepareTSPOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;

        public PrepareTSPOperation(RestController restController){
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

            goToTSPPrepareActivity(token, username, serverOutput);
        }
    }

    private void goToTSPPrepareActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());

            Intent intent = new Intent(this, TSPPrepareActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }*/

    private void goToTSPPrepareActivity(String token, String username) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);

            Intent intent = new Intent(this, TSPPrepareActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(!block_back_pressed) {
            block_back_pressed = true;
            try {
                baseUrl = "https://demotspservice.pl";
                urlResource = "api/logout";
                urlPath = "";
                httpMethod = "GET";
                parameters = new HashMap<String, String>();
                headerRequestFields = new HashMap<String, String>();
                headerRequestFields.put("Authorization", "Bearer " + token);
                RestController restController =
                        new RestController(baseUrl, urlResource, urlPath,
                                httpMethod, parameters, headerRequestFields);

                AsyncTask<Void, Void, JSONObject> execute = new MenuActivity.LogoutOperation(restController);
                execute.execute();
            } catch (Exception ex) {
            }
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
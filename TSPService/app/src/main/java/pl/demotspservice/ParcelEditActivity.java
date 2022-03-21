package pl.demotspservice;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ParcelEditActivity extends AppCompatActivity {

    private EditText edit_text_deliver_order;
    private EditText edit_text_get_order;
    private EditText edit_text_current_address;
    private Spinner spinner_state_of_delivery;
    private ArrayAdapter<CharSequence> adapter;
    private EditText edit_text_state_of_delivery;
    private EditText edit_text_date_of_delivery;
    private EditText edit_text_date_of_get_delivery;
    private Button button_edit_parcel;

    private String token;
    private String username;
    private JSONObject serverOutput;
    private JSONObject TSPdata;

    private String id;
    private String deliver_order;
    private String get_order;
    private String current_address;
    private String state_of_delivery;
    private String date_of_delivery;
    private String date_of_get_delivery;

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
        setContentView(R.layout.activity_parcel_edit);

        block_back_pressed = false;

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        username = extras.getString("username");
        serverOutput = null;
        id = null;
        try {
            serverOutput = new JSONObject(String.valueOf(extras.getString("serverOutput")));
            id = serverOutput.getJSONObject("parcel").getString("id");
            deliver_order = serverOutput.getJSONObject("parcel").getString("deliver_order");
            get_order = serverOutput.getJSONObject("parcel").getString("get_order");
            current_address = serverOutput.getJSONObject("parcel").getString("current_address");
            state_of_delivery = serverOutput.getJSONObject("parcel").getString("state_of_delivery");
            date_of_delivery = serverOutput.getJSONObject("parcel").getString("date_of_delivery");
            date_of_get_delivery = serverOutput.getJSONObject("parcel").getString("date_of_get_delivery");
            TSPdata = new JSONObject(String.valueOf(extras.getString("TSPdata")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        baseUrl = "https://demotspservice.pl";
        urlResource = "api/parcels";

        PrepareForm();
    }

    private void PrepareForm() {
        edit_text_deliver_order = (EditText) findViewById(R.id.edit_text_deliver_order);
        edit_text_get_order = (EditText) findViewById(R.id.edit_text_get_order);
        edit_text_current_address = (EditText) findViewById(R.id.edit_text_current_address);
        spinner_state_of_delivery = (Spinner) findViewById(R.id.spinner_state_of_delivery);
        edit_text_state_of_delivery = (EditText) findViewById(R.id.edit_text_state_of_delivery);
        edit_text_date_of_delivery = (EditText) findViewById(R.id.edit_text_date_of_delivery);
        edit_text_date_of_get_delivery = (EditText) findViewById(R.id.edit_text_date_of_get_delivery);
        button_edit_parcel = (Button) findViewById(R.id.button_edit_parcel);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.state_of_delivery_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_state_of_delivery.setAdapter(adapter);

        if(!(deliver_order.equals("null"))){
            edit_text_deliver_order.setText(deliver_order);}
        if(!(get_order.equals("null"))){
            edit_text_get_order.setText(get_order);}
        edit_text_current_address.setText(current_address);
        if(state_of_delivery.equals("Przesyłka zarejestrowana w systemie") ||
                state_of_delivery.equals("Odebrana od nadawcy") ||
                state_of_delivery.equals("W magazynie") ||
                state_of_delivery.equals("Przygotowana do nadania") ||
                state_of_delivery.equals("W trasie") ||
                state_of_delivery.equals("W sortowni") ||
                state_of_delivery.equals("W oddziale docelowym") ||
                state_of_delivery.equals("W drodze do klienta") ||
                state_of_delivery.equals("Doręczona") ||
                state_of_delivery.equals("Awizo") ||
                state_of_delivery.equals("Zwrócona do nadawcy")) {
            int pos = adapter.getPosition(state_of_delivery);
            spinner_state_of_delivery.setSelection(pos);
            edit_text_state_of_delivery.setText("");
            edit_text_state_of_delivery.setFocusable(false);
            edit_text_state_of_delivery.setFocusableInTouchMode(false);
            edit_text_state_of_delivery.setClickable(false);
            edit_text_state_of_delivery.setInputType(InputType.TYPE_NULL);
        } else {
            int pos = adapter.getPosition("Inny");
            spinner_state_of_delivery.setSelection(pos);
            edit_text_state_of_delivery.setText(state_of_delivery);
            edit_text_state_of_delivery.setFocusable(true);
            edit_text_state_of_delivery.setFocusableInTouchMode(true);
            edit_text_state_of_delivery.setClickable(true);
            edit_text_state_of_delivery.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        edit_text_date_of_delivery.setText(date_of_delivery);
        edit_text_date_of_get_delivery.setText(date_of_get_delivery);

        spinner_state_of_delivery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(spinner_state_of_delivery.getSelectedItem().toString().equals("Inny")){
                    edit_text_state_of_delivery.setFocusable(true);
                    edit_text_state_of_delivery.setFocusableInTouchMode(true);
                    edit_text_state_of_delivery.setClickable(true);
                    edit_text_state_of_delivery.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    edit_text_state_of_delivery.setText("");
                    edit_text_state_of_delivery.setFocusable(false);
                    edit_text_state_of_delivery.setFocusableInTouchMode(false);
                    edit_text_state_of_delivery.setClickable(false);
                    edit_text_state_of_delivery.setInputType(InputType.TYPE_NULL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // do nothing
            }

        });

        button_edit_parcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SetParametersForEditRequest();
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new ParcelEditActivity.EditParcelOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });
    }

    private void SetParametersForEditRequest() {
        urlPath = id + "/edit";
        httpMethod = "PUT";
        parameters = new HashMap<String, String>();
        parameters.put("deliver_order", edit_text_deliver_order.getText().toString());
        parameters.put("get_order", edit_text_get_order.getText().toString());
        parameters.put("current_address", edit_text_current_address.getText().toString());
        if (spinner_state_of_delivery.getSelectedItem().toString().equals("Inny")){
            parameters.put("state_of_delivery", edit_text_state_of_delivery.getText().toString());
        } else {
            parameters.put("state_of_delivery", spinner_state_of_delivery.getSelectedItem().toString());
        }
        parameters.put("date_of_delivery", edit_text_date_of_delivery.getText().toString());
        parameters.put("date_of_get_delivery", edit_text_date_of_get_delivery.getText().toString());
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);
        headerRequestFields.put("Content-Type", "application/json");
        headerRequestFields.put("X-Requested-With", "XMLHttpRequest");
    }

    private void SetParametersForBackRequest() {
        urlPath = id;
        httpMethod = "GET";
        parameters = new HashMap<String, String>();
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);
    }

    public class EditParcelOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;
        private String resultString;

        public EditParcelOperation(RestController restController){
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

            resultString = "false";
            try {
                resultString = serverOutput.get("result").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(resultString.equals("true")){
                Toast.makeText(getApplicationContext(), "Sukces",
                        Toast.LENGTH_LONG).show();
                onBackPressed();
            }
            else {
                Toast.makeText(getApplicationContext(), "Operacja zakończona niepowodzeniem!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public class ShowParcelMenuOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;

        public ShowParcelMenuOperation(RestController restController){
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
            goToParcelMenuActivity(token, username, serverOutput);
        }
    }

    private void goToParcelMenuActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());
            bundle.putString("TSPdata", TSPdata.toString());

            Intent intent = new Intent(this, ParcelMenuActivity.class);
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
                SetParametersForBackRequest();
                RestController restController =
                        new RestController(baseUrl, urlResource, urlPath,
                                httpMethod, parameters, headerRequestFields);

                AsyncTask<Void, Void, JSONObject> execute = new ParcelEditActivity.ShowParcelMenuOperation(restController);
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
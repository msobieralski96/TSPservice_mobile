package pl.demotspservice;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ParcelDetailActivity extends AppCompatActivity {

    TableLayout detail_table;

    private String token;
    private String username;
    private JSONObject serverOutput;
    private JSONObject TSPdata;
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
        setContentView(R.layout.activity_parcel_detail);

        block_back_pressed = false;

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        username = extras.getString("username");
        serverOutput = null;
        id = null;
        try {
            serverOutput = new JSONObject(String.valueOf(extras.getString("serverOutput")));
            id = serverOutput.getJSONObject("parcel").getString("id");
            TSPdata = new JSONObject(String.valueOf(extras.getString("TSPdata")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        baseUrl = "https://demotspservice.pl";
        urlResource = "api/parcels";
        urlPath = id;
        httpMethod = "GET";
        parameters = new HashMap<String, String>();
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);

        detail_table = (TableLayout) findViewById(R.id.table_parcel_detail);
        createDetailsTable();
    }

    private void createDetailsTable(){
        JSONObject parcel = new JSONObject();
        JSONObject courier = new JSONObject();
        try {
            parcel = serverOutput.getJSONObject("parcel");
            courier = serverOutput.getJSONObject("courier");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int i = 0;
        i = addCategoryRow("DANE PRZESYŁKI:", i);
        i = addRow("Numer przesyłki (SSCC)", parcel, "SSCC_number", i);
        i = addRow("Zawartość przesyłki", parcel, "parcel_content", i);
        i = addRow("Masa przesyłki", parcel, "mass", i);
        i = addRow("Rozmiar przesyłki", parcel, "size", i);
        i = addCategoryRow("DANE ODBIORCY:", i);
        i = addRow("Adres dostawy", parcel, "address", i);
        i = addRow("Imię", parcel, "client_first_name", i);
        i = addRow("Nazwisko", parcel, "client_last_name", i);
        i = addRow("Numer telefonu", parcel, "client_phone_number", i);
        i = addRow("e-mail", parcel, "client_email", i);
        i = addCategoryRow("DANE NADAWCY:", i);
        i = addRow("Adres nadawcy", parcel, "sender_address", i);
        i = addRow("Imię", parcel, "sender_first_name", i);
        i = addRow("Nazwisko", parcel, "sender_last_name", i);
        i = addRow("Numer telefonu", parcel, "sender_phone_number", i);
        i = addRow("e-mail", parcel, "sender_email", i);
        i = addCategoryRow("STATUS PRZESYŁKI:", i);
        i = addRow("Aktualna lokalizacja", parcel, "current_address", i);
        i = addRow("Status przesyłki", parcel, "state_of_delivery", i);
        i = addRow("Szacowany dzień odbioru od nadawcy", parcel, "date_of_get_delivery", i);
        i = addRow("Szacowany dzień dostawy", parcel, "date_of_delivery", i);
        i = addCategoryRow("DANE KURIERA:", i);
        i = addRow("Kurier", courier, "name", i);
        i = addRow("Numer telefonu", courier, "phone_number", i);
        i = addRow("e-mail", courier, "email", i);
        i = addCategoryRow("INNE:", i);
        i = addRow("Numer Id w systemie", parcel, "id", i);
        i = addRow("Zarejestrowana w systemie", parcel, "created_at", i);
        i = addRow("Ostatnia edycja danych", parcel, "updated_at", i);
    }

    private int setColorBackground(int i, TextView txt){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if(i%2==0) {
                txt.setBackgroundResource(R.color.colorRowSeparator2);
            } else {
                txt.setBackgroundResource(R.color.colorRowSeparator);
            }
        }
        i++;
        return i;
    }

    private int addCategoryRow(String name, int i){
        TableRow row = new TableRow(this);

        TextView textview = prepareTextView(name);
        textview.setBackgroundResource(R.color.dimBackgroundColor);
        i++;

        row.addView(textview);
        detail_table.addView(row);
        return i;
    }

    private int addRow(String btnTxt, JSONObject data, String JSONkey, int i){
        String key = null;
        if(data.toString() != "null") {
            try {
                key = data.getString(JSONkey);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(key == null){
            return i;//not add new row
        }// else do code below

        TableRow row = new TableRow(this);

        Button button = prepareButton(btnTxt);
        TextView textview = prepareTextView(key);
        i = setColorBackground(i, textview);

        row.addView(button);
        row.addView(textview);
        detail_table.addView(row);
        return i;
    }

    private Button prepareButton(String btnTxt){
        Button button = new Button(this);
        button.setText(btnTxt);
        button.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.4));
        button.setClickable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            button.setTextColor(Color.rgb(0xff,0xff,0xff));
        }
        return button;
    }

    private TextView prepareTextView(String key) {
        TextView textview = new TextView(this);
        textview.setText(key);
        textview.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.6));
        textview.setGravity(Gravity.CENTER);
        return textview;
    }

    @Override
    public void onBackPressed() {
        if(!block_back_pressed) {
            block_back_pressed = true;
            try {
                RestController restController =
                        new RestController(baseUrl, urlResource, urlPath,
                                httpMethod, parameters, headerRequestFields);

                AsyncTask<Void, Void, JSONObject> execute = new ParcelDetailActivity.ShowParcelMenuOperation(restController);
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
}

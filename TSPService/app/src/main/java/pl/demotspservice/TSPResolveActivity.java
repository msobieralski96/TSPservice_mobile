package pl.demotspservice;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TSPResolveActivity extends AppCompatActivity {

    private TableLayout table_parcels;
    private Button buttonOrder;
    private Button buttonId;
    private Button buttonAddressType;
    private Button buttonAddress;
    private Button buttonDate;
    private Button buttonState;

    private String token;
    private String username;
    private JSONObject serverOutput;

    private String baseUrl;
    private String urlResource;
    private String urlPath;
    private String httpMethod;
    private HashMap<String, String> parameters;
    private HashMap<String, String> headerRequestFields;

    private List<JSONObject> data;

    private boolean block_back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tspresolve);

        block_back_pressed = false;

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        username = extras.getString("username");
        serverOutput = null;
        try {
            serverOutput = new JSONObject(String.valueOf(extras.getString("serverOutput")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initTableLayout();
        data = getData();
        fillTableWithParcels();
    }

    private void initTableLayout(){
        table_parcels = (TableLayout) findViewById(R.id.table_parcels);

        buttonOrder = (Button) findViewById(R.id.button_order) ;
        buttonId = (Button) findViewById(R.id.button_id);
        buttonAddressType = (Button) findViewById(R.id.button_address_type);
        buttonAddress = (Button) findViewById(R.id.button_address);
        buttonDate = (Button) findViewById(R.id.button_date);
        buttonState = (Button) findViewById(R.id.button_state);
    }

    private void SetParametersForGetToParcelMenuRequest(String id) {
        baseUrl = "https://demotspservice.pl";
        urlResource = "api/parcels";
        urlPath = id;
        httpMethod = "GET";
        parameters = new HashMap<String, String>();
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);
    }

    private void fillTableWithParcels(){
        int j = 0;
        int k = 0;
        for (int i=0; i < data.size(); i++) {
            JSONObject obj = data.get(i);
            TableRow row = new TableRow(this);
            String id = null;
            TextView order = new TextView(this);;
            Button custom_id = new Button(this);
            TextView address = new TextView(this);
            TextView address_type = new TextView(this);
            TextView date = new TextView(this);
            TextView state = new TextView(this);
            try {
                order.setText(String.valueOf(i));
                id = obj.getString("id");
                custom_id.setText(obj.getString("SSCC_number"));
                address_type.setText(obj.getString("address_type"));
                address.setText(obj.getString("address"));
                date.setText(obj.getString("date_of_delivery"));
                state.setText(obj.getString("state_of_delivery"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String id_string = id;//compulsory to pass a parameter
            order.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.06));
            order.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, order);
            custom_id.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.18));
            custom_id.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, custom_id);
            if(!custom_id.getText().toString().equals(" ")) {
                custom_id.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            SetParametersForGetToParcelMenuRequest(id_string);
                            RestController restController =
                                    new RestController(baseUrl, urlResource, urlPath,
                                            httpMethod, parameters, headerRequestFields);

                            AsyncTask<Void, Void, JSONObject> execute = new TSPResolveActivity.ShowParcelMenuOperation(restController);
                            execute.execute();
                        } catch (Exception ex) {
                        }
                    }
                });
            } else {
                custom_id.setClickable(false);
            }
            address_type.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
            address_type.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, address_type);
            address.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.3));
            address.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, address);
            date.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
            date.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, date);
            state.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
            state.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, state);
            row.addView(order);
            row.addView(custom_id);
            row.addView(address_type);
            row.addView(address);
            row.addView(date);
            row.addView(state);
            table_parcels.addView(row);
            k = i + 1;
        }
        String edges = "";
        String option = "";
        try {
            edges = serverOutput.getJSONArray("options").getString(0);
            option = serverOutput.getJSONArray("options").getString(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addSummaryRow(k, j, "Całkowity dystans:", "totalDistance");
        k++;
        if (option.length() > 0){
            addSummaryRow(k, j, "Całkowity czas:", "totalTime");
            k++;
        }
        addSummaryRow(k, j, "Odleglości między dwoma punktami:", edges);
        k++;
        if (option.length() > 0){
            addSummaryRow(k, j, "Kryterium wyszukiwania:", option);
        }
    }

    private void addSummaryRow(int i, int j, String str, String metric){
        TableRow row = new TableRow(this);

        TextView order = new TextView(this);;
        Button custom_id = new Button(this);
        TextView address = new TextView(this);
        TextView address_type = new TextView(this);
        TextView date = new TextView(this);
        TextView state = new TextView(this);

        order.setText(str);
        try {
            if (metric.equals("totalDistance")){
                custom_id.setText(serverOutput.getDouble("totalDistance") + " km");
            } else if (metric.equals("totalTime")){
                custom_id.setText(serverOutput.getString("totalTime"));
            } else {
                custom_id.setText(metric);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        order.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.06));
        order.setGravity(Gravity.CENTER);
        j = setColorBackground(i, j, order);

        custom_id.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.18));
        custom_id.setGravity(Gravity.CENTER);
        j = setColorBackground(i, j, custom_id);
        custom_id.setClickable(false);

        address_type.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
        address_type.setGravity(Gravity.CENTER);
        j = setColorBackground(i, j, address_type);

        address.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.3));
        address.setGravity(Gravity.CENTER);
        j = setColorBackground(i, j, address);

        date.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
        date.setGravity(Gravity.CENTER);
        j = setColorBackground(i, j, date);

        state.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
        state.setGravity(Gravity.CENTER);
        j = setColorBackground(i, j, state);

        row.addView(order);
        row.addView(custom_id);
        row.addView(address_type);
        row.addView(address);
        row.addView(date);
        row.addView(state);
        table_parcels.addView(row);
    }

    private int setColorBackground(int i, int j, TextView txt){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if(i%2==0) {
                if(j%2==0){
                    txt.setBackgroundResource(R.color.colorRowSeparator4);
                } else {
                    txt.setBackgroundResource(R.color.colorRowSeparator3);
                }
            } else {
                if(j%2==0){
                    txt.setBackgroundResource(R.color.colorRowSeparator2);
                } else {
                    txt.setBackgroundResource(R.color.colorRowSeparator);
                }
            }
        }
        j++;
        return j;
    }

    private int setColorBackground(int i, int j, Button btn){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(i%2==0) {
                if(j%2==0){
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator4));
                } else {
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator3));
                }
            } else {
                if(j%2==0){
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator2));
                } else {
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator));
                }
            }
        }
        j++;
        return j;
    }

    private List<JSONObject> getData(){
        JSONArray parcels = new JSONArray();
        List<JSONObject> data = new ArrayList<JSONObject>();

        try {
            parcels = serverOutput.getJSONArray("parcels");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i=0; i < parcels.length(); i++) {
            JSONObject outputData = null;
            Integer id = null;
            String custom_id = null;
            String address_type = null;
            String address = null;
            String date = null;
            String state = null;
            JSONObject parcel = new JSONObject();
            try {
                outputData = parcels.getJSONObject(i);
                address_type = outputData.getString("addressType");
                address = outputData.getString("address");
                if (address_type.equals("Adres docelowy") ||
                        address_type.equals("Adres nadawcy")) {
                    id = outputData.getInt("parcelId");
                    custom_id = outputData.getString("SSCC_number");
                    date = outputData.getString("date_of_delivery");
                    state = outputData.getString("state_of_delivery");
                } else {
                    id = 0;
                    custom_id = " ";
                    date = " ";
                    state = " ";
                }

                parcel.put("order", i);
                parcel.put("id", id);
                parcel.put("SSCC_number", custom_id);
                parcel.put("address_type", address_type);
                parcel.put("address", address);
                parcel.put("date_of_delivery", date);
                parcel.put("state_of_delivery", state);
                data.add(parcel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return data;
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
            bundle.putString("TSPdata", this.serverOutput.toString());

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
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);

            Intent intent = new Intent(this, MenuActivity.class);
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

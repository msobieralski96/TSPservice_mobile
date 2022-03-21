package pl.demotspservice;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TSPPrepareActivity extends AppCompatActivity {

    EditText edit_text_date;
    Button button_search_by_date;
    Spinner spinner_current_location;
    private ArrayAdapter<String> adapter;

    ScrollView scroll_table;
    private TableLayout table_parcels;
    private Button buttonParcelSortById;
    private Button buttonParcelSortByAddressType;
    private Button buttonParcelSortByAddress;
    private Button buttonParcelSortByCurLoc;
    private Button buttonParcelSortByDate;
    private Button buttonParcelSortByState;
    private Button text_viewChoose;
    private Button text_viewFirst;

    private RadioButton radio_linear_distance;
    private RadioButton radio_real_distance;
    private RadioButton radio_by_distance;
    private RadioButton radio_by_time;
    private Button button_resolve_tsp;
    private Button button_info;
    private RelativeLayout helpPanel;
    private Button tip_button;

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
    private List<String> chosenParcelsData;
    private String firstParcelData;
    private String edges;
    private String option;

    private String currentSpinnerValue;
    private boolean block_back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tspprepare);

        block_back_pressed = false;

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        username = extras.getString("username");
        serverOutput = null;
        /*try {
            serverOutput = new JSONObject(String.valueOf(extras.getString("serverOutput")));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        currentSpinnerValue = "Lokalizacja aktualna";

        initSearchLayout();
        initTableLayout();
        initActionLayout();
    }

    private void initSearchLayout() {
        edit_text_date = (EditText) findViewById(R.id.edit_text_date);
        button_search_by_date = (Button) findViewById(R.id.button_search_by_date);
        spinner_current_location = (Spinner) findViewById(R.id.spinner_current_location);
        scroll_table = (ScrollView) findViewById(R.id.scroll_table);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        edit_text_date.setText(format.format(calendar.getTime()));
        scroll_table.setVisibility(View.INVISIBLE);
        spinner_current_location.setVisibility(View.INVISIBLE);

        button_search_by_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_resolve_tsp.setClickable(false);
                scroll_table.setVisibility(View.INVISIBLE);
                spinner_current_location.setVisibility(View.INVISIBLE);
                try {
                    SetParametersForFindByDateRequest();
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new TSPPrepareActivity.FindByDateOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });

        spinner_current_location.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (spinnerValueChanged()) {
                    button_resolve_tsp.setClickable(false);
                    if (spinner_current_location.getSelectedItem().toString().equals("Lokalizacja aktualna")) {
                        try {
                            SetParametersForFindByDateRequest();
                            RestController restController =
                                    new RestController(baseUrl, urlResource, urlPath,
                                            httpMethod, parameters, headerRequestFields);

                            AsyncTask<Void, Void, JSONObject> execute = new TSPPrepareActivity.FindByDateOperation(restController);
                            execute.execute();
                        } catch (Exception ex) {
                        }
                    } else {
                        try {
                            SetParametersForFindByDateAndLocRequest();
                            RestController restController =
                                    new RestController(baseUrl, urlResource, urlPath,
                                            httpMethod, parameters, headerRequestFields);

                            AsyncTask<Void, Void, JSONObject> execute = new TSPPrepareActivity.FindByDateAndLocOperation(restController);
                            execute.execute();
                        } catch (Exception ex) {
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //scroll_table.setVisibility(View.INVISIBLE);
            }

        });
    }

    private Boolean spinnerValueChanged() {
        if (currentSpinnerValue == spinner_current_location.getSelectedItem().toString()) {
            return false;
        }
        currentSpinnerValue = spinner_current_location.getSelectedItem().toString();
        return true;
    }

    private void initTableLayout() {
        table_parcels = (TableLayout) findViewById(R.id.table_parcels);

        buttonParcelSortById = (Button) findViewById(R.id.button_parcel_sort_by_id);
        buttonParcelSortByAddressType = (Button) findViewById(R.id.button_parcel_sort_by_address_type);
        buttonParcelSortByAddress = (Button) findViewById(R.id.button_parcel_sort_by_address);
        buttonParcelSortByCurLoc = (Button) findViewById(R.id.button_parcel_sort_by_cur_loc);
        buttonParcelSortByDate = (Button) findViewById(R.id.button_parcel_sort_by_date);
        buttonParcelSortByState = (Button) findViewById(R.id.button_parcel_sort_by_state);
        text_viewChoose = (Button) findViewById(R.id.text_view_choose);
        text_viewFirst = (Button) findViewById(R.id.text_view_first);

        buttonParcelSortById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clearTable();
                    data = getData();
                    data = sortRowsById(data);
                    fillTableWithParcels();
                } catch (Exception ex) {
                }
            }
        });
        buttonParcelSortByAddressType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clearTable();
                    data = getData();
                    data = sortRowsByAddressType(data);
                    fillTableWithParcels();
                } catch (Exception ex) {
                }
            }
        });
        buttonParcelSortByAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clearTable();
                    data = getData();
                    data = sortRowsByAddress(data);
                    fillTableWithParcels();
                } catch (Exception ex) {
                }
            }
        });
        buttonParcelSortByCurLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clearTable();
                    data = getData();
                    data = sortRowsByCurLoc(data);
                    fillTableWithParcels();
                } catch (Exception ex) {
                }
            }
        });
        buttonParcelSortByDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clearTable();
                    data = getData();
                    data = sortRowsByDate(data);
                    fillTableWithParcels();
                } catch (Exception ex) {
                }
            }
        });
        buttonParcelSortByState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clearTable();
                    data = getData();
                    data = sortRowsByState(data);
                    fillTableWithParcels();
                } catch (Exception ex) {
                }
            }
        });
    }

    private void initActionLayout() {
        radio_linear_distance= (RadioButton) findViewById(R.id.radio_straight_distance);
        radio_real_distance= (RadioButton) findViewById(R.id.radio_road_distance);
        radio_by_distance = (RadioButton) findViewById(R.id.radio_by_distance);
        radio_by_time = (RadioButton) findViewById(R.id.radio_by_time);
        button_resolve_tsp = (Button) findViewById(R.id.button_resolve_tsp);
        button_info = (Button) findViewById(R.id.button_info);

        helpPanel = (RelativeLayout) findViewById(R.id.helpPanel);
        tip_button = (Button) findViewById(R.id.tip_button);

        option = "distance";
        edges = "real";
        radio_real_distance.setChecked(true);
        radio_by_distance.setChecked(true);

        button_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(helpPanel.getVisibility() == View.VISIBLE){
                    helpPanel.setVisibility(View.GONE);
                } else if (helpPanel.getVisibility() == View.GONE){
                    helpPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        tip_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(helpPanel.getVisibility() == View.VISIBLE){
                    helpPanel.setVisibility(View.GONE);
                } else if (helpPanel.getVisibility() == View.GONE){
                    helpPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        radio_linear_distance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    edges = "linear";
                    radio_real_distance.setChecked(false);
                }
            }
        });

        radio_real_distance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    edges = "real";
                    radio_linear_distance.setChecked(false);
                }
            }
        });

        radio_by_distance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    option = "distance";
                    radio_by_time.setChecked(false);
                }
            }
        });

        radio_by_time.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    option = "duration";
                    radio_by_distance.setChecked(false);
                }
            }
        });

        button_resolve_tsp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_resolve_tsp.setClickable(false);
                button_search_by_date.setClickable(false);
                spinner_current_location.setEnabled(false);
                spinner_current_location.setClickable(false);
                block_back_pressed = true;
                try {
                    SetParametersForTSPResolveRequest();
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new TSPPrepareActivity.ResolveTSPOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });

        button_resolve_tsp.setClickable(false);
    }

    private void SetParametersForTSPResolveRequest() {
        baseUrl = "https://demotspservice.pl";
        urlResource = "api/TSP";
        urlPath = "";
        httpMethod = "POST";
        parameters = new HashMap<String, String>();
        parameters.put("addresses", prepareAdresses(chosenParcelsData));
        parameters.put("first", firstParcelData);
        parameters.put("option", option);
        parameters.put("edges", edges);
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);
        headerRequestFields.put("Content-Type", "application/json");
        headerRequestFields.put("X-Requested-With", "XMLHttpRequest");
    }

    private String prepareAdresses(List<String> list){
        String str = "";
        for(int i = 0; i < list.size(); i++){
            str += list.get(i);
            if(i < list.size()-1){
                str += "@-@";
            }
        }
        return str;
    }

    private void SetParametersForFindByDateRequest() {
        baseUrl = "https://demotspservice.pl";
        urlResource = "api/TSP/order/by_date";
        urlPath = "";
        httpMethod = "POST";
        parameters = new HashMap<String, String>();
        parameters.put("date", edit_text_date.getText().toString());
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);
        headerRequestFields.put("Content-Type", "application/json");
        headerRequestFields.put("X-Requested-With", "XMLHttpRequest");
    }

    private void SetParametersForFindByDateAndLocRequest() {
        baseUrl = "https://demotspservice.pl";
        urlResource = "api/TSP/order/by_date_and_loc";
        urlPath = "";
        httpMethod = "POST";
        parameters = new HashMap<String, String>();
        parameters.put("date", edit_text_date.getText().toString());
        parameters.put("localization", spinner_current_location.getSelectedItem().toString());
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);
        headerRequestFields.put("Content-Type", "application/json");
        headerRequestFields.put("X-Requested-With", "XMLHttpRequest");
    }

    private void fillTableWithParcels() {
        int j = 0;
        for (int i = 0; i < data.size(); i++) {
            JSONObject obj = data.get(i);
            TableRow row = new TableRow(this);
            String id = null;
            String add_type = null;
            TextView custom_id = new Button(this);
            TextView address_type = new TextView(this);
            TextView address = new TextView(this);
            TextView current_address = new TextView(this);
            TextView date = new TextView(this);
            TextView state = new TextView(this);
            CheckBox choose = new CheckBox(this);
            RadioButton first = new RadioButton(this);
            try {
                id = obj.getString("id");
                add_type = obj.getString("address_type");
                if(add_type.substring(0, 1).equals("#")){
                    add_type = "Miejsce predefiniowane";
                }
                custom_id.setText(obj.getString("SSCC_number"));
                address_type.setText(obj.getString("address_type"));
                address.setText(obj.getString("address"));
                current_address.setText(obj.getString("current_address"));
                date.setText(obj.getString("date"));
                state.setText(obj.getString("state_of_delivery"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final String id_final = id;
            final String add_type_final = add_type;

            custom_id.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.18));
            custom_id.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, custom_id);

            address_type.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
            address_type.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, address_type);

            address.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.24));
            address.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, address);

            current_address.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.24));
            current_address.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, current_address);

            date.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
            date.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, date);

            state.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.12));
            state.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, state);

            choose.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.06));
            choose.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, choose);

            choose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    String parcelData = id_final + "|" + add_type_final;
                    if (isChecked)
                    {
                        chosenParcelsData.add(parcelData);
                    } else {
                        for(int i = chosenParcelsData.size()-1; i>=0; i--){
                            if(chosenParcelsData.get(i).equals(parcelData)){
                                chosenParcelsData.remove(i);
                            }
                        }
                    }
                }
            });

            first.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, (float) 0.06));
            first.setGravity(Gravity.CENTER);
            j = setColorBackground(i, j, first);

            first.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    String parcelData = id_final + "|" + add_type_final;
                    if (isChecked)
                    {
                        unCheckRadioButtons(buttonView);
                        firstParcelData = parcelData;
                    }/* else {
                        firstParcelData = null;
                    }*/
                }
            });

            row.addView(custom_id);
            row.addView(address_type);
            row.addView(address);
            row.addView(current_address);
            row.addView(date);
            row.addView(state);
            row.addView(choose);
            row.addView(first);
            table_parcels.addView(row);

            if(data.size() > 0) {
                button_resolve_tsp.setClickable(true);
            }

            chosenParcelsData = new ArrayList<String>();
            firstParcelData = null;
        }
    }

    private void unCheckRadioButtons(View buttonView) {
        RadioButton checkedRadio = (RadioButton) buttonView;
        for(int i = table_parcels.getChildCount()-1; i >= 0; i--) {
            View view = table_parcels.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                for(int j = row.getChildCount()-1; j >= 0; j--) {
                    View component = row.getChildAt(j);
                    if (component instanceof RadioButton && !(component == checkedRadio)) {
                        RadioButton radio = (RadioButton) component;
                        radio.setChecked(false);
                    }
                }
            }
        }
    }

    private int setColorBackground(int i, int j, TextView txt) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (i % 2 == 0) {
                if (j % 2 == 0) {
                    txt.setBackgroundResource(R.color.colorRowSeparator4);
                } else {
                    txt.setBackgroundResource(R.color.colorRowSeparator3);
                }
            } else {
                if (j % 2 == 0) {
                    txt.setBackgroundResource(R.color.colorRowSeparator2);
                } else {
                    txt.setBackgroundResource(R.color.colorRowSeparator);
                }
            }
        }
        j++;
        return j;
    }

    private int setColorBackground(int i, int j, Button btn) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (i % 2 == 0) {
                if (j % 2 == 0) {
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator4));
                } else {
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator3));
                }
            } else {
                if (j % 2 == 0) {
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator2));
                } else {
                    btn.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRowSeparator));
                }
            }
        }
        j++;
        return j;
    }

    private List<JSONObject> getData() {
        List<JSONObject> data = new ArrayList<JSONObject>();

        JSONArray places = new JSONArray();
        JSONArray parcels = new JSONArray();

        try {
            places = serverOutput.getJSONArray("places");
            parcels = serverOutput.getJSONArray("parcels");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < places.length(); i++) {
            JSONObject outputData = null;
            Integer id = null;
            String address_type = null;
            String address = null;
            String current_address = null;
            JSONObject place = new JSONObject();
            try {
                outputData = places.getJSONObject(i);
                id = outputData.getInt("id");
                address_type = "#" + outputData.getString("name");
                address = outputData.getString("address");
                current_address = outputData.getString("address");

                place.put("id", id);
                place.put("SSCC_number", " ");
                place.put("address_type", address_type);
                place.put("address", address);
                place.put("current_address", current_address);
                place.put("date", " ");
                place.put("state_of_delivery", " ");
                data.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        for (int i = 0; i < parcels.length(); i++) {
            JSONObject outputData = null;
            Integer id = null;
            String custom_id = null;
            String address_type_deliver = null;
            String address_type_sender = null;
            String address_deliver = null;
            String address_sender = null;
            String current_address = null;
            String date_deliver = null;
            String date_sender = null;
            String state = null;

            String selected_date = edit_text_date.getText().toString();
            String deliver_date = null;
            String sender_date = null;

            JSONObject parcel_deliver = new JSONObject();
            JSONObject parcel_sender = new JSONObject();
            try {
                outputData = parcels.getJSONObject(i);

                date_deliver = outputData.getString("date_of_delivery");
                date_sender = outputData.getString("date_of_get_delivery");

                deliver_date = convertDate(date_deliver);
                sender_date = convertDate(date_sender);

                if (selected_date.equals(deliver_date) ||
                        selected_date.equals(sender_date)) {
                    id = outputData.getInt("id");
                    custom_id = outputData.getString("SSCC_number");
                    address_type_deliver = "Adres docelowy";
                    address_type_sender = "Adres nadawcy";
                    address_deliver = outputData.getString("address");
                    address_sender = outputData.getString("sender_address");
                    current_address = outputData.getString("current_address");
                    state = outputData.getString("state_of_delivery");
                    if (selected_date.equals(deliver_date)){
                        parcel_deliver.put("id", id);
                        parcel_deliver.put("SSCC_number", custom_id);
                        parcel_deliver.put("address_type", address_type_deliver);
                        parcel_deliver.put("address", address_deliver);
                        parcel_deliver.put("current_address", current_address);
                        parcel_deliver.put("date", date_deliver);
                        parcel_deliver.put("state_of_delivery", state);
                        data.add(parcel_deliver);
                    }
                    if (selected_date.equals(sender_date)){
                        parcel_sender.put("id", id);
                        parcel_sender.put("SSCC_number", custom_id);
                        parcel_sender.put("address_type", address_type_sender);
                        parcel_sender.put("address", address_sender);
                        parcel_sender.put("current_address", current_address);
                        parcel_sender.put("date", date_sender);
                        parcel_sender.put("state_of_delivery", state);
                        data.add(parcel_sender);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private String convertDate(String str_date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try
        {
            date = sdf.parse(str_date);
        } catch(Exception ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }
        sdf.applyPattern("yyyy-MM-dd");
        return sdf.format(date);
    }

    private List<JSONObject> sortRowsById(List<JSONObject> data){
        Collections.sort(data, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
                int compare = 0;
                try
                {
                    String str1 = jsonObjectA.getString("SSCC_number");
                    String str2 = jsonObjectB.getString("SSCC_number");
                    compare = str1.compareTo(str2);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
                return compare;
            }
        });
        return data;
    }

    private List<JSONObject> sortRowsByAddressType(List<JSONObject> data){
        Collections.sort(data, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
                int compare = 0;
                try
                {
                    String str1 = jsonObjectA.getString("address_type");
                    String str2 = jsonObjectB.getString("address_type");
                    compare = str1.compareTo(str2);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
                return compare;
            }
        });
        return data;
    }

    private List<JSONObject> sortRowsByAddress(List<JSONObject> data){
        Collections.sort(data, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
                int compare = 0;
                try
                {
                    String str1 = jsonObjectA.getString("address");
                    String str2 = jsonObjectB.getString("address");
                    compare = str1.compareTo(str2);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
                return compare;
            }
        });
        return data;
    }

    private List<JSONObject> sortRowsByCurLoc(List<JSONObject> data){
        Collections.sort(data, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
                int compare = 0;
                try
                {
                    String str1 = jsonObjectA.getString("current_address");
                    String str2 = jsonObjectB.getString("current_address");
                    compare = str1.compareTo(str2);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
                return compare;
            }
        });
        return data;
    }

    private List<JSONObject> sortRowsByDate(List<JSONObject> data){
        Collections.sort(data, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
                int compare = 0;
                try
                {
                    String str1 = jsonObjectA.getString("date_of_delivery");
                    String str2 = jsonObjectB.getString("date_of_delivery");
                    compare = str1.compareTo(str2);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
                return compare;
            }
        });
        return data;
    }

    private List<JSONObject> sortRowsByState(List<JSONObject> data){
        Collections.sort(data, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
                int compare = 0;
                try
                {
                    HashMap<String,Integer> delivery_states = new HashMap<String,Integer>();
                    delivery_states.put("Przesyłka zarejestrowana w systemie", 1);
                    delivery_states.put("Odebrana od nadawcy", 2);
                    delivery_states.put("W magazynie", 3);
                    delivery_states.put("Przygotowana do nadania", 4);
                    delivery_states.put("W trasie", 5);
                    delivery_states.put("W sortowni", 6);
                    delivery_states.put("W oddziale docelowym", 7);
                    delivery_states.put("W drodze do klienta", 8);
                    delivery_states.put("Doręczona", 9);
                    delivery_states.put("Awizo", 10);
                    delivery_states.put("Zwrócona do nadawcy", 11);
                    String str1 = jsonObjectA.getString("state_of_delivery");
                    String str2 = jsonObjectB.getString("state_of_delivery");
                    int valueA;
                    int valueB;
                    if(customStateOfDelivery(str1)){
                        valueA = 12;
                    } else {
                        valueA = delivery_states.get(str1);
                    }
                    if(customStateOfDelivery(str2)){
                        valueB = 12;
                    } else {
                        valueB = delivery_states.get(str2);
                    }
                    if (valueA == 12 && valueB == 12){
                        return compare = str1.compareTo(str2);
                    }
                    return compareIntegers(valueA, valueB);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
                return compare;
            }
        });
        return data;
    }

    private Boolean customStateOfDelivery(String state){
        if(state.equals("Przesyłka zarejestrowana w systemie") ||
                state.equals("Odebrana od nadawcy") ||
                state.equals("W magazynie") ||
                state.equals("Przygotowana do nadania") ||
                state.equals("W trasie") ||
                state.equals("W sortowni") ||
                state.equals("W oddziale docelowym") ||
                state.equals("W drodze do klienta") ||
                state.equals("Doręczona") ||
                state.equals("Awizo") ||
                state.equals("Zwrócona do nadawcy")) {
            return false;
        }
        return true;
    }

    private int compareIntegers(int x, int y){
        //because Integer.compare() call requires API level 19 (at minimum)
        if (x == y){
            return 0;
        } else {
            return x - y;
        }
    }

    private void clearTable(){
        TableRow header = (TableRow) findViewById(R.id.table_parcels_header);
        for(int i = table_parcels.getChildCount()-1; i >= 0; i--) {
            View view = table_parcels.getChildAt(i);
            if (view instanceof TableRow && !(view == header)) {
                TableRow row = (TableRow) view;
                table_parcels.removeView(row);
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

            Intent intent = new Intent(this, ParcelMenuActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    public class FindByDateOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;
        private String resultString;

        public FindByDateOperation(RestController restController){
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
                spinner_current_location.setVisibility(View.VISIBLE);
                String localizations[] = getLocalizationList(serverOutput);
                seedSpinnerWithLocalizations(localizations);
                scroll_table.setVisibility(View.VISIBLE);
                clearTable();
                updateData(serverOutput);
                data = getData();
                fillTableWithParcels();
            }
            else {
                Toast.makeText(getApplicationContext(), "Operacja zakończona niepowodzeniem!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public class FindByDateAndLocOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;
        private String resultString;

        public FindByDateAndLocOperation(RestController restController){
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
                scroll_table.setVisibility(View.VISIBLE);
                clearTable();
                updateData(serverOutput);
                data = getData();
                fillTableWithParcels();
            }
            else {
                Toast.makeText(getApplicationContext(), "Operacja zakończona niepowodzeniem!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateData(JSONObject serverOutput){
        this.serverOutput = serverOutput;
    }

    private String[] getLocalizationList(JSONObject serverOutput){
        JSONObject JSONlocs = null;
        String[] arrayLocs = null;
        try {
            JSONlocs = serverOutput.getJSONObject("localization_list");
            arrayLocs = new String[JSONlocs.length() + 1];
            arrayLocs[0] = "Lokalizacja aktualna";
            int i = 1;
            Iterator<String> iter = JSONlocs.keys();
            while (iter.hasNext()) {
                arrayLocs[i] = iter.next();
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayLocs;
    }

    private void seedSpinnerWithLocalizations(String localizations[]){
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, localizations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_current_location.setAdapter(adapter);
    }

    public class ResolveTSPOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;
        private String resultString;

        public ResolveTSPOperation(RestController restController){
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

            block_back_pressed = false;
            if(resultString.equals("true")){
                goToTSPResolveActivity(token, username, serverOutput);
            }
            else {
                button_resolve_tsp.setClickable(true);
                button_search_by_date.setClickable(true);
                spinner_current_location.setEnabled(true);
                spinner_current_location.setClickable(true);
                try {
                    String message = serverOutput.get("message").toString();
                    Toast.makeText(getApplicationContext(), message,
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void goToTSPResolveActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());

            Intent intent = new Intent(this, TSPResolveActivity.class);
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

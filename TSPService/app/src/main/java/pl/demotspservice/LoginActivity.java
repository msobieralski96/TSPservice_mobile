package pl.demotspservice;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private Button button_login_login;
    private EditText editText_login_name;
    private EditText editText_login_password;
    private Button url_forgot_password;
    private Button url_register;

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
        setContentView(R.layout.activity_login);

        block_back_pressed = false;

        baseUrl = "https://demotspservice.pl";
        urlResource = "api/login";
        urlPath = "";
        httpMethod = "POST";
        parameters = new HashMap<String, String>();
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Content-Type", "application/json");
        headerRequestFields.put("X-Requested-With", "XMLHttpRequest");

        editText_login_name = (EditText) findViewById(R.id.edit_text_login_name);
        editText_login_password = (EditText) findViewById(R.id.edit_text_login_password);

        button_login_login = (Button) findViewById(R.id.button_login_login);

        button_login_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    parameters.put("name", editText_login_name.getText().toString());
                    parameters.put("password", editText_login_password.getText().toString());

                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new LoginOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });

        url_forgot_password = (Button) findViewById(R.id.url_forgot_password);

        url_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("https://demotspservice.pl/password/reset/"));
                startActivity(intent);
            }
        });

        url_register = (Button) findViewById(R.id.url_register);

        url_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.action.VIEW",
                        Uri.parse("https://demotspservice.pl/register"));
                startActivity(intent);
            }
        });
    }

    public class LoginOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;
        private String resultString;
        private String token;
        private String username;

        public LoginOperation(RestController restController){
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
                token = serverOutput.get("access_token").toString();
                username = serverOutput.get("username").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //login success
            if(resultString.equals("true")){
                goToMenuActivity(token, username);
            }
            //login failure
            else {
                Toast.makeText(getApplicationContext(), "Próba logowania zakończona niepowodzeniem!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goToMenuActivity(String token, String username) {
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
    public void onBackPressed() {
        if(!block_back_pressed) {
            block_back_pressed = true;
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

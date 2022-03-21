package pl.demotspservice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfirmDeliveryActivity extends AppCompatActivity {

    Button button_clear_signature;
    Button button_confirm_signature;
    RelativeLayout layout_for_canvas;
    View view;
    SignatureView mSignature;
    Bitmap bitmap;

    private String token;
    private String username;
    private JSONObject serverOutput;
    private JSONObject TSPdata;

    private String baseUrl;
    private String urlResource;
    private String urlPath;
    private String httpMethod;
    private HashMap<String, String> parameters;
    private HashMap<String, String> headerRequestFields;

    private String id;
    private String SignatureAsBase64;
    private boolean block_back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_delivery);

        block_back_pressed = false;

        layout_for_canvas = (RelativeLayout) findViewById(R.id.layout_for_canvas);
        mSignature = new SignatureView(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);

        layout_for_canvas.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        button_clear_signature = (Button) findViewById(R.id.button_clear_signature);
        button_confirm_signature = (Button) findViewById(R.id.button_confirm_signature);
        button_confirm_signature.setEnabled(false);
        view = layout_for_canvas;
        button_clear_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignature.clear();
                button_confirm_signature.setEnabled(false);
            }
        });
        button_confirm_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (Build.VERSION.SDK_INT >= 23) {
                    isStoragePermissionGranted();
                } else {
                    saveSignature();
                }*/
                saveSignature();
                try {
                    SetParametersForEditRequest();
                    RestController restController =
                            new RestController(baseUrl, urlResource, urlPath,
                                    httpMethod, parameters, headerRequestFields);

                    AsyncTask<Void, Void, JSONObject> execute = new ConfirmDeliveryActivity.EditParcelOperation(restController);
                    execute.execute();
                } catch (Exception ex) {
                }
            }
        });

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

        SignatureAsBase64 = null;
    }

    /*public boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveSignature();
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveSignature();
        }
        else
        {
            Toast.makeText(this, "Zezwól aplikacji na dostęp do pamięci, żeby" +
                    " móc zatwierdzić przesyłkę", Toast.LENGTH_LONG).show();
        }
    }*/

    private void saveSignature(){
        view.setDrawingCacheEnabled(true);
        mSignature.save(view);
        //Toast.makeText(getApplicationContext(), "Zapisano pomyślnie", Toast.LENGTH_LONG).show();
        recreate();
    }

    private void SetParametersForEditRequest() {
        urlPath = id + "/edit";
        httpMethod = "POST";
        parameters = new HashMap<String, String>();
        parameters.put("signature", SignatureAsBase64);
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
                Toast.makeText(getApplicationContext(), "Zatwierdzono odbiór przesyłki",
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

                AsyncTask<Void, Void, JSONObject> execute = new ConfirmDeliveryActivity.ShowParcelMenuOperation(restController);
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

    public class SignatureView extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public SignatureView(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(layout_for_canvas.getWidth(),
                        layout_for_canvas.getHeight(),
                        Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                v.draw(canvas);

                bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                getSignatureAsBase64(byteArray);

                byteArrayOutputStream.close();
            } catch (Exception e) {
            }
        }

        public void clear() {
            path.reset();
            invalidate();
            button_confirm_signature.setEnabled(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            button_confirm_signature.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

    private void getSignatureAsBase64(byte[] byteArray){
        SignatureAsBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
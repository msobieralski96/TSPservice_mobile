package pl.demotspservice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public class ScanBarcodeActivity extends AppCompatActivity {

    private SurfaceView camera_preview;
    private CameraSource camera_source;
    private TextView code_name;
    private BarcodeDetector barcodeDetector;
    private Button button_set_facing;

    private String token;
    private String username;

    private String baseUrl;
    private String urlResource;
    private String urlPath;
    private String httpMethod;
    private HashMap<String, String> parameters;
    private HashMap<String, String> headerRequestFields;

    private static final int CAMERA_REQUEST_CODE = 100;
    private boolean block_detection;
    private boolean block_back_pressed;
    private int camera_facing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        Bundle extras = getIntent().getExtras();
        token = extras.getString("token");
        username = extras.getString("username");
        camera_facing = extras.getInt("camera_facing");

        block_back_pressed = false;

        camera_preview = (SurfaceView) findViewById(R.id.camera_preview);
        code_name = (TextView) findViewById(R.id.code_name);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.CODE_128).build();

        camera_source = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(128, 128)
                .setAutoFocusEnabled(true).setFacing(camera_facing).build();

        button_set_facing = (Button) findViewById(R.id.button_set_facing);

        button_set_facing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshActivity(true);
            }
        });

        if(Camera.getNumberOfCameras() < 2){
            button_set_facing.setClickable(false);
            button_set_facing.setVisibility(View.GONE);
        }

        baseUrl = "https://demotspservice.pl";
        urlResource = "api/parcels/bySSCC";
        httpMethod = "GET";
        parameters = new HashMap<String, String>();
        headerRequestFields = new HashMap<String, String>();
        headerRequestFields.put("Authorization", "Bearer " + token);

        block_detection = false;

        camera_preview.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ScanBarcodeActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                }
                try {
                    camera_source.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camera_source.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                if(!block_detection){
                    final SparseArray<Barcode> barCode = detections.getDetectedItems();

                    if(barCode.size()!=0){
                        code_name.post(new Runnable() {
                            @Override
                            public void run() {
                                block_detection = true;
                                Vibrator vibrator = (Vibrator) getApplicationContext()
                                        .getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(250);
                                urlPath = barCode.valueAt(0).displayValue.substring(2);//cut IZ 00
                                try {
                                    RestController restController =
                                            new RestController(baseUrl, urlResource, urlPath,
                                                    httpMethod, parameters, headerRequestFields);

                                    AsyncTask<Void, Void, JSONObject> execute = new ScanBarcodeActivity.GetParcelFromBarcodeOperation(restController);
                                    execute.execute();
                                } catch (Exception ex) {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    ex.printStackTrace(pw);
                                    String stringEx = sw.toString(); // stacktrace as a string
                                    code_name.setText("Wystąpił błąd:\n" + stringEx);
                                }
                                try {
                                    AsyncTask<Void, Void, String> detection_unlocker = new ScanBarcodeActivity.UnlockDetectionOperation();
                                    detection_unlocker.execute();
                                } catch (Exception ex) {
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private int setCameraFacing(){
        int numOfCameras = Camera.getNumberOfCameras();
        return (camera_facing + 1) % numOfCameras;
    }

    public class UnlockDetectionOperation extends AsyncTask<Void, Void, String> {

        public UnlockDetectionOperation(){}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            block_detection = false;
        }
    }

    public class GetParcelFromBarcodeOperation extends AsyncTask<Void, Void, JSONObject> {
        private RestController restController;
        private JSONObject serverOutput;
        private String resultString;

        public GetParcelFromBarcodeOperation(RestController restController){
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
                goToParcelMenuActivity(token, username, serverOutput);
            }
            else {
                code_name.setText("Nie znaleziono przesyłki o danym SSCC");
            }
        }
    }

    private void goToParcelMenuActivity(String token, String username, JSONObject serverOutput) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            bundle.putString("serverOutput", serverOutput.toString());
            bundle.putString("TSPdata", new JSONObject().toString());

            Intent intent = new Intent(this, ParcelMenuActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                refreshActivity(false);

            } else {
                onBackPressed();
                Toast.makeText(this,
                        "Zezwól aplikacji dostęp do aparatu, aby uzyskać dostęp do funkcji",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void refreshActivity(boolean changeCameraFacing) {
        if(!block_back_pressed) {
            block_back_pressed = true;
            camera_source.stop();
            Bundle bundle = new Bundle();
            bundle.putString("token", token);
            bundle.putString("username", username);
            if(changeCameraFacing) {
                bundle.putInt("camera_facing", setCameraFacing());
            } else {
                bundle.putInt("camera_facing", 0);
            }

            Intent intent = new Intent(this, ScanBarcodeActivity.class);
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

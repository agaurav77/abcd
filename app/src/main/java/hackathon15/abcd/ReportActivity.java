package hackathon15.abcd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ReportActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    ImageView imageView;
    Button button;
    Bitmap bitmap;
    SomeLocationListener someLocationListener;
    LocationManager locationManager;
    protected GoogleApiClient mgac;
    Location location;

    @Override
    public void onConnected(Bundle connectionHint) {
        location = LocationServices.FusedLocationApi.getLastLocation(mgac);
    }

    @Override
    public void onStart() {
        super.onStart();
        mgac.connect();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mgac.isConnected()) mgac.disconnect();
    }
    @Override
    public void onConnectionSuspended(int x) {mgac.connect();}
    @Override
    public void onConnectionFailed(ConnectionResult c) {}

    /* starting another image chooser and waiting for result */
    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    /* what to do when the image selected is returned via an intent */
    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == 1 && res == Activity.RESULT_OK) {
            /* reclaim memory on lower end devices */
            if (bitmap != null) bitmap.recycle();
            try {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(bitmap);
                if (!button.getText().equals(R.string.text_choose_another)) button.setText(R.string.text_choose_another);
            } catch (Exception e) {
                Toast t = Toast.makeText(this, "error_message", Toast.LENGTH_SHORT);
                t.setText("Unable to load image.");
                t.show();
            }
        }
    }

    /* send the image */
    public void sendReport(View v) {
        /* is network allowed ?? */
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || bitmap == null) {
            /* show a toast, we cannot use the internet right now */
            Toast t = Toast.makeText(this, "network_unavailable_message", Toast.LENGTH_SHORT);
            if (bitmap == null) t.setText("Please select an image.");
            else t.setText("Network Unavailable.");
            t.show();
            return;
            /* aka, do nothing */
        }
        /* network fine, so just try a FileSenderTask */
        new FileSenderTask().execute(bitmap);
    }

    /* what to do when ReportActivity is created */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        imageView = (ImageView) findViewById(R.id.imageview_chosen);
        button = (Button) findViewById(R.id.button);
        /* we'll use last used location instead of this for now, but it's possible to extend this
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    /* GPS is not enabled
                final AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                builder.setMessage("Please turn on your GPS for accuracy.").setCancelable(true);
                final AlertDialog alert = builder.create();
                alert.show();
            }
            someLocationListener = new SomeLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, someLocationListener);
        } catch (Exception e) {
            Log.d("[Map Error]", e.toString());
        }*/
        buildClient();
    }

    /* just build the Google API Client */
    protected synchronized void buildClient() {
        mgac = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /* inflate the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    /* action for menu options */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FileSenderTask extends AsyncTask<Bitmap, Void, String> {

        private ProgressDialog progressDialog;
        private int status = -1;

        /* show a spinner, that you're trying */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ReportActivity.this, "Sending file ...", "");
            progressDialog.setCancelable(false);
        }


        /* the real devil */
        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap toSend = params[0];
            String response = "";
            /* just simply get the coordinates, no complications for now */
            try {
                /* now comes the networking part */
                HTTPHelper httpHelper = new HTTPHelper();
                SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.prefs, 0);
                String id = sharedPreferences.getString("_id", "");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                toSend.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] buf = byteArrayOutputStream.toByteArray();
                String str = Base64.encodeToString(buf, Base64.DEFAULT);
                //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d("[Coordinates]",location.getLatitude()+","+location.getLongitude());
                response = httpHelper.post(LoginActivity.url + "users/report", "image=" + str + "&_id=" + id + "&lat=" + location.getLatitude() + "&long=" + location.getLongitude());
            } catch (Exception e) {
                Log.d("[TalkError]", e.toString());
            }
            return response;
        }

        /* stop the spinner, it's done !! */
        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            if (progressDialog.isShowing()) progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(res);
                if (jsonObject.get("success").toString().equals("true")) {
                    /* go to profile */
                    Intent i = new Intent(ReportActivity.this, ProfileActivity.class);
                    startActivity(i);
                } else {
                    Toast t = Toast.makeText(ReportActivity.this, "error_message", Toast.LENGTH_LONG);
                    t.setText("There was some problem.");
                    t.show();
                }
            } catch (Exception e) {
                Log.d("[Exception]", e.toString());
            }
        }

    }


}

package hackathon15.abcd;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

public class ReportActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    Bitmap bitmap;

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


    /* what to do when ReportActivity is created */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        imageView = (ImageView) findViewById(R.id.imageview_chosen);
        button = (Button) findViewById(R.id.button);
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
}

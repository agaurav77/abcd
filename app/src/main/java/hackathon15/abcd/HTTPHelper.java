package hackathon15.abcd;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class HTTPHelper {

    int response_code;
    boolean useHeader;

    HTTPHelper() {
        this.response_code = -1;
        this.useHeader = true;
    }

    HTTPHelper(boolean useHeader) {
        this.response_code = -1;
        this.useHeader = useHeader;
    }

    String post(String url, String data) throws Exception {
        byte[] postData = data.getBytes(Charset.forName("UTF-8"));
        URL urlobj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlobj.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        if (useHeader) conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setUseCaches(false);
        conn.getOutputStream().write(postData);
        Reader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        int ch = r.read();
        String response = "";
        while (ch != -1) {
            response += (char)ch;
            ch = r.read();
        }
        int status = conn.getResponseCode();
        response_code = status;
        return response;
    }

    String postImage(String url, Bitmap data, String id) throws Exception {
        URL urlobj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) urlobj.openConnection();
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
        conn.setRequestProperty("_id", id);
        DataOutputStream req = new DataOutputStream(conn.getOutputStream());
        req.writeBytes("--*****\r\n");
        req.writeBytes("Content-Disposition: form-data;\r\n");
        req.writeBytes("\r\n");
        byte[] buf = new byte[data.getWidth()*data.getHeight()];
        for (int i = 0; i < data.getWidth(); ++i) {
            for (int j = 0; j < data.getHeight(); ++j) {
                buf[i+j] = (byte) ((data.getPixel(i,j)&0x80) >> 7);
            }
        }
        req.write(buf);
        req.writeBytes("\r\n");
        req.writeBytes("--*****--\r\n");
        req.flush();
        req.close();
        Reader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        int ch = r.read();
        String response = "";
        while (ch != -1) {
            response += (char)ch;
            ch = r.read();
        }
        int status = conn.getResponseCode();
        response_code = status;
        return response;
    }

}

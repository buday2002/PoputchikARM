package com.dvl.arm.arm;

import android.app.Application;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

/**
 * Created by admin on 29.01.2017.
 */

public class POST extends AsyncTask<String,String, String> {

    @Override
    protected String doInBackground(String... strings) {
        final String Url =strings[0];
        final String param1 =strings[1];
        final String param2 = strings[2];    //названия параметров
        final String param3 = strings[3];
        final String param4 = strings[4];
        final String param5 = strings[5];

        final String value1 =strings[6];
        final String value2 = strings[7];    //значения параметров
        final String value3 = strings[8];
        final String value4 = strings[9];
        final String value5 = strings[10];

        String data ="";
        String Content="Error"; //ответ от сервера
        BufferedReader reader=null;
        // Create data variable for sent values to server


        URL url1 = null;
        try {
            url1 = new URL(Url);
            HttpURLConnection connection = (HttpURLConnection)url1.openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // все ок
                try {
                    if(!(param1.isEmpty()))
                    {
                        data = URLEncoder.encode(param1, "UTF-8")
                                + "=" + URLEncoder.encode(value1, "UTF-8");
                    }
                    if(!(param2.isEmpty())) {
                        data += "&" + URLEncoder.encode(param2, "UTF-8") + "="
                                + URLEncoder.encode(value2, "UTF-8");
                    }
                    if(!(param3.isEmpty())) {
                        data += "&" + URLEncoder.encode(param3, "UTF-8")
                                + "=" + URLEncoder.encode(value3, "UTF-8");
                    }

                    if(!(param4.isEmpty())) {
                        data += "&" + URLEncoder.encode(param4, "UTF-8")
                                + "=" + URLEncoder.encode(value4, "UTF-8");
                    }


                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Send data
                try
                {

                    // Defined URL  where to send data
                    URL url = new URL(Url);

                    // Send POST data request

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
                    conn.setRequestProperty("Accept-Encoding","gzip, deflate");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

                    //        Authenticator.setDefault(new Authenticator() {
                    //           protected PasswordAuthentication getPasswordAuthentication() {
                    //               return new PasswordAuthentication("rdl", "gfhjkm`1".toCharArray());
                    //          }
                    //      });


                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write( data );
                    wr.flush();

                    int responseCode = conn.getResponseCode();

                    // Get the server response

                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    String sbb ="";

                    Content = reader.toString();
                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        // Append server response in string
                        sbb=sbb+line;
                    }


                    Content = sbb;
                }
                catch(Exception ex)
                {
                }
                finally
                {
                    try
                    {

                        reader.close();
                    }

                    catch(Exception ex) {}
                }





            } else {
                // ошибка

            }
        } catch (IOException e) {
            e.printStackTrace();
            return Content;
        }


        return Content;

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

    }

}


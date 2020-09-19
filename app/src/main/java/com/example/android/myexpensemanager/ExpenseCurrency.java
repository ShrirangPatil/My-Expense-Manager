package com.example.android.myexpensemanager;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Currency exchange rate api by github.com/madisvain
 * open source project
 * https://github.com/exchangeratesapi/exchangeratesapi
 */
public class ExpenseCurrency {
    static private String TAG = ExpenseCurrency.class.getName();
    public static SharedPreferences mCurrencyChoicePref = null;
    static private String mRequestURL = "https://api.exchangeratesapi.io/latest?base=INR";
    public static void setBaseCurrency(String base) {
        base = "="+base;
        Log.i(TAG, mRequestURL);
        mRequestURL = mRequestURL.split("=")[0].concat(base);
        Log.i(TAG, mRequestURL);
    }
    static final Map<String, String> mCurrencyNameHash = new HashMap<String, String>() {
        {
            put("Australian dollar", "AUD");
            put("Bulgarian lev", "BGN");
            put("Brazilian real", "BRL");
            put("Canadian dollar", "CAD");
            put("Swiss franc", "CHF");
            put("Chinese yuan renminbi", "CNY");
            put("Czech koruna", "CZK");
            put("Danish krone", "DKK");
            put("Pound sterling", "GBP");
            put("Hong Kong dollar", "HKD");
            put("Croatian kuna", "HRK");
            put("Hungarian forint", "HUF");
            put("Indonesian rupiah", "IDR");
            put("Israeli shekel", "ILS");
            put("Indian rupee", "INR");
            put("Icelandic krona", "ISK");
            put("Japanese yen", "JPY");
            put("South Korean won", "KRW");
            put("Mexican peso", "MXN");
            put("Malaysian ringgit", "MYR");
            put("Norwegian krone", "NOK");
            put("New Zealand dollar", "NZD");
            put("Philippine peso", "PHP");
            put("Polish zloty", "PLN");
            put("Romanian leu", "RON");
            put("Russian rouble", "RUB");
            put("Swedish krona", "SEK");
            put("Singapore dollar", "SGD");
            put("Thai baht", "THB");
            put("Turkish lira", "TRY");
            put("US dollar", "USD");
            put("South African rand", "ZAR");
        }
    };
    static Map<String, Double> mCurrencyRatesHash = new HashMap<String, Double>();

    private ExpenseCurrency(){
    }

    /**
     * Creates Url obj from string obj
     * @param stringUrl Url string
     * @return Url object
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * reads from json stream and returns string
     * @param inputStream input stream obtained from api
     * @return string object of input stream
     * @throws IOException throws IOException
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * makes http request and gets the json response
     * @param url obtained url object
     * @return String object of json
     * @throws IOException throws IOException
     */
    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        if (url == null) {
            return jsonResponse;
        }
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(5000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    public  static String  getJsonString() {
        URL url = createUrl(mRequestURL);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
            e.printStackTrace();
        }
        if (jsonResponse != null) {
            Log.i(TAG, jsonResponse);
        } else {
            Log.d(TAG, "jsonResponce is null");
        }
        return jsonResponse;
    }

    public static void extractCurrencyRatesFromJson() {
        String jsonString = getJsonString();
        if (jsonString == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject ratesJsonObject = jsonObject.getJSONObject("rates");
            Iterator i = ratesJsonObject.keys();
            while (i.hasNext()) {
                String key = (String) i.next();
                mCurrencyRatesHash.put(key, ratesJsonObject.getDouble(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrencySymbol(String currencyName) {
        return "";
    }

    public static ArrayList<String> getCurrencyMatch(CharSequence subString) {
        ArrayList<String> matchedCurrency = new ArrayList<>();
        for (String c : mCurrencyNameHash.keySet()) {
            if ((c.toLowerCase()).contains(subString.toString().toLowerCase())) {
                matchedCurrency.add(c);
            }
        }
        return matchedCurrency;
    }

    public static double getConvertedRate(double currentCurrency, double newCurrency) {
        return 0;
    }
}

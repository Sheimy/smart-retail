package com.bongiovanni.smartretail;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashSet;
import java.util.Set;
import cz.msebera.android.httpclient.Header;

public class BeaconResultsActivity extends AppCompatActivity {

    private static String url = "http://172.20.10.4:4000/"; //my ipv4 address
    public static final Set<String> DEFAULT = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_results);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Surgelati");
            if(getIntent().getStringExtra("BeaconHash").equals("443575690")){
                actionBar.setTitle("Macelleria");
            }
        }

        final View progressBar2 = findViewById(R.id.progressBar2);
        String beaconHash = getIntent().getStringExtra("BeaconHash");
        new AsyncHttpClient().get(url+beaconHash, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                progressBar2.setVisibility(View.GONE);
                showResults(responseBody);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressBar2.setVisibility(View.GONE);
                Toast.makeText(BeaconResultsActivity.this, "Error to load info", Toast.LENGTH_LONG).show();
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    /** Retrieving products from the server **/
    private void showResults(JSONArray results) {
        if(results.length() <= 0) return;

        LinearLayout dynamicContent = findViewById(R.id.dynamic_product);
        dynamicContent.removeAllViews();

        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject product = results.getJSONObject(i);
                View newProductView = getLayoutInflater().inflate(R.layout.item_product, dynamicContent, false);

                //Compare the products associated with the beacon with those on the shopping list
                SharedPreferences sharedPreferences = getSharedPreferences("sharedList", MODE_PRIVATE);
                Set shoppingList = sharedPreferences.getStringSet("shoppingList", DEFAULT);
                for (Object temp : shoppingList) {
                    //Only products associated with the beacon that are on the shopping list will be displayed
                    if((product.getString("Name")).equals(temp.toString())){
                        ((TextView)newProductView.findViewById(R.id.product_name)).setText(product.getString("Name"));
                        ((TextView)newProductView.findViewById(R.id.product_category)).setText(product.getString("Category"));
                        dynamicContent.addView(newProductView);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }
}

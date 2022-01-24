package com.pmc.huawei;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.pmc.huawei.verification.beans.IdTokenEntity;
import com.pmc.huawei.verification.interfaces.IVerifyCallBack;
import com.pmc.huawei.verification.utils.IdTokenUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 2000;
//    private ArrayList<Item> arrayList;
    private RecyclerView recyclerView;
    private int current;
    private SharedPreferences preferences;
    private JSONArray jsonArray = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        BannerView bannerView = findViewById(R.id.hw_banner_view);
        bannerView.setAdId("testw6vs28auh3");
        bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
        bannerView.setBannerRefresh(60);
        bannerView.loadAd(new AdParam.Builder().build());
        IdTokenUtils idTokenUtils = new IdTokenUtils();
        IdTokenEntity idTokenEntity = idTokenUtils.decodeJsonStringFromIdtoken(preferences.getString("token",null));
        if (idTokenEntity != null) {
            Log.i("Token", idTokenEntity.toString() + "\n" + "exp:" + idTokenEntity.getExpTime());
            idTokenUtils.validateIdToken(preferences.getString("union",""), preferences.getString("token",""),
                    "105393881", idTokenEntity, new IVerifyCallBack() {
                        @Override
                        public void onSuccess() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("Token", "IdToken validate success\n" + "user infos:" + idTokenEntity.getPayloadJson());

                                }
                            });
                        }

                        @Override
                        public void onFailed(String errorMsg) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("Token", "IdToken validate failed\n" + errorMsg);
                                    finish();
                                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                }
                            });
                        }
                    });
        }else{
            finish();
            startActivity(new Intent(this,LoginActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        current = -1;

        AccountAuthService mAuthService = AccountAuthManager.getService(getApplicationContext(),
                new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .createParams());
        findViewById(R.id.SignOutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuthService.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        // Processing after the sign-out.
                        Log.i("Account", "signOut complete");
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                .putString("token",null).putString("open",null).apply();
                        finish();
                        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    }
                });
            }
        });
        findViewById(R.id.CancelAuthButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuthService.cancelAuthorization().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Processing after a successful authorization revoking.
                            Log.i("Account", "onSuccess: ");
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                    .putString("token",null).putString("union",null).putString("open",null).apply();
                            finish();
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        } else {
                            // Handle the exception.
                            Exception exception = task.getException();
                            if (exception instanceof ApiException){
                                Log.i("Account", "onFailure: " + ((ApiException) exception).getStatusCode());
                            }
                        }
                    }
                });
            }
        });
        findViewById(R.id.CreateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), ItemActivity.class),REQUEST_CODE);
            }
        });
        try {
            jsonArray = new JSONArray(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(preferences.getString("open","items"),""));
            Log.d("json",jsonArray.toString());
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject object = new JSONObject(jsonArray.getString(i));
//                arrayList.add(new Item(object.getString("string1"),object.getString("string2")));
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        arrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ItemAdapter(jsonArray, new ItemAdapter.OnItemClick() {
            @Override
            public void onClickView(int item) {
                try {
                    JSONObject object = new JSONObject(jsonArray.getString(item));
                    startActivityForResult(new Intent(getApplicationContext(), ItemActivity.class)
                            .putExtra("item",new Item(object.getString("string1"),object.getString("string2"))),REQUEST_CODE+1);
                    current = item;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClickButton(int item) {
//                arrayList.remove(item);
                jsonArray.remove(item);
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRemoved(item);
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString(preferences.getString("open","items"),jsonArray.toString()).apply();
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            Item item = (Item) data.getSerializableExtra("item");
            if(requestCode==REQUEST_CODE){
//                arrayList.add(item);
                jsonArray.put(item);
            }else if(requestCode==REQUEST_CODE+1){
                try {
//                    arrayList.get(current).setString1(item.getString1());
//                    arrayList.get(current).setString2(item.getString2());
                    jsonArray.put(current,item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putString(preferences.getString("open","items"),jsonArray.toString()).apply();
            Log.d("json",jsonArray.toString());
        }
    }

}
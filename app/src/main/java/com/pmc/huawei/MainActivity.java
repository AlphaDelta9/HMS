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
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.pmc.huawei.verification.beans.IdTokenEntity;
import com.pmc.huawei.verification.interfaces.IVerifyCallBack;
import com.pmc.huawei.verification.utils.IdTokenUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_AUTH = 1000;
    private static final int REQUEST_CODE_ITEM = 2000;
    private AccountAuthParams mAuthParam;
    private AccountAuthService mAuthService;
//    private ArrayList<Item> arrayList;
    private RecyclerView recyclerView;
    private int current;
    private SharedPreferences preferences;
    private JSONArray jsonArray;
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
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken().createParams();
        mAuthService = AccountAuthManager.getService(getApplicationContext(), mAuthParam);
        IdTokenUtils idTokenUtils = new IdTokenUtils();
        IdTokenEntity idTokenEntity = idTokenUtils.decodeJsonStringFromIdtoken(preferences.getString("token",null));
        if (idTokenEntity != null) {
            findViewById(R.id.HuaweiIdAuthButton).setVisibility(View.GONE);
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
            findViewById(R.id.SignOutButton).setVisibility(View.GONE);
            findViewById(R.id.CancelAuthButton).setVisibility(View.GONE);
            findViewById(R.id.HuaweiIdAuthButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(mAuthService.getSignInIntent(), REQUEST_CODE_AUTH);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        current = -1;

//        AccountAuthService mAuthService = AccountAuthManager.getService(getApplicationContext(),
//                new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
//                .createParams());
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
                        finish();jsonArray=new JSONArray();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
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
                            finish();jsonArray=new JSONArray();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
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
                startActivityForResult(new Intent(getApplicationContext(), ItemActivity.class),REQUEST_CODE_ITEM);
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
                            .putExtra("item",new Item(object.getString("string1"),object.getString("string2"))),REQUEST_CODE_ITEM+1);
                    current = item;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClickButton(int item) {
//                arrayList.remove(item);
                jsonArray.remove(item);
                recyclerView.getAdapter().notifyItemRemoved(item);
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString(preferences.getString("open","items"),jsonArray.toString()).apply();
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            if (requestCode == REQUEST_CODE_AUTH) {
                Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
                if (authAccountTask.isSuccessful()) {
                    // The sign-in is successful, and the user's ID information and ID token are obtained.
                    AuthAccount authAccount = authAccountTask.getResult();
                    Log.i("Account", "idToken:" + authAccount.getIdToken());
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("token",authAccount.getIdToken())
                            .putString("union",authAccount.getUnionId()).putString("open",authAccount.getOpenId()).apply();
                    jsonArray=new JSONArray();
                    finish();
                    startActivity(new Intent(this,MainActivity.class));
                } else {
                    // The sign-in failed. No processing is required. Logs are recorded for fault locating.
                    Log.e("Account", "sign in failed : " +((ApiException) authAccountTask.getException()).getStatusCode());
                }
            }else
            if(requestCode==REQUEST_CODE_ITEM){
//                arrayList.add(item);
                jsonArray.put((Item) data.getSerializableExtra("item"));
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putString(preferences.getString("open","items"),jsonArray.toString()).apply();
                Log.d("json",jsonArray.toString());
            }else if(requestCode==REQUEST_CODE_ITEM+1){
                try {
                    Item item = (Item) data.getSerializableExtra("item");
//                    arrayList.get(current).setString1(item.getString1());
//                    arrayList.get(current).setString2(item.getString2());
                    jsonArray.put(current,item);
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                            .putString(preferences.getString("open","items"),jsonArray.toString()).apply();
                    Log.d("json",jsonArray.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
package com.pmc.huawei;

import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

public class ItemActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        findViewById(R.id.ScanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemActivity.this.requestPermissions(
                        new String[]{android.Manifest.permission.CAMERA,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
        });
        if(getIntent().hasExtra("item")){
            EditText title = findViewById(R.id.EditTextTitle);
            EditText multi = findViewById(R.id.EditTextMulti);
            Item item = (Item) getIntent().getSerializableExtra("item");
            title.setText(item.getString1());
            multi.setText(item.getString2());
            findViewById(R.id.SaveButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText title = findViewById(R.id.EditTextTitle);
                    EditText multi = findViewById(R.id.EditTextMulti);
                    if(!title.getText().toString().isEmpty()||!multi.getText().toString().isEmpty())
                    setResult(REQUEST_CODE+1,new Intent().putExtra("item",new Item(
                            title.getText().toString(),multi.getText().toString()
                    )));
                    finish();
                }
            });
        }else{
            findViewById(R.id.SaveButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText title = findViewById(R.id.EditTextTitle);
                    EditText multi = findViewById(R.id.EditTextMulti);
                    if(!title.getText().toString().isEmpty()||!multi.getText().toString().isEmpty())
                    setResult(REQUEST_CODE,new Intent().putExtra("item",new Item(
                            title.getText().toString(),multi.getText().toString()
                    )));
                    finish();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE && grantResults[0] != PackageManager.PERMISSION_GRANTED && grantResults[1] != PackageManager.PERMISSION_GRANTED){
            return;
        }
        ScanUtil.startScan(this, REQUEST_CODE, new HmsScanAnalyzerOptions.Creator()
                .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE).create());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE){
//            ArrayList<String> strings = data.getStringArrayListExtra("scan");
//            if(!strings.get(0).isEmpty()){
//                ((EditText)findViewById(R.id.EditTextTitle)).setText(strings.get(0));
//            }
//            ((EditText)findViewById(R.id.EditTextMulti)).setText(strings.get(1));
            if(data!=null){
                HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
                if (!obj.getOriginalValue().isEmpty()) {
//                Toast.makeText(this, obj.getOriginalValue(),
//                        Toast.LENGTH_SHORT).show();
                    EditText editText = findViewById(R.id.EditTextMulti);
                    if(obj.getScanTypeForm()==HmsScan.WIFI_CONNECT_INFO_FORM){
                        editText.setText(String.format("%s\nSSID: %s\nPassword: %s", editText.getText().toString(), obj.getWiFiConnectionInfo().getSsidNumber(), obj.getWiFiConnectionInfo().getPassword()));
                    }else if(obj.getScanTypeForm()==HmsScan.URL_FORM){
                        editText.setText(String.format("%s\n%s", editText.getText().toString(), obj.getLinkUrl().getLinkValue()));
                    }else{
                        editText.setText(String.format("%s\n%s", editText.getText().toString(), obj.originalValue));
                    }
                }
//                Item item = (Item) data.getSerializableExtra("scan");
//                if(!item.getString1().isEmpty()){
//                    ((EditText)findViewById(R.id.EditTextTitle)).setText(item.getString1());
//                }
//                EditText editText = findViewById(R.id.EditTextMulti);
//                editText.setText(String.format("%s\n%s", editText.getText().toString(), item.getString2()));
            }
        }
    }
}
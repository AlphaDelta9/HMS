package com.pmc.huawei;

import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;

import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.text.MLLocalTextSetting;
import com.huawei.hms.mlsdk.text.MLText;
import com.huawei.hms.mlsdk.text.MLTextAnalyzer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ItemActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 2000;
    private EditText title;
    private EditText multi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        title = findViewById(R.id.EditTextTitle);
        multi = findViewById(R.id.EditTextMulti);
        findViewById(R.id.ScanButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemActivity.this.requestPermissions(
                        new String[]{android.Manifest.permission.CAMERA,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
        });
        findViewById(R.id.ExportButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ItemActivity.this, BarcodeActivity.class).putExtra("barcode",new Item(title.getText().toString(),multi.getText().toString())));
            }
        });
        if(getIntent().hasExtra("item")){
            Item item = (Item) getIntent().getSerializableExtra("item");
            title.setText(item.getString1());
            multi.setText(item.getString2());
            findViewById(R.id.SaveButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
        if(requestCode==REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            ScanUtil.startScan(this, REQUEST_CODE, new HmsScanAnalyzerOptions.Creator()
                    .setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
        }else
        if(requestCode==REQUEST_CODE-1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),REQUEST_CODE-1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE&&data!=null){
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
                        try {
                            JSONObject object = new JSONObject(obj.originalValue);
                            title.setText(object.getString("string1"));
                            multi.setText(object.getString("string2").replace("[ASCII10]","\n"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            editText.setText(String.format("%s\n%s", editText.getText().toString(), obj.originalValue));
                        }
                    }
                }
//                Item item = (Item) data.getSerializableExtra("scan");
//                if(!item.getString1().isEmpty()){
//                    ((EditText)findViewById(R.id.EditTextTitle)).setText(item.getString1());
//                }
//                EditText editText = findViewById(R.id.EditTextMulti);
//                editText.setText(String.format("%s\n%s", editText.getText().toString(), item.getString2()));
        }else
            if(requestCode==REQUEST_CODE-1&&data!=null){
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),data.getData());
                    MLLocalTextSetting setting = new MLLocalTextSetting.Factory()
                            .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                            .setLanguage("en")
                            .create();
                    MLTextAnalyzer analyzer = MLAnalyzerFactory.getInstance()
                            .getLocalTextAnalyzer(setting);
                    MLFrame frame = MLFrame.fromBitmap(bitmap);
                    Task<MLText> task = analyzer.asyncAnalyseFrame(frame);
                    task.addOnSuccessListener(new OnSuccessListener<MLText>() {
                        @Override
                        public void onSuccess(MLText text) {
                            // Recognition success.
                            String result = "";
                            List<MLText.Block> blocks = text.getBlocks();
                            for (MLText.Block block : blocks) {
                                for (MLText.TextLine line : block.getContents()) {
                                    result += line.getStringValue() + "\n";
                                }
                            }
                            title.setText(result);
                        }
                    });
                    analyzer.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
}
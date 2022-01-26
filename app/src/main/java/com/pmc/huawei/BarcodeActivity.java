package com.pmc.huawei;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;
import com.pmc.huawei.Item;

public class BarcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        Item item = (Item) getIntent().getSerializableExtra("barcode");
        HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().create();
        try {
            Bitmap bitmap = ScanUtil.buildBitmap(item.toString().replace("\n","[ASCII10]"), HmsScan.DATAMATRIX_SCAN_TYPE, 400, 400, options);
            ((ImageView)findViewById(R.id.imageView)).setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        findViewById(R.id.CloseButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
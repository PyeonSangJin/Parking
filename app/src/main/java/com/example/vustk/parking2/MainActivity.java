package com.example.vustk.parking2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnPayment = (Button) findViewById(R.id.btnPayment);
    Bluetooth bt = new Bluetooth();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPayment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        getApplicationContext(), // 현재 화면의 제어권자
                        Charge.class); // 다음 넘어갈 클래스 지정
                startActivity(intent); // 다음 화면으로 넘어간다
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
        });

        bt.runToBt();
        bt.selectDevice();
    }
}

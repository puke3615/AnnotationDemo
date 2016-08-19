package com.puke.annotationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@Bind(R.layout.activity_main)
public class MainActivity extends Activity {

    @Bind(R.id.username)
    private EditText mUsername;
    @Bind(R.id.submit)
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BindHandler.handleBind(this);
    }

    @Bind(R.id.submit)
    public void submit() {
        String username = mUsername.getText().toString().trim();
        Toast.makeText(MainActivity.this, username, Toast.LENGTH_SHORT).show();
    }

}

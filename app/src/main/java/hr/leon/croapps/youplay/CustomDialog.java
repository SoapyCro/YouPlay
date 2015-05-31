package hr.leon.croapps.youplay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    private Activity c;
    private Item item;
    public CustomDialog(Activity a, Item item) {
        super(a);
        this.c = a;
        this.item = item;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setContentView(R.layout.custom_dialog);
        Button yes = (Button) findViewById(R.id.btn_yes);
        Button no = (Button) findViewById(R.id.btn_no);
        EditText text = (EditText) findViewById(R.id.startAt);

        if(text.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        EditText text = (EditText) findViewById(R.id.startAt);
        switch (v.getId()) {
            case R.id.btn_yes:
                Intent intent = new Intent(c, PlayerActivity.class);
                intent.putExtra("id", item.getId());
                String sec = text.getText().toString();
                intent.putExtra("startTime", sec);
                c.startActivity(intent);
                break;
            case R.id.btn_no:
                InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
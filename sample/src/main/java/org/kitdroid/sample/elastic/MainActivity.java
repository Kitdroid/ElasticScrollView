package org.kitdroid.sample.elastic;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.kitdroid.widget.ElasticScrollView;

public class MainActivity extends ActionBarActivity implements OnSeekBarChangeListener {

    private ElasticScrollView sv;
    private ImageView iv;
    private SeekBar sb;
    private TextView tvType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sv = (ElasticScrollView) findViewById(R.id.sv);
        iv = (ImageView)findViewById(R.id.iv);

        tvType = (TextView) findViewById(R.id.text_elastic_type);

        sb = (SeekBar) findViewById(R.id.seekBar1);
        sb.setOnSeekBarChangeListener(this);
        resetDamk();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_set_type:{
                changeElasticType();
                break;
            }
            case R.id.action_reset_damk:{
                resetDamk();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetDamk() {
        sb.setProgress(10);
    }

    private void changeElasticType() {
        if(sv.getElasticView() == null){
            sv.setElasticView(iv);
            tvType.setText("Elastic Type: ONE");
        }else{
            sv.setElasticView(null);
            tvType.setText("Elastic Type: ALL");
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        sv.setDamk(progress/10 +1);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) { }

}

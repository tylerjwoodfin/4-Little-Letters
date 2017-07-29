package tylerwoodfin.com.fourlittleletters;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TitleActivity extends ActionBarActivity
{
    Typeface font;
    TextView title;
    TextView text_copyright;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_title);

        //Initialize items
        title = (TextView) findViewById(R.id.title);
        startButton = (Button) findViewById(R.id.start_button);
        text_copyright = (TextView) findViewById(R.id.text_copyright);

        //Set font
        font = Typeface.createFromAsset(getAssets(),  "fonts/visitor1.ttf");

        title.setTypeface(font);
        startButton.setTypeface(font);
        text_copyright.setTypeface(font);

        //Button listener
        startButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.start);
                mp.start();
                start();
                finish();
            }
        });
    }

    public void start()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

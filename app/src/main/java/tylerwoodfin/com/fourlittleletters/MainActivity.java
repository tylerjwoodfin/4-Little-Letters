package tylerwoodfin.com.fourlittleletters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Random;


public class MainActivity extends ActionBarActivity
{

    String[] words;
    String wordString;
    String startWord;
    String endWord = "";
    TextView selectedLetter;
    TextView letter1;
    TextView letter2;
    TextView letter3;
    TextView letter4;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView helloWorld = (TextView) findViewById(R.id.MainText);
        selectedLetter = (TextView) findViewById(R.id.letter1);

        letter1 = (TextView) findViewById(R.id.letter1);
        letter2 = (TextView) findViewById(R.id.letter2);
        letter3 = (TextView) findViewById(R.id.letter3);
        letter4 = (TextView) findViewById(R.id.letter4);

        wordString = loadAllWords("words.txt");
        words = wordString.split("\\s+");

        startWord = words[(new Random()).nextInt(words.length)];

        while(endWord.equals(startWord) || endWord.equals(""))  //On the very low chance endWord is the same as startWord
            endWord = words[(new Random()).nextInt(words.length)];

        helloWorld.setText(endWord);
        //toast("Transform " + startWord + " to " + endWord);

        letter1.setText(Character.toString(startWord.charAt(0)));
        letter2.setText(Character.toString(startWord.charAt(1)));
        letter3.setText(Character.toString(startWord.charAt(2)));
        letter4.setText(Character.toString(startWord.charAt(3)));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v)
    {
        selectedLetter.setTextColor(Color.WHITE);   //Clear initial selection

        selectedLetter = (TextView) findViewById(v.getId());
        selectedLetter.setTextColor(Color.RED);

        //Show keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        }

        //Hide Keyboard
        /*
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
         */
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        String old = selectedLetter.getText().toString();
        String c = Character.toString((char)event.getUnicodeChar());
        String newWord = "";
        c = c.toUpperCase();
        selectedLetter.setText(c);

        newWord += (letter1.getText().toString());
        newWord += (letter2.getText().toString());
        newWord += (letter3.getText().toString());
        newWord += (letter4.getText().toString());

        if(!isValidWord(newWord)) {
            selectedLetter.setText(old);
            toast(newWord + " is not a valid word.");
        }

        return true;
    }

    public void toast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public String loadAllWords(String filename)
    {
        String w = "FILE_ERROR";
        try {
            AssetManager assManager = getApplicationContext().getAssets();
            InputStream inputStream = assManager.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            w = reader.readLine();
        }
        catch(Exception e)
        {
            toast("Stupid Error");
        }
        return w;
    }

    public boolean isValidWord(String word)
    {
        if(wordString.contains(word))
            return true;
        return false;
    }
}

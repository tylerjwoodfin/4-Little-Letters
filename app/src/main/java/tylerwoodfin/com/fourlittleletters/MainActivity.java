package tylerwoodfin.com.fourlittleletters;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;


public class MainActivity extends ActionBarActivity
{
    private InterstitialAd mInterstitialAd;
    HashSet<String> dictionary;
    ArrayList<String> solution;
    String[] words;
    ImageView refresh;
    ImageView bomb;
    ImageView music;
    String wordString;
    String startWord = "";
    String endWord = "";
    Button bombButton;
    Button musicButton;
    TextView selectedLetter;
    TextView endWordText;
    TextView scoreTextView;
    TextView hintText;
    TextView letter1;
    TextView letter2;
    TextView letter3;
    TextView letter4;
    TextView tutorial_your_word;
    TextView tutorial_tap_a;
    TextView tutorial_type_i;
    Random r;
    int score;
    int soundYes;
    int soundNo;
    int soundScoreUp;
    int soundBomb;
    int soundMusic;
    Context context;
    Typeface font;// custom dialog
    Dialog dialog_bomb;
    Dialog dialog_music;
    SoundPool ourSounds;
    boolean isMusicOn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        r = new Random();
        context = this;
        dialog_bomb = new Dialog(context);
        dialog_music = new Dialog(context);
        refresh = (ImageView) findViewById(R.id.refresh);
        bomb = (ImageView) findViewById(R.id.bomb);
        bombButton = (Button) dialog_bomb.findViewById(R.id.dialogButtonOK);
        music = (ImageView) findViewById(R.id.music);
        musicButton = (Button) dialog_music.findViewById(R.id.dialogButtonOK);
        endWordText = (TextView) findViewById(R.id.MainText);
        selectedLetter = (TextView) findViewById(R.id.letter1);
        scoreTextView = (TextView) findViewById(R.id.scoreText);
        hintText = (TextView) findViewById(R.id.bombHints);
        hintText.setText("");
        scoreTextView.setText(getScore() + "");

        letter1 = (TextView) findViewById(R.id.letter1);
        letter2 = (TextView) findViewById(R.id.letter2);
        letter3 = (TextView) findViewById(R.id.letter3);
        letter4 = (TextView) findViewById(R.id.letter4);

        wordString = loadAllWords("words.txt");
        words = wordString.split("\\s+");

        //load dictionary
        dictionary = new HashSet<>(Arrays.asList(words));

        //Set font
        font = Typeface.createFromAsset(getAssets(),  "fonts/visitor1.ttf");
        endWordText.setTypeface(font);
        scoreTextView.setTypeface(font);
        hintText.setTypeface(font);
        letter1.setTypeface(font);
        letter2.setTypeface(font);
        letter3.setTypeface(font);
        letter4.setTypeface(font);

        //Set audio
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        ourSounds = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        soundYes = ourSounds.load(this, R.raw.yes, 1);
        soundNo = ourSounds.load(this, R.raw.no, 1);
        soundScoreUp = ourSounds.load(this, R.raw.score_up, 1);
        soundBomb = ourSounds.load(this, R.raw.bomb, 1);
        soundMusic = ourSounds.load(this, R.raw.music, 1);

        //Set letters
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultStart = "BALL";
        String defaultEnd = "BILL";
        startWord = sharedPref.getString(getString(R.string.startWord), defaultStart);
        endWord = sharedPref.getString(getString(R.string.endWord), defaultEnd);
        loadNewWords(startWord, endWord);

        //Set Tutorial
        if(getCurrentRound() < 2)
        {
            tutorial(0);
        }
        else
            tutorial(getCurrentRound());

        //Refresh Listener
        refresh.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                loadNewWords(true);
            }
        });

        //Ad setup
        MobileAds.initialize(this, "ca-app-pub-6931519863143232~5066617712");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6931519863143232/2676435703");
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("1C5679C20747B8A9ED54759D5C94F085")
                .build());

        //TEMPORARY MainText Listener
        endWordText.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                //toast(solution.toString());
            }
        });

        //Bomb listener
        bomb.setOnClickListener(new View.OnClickListener()
        {
            String bombDescriptionText = "Bombs reveal up to 3 hint words for 30 points.";
            String bombText = "Bomb!";

            @Override
            public void onClick(View arg0)
            {

                dialog_bomb.setContentView(R.layout.bomb_dialog);
                dialog_bomb.setTitle("Title...");

                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog_bomb.findViewById(R.id.text);
                text.setText(bombDescriptionText);
                text.setTypeface(font);

                bombButton = (Button) dialog_bomb.findViewById(R.id.dialogButtonOK);
                bombButton.setTypeface(font);

                //checks if the bomb hasn't already been used this round
                if(hintText.length() > 0 || getScore() < 30)
                {
                    if(hintText.length() > 0)
                    {
                        bombDescriptionText = "You've already used the bomb this round.";
                        text.setText(bombDescriptionText);
                    }
                    else
                    {
                        switch(getScore())
                        {
                            case 9:
                                bombText = "Just 1 more point needed!";
                                break;
                            default:
                                bombText = 30 - getScore() + " more points needed!";
                                break;
                        }
                    }
                    bombButton.setText(bombText);
                    bombButton.setEnabled(false);
                }

                //Bomb Button listener
                bombButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ourSounds.play (soundBomb, 0.9f, 0.9f, 1, 0, 1);

                        try
                        {
                            hintText.setText(solution.get(solution.size() - 4) + ", " +
                                    solution.get(solution.size() - 3) + ", " +
                                    solution.get(solution.size() - 2));
                        }
                        catch(Exception e)
                        {
                            try
                            {
                                hintText.setText(solution.get(solution.size() - 3) + ", " +
                                        solution.get(solution.size() - 2));
                            }
                            catch(Exception f)
                            {
                                String justOneLetter = "Just change one letter!";
                                hintText.setText(justOneLetter);
                            }
                        }
                        setScore(getScore() - 30);
                        dialog_bomb.dismiss();
                    }
                });

                Button cancelButton = (Button) dialog_bomb.findViewById(R.id.button_cancel);
                cancelButton.setTypeface(font);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_bomb.dismiss();
                    }
                });

                dialog_bomb.show();
            }
        });

        //Music Listener
        music.setOnClickListener(new View.OnClickListener() {

            String musicDescriptionText = "Music can replace any letter with any other" +
                    " letter. Make a word up, like Dr. Seuss, for 10 points.";
            String musicText = "Get Creative";

            @Override
            public void onClick(View arg0)
            {

                dialog_music.setContentView(R.layout.bomb_dialog);
                dialog_music.setTitle("Title...");

                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog_music.findViewById(R.id.text);
                text.setText(musicDescriptionText);
                text.setTypeface(font);

                musicButton = (Button) dialog_music.findViewById(R.id.dialogButtonOK);
                musicButton.setTypeface(font);

                //checks if the bomb hasn't already been used this round
                if(getScore() < 10)
                {
                    switch(getScore())
                    {
                        case 9:
                            musicText = "Just 1 more point needed!";
                            break;
                        default:
                            musicText = 10 - getScore() + " more points needed!";
                            break;
                    }

                    musicButton.setEnabled(false);
                }
                else
                    musicText = "Get creative!";

                //Bomb Button listener
                musicButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ourSounds.play (soundMusic, 0.9f, 0.9f, 1, 0, 1);

                        try
                        {
                            //toast("Making Music");
                            selectedLetter.setTextColor(Color.MAGENTA);
                            isMusicOn = true;
                        }
                        catch(Exception e)
                        {
                            toast("I can't make music yet! Here's a refund. Tell the developer!");
                            setScore(getScore() + 10);
                        }
                        setScore(getScore() - 10);
                        dialog_music.dismiss();
                    }
                });

                Button cancelButton = (Button) dialog_music.findViewById(R.id.button_cancel);
                cancelButton.setTypeface(font);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_music.dismiss();
                    }
                });

                musicButton.setText(musicText);
                dialog_music.show();
            }
        });

    }

    public void loadNewWords(boolean refreshButton)
    {
        startWord = "LLLL";
        endWord = "FFFF";

        //use current round, if the refresh button was not clicked
        int currentRound = getCurrentRound();
        if(refreshButton)
        {
            currentRound = -1;
        }

        switch(currentRound)
        {
            case 2:
                startWord = "CODE";
                endWord = "CODS";
                break;
            case 3:
                startWord = "HATE";
                endWord = "DOTS";
                break;
            case 4:
                startWord = "FIVE";
                endWord = "FADS";
                break;
            case 5:
                startWord = "SAME";
                endWord = "COST";
                break;
            case 6:
                startWord = "WORD";
                endWord = "LAIR";
                break;
            case 7:
                startWord = "BATH";
                endWord = "TUBS";
                break;
            case 8:
                startWord = "SLOW";
                endWord = "DOWN";
                break;
            case 9:
                startWord = "NICE";
                endWord = "DIMS";
                break;
            case 10:
                startWord = "TENS";
                endWord = "BENT";
                break;
            case 11:
                startWord = "CAKE";
                endWord = "LIMP";
                break;
            default:
                while((solution = transform(startWord, endWord, dictionary)).isEmpty())
                {
                    //toast("Testing: " + startWord +", " + endWord + ": " + solution.toString());
                    startWord = words[r.nextInt(words.length)];
                    endWord = words[r.nextInt(words.length)];

                    while(endWord.equals(startWord) || endWord.equals(""))
                    //On the very low chance endWord is the same as startWord
                    {
                        endWord = words[r.nextInt(words.length)];
                    }
                }
        }

        loadNewWords(startWord, endWord);

        if(getCurrentRound() % 3 == 0)
        {
            mInterstitialAd.show();
        }
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    public void loadNewWords(String startWord, String endWord)
    {
        solution = transform(startWord, endWord, dictionary);

        String text = getCurrentRound() + ": " + endWord;
        endWordText.setText(text);
        hintText.setText("");
        try
        {
            bombButton.setEnabled(true);
        }
        catch(Exception e)
        {
            //toast(e.toString());
        }

        letter1.setText(Character.toString(startWord.charAt(0)));
        letter2.setText(Character.toString(startWord.charAt(1)));
        letter3.setText(Character.toString(startWord.charAt(2)));
        letter4.setText(Character.toString(startWord.charAt(3)));

        saveWords();

        if(getCurrentRound() < 2)
        {
            tutorial(1);
        }
        else
            tutorial(getCurrentRound());
    }

    public void setScore(int i)
    {
        score = i;
        try {
            String scoreTextViewText = score + "";
            scoreTextView.setText(scoreTextViewText);
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.score), score);
            editor.apply();
        }
        catch(Exception e)
        {
            toast(e.toString());
        }
    }

    public int getScore()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int defaultValue = 0;
        return sharedPref.getInt(getString(R.string.score), defaultValue);
    }

    public void saveWords()
    {
        //save words permanently
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.startWord), startWord);
        editor.putString(getString(R.string.endWord), endWord);
        editor.apply();
    }

    public int getCurrentRound()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        int defaultValue = 1;
        return sharedPref.getInt(getString(R.string.currentRound), defaultValue);
    }

    public void setIncreaseCurrentRound()
    {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.currentRound), getCurrentRound() + 1);
        editor.apply();
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

        if(!isMusicOn)
            selectedLetter.setTextColor(Color.RED);
        else
            selectedLetter.setTextColor(Color.MAGENTA);

        //Show keyboard
        InputMethodManager imm;

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);

        }

        //tutorial handling
        if(getCurrentRound() < 2 && selectedLetter.getText().equals("A"))
        {
            tutorial(1);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            return false;
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            System.exit(0);
        }
        String old = selectedLetter.getText().toString();
        String c = Character.toString((char)event.getUnicodeChar());
        String newWord = "";
        c = c.toUpperCase();
        selectedLetter.setText(c);

        newWord += (letter1.getText().toString());
        newWord += (letter2.getText().toString());
        newWord += (letter3.getText().toString());
        newWord += (letter4.getText().toString());

        boolean validWord = isValidWord(newWord);

        if(!validWord && !isMusicOn) //invalid, no music power
        {
            selectedLetter.setText(old);
            ourSounds.play (soundNo, 0.9f, 0.9f, 1, 0, 1);
        }
        else if(!newWord.equals(endWord)) //valid xor music , and not matching
        {
            ourSounds.play (soundYes, 0.9f, 0.9f, 1, 0, 1);
        }
        else    //matching words- next round
        {
            ourSounds.play (soundScoreUp, 0.9f, 0.9f, 1, 0, 1);
            int points = solution.size();
            setScore(getScore() + points);
            setIncreaseCurrentRound();
            loadNewWords(false);

            int currentRound = getCurrentRound();
            String text = currentRound + ": " + endWord;
            endWordText.setText(text);
        }

        startWord = newWord;
        saveWords();
        isMusicOn = false;
        selectedLetter.setTextColor(Color.RED);
        return true;
    }

    public void tutorial(int part)
    {
        tutorial_your_word = (TextView) findViewById(R.id.tutorial_your_word);
        tutorial_tap_a = (TextView) findViewById(R.id.tutorial_tap_a);
        tutorial_type_i = (TextView) findViewById(R.id.tutorial_type_i);

        //font
        tutorial_your_word.setTypeface(font);
        tutorial_tap_a.setTypeface(font);
        tutorial_type_i.setTypeface(font);

        if(part == 0)
        {
            tutorial_your_word.setVisibility(View.VISIBLE);
            tutorial_tap_a.setVisibility(View.VISIBLE);
            tutorial_type_i.setVisibility(View.INVISIBLE);
        }
        else if(part == 1)
        {
            tutorial_your_word.setVisibility(View.INVISIBLE);
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.VISIBLE);
        }
        else if(part == 2)
        {
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.VISIBLE);
            tutorial_your_word.setVisibility(View.INVISIBLE);
            tutorial_type_i.setText("Cool! Now try another one. We want to turn CODE into CODS.");
        }
        else if(part == 3)
        {
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.VISIBLE);
            tutorial_your_word.setVisibility(View.INVISIBLE);
            tutorial_type_i.setText("The hard part: when you change a letter, it has to make a word." +
                    " You can turn HATE to HAVE, but not to HAXE, because that's not a word. Make " +
                    "DOTS.");
        }
        else if(part == 4)
        {
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.VISIBLE);
            tutorial_your_word.setVisibility(View.INVISIBLE);
            tutorial_type_i.setText("You get a few points each round. Harder levels" +
                    " are worth more points. Look at the top: you have " + score + " points!");
        }
        else if(part == 5)
        {
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.VISIBLE);
            tutorial_your_word.setVisibility(View.INVISIBLE);
            tutorial_type_i.setText("Spend your points on power-ups (at the bottom!) to get" +
                    " through tricky levels.");
        }
        else if(part == 6)
        {
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.VISIBLE);
            tutorial_your_word.setVisibility(View.INVISIBLE);
            tutorial_type_i.setText("If you want a new set of words, tap the blue refresh " +
                    "button at the top! You can do this as many times as you want- no penalty!");
        }
        else if(part == 7)
        {
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.VISIBLE);
            tutorial_your_word.setVisibility(View.INVISIBLE);
            tutorial_type_i.setText("I think you're getting it! That's it from me. Good luck!");
        }
        else
        {
            tutorial_tap_a.setVisibility(View.INVISIBLE);
            tutorial_type_i.setVisibility(View.INVISIBLE);
            tutorial_your_word.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putStringArrayList("solution", solution);
        savedInstanceState.putString("startWord", startWord);
        savedInstanceState.putString("endWord", endWord);

        saveWords();

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        startWord = savedInstanceState.getString("startWord");
        endWord = savedInstanceState.getString("endWord");
        solution = savedInstanceState.getStringArrayList("solution");

        letter1.setText(Character.toString(startWord.charAt(0)));
        letter2.setText(Character.toString(startWord.charAt(1)));
        letter3.setText(Character.toString(startWord.charAt(2)));
        letter4.setText(Character.toString(startWord.charAt(3)));
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

    /**
     * Given two words of equal length that are in a dictionary, write a
     * method to transform one word into another word by changing only
     * one letter at a time. The new word you get in each step must be
     * in the dictionary.
     *
     * EXAMPLE
     * Input: DAMP, LIKE
     * Output: DAMP -> LAMP -> LIMP -> LIME -> LIKE
     */
        public ArrayList<String> transform(String start, String end, HashSet<String> dictionary) {
            LinkedList<String> queue = new LinkedList<>();
            HashMap<String, String> backtrackMap = new HashMap<>();
            HashSet<String> visited = new HashSet<>();
            ArrayList<String> result = new ArrayList<>();
            queue.add(start);
            visited.add(start);
            while(!queue.isEmpty()) {
                String word = queue.remove();
                for (String newWord : transformOneLetter(word, dictionary)) {

                    if (!visited.contains(newWord)) {
                        visited.add(newWord);
                        backtrackMap.put(newWord, word);
                        if (newWord.equals(end)) {
                            backtrack(start, newWord, backtrackMap, result);
                            break;
                        }
                        queue.add(newWord);
                    }
                }
            }
            return result;
        }

        private HashSet<String> transformOneLetter(String word, HashSet<String> dictionary) {
            HashSet<String> result = new HashSet<>();

            for (int i = 0; i < word.length(); ++i) {
                StringBuilder sb = new StringBuilder(word);
                for (char letter = 'A'; letter <= 'Z'; ++letter) {
                    if (sb.charAt(i) != letter) {
                        sb.setCharAt(i, letter);
                        String newWord = sb.toString();
                        if (dictionary.contains(newWord)) {
                            result.add(newWord);
                        }
                    }
                }
            }
//            Toast t = Toast.makeText(context, String.valueOf(result.isEmpty()), Toast.LENGTH_SHORT);
//            t.show();

            return result;
        }

        private static void backtrack(String start, String word, HashMap<String, String> backtrackMap, ArrayList<String> result) {
            result.add(word);
            while (backtrackMap.containsKey(word)) {
                if (word.equals(start)) break;
                word = backtrackMap.get(word);
                result.add(0, word);
            }
        }

    public boolean isValidWord(String word)
    {
        return wordString.contains(word);
    }
}

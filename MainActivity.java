package com.clin.chatbot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //initiates text to speech, speech recognizer, and output variables
    private TextToSpeech tts;
    private SpeechRecognizer talkTeller;
    private String output;
    private boolean connected;
    private boolean flashStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        flashStatus = false;
        //sets the floating action buttons action to pick up audio and start listening
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                talkTeller.startListening(intent);
            }
        });

        //sets the on launch message
        String message = ">Hello, welcome to chat bot";
        setOutput(message);

        //initializes text to speech and speech recognizer
        initializeTextToSpeech();
        initializeSpeechRecognizer();

        getPermissions();
    }

    private void getPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 5);

        }

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA}, 5);

        }
    }

    private void getResponse(String message) {
        final boolean hasCameraFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else
            connected = false;

        String alt = "I'm not sure what you mean";

        message = message.toLowerCase();
        if(message.indexOf("open") != -1) {
            if(message.indexOf("browser") != -1 || message.indexOf("web") != -1) {
                if(connected) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));
                    startActivity(intent);
                    speak("Opening Browser");
                    setOutput(">Opening Browser...");
                } else {
                    speak("You are currently offline");
                    setOutput(">You are currently offline");
                }
            } else {
                speak("What would you like me to open?");
                setOutput(">What would you like me to open?");
            }
        } else if(message.indexOf("flashlight") != -1) {
            if(hasCameraFlash) {
                if(flashStatus) {
                    speak("turning off flashlight");
                    setOutput(">Turning off flashlight...");
                    lightOff();
                    flashStatus = false;
                } else {
                    speak("turning on flashlight");
                    setOutput(">Turning on flashlight...");
                    lightOn();
                    flashStatus = true;
                }
            } else {
                speak("flashlight is not available");
                setOutput(">Flashlight is not available");
            }
        //if no commands are detected
        } else if(message.indexOf("hi") != -1 || message.indexOf("hello") != -1 || message.indexOf("hey") != -1) {
            speak("hi hows it going");
            setOutput(">Hi hows it going");
        } else {
            speak(alt);
            setOutput((">" + alt));
        }
    }

    private void lightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
        }
    }

    private void lightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
        }
    }

    //changes the text and scrolls to the bottom of the textView
    private void setOutput(String str)
    {
        output += "\n" + str;
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(output);
        scrollBottom();
    }

    //changes the scrollView position to the very bottom
    private void scrollBottom() {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        final TextView textView = (TextView) findViewById(R.id.textView);
        scrollView.post(new Runnable()
        {
            public void run()
            {
                scrollView.smoothScrollTo(0, textView.getBottom());
            }
        });
    }

    //initializes speech recognizer and accesses the most confident input and returns it as an arrayList
    private void initializeSpeechRecognizer() {
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            talkTeller = SpeechRecognizer.createSpeechRecognizer(this);
            talkTeller.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float v) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );
                    processResult(results.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    //takes in the audio message and returns a message
    private void processResult(String message) {
        message.toLowerCase();
        setOutput(message);
        getResponse(message);
    }

    //sends messages from te editText box when the user clicks send
    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        setOutput(message);
        getResponse(message);
        editText.setText("");
    }

    //initializes text to speech, sets the language, and checks the availability of a tts engine
    private void initializeTextToSpeech() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(tts.getEngines().size() == 0) {
                    Toast.makeText(MainActivity.this, "There is no TTS Engine on your device", Toast.LENGTH_LONG).show();
                    finish();
                } else  {
                    tts.setLanguage(Locale.US);
                    speak("Hello, welcome to chat bot");
                }
            }
        });
    }

    //outputs audio from a received message
    private void speak(String message) {
        if(Build.VERSION.SDK_INT > 21) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.shutdown();
    }
}
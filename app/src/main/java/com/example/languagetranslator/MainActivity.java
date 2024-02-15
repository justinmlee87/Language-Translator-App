package com.example.languagetranslator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText sourceLanguageEt;
    private TextView destinationLanguageTv;
    private MaterialButton sourceLanguageChooseBtn;
    private MaterialButton destinationLanguageChooseBtn;
    private MaterialButton translateBtn;
    private TranslatorOptions translatorOptions;
    //translator object
    private Translator translator;

    //progress dialog to show during translation process
    private ProgressDialog progressDialog;

    //will contain list with language code and title
    private ArrayList<ModelLanguage> languageArrayList;

    //to show logs
    private static final String TAG = "MAIN_TAG";
    private String sourceLanguageCode = "en";
    private String sourceLanguageTitle = "English";
    private String destinationLanguageCode = "ur";
    private String destinationLanguageTitle = "Urdu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI views
        sourceLanguageEt = findViewById(R.id.sourceLanguageEt);
        destinationLanguageTv = findViewById(R.id.destinationLanguageTv);
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn);
        destinationLanguageChooseBtn = findViewById(R.id.destinationLanguageChooseBtn);
        translateBtn = findViewById(R.id.translateBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);





        loadAvailableLanguages();

        //handle sourceLanguageChooseBtn click which you want to translate
        sourceLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sourceLanguageChoose();
            }
        });
        //handle destinationLanguageChooseBtn click which you want to translate
        destinationLanguageChooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationLanguageChoose();
            }
        });
        //handle translateBtn click, translate text to desired language
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });


    }

    private String sourceLanguageText = "";
    private void validateData() {

        //input text to be translated
        sourceLanguageText = sourceLanguageEt.getText().toString().trim();

        //print in logs
        Log.d(TAG, "validateData: sourceLanguageText: " + sourceLanguageText);

        //validate data. If empty, show message, if not start translation process
        if (sourceLanguageText.isEmpty()){
            Toast.makeText(this, "Enter text to translate...", Toast.LENGTH_SHORT).show();
        }
        else {
            startTranslations();
        }
    }

    private void startTranslations() {

        progressDialog.setMessage("Processing language model...");
        progressDialog.show();

        //init translation options with source and target languages
        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(destinationLanguageCode)
                .build();
        translator = Translation.getClient(translatorOptions);

        //init DownloadConditions with option to require wifi (optional)
        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        //start downloading translation model if required
        translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //translation model ready to be translated, lets translate

                        Log.d(TAG, "onSuccess: model ready, starting translate...");

                        //change progress message to translating
                        progressDialog.setMessage("Translating...");

                        //start translation process
                        translator.translate(sourceLanguageText)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String translatedText) {
                                        //successfully translated

                                        Log.d(TAG, "onSuccess: translatedText"+translatedText);
                                        progressDialog.dismiss();

                                        destinationLanguageTv.setText(translatedText);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to translate

                                        progressDialog.dismiss();
                                        Log.e(TAG, "onFailure: ", e);
                                        Toast.makeText(MainActivity.this, "Failed to translate due to "+e.getMessage(), Toast.LENGTH_SHORT).show();


                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to be ready translation model, can't proceed to translation
                        progressDialog.dismiss();
                        Log.e(TAG, "onFailure: ", e);
                        Toast.makeText(MainActivity.this, "Failed to translate due to "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sourceLanguageChoose(){

        PopupMenu popupMenu = new PopupMenu(this, sourceLanguageChooseBtn);

        for (int i=0;i<languageArrayList.size(); i++){

            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).languageTitle);
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int position = item.getItemId();

                sourceLanguageCode = languageArrayList.get(position).languageCode;
                sourceLanguageTitle = languageArrayList.get(position).languageTitle;

                sourceLanguageChooseBtn.setText(sourceLanguageTitle);
                sourceLanguageEt.setHint("Enter " + sourceLanguageTitle);

                Log.d(TAG, "onMenuItemClick: sourceLanguageCode" + sourceLanguageCode);
                Log.d(TAG, "onMenuItemClick: sourceLanguageTitle" + sourceLanguageTitle);

                return false;
            }
        });
    }

    //display language titles from array list
    private void destinationLanguageChoose() {

        PopupMenu popupMenu = new PopupMenu(this,destinationLanguageChooseBtn);

        for (int i=0; i<languageArrayList.size(); i++){

            popupMenu.getMenu().add(Menu.NONE, i, i, languageArrayList.get(i).getLanguageTitle());
        }

        //show popup menu
        popupMenu.show();

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                //get clicked item id
                int position = item.getItemId();

                //get code and title of the language selected
                destinationLanguageCode = languageArrayList.get(position).languageCode;
                destinationLanguageTitle = languageArrayList.get(position).languageTitle;

                //set selected language as text
                destinationLanguageChooseBtn.setText(destinationLanguageTitle);

                //show in logs
                Log.d(TAG, "onMenuItemClick: destinationLanguageCode: " + destinationLanguageCode);
                Log.d(TAG, "onMenuItemClick: destinationLanguageTitle: " + destinationLanguageTitle);

                return false;
            }
        });
    }

    private void loadAvailableLanguages() {
        languageArrayList = new ArrayList<>();

        List<String> languageCodeList = TranslateLanguage.getAllLanguages();

        for (String languageCode: languageCodeList){
            String languageTitle = new Locale(languageCode).getDisplayLanguage(); //en = english
            Log.d(TAG, "loadAvailableLanguages: languageCode: "+languageCode);
            Log.d(TAG, "loadAvailableLanguages: languageTitle: "+languageTitle);

            ModelLanguage modelLanguage = new ModelLanguage(languageCode, languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }
}
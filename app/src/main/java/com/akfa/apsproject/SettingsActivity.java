package com.akfa.apsproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.akfa.apsproject.classes_serving_other_classes.InitNavigationBar;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements View.OnTouchListener {

    private ActionBarDrawerToggle toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getResources().getString(R.string.settings_submenu));
        toggle = InitNavigationBar.setUpNavBar(SettingsActivity.this, getApplicationContext(), Objects.requireNonNull(getSupportActionBar()), R.id.about, R.id.activity_about_app);
        Button changeLang = findViewById(R.id.change_lang);
        changeLang.setOnTouchListener(this);
    }

//    private void setLocale

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setBackgroundResource(R.drawable.neutral_button_pressed);
                break;
            case MotionEvent.ACTION_UP:
                v.setBackgroundResource(R.drawable.neutral_button);
                switch (v.getId()) {
                    case R.id.change_lang:
                        openChangeLanguageDialog();
                        break;
                }
                break;
        }
        return false;
    }

    private void openChangeLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(getString(R.string.app_lang));
        String[] langArray = {"Русский", "Ўзбек тили", "O`zbek tili", "English"};
        int checkedLang = 0;
        String language = Locale.getDefault().getDisplayLanguage();
        switch (language)
        {
            case "ab": //кириллский узбекский переведен на locale абхазского, тк в андроид не получилось два узбекских языка сделать (кир и лат)
                checkedLang = 1;
                break;
            case "uz":
                checkedLang = 2;
                break;
            case "en":
                checkedLang = 3;
                break;
        }
        builder.setSingleChoiceItems(langArray, checkedLang, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int langIndex) {
                switch (langIndex){
                    case 0: //rus
                        setLocale("default", getBaseContext());
                        break;
                    case 1: //uz_cyrillic
                        setLocale("ab", getBaseContext());
                        break;
                    case 2: //uz_latin
                        setLocale("uz", getBaseContext());
                        break;
                    case 3: //eng
                        setLocale("en", getBaseContext());
                        break;
                }
                recreate();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    public static void setLocale(String lang, Context context) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        Locale current = context.getResources().getConfiguration().locale;
        String language = current.getLanguage();
    }
}

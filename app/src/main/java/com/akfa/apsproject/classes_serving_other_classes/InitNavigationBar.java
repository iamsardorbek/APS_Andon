package com.akfa.apsproject.classes_serving_other_classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.akfa.apsproject.BackgroundService;
import com.akfa.apsproject.monitoring_activities.ChecksHistory;
import com.akfa.apsproject.monitoring_activities.FactoryCondition;
import com.akfa.apsproject.R;
import com.akfa.apsproject.SettingsActivity;
import com.akfa.apsproject.monitoring_activities.TodayChecks;
import com.akfa.apsproject.calls.CallsList;
import com.akfa.apsproject.calls.MakeACall;
import com.akfa.apsproject.checking_equipment_maintenance.QuestListOfEquipment;
import com.akfa.apsproject.checking_equipment_maintenance.RepairersMaintenanceProblemsList;
import com.akfa.apsproject.Login;
import com.akfa.apsproject.general_data_classes.UserData;
import com.akfa.apsproject.pult_and_urgent_problems.PultActivity;
import com.akfa.apsproject.pult_and_urgent_problems.UrgentProblemsList;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import static android.content.Context.NOTIFICATION_SERVICE;

public class InitNavigationBar {

    public static ActionBarDrawerToggle setUpNavBar(final Activity activity, final Context context, ActionBar actionBar, final int currentMenuID, final int currentActivityID)
    {
        //---------код связанный с nav bar---------//
        //настрой actionBar
        actionBar.show();
        //настрой сам навигейшн бар
        final DrawerLayout drawerLayout;
        ActionBarDrawerToggle toggle;
        NavigationView navigationView;
        drawerLayout = activity.findViewById(currentActivityID);
        toggle = new ActionBarDrawerToggle(activity, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        actionBar.setDisplayHomeAsUpEnabled(true);
        navigationView = activity.findViewById(R.id.nv);
        View headerView = navigationView.getHeaderView(0);
        TextView userInfo = headerView.findViewById(R.id.user_info);
        userInfo.setText(UserData.login);
        navigationView.getMenu().clear();
        switch(UserData.position){ //у каждого специалиста свое меню выводится в nav bar
            case "operator":
                navigationView.inflateMenu(R.menu.operator_menu);
                break;
            case "repair":
                navigationView.inflateMenu(R.menu.repair_menu);
                break;
            case "master":
                navigationView.inflateMenu(R.menu.master_menu);
                break;
            case "raw":
                navigationView.inflateMenu(R.menu.raw_menu);
                break;
            case "quality":
                navigationView.inflateMenu(R.menu.quality_menu);
                break;
            case "head":
                navigationView.inflateMenu(R.menu.head_staff_menu);
                break;
            //other positions shouldn't be able to access checking page at all
            //if some changes, u can add a case
        }
        //ниже действия, выполняемые при нажатиях на элементы нав бара
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("ApplySharedPref")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == currentMenuID) {
                    //когда нажали на подменю текущего окна, нав бар просто закрывается
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else
                {
                    switch (id) {
                        case R.id.urgent_problems:
                            Intent openUrgentProblemsList = new Intent(context, UrgentProblemsList.class);
                            activity.startActivity(openUrgentProblemsList);
                            activity.finish();
                            break;
                        case R.id.problems_list: //открой лист ТО проблем
                            Intent openProblemsList = new Intent(context, RepairersMaintenanceProblemsList.class);
                            activity.startActivity(openProblemsList);
                            activity.finish();
                            break;
                        case R.id.calls:
                            Intent openCallsList = new Intent(context, CallsList.class);
                            activity.startActivity(openCallsList);
                            activity.finish();
                            break;
                        case R.id.make_a_call:
                            Intent openMakeACall = new Intent(context, MakeACall.class);
                            activity.startActivity(openMakeACall);
                            activity.finish();
                            break;
                        case R.id.pult:
                            Intent openMainActivity = new Intent(context, PultActivity.class);
                            activity.startActivity(openMainActivity);
                            activity.finish();
                            break;
                        case R.id.check_equipment: //переход в модуль проверки
                            Intent openQuest = new Intent(context, QuestListOfEquipment.class);
                            activity.startActivity(openQuest);
                            activity.finish();
                            break;
                        case R.id.web_monitoring: //переход в модуль веб-мониторинга
                            Intent openFactoryCondition = new Intent(context, FactoryCondition.class);
                            activity.startActivity(openFactoryCondition);
                            activity.finish();
                            break;
                        case R.id.today_checks:
                            Intent openTodayChecks = new Intent(context, TodayChecks.class);
                            activity.startActivity(openTodayChecks);
                            activity.finish();
                            break;
                        case R.id.checks_history:
                            Intent openChecksHistory = new Intent(context, ChecksHistory.class);
                            activity.startActivity(openChecksHistory);
                            activity.finish();
                            break;
                        case R.id.about: //инфа про приложение и компанию и иинструкции может
                            Intent openAboutApp = new Intent(activity.getApplicationContext(), SettingsActivity.class);
                            activity.startActivity(openAboutApp);
                            break;
                        case R.id.log_out: //возвращение в логин page
                            activity.stopService(new Intent(context, BackgroundService.class)); //если до этого уже сервис был включен, выключи сервис
                            NotificationManager notificationManager = (NotificationManager)  context.getSystemService(NOTIFICATION_SERVICE);
                            try {
                                Objects.requireNonNull(notificationManager).cancelAll();
                            } catch (NullPointerException npe) {ExceptionProcessing.processException(npe);}
                            @SuppressWarnings("deprecation") SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.clear();
                            editor.commit();
                            Intent logOut = new Intent(context, Login.class);
                            logOut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + UserData.login);
                            userRef.child("active_session_android_id").removeValue();
                            activity.startActivity(logOut);
                            activity.finish();
                    }
                }
                return true;
            }
        });
        return toggle;
    }


}

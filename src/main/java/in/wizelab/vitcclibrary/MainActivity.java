package in.wizelab.vitcclibrary;


import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.xml.sax.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.wizelab.vitcclibrary.Adapter.MyAdapter;
import in.wizelab.vitcclibrary.Models.TitleChild;
import in.wizelab.vitcclibrary.Models.TitleCreator;
import in.wizelab.vitcclibrary.Models.TitleParent;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    ScrollView scrollView;
    TitleCreator titleCreator;
    MyAdapter adapter;
    protected Button bMIUI;
    private PendingIntent pendingIntent;
    private Intent alarmIntent;
    private AlarmManager manager;

    private boolean permisisonMIUI;
    private StringBuilder smsBuilder = new StringBuilder();
    private StringBuilder vitBuilder = new StringBuilder();
    private StringBuilder libBuilder = new StringBuilder();

    private static final int maxBooks=3;
    int borrowedBooks =0;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    int ACCno;
    Date Duedate;
    List<Integer> checkedIn=new ArrayList<Integer>();
    private class Book{
        public int mACCno;
        public Date mDuedate;
        public boolean mRenewed;
        public Book(int ACCno){
            mACCno=ACCno;
            mRenewed=false;
        }
        public void set(int ACCno,Date Duedate,boolean renewed){
            mACCno= ACCno;
            mDuedate =Duedate;
            mRenewed=renewed;
        }
        public boolean dataValid(){
            if(mDuedate!=null){
                return true;
            }else{
                return false;
            }
        }
    }

    Book[] book=new Book[maxBooks];

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    recyclerView.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.INVISIBLE);
                    displayBooks();
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    recyclerView.setVisibility(View.INVISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                    mTextMessage.setText(libBuilder);
                    return true;
                case R.id.navigation_notifications:
                    recyclerView.setVisibility(View.INVISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                    //mTextMessage.setText(R.string.title_notifications);
                    mTextMessage.setText(vitBuilder);
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }
                    return true;
            }
            return false;
        }

    };

    AdView adView,adView2;
    InterstitialAd mInterstitialAd;
    RecyclerView recyclerView;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
            ((MyAdapter) recyclerView.getAdapter()).onSaveInstanceState(outState);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve a PendingIntent that will perform a broadcast
        alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        recyclerView = (RecyclerView)findViewById(R.id.myRecyclerView);
        scrollView = (ScrollView)findViewById(R.id.scrollView2);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        titleCreator = TitleCreator.get(this);
        mTextMessage = (TextView) findViewById(R.id.message);
        bMIUI = (Button) findViewById(R.id.b_miui);
        permisisonMIUI=false;
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        adManagement();
        for (int i = 0; i < maxBooks; i++) {
            book[i] = new Book(0);
        }
        vitBuilder.append("VIT MESSAGES\n\n");
        libBuilder.append("Recent LIBRARY Messages\n\n");
        getPermission();
    }
    private List<ParentObject> initData(List<String> details) {
        List<TitleParent> titles = titleCreator.getAll();
        List<ParentObject> parentObject = new ArrayList<>();
        int i=0;
        for(TitleParent title:titles)
        {
            List<Object> childList = new ArrayList<>();
            childList.add(new TitleChild(details.get(i),""));
            title.setChildObjectList(childList);
            parentObject.add(title);
            i++;
        }
        return parentObject;
    }


    public void startAlarm(long interval) {
        cancelAlarm();
        long triggerMillis=interval-TimeUnit.DAYS.toMillis(1)>0?interval-TimeUnit.DAYS.toMillis(2):TimeUnit.DAYS.toMillis(1);
        long intervalMillis=TimeUnit.DAYS.toMillis(1)/2;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+triggerMillis, intervalMillis, pendingIntent);
        //Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm() {
        if (manager != null) {
            manager.cancel(pendingIntent);
            //Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
        }

    }

    void adManagement(){
        MobileAds.initialize(this,"ca-app-pub-6346603383268337~7980860163");
        adView = (AdView)findViewById(R.id.adView);
        adView2 = (AdView)findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("441269B8FC24A3E79CFABAF71DD1C486")
                .build();
        adView.loadAd(adRequest);
        adView2.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6346603383268337/1041158089");
        //mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    public static boolean isMIUI() {
        String device = Build.MANUFACTURER;
        if (device.equals("Xiaomi")) {
            try {
                Properties prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                return prop.getProperty("ro.miui.ui.version.code", null) != null
                        && prop.getProperty("ro.miui.ui.version.name", null) != null
                        && prop.getProperty("ro.miui.internal.storage", null) == null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    void readSMS(){
        final String SMS_URI_INBOX = "content://sms/inbox";
        final String SMS_URI_ALL = "content://sms/";
        try {
            Uri uri = Uri.parse(SMS_URI_ALL);
            //String[] projection = new String[] { "TM-VITCHN" };
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cur = getContentResolver().query(uri, projection, null, null, "date desc");
            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");

                do {
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String strbody = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    int int_Type = cur.getInt(index_Type);

                    smsBuilder.append("[ ");
                    smsBuilder.append(strAddress + ", ");
                    smsBuilder.append(intPerson + ", ");
                    smsBuilder.append(strbody + ", ");
                    smsBuilder.append(longDate + ", ");
                    smsBuilder.append(int_Type);
                    smsBuilder.append(" ]\n\n");

                    if(strAddress!=null) {
                        if (strAddress.contains("VITCHN")) {
                            bMIUI.setVisibility(View.GONE);
                            if (strbody.toLowerCase().contains("book")) {
                                if (borrowedBooks < maxBooks) {
                                    libBuilder.append(strAddress + " " + strbody + " " + longDate + "\n\n");

                                    Matcher ACCnoMatcher = Pattern.compile("\\d{6}").matcher(strbody);
                                    Matcher DuedateMatcher = Pattern.compile("(\\d{2}|\\d{1})/\\d{2}/\\d{4}").matcher(strbody);
                                    if (ACCnoMatcher.find()) {
                                        ACCno = Integer.parseInt(ACCnoMatcher.group());
                                        System.out.println("ACCno: " + ACCno);
                                    }
                                    if (DuedateMatcher.find()) {
                                        try {
                                            Duedate = sdf.parse(DuedateMatcher.group());
                                            System.out.println("Date: " + Duedate.toString());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (strbody.toLowerCase().contains("checked out")) {
                                        boolean bookPresent = false;
                                        for (int i = 0; i < borrowedBooks; i++) {
                                            if (book[i].mACCno == ACCno) {
                                                bookPresent = true;
                                            }
                                        }
                                        if (!bookPresent && !checkedIn.contains(ACCno)) {
                                            book[borrowedBooks].set(ACCno, Duedate, false);
                                            borrowedBooks++;
                                        }
                                    }
                                    if (strbody.toLowerCase().contains("renewed")) {
                                        if (!checkedIn.contains(ACCno)) {
                                            book[borrowedBooks].set(ACCno, Duedate, true);
                                            borrowedBooks++;
                                        }else{

                                        }
                                    }
                                    if (strbody.toLowerCase().contains("checked in")) {
                                        checkedIn.add(ACCno);
                                        borrowedBooks++;
                                    }
                                }
                            } else {
                                vitBuilder.append(strAddress + " " + strbody + " " + longDate + "\n\n");
                            }
                        }
                    }
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
                displayBooks();
            } else {
                smsBuilder.append("no result!");
            } // end if
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
    }

    private void displayBooks(){
        titleCreator.removeAll();
        mTextMessage.setText("BOOKS:\n");
        List<String> details = new ArrayList<>();
        if(borrowedBooks<=0){
            cancelAlarm();
            titleCreator.add("NO DUE BOOKS DETECTED");
            details.add(":->");
            adapter= new MyAdapter(this,initData(details));
            adapter.setParentClickableViewAnimationDefaultDuration();
            adapter.setParentAndIconExpandOnClick(true);
            recyclerView.setAdapter(adapter);
        }else{
            long shortestDiff=Long.MAX_VALUE;
            //mTextMessage.setText("Due Books: "+borrowedBooks+"\n\n");
            boolean booksDue=false;
            for (int i = 0; i < borrowedBooks; i++) {
                if(book[i].dataValid()) {
                    booksDue=true;
                    String dayOfTheWeek = (String) DateFormat.format("EEEE", book[i].mDuedate); // Thursday

                    details.add("RENEWED: " + String.valueOf(book[i].mRenewed).toUpperCase()+"\n"
                            + "DUE ON: " + String.valueOf(sdf.format(book[i].mDuedate)+"\n")
                            + "("+dayOfTheWeek+")");

                    Date date = new Date();
                    sdf.format(date);
                    long diff = book[i].mDuedate.getTime() - date.getTime();
                    shortestDiff=diff<shortestDiff?diff:shortestDiff;
                    long days =1+ TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    String daysMessage;
                    if(days>1) {
                        daysMessage=days + " days left";
                    }
                    else if(days==1){
                        daysMessage=days + " day left";
                    }else if(days==0){
                        daysMessage="Due Today!";
                    }else{
                        daysMessage="Over Due by " + (-days) + " days!";
                    }
                    titleCreator.add(String.valueOf(book[i].mACCno)+": "+daysMessage);
                }
            }
            if(booksDue) {
                startAlarm(shortestDiff);
                adapter = new MyAdapter(this, initData(details));
                adapter.setParentClickableViewAnimationDefaultDuration();
                adapter.setParentAndIconExpandOnClick(true);
                recyclerView.setAdapter(adapter);
            }else{
                cancelAlarm();
                titleCreator.add("NO DUE BOOKS DETECTED");
                details.add(":->");
                adapter= new MyAdapter(this,initData(details));
                adapter.setParentClickableViewAnimationDefaultDuration();
                adapter.setParentAndIconExpandOnClick(true);
                recyclerView.setAdapter(adapter);
            }
        }

    }

    private void getPermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(!permisisonMIUI) {
                            checkforMIUI();
                        }
                        permisisonMIUI=false;
                        readSMS();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(permisisonMIUI){
            getPermission();
        }
    }

    private void checkforMIUI(){
        try {
            if(isMIUI()) {
                bMIUI.setVisibility(View.VISIBLE);
                bMIUI.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permisisonMIUI=true;
                        Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                        localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                        localIntent.putExtra("extra_pkgname", getPackageName());
                        localIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        localIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(localIntent);
                    }
                });

            }
        } catch (Exception e) {}
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

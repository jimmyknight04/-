package kr.co.ktmi.companymanager;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final String SITE="https://company-qms-manager.kimyangha.chatgpt.site";
    private static final int NAVY=Color.rgb(8,8,79), TEAL=Color.rgb(143,193,195), BG=Color.rgb(244,246,250);
    private LinearLayout root,content,nav; private TextView title; private WebView web; private ValueCallback<Uri[]> chooser;
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(16),dp(12),dp(16),dp(12));return v;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(NAVY);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);build();for(JSONObject o:LocalStore.schedules(this))ReminderScheduler.schedule(this,o);show("schedule".equals(getIntent().getStringExtra("openTab"))?"schedule":"home");}
    private void build(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(18),dp(10),dp(12),dp(10));head.setBackgroundColor(Color.WHITE);
        title=text("KTM 업무관리",20,NAVY);title.setTypeface(null,1);head.addView(title,new LinearLayout.LayoutParams(0,dp(54),1));TextView bell=text("🔔",22,NAVY);head.addView(bell);root.addView(head);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(14),dp(12),dp(14),dp(12));root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this);nav.setBackgroundColor(Color.WHITE);nav.setPadding(dp(4),dp(4),dp(4),dp(8));for(String[] n:new String[][]{{"home","⌂\n홈"},{"schedule","▣\n일정"},{"message","✎\n업무메모"},{"system","☰\n관리시스템"}}){Button x=new Button(this);x.setText(n[1]);x.setTextSize(12);x.setTextColor(NAVY);x.setBackgroundColor(Color.TRANSPARENT);x.setOnClickListener(v->show(n[0]));nav.addView(x,new LinearLayout.LayoutParams(0,dp(64),1));}root.addView(nav);setContentView(root);
    }
    private void clear(String t){title.setText(t);content.removeAllViews();content.setPadding(dp(14),dp(12),dp(14),dp(12));}
    private void show(String tab){if("schedule".equals(tab))showSchedule();else if("message".equals(tab))showMessages();else if("system".equals(tab))showSystem();else showHome();}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(10),dp(16),dp(10));c.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(10));c.setLayoutParams(p);return c;}
    private void showHome(){
        clear("오늘의 업무");String today=new SimpleDateFormat("yyyy-MM-dd",Locale.KOREA).format(new Date());List<JSONObject> all=LocalStore.schedules(this);int todayCount=0,up=0;for(JSONObject o:all){if(today.equals(o.optString("date")))todayCount++;if(o.optString("date").compareTo(today)>=0&&!o.optBoolean("done"))up++;}
        LinearLayout summary=card();TextView hi=text("안녕하세요\n오늘 확인할 업무를 모았습니다.",19,NAVY);hi.setTypeface(null,1);summary.addView(hi);summary.addView(text("오늘 일정 "+todayCount+"건  ·  예정 일정 "+up+"건",14,Color.DKGRAY));content.addView(summary);
        Button add=new Button(this);add.setText("+ 새 일정 등록");add.setTextColor(Color.WHITE);add.setBackgroundColor(NAVY);add.setOnClickListener(v->editSchedule(null));content.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));
        content.addView(text("다가오는 일정",17,NAVY));int shown=0;for(JSONObject o:all){if(o.optString("date").compareTo(today)>=0&&!o.optBoolean("done")&&shown++<4)content.addView(scheduleCard(o));}if(shown==0)content.addView(text("등록된 예정 일정이 없습니다.",15,Color.GRAY));
        LinearLayout info=card();info.addView(text("알림 설정",16,NAVY));info.addView(text("모든 일정은 2일 전과 1일 전에 휴대폰 알림으로 안내합니다.",14,Color.DKGRAY));content.addView(info);
    }
    private View scheduleCard(JSONObject o){
        LinearLayout c=card();TextView d=text(o.optString("date")+"  "+(o.optBoolean("done")?"완료":"예정"),13,o.optBoolean("done")?Color.GRAY:Color.rgb(82,125,142));TextView t=text(o.optString("title"),17,NAVY);t.setTypeface(null,1);c.addView(d);c.addView(t);String note=o.optString("note");if(!note.isEmpty())c.addView(text(note,14,Color.DKGRAY));c.setOnClickListener(v->editSchedule(o));c.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("일정 삭제").setMessage(o.optString("title")+" 일정을 삭제할까요?").setNegativeButton("취소",null).setPositiveButton("삭제",(a,b)->{long id=o.optLong("id");ReminderScheduler.cancel(this,id);LocalStore.deleteSchedule(this,id);showSchedule();}).show();return true;});return c;
    }
    private void showSchedule(){
        clear("일정");LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.addView(text("전체 일정",19,NAVY),new LinearLayout.LayoutParams(0,-2,1));Button add=new Button(this);add.setText("+ 등록");add.setTextColor(Color.WHITE);add.setBackgroundColor(NAVY);add.setOnClickListener(v->editSchedule(null));bar.addView(add);content.addView(bar);
        ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);for(JSONObject o:LocalStore.schedules(this))list.addView(scheduleCard(o));if(list.getChildCount()==0)list.addView(text("일정을 등록하면 2일 전·1일 전에 알려드립니다.",15,Color.GRAY));scroll.addView(list);content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }
    private void editSchedule(JSONObject old){
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(20),0,dp(20),0);EditText name=new EditText(this);name.setHint("일정명");name.setText(old==null?"":old.optString("title"));form.addView(name);TextView date=text(old==null?"날짜 선택":old.optString("date"),16,NAVY);date.setBackgroundColor(Color.rgb(235,242,243));form.addView(date);final String[] selected={old==null?"":old.optString("date")};date.setOnClickListener(v->{Calendar now=Calendar.getInstance();new DatePickerDialog(this,(x,y,m,d)->{selected[0]=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);date.setText(selected[0]);},now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH)).show();});EditText note=new EditText(this);note.setHint("메모·준비사항");note.setText(old==null?"":old.optString("note"));form.addView(note);CheckBox done=new CheckBox(this);done.setText("완료된 일정");done.setChecked(old!=null&&old.optBoolean("done"));form.addView(done);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle(old==null?"새 일정 등록":"일정 수정").setView(form).setNegativeButton("취소",null).setPositiveButton("저장",null).create();dlg.setOnShowListener(x->dlg.getButton(-1).setOnClickListener(v->{if(name.getText().toString().trim().isEmpty()||selected[0].isEmpty()){Toast.makeText(this,"일정명과 날짜를 입력해 주세요.",Toast.LENGTH_SHORT).show();return;}try{JSONObject o=old==null?new JSONObject():new JSONObject(old.toString());o.put("title",name.getText().toString().trim());o.put("date",selected[0]);o.put("note",note.getText().toString().trim());o.put("done",done.isChecked());LocalStore.saveSchedule(this,o);ReminderScheduler.cancel(this,o.optLong("id"));if(!done.isChecked())ReminderScheduler.schedule(this,o);dlg.dismiss();showSchedule();Toast.makeText(this,"일정을 저장했습니다.",Toast.LENGTH_SHORT).show();}catch(Exception ignored){}}));dlg.show();
    }
    private void showMessages(){
        clear("업무메모");EditText input=new EditText(this);input.setHint("할 일, 전화내용, 확인사항을 남겨 주세요.");input.setMinLines(2);content.addView(input);Button add=new Button(this);add.setText("메모 저장");add.setTextColor(Color.WHITE);add.setBackgroundColor(NAVY);add.setOnClickListener(v->{String s=input.getText().toString().trim();if(!s.isEmpty()){LocalStore.addMessage(this,s);showMessages();}});content.addView(add,new LinearLayout.LayoutParams(-1,dp(50)));
        ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(0,dp(14),0,0);for(JSONObject o:LocalStore.messages(this)){LinearLayout c=card();c.addView(text(o.optString("text"),16,NAVY));c.addView(text(o.optString("created")+"  ·  길게 눌러 삭제",12,Color.GRAY));c.setOnLongClickListener(v->{LocalStore.deleteMessage(this,o.optLong("id"));showMessages();return true;});list.addView(c);}if(list.getChildCount()==0)list.addView(text("저장된 업무메모가 없습니다.",15,Color.GRAY));scroll.addView(list);content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    }
    private void showSystem(){
        clear("기존 관리시스템");content.setPadding(0,0,0,0);web=new WebView(this);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(true);CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){v.loadUrl(r.getUrl().toString());return true;}});web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> cb,FileChooserParams p){if(chooser!=null)chooser.onReceiveValue(null);chooser=cb;try{startActivityForResult(p.createIntent(),1001);return true;}catch(Exception e){chooser=null;return false;}}});web.setDownloadListener((url,ua,cd,mime,len)->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))));web.loadUrl(SITE);content.addView(web,new LinearLayout.LayoutParams(-1,-1));
    }
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data);if(req==1001&&chooser!=null){chooser.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res,data));chooser=null;}}
    @Override public void onBackPressed(){if(web!=null&&web.getParent()!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
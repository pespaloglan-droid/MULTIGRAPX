package com.multigrapx.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainActivity extends Activity {
 private final int GOLD=Color.rgb(222,195,145), BG=Color.rgb(8,8,10), MUTED=Color.rgb(157,157,169);
 private final List<JSONObject> all=new ArrayList<>();
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final AtomicBoolean busy=new AtomicBoolean(false);
 private LinearLayout root,grid; private String tab="Duvar Kâğıtları", category="Tümü",query="";
 private JSONObject selected; private volatile boolean closed=false;
 private Set<String> favorites=new HashSet<>();
 private static byte[] readBytes(InputStream in) throws IOException { ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] b=new byte[8192]; int n; while((n=in.read(b))!=-1)out.write(b,0,n); return out.toByteArray(); }
 private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private String str(JSONObject a,String k){return a.optString(k);}
 private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(5),0,dp(5));return t;}
 private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 private Button button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setTextColor(GOLD);b.setTextSize(12);b.setAllCaps(false);b.setBackground(shape(Color.rgb(26,26,30),12));b.setPadding(dp(12),0,dp(12),0);b.setOnClickListener(v->action.run());return b;}
 private void space(LinearLayout l,int height){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(height)));}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(0);
  favorites=new HashSet<>(getPreferences(0).getStringSet("favorites",new HashSet<>()));
  try(InputStream in=getAssets().open("catalog.json")){JSONArray arr=new JSONArray(new String(readBytes(in),StandardCharsets.UTF_8));for(int i=0;i<arr.length();i++)all.add(arr.getJSONObject(i));}
  catch(Exception e){new AlertDialog.Builder(this).setMessage("Katalog okunamadı. Uygulamayı yeniden yükleyin.").setPositiveButton("Kapat",(d,w)->finish()).show();return;}
  home();
 }
 private void shell(){root=column();root.setBackgroundColor(BG);root.setPadding(dp(20),0,dp(20),0);setContentView(root);
  root.setOnApplyWindowInsetsListener((v,i)->{android.graphics.Insets s=i.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());v.setPadding(dp(20)+s.left,s.top,dp(20)+s.right,s.bottom);return i;});root.requestApplyInsets();}
 private void home(){selected=null;shell();space(root,10);
  TextView mark=text("MULTI / GRAPX",26,Color.WHITE);mark.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(mark);root.addView(text("BLACK EDITION     /     ORIGINAL COLLECTION",10,GOLD));
  LinearLayout tabs=new LinearLayout(this);for(String name:new String[]{"Duvar Kâğıtları","Görseller","Favoriler"}){Button b=button(name,()->{tab=name;category="Tümü";query="";home();});if(tab.equals(name))b.setTextColor(Color.WHITE);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(46),1);lp.setMargins(0,dp(8),dp(5),dp(8));tabs.addView(b,lp);}root.addView(tabs);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout body=column();scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  if(!tab.equals("Favoriler")){TextView headline=text(tab.equals("Görseller")?"Bir görsel. Bin his.":"Ekranın, senin dünyan.",28,Color.WHITE);headline.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(headline);body.addView(text("4K & 8K • Çevrimdışı koleksiyon • Ücretsiz indirme",12,MUTED));}
  EditText search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(MUTED);search.setTextSize(14);search.setHint("Başlık veya kategori ara");search.setBackground(shape(Color.rgb(22,22,26),12));search.setPadding(dp(12),0,dp(12),0);LinearLayout.LayoutParams sl=new LinearLayout.LayoutParams(-1,dp(48));sl.setMargins(0,dp(12),0,dp(10));body.addView(search,sl);
  HorizontalScrollView chips=new HorizontalScrollView(this);chips.setHorizontalScrollBarEnabled(false);LinearLayout row=new LinearLayout(this);chips.addView(row);for(String cat:new String[]{"Tümü","Money","Love","Luxury Life","Black","Lonely"}){Button b=button(cat,()->{category=cat;renderGrid();for(int j=0;j<row.getChildCount();j++)((Button)row.getChildAt(j)).setTextColor(((Button)row.getChildAt(j)).getText().toString().equals(category)?Color.WHITE:GOLD);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(42));lp.setMargins(0,0,dp(7),0);row.addView(b,lp);}body.addView(chips);space(body,12);
  grid=column();body.addView(grid);renderGrid();search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){query=s.toString();renderGrid();}public void afterTextChanged(android.text.Editable e){}});
  Button info=button("Koleksiyon ve gizlilik",()->new AlertDialog.Builder(this).setTitle("MultiGrapx · Black Edition").setMessage("10 özgün, prosedürel dijital sanat eseri.\n\n8K: 4320 × 7680 piksel\n4K: 2160 × 3840 piksel\n\nGörseller algoritmik olarak tam 8K boyutunda çizilmiştir; fotoğraf veya yapay zekâ çıktısı değildir. Promptlar tasarım betimleridir, birebir yeniden üretim garantisi vermez.\n\nHesap, reklam, takip ve internet bağlantısı yoktur. Favoriler cihazda saklanır. İndirilen görseller Galeri / Pictures / MultiGrapx içinde kalır.\n\nDuvar kâğıdı uygulanırken 4K örnek kullanılır; cihaz ekranına göre kırpılabilir. İçerik kullanımı için LICENSE-CONTENT.txt dosyasına bakın.").setPositiveButton("Tamam",null).show());root.addView(info,new LinearLayout.LayoutParams(-1,dp(42)));space(root,8);
 }
 private void renderGrid(){grid.removeAllViews();LinearLayout row=null;int count=0;for(JSONObject a:all){String id=str(a,"id");if(!category.equals("Tümü")&&!category.equals(str(a,"category")))continue;if(tab.equals("Favoriler")&&!favorites.contains(id))continue;if(!(str(a,"title")+" "+str(a,"category")).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;
   if(count%2==0){row=new LinearLayout(this);grid.addView(row);}LinearLayout card=column();card.setBackground(shape(Color.rgb(19,19,23),14));card.setPadding(dp(7),dp(7),dp(7),dp(7));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,0,dp(8),dp(12));row.addView(card,lp);
   ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);try(InputStream in=getAssets().open("thumb/"+id+".jpg")){image.setImageBitmap(BitmapFactory.decodeStream(in));}catch(Exception ignored){}card.addView(image,new LinearLayout.LayoutParams(-1,dp(235)));card.addView(text(str(a,"category").toUpperCase(Locale.ROOT)+"  /  8K",9,GOLD));card.addView(text(str(a,"title"),14,Color.WHITE));card.setContentDescription(str(a,"title")+", "+str(a,"category"));card.setOnClickListener(v->detail(a));count++;
  }if(count==0)grid.addView(text("Henüz görsel yok. Başka bir kategori veya arama deneyin.",15,MUTED));else if(count%2==1)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));}
 private void detail(JSONObject a){selected=a;shell();ScrollView sc=new ScrollView(this);LinearLayout body=column();sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,-1));body.addView(button("‹ Koleksiyona dön",()->home()));
  ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);try(InputStream in=getAssets().open("art/"+str(a,"id")+".jpg")){BitmapFactory.Options op=new BitmapFactory.Options();op.inSampleSize=8;image.setImageBitmap(BitmapFactory.decodeStream(in,null,op));}catch(Exception ignored){}body.addView(image,new LinearLayout.LayoutParams(-1,dp(380)));
  body.addView(text(str(a,"category").toUpperCase(Locale.ROOT)+"  /  ORIGINAL",11,GOLD));body.addView(text(str(a,"title"),28,Color.WHITE));body.addView(text("8K · 4320 × 7680   |   4K · 2160 × 3840",12,MUTED));
  Button fav=button(favorites.contains(str(a,"id"))?"♥ Favorilerden çıkar":"♡ Favorilere ekle",()->{String id=str(a,"id");if(!favorites.add(id))favorites.remove(id);getPreferences(0).edit().putStringSet("favorites",new HashSet<>(favorites)).apply();detail(a);});body.addView(fav);space(body,8);
  body.addView(button("İndir · 4K / 8K",()->new AlertDialog.Builder(this).setTitle("İndirme kalitesi").setItems(new String[]{"4K · 2160 × 3840","8K · 4320 × 7680"},(d,w)->start(()->save(a,w==0?4:8))).show()));space(body,8);
  body.addView(button("Duvar kâğıdı olarak kullan",()->new AlertDialog.Builder(this).setTitle("Nereye uygulansın?").setItems(new String[]{"Ana ekran","Kilit ekranı","Her ikisi"},(d,w)->start(()->wallpaper(a,w==0?1:w==1?2:3))).show()));space(body,12);
  body.addView(text("GÖRSEL PROMPTU",11,GOLD));TextView prompt=text(str(a,"prompt"),14,MUTED);prompt.setTextIsSelectable(true);body.addView(prompt);body.addView(button("Promptu kopyala",()->{((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("MultiGrapx prompt",str(a,"prompt")));toast("Prompt kopyalandı");}));space(body,24);
 }
 private void toast(String s){runOnUiThread(()->{if(!closed)Toast.makeText(this,s,Toast.LENGTH_LONG).show();});}
 private void start(Runnable r){if(!busy.compareAndSet(false,true)){toast("Önceki işlem devam ediyor");return;}toast("İşleniyor…");worker.execute(()->{try{r.run();}finally{busy.set(false);}});}
 private void save(JSONObject a,int size){Uri uri=null;Bitmap bitmap=null;try{ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,"MultiGrapx_"+str(a,"id")+"_"+size+"K_"+System.currentTimeMillis()+".jpg");values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");values.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+"/MultiGrapx");values.put(MediaStore.Images.Media.IS_PENDING,1);uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new IOException();
  try(InputStream in=getAssets().open("art/"+str(a,"id")+".jpg");OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException();if(size==8){byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}else{BitmapFactory.Options op=new BitmapFactory.Options();op.inSampleSize=2;bitmap=BitmapFactory.decodeStream(in,null,op);if(bitmap==null||!bitmap.compress(Bitmap.CompressFormat.JPEG,95,out))throw new IOException();}}
  values.clear();values.put(MediaStore.Images.Media.IS_PENDING,0);if(getContentResolver().update(uri,values,null,null)<1)throw new IOException();toast(size+"K görsel Galeri / MultiGrapx içine kaydedildi");
 }catch(Exception|OutOfMemoryError e){if(uri!=null)try{getContentResolver().delete(uri,null,null);}catch(Exception ignored){}toast("İndirilemedi. Depolama alanını kontrol edin.");}finally{if(bitmap!=null)bitmap.recycle();}}
 private void wallpaper(JSONObject a,int flags){Bitmap bitmap=null;try(InputStream in=getAssets().open("art/"+str(a,"id")+".jpg")){WallpaperManager wm=WallpaperManager.getInstance(this);if(!wm.isWallpaperSupported()||!wm.isSetWallpaperAllowed()){toast("Cihaz duvar kâğıdı değişikliğine izin vermiyor");return;}BitmapFactory.Options op=new BitmapFactory.Options();op.inSampleSize=2;bitmap=BitmapFactory.decodeStream(in,null,op);if(bitmap==null)throw new IOException();wm.setBitmap(bitmap,null,true,flags);toast("Duvar kâğıdı uygulandı");}catch(Exception|OutOfMemoryError e){toast("Uygulanamadı. Görseli indirip Galeri üzerinden deneyin.");}finally{if(bitmap!=null)bitmap.recycle();}}
 @Override public void onBackPressed(){if(selected!=null)home();else super.onBackPressed();}
 @Override public void onDestroy(){closed=true;worker.shutdown();super.onDestroy();}
}

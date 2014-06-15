// ライブラリのインポート
import processing.opengl.*;
import java.util.*;
import java.text.SimpleDateFormat;

// =====================================================
// OSC
import oscP5.*;
import netP5.*;
OscP5 oscP5;
NetAddress myRemoteLocation;
// =====================================================

// =====================================================
// グローバル変数
// String URL = "https://api.xively.com/v2/feeds/155754594.csv"; // 出雲神社
String URL = "https://api.xively.com/v2/feeds/604049346.csv"; // やましぎの杜 (宮崎県諸塚村）
// String URL = "https://api.xively.com/v2/feeds/176655154.csv"; // 北海道
// String URL = "https://api.xively.com/v2/feeds/638051676.csv"; // 熊野神社（山口市熊野）
// String URL = "https://api.xively.com/v2/feeds/534301954.csv"; // オーストラリア
String MY_KEY = "7DGFbpKeGma3UanSeYVdSeI47yAt87DGWOux0nYtZnwIzH5s";
String strTimeStamp = "";
ArrayList<ArrayList<dotObj>> dotLists;
// =====================================================

// 初期設定
void setup(){ 

  size(420, 200, OPENGL);
  background(60);
  frameRate(0.2);
  smooth();

  // procressingの受信ポート(pdの送信ポート)
  oscP5 = new OscP5(this, 7704);
  
  // procressingの送信ポート(pdの受信ポート)
  myRemoteLocation = new NetAddress("127.0.0.1", 7703);

  dotLists = new ArrayList<ArrayList<dotObj>>();
}

// 描画
void draw(){

  // コンソールにタイムスタンプを表示
  Calendar cal = Calendar.getInstance();
  Date theDate = cal.getTime();
  println("now : " + theDate);
  
  // データ取得
  getAllData();

  // 描画
  drawAllData();

  // パラメータの描画
  drawParam();
}

// パラメータの描画
void drawParam(){

  stroke(30, 80);

  // 軸
  float zero = map(0, -0.5, 0.5, height/4, height);
  line(0, zero, width, zero);

  // パラメータ
  text("URL: " + URL, 10, 20);
  text("KEY: " + MY_KEY, 10, 35);
  text(strTimeStamp, 10, height-15);
}

// データ取得
void getAllData(){

  // xivelyからのデータ取得
  String lines[] = getDataFromXively(URL, MY_KEY);

  // 取得したデータをリストに格納
  dotLists = new ArrayList<ArrayList<dotObj>>();

  // チャンネルの取得
  int max_channel = 0;
  int min_channel = 9999;
  for (int i = 0; i < lines.length; i++) {
    String data[] = split(lines[i], ',');
    int theChannel = int(data[0]);
    if(min_channel > theChannel){
      min_channel = theChannel;
    }
    if(max_channel < theChannel){
      max_channel = theChannel;
    }
  }

  // 全チャンネルのデータを取得し、リストに格納
  for(int channel = min_channel; channel <= max_channel; channel++){
    ArrayList<dotObj> dotList = getDataList(lines, channel);
    dotLists.add(dotList);
  }
}

void drawAllData(){
  // リストに要素がある場合のみ画面の初期化および描画の設定を行う
  if(dotLists.size() > 0){
    background(60);
  }

  // 全チャンネルのデータを描画、OSCで送信
  for(int i = 0; i < dotLists.size(); i++){
    // 描画
    drawData(dotLists.get(i));

    // 送信
    String prefix = "/prefix" + i;
    OscMessage myMessage = new OscMessage(prefix);
    sendOSC(dotLists.get(i), myMessage, prefix);
  }
}

// xivelyからのデータ取得
// 現在の日付から2日さかのぼった日付からデータを取得
String[] getDataFromXively_Current(String URL, String MY_KEY){

  // // 現在の日付を取得
  // Calendar cal = Calendar.getInstance();
  // // 指定した日数を加算
  // cal.add(Calendar.DATE, -6);

  // // 基準日(現在より7日前の日付を基準日としている)より後の日付になった場合は初期化
  // Calendar before1 = Calendar.getInstance();
  // before1.add(Calendar.DATE, -1);
  // int result = cal.compareTo(before1);
  // if(result > 0){
  //   cal = Calendar.getInstance();
  //   cal.add(Calendar.DATE, -2);
  // }
  // // 指定した日付をData型で取得
  // Date theDate = cal.getTime();
  // println("data: " + theDate);

  // // 指定した日付をISO 8601(UTC)フォーマットで文字列化
  // SimpleDateFormat dateFormat = getDateFormat_ISO8601();
  // String startDate = dateFormat.format(theDate);
  // println("format: " + startDate);
  
  // 戻り値用配列
  String[] retlines = new String[0];
  String[] dataLines = new String[0];

  // 文字列格納用の動的配列
  ArrayList<String> strList = new ArrayList<String>();

  // xivelyへのURLリクエストを作成
  String URL_REQUEST = URL + "?key=" + MY_KEY;

  println(URL_REQUEST);

  // xivelyからのデータ取得(取得できない場合は何もしない)
  try {
    // xivlyからのデータを一時的に格納
    dataLines = loadStrings(URL_REQUEST);
  }catch (Exception e) {
    // 取得できない場合
    e.printStackTrace();
    dataLines = null;
  }

  // 取得できない場合
  if(dataLines == null){
    background(60);
    text("error!", width/2, height/2);
    dataLines = new String[0];
    strList.clear();
  }

  // xivlyからのデータを動的配列に追加
  for(int i = 0; i < dataLines.length; i++){
    strList.add(dataLines[i]);
  }

  // 戻り値用配列へデータをコピー
  retlines = new String[strList.size()];
  for(int i = 0; i < strList.size(); i++){
    retlines[i] = strList.get(i);
  }

  return retlines;
}

// xivelyからのデータ取得
// 現在の日付から2日さかのぼった日付からデータを取得
String[] getDataFromXively(String URL, String MY_KEY){

  // 現在の日付を取得
  Calendar cal = Calendar.getInstance();
  // 指定した日数を加算
  cal.add(Calendar.DATE, -10);

  // 基準日(現在より7日前の日付を基準日としている)より後の日付になった場合は初期化
  Calendar before1 = Calendar.getInstance();
  before1.add(Calendar.DATE, -1);
  int result = cal.compareTo(before1);
  if(result > 0){
    cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -2);
  }
  // 指定した日付をData型で取得
  Date theDate = cal.getTime();
  println("data: " + theDate);

  // 指定した日付をISO 8601(UTC)フォーマットで文字列化
  SimpleDateFormat dateFormat = getDateFormat_ISO8601();
  String startDate = dateFormat.format(theDate);
  println("format: " + startDate);
  
  // 戻り値用配列
  String[] retlines = new String[0];
  String[] dataLines = new String[0];

  // 文字列格納用の動的配列
  ArrayList<String> strList = new ArrayList<String>();

  // xivelyへのURLリクエストを作成
  // String strDuration = "&duration=1days&interval=300";  // 300秒間隔で1日分のデータを取得
  // String URL_REQUEST = URL + "?start=" + startDate + strDuration + "&limit=450" + "?key=" + MY_KEY;
  String strDuration = "&duration=6hours&interval=0";  // 1秒間隔で1日分のデータを取得
  String URL_REQUEST = URL + "?start=" + startDate + strDuration + "&limit=450" + "?key=" + MY_KEY;

  println(URL_REQUEST);

  // xivelyからのデータ取得(取得できない場合は何もしない)
  try {
    // xivlyからのデータを一時的に格納
    dataLines = loadStrings(URL_REQUEST);
  }catch (Exception e) {
    // 取得できない場合
    e.printStackTrace();
    dataLines = null;
  }

  // 取得できない場合
  if(dataLines == null){
    background(60);
    text("error!", width/2, height/2);
    dataLines = new String[0];
    strList.clear();
  }

  // xivlyからのデータを動的配列に追加
  for(int i = 0; i < dataLines.length; i++){
    strList.add(dataLines[i]);
  }

  // 戻り値用配列へデータをコピー
  retlines = new String[strList.size()];
  for(int i = 0; i < strList.size(); i++){
    retlines[i] = strList.get(i);
  }

  return retlines;
}

// 日付フォーマットの取得
SimpleDateFormat getDateFormat_ISO8601(){

  // ISO 8601(UTC)　のフォーマットを作成
  String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.000000'Z'";
  // String timeZoneName = "UTC";
  String timeZoneName = "JST";
  SimpleDateFormat sdfIso8601ExtendedFormatUtc = new SimpleDateFormat(dateFormat);
  sdfIso8601ExtendedFormatUtc.setTimeZone(TimeZone.getTimeZone(timeZoneName));

  return sdfIso8601ExtendedFormatUtc;
}

// データをリストに格納して取得
ArrayList<dotObj> getDataList(String[] lines, int targetChannel){

  ArrayList<dotObj> retList = new ArrayList<dotObj>();

  // 全データを参照して必要な情報を取り出す
  int index = 0;
  for (int i = 0; i < lines.length; i++) {
    // ','で区切り配列に格納
    String data[] = split(lines[i], ',');
    // channel, date, valuse の順でデータが格納されている
    int channel = int(data[0]); // チャンネル
    String timeStamp = data[1]; // 日付
    float val = float(data[2]); // 値

    if(channel == targetChannel){
      // println("channel: " + channel);
      // println("date   : " + timeStamp);
      // println("val    : " + val);

      // 取得したデータをオブジェクトにセットし、配列に格納
      float m = map(val, -0.5, 0.5, height/4, height);
      dotObj theObj = new dotObj(index * width/70, m, 0);
      theObj.channel = channel;
      theObj.timeStamp = timeStamp;
      theObj.val = val;
      retList.add(theObj);

      index++;
    }
  }
  
  return retList;
}

// 取得したデータの可視化
void drawData(ArrayList<dotObj> theList){

  // 現在参照する点と直前に参照した点を格納するオブジェクトを生成
  PVector currentPoint = new PVector(0, height/2, 0);
  PVector lastPoint = new PVector(0, height/2, 0);

  // 現在の点と直前の点を線で結ぶ
  for(int i = 0; i < theList.size(); i++){
    // 現在の点を取得
    currentPoint = theList.get(i).p;
    // 現在の点と直前の点を線で結ぶ
    strokeWeight(0.8);
    stroke(200, 80);
    // stroke(random(30)+200, 20);
    line(lastPoint.x, lastPoint.y, lastPoint.z, currentPoint.x, currentPoint.y, currentPoint.z);
    // 直前の点を現在の点で更新
    lastPoint = currentPoint;

    // タイムスタンプ
    if(i == 0){
      strTimeStamp = theList.get(0).timeStamp;
      text(theList.get(0).channel, theList.get(0).p.x + 10, theList.get(0).p.y, theList.get(0).p.z);
    }
  }
}

// OSC送信
void sendOSC(ArrayList<dotObj> theList, OscMessage message, String prefix){

  if(theList.size() > 0){
    // 通信オブジェクトの初期化
    message.clear();
    // prefixの指定
    message.setAddrPattern(prefix);
    // 値の指定(下記のように続けて追加することでPureDataではlistとして扱うことができる)
    // message.add(mouseX);
    // message.add(mouseY);

    float average = 0;
    for(int i = 0; i < theList.size(); i++){
      average += theList.get(i).val;
    }
    average = average / theList.size();
    //println(average);

    for(int i = 0; i < theList.size(); i++){
      float val = map(theList.get(i).val, -0.7, 0.7, 0, 127);
      // float zero_shift_val = theList.get(i).val - average;
      // float val = map(zero_shift_val, -0.02, 0.02, 0, 127);
      message.add(val);
    }

    // OSCの送信
    oscP5.send(message, myRemoteLocation);
  }
}

// 点クラス
class dotObj{
  PVector p;  // 座標
  float val;  // 値
  int channel;  // チャンネル
  String timeStamp;  // 日付

  // コンストラクタ
  dotObj(){}

  dotObj(float x, float y, float z){
    this.p = new PVector(x, y, z);
  }
}
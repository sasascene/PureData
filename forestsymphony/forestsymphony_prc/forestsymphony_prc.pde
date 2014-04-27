import java.util.*;
import java.text.SimpleDateFormat;
import processing.opengl.*;
import java.util.ArrayList;  
import java.util.Collections;  
import java.util.Comparator;  
import java.util.List;

import oscP5.*;
import netP5.*;
  
OscP5 oscP5;
NetAddress myRemoteLocation;

// 通信オブジェクト(引数にprefixを指定)
OscMessage myMessage = new OscMessage("/prefix1");

float _noiseSeed; // ノイズの種
ArrayList<dotObj> dotList;  // 球上の点リスト

int i;

int counter = 0;

// 初期設定
void setup(){ 
  size(1000, 400, OPENGL);
  smooth();
  frameRate(3);   // 5秒に1回のデータ更新
  i = 1;

  // ノイズの種をランダムに設定
  _noiseSeed = random(10);

  // 球上の点リスト
  dotList = new ArrayList<dotObj>();

  // procressingの受信ポート(pdの送信ポート)
  oscP5 = new OscP5(this,7704);
  
  // procressingの送信ポート(pdの受信ポート)
  myRemoteLocation = new NetAddress("127.0.0.1",7703);
}

// 描画
void draw(){

  // xivelyからのデータ取得
  String lines[] = getData();

  // 取得したデータの可視化
  showData(lines);

  // 取得したデータの可視化
  drawData();

  //println(frameCount);
  sendOSC();
}

// xivelyからのデータ取得
String[] getData(){

  // 現在の日付を取得
  Calendar cal = Calendar.getInstance();
  i = i + 60; // 60分間隔でデータを取得
  // 指定した日数を加算
  cal.add(Calendar.DATE, -9);
  // 指定した分を加算
  cal.add(Calendar.MINUTE, i);
  // 指定した日付をData型で取得
  Date theDate = cal.getTime();

  // 指定した日付をISO 8601(UTC)フォーマットで文字列化
  SimpleDateFormat dateFormat = getDateFormat();
  String startDate = dateFormat.format(theDate);
  
  // xivelyへのURLリクエストを作成
  //String URL = "https://api.xively.com/v2/feeds/1138710374.csv";  // ハンガリー
  String URL = "https://api.xively.com/v2/feeds/155754594.csv"; // 出雲神社
  String MY_KEY ="7DGFbpKeGma3UanSeYVdSeI47yAt87DGWOux0nYtZnwIzH5s";
  String URL_REQUEST = URL + "?start=" + startDate + "?key=" + MY_KEY;

  //println(URL_REQUEST);
  
  String[] lines = new String[0];
  try {
    // xivelyからのデータ取得
    lines = loadStrings(URL_REQUEST);
    //println("there are " + lines.length + " lines");
  }catch (Exception e) {
    // 例外発生時の処理
  }

  return lines;
}

// 日付フォーマットの取得
SimpleDateFormat getDateFormat(){
  // ISO 8601(UTC)　のフォーマットを作成
  String format = "yyyy-MM-dd'T'HH:mm:00.000000'Z'";
  SimpleDateFormat sdfIso8601ExtendedFormatUtc = new SimpleDateFormat(format);
  sdfIso8601ExtendedFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

  return sdfIso8601ExtendedFormatUtc;
}

// 取得したデータの可視化
void showData(String[] lines){
  
  try{
    // データを取得できた場合のみリストを初期化
    if(lines.length > 0){
      dotList.clear();
    }

    // 全データを参照し、値(要素2)のみを取り出す
    for (int i = 0 ; i < lines.length; i++) {
      //println(lines[i]);
      String data [] = split(lines[i], ',');  // ','で区切り配列に格納
      String theData = data[2]; // 要素2が値
      // 値の描画
      //line(i*20, height/2, i*20, float(theData)*1000 + height);
      //line(i*10, height/2, i*10, float(theData)*200 + height/2);

      dotObj theObj = new dotObj();
      theObj.p = new PVector(i * 10.0, float(theData)*400 * noise(_noiseSeed) + height/2, 0);
      dotList.add(theObj);
      _noiseSeed += 0.1;
    }
  } catch (Exception e){
    // 例外発生時の処理
  }
}

// 取得したデータの可視化
void drawData(){

  // リストに要素がある場合、描画の設定を指定
  if(dotList.size() > 0){
    background(240, 20);
    strokeWeight(0.8);
    stroke(random(20)+40);
  }

  PVector lastP = new PVector(0, height/2, 0);
  PVector theP = new PVector(0, height/2, 0);

  // 前の点と現在の点を線で結ぶ
  for(int i = 0; i < dotList.size(); i++){
    theP = dotList.get(i).p;
    line(lastP.x, lastP.y, lastP.z, theP.x, theP.y, theP.z);
    lastP = theP;
  }
}

void mousePressed(){
  // OSC送信
  sendOSC();
}

// OSC送信
void sendOSC(){
  // 通信オブジェクトの初期化
  myMessage.clear();
  // prefixの指定
  myMessage.setAddrPattern("/prefix1");
  // 値の指定(下記のように続けて追加することでPureDataではlistとして扱うことができる)
  //myMessage.add(mouseX);
  // myMessage.add(mouseY);
  for(int i = 0; i < dotList.size(); i++){
     myMessage.add(dotList.get(i).p.y);
  }
  for(int i = 0; i < dotList.size(); i++){
     myMessage.add(dotList.get(i).p.y);
  }
  for(int i = 0; i < dotList.size(); i++){
     myMessage.add(dotList.get(i).p.y);
  }
  for(int i = 0; i < dotList.size(); i++){
     myMessage.add(dotList.get(i).p.y);
  }
  for(int i = 0; i < dotList.size(); i++){
     myMessage.add(dotList.get(i).p.y);
  }
  // OSCの送信
  oscP5.send(myMessage, myRemoteLocation);
}

// 点クラス
class dotObj{
  PVector p;  // 座標
  float d;    // 指定した点との距離

  // コンストラクタ
  void dotObj(){}

  void dotObj(float x, float y, float z){
    p = new PVector(x, y, z);
  }
}


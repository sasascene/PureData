import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import java.text.SimpleDateFormat; 
import processing.opengl.*; 
import java.util.ArrayList; 
import java.util.Collections; 
import java.util.Comparator; 
import java.util.List; 
import oscP5.*; 
import netP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class xively_csv extends PApplet {




  
  
  




  
OscP5 oscP5;
NetAddress myRemoteLocation;

// \u901a\u4fe1\u30aa\u30d6\u30b8\u30a7\u30af\u30c8(\u5f15\u6570\u306bprefix\u3092\u6307\u5b9a)
OscMessage myMessage = new OscMessage("/prefix1");

float _noiseSeed; // \u30ce\u30a4\u30ba\u306e\u7a2e
ArrayList<dotObj> dotList;  // \u7403\u4e0a\u306e\u70b9\u30ea\u30b9\u30c8

int i;

int counter = 0;

// \u521d\u671f\u8a2d\u5b9a
public void setup(){ 
  size(1000, 400, OPENGL);
  smooth();
  frameRate(3);   // 5\u79d2\u306b1\u56de\u306e\u30c7\u30fc\u30bf\u66f4\u65b0
  i = 1;

  // \u30ce\u30a4\u30ba\u306e\u7a2e\u3092\u30e9\u30f3\u30c0\u30e0\u306b\u8a2d\u5b9a
  _noiseSeed = random(10);

  // \u7403\u4e0a\u306e\u70b9\u30ea\u30b9\u30c8
  dotList = new ArrayList<dotObj>();

  // procressing\u306e\u53d7\u4fe1\u30dd\u30fc\u30c8(pd\u306e\u9001\u4fe1\u30dd\u30fc\u30c8)
  oscP5 = new OscP5(this,7704);
  
  // procressing\u306e\u9001\u4fe1\u30dd\u30fc\u30c8(pd\u306e\u53d7\u4fe1\u30dd\u30fc\u30c8)
  myRemoteLocation = new NetAddress("127.0.0.1",7703);
}

// \u63cf\u753b
public void draw(){

  // xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97
  String lines[] = getData();

  // \u53d6\u5f97\u3057\u305f\u30c7\u30fc\u30bf\u306e\u53ef\u8996\u5316
  showData(lines);

  // \u53d6\u5f97\u3057\u305f\u30c7\u30fc\u30bf\u306e\u53ef\u8996\u5316
  drawData();

  //println(frameCount);
  sendOSC();
}

// xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97
public String[] getData(){

  // \u73fe\u5728\u306e\u65e5\u4ed8\u3092\u53d6\u5f97
  Calendar cal = Calendar.getInstance();
  i = i + 60; // 60\u5206\u9593\u9694\u3067\u30c7\u30fc\u30bf\u3092\u53d6\u5f97
  // \u6307\u5b9a\u3057\u305f\u65e5\u6570\u3092\u52a0\u7b97
  cal.add(Calendar.DATE, -9);
  // \u6307\u5b9a\u3057\u305f\u5206\u3092\u52a0\u7b97
  cal.add(Calendar.MINUTE, i);
  // \u6307\u5b9a\u3057\u305f\u65e5\u4ed8\u3092Data\u578b\u3067\u53d6\u5f97
  Date theDate = cal.getTime();

  // \u6307\u5b9a\u3057\u305f\u65e5\u4ed8\u3092ISO 8601(UTC)\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u3067\u6587\u5b57\u5217\u5316
  SimpleDateFormat dateFormat = getDateFormat();
  String startDate = dateFormat.format(theDate);
  
  // xively\u3078\u306eURL\u30ea\u30af\u30a8\u30b9\u30c8\u3092\u4f5c\u6210
  //String URL = "https://api.xively.com/v2/feeds/1138710374.csv";  // \u30cf\u30f3\u30ac\u30ea\u30fc
  String URL = "https://api.xively.com/v2/feeds/155754594.csv"; // \u51fa\u96f2\u795e\u793e
  String MY_KEY ="7DGFbpKeGma3UanSeYVdSeI47yAt87DGWOux0nYtZnwIzH5s";
  String URL_REQUEST = URL + "?start=" + startDate + "?key=" + MY_KEY;

  //println(URL_REQUEST);
  
  String[] lines = new String[0];
  try {
    // xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97
    lines = loadStrings(URL_REQUEST);
    //println("there are " + lines.length + " lines");
  }catch (Exception e) {
    // \u4f8b\u5916\u767a\u751f\u6642\u306e\u51e6\u7406
  }

  return lines;
}

// \u65e5\u4ed8\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u306e\u53d6\u5f97
public SimpleDateFormat getDateFormat(){
  // ISO 8601(UTC)\u3000\u306e\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u3092\u4f5c\u6210
  String format = "yyyy-MM-dd'T'HH:mm:00.000000'Z'";
  SimpleDateFormat sdfIso8601ExtendedFormatUtc = new SimpleDateFormat(format);
  sdfIso8601ExtendedFormatUtc.setTimeZone(TimeZone.getTimeZone("UTC"));

  return sdfIso8601ExtendedFormatUtc;
}

// \u53d6\u5f97\u3057\u305f\u30c7\u30fc\u30bf\u306e\u53ef\u8996\u5316
public void showData(String[] lines){
  
  try{
    // \u30c7\u30fc\u30bf\u3092\u53d6\u5f97\u3067\u304d\u305f\u5834\u5408\u306e\u307f\u30ea\u30b9\u30c8\u3092\u521d\u671f\u5316
    if(lines.length > 0){
      dotList.clear();
    }

    // \u5168\u30c7\u30fc\u30bf\u3092\u53c2\u7167\u3057\u3001\u5024(\u8981\u7d202)\u306e\u307f\u3092\u53d6\u308a\u51fa\u3059
    for (int i = 0 ; i < lines.length; i++) {
      //println(lines[i]);
      String data [] = split(lines[i], ',');  // ','\u3067\u533a\u5207\u308a\u914d\u5217\u306b\u683c\u7d0d
      String theData = data[2]; // \u8981\u7d202\u304c\u5024
      // \u5024\u306e\u63cf\u753b
      //line(i*20, height/2, i*20, float(theData)*1000 + height);
      //line(i*10, height/2, i*10, float(theData)*200 + height/2);

      dotObj theObj = new dotObj();
      theObj.p = new PVector(i * 10.0f, PApplet.parseFloat(theData)*400 * noise(_noiseSeed) + height/2, 0);
      dotList.add(theObj);
      _noiseSeed += 0.1f;
    }
  } catch (Exception e){
    // \u4f8b\u5916\u767a\u751f\u6642\u306e\u51e6\u7406
  }
}

// \u53d6\u5f97\u3057\u305f\u30c7\u30fc\u30bf\u306e\u53ef\u8996\u5316
public void drawData(){

  // \u30ea\u30b9\u30c8\u306b\u8981\u7d20\u304c\u3042\u308b\u5834\u5408\u3001\u63cf\u753b\u306e\u8a2d\u5b9a\u3092\u6307\u5b9a
  if(dotList.size() > 0){
    background(240, 20);
    strokeWeight(0.8f);
    stroke(random(20)+40);
  }

  PVector lastP = new PVector(0, height/2, 0);
  PVector theP = new PVector(0, height/2, 0);

  // \u524d\u306e\u70b9\u3068\u73fe\u5728\u306e\u70b9\u3092\u7dda\u3067\u7d50\u3076
  for(int i = 0; i < dotList.size(); i++){
    theP = dotList.get(i).p;
    line(lastP.x, lastP.y, lastP.z, theP.x, theP.y, theP.z);
    lastP = theP;
  }
}

public void mousePressed(){
  // OSC\u9001\u4fe1
  sendOSC();
}

// OSC\u9001\u4fe1
public void sendOSC(){
  // \u901a\u4fe1\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u306e\u521d\u671f\u5316
  myMessage.clear();
  // prefix\u306e\u6307\u5b9a
  myMessage.setAddrPattern("/prefix1");
  // \u5024\u306e\u6307\u5b9a(\u4e0b\u8a18\u306e\u3088\u3046\u306b\u7d9a\u3051\u3066\u8ffd\u52a0\u3059\u308b\u3053\u3068\u3067PureData\u3067\u306flist\u3068\u3057\u3066\u6271\u3046\u3053\u3068\u304c\u3067\u304d\u308b)
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
  // OSC\u306e\u9001\u4fe1
  oscP5.send(myMessage, myRemoteLocation);
}

// \u70b9\u30af\u30e9\u30b9
class dotObj{
  PVector p;  // \u5ea7\u6a19
  float d;    // \u6307\u5b9a\u3057\u305f\u70b9\u3068\u306e\u8ddd\u96e2

  // \u30b3\u30f3\u30b9\u30c8\u30e9\u30af\u30bf
  public void dotObj(){}

  public void dotObj(float x, float y, float z){
    p = new PVector(x, y, z);
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "xively_csv" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

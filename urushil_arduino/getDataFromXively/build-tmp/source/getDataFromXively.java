import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.opengl.*; 
import java.util.*; 
import java.text.SimpleDateFormat; 
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

public class getDataFromXively extends PApplet {

// \u30e9\u30a4\u30d6\u30e9\u30ea\u306e\u30a4\u30f3\u30dd\u30fc\u30c8




// =====================================================
// OSC


OscP5 oscP5;
NetAddress myRemoteLocation;
// =====================================================

// =====================================================
// \u30b0\u30ed\u30fc\u30d0\u30eb\u5909\u6570
// String URL = "https://api.xively.com/v2/feeds/155754594.csv"; // \u51fa\u96f2\u795e\u793e
String URL = "https://api.xively.com/v2/feeds/604049346.csv"; // \u3084\u307e\u3057\u304e\u306e\u675c (\u5bae\u5d0e\u770c\u8af8\u585a\u6751\uff09
// String URL = "https://api.xively.com/v2/feeds/176655154.csv"; // \u5317\u6d77\u9053
// String URL = "https://api.xively.com/v2/feeds/638051676.csv"; // \u718a\u91ce\u795e\u793e\uff08\u5c71\u53e3\u5e02\u718a\u91ce\uff09
// String URL = "https://api.xively.com/v2/feeds/534301954.csv"; // \u30aa\u30fc\u30b9\u30c8\u30e9\u30ea\u30a2
String MY_KEY = "7DGFbpKeGma3UanSeYVdSeI47yAt87DGWOux0nYtZnwIzH5s";
String strTimeStamp = "";
ArrayList<ArrayList<dotObj>> dotLists;
// =====================================================

// \u521d\u671f\u8a2d\u5b9a
public void setup(){ 

  size(420, 200, OPENGL);
  background(60);
  frameRate(0.2f);
  smooth();

  // procressing\u306e\u53d7\u4fe1\u30dd\u30fc\u30c8(pd\u306e\u9001\u4fe1\u30dd\u30fc\u30c8)
  oscP5 = new OscP5(this, 7704);
  
  // procressing\u306e\u9001\u4fe1\u30dd\u30fc\u30c8(pd\u306e\u53d7\u4fe1\u30dd\u30fc\u30c8)
  myRemoteLocation = new NetAddress("127.0.0.1", 7703);

  dotLists = new ArrayList<ArrayList<dotObj>>();
}

// \u63cf\u753b
public void draw(){

  // \u30b3\u30f3\u30bd\u30fc\u30eb\u306b\u30bf\u30a4\u30e0\u30b9\u30bf\u30f3\u30d7\u3092\u8868\u793a
  Calendar cal = Calendar.getInstance();
  Date theDate = cal.getTime();
  println("now : " + theDate);
  
  // \u30c7\u30fc\u30bf\u53d6\u5f97
  getAllData();

  // \u63cf\u753b
  drawAllData();

  // \u30d1\u30e9\u30e1\u30fc\u30bf\u306e\u63cf\u753b
  drawParam();
}

// \u30d1\u30e9\u30e1\u30fc\u30bf\u306e\u63cf\u753b
public void drawParam(){

  stroke(30, 80);

  // \u8ef8
  float zero = map(0, -0.5f, 0.5f, height/4, height);
  line(0, zero, width, zero);

  // \u30d1\u30e9\u30e1\u30fc\u30bf
  text("URL: " + URL, 10, 20);
  text("KEY: " + MY_KEY, 10, 35);
  text(strTimeStamp, 10, height-15);
}

// \u30c7\u30fc\u30bf\u53d6\u5f97
public void getAllData(){

  // xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97
  String lines[] = getDataFromXively(URL, MY_KEY);

  // \u53d6\u5f97\u3057\u305f\u30c7\u30fc\u30bf\u3092\u30ea\u30b9\u30c8\u306b\u683c\u7d0d
  dotLists = new ArrayList<ArrayList<dotObj>>();

  // \u30c1\u30e3\u30f3\u30cd\u30eb\u306e\u53d6\u5f97
  int max_channel = 0;
  int min_channel = 9999;
  for (int i = 0; i < lines.length; i++) {
    String data[] = split(lines[i], ',');
    int theChannel = PApplet.parseInt(data[0]);
    if(min_channel > theChannel){
      min_channel = theChannel;
    }
    if(max_channel < theChannel){
      max_channel = theChannel;
    }
  }

  // \u5168\u30c1\u30e3\u30f3\u30cd\u30eb\u306e\u30c7\u30fc\u30bf\u3092\u53d6\u5f97\u3057\u3001\u30ea\u30b9\u30c8\u306b\u683c\u7d0d
  for(int channel = min_channel; channel <= max_channel; channel++){
    ArrayList<dotObj> dotList = getDataList(lines, channel);
    dotLists.add(dotList);
  }
}

public void drawAllData(){
  // \u30ea\u30b9\u30c8\u306b\u8981\u7d20\u304c\u3042\u308b\u5834\u5408\u306e\u307f\u753b\u9762\u306e\u521d\u671f\u5316\u304a\u3088\u3073\u63cf\u753b\u306e\u8a2d\u5b9a\u3092\u884c\u3046
  if(dotLists.size() > 0){
    background(60);
  }

  // \u5168\u30c1\u30e3\u30f3\u30cd\u30eb\u306e\u30c7\u30fc\u30bf\u3092\u63cf\u753b\u3001OSC\u3067\u9001\u4fe1
  for(int i = 0; i < dotLists.size(); i++){
    // \u63cf\u753b
    drawData(dotLists.get(i));

    // \u9001\u4fe1
    String prefix = "/prefix" + i;
    OscMessage myMessage = new OscMessage(prefix);
    sendOSC(dotLists.get(i), myMessage, prefix);
  }
}

// xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97
// \u73fe\u5728\u306e\u65e5\u4ed8\u304b\u30892\u65e5\u3055\u304b\u306e\u307c\u3063\u305f\u65e5\u4ed8\u304b\u3089\u30c7\u30fc\u30bf\u3092\u53d6\u5f97
public String[] getDataFromXively_Current(String URL, String MY_KEY){

  // // \u73fe\u5728\u306e\u65e5\u4ed8\u3092\u53d6\u5f97
  // Calendar cal = Calendar.getInstance();
  // // \u6307\u5b9a\u3057\u305f\u65e5\u6570\u3092\u52a0\u7b97
  // cal.add(Calendar.DATE, -6);

  // // \u57fa\u6e96\u65e5(\u73fe\u5728\u3088\u308a7\u65e5\u524d\u306e\u65e5\u4ed8\u3092\u57fa\u6e96\u65e5\u3068\u3057\u3066\u3044\u308b)\u3088\u308a\u5f8c\u306e\u65e5\u4ed8\u306b\u306a\u3063\u305f\u5834\u5408\u306f\u521d\u671f\u5316
  // Calendar before1 = Calendar.getInstance();
  // before1.add(Calendar.DATE, -1);
  // int result = cal.compareTo(before1);
  // if(result > 0){
  //   cal = Calendar.getInstance();
  //   cal.add(Calendar.DATE, -2);
  // }
  // // \u6307\u5b9a\u3057\u305f\u65e5\u4ed8\u3092Data\u578b\u3067\u53d6\u5f97
  // Date theDate = cal.getTime();
  // println("data: " + theDate);

  // // \u6307\u5b9a\u3057\u305f\u65e5\u4ed8\u3092ISO 8601(UTC)\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u3067\u6587\u5b57\u5217\u5316
  // SimpleDateFormat dateFormat = getDateFormat_ISO8601();
  // String startDate = dateFormat.format(theDate);
  // println("format: " + startDate);
  
  // \u623b\u308a\u5024\u7528\u914d\u5217
  String[] retlines = new String[0];
  String[] dataLines = new String[0];

  // \u6587\u5b57\u5217\u683c\u7d0d\u7528\u306e\u52d5\u7684\u914d\u5217
  ArrayList<String> strList = new ArrayList<String>();

  // xively\u3078\u306eURL\u30ea\u30af\u30a8\u30b9\u30c8\u3092\u4f5c\u6210
  String URL_REQUEST = URL + "?key=" + MY_KEY;

  println(URL_REQUEST);

  // xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97(\u53d6\u5f97\u3067\u304d\u306a\u3044\u5834\u5408\u306f\u4f55\u3082\u3057\u306a\u3044)
  try {
    // xivly\u304b\u3089\u306e\u30c7\u30fc\u30bf\u3092\u4e00\u6642\u7684\u306b\u683c\u7d0d
    dataLines = loadStrings(URL_REQUEST);
  }catch (Exception e) {
    // \u53d6\u5f97\u3067\u304d\u306a\u3044\u5834\u5408
    e.printStackTrace();
    dataLines = null;
  }

  // \u53d6\u5f97\u3067\u304d\u306a\u3044\u5834\u5408
  if(dataLines == null){
    background(60);
    text("error!", width/2, height/2);
    dataLines = new String[0];
    strList.clear();
  }

  // xivly\u304b\u3089\u306e\u30c7\u30fc\u30bf\u3092\u52d5\u7684\u914d\u5217\u306b\u8ffd\u52a0
  for(int i = 0; i < dataLines.length; i++){
    strList.add(dataLines[i]);
  }

  // \u623b\u308a\u5024\u7528\u914d\u5217\u3078\u30c7\u30fc\u30bf\u3092\u30b3\u30d4\u30fc
  retlines = new String[strList.size()];
  for(int i = 0; i < strList.size(); i++){
    retlines[i] = strList.get(i);
  }

  return retlines;
}

// xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97
// \u73fe\u5728\u306e\u65e5\u4ed8\u304b\u30892\u65e5\u3055\u304b\u306e\u307c\u3063\u305f\u65e5\u4ed8\u304b\u3089\u30c7\u30fc\u30bf\u3092\u53d6\u5f97
public String[] getDataFromXively(String URL, String MY_KEY){

  // \u73fe\u5728\u306e\u65e5\u4ed8\u3092\u53d6\u5f97
  Calendar cal = Calendar.getInstance();
  // \u6307\u5b9a\u3057\u305f\u65e5\u6570\u3092\u52a0\u7b97
  cal.add(Calendar.DATE, -10);

  // \u57fa\u6e96\u65e5(\u73fe\u5728\u3088\u308a7\u65e5\u524d\u306e\u65e5\u4ed8\u3092\u57fa\u6e96\u65e5\u3068\u3057\u3066\u3044\u308b)\u3088\u308a\u5f8c\u306e\u65e5\u4ed8\u306b\u306a\u3063\u305f\u5834\u5408\u306f\u521d\u671f\u5316
  Calendar before1 = Calendar.getInstance();
  before1.add(Calendar.DATE, -1);
  int result = cal.compareTo(before1);
  if(result > 0){
    cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -2);
  }
  // \u6307\u5b9a\u3057\u305f\u65e5\u4ed8\u3092Data\u578b\u3067\u53d6\u5f97
  Date theDate = cal.getTime();
  println("data: " + theDate);

  // \u6307\u5b9a\u3057\u305f\u65e5\u4ed8\u3092ISO 8601(UTC)\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u3067\u6587\u5b57\u5217\u5316
  SimpleDateFormat dateFormat = getDateFormat_ISO8601();
  String startDate = dateFormat.format(theDate);
  println("format: " + startDate);
  
  // \u623b\u308a\u5024\u7528\u914d\u5217
  String[] retlines = new String[0];
  String[] dataLines = new String[0];

  // \u6587\u5b57\u5217\u683c\u7d0d\u7528\u306e\u52d5\u7684\u914d\u5217
  ArrayList<String> strList = new ArrayList<String>();

  // xively\u3078\u306eURL\u30ea\u30af\u30a8\u30b9\u30c8\u3092\u4f5c\u6210
  // String strDuration = "&duration=1days&interval=300";  // 300\u79d2\u9593\u9694\u30671\u65e5\u5206\u306e\u30c7\u30fc\u30bf\u3092\u53d6\u5f97
  // String URL_REQUEST = URL + "?start=" + startDate + strDuration + "&limit=450" + "?key=" + MY_KEY;
  String strDuration = "&duration=6hours&interval=0";  // 1\u79d2\u9593\u9694\u30671\u65e5\u5206\u306e\u30c7\u30fc\u30bf\u3092\u53d6\u5f97
  String URL_REQUEST = URL + "?start=" + startDate + strDuration + "&limit=450" + "?key=" + MY_KEY;

  println(URL_REQUEST);

  // xively\u304b\u3089\u306e\u30c7\u30fc\u30bf\u53d6\u5f97(\u53d6\u5f97\u3067\u304d\u306a\u3044\u5834\u5408\u306f\u4f55\u3082\u3057\u306a\u3044)
  try {
    // xivly\u304b\u3089\u306e\u30c7\u30fc\u30bf\u3092\u4e00\u6642\u7684\u306b\u683c\u7d0d
    dataLines = loadStrings(URL_REQUEST);
  }catch (Exception e) {
    // \u53d6\u5f97\u3067\u304d\u306a\u3044\u5834\u5408
    e.printStackTrace();
    dataLines = null;
  }

  // \u53d6\u5f97\u3067\u304d\u306a\u3044\u5834\u5408
  if(dataLines == null){
    background(60);
    text("error!", width/2, height/2);
    dataLines = new String[0];
    strList.clear();
  }

  // xivly\u304b\u3089\u306e\u30c7\u30fc\u30bf\u3092\u52d5\u7684\u914d\u5217\u306b\u8ffd\u52a0
  for(int i = 0; i < dataLines.length; i++){
    strList.add(dataLines[i]);
  }

  // \u623b\u308a\u5024\u7528\u914d\u5217\u3078\u30c7\u30fc\u30bf\u3092\u30b3\u30d4\u30fc
  retlines = new String[strList.size()];
  for(int i = 0; i < strList.size(); i++){
    retlines[i] = strList.get(i);
  }

  return retlines;
}

// \u65e5\u4ed8\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u306e\u53d6\u5f97
public SimpleDateFormat getDateFormat_ISO8601(){

  // ISO 8601(UTC)\u3000\u306e\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u3092\u4f5c\u6210
  String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.000000'Z'";
  // String timeZoneName = "UTC";
  String timeZoneName = "JST";
  SimpleDateFormat sdfIso8601ExtendedFormatUtc = new SimpleDateFormat(dateFormat);
  sdfIso8601ExtendedFormatUtc.setTimeZone(TimeZone.getTimeZone(timeZoneName));

  return sdfIso8601ExtendedFormatUtc;
}

// \u30c7\u30fc\u30bf\u3092\u30ea\u30b9\u30c8\u306b\u683c\u7d0d\u3057\u3066\u53d6\u5f97
public ArrayList<dotObj> getDataList(String[] lines, int targetChannel){

  ArrayList<dotObj> retList = new ArrayList<dotObj>();

  // \u5168\u30c7\u30fc\u30bf\u3092\u53c2\u7167\u3057\u3066\u5fc5\u8981\u306a\u60c5\u5831\u3092\u53d6\u308a\u51fa\u3059
  int index = 0;
  for (int i = 0; i < lines.length; i++) {
    // ','\u3067\u533a\u5207\u308a\u914d\u5217\u306b\u683c\u7d0d
    String data[] = split(lines[i], ',');
    // channel, date, valuse \u306e\u9806\u3067\u30c7\u30fc\u30bf\u304c\u683c\u7d0d\u3055\u308c\u3066\u3044\u308b
    int channel = PApplet.parseInt(data[0]); // \u30c1\u30e3\u30f3\u30cd\u30eb
    String timeStamp = data[1]; // \u65e5\u4ed8
    float val = PApplet.parseFloat(data[2]); // \u5024

    if(channel == targetChannel){
      // println("channel: " + channel);
      // println("date   : " + timeStamp);
      // println("val    : " + val);

      // \u53d6\u5f97\u3057\u305f\u30c7\u30fc\u30bf\u3092\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u306b\u30bb\u30c3\u30c8\u3057\u3001\u914d\u5217\u306b\u683c\u7d0d
      float m = map(val, -0.5f, 0.5f, height/4, height);
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

// \u53d6\u5f97\u3057\u305f\u30c7\u30fc\u30bf\u306e\u53ef\u8996\u5316
public void drawData(ArrayList<dotObj> theList){

  // \u73fe\u5728\u53c2\u7167\u3059\u308b\u70b9\u3068\u76f4\u524d\u306b\u53c2\u7167\u3057\u305f\u70b9\u3092\u683c\u7d0d\u3059\u308b\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u3092\u751f\u6210
  PVector currentPoint = new PVector(0, height/2, 0);
  PVector lastPoint = new PVector(0, height/2, 0);

  // \u73fe\u5728\u306e\u70b9\u3068\u76f4\u524d\u306e\u70b9\u3092\u7dda\u3067\u7d50\u3076
  for(int i = 0; i < theList.size(); i++){
    // \u73fe\u5728\u306e\u70b9\u3092\u53d6\u5f97
    currentPoint = theList.get(i).p;
    // \u73fe\u5728\u306e\u70b9\u3068\u76f4\u524d\u306e\u70b9\u3092\u7dda\u3067\u7d50\u3076
    strokeWeight(0.8f);
    stroke(200, 80);
    // stroke(random(30)+200, 20);
    line(lastPoint.x, lastPoint.y, lastPoint.z, currentPoint.x, currentPoint.y, currentPoint.z);
    // \u76f4\u524d\u306e\u70b9\u3092\u73fe\u5728\u306e\u70b9\u3067\u66f4\u65b0
    lastPoint = currentPoint;

    // \u30bf\u30a4\u30e0\u30b9\u30bf\u30f3\u30d7
    if(i == 0){
      strTimeStamp = theList.get(0).timeStamp;
      text(theList.get(0).channel, theList.get(0).p.x + 10, theList.get(0).p.y, theList.get(0).p.z);
    }
  }
}

// OSC\u9001\u4fe1
public void sendOSC(ArrayList<dotObj> theList, OscMessage message, String prefix){

  if(theList.size() > 0){
    // \u901a\u4fe1\u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u306e\u521d\u671f\u5316
    message.clear();
    // prefix\u306e\u6307\u5b9a
    message.setAddrPattern(prefix);
    // \u5024\u306e\u6307\u5b9a(\u4e0b\u8a18\u306e\u3088\u3046\u306b\u7d9a\u3051\u3066\u8ffd\u52a0\u3059\u308b\u3053\u3068\u3067PureData\u3067\u306flist\u3068\u3057\u3066\u6271\u3046\u3053\u3068\u304c\u3067\u304d\u308b)
    // message.add(mouseX);
    // message.add(mouseY);

    float average = 0;
    for(int i = 0; i < theList.size(); i++){
      average += theList.get(i).val;
    }
    average = average / theList.size();
    //println(average);

    for(int i = 0; i < theList.size(); i++){
      float val = map(theList.get(i).val, -0.7f, 0.7f, 0, 127);
      // float zero_shift_val = theList.get(i).val - average;
      // float val = map(zero_shift_val, -0.02, 0.02, 0, 127);
      message.add(val);
    }

    // OSC\u306e\u9001\u4fe1
    oscP5.send(message, myRemoteLocation);
  }
}

// \u70b9\u30af\u30e9\u30b9
class dotObj{
  PVector p;  // \u5ea7\u6a19
  float val;  // \u5024
  int channel;  // \u30c1\u30e3\u30f3\u30cd\u30eb
  String timeStamp;  // \u65e5\u4ed8

  // \u30b3\u30f3\u30b9\u30c8\u30e9\u30af\u30bf
  dotObj(){}

  dotObj(float x, float y, float z){
    this.p = new PVector(x, y, z);
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "getDataFromXively" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

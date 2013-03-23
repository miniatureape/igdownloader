import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
	
public class RequestMgr
  extends Thread
{
  String hashtag = "";
  JSONParser json = null;
  File outputDir;
  HttpClient client;
  private volatile Boolean stopFlag = Boolean.valueOf(false);
  ArrayList<String> prevDownloaded = null;
  Logger logger;
  Boolean debug = Boolean.valueOf(false);
  
  public RequestMgr(String hashtag, File outputDir, ArrayList<String> prevDownloaded) {
    this.hashtag = hashtag.trim();
    this.outputDir = outputDir;
    this.client = new DefaultHttpClient();
    this.prevDownloaded = (prevDownloaded == null ? new ArrayList() : prevDownloaded);
    this.json = new JSONParser();
    this.logger = new Logger("reqmgr-log");
  } 
  
  public ArrayList<String> getResults()
  {
    String urlStr = getIGramUrlStr();
    this.logger.log("Starting Request Process: " + urlStr);
    
    ArrayList results = new ArrayList();
    HttpClient client = new DefaultHttpClient();
    HttpGet req = new HttpGet(urlStr);
    HttpResponse response = getHttpResponse(client, req);
    
    if (response != null) {
      ArrayList images = getImageListFromEntity(response.getEntity());
      results = downloadImages(images);
    } else {
      this.logger.log("No Valid Response");
    } 
    
    return results;
  } 
  
  private String getIGramUrlStr() {
    return "https://api.instagram.com/v1/tags/" + this.hashtag + "/media/recent?client_id=fbb13314c6b34723a09523ab3521acca";
  } 
  
  protected HttpResponse getHttpResponse(HttpClient client, HttpGet req) {
    HttpResponse response = null;
    
    if (this.stopFlag.booleanValue()) {
      return response;
    } 
    try
    {
      response = client.execute(req);
    } catch (ClientProtocolException e) {
      this.logger.log("Could not make request Client Protocol Exception: " + e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      this.logger.log("Could not make request IO Exception: " + e.getMessage());
      e.printStackTrace();
    } 
    return response;
  } 
  
  protected ArrayList<String> getImageListFromEntity(HttpEntity entity) {
    String content = null;
    ArrayList imageUrls = new ArrayList();
    try
    {
      content = EntityUtils.toString(entity);
    } catch (org.apache.http.ParseException e) {
      this.logger.log("Could not parse response content: " + e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      this.logger.log("Could not read response content: " + e.getMessage());
      e.printStackTrace();
    } 
    
    JSONObject results = null;
    try {
      results = (JSONObject)this.json.parse(content);
    } catch (org.json.simple.parser.ParseException e) {
      this.logger.log("Could not parse json content: " + e.getMessage());
      e.printStackTrace();
    } 
    
    JSONArray data = (JSONArray)results.get("data");
    for (int i = 0; i < data.size(); i++) {
      JSONObject images = (JSONObject)((JSONObject)data.get(i)).get("images");
      String url = (String)((JSONObject)images.get("standard_resolution")).get("url");
      imageUrls.add(url);
    } 
    
    return imageUrls;
  } 
  
  protected ArrayList<String> downloadImages(ArrayList<String> images) {
    URL url = null;
    ArrayList downloaded = new ArrayList();
    Image image = null;
    Boolean success = Boolean.valueOf(true);
    
    for (int i = 0; i < images.size(); i++)
    {
      if (!this.stopFlag.booleanValue())
      {
        try
        {
          url = new URL((String)images.get(i));
        } catch (MalformedURLException e) {
          this.logger.log("Url is malformed. " + e.getMessage());
          success = Boolean.valueOf(false);
          e.printStackTrace();
        } 
        
        if (!this.prevDownloaded.contains(getNameFromUrl(url)))
        {
          try
          {
            image = ImageIO.read(url);
          } catch (IOException e) {
            this.logger.log("Could not read Image from " + url.toString());
            success = Boolean.valueOf(false);
            e.printStackTrace();
          } 
          
          String fullpath = null;
          try {
            if (image != null) {
              String[] parts = url.getFile().split("/");
              String name = parts[(parts.length - 1)];
              fullpath = this.outputDir.getAbsolutePath() + File.separatorChar + name;
              ImageIO.write((RenderedImage)image, "jpg", new File(fullpath));
            } 
          } catch (IOException e) {
            this.logger.log("Could not write Image to " + fullpath);
            success = Boolean.valueOf(false);
            e.printStackTrace();
          } 
          
          if (success.booleanValue()) {
            downloaded.add((String)images.get(i));
            this.prevDownloaded.add(getNameFromUrl(url));
          } 
        } 
      } 
    } 
    return downloaded;
  } 
  
  private String getNameFromUrl(URL url) {
    String[] parts = url.getFile().split("/");
    String name = parts[(parts.length - 1)];
    return name;
  } 
  
  public void run() {
    this.logger.log("Request Manager Run Called");
    
    while (!this.stopFlag.booleanValue()) {
      getResults();
      try {
        Thread.sleep(2000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } 
    } 
    this.logger.close();
  } 
  
  public synchronized void end()
  {
    this.logger.log("Called End");
    this.stopFlag = Boolean.valueOf(true);
  } 
  
  public void setDebug(Boolean debug) {
    this.logger.setEnabled(debug);
  } 
} 

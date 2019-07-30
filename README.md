# Android HTTPLib
A small Android Java library built using [HttpURLConnection](https://developer.android.com/reference/java/net/HttpURLConnection) for handling HTTP POST and GET requests.

## Features

1. HTTP POST request with JSON data
2. HTTP POST request with Multipart Form data and file upload
3. HTTP GET requests with URL parameters

## How to include

To include this library in your project, add a new Java class named 'HTTPLib' in your project and copy-paste the whole code from 'HTTPLib.java'. After pasting the code, change the package name (currently set as 'shubhadeep.com.utils') to your project's package name.

Note: HTTPLib needs 'android.permission.INTERNET' to run. Add the following line to the manifest 
```
<uses-permission android:name="android.permission.INTERNET" />
```

## Compatibility

Tested on API version 21+ (Android 5.0 or above). 

## Usage

### 1. JSON POST Request :

**1.a. Create JSONPostUtility Object**
```
HTTPLib.JSONPostUtility jsonPostUtility = new HTTPLib().new JSONPostUtility("https://postman-echo.com/post");
```
**1.b. Add Headers (optional)**
```
jsonPostUtility.addHeader("AuthToken", "abcd123");
```
**1.c. Add JSON Parameters**
```
jsonPostUtility.addParameter("title", "New Post");
jsonPostUtility.addParameter("body", "This is a post");
jsonPostUtility.addParameter("userId", 1);
```
**1.d. Finish the request and get the response**
```
HTTPLib.HTTPResponse response = jsonPostUtility.finish();
int statusCode = response.getResponseCode();
String responseText = response.getResponseBody();
```
Note: response.getResponseCode() will return -1 if HTTPLib fails to connect the server for any reason (e.g. network error, server offline etc.). 

### 2. Multipart Form POST Request :

**2.a. Create MultipartPostUtility Object**
```
HTTPLib.MultipartPostUtility multipartPostUtility = new HTTPLib().new MultipartPostUtility("https://postman-echo.com/post");
```
**2.b. Add Headers (optional)**
```
multipartPostUtility.addHeader("AuthToken", "abcd123");
```
**2.c. Add Form Parameters**
```
multipartPostUtility.addParameter("title", "New Post");
multipartPostUtility.addParameter("body", "This is a post");
multipartPostUtility.addParameter("userId", 1);
```
**2.d. Add Files**
```
File file = new File(path);
byte[] bytes = new byte[(int) file.length()];
BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
buf.read(bytes, 0, bytes.length);
buf.close();
multipartPostUtility.addFile("myfile", file.getName(), bytes);
```
Note : Reading file needs 'android.permission.READ_EXTERNAL_STORAGE'. Add the following line to the manifest  
```
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
**2.e. Finish the request and get the response**
```
HTTPLib.HTTPResponse response = multipartPostUtility.finish();
int statusCode = response.getResponseCode();
String responseText = response.getResponseBody();
```
Note: response.getResponseCode() will return -1 if HTTPLib fails to connect the server for any reason (e.g. network error, server offline etc.). 

### 3. HTTP GET Request :

**3.a. Create JSONPostUtility Object**
```
HTTPLib.HTTPGetUtility httpGetUtility = new HTTPLib().new HTTPGetUtility("https://postman-echo.com/get");
```
**3.b. Add Headers (optional)**
```
httpGetUtility.addHeader("AuthToken", "abcd123");
```
**3.c. Add JSON Parameters**
```
httpGetUtility.addParameter("userId", 1);
httpGetUtility.addParameter("key", "New Post");
```
**3.d. Finish the request and get the response**
```
HTTPLib.HTTPResponse response = httpGetUtility.finish();
int statusCode = response.getResponseCode();
String responseText = response.getResponseBody();
```
Note: response.getResponseCode() will return -1 if HTTPLib fails to connect the server for any reason (e.g. network error, server offline etc.). 


package com.rntensorflowlite.imagerecognition;

import com.facebook.react.bridge.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class RNTFLiteImageRecognizerModule extends ReactContextBaseJavaModule {

  private Map<Integer, TFLiteImageRecognizer> imageRecognizers = new HashMap<>();
  private final ReactApplicationContext reactContext;

  public RNTFLiteImageRecognizerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNTFLiteImageRecognition";
  }
  
  @ReactMethod
  public void createImageRecognizer(ReadableMap data, Promise promise) {
	try {
		String modelPath = data.getString("model");
        String labelPath = data.getString("labels");
		
		TFLiteImageRecognizer ImageRecognizer = TFLiteImageRecognizer.create(this.reactContext, modelPath, labelPath);
		imageRecognizers.put(1, ImageRecognizer);
		promise.resolve(true);
	} catch (Exception e) {
		promise.reject(e);
	}
  }
  
  @ReactMethod
  public void recognize(ReadableMap data, Promise promise) {
	try {
		String image = data.getString("image");
        Integer inputShape = data.hasKey("inputShape") ? data.getInt("inputShape") : 224;
		
		TFLiteImageRecognizer ImageRecognizer = imageRecognizers.get(1);
		WritableArray result = ImageRecognizer.recognizeImage(image, inputShape);
		promise.resolve(result);
	} catch (Exception e) {
		promise.reject(e);
	}
  }
  
  @ReactMethod
  public void close(Promise promise) {
	 try {
		imageRecognizers.remove(1);
		promise.resolve(true);
	 } catch (Exception e) {
		promise.reject(e);
	 }
  }
}
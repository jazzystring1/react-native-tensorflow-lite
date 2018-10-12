
package com.rntensorflowlite.imagerecognition;

import com.facebook.react.bridge.*;
import com.rntensorflowlite.Classifier;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import org.tensorflow.lite.Interpreter;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.lang.System;
import java.util.*;

public class TFLiteImageRecognizer implements Classifier {

  private TFLiteImageRecognizer ImageRecognizer;
  private ReactContext reactContext;
  
  private static final int MAX_RESULTS = 3;
  private static final int BATCH_SIZE = 1;
  private static final int PIXEL_SIZE = 3;
  private static final float THRESHOLD = 0.1f;

  long initialTime;
  long finalTime;
  
  private Interpreter classifier;
  private int inputShape;
  private List<String> labelList;

  
  public TFLiteImageRecognizer() {
  }
  
  public static TFLiteImageRecognizer create(ReactContext reactContext, String modelPath, String labelPath) throws IOException {
		TFLiteImageRecognizer imageRecognizer = new TFLiteImageRecognizer();
		imageRecognizer.reactContext = reactContext;
		imageRecognizer.classifier = new Interpreter(imageRecognizer.loadModelFile(reactContext.getAssets(), modelPath));
		imageRecognizer.labelList = imageRecognizer.loadLabelList(reactContext.getAssets(), labelPath);
		
		return imageRecognizer;
  }
  
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
	
	@Override
    public WritableArray recognizeImage(final String image, final Integer inputShape) {
		this.inputShape = inputShape;
		
		Bitmap bitmap = convertToBitmap(loadResource(image));
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        float[][] result = new float[1][labelList.size()];
		initialTime = System.currentTimeMillis();
        classifier.run(byteBuffer, result);
		finalTime = System.currentTimeMillis() - initialTime;
        return getSortedResult(result);
	}
	
	public Boolean hasFilePrefix(String resource) {
		if(resource.startsWith("file://")) {
			return true;
		}
		return false;
	}
	
	private byte[] loadResource(String resource) {
		try {
			InputStream inputStream = this.reactContext.getAssets().open(hasFilePrefix(resource) ? resource.substring(7) : resource);
			return inputStreamToByteArray(inputStream);
		} catch (Exception e) {
			try {
				InputStream inputStream = new FileInputStream(hasFilePrefix(resource) ? resource.substring(7) : resource);
					return inputStreamToByteArray(inputStream);
				} catch (Exception e2) {
					return null;
				}
		}
					
	}
	
	private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
		try {
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);
			return b;
		} catch (Exception e) {
			return null;
		}
    }
	
	private Bitmap convertToBitmap(byte[] image) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmapRaw) {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * this.inputShape * this.inputShape * PIXEL_SIZE * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		int[] intValues = new int[this.inputShape * this.inputShape];
		
		Bitmap scaledBitmap = Bitmap.createBitmap(this.inputShape, this.inputShape, Bitmap.Config.ARGB_8888);
        Matrix matrix = createMatrix(bitmapRaw.getWidth(), bitmapRaw.getHeight(), this.inputShape, this.inputShape);
        final Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(bitmapRaw, matrix, null);		
		scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
		int pixel = 0;
		for (int i = 0; i < this.inputShape; ++i) {
			for (int j = 0; j < this.inputShape; ++j) {
				final int val = intValues[pixel++];
				byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
				byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
				byteBuffer.putFloat((val & 0xFF) / 255.0f);
			}
		}
		return byteBuffer;
		
    }


    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }
	
	@SuppressLint("DefaultLocale")
    private WritableArray getSortedResult(float[][] labelProbArray) {

		List<WritableMap> results = new ArrayList<>();
		
        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = (labelProbArray[0][i] * 100) / 127.0f;
            if (confidence > THRESHOLD) {
				WritableMap entry = new WritableNativeMap();
				entry.putString("id", String.valueOf(i));
                entry.putString("name", labelList.size() > i ? labelList.get(i) : "unknown");
                entry.putDouble("confidence", confidence);
				entry.putString("inference", String.valueOf(finalTime));
                results.add(entry);
            }
        }
		
		Collections.sort(results, new Comparator<ReadableMap>() {
            @Override
            public int compare(ReadableMap first, ReadableMap second) {
                return Double.compare(second.getDouble("confidence"), first.getDouble("confidence"));
            }
        });

        WritableArray recognitions = new WritableNativeArray();
        int recognitionsSize = Math.min(results.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.pushMap(results.get(i));
        }

        return recognitions;
	}
	
	private Matrix createMatrix(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        Matrix matrix = new Matrix();

        if (srcWidth != dstWidth || srcHeight != dstHeight) {
            float scaleFactorX = dstWidth / (float) srcWidth;
            float scaleFactorY = dstHeight / (float) srcHeight;
            float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
            matrix.postScale(scaleFactor, scaleFactor);
        }

        matrix.invert(new Matrix());
        return matrix;
    }
	

    @Override
    public void close() {
        this.classifier.close();
        this.classifier = null;
	}
}
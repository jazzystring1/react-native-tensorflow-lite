
# react-native-tensorflow-lite

A react native library for running Tensorflow Lite Image Recognition on Android app.

## Installing

`$ npm install react-native-tensorflow-lite --save`

### Linking

`$ react-native link react-native-tensorflow-lite`

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNTensorflowLitePackage;` to the imports at the top of the file
  - Add `new RNTensorflowLitePackage()` to the list returned by the `getPackages()` method
  - Add the following lines to your app's build.gradle(`android/app/build.gradle`):
  	```
  	android {
		aaptOptions {
		   noCompress 'tflite'
		   noCompress 'lite'
		}
	}
  	```

## Usage
Place your tflite model file and labels.txt in your app's asset folder. 

```javascript
import {TFLiteImageRecognition} from 'react-native-tensorflow-lite';

class MyImageClassifier extends Component {

  constructor() {
    super()
    this.state = {}

    try {
	  // Initialize Tensorflow Lite Image Recognizer
      this.classifier = new TFLiteImageRecognition({
        model: "mymodel.tflite",  // Your tflite model in assets folder.
        labels: "label.txt" // Your label file
      })

    } catch(err) {
      alert(err)
    }
  }

  componentWillMount() {
	this.classifyImage("apple.jpg") // Your image path.
  }
  
  async classifyImage(imagePath) {
	try {
      const results = await this.classifier.recognize({
        image: imagePath, // Your image path.
        inputShape: 224, // the input shape of your model. If none given, it will be default to 224.
      })

      const resultObj = {
				name: "Name: " + results[0].name,  
				confidence: "Confidence: " + results[0].confidence, 
				inference: "Inference: " + results[0].inference + "ms"
			};
      this.setState(resultObj)
		
    } catch(err) {
      alert(err)
    }   
  }
  
  componentWillUnmount() {
    this.classifier.close() // Must close the classifier when destroying or unmounting component to release object.
  }

  render() {
    return (
      <View style={styles.container}>
        <View>
          <Text style={styles.results}>
            {this.state.name}
          </Text>
          <Text style={styles.results}>
            {this.state.confidence}
          </Text>
          <Text style={styles.results}>
            {this.state.inference}
          </Text>
        </View>
      </View>
    );
  }
}
```
  
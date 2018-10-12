import { NativeModules, Image } from 'react-native';

const { RNTFLiteImageRecognition } = NativeModules;

class TFLiteImageRecognition {
	constructor(data) {
		data['model'] = Image.resolveAssetSource(data['model']) != null
		  ? Image.resolveAssetSource(data['model']).uri
		  : data['model']

		data['labels'] = Image.resolveAssetSource(data['labels']) != null
		  ? Image.resolveAssetSource(data['labels']).uri
		  : data['labels']  
		
		this.state = RNTFLiteImageRecognition.createImageRecognizer(data);
	}
	
	async recognize(data) {
		await this.state;
		
		data['image'] = Image.resolveAssetSource(data['image']) != null
		  ? Image.resolveAssetSource(data['image']).uri
		  : data['image']
		  
		return RNTFLiteImageRecognition.recognize(data);
	}
	
	async close() {
		await this.state;
		
		RNTFLiteImageRecognition.close();
	}
	
}

export { TFLiteImageRecognition };

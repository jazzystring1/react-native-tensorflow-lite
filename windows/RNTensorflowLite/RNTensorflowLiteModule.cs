using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Tensorflow.Lite.RNTensorflowLite
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNTensorflowLiteModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNTensorflowLiteModule"/>.
        /// </summary>
        internal RNTensorflowLiteModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNTensorflowLite";
            }
        }
    }
}

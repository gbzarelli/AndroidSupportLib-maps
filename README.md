# AndroidSupportLib-maps
Library developed to assist in the development of Android applications with Google Maps API, with several classes and helper methods. This project uses the AndroidSupportLib library.

How to use AndroidSupportLib-maps in your Android Project:

Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}

Step 2. Add the dependency

	dependencies {
	        compile 'com.github.helpdeveloper:AndroidSupportLib-maps:1.1.0'
	}

Step 3. Configure your project

  Add meta-data in Manifest.xml

      <meta-data
      android:name="com.google.android.geo.API_KEY"
      android:value="@string/google_maps_key" />
  
  Configure your Google Maps key:
    
    <![CDATA[
	    <resources>
	      <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
		  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
	      </string>
	    </resources>
    ]]>
    
Questions/Help: https://developers.google.com/maps/documentation/android/start#get-key
     


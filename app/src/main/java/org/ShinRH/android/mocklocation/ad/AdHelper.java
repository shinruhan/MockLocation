package org.ShinRH.android.mocklocation.ad;

import org.ShinRH.android.mocklocation.MyContext;
import org.ShinRH.android.mocklocation.R;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;


import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;
/**
 * An ad Helper that handle all ad events.
 */
public class AdHelper extends AdListener {
    private Context mContext;
	private AdView mAdView;
	private InterstitialAd mInterstitial;
	private AdRequest mAdRequest;
	
	public AdHelper(Context context, AdView adview, String device_id , String interstitial_id) {
		this.mContext = context;
		this.mAdView = adview;

		mAdView.setAdListener(this);
		mAdView.setBackgroundColor(Color.TRANSPARENT);
		AdRequest.Builder builder = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
				
		if (device_id != null) {
			builder.addTestDevice(device_id);
		}
		
		mAdRequest = builder.build();
		
		mInterstitial = new InterstitialAd(mContext);
	    mInterstitial.setAdUnitId(interstitial_id);
	    
		mAdView.loadAd(mAdRequest);
		mInterstitial.loadAd(mAdRequest);
		
	}


    public void showInterstitial(View unusedView) {
        if (mInterstitial.isLoaded()) {
            mInterstitial.show();
        } else {
        	mInterstitial.loadAd(mAdRequest);
        }
    }
    
    @Override
    public void onAdLoaded() {
        Toast.makeText(mContext, "onAdLoaded()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        String errorReason = "";
        switch(errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No fill";
                break;
        }
        Toast.makeText(mContext, String.format("onAdFailedToLoad(%s)", errorReason),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdOpened() {
        Toast.makeText(mContext, "onAdOpened()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdClosed() {
        Toast.makeText(mContext, "onAdClosed()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAdLeftApplication() {
        Toast.makeText(mContext, "onAdLeftApplication()", Toast.LENGTH_SHORT).show();
    }
    

	public void onPause() {
		mAdView.pause();
	}

	public void onResume() {
		mAdView.resume();
	}

	public void onDestroy() {
		mAdView.destroy();
	}
    
    
}

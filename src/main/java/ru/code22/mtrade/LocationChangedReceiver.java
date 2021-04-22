package ru.code22.mtrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred. This is used instead of a LocationListener
 * within an Activity is our only action is to start a service.
 */

public class LocationChangedReceiver extends BroadcastReceiver {
	
	protected static String TAG = "LocationChangedReceiver";

	/**
	   * When a new location is received, extract it from the Intent and use
	   * it to start the Service used to update the list of nearby places.
	   * 
	   * This is the Active receiver, used to receive Location updates when 
	   * the Activity is visible. 
	   */
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String locationKey = LocationManager.KEY_LOCATION_CHANGED;
	    String providerEnabledKey = LocationManager.KEY_PROVIDER_ENABLED;
	    if (intent.hasExtra(providerEnabledKey)) {
	      if (!intent.getBooleanExtra(providerEnabledKey, true)) {
	        Intent providerDisabledIntent = new Intent(Constants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
	        context.sendBroadcast(providerDisabledIntent);    
	      }
	    }
	    if (intent.hasExtra(locationKey)) {
	      Location location = (Location)intent.getExtras().get(locationKey);
	      // TODO 2015
	      //Log.d(TAG, "Actively Updating place list");
	      //Intent updateServiceIntent = new Intent(context, PlacesConstants.SUPPORTS_ECLAIR ? EclairPlacesUpdateService.class : PlacesUpdateService.class);
	      //updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_LOCATION, location);
	      //updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_RADIUS, PlacesConstants.DEFAULT_RADIUS);
	      //updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_FORCEREFRESH, true);
	      //context.startService(updateServiceIntent);
	    }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

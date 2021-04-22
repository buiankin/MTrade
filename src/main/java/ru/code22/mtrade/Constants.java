package ru.code22.mtrade;

public final class Constants {
	
	// TODO брать из ресурсов
	//public static final int SOFTWARE_VER=303; // java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION 
	//public static final String SOFTWARE_VER_TXT="Mobile Trade 3.00";
	
    //public static final boolean MY_DEBUG = true;
	public static final boolean MY_DEBUG = false;
	public static final boolean MY_INFOSTART = false;

	public static final boolean FIREBASE_MESSAGING = false;

    public static final double MAX_QUANTITY = 90000000.0; // в 1С значение должно быть выставлено больше этого
	
	public static int max_image_side_size=1600;

	public static final boolean ENABLE_PRICE_TYPE_SELECT = false;
	
	public static final String emptyID="     0   ";
	public static final String emptyID36="00000000-0000-0000-0000-000000000000";

	public static String ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED = "ru.code22.mtrade.active_location_update_provider_disabled";
	
    private Constants() {
         throw new Error();
     }
}
package ai.txai.common.router;

/**
 * Activity 的PATH 一定要以Activity结尾
 * Fragment 的PATH 一定要以Fragment结尾
 *
 * @Authtor Hay
 */
public class ARouterConstants {
    //commonbiz
    public final static String PATH_ACTIVITY_V2_MAIN = "/commonbiz/MainV2Activity";
    public final static String PATH_ACTIVITY_SEARCH = "/commonbiz/SearchActivity";
    public final static String PATH_ACTIVITY_REQUEST_RIDE = "/commonbiz/RequestRideActivity";
    public final static String PATH_ACTIVITY_SEARCH_IN_MAP = "/commonbiz/SearchInMapActivity";
    public final static String PATH_ACTIVITY_PENDING = "/commonbiz/PendingActivity";
    public final static String PATH_ACTIVITY_ARRIVING = "/commonbiz/ArrivingActivity";
    public final static String PATH_ACTIVITY_ARRIVED = "/commonbiz/ArrivedActivity";
    public final static String PATH_ACTIVITY_CHARGING = "/commonbiz/ChargingActivity";
    public final static String PATH_ACTIVITY_FINISH = "/commonbiz/FinishedActivity";
    public final static String PATH_ACTIVITY_COMPLETE = "/commonbiz/CompleteActivity";
    public final static String PATH_ACTIVITY_CANCEL = "/commonbiz/CancelActivity";
    public final static String PATH_ACTIVITY_TRIP_LIST = "/commonbiz/TripListActivity";

    //common
    public final static String PATH_ACTIVITY_CONTAINER = "/common/ContainerActivity";

    //fragment
    //commonbiz
    public final static String PATH_FRAGMENT_MAPBOX = "/commonbiz/MapBoxFragment";
    public final static String PATH_FRAGMENT_EXPEND_DETAILS = "/commonbiz/ExpendDetailsFragment";
    public final static String PATH_FRAGMENT_ESTIMATED_FARE = "/commonbiz/EstimatedFareFragment";
    public final static String PATH_FRAGMENT_FARE_BREAKDOWN = "/commonbiz/FareBreakdownFragment";
    public final static String PATH_FRAGMENT_TRIP_LIST = "/commonbiz/TripListFragment";


    //login
    public final static String PATH_ACTIVITY_LOGIN = "/login/LoginActivity";
    //personal information
    public final static String PATH_ACTIVITY_PERSONAL = "/personal/PersonalActivity";
    //Feedback
    public final static String PATH_ACTIVITY_FEEDBACK = "/feedback/FeedbackActivity";
    //setting
    public final static String PATH_ACTIVITY_SETTING = "/setting/SettingActivity";


    //service
    public final static String PATH_SERVICE_BIZ = "/serviceBiz/BizProvider";
    public final static String PATH_SERVICE_LOST_FOUND = "/serviceLF/LostFoundProvider";
    public final static String PATH_SERVICE_PAYMENT = "/servicePm/PaymentProvider";

    public final static String PATH_ACTIVITY_LF = "/lostFound/LostFoundMainActivity";
    public final static String PATH_FRAGMENT_LOST_REPORT = "/lostFound/LostReportFragment";
    public final static String PATH_FRAGMENT_LOST_SUCCESS_SUBMIT = "/lostFound/LostSuccessFragment";
    public final static String PATH_FRAGMENT_LOST_LIST = "/lostFound/LostFoundListFragment";
    public final static String PATH_FRAGMENT_LOST_PROGRESS = "/lostFound/LostProgressFragment";

    //payment
    public final static String PATH_ACTIVITY_PAYMENT = "/payment/PaymentActivity";
    public final static String PATH_FRAGMENT_PAYMENT_METHOD_LIST = "/payment/MethodListFragment";
    public final static String PATH_FRAGMENT_PAYMENT_ADD_CARD = "/payment/AddCardFragment";
    public final static String PATH_FRAGMENT_CVV_INPUT = "/payment/CvvInputFragment";
    public final static String PATH_FRAGMENT_WEB = "/payment/PaymentWebFragment";
    public final static String PATH_FRAGMENT_PAYBY = "/payment/PaybyFragment";
}

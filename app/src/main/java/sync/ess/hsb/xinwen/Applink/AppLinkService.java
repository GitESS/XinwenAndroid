package sync.ess.hsb.xinwen.Applink;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.ford.syncV4.exception.SyncException;
import com.ford.syncV4.exception.SyncExceptionCause;
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.TTSChunkFactory;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALM;
import com.ford.syncV4.proxy.rpc.AddCommandResponse;
import com.ford.syncV4.proxy.rpc.AddSubMenuResponse;
import com.ford.syncV4.proxy.rpc.AlertResponse;
import com.ford.syncV4.proxy.rpc.ChangeRegistrationResponse;
import com.ford.syncV4.proxy.rpc.Choice;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSet;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteCommandResponse;
import com.ford.syncV4.proxy.rpc.DeleteFileResponse;
import com.ford.syncV4.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteSubMenuResponse;
import com.ford.syncV4.proxy.rpc.DialNumberResponse;
import com.ford.syncV4.proxy.rpc.EndAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.GenericResponse;
import com.ford.syncV4.proxy.rpc.GetDTCsResponse;
import com.ford.syncV4.proxy.rpc.GetVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.ListFilesResponse;
import com.ford.syncV4.proxy.rpc.OnAudioPassThru;
import com.ford.syncV4.proxy.rpc.OnButtonEvent;
import com.ford.syncV4.proxy.rpc.OnButtonPress;
import com.ford.syncV4.proxy.rpc.OnCommand;
import com.ford.syncV4.proxy.rpc.OnDriverDistraction;
import com.ford.syncV4.proxy.rpc.OnHMIStatus;
import com.ford.syncV4.proxy.rpc.OnLanguageChange;
import com.ford.syncV4.proxy.rpc.OnPermissionsChange;
import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.ford.syncV4.proxy.rpc.PerformAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.PerformInteraction;
import com.ford.syncV4.proxy.rpc.PerformInteractionResponse;
import com.ford.syncV4.proxy.rpc.PutFileResponse;
import com.ford.syncV4.proxy.rpc.ReadDIDResponse;
import com.ford.syncV4.proxy.rpc.ResetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.ScrollableMessageResponse;
import com.ford.syncV4.proxy.rpc.SetAppIconResponse;
import com.ford.syncV4.proxy.rpc.SetDisplayLayoutResponse;
import com.ford.syncV4.proxy.rpc.SetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.SetMediaClockTimerResponse;
import com.ford.syncV4.proxy.rpc.ShowResponse;
import com.ford.syncV4.proxy.rpc.SliderResponse;
import com.ford.syncV4.proxy.rpc.SoftButton;
import com.ford.syncV4.proxy.rpc.SpeakResponse;
import com.ford.syncV4.proxy.rpc.SubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.SubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.TTSChunk;
import com.ford.syncV4.proxy.rpc.UnsubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.DriverDistractionState;
import com.ford.syncV4.proxy.rpc.enums.InteractionMode;
import com.ford.syncV4.proxy.rpc.enums.SoftButtonType;
import com.ford.syncV4.proxy.rpc.enums.SystemAction;
import com.ford.syncV4.proxy.rpc.enums.TextAlignment;
import com.ford.syncV4.util.DebugTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import sync.ess.hsb.xinwen.entity.News;
import sync.ess.hsb.xinwen.parser.RSSItem;
import sync.ess.hsb.xinwen.parser.RSSParser;
import sync.ess.hsb.xinwen.ui.MainActivity;
import sync.ess.hsb.xinwen.util.Utils;

/**
 * Created by Administrator on 10/21/2014.
 */
public class AppLinkService extends Service implements IProxyListenerALM  {


    String TAG = "hemant";

    //variable used to increment correlation ID for every request sent to SYNC
    public int autoIncCorrId = 0;
    //variable to contain the current state of the service
    private static AppLinkService instance = null;
    //variable to contain the current state of the main UI ACtivity
    private MainActivity currentUIActivity;
    //variable to access the BluetoothAdapter
    private BluetoothAdapter mBtAdapter;
    //variable to create and call functions of the SyncProxy
    private SyncProxyALM proxy = null;
    //variable that keeps track of whether SYNC is sending driver distractions
    //(older versions of SYNC will not send this notification)
    private boolean driverdistrationNotif = false;
    //variable to contain the current state of the lockscreen
    private boolean lockscreenUP = false;

    //variable which defines NewsCategoryList
    private static final int NEWS_COMMAND_ID = 2001;

    private int news_counter;

    private static final String NEWSCAT_NAME[] = {"Top News", "India", "World", "China", "Sports", "Sci-tech", "Business", "Entertainment"};
    private static final int NEWSCAT_ID[] = {201, 202, 203, 204, 205, 206, 207, 208};

    //temp StringArray
    private static final String Temp_NEWSCAT_NAME[] = {"Anup", "Anuppp", "Anupppp", "Android", "Ios suks", "windows ", "Lenovo","Anup", "Anuppp", "Anupppp", "Android", "Ios suks", "windows ", "Lenovo"};

    private static final int NEWS_CAT_TITLE_ID = 3001;
    private static final int NEWS_TITLE_ID = 301;


    //Vr Commmands

    private static final int VR_NEXT_OPTION = 1001;
    private static final int VR_PREVIOUS_OPTION = 1002;
    private static final int VR_MORE_OPTION = 1003;
//    private int VRCommandSelected = 0;
//    private static final int vrNext = 0;
//    private static final int vrPrevious = 1;
//    private static final int vrMore = 2;

    CountDownTimer questionTimer;
    boolean isCountDownTimer;
    List<News> newsList;

    public static AppLinkService getInstance() {
        return instance;
    }

    public MainActivity getCurrentActivity() {
        return currentUIActivity;
    }

    public SyncProxyALM getProxy() {
        return proxy;
    }

    public void setCurrentActivity(MainActivity currentActivity) {
        this.currentUIActivity = currentActivity;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBtAdapter != null) {
                if (mBtAdapter.isEnabled()) {
                    startProxy();
                }
            }
        }
        if (MainActivity.getInstance() != null) {
            setCurrentActivity(MainActivity.getInstance());
        }

        return START_STICKY;
    }

    public void startProxy() {
        if (proxy == null) {
            try {
                proxy = new SyncProxyALM(this, "Xinwen", true, "438316430");
            } catch (SyncException e) {
                e.printStackTrace();
                //error creating proxy, returned proxy = null
                if (proxy == null) {
                    stopSelf();
                }
            }
        }
    }

    public void onDestroy() {
        disposeSyncProxy();
        clearlockscreen();
        instance = null;
        super.onDestroy();
    }

    public void disposeSyncProxy() {
        if (proxy != null) {
            try {
                proxy.dispose();
            } catch (SyncException e) {
                e.printStackTrace();
            }
            proxy = null;
            clearlockscreen();
        }
    }

    @Override
    public void onProxyClosed(String info, Exception e) {
        clearlockscreen();

        if ((((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.SYNC_PROXY_CYCLED)) {
            if (((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.BLUETOOTH_DISABLED) {
                Log.v(TAG, "reset proxy in onproxy closed");
                reset();
            }
        }
    }

    public void reset() {
        if (proxy != null) {
            try {
                proxy.resetProxy();
            } catch (SyncException e1) {
                e1.printStackTrace();
                //something goes wrong, & the proxy returns as null, stop the service.
                //do not want a running service with a null proxy
                if (proxy == null) {
                    stopSelf();
                }
            }
        } else {
            startProxy();
        }
    }

    @Override
    public void onOnHMIStatus(OnHMIStatus notification) {

        switch (notification.getSystemContext()) {
            case SYSCTXT_MAIN:
                break;
            case SYSCTXT_VRSESSION:
                break;
            case SYSCTXT_MENU:
                break;
            default:
                return;
        }

        switch (notification.getAudioStreamingState()) {
            case AUDIBLE:
                //play audio if applicable
                break;
            case NOT_AUDIBLE:
                //pause/stop/mute audio if applicable
                break;
            default:
                return;
        }

        switch (notification.getHmiLevel()) {
            case HMI_FULL:
                if (driverdistrationNotif == false) {
                    showLockScreen();
                }
                if (notification.getFirstRun()) {
                    //setup app on SYNC
                    //send welcome message if applicable


                    try {
                        proxy.show("Welcome to ", "Xinwen",
                                TextAlignment.CENTERED, autoIncCorrId++);
                        proxy.speak("Welcome to xinwen", autoIncCorrId++);

                    } catch (SyncException e) {
                        DebugTool.logError("Failed to send Show", e);
                    }
                    // send addcommands
                    // subscribe to buttons
                    initializeVoiceCommand();
                    subButtons();

                    initializeSoftButtons();
                    createChoiceSet();


                    if (MainActivity.getInstance() != null) {
                        setCurrentActivity(MainActivity.getInstance());
                    }
                } else {
//                    try {
//                      //  proxy.show("SyncProxy is", "Alive", TextAlignment.CENTERED, autoIncCorrId++);
//                    } catch (SyncException e) {
//                        DebugTool.logError("Failed to send Show", e);
//                    }
                }
                break;
            case HMI_LIMITED:
                if (driverdistrationNotif == false) {
                    showLockScreen();
                }
                break;
            case HMI_BACKGROUND:
                if (driverdistrationNotif == false) {
                    showLockScreen();
                }
                break;
            case HMI_NONE:
                Log.i("hello", "HMI_NONE");
                driverdistrationNotif = false;
                clearlockscreen();
                break;
            default:
                return;
        }
    }

    public void showLockScreen() {
        //only throw up lockscreen if main activity is currently on top
        //else, wait until onResume() to throw lockscreen so it doesn't
        //pop-up while a user is using another app on the phone
        if (currentUIActivity != null) {
            if (currentUIActivity.isActivityonTop() == true) {
//                if(LockScreenActivity.getInstance() == null) {
//                    Intent i = new Intent(this, LockScreenActivity.class);
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    i.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
//                    startActivity(i);
//                }
            }
        }
        lockscreenUP = true;
    }

    private void clearlockscreen() {
//        if(LockScreenActivity.getInstance() != null) {
//            LockScreenActivity.getInstance().exit();
//        }
        lockscreenUP = false;
    }

    public boolean getLockScreenStatus() {
        return lockscreenUP;
    }

    public void subButtons() {
        try {
            proxy.subscribeButton(ButtonName.OK, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.SEEKLEFT, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.SEEKRIGHT, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.TUNEUP, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.TUNEDOWN, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_1, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_2, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_3, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_4, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_5, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_6, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_7, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_8, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_9, autoIncCorrId++);
            proxy.subscribeButton(ButtonName.PRESET_0, autoIncCorrId++);
        } catch (SyncException e) {
        }
    }

    @Override
    public void onOnDriverDistraction(OnDriverDistraction notification) {
        driverdistrationNotif = true;
        //Log.i(TAG, "dd: " + notification.getStringState());
        if (notification.getState() == DriverDistractionState.DD_OFF) {
            Log.i(TAG, "clear lock, DD_OFF");
            clearlockscreen();
        } else {
            Log.i(TAG, "show lockscreen, DD_ON");
            showLockScreen();
        }
    }

    @Override
    public void onError(String info, Exception e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onGenericResponse(GenericResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onOnCommand(OnCommand notification) {

        Log.i("Sync Service" , "onOnCommand - "+notification.getTriggerSource());
        switch (notification.getCmdID()) {
            case VR_MORE_OPTION: // for Choice set

                Log.i("SyncService" , "Choice More");

//                performInteraction();
//
//                VRCommandSelected = vrFriend;
//                refreshOnlineFriendsAndGroup();
                break;

            case VR_PREVIOUS_OPTION:

                Log.i("SyncService" , "Choice Previous");

                news_counter--;
                startCountDownTimerForNews(newsList);
//                performInteractionGroup();
//
//                VRCommandSelected = vrGroup;
//                refreshOnlineFriendsAndGroup();
                break;

            case VR_NEXT_OPTION:
                Log.e("SyncService" , "Choice Next");
                   news_counter++;
                startCountDownTimerForNews(newsList);
                break;

            // case VR_SEND_MESSAGE_ID:
            // sendRecorderMessage();
            //
            // break;
            default:
                break;
            // TODO Auto-generated method stub
        }
    }

    @Override
    public void onAddCommandResponse(AddCommandResponse response) {
        // TODO Auto-generated method stub
        Log.i("sync service" , "on Add Command Response"+response.getCorrelationID() + response.getFunctionName() + response.toString());

    }

    @Override
    public void onAddSubMenuResponse(AddSubMenuResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCreateInteractionChoiceSetResponse(
            CreateInteractionChoiceSetResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onAlertResponse(AlertResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeleteCommandResponse(DeleteCommandResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeleteInteractionChoiceSetResponse(
            DeleteInteractionChoiceSetResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPerformInteractionResponse(PerformInteractionResponse response) {
        // TODO Auto-generated method stub


        Log.e(TAG, "" + response);
        Log.e(TAG, "onPerformInteractionResponse " + response.getResultCode());
        Log.e(TAG, "onPerformInteractionResponse " + response.getInfo());
        Log.e(TAG, "onPerformInteractionResponse " + response.getChoiceID());


//        String rss_link = Utils.PRE_CAT_URL + rss_cat + Utils.POST_CAT_URL;
//        new loadRSSFeedItems().executeOnExecutor(
//                AsyncTask.THREAD_POOL_EXECUTOR, rss_link);
//        NewsReader news = new NewsReader();
//        news.fetchNews();
        String rss_link;
        switch (response.getChoiceID()){

    case 201:
        String cat = NEWSCAT_NAME[0];
        cat=cat.replace(" ","");
         rss_link = Utils.PRE_CAT_URL + cat + Utils.POST_CAT_URL;
        Log.i("sync service", "Url"+rss_link);
        new ReadNewsForCategory().execute(rss_link);
        break;

    case 202:
        rss_link = Utils.PRE_CAT_URL + NEWSCAT_NAME[1] + Utils.POST_CAT_URL;
        new ReadNewsForCategory().execute(rss_link);
        break;
    case 203:
         rss_link = Utils.PRE_CAT_URL + NEWSCAT_NAME[2] + Utils.POST_CAT_URL;
        new ReadNewsForCategory().execute(rss_link);
        break;
    case 204:
         rss_link = Utils.PRE_CAT_URL + NEWSCAT_NAME[3] + Utils.POST_CAT_URL;
        new ReadNewsForCategory().execute(rss_link);
        break;
    case 205:
         rss_link = Utils.PRE_CAT_URL + NEWSCAT_NAME[4] + Utils.POST_CAT_URL;
        new ReadNewsForCategory().execute(rss_link);
        break;
    case 206:
         rss_link = Utils.PRE_CAT_URL + NEWSCAT_NAME[5] + Utils.POST_CAT_URL;
        new ReadNewsForCategory().execute(rss_link);
        break;
    case 207:
       rss_link = Utils.PRE_CAT_URL + NEWSCAT_NAME[6] + Utils.POST_CAT_URL;
        new ReadNewsForCategory().execute(rss_link);
        break;
    case 208:
         rss_link = Utils.PRE_CAT_URL + NEWSCAT_NAME[7] + Utils.POST_CAT_URL;
        new ReadNewsForCategory().execute(rss_link);
        break;

}

//        if(response.getChoiceID()==201)
//        new ReadNewsForCategory().execute();
    }

    @Override
    public void onResetGlobalPropertiesResponse(
            ResetGlobalPropertiesResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
    }

    @Override
    public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onShowResponse(ShowResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSpeakResponse(SpeakResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onOnButtonEvent(OnButtonEvent notification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnButtonPress(OnButtonPress notification) {
        // TODO Auto-generated method stub
        switch (notification.getCustomButtonName()) {

            case 101:
                performInteractionMessage();
                break;

            case 103:
news_counter++;
                startCountDownTimerForNews(newsList);

                break;
        }

    }

    @Override
    public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onOnPermissionsChange(OnPermissionsChange notification) {
        // TODO Auto-generated method stub
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUnsubscribeVehicleDataResponse(
            UnsubscribeVehicleDataResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReadDIDResponse(ReadDIDResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onGetDTCsResponse(GetDTCsResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnVehicleData(OnVehicleData notification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnAudioPassThru(OnAudioPassThru notification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPutFileResponse(PutFileResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteFileResponse(DeleteFileResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onListFilesResponse(ListFilesResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSetAppIconResponse(SetAppIconResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScrollableMessageResponse(ScrollableMessageResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnLanguageChange(OnLanguageChange notification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSliderResponse(SliderResponse response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDialNumberResponse(DialNumberResponse response) {
        // TODO Auto-generated method stub

    }


    private void initializeSoftButtons() {
        // Add Soft button name
        ArrayList<String> SoftButtonName = new ArrayList<String>();
        SoftButtonName.add("News");
        SoftButtonName.add("Vehicle Data");
        SoftButtonName.add("Next");

        ArrayList<Integer> SoftButtonId = new ArrayList<Integer>();
        SoftButtonId.add(101);
        SoftButtonId.add(102);
        SoftButtonId.add(103);

        Vector<SoftButton> vsoftButton = new Vector<SoftButton>();
        SoftButton softButton;
        try {
            for (int i = 0; i < SoftButtonName.size(); i++) {
                softButton = new SoftButton();
                softButton.setText(SoftButtonName.get(i));
                softButton.setSoftButtonID(SoftButtonId.get(i));
                softButton.setType(SoftButtonType.SBT_TEXT);
                softButton.setSystemAction(SystemAction.DEFAULT_ACTION);
                vsoftButton.add(softButton);

            }
            // Send Show RPC:
            proxy.show("", "", "", "", null, vsoftButton, null, null,
                    autoIncCorrId++);
        } catch (SyncException e) {

        }

    }


    //First call CreateChoiceSet to create a ChoiceSet
    private void createChoiceSet() {
        Vector<Choice> commands = new Vector<Choice>();
        // int size= NEWSCAT_NAME.length
        for (int i = 0; i < NEWSCAT_NAME.length; i++) {

            Choice one = new Choice();
            one.setChoiceID(NEWSCAT_ID[i]);
            one.setMenuName(NEWSCAT_NAME[i]);
            one.setVrCommands(new Vector<String>(Arrays
                    .asList(new String[]{NEWSCAT_NAME[i]})));
            one.setImage(null);
            commands.add(one);

        }

        if (!commands.isEmpty()) {
            Log.e(TAG, "send choice set to SYNC");
            CreateInteractionChoiceSet msg2 = new CreateInteractionChoiceSet();
            msg2.setCorrelationID(autoIncCorrId++);
            int choiceSetID = NEWS_COMMAND_ID;
            msg2.setInteractionChoiceSetID(choiceSetID);
            msg2.setChoiceSet(commands);
            try {
                proxy.sendRPCRequest(msg2);
            } catch (SyncException e) {
                Log.e(TAG, "Error sending message: ");
            }
        } else {

        }
    }

    public void performInteractionMessage() {
        PerformInteraction msg = new PerformInteraction();
        msg.setCorrelationID(autoIncCorrId++);
        Vector<Integer> interactionChoiceSetIDs = new Vector<Integer>();
        interactionChoiceSetIDs.add(NEWS_COMMAND_ID);
        Vector<TTSChunk> initChunks = TTSChunkFactory
                .createSimpleTTSChunks("Select Your Category");
        Vector<TTSChunk> helpChunks = TTSChunkFactory
                .createSimpleTTSChunks("please say world or Technology");
        Vector<TTSChunk> timeoutChunks = TTSChunkFactory
                .createSimpleTTSChunks("you miss the chance to pick");
        msg.setInitialPrompt(initChunks);
        msg.setInitialText("Select Your choice");

        msg.setInteractionChoiceSetIDList(interactionChoiceSetIDs);
        msg.setInteractionMode(InteractionMode.BOTH);
        msg.setTimeout(10000);
        msg.setHelpPrompt(helpChunks);
        msg.setTimeoutPrompt(timeoutChunks);
        try {
            proxy.sendRPCRequest(msg);
        } catch (SyncException e) {
            Log.e(TAG, "Error sending message");
        }
    }

  public void readNewsTitles(final List<News> newsList) {
    //final int news_counter = 0;

news_counter = 0;

     startCountDownTimerForNews(newsList);


////      try {
//
//
////          proxy.show("News  "+ 1, ""+realNews.getTitle(),
////                  TextAlignment.CENTERED, autoIncCorrId++);
////          proxy.speak("News  "+ 1+ "."+realNews.getTitle(), autoIncCorrId++);
//         // proxy.speak("News  "+ 1+ "."+realNews.getTitle(), autoIncCorrId++);
//
//      } catch (SyncException e) {
//          e.printStackTrace();
//      }



////       RPCMessage req;
////        req = RPCRequestFactory.buildDeleteInteractionChoiceSet(NEWS_COMMAND_ID, autoIncCorrId++);
////        proxy.sendRPCRequest( req);
//        String[] sampleList= {"A","B","C","D" ,"E","F","G","A","B","C","D" ,"E","F","G"};
//        //newsList =sampleList;
//         int NEWSCAT_ID[] = {301, 302, 303, 304, 305, 306, 307, 308,309, 310, 311, 312, 313, 314, 315, 316};
//
//        Vector<Choice> commands = new Vector<Choice>();
//        // int size= NEWSCAT_NAME.length
//        for (int i = 0; i < newsList.size(); i++) {
//            News realNews = newsList.get(i);
//           // String a=;
//            String newsTitle=realNews.getTitle();
//            Log.i(TAG ,""+newsTitle);
//            Choice one = new Choice();
//            one.setChoiceID(NEWSCAT_ID[i]);
//            //one.setChoiceID(NEWS_TITLE_ID + i);
//            one.setMenuName(sampleList[i]);
//           // one.setMenuName(newsTitle);
//            one.setVrCommands(new Vector<String>(Arrays
//                    .asList(new String[]{newsTitle})));
//            one.setImage(null);
//            commands.add(one);
//        }
//
//        if (!commands.isEmpty()) {
//            Log.e(TAG, "send choice set to SYNC");
//            CreateInteractionChoiceSet msg2 = new CreateInteractionChoiceSet();
//            msg2.setCorrelationID(autoIncCorrId++);
//            int choiceSetID = NEWS_CAT_TITLE_ID;
//            msg2.setInteractionChoiceSetID(choiceSetID);
//            msg2.setChoiceSet(commands);
//            try {
//                proxy.sendRPCRequest(msg2);
//            } catch (SyncException e) {
//                Log.e(TAG, "Error sending message: ");
//            }
//        } else {
//
//        }
//
//        PerformInteraction msg = new PerformInteraction();
//        msg.setCorrelationID(autoIncCorrId++);
//        Vector<Integer> interactionChoiceSetIDs = new Vector<Integer>();
//        interactionChoiceSetIDs.add(NEWS_CAT_TITLE_ID);
//        Vector<TTSChunk> initChunks = TTSChunkFactory
//                .createSimpleTTSChunks("Select Your News");
//        Vector<TTSChunk> helpChunks = TTSChunkFactory
//                .createSimpleTTSChunks("please say world or Technology");
//        Vector<TTSChunk> timeoutChunks = TTSChunkFactory
//                .createSimpleTTSChunks("you miss the chance to pick");
//        msg.setInitialPrompt(initChunks);
//        msg.setInitialText("Select Your choice");
//
//        msg.setInteractionChoiceSetIDList(interactionChoiceSetIDs);
//        msg.setInteractionMode(InteractionMode.VR_ONLY);
//        msg.setTimeout(10000);
//        msg.setHelpPrompt(helpChunks);
//        msg.setTimeoutPrompt(timeoutChunks);
//        try {
//            proxy.sendRPCRequest(msg);
//        } catch (SyncException e) {
//            Log.e(TAG, "Error sending message");
//        }
//
   }

//    @Override
//    public void onTick(long millisUntilFinished) {
//
//        Log.e("sync service", "On Tick is clicked");
//        if(news_counter<newsList.size()) {
//            News new_titles = newsList.get(news_counter);
//            readTitles(new_titles.getTitle(), news_counter);
//            news_counter++;
//        }
//    }
//
//    @Override
//    public void onFinish() {
//
//    }

    public class ReadNewsForCategory extends AsyncTask<String, Void, Void> {
        List<RSSItem> rssItems = new ArrayList<RSSItem>();
        RSSParser rssParser = new RSSParser();
        // onItemDownloaded itemDownloaded;
        public String google_temp_url = "http://news.google.com/news?pz=1&cf=all&ned=us&hl=en&q=sports&cf=all&output=rss";

        //        String rss_cat = "TopNews";
//        String rss_link = Utils.PRE_CAT_URL + rss_cat + Utils.POST_CAT_URL;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            newsList = new ArrayList<News>();

        }

        @Override
        protected Void doInBackground(String... params) {
            // rss link url
//            String rss_url = google_temp_url;
            String rss_url = params[0];


            // list of rss items
            rssItems = rssParser.getRSSFeedItems(rss_url);
            int newscounter = 0;
            // looping through each item
            for (RSSItem item : rssItems) {
                String htmlpars = item.getDescription();
                String newsDescription = rssParser
                        .getDescriptionFromHtml(htmlpars);
                newsList.add(new News(item.getTitle(), item.getDescription(), "" + newscounter));
                newscounter++;
            }
            return null;
        }



               @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG,"on post Execute called");
           readNewsTitles(newsList);
        }
    }




    public void readTitles(String News , int newsId){




        try {
            proxy.show("News  "+ newsId+1, ""+News,
                    TextAlignment.CENTERED, autoIncCorrId++);
            proxy.speak("News  "+ newsId+1+ "."+News, autoIncCorrId++);
        } catch (SyncException e) {
            e.printStackTrace();
        }


    }

    private void initializeVoiceCommand() {
        try {
            Log.i("sync service","voice command initiated");
            proxy.addCommand(
					/* 1002 */VR_NEXT_OPTION,
                    "Next",
                    new Vector<String>(Arrays.asList(new String[] { "NEXT",
                            "NXT", "NEXXT" })), autoIncCorrId++);

            proxy.addCommand(
					/* 1002 */VR_PREVIOUS_OPTION,
                    "Previous",
                    new Vector<String>(Arrays.asList(new String[] { "PREVIOUS",
                            "BACK", "PREVIOUS" })), autoIncCorrId++);
            proxy.addCommand(
					/* 1002 */VR_MORE_OPTION,
                    "More",
                    new Vector<String>(Arrays.asList(new String[] { "MORE",
                            "DETAILS", "MORRE" })), autoIncCorrId++);
        } catch (SyncException e) {
            // Log.e(TAG, "Error adding AddCommands", e);
            e.printStackTrace();
        }
    }

    void startCountDownTimerForNews(final List<News> newsList){

        if(!isCountDownTimer) {
           runCountDownTimer(newsList);
        }else {
            questionTimer.onFinish();
            runCountDownTimer(newsList);
        }
    }

    void runCountDownTimer(final List<News> newsList){

        questionTimer = new CountDownTimer(Long.MAX_VALUE, 10000) {

            // This is called every interval. (Every 10 seconds in this example)
            public void onTick(long millisUntilFinished) {
                Log.d("test", "Timer tick");
                if (news_counter < newsList.size()) {
                    isCountDownTimer = true;
                    News new_titles = newsList.get(news_counter);
                    readTitles(new_titles.getTitle(), news_counter);
                    news_counter++;
                }
            }

            public void onFinish() {
                Log.d("test", "Timer last tick");
                isCountDownTimer = false;
                try {
                    proxy.show("Xinwen", "",
                            TextAlignment.CENTERED, autoIncCorrId++);
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        }.start();
    }



//    void initializeVRCommands(){
//
//        proxy.addCommand(vrNext, "Play", new 						Vector<String>(Arrays.asList(new   String[] {"Next", "Nexxt"})), 	autoIncCorrId++);
//
//    }
}

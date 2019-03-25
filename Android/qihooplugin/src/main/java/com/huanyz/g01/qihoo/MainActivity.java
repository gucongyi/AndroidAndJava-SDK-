package com.huanyz.g01.qihoo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.protocols.CPCallBackMgr;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends UnityPlayerActivity
{
    private static final String SDKTag="====QihooAndroidJar===";
    String gameObject;
    boolean isInitSucc;
    private static final boolean debugMode=true;//上线的时候设置为false
    boolean isExitSDK=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE );
        isInitSucc=false;
        isExitSDK=true;
        //设置全屏
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT > 14) {
            Window _window = getWindow();
            WindowManager.LayoutParams params = _window.getAttributes();
            params.systemUiVisibility= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN;
            _window.setAttributes(params);
        }

        if (Build.VERSION.SDK_INT >= 19 ) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }


        super.onCreate(savedInstanceState);

        /**
         * 此函数原来叫做：init，现在改名为：setActivity
         * 调用其他SDK接口之前必须先调用 setActivity
         * 注意：参数一定是主界面对应 activity 的 context，我们依赖这个 activity 来显示浮窗的，
         *       还有就是这个 activity 的 manifest 属性里添加 android:configChanges="orientation|keyboardHidden|screenSize"
         *       为了防止横竖屏切换时此 activity 重新创建，引起的一些问题。
         */
        /**注意参数：
         * debugMode (第三个参数)：360SDK内部提供了自动检测SDK接入是否完整的功能和输出调试LOG的功能，默认为 false，这两项功能关闭，true则打开这两个功能，上线前请务必设置为 false
         * */
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Matrix.setActivity(MainActivity.this, mSDKCallback, debugMode);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Matrix.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Matrix.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Matrix.onPause(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        Matrix.onStop(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Matrix.onRestart(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Matrix.onActivityResult(this,requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Matrix.onNewIntent(this,intent);
    }

    public void onDestroy() {
        super.onDestroy();
        Matrix.destroy(this);
    }

    public void initSDK(String gameObject) {
        if(isInitSucc==false){
            Log.d(SDKTag,SDKTag+" init fail!");
            return;
        }
        this.gameObject = gameObject;
        UnityPlayer.UnitySendMessage(this.gameObject, "onInitCallBack", "0");
    }

    public void doLogin() {

        doSdkLogin(true);
    }
    public void doLogout(){
        doSdkLogout();
    }

    public void doSwitchAccount(){
        getSwitchAccountIntent(true);
    }
    public void doExitSDK() {

        doSdkQuit(true);
    }


    //---------------------------------360接口------------------------------------
    /**
     * 使用360SDK的登录接口
     *
     * @param isLandScape 是否横屏显示登录界面
     */
    protected void doSdkLogin(boolean isLandScape) {
        if(isInitSucc==false){
            Log.d(SDKTag,SDKTag+" init fail!");
            return;
        }
        Intent intent = getLoginIntent(isLandScape);
        IDispatcherCallback callback = mLoginCallback;
        Matrix.execute(this, intent, callback);
    }

    // ------------------注销登录----------------
    protected void doSdkLogout(){
        if(isInitSucc==false){
            Log.d(SDKTag,SDKTag+" init fail!");
            return;
        }
        Intent intent = getLogoutIntent();
        IDispatcherCallback callback =mLogoutCallBack;
        Matrix.execute(this, intent, callback);
    }

    /**
     * 生成调用360SDK登录接口的Intent
     * @param isLandScape 是否横屏
     * @return intent
     */
    private Intent getLoginIntent(boolean isLandScape) {

        Intent intent = new Intent(this, ContainerActivity.class);

        // 界面相关参数，360SDK界面是否以横屏显示。
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的登录模块。
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGIN);

        // 可选参数，是否在自动登录的过程中显示切换账号按钮
        intent.putExtra(ProtocolKeys.IS_SHOW_AUTOLOGIN_SWITCH, getCheckBoxBoolean(R.id.isShowSwitchButton));

        //-- 以下参数仅仅针对自动登录过程的控制
        // 可选参数，自动登录过程中是否不展示任何UI，默认展示。
        intent.putExtra(ProtocolKeys.IS_AUTOLOGIN_NOUI, getCheckBoxBoolean(R.id.isAutoLoginHideUI));
        intent.putExtra(ProtocolKeys.IS_LOGIN_SHOW_CLOSE_ICON,true);
        intent.putExtra(ProtocolKeys.UI_BACKGROUND_PICTRUE, "");
        intent.putExtra(ProtocolKeys.UI_BACKGROUND_PICTURE_IN_ASSERTS, "fn_transparent_bg.png");
        return intent;
    }



    /**
     * 使用360SDK的切换账号接口
     *
     * @param isLandScape 是否横屏显示登录界面
     */
    protected void doSdkSwitchAccount(boolean isLandScape) {
        if(isInitSucc==false){
            Log.d(SDKTag,SDKTag+" init fail!");
            return;
        }
        Intent intent = getSwitchAccountIntent(isLandScape);
        IDispatcherCallback callback = mAccountSwitchCallback;
        Matrix.invokeActivity(this, intent, callback);
    }

    /**
     * 使用360SDK的退出接口
     *
     * @param isLandScape 是否横屏显示支付界面
     */
    protected void doSdkQuit(boolean isLandScape) {
        if(isInitSucc==false){
            Log.d(SDKTag,SDKTag+" init fail!");
            return;
        }
        if(isExitSDK==false){
            return;
        }
        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的退出模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_QUIT);

        // 可选参数，登录界面的背景图片路径，必须是本地图片路径
        bundle.putString(ProtocolKeys.UI_BACKGROUND_PICTRUE, "");

        Intent intent = new Intent(this, ContainerActivity.class);
        intent.putExtras(bundle);

        Matrix.invokeActivity(this, intent, mQuitCallback);
    }


    public void doUserInfo(String type,
                           String zonename,
                           String roleName,
                           String roleId,
                           String roleLevel,
                           String professionid,
                           String profession,
                           String gender,
                           String power,//战力值
                           String vip,
                           String diamond//充值现金货币，即用现金直接购买的货币
                            )
    {
        Log.d("doUserInfo", SDKTag+" type:"+type+" zonename:"+zonename+" roleName:"+roleName+" roleId:"+roleId
                +" roleLevel:"+roleLevel+" professionid:"+professionid+" profession:"+profession+" gender:"+gender
                +" power："+power+" vip:"+vip+" diamond:"+diamond);
        if(null==zonename||"".equals(zonename)){
            zonename="无";
        }
        if(null==roleName||"".equals(roleName)){
            roleName="无";
        }
        if(null==roleId||"".equals(roleId)){
            roleId="0";
        }
        if(null==roleLevel||"".equals(roleLevel)){
            roleLevel="0";
        }
        if(null==professionid||"".equals(professionid)){
            professionid="0";
        }

        if(null==profession||"".equals(profession)){
            profession="无";
        }

        if(null==gender||"".equals(gender)){
            gender="无";
        }
        if(null==power||"".equals(power)){
            power="0";
        }
        if(null==vip||"".equals(vip)){
            vip="0";
        }
        if(null==diamond||"".equals(diamond)){
            diamond="0";
        }


        HashMap eventParams = new HashMap();
        eventParams.put("type",type);
        eventParams.put("zoneid",0);
        eventParams.put("zonename",zonename);
        eventParams.put("roleid",Integer.parseInt(roleId));
        eventParams.put("rolename",roleName);
        eventParams.put("rolelevel",Integer.parseInt(roleLevel));
        eventParams.put("professionid",professionid);
        eventParams.put("profession",profession);
        eventParams.put("gender",gender);

        eventParams.put("power",power);
        eventParams.put("vip",Integer.parseInt(vip));
        //    	//帐号余额
        JSONArray balancelist = new JSONArray();
        JSONObject balance1 = new JSONObject();
        try {
            balance1.put("balanceid",1);
            balance1.put("balancename","钻石");
            balance1.put("balancenum",Integer.parseInt(diamond));
            balancelist.put(balance1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventParams.put("balance",balancelist.toString());
        eventParams.put("partyid",0);
        eventParams.put("partyname","无");
        eventParams.put("partyroleid",0);
        eventParams.put("partyrolename","无");
        eventParams.put("friendlist","无");
        doSdkGetUserInfoByCP(eventParams);
    }

    private boolean getCheckBoxBoolean(int id) {
        CheckBox cb = (CheckBox)findViewById(id);
        if (cb != null) {
            return cb.isChecked();
        }
        return false;
    }
    private boolean isCancelLogin(String data) {
        try {
            JSONObject joData = new JSONObject(data);
            int errno = joData.optInt("errno", -1);
            if (-1 == errno) {
                //Toast.makeText(SdkUserBaseActivity.this, data, Toast.LENGTH_LONG).show();
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    private String parseAccessTokenFromLoginResult(String loginRes) {
        try {
            JSONObject joRes = new JSONObject(loginRes);
            JSONObject joData = joRes.getJSONObject("data");
            return joData.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseLogoutResult(String logoutRes) {
        try {
            JSONObject joRes = new JSONObject(logoutRes);
            return joRes.getString("errno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 生成调用360SDK切换账号接口的Intent
     *
     * @param isLandScape 是否横屏
     * @return Intent
     */
    private Intent getSwitchAccountIntent(boolean isLandScape) {

        Intent intent = new Intent(this, ContainerActivity.class);
        // 可选参数，是否在自动登录的过程中显示切换账号按钮
        intent.putExtra(ProtocolKeys.IS_SHOW_AUTOLOGIN_SWITCH, getCheckBoxBoolean(R.id.isShowSwitchButton));

        // 必须参数，360SDK界面是否以横屏显示。
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的切换账号模块。
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT);

        return intent;
    }

    private Intent getLogoutIntent(){

        /*
         * 必须参数：
         *  function_code : 必须参数，表示调用SDK接口执行的功能
         */
        Intent intent = new Intent();
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGOUT);
        return intent;
    }

    /**
     * 角色信息采集接口
     */
    protected void doSdkGetUserInfoByCP(HashMap info) {

//    	//----------------------------模拟数据------------------------------
//    	//帐号余额
//        JSONArray balancelist = new JSONArray();
//        JSONObject balance1 = new JSONObject();
//        JSONObject balance2 = new JSONObject();
//
//        //好友关系
//        JSONArray friendlist = new JSONArray();
//        JSONObject friend1 = new JSONObject();
//        JSONObject friend2 = new JSONObject();
//
//        //排行榜列表
//        JSONArray ranklist = new JSONArray();
//        JSONObject rank1 = new JSONObject();
//        JSONObject rank2 = new JSONObject();
//
//        try {
//            balance1.put("balanceid","1");
//            balance1.put("balancename","bname1");
//            balance1.put("balancenum","200");
//            balance2.put("balanceid","2");
//            balance2.put("balancename","bname2");
//            balance2.put("balancenum","300");
//            balancelist.put(balance1).put(balance2);
//
//            friend1.put("roleid","1");
//            friend1.put("intimacy","0");
//            friend1.put("nexusid","300");
//            friend1.put("nexusname","情侣");
//            friend2.put("roleid","2");
//            friend2.put("intimacy","0");
//            friend2.put("nexusid","600");
//            friend2.put("nexusname","情侣");
//            friendlist.put(friend1).put(friend2);
//
//            rank1.put("listid","1");
//            rank1.put("listname","listname1");
//            rank1.put("num","num1");
//            rank1.put("coin","coin1");
//            rank1.put("cost","cost1");
//            rank2.put("listid","2");
//            rank2.put("listname","listname2");
//            rank2.put("num","num2");
//            rank2.put("coin","coin2");
//            rank2.put("cost","cost2");
//            ranklist.put(rank1).put(rank2);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        HashMap<String, String> eventParams=new HashMap<String, String>();
//
//        eventParams.put("type","enterServer");  //（必填）角色状态（enterServer（登录），levelUp（升级），createRole（创建角色），exitServer（退出））
//        eventParams.put("zoneid","2");  //（必填）游戏区服ID
//        eventParams.put("zonename","测试服");  //（必填）游戏区服名称
//        eventParams.put("roleid","123456");  //（必填）玩家角色ID
//        eventParams.put("rolename","冷雨夜风");  //（必填）玩家角色名
//        eventParams.put("professionid","1");  //（必填）职业ID
//        eventParams.put("profession","战士");  //（必填）职业名称
//        eventParams.put("gender","男");  //（必填）性别
//        eventParams.put("professionroleid","0");  //（选填）职业称号ID
//        eventParams.put("professionrolename","无");  //（选填）职业称号
//        eventParams.put("rolelevel","30");  //（必填）玩家角色等级
//        eventParams.put("power","120000");  //（必填）战力数值
//        eventParams.put("vip","5");  //（必填）当前用户VIP等级
//        eventParams.put("balance",balancelist.toString());  //（必填）帐号余额
//        eventParams.put("partyid","100");  //（必填）所属帮派帮派ID
//        eventParams.put("partyname","王者依旧");  //（必填）所属帮派名称
//        eventParams.put("partyroleid","1");  //（必填）帮派称号ID
//        eventParams.put("partyrolename","会长");  //（必填）帮派称号名称
//        eventParams.put("friendlist",friendlist.toString());  //（必填）好友关系
//        eventParams.put("ranking",ranklist.toString());  //（选填）排行榜列表
//        //参数eventParams相关的 key、value键值对 相关具体使用说明，请参考文档。
//        //----------------------------模拟数据------------------------------
//    	//Matrix.statEventInfo(getApplicationContext(), eventParams);
        Matrix.statEventInfo(getApplicationContext(), info);
    }

    //---------------------------------回调接口------------------------------------
    protected CPCallBackMgr.MatrixCallBack mSDKCallback = new CPCallBackMgr.MatrixCallBack() {
        @Override
        public void execute(Context context, int functionCode, String functionParams) {
            if (functionCode == ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT) {
                Log.d(SDKTag,SDKTag+" functionCode:FUNC_CODE_SWITCH_ACCOUNT");
                doSdkSwitchAccount(true);
            }else if (functionCode == ProtocolConfigs.FUNC_CODE_INITSUCCESS) {
                //这里返回成功之后才能调用SDK 其它接口
                isInitSucc=true;
                Log.d(SDKTag,SDKTag+" functionCode:FUNC_CODE_INITSUCCESS");
            }
        }

    };

    // 登录、注册的回调
    private IDispatcherCallback mLoginCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            // press back
            if (isCancelLogin(data)) {
                isExitSDK=false;
                UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "LoginCallback", "-1");
                return;
            }
            // 显示一下登录结果
            // 解析access_token
            String mAccessToken = parseAccessTokenFromLoginResult(data);
            if (!TextUtils.isEmpty(mAccessToken)) {
                UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "LoginCallback", mAccessToken);
            }
        }
    };
    //注销登录回调
    private IDispatcherCallback mLogoutCallBack = new IDispatcherCallback() {
        @Override
        public void onFinished(String data) {
            String ErrorCode=parseLogoutResult(data);
            if("0".equals(ErrorCode)){
                UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "onLogoutCallBack", "0");
            }else {
                //返回键取消退出
                isExitSDK=false;
                UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "onLogoutCallBack", data);
            }

    }
};

    // 切换账号的回调
    private IDispatcherCallback mAccountSwitchCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            // press back
            if (isCancelLogin(data)) {
                isExitSDK=false;
                UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "AccountSwitchCallback", "-1");
                return;
            }
            // 解析access_token
            String mAccessToken = parseAccessTokenFromLoginResult(data);

            if (!TextUtils.isEmpty(mAccessToken)) {
                UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "AccountSwitchCallback", mAccessToken);
            }
        }
    };

    // 退出的回调
    private IDispatcherCallback mQuitCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            isExitSDK=true;
            JSONObject json;
            try {
                json = new JSONObject(data);
                int which = json.optInt("which", -1);
                String label = json.optString("label");
                switch (which) {
                    case 0: // 用户关闭退出界面
                        UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "onExitCanceled", "-1");
                        return;
                    default:// 退出游戏
                        UnityPlayer.UnitySendMessage(MainActivity.this.gameObject, "onExitSucc", "0");
                        finish();
                        return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

}


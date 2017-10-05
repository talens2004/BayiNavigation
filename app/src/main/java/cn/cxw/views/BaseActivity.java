/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.cxw.views;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;

import cn.cxw.armymap.MainApplication;
import cn.cxw.armymap.R;


public class BaseActivity extends FragmentActivity implements BackHandledInterface {
	public String TAG = "";
	public static MainApplication mApp;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = MainApplication.INSTANCE;
        mApp.registerEvent(this);
        TAG = this.getClass().getSimpleName();

        //去掉窗口标题栏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    
    @Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
    	mApp.unregisterEvent(this);
		super.onDestroy();
	}

    
    
	/**
     * 返回，在XML中定义
     * @param view
     */
    public void back(View view) {
        finish();
    }

    /**
     * 设置返回按钮点击事件
     */
    public void setBackButtonOnClick(){
    	View backButton = (View)findViewById(R.id.btn_back);
    	if(backButton != null){
	    	backButton.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	finish();
	            }
	        });
    	}
    }
    
    private BackHandledFragment mBackHandedFragment;

	@Override
	public void setSelectedFragment(BackHandledFragment selectedFragment) {
		this.mBackHandedFragment = selectedFragment; 
	}
	  
    public boolean onBackPress() {  
        if(mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()){ 
            if(getSupportFragmentManager().getBackStackEntryCount() == 0 && !getClass().getSimpleName().equals("MainActivity")){
        		System.out.println("当前Activity->" + getClass().getSimpleName()); 
        		finish();
    			return false;
            }
            
            if(getSupportFragmentManager().getBackStackEntryCount() == 1 ){  
                super.onBackPressed();  
        		System.out.println(">>>已经返回到首页了");
        		return false;
            }else{  
                getSupportFragmentManager().popBackStack();  
        		System.out.println(">>>退出Fragment界面");
        		return true;
            }  
        }
		return false;  
    }  
	
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
        		System.out.println(">>>点击了返回键");
        		return onBackPress();//这是自定义的代码                
            }
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}

package com.inmobiles.demosdk.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inmobiles.demosdk.LinphoneImpl;
import com.inmobiles.demosdk.R;
import com.inmobiles.demosdk.callback.PhoneCallback;
import com.inmobiles.demosdk.linphone_managment.LinphoneManager;
import com.inmobiles.demosdk.linphone_managment.LinphoneUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.linphone.core.Call;
import org.linphone.core.Call.State;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.mediastream.Log;

public class CallIncomingActivity extends Activity implements PhoneCallback {
	private static CallIncomingActivity instance;

	private TextView name, number;
	private ImageView contactPicture, accept, decline, arrow,hang_up,micro, speaker;
	private Call mCall;
	private CoreListenerStub mListener;
	private LinearLayout acceptUnlock;
	private LinearLayout declineUnlock;
	private boolean alreadyAcceptedOrDeniedCall, begin;
	private float answerX, oldMove;
	private float declineX;
	private LinearLayout menu,callHangup;
	private boolean isMicMuted, isSpeakerEnabled;

	private boolean isCallRunning=false;

	public static CallIncomingActivity instance() {
		return instance;
	}

	public static boolean isInstanciated() {
		return instance != null;
	}

	String usercalling="";
	private AudioManager mAudioManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.call_incoming);

		LinphoneImpl.addPhoneCallBack(this);

		if (getIntent().getExtras()!=null){
			usercalling = getIntent().getExtras().getString("usercalling");
			isCallRunning = getIntent().getExtras().getBoolean("isCallRunning");
		}


//		LinphoneImpl.addCallback(null, new PhoneCallback() {
//			@Override
//			public void incomingCall(Call linphoneCall) {
//				super.incomingCall(linphoneCall);
//			}
//
//			@Override
//			public void outgoingInit() {
//				super.outgoingInit();
//			}
//
//			@Override
//			public void callConnected() {
//				super.callConnected();
//			}
//
//			@Override
//			public void callEnd() {
//				super.callEnd();
//				finish();
//			}
//
//			@Override
//			public void callReleased() {
//				super.callReleased();
//				finish();
//			}
//
//			@Override
//			public void error() {
//				super.error();
//				finish();
//			}
//		});

		name = (TextView) findViewById(R.id.contact_name);
		number = (TextView) findViewById(R.id.contact_number);
		contactPicture = (ImageView) findViewById(R.id.contact_picture);
		menu = (LinearLayout)findViewById(R.id.menu);
		callHangup = (LinearLayout)findViewById(R.id.call_hangup);
		hang_up = (ImageView)findViewById(R.id.hang_up);
		micro = (ImageView) findViewById(R.id.micro);
		speaker = (ImageView) findViewById(R.id.speaker);
		isMicMuted = false;
		isSpeakerEnabled = false;
		mAudioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));
		// set this flag so this activity will stay in front of the keyguard
		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
		getWindow().addFlags(flags);

		final int screenWidth = getResources().getDisplayMetrics().widthPixels;

		acceptUnlock = (LinearLayout) findViewById(R.id.acceptUnlock);
		declineUnlock = (LinearLayout) findViewById(R.id.declineUnlock);

		accept = (ImageView) findViewById(R.id.accept);
		lookupCurrentCall();

		decline = (ImageView) findViewById(R.id.decline);
		arrow = (ImageView) findViewById(R.id.arrow_hangup);
		accept.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LinphoneUtils.getLc().enableMic(true);
				answer();
				menu.setVisibility(View.GONE);
				callHangup.setVisibility(View.VISIBLE);
				Log.d("testMic", LinphoneImpl.getLC().micEnabled());
//				decline.setVisibility(View.GONE);
//				acceptUnlock.setVisibility(View.VISIBLE);

			}
		});


		hang_up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hangUp();
				finish();
			}
		});

		micro.setOnClickListener(v -> {
			isMicMuted = !isMicMuted;
			if(isMicMuted) {
				micro.setImageResource(R.drawable.micro_selected);
			} else {
				micro.setImageResource(R.drawable.micro_default);
			}
			LinphoneImpl.toggleMicro(isMicMuted);
		});

		speaker.setOnClickListener(v -> {
			isSpeakerEnabled = !isSpeakerEnabled;
			if(isSpeakerEnabled) {
				speaker.setImageResource(R.drawable.speaker_selected);
			} else {
				speaker.setImageResource(R.drawable.speaker_default);
			}
			//TODO: SPEAKER
			mAudioManager.setSpeakerphoneOn(isSpeakerEnabled);
		});

		number.setText(usercalling);

//		accept.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View view, MotionEvent motionEvent) {
//				float curX;
//				switch (motionEvent.getAction()) {
//					case MotionEvent.ACTION_DOWN:
//						acceptUnlock.setVisibility(View.VISIBLE);
//						decline.setVisibility(View.GONE);
//						answerX = motionEvent.getX()+accept.getWidth()/2;
//						begin = true;
//						oldMove = 0;
//						break;
//					case MotionEvent.ACTION_MOVE:
//						curX = motionEvent.getX();
//						view.scrollBy((int) (answerX - curX), view.getScrollY());
//						oldMove -= answerX - curX;
//						answerX = curX;
//						if (oldMove < -25)
//							begin = false;
//						if (curX < arrow.getWidth() && !begin) {
//							answer();
//							return true;
//						}
//						break;
//					case MotionEvent.ACTION_UP:
//						view.scrollTo(0, view.getScrollY());
//						decline.setVisibility(View.VISIBLE);
//						acceptUnlock.setVisibility(View.GONE);
//						break;
//				}
//				return true;
//			}
//		});
//
//		decline.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View view, MotionEvent motionEvent) {
//				float curX;
//				switch (motionEvent.getAction()) {
//					case MotionEvent.ACTION_DOWN:
//						declineUnlock.setVisibility(View.VISIBLE);
//						accept.setVisibility(View.GONE);
//						declineX = motionEvent.getX();
//						break;
//					case MotionEvent.ACTION_MOVE:
//						curX = motionEvent.getX();
//						view.scrollBy((int) (declineX - curX), view.getScrollY());
//						declineX = curX;
//						if (curX > (screenWidth-arrow.getWidth()*4)) {
//							decline();
//							return true;
//						}
//						break;
//					case MotionEvent.ACTION_UP:
//						view.scrollTo(0, view.getScrollY());
//						accept.setVisibility(View.VISIBLE);
//						declineUnlock.setVisibility(View.GONE);
//						break;
//				}
//				return true;
//			}
//		});


		decline.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				decline();
//				accept.setVisibility(View.GONE);
//				acceptUnlock.setVisibility(View.VISIBLE);
			}
		});

		if (isCallRunning){
			menu.setVisibility(View.GONE);
			callHangup.setVisibility(View.VISIBLE);
		}
		else {
			menu.setVisibility(View.VISIBLE);
			callHangup.setVisibility(View.GONE);
		}

		instance = this;
	}

	private void hangUp() {
		LinphoneImpl.hangUp();
	}

	@Override
	protected void onResume() {
		super.onResume();
		instance = this;
		Core lc = LinphoneManager.getLc();
		if (lc != null) {
			lc.addListener(mListener);
		}

		alreadyAcceptedOrDeniedCall = false;
		mCall = null;

		// Only one call ringing at a time is allowed
		lookupCurrentCall();
//		if (mCall == null) {
//			//The incoming call no longer exists.
//			Log.d("Couldn't find incoming call");
//			finish();
//			return;
//		}

		//TODO:FIND NAME
//		LinphoneContact contact = ContactsManager.getInstance().findContactFromAddress(address);
//		if (contact != null) {
//			LinphoneUtils.setImagePictureFromUri(this, contactPicture, contact.getPhotoUri(), contact.getThumbnailUri());
//			name.setText(contact.getFullName());
//		} else {
//			name.setText(LinphoneUtils.getAddressDisplayName(address));
//		}
//		number.setText(address.asStringUriOnly());
	}

	@Override
	protected void onStart() {
		super.onStart();
		checkAndRequestCallPermissions();
	}

	@Override
	protected void onPause() {
		Core lc = LinphoneManager.getLc();
		if (lc != null) {
			lc.removeListener(mListener);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//TODO: Terminate call
//		if (LinphoneManager.isInstanciated() && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
//			LinphoneManager.getLc().terminateCall(mCall);
//			finish();
//		}
		return super.onKeyDown(keyCode, event);
	}

	private void lookupCurrentCall() {
	Call currentCall = 	LinphoneManager.getLc().getCurrentCall();
		if (State.IncomingReceived == currentCall.getState()) {
			mCall = currentCall;
		}
//		if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
//			List<Call> calls = LinphoneUtils.getCalls(LinphoneManager.getLc());
//			for (Call call : calls) {
//				if (State.IncomingReceived == call.getState()) {
//					mCall = call;
//					break;
//				}
//			}
//		}
	}

	private void decline() {
		if (alreadyAcceptedOrDeniedCall) {
			return;
		}
		alreadyAcceptedOrDeniedCall = true;

//		LinphoneManager.getLc().terminateCall(mCall);

		LinphoneImpl.declineCall();

		finish();
	}

	private void answer() {
		if (alreadyAcceptedOrDeniedCall) {
			return;
		}
		alreadyAcceptedOrDeniedCall = true;

		LinphoneImpl.acceptCall();

//		CallParams params = LinphoneManager.getLc().createCallParams(mCall);
//
//		boolean isLowBandwidthConnection = !LinphoneUtils.isHighBandwidthConnection(LinphoneService.instance().getApplicationContext());
//
//		if (params != null) {
//			params.enableLowBandwidth(isLowBandwidthConnection);
//		}else {
//			Log.e("Could not create call params for call");
//		}
//
//		if (params == null || !LinphoneManager.getInstance().acceptCallWithParams(mCall, params)) {
//			// the above method takes care of Samsung Galaxy S
//			Toast.makeText(this, R.string.couldnt_accept_call, Toast.LENGTH_LONG).show();
//		} else {
//			if (!LinphoneActivity.isInstanciated()) {
//				return;
//			}
//			LinphoneManager.getInstance().routeAudioToReceiver();
//			LinphoneManager.getInstance().startIncallActivity(mCall);
//		}
	}

	private void checkAndRequestCallPermissions() {
		Dexter.withContext(this)
				.withPermission(Manifest.permission.RECORD_AUDIO)
				.withListener(new PermissionListener() {
					@Override public void onPermissionGranted(PermissionGrantedResponse response)
					{}
					@Override public void onPermissionDenied(PermissionDeniedResponse response)
					{decline();}
					@Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
					{ token.continuePermissionRequest();}
				}).check();
	}

	@Override
	public void incomingCall(Call linphoneCall) {

	}

	@Override
	public void outgoingInit() {

	}

	@Override
	public void callConnected() {

	}

	@Override
	public void callEnd() {
		finish();
	}

	@Override
	public void callReleased() {
		finish();
	}

	@Override
	public void error() {
		finish();
	}

//	@Override
//	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//		for (int i = 0; i < permissions.length; i++) {
//			Log.i("[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
//		}
//	}
}
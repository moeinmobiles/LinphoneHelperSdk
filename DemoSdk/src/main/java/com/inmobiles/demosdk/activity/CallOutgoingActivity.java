package com.inmobiles.demosdk.activity;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.inmobiles.demosdk.LinphoneImpl;
import com.inmobiles.demosdk.R;
import com.inmobiles.demosdk.callback.PhoneCallback;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.linphone.core.Call;
import org.linphone.core.CoreListenerStub;
import org.linphone.mediastream.Log;

public class CallOutgoingActivity extends AppCompatActivity implements OnClickListener, PhoneCallback {
	private static CallOutgoingActivity instance;

	private TextView name, number;
	private ImageView contactPicture, micro, speaker, hangUp;
	private Call mCall;
	private CoreListenerStub mListener;
	private boolean isMicMuted, isSpeakerEnabled;

	private static final String TAG = "OUTGOINGACTIVITY";

	public static CallOutgoingActivity instance() {
		return instance;
	}

	public static boolean isInstanciated() {
		return instance != null;
	}

	String usercalling = "";

	private AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.call_outgoing);

		Log.d(TAG,"oncreate");

		LinphoneImpl.addPhoneCallBack(this);

		if (getIntent().getExtras()!=null)
			usercalling = getIntent().getExtras().getString("usercalling");

		name = (TextView) findViewById(R.id.contact_name);
		number = (TextView) findViewById(R.id.contact_number);
		contactPicture = (ImageView) findViewById(R.id.contact_picture);

		isMicMuted = false;
		isSpeakerEnabled = false;

		mAudioManager = ((AudioManager)getSystemService(Context.AUDIO_SERVICE));

		micro = (ImageView) findViewById(R.id.micro);
		micro.setOnClickListener(this);
		speaker = (ImageView) findViewById(R.id.speaker);
		speaker.setOnClickListener(this);

		// set this flag so this activity will stay in front of the keyguard
//		int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
//		getWindow().addFlags(flags);

		hangUp = (ImageView) findViewById(R.id.outgoing_hang_up);
		hangUp.setOnClickListener(this);

		number.setText(usercalling);

//		LinphoneImpl.addCallback(null, new PhoneCallback() {
//			@Override
//			public void incomingCall(Call linphoneCall) {
//				super.incomingCall(linphoneCall);
//				Log.d(TAG,"incomingCall");
//			}
//
//			@Override
//			public void outgoingInit() {
//				super.outgoingInit();
//				Log.d(TAG,"incomingCall");
//			}
//
//			@Override
//			public void callConnected() {
//				super.callConnected();
//				Log.d(TAG,"callConnected");
//			}
//
//			@Override
//			public void callEnd() {
//				super.callEnd();
//				Log.d(TAG,"callEnd");
//				finish();
//			}
//
//			@Override
//			public void callReleased() {
//				super.callReleased();
//				Log.d(TAG,"callReleased");
//				finish();
//			}
//
//			@Override
//			public void error() {
//				super.error();
//				Log.d(TAG,"error");
//				finish();
//			}
//		});

		//TODO:REPLACE
//		mListener = new CoreListenerStub(){
//			@Override
//			public void onCallStateChanged(Core lc, Call call, State state, String message) {
//				if (call == mCall && State.Connected == state) {
//					/*if (!LinphoneActivity.isInstanciated()) {
//						return;
//					}*/
//					LinphoneManager.getInstance().startIncallActivity(mCall);
//					finish();
//					return;
//				} else if (state == State.Error) {
//					// Convert Core message for internalization
//					if (call.getErrorInfo().getReason() == Reason.Declined) {
//						displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
//						decline();
//					} else if (call.getErrorInfo().getReason() == Reason.NotFound) {
//						displayCustomToast(getString(R.string.error_user_not_found), Toast.LENGTH_SHORT);
//						decline();
//					} else if (call.getErrorInfo().getReason() == Reason.NotAcceptable) {
//						displayCustomToast(getString(R.string.error_incompatible_media), Toast.LENGTH_SHORT);
//						decline();
//					} else if (call.getErrorInfo().getReason() == Reason.Busy) {
//						displayCustomToast(getString(R.string.error_user_busy), Toast.LENGTH_SHORT);
//						decline();
//					} else if (message != null) {
//						displayCustomToast(getString(R.string.error_unknown) + " - " + message, Toast.LENGTH_SHORT);
//						decline();
//					}
//				}else if (state == State.End) {
//					// Convert Core message for internalization
//					if (call.getErrorInfo().getReason() == Reason.Declined) {
//						displayCustomToast(getString(R.string.error_call_declined), Toast.LENGTH_SHORT);
//						decline();
//					}
//				}
//
//				if (LinphoneManager.getLc().getCallsNb() == 0) {
//					finish();
//					return;
//				}
//			}
//		};
		instance = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume");
		instance = this;
		mCall = null;

		// Only one call ringing at a time is allowed
//		if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
//			List<Call> calls = LinphoneUtils.getCalls(LinphoneManager.getLc());
//			for (Call call : calls) {
//				State cstate = call.getState();
//				if (State.OutgoingInit == cstate || State.OutgoingProgress == cstate
//						|| State.OutgoingRinging == cstate || State.OutgoingEarlyMedia == cstate) {
//					mCall = call;
//					break;
//				}
//				if (State.StreamsRunning == cstate) {
//					/*if (!LinphoneActivity.isInstanciated()) {
//						return;
//					}*/
//					LinphoneManager.getInstance().startIncallActivity(mCall);
//					finish();
//					return;
//				}
//			}
//		}
//		if (mCall == null) {
//			Log.e("Couldn't find outgoing call");
////			finish();
//			return;
//		}


		//TODO: FIND BEST SOLUTION
//		Address address = mCall.getRemoteAddress();
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
		Log.d(TAG,"onStart");
	}

	@Override
	protected void onPause() {
		Log.d(TAG,"onPause");
//		Core lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
//		if (lc != null) {
//			lc.removeListener(mListener);
//		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
		Log.d(TAG,"onDestroy");
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.micro) {
			isMicMuted = !isMicMuted;
			if(isMicMuted) {
				micro.setImageResource(R.drawable.micro_selected);
			} else {
				micro.setImageResource(R.drawable.micro_default);
			}
			LinphoneImpl.toggleMicro(isMicMuted);
//			LinphoneManager.getLc().enableMic(!isMicMuted);
		}
		if (id == R.id.speaker) {
			isSpeakerEnabled = !isSpeakerEnabled;
			if(isSpeakerEnabled) {
				speaker.setImageResource(R.drawable.speaker_selected);
			} else {
				speaker.setImageResource(R.drawable.speaker_default);
			}
			//TODO: SPEAKER
			mAudioManager.setSpeakerphoneOn(isSpeakerEnabled);
//			LinphoneManager.getInstance().enableSpeaker(isSpeakerEnabled);
		}
		if (id == R.id.outgoing_hang_up) {
			decline();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
//			LinphoneCallHelper.hangUp();
//			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void decline() {
		LinphoneImpl.hangUp();
		finish();
	}



	private void checkAndRequestCallPermissions() {
		Dexter.withContext(this)
				.withPermission(Manifest.permission.RECORD_AUDIO)
				.withListener(new PermissionListener() {
					@Override public void onPermissionGranted(PermissionGrantedResponse response)
					{}
					@Override public void onPermissionDenied(PermissionDeniedResponse response)
					{decline();
					finish();
					}
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
}

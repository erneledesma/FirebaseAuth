package com.borjabravo.firebaseauth;
/**
 * Created by borjabravo on 23/5/16.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

/**
 * Demonstrate Firebase Authentication using a Twitter access token.
 */
public class TwitterActivity extends BaseActivity {

    private static final String TAG = "TwitterLogin";

    @BindView(R.id.status)
    TextView mStatusTextView;
    @BindView(R.id.detail)
    TextView mDetailTextView;
    @BindView(R.id.button_twitter)
    TwitterLoginButton buttonTwitter;
    @BindView(R.id.button_twitter_signout)
    Button buttonTwitterSignout;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_twitter);
        unbinder = ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };
        // Initialize Twitter login button
        buttonTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                handleTwitterAccessToken(result);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                Log.i("", "Error: " + exception);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        buttonTwitter.onActivityResult(requestCode, resultCode, data);
    }

    private void handleTwitterAccessToken(Result<TwitterSession> result) {
        Log.d(TAG, "handleTwitterAccessToken:" + result.data.getAuthToken().token);
        showProgressDialog();
        AuthCredential credential =
                TwitterAuthProvider.getCredential(result.data.getAuthToken().token, result.data.getAuthToken().secret);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    Toast.makeText(TwitterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.twitter_status_fmt, user.getDisplayName()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            buttonTwitter.setVisibility(View.GONE);
            buttonTwitterSignout.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);
            buttonTwitter.setVisibility(View.VISIBLE);
            buttonTwitterSignout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.button_twitter_signout)
    public void onClickButtonTwitterSignout() {
        signOut();
    }
}
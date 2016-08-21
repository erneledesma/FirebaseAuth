/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borjabravo.firebaseauth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity to demonstrate anonymous login and account linking (with an email/password account).
 */
public class AnonymousAuthActivity extends BaseActivity {

    private static final String TAG = "AnonymousAuth";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @BindView(R.id.field_email)
    EditText mEmailField;
    @BindView(R.id.field_password)
    EditText mPasswordField;
    @BindView(R.id.button_anonymous_sign_in)
    Button buttonAnonymousSignIn;
    @BindView(R.id.button_anonymous_sign_out)
    Button buttonAnonymousSignOut;
    @BindView(R.id.button_link_account)
    Button buttonLinkAccount;
    @BindView(R.id.anonymous_status_id)
    TextView idView;
    @BindView(R.id.anonymous_status_email)
    TextView emailView;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_auth);
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

    private void signInAnonymously() {
        showProgressDialog();
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously", task.getException());
                    Toast.makeText(AnonymousAuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void linkAccount() {
        if (!validateLinkForm()) {
            return;
        }
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        // Create EmailAuthCredential with email and password
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        showProgressDialog();
        // Link the anonymous user to the email credential
        mAuth.getCurrentUser()
                .linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(AnonymousAuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private boolean validateLinkForm() {
        boolean valid = true;
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }
        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        boolean isSignedIn = (user != null);
        if (isSignedIn) {
            idView.setText(getString(R.string.id_fmt, user.getUid()));
            emailView.setText(getString(R.string.email_fmt, user.getEmail()));
        } else {
            idView.setText(R.string.signed_out);
            emailView.setText(null);
        }
        buttonAnonymousSignIn.setEnabled(!isSignedIn);
        buttonAnonymousSignOut.setEnabled(isSignedIn);
        buttonLinkAccount.setEnabled(isSignedIn);
    }

    @OnClick(R.id.button_anonymous_sign_in)
    public void onClickAnonymousSignIn() {
        signInAnonymously();
    }

    @OnClick(R.id.button_anonymous_sign_out)
    public void onClickAnonymousSignOut() {
        signOut();
    }

    @OnClick(R.id.button_link_account)
    public void onClickLinkAccount() {
        linkAccount();
    }
}

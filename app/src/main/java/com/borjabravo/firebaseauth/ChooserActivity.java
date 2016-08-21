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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

/**
 * Simple list-based Activity to redirect to one of the other Activities. This Activity does not
 * contain any useful code related to Firebase Authentication. You may want to start with
 * one of the following Files:
 * {@link GoogleSignInActivity}
 * {@link FacebookLoginActivity}
 * {@link EmailPasswordActivity}
 * {@link TwitterActivity}
 */
public class ChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "";
    private static final String TWITTER_SECRET = "";

    private Unbinder unbinder;

    @BindView(R.id.list_view)
    ListView listView;

    private static final Class[] CLASSES = new Class[] {
            GoogleSignInActivity.class, FacebookLoginActivity.class, EmailPasswordActivity.class,
            AnonymousAuthActivity.class, TwitterActivity.class,
    };

    private static final int[] DESCRIPTION_IDS = new int[] {
            R.string.desc_google_sign_in, R.string.desc_facebook_login, R.string.desc_emailpassword,
            R.string.desc_anonymous_auth, R.string.desc_twitter
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_chooser);
        unbinder = ButterKnife.bind(this);
        MyArrayAdapter adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES);
        adapter.setDescriptionIds(DESCRIPTION_IDS);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class clicked = CLASSES[position];
        startActivity(new Intent(this, clicked));
    }

    public static class MyArrayAdapter extends ArrayAdapter<Class> {

        private Context mContext;
        private Class[] mClasses;
        private int[] mDescriptionIds;

        public MyArrayAdapter(Context context, int resource, Class[] objects) {
            super(context, resource, objects);
            mContext = context;
            mClasses = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }
            ((TextView) view.findViewById(android.R.id.text1)).setText(mClasses[position].getSimpleName());
            ((TextView) view.findViewById(android.R.id.text2)).setText(mDescriptionIds[position]);
            return view;
        }

        public void setDescriptionIds(int[] descriptionIds) {
            mDescriptionIds = descriptionIds;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}

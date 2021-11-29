package com.mmaoo.spimag;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mmaoo.spimag.ui.ItemView.ItemViewFragment;
import com.mmaoo.spimag.ui.areaAdd.AreaAddFragment;
import com.mmaoo.spimag.ui.areaList.AreaListFragment;
import com.mmaoo.spimag.ui.areaShow.AreaShowFragment;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Navigable {

    BottomNavigationView navView;
    ViewPager2 viewPager;
    MainFragmentStateAdapter mainFragmentStateAdapter;

    int currentTabPosition = 0;

    ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) onSignInResult(RESULT_OK);
        else {
            setContentView(new LinearLayout(this));

            signInLauncher = registerForActivityResult(
                    new FirebaseAuthUIActivityResultContract(),
                    new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                        @Override
                        public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                            IdpResponse response = result.getIdpResponse();
                            onSignInResult(result.getResultCode());
                        }
                    }
            );

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.

            launchSignInActivity();
        }
    }

    public void launchSignInActivity(){

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
                ,new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(Integer result){
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);

        if(result == RESULT_OK){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Toast toast = Toast.makeText(this,"Zalogowano",Toast.LENGTH_SHORT);
            toast.show();

//            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                    R.id.navigation_item_list, R.id.navigation_area_show, R.id.navigation_area_list)
//                    .build();
//            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//            NavigationUI.setupWithNavController(navView, navController);
            viewPager = findViewById(R.id.viewPager);
            mainFragmentStateAdapter = new MainFragmentStateAdapter(getSupportFragmentManager(),getLifecycle());
            viewPager.setAdapter(mainFragmentStateAdapter);
            viewPager.setOffscreenPageLimit(2);
            navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Log.d(this.getClass().toString(),"onNavigationItemSelected, item="+item.getItemId());
                    switch (item.getItemId()){
                        case R.id.navigation_item_list:
                            currentTabPosition = MainFragmentStateAdapter.ITEMS_POSITION;
                            viewPager.setCurrentItem(currentTabPosition);
                            return true;
                        case R.id.navigation_area_list:
                            currentTabPosition = MainFragmentStateAdapter.AREAS_POSITION;
                            viewPager.setCurrentItem(currentTabPosition);
                            return true;
                    }
                    return false;
                }
            });
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    Log.d(this.getClass().toString(),"onPageChangeCallback, position="+position+", currentPosition="+currentTabPosition);
                    navView.getMenu().getItem(position).setChecked(true);
//                    if(position != currentTabPosition) {
//                        currentTabPosition = position;
//                        navView.setSelectedItemId(position);
//                    }
                    currentTabPosition = position;
                    if(mainFragmentStateAdapter.createFragment(currentTabPosition) instanceof AreaShowFragment ) viewPager.setUserInputEnabled(false);
                    else viewPager.setUserInputEnabled(true);
                    super.onPageSelected(position);
                }
            });

        }else {
            Toast toast = Toast.makeText(this,"Błąd logowania",Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.change_password: break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent restartActivity = new Intent(this,MainActivity.class);
                restartActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(restartActivity);
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
//        Log.w(this.getClass().toString(),"onBackPressed");

        boolean handled = false;
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        if(navHostFragment != null) {
            List<Fragment> fragmentList = navHostFragment.getChildFragmentManager().getFragments();
            for (Fragment f : fragmentList) {
                if (f instanceof Backable) {
                    handled = ((Backable) f).onBackPressed();
                    if (handled) break;
                }
            }
        }
        if(!handled) {
            viewPager.setUserInputEnabled(true);
            if(!mainFragmentStateAdapter.removeFragment(mainFragmentStateAdapter.createFragment(currentTabPosition),currentTabPosition)){
                super.onBackPressed();
            }

        }
    }


    @Override
    public void navigate(int action) {
        viewPager.setUserInputEnabled(true);
//        setProperBottomNavigationItem(action);
//        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action);
        setProperBottomNavigationItem(action);
        switch (action){
            case R.id.action_navigate_to_item_view:
                mainFragmentStateAdapter.updateFragment(new ItemViewFragment(),currentTabPosition);
                break;
            case R.id.action_navigate_to_area_add:
                mainFragmentStateAdapter.updateFragment(new AreaAddFragment(),currentTabPosition);
                break;
            case R.id.action_navigate_to_area_show:
                mainFragmentStateAdapter.updateFragment(new AreaShowFragment(),currentTabPosition);
                break;
            case R.id.action_navigate_to_area_list:
                mainFragmentStateAdapter.updateFragment(new AreaListFragment(),currentTabPosition);
                break;
        }
    }

    @Override
    public void navigate(int action, Bundle bundle) {
        viewPager.setUserInputEnabled(true);
        setProperBottomNavigationItem(action);
        Fragment fragment;
        switch (action){
            case R.id.action_navigate_to_item_view:
                fragment = new ItemViewFragment();
                fragment.setArguments(bundle);
                mainFragmentStateAdapter.updateFragment(fragment,currentTabPosition);
                break;
            case R.id.action_navigate_to_area_add:
                fragment = new AreaAddFragment();
                fragment.setArguments(bundle);
                mainFragmentStateAdapter.updateFragment(fragment,currentTabPosition);
                break;
            case R.id.action_navigate_to_area_show:
                viewPager.setUserInputEnabled(false);
                fragment = new AreaShowFragment();
                fragment.setArguments(bundle);
                mainFragmentStateAdapter.updateFragment(fragment,currentTabPosition);
                break;
            case R.id.action_navigate_to_area_list:
                fragment = new AreaListFragment();
                fragment.setArguments(bundle);
                mainFragmentStateAdapter.updateFragment(fragment,currentTabPosition);
                break;
        }
//        setProperBottomNavigationItem(action);
//        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(action,bundle);
    }

    @Override
    public void navigateUp() {
        onBackPressed();
//        if(!mainFragmentStateAdapter.removeFragment(mainFragmentStateAdapter.createFragment(currentTabPosition),currentTabPosition)){
//            finish();
//        }
//        Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }

    private void setProperBottomNavigationItem(int action){
        switch (action){
            case R.id.action_navigate_to_item_view:
                currentTabPosition = MainFragmentStateAdapter.ITEMS_POSITION;
                viewPager.setCurrentItem(currentTabPosition);
                break;
            case R.id.action_navigate_to_area_add:
            case R.id.action_navigate_to_area_show:
            case R.id.action_navigate_to_area_list:
                currentTabPosition = MainFragmentStateAdapter.AREAS_POSITION;
                viewPager.setCurrentItem(currentTabPosition);
                break;
        }
    }
}
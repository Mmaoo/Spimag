package com.mmaoo.spimag;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mmaoo.spimag.ui.areaList.AreaListFragment;
import com.mmaoo.spimag.ui.itemList.ItemListFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainFragmentStateAdapter extends FragmentStateAdapter {

    private static final List<Fragment> BASE_FRAGMENTS =
            Arrays.asList(new ItemListFragment(), new AreaListFragment());

    public static final int ITEMS_POSITION = 0;
    public static final int AREAS_POSITION = 1;

    private List<Fragment> itemsFragments = new ArrayList<>();
    private List<Fragment> areasFragments = new ArrayList<>();

    public MainFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public MainFragmentStateAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public MainFragmentStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(this.getClass().toString(),"createFragment, position="+position+
                ", itemFragmentsSize="+itemsFragments.size()+", areaFragmentSize="+areasFragments.size());
        if(position == AREAS_POSITION){
            if(areasFragments.isEmpty()) return BASE_FRAGMENTS.get(position);
            else return areasFragments.get(areasFragments.size()-1);
        } else{
            if(itemsFragments.isEmpty()) return BASE_FRAGMENTS.get(position);
            else return itemsFragments.get(itemsFragments.size()-1);
        }
    }

    @Override
    public int getItemCount() {
        Log.d(this.getClass().toString(),"getItemCount");
        return 2;
    }

    @Override
    public long getItemId(int position) {
        Log.d(this.getClass().toString(),"getItemId, position="+position);
        if(position == ITEMS_POSITION &&
        createFragment(position).equals(BASE_FRAGMENTS.get(position)))
            return ITEMS_POSITION;
        else if(position == AREAS_POSITION &&
        createFragment(position).equals(BASE_FRAGMENTS.get(position)))
            return AREAS_POSITION;
        else return createFragment(position).hashCode();
    }

    public void updateFragment(Fragment fragment, int position){
        Log.d(this.getClass().toString(),"updateFragment, fragment="+fragment.toString()+", position="+position);
        if(!BASE_FRAGMENTS.contains(fragment)){
            addInnerFragment(fragment,position);
        }
        notifyDataSetChanged();
    }

    private void addInnerFragment(Fragment fragment, int position){
        Log.d(this.getClass().toString(),"addInnerFragment, fragment="+fragment.toString()+", position="+position);
        if(position == AREAS_POSITION) {
            areasFragments.add(fragment);
        }else{
            itemsFragments.add(fragment);
        }
    }

    public boolean removeFragment(Fragment fragment, int position){
        Log.d(this.getClass().toString(),"removeFragment, fragment="+fragment.toString()+", position="+position);
        if(position == ITEMS_POSITION){
            if(itemsFragments.contains(fragment)){
                removeInnerFragment(fragment,itemsFragments);
                return true;
            }
        }else if (position == AREAS_POSITION){
            if(areasFragments.contains(fragment)){
                removeInnerFragment(fragment,areasFragments);
                return true;
            }
        }
        return false;
    }

    private void removeInnerFragment(Fragment fragment, List<Fragment> tabFragments){
        Log.d(this.getClass().toString(),"removeInnerFragment, fragment="+fragment.toString());
        tabFragments.remove(fragment);
        notifyDataSetChanged();
    }
}

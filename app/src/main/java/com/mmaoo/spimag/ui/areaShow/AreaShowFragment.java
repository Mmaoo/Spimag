package com.mmaoo.spimag.ui.areaShow;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.mmaoo.spimag.Backable;
import com.mmaoo.spimag.Navigable;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.Area;
import com.mmaoo.spimag.model.AreaElement;
import com.mmaoo.spimag.model.Item;
import com.mmaoo.spimag.model.RectAreaElement;

import java.util.Arrays;
import java.util.Random;

public class AreaShowFragment extends Fragment implements Backable {

    public static final int ACTION_SHOW = 0;
    public static final int ACTION_ADD_ITEM = 1;

    private AreaShowViewModel areaShowViewModel;
    private AreaSurfaceView areaSurfaceView;

    private View root;
    private Navigable navigable;

    private int action = -1;

    private Area area;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        areaShowViewModel =
                new ViewModelProvider(this).get(AreaShowViewModel.class);
        root = inflater.inflate(R.layout.fragment_area_show, container, false);
        setHasOptionsMenu(true);

        //final TextView textView = root.findViewById(R.id.text_dashboard);
//        areaShowViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });


        //areaSurfaceView.setElements(rectAreaElements);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();

        if(arguments == null) {
            Log.d(this.getClass().toString(),"Arguments is null");
            navigable.navigateUp();
        }

        action = arguments.getInt("action",Integer.MAX_VALUE);
        try {
            area = (Area) arguments.getSerializable("area");
        }catch (ClassCastException e) {
            Log.w(this.getClass().toString(),e.getMessage());
            Log.w(this.getClass().toString(), Arrays.toString(e.getStackTrace()));
            navigable.navigateUp();
        }

        areaSurfaceView = root.findViewById(R.id.area_surface_view);
        areaSurfaceView.setArea(area);

        areaSurfaceView.edited.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean value) {
                if(value){

                }else{

                }
            }
        });

        switch(action){
            case ACTION_ADD_ITEM:
                try {
                    areaSurfaceView.setAction(action);
                    Item item = (Item) arguments.getSerializable("item");
                    RectAreaElement rectAreaElement = new RectAreaElement();
                    rectAreaElement.areaId = area.getId();
                    int size = areaSurfaceView.getAreaWidth() < areaSurfaceView.getAreaHeight() ? areaSurfaceView.getAreaWidth()/8 : areaSurfaceView.getAreaHeight()/8;
                    rectAreaElement.width = size;
                    rectAreaElement.height = size;
                    rectAreaElement.x = (float) ((areaSurfaceView.getAreaWidth()/2.0) - (size/2.0));
                    rectAreaElement.y = (float) ((areaSurfaceView.getAreaHeight()/2.0) - (size/2.0));
                    Random rnd = new Random();
                    rectAreaElement.color = Color.argb(255,rnd.nextInt(256),rnd.nextInt(256),rnd.nextInt(256));
                    item.setAreaElement(rectAreaElement);
                    areaSurfaceView.setEditedAreaElement(new Pair<Item, AreaElement>(item,rectAreaElement));
                }catch (ClassCastException e) {
                    e.printStackTrace();
                    navigable.navigateUp();
                }
                break;
            case ACTION_SHOW:
                areaSurfaceView.setAction(action);
                break;
            default: navigable.navigateUp();
        }

//        areaSurfaceView.resumePaint();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.area_fragment_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.addElement:
//                RectAreaElement rectAreaElement = new RectAreaElement();
//                int size = areaSurfaceView.getWidth() < areaSurfaceView.getHeight() ? areaSurfaceView.getWidth()/8 : areaSurfaceView.getHeight()/8;
//                rectAreaElement.width = size;
//                rectAreaElement.height = size;
//                Random rnd = new Random();
//                rectAreaElement.color = Color.argb(255,rnd.nextInt(256),rnd.nextInt(256),rnd.nextInt(256));
//                areaSurfaceView.areaElements.add(rectAreaElement);
                break;
        }

        return true;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigable = (Navigable) context;
        }catch (ClassCastException e){
            Log.e(this.getClass().toString(),e.getMessage());
            Log.e(this.getClass().toString(), Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public boolean onBackPressed() {
        if(areaSurfaceView instanceof Backable) return  ((Backable) areaSurfaceView).onBackPressed();
        return true;
    }
}
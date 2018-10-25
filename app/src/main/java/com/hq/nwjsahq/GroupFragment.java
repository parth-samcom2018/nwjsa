package com.hq.nwjsahq;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hq.nwjsahq.models.Event;
import com.hq.nwjsahq.models.Group;
import com.hq.nwjsahq.models.GroupResponse;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Vector;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GroupFragment extends Fragment {

    Group group;
    private GridView gridView;
    private ArrayAdapter<Event> gridAdapter;
    private SwipeRefreshLayout refreshLayout;
    private ImageView emptyIV,tv_group_edit;
    private ProgressDialog pd;

    private List<Group> groups = new Vector<Group>();


    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_group_menu, menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //theres only create
        if(item.getItemId() == R.id.creategrp)this.newGroupAction();
        return super.onOptionsItemSelected(item);
    }

    private void newGroupAction() {

        final AlertDialog.Builder alert = new AlertDialog.Builder(GroupFragment.this.getActivity());

        LinearLayout lila1 = new LinearLayout(GroupFragment.this.getActivity());
        lila1.setOrientation(LinearLayout.VERTICAL);
        final EditText nameET = new EditText(GroupFragment.this.getActivity());
        nameET.setHint("Group Name");
        final EditText descET = new EditText(GroupFragment.this.getActivity());
        descET.setVisibility(View.GONE);
        descET.setHint("Group Description");
        lila1.addView(nameET);
        int pad = (int)getResources().getDimension(R.dimen.small_pad);
        lila1.setPadding(pad,pad,pad,pad);
        alert.setView(lila1);

        alert.setTitle("Create Group");


        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int whichButton) {
                String name = nameET.getText().toString();

                if(name.length() == 0 || name == null)
                {
                    Toast.makeText(GroupFragment.this.getActivity(),"Enter a Group Name",Toast.LENGTH_LONG).show();
                    DM.hideKeyboard(GroupFragment.this.getActivity());
                    return;
                }

                pd = DM.getPD(GroupFragment.this.getActivity(),"Loading Creating Group..");
                pd.show();

                DM.getApi().creategroup(DM.getAuthString(), name, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        Toast.makeText(GroupFragment.this.getActivity(),"Group Created!",Toast.LENGTH_LONG).show();
                        pd.dismiss();
                        dialog.dismiss();
                        DM.hideKeyboard(GroupFragment.this.getActivity());

                        loadData();
                        refreshLayout.setRefreshing(true);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(GroupFragment.this.getActivity(),"Could not create group: "+error.getMessage(),Toast.LENGTH_LONG).show();
                        pd.dismiss();
                        dialog.dismiss();
                        DM.hideKeyboard(GroupFragment.this.getActivity());

                        loadData();
                        refreshLayout.setRefreshing(true);
                    }
                });

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DM.hideKeyboard(GroupFragment.this.getActivity());
                dialog.dismiss();
            }
        });

        alert.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.fragment_groups, container, false);

        isOnline();

        emptyIV = v.findViewById(R.id.empty);
        tv_group_edit = v.findViewById(R.id.tvMessage_edit);

        final ProgressDialog pd = DM.getPD(getActivity(),"Loading Groups...");
        if(this.isVisible())pd.show();

        gridView = v.findViewById(R.id.list);


        gridAdapter= new ArrayAdapter<Event>(GroupFragment.this.getActivity(), R.layout.group_cell){


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    try
                    {
                        convertView = LayoutInflater.from(GroupFragment.this.getActivity()).inflate(R.layout.group_cell, parent, false);
                    }

                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }

                final Group group = groups.get(position);


                final ImageView myImageView = convertView.findViewById(R.id.myImageView);


                final TextView tv = convertView.findViewById(R.id.textView);


                Picasso.Builder builder = new Picasso.Builder(GroupFragment.this.getActivity());
                builder.listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        Log.d("hq", "uri: " + uri.getPath());
                        exception.printStackTrace();
                    }
                });
                try {
                    Picasso p = builder.build();
                    p.load(group.groupImage).placeholder(R.drawable.group_first).into(myImageView);//.networkPolicy(NetworkPolicy.NO_CACHE).
                    tv.setText(group.groupName);

                }
                catch (NullPointerException n){
                    n.printStackTrace();
                }



                return convertView;
            }

            @Override
            public int getCount() {
                return groups.size();
            }
        };
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    Group g = groups.get(position);
                    GroupVC.group = g;
                    Intent i = new Intent(GroupFragment.this.getActivity(), GroupVC.class);
                    startActivity(i);
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }

            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {

                Group gs = groups.get(pos);
                renameGroup(gs.groupId, gs.groupName);
                return true;
            }
        });

        refreshLayout = v.findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                loadData();
            }
        });



        return v;
    }


    public boolean isOnline() {
        ConnectivityManager connec =
                (ConnectivityManager)getActivity().getSystemService(getActivity().getBaseContext().CONNECTIVITY_SERVICE);

        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {

            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {

            Toast.makeText(GroupFragment.this.getActivity(), "Internet is not Connected! ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }


    private void renameGroup(final int groupId, final String groupName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(GroupFragment.this.getActivity());

        final EditText edittext = new EditText(GroupFragment.this.getActivity());
        edittext.setText(groupName);
        String message = "Enter the new Group name";
        alert.setMessage(message);
        alert.setTitle("Edit Group");
        alert.setView(edittext);

        alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String name = edittext.getText().toString();
                Log.d("hq", "update group with name:" + name);

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(GroupFragment.this.getActivity(), "Enter a name", Toast.LENGTH_LONG).show();
                    return;
                }

                DM.getApi().putGroupname(DM.getAuthString(), groupId, edittext.getText().toString() , new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {

                        Toast.makeText(GroupFragment.this.getActivity(), "Group Updated!", Toast.LENGTH_LONG).show();
                        DM.hideKeyboard(GroupFragment.this.getActivity());
                        loadData();
                        refreshLayout.setRefreshing(true);
                    }

                    @Override
                    public void failure(RetrofitError error) {

                        Toast.makeText(GroupFragment.this.getActivity(), "Could not update group:" + error.getMessage(), Toast.LENGTH_LONG).show();
                        DM.hideKeyboard(GroupFragment.this.getActivity());
                    }
                });
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();
    }

    // NEEDED with configChange in manifest, stops view changer from recalling onCreateView
    private boolean initialLoaded = false;
    public void loadIfUnloaded()
    {
        if(initialLoaded == false) loadData();
    }



    private void loadData()
    {

        initialLoaded = true;
        //checkPermission();

        final ProgressDialog pd = DM.getPD(getActivity(),"Loading Groups...");
        if(this.isVisible())pd.show();


        String auth = DM.getAuthString();


        DM.getApi().getAllGrouping(auth, new Callback<GroupResponse>() {
            @Override
            public void success(GroupResponse gs, Response response) {


                groups = gs.getData();
                gridAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                pd.dismiss();

                if(gs.getData().size()==0)  {
                    emptyIV.setVisibility(View.VISIBLE);
                    tv_group_edit.setVisibility(View.GONE);
                }
                else {
                    emptyIV.setVisibility(View.GONE);
                    tv_group_edit.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                refreshLayout.setRefreshing(false);
                pd.dismiss();

            }
        });

    }

}

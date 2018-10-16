package com.hq.nwjsahq;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hq.nwjsahq.models.Event;
import com.hq.nwjsahq.models.EventResponse;
import com.hq.nwjsahq.models.Group;

import java.util.List;
import java.util.Vector;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    Event event;
    Group group;
    private ListView listView;
    private ArrayAdapter<Event> listAdapter;
    private SwipeRefreshLayout refreshLayout;
    private ImageView emptyIV;
    private TextView tvMsg;
    public static boolean oneShotRefresh = false;


    //MODELS
    private List<Event> events = new Vector<Event>(); //empty



    public EventsFragment() {
        // Required empty public constructor
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_menu, menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //theres only create
        if(item.getItemId() == R.id.create)this.newEventAction();
        return super.onOptionsItemSelected(item);
    }

    private void newEventAction()
    {
        Log.d("hq","click!");
        Intent i = new Intent(EventsFragment.this.getContext(),EventFormVC.class);
        startActivity(i);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(false);
        // Inflate the layout for this fragment
        inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.fragment_events, container, false);

        emptyIV = v.findViewById(R.id.empty);
        tvMsg = v.findViewById(R.id.tvMessage);

        listView = v.findViewById(R.id.list);

        listAdapter= new ArrayAdapter<Event>(this.getContext(), R.layout.event_cell){


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = LayoutInflater.from(EventsFragment.this.getContext()).inflate(R.layout.event_cell, parent, false);
                }

                Event e = events.get(position);

                TextView titleTV = convertView.findViewById(R.id.titleTV);
                titleTV.setText(e.eventName);

                TextView tv_group_title = convertView.findViewById(R.id.tv_group_title);
                tv_group_title.setText(e.groupName);

                TextView locationTV = convertView.findViewById(R.id.locationTV);
                locationTV.setText("At "+e.location);

                String topString = " <font color='#d7d7d7'> At </font>" +e.location ;
                locationTV.setText(Html.fromHtml(topString));

                TextView timeTV = convertView.findViewById(R.id.timeTV);
                timeTV.setText(e.getDateString()+"\n"+e.getTimeString());

                return convertView;
            }

            @Override
            public int getCount() {
                return events.size();
            }
        };
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event e = events.get(position);
                EventVC.event = e;

                Intent i = new Intent(EventsFragment.this.getActivity(), EventVC.class);
                i.putExtra("Key",e.groupName);
                startActivity(i);
            }
        });


        refreshLayout = v.findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(this);


        return v;
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

        final ProgressDialog pd = DM.getPD(this.getActivity(),"Loading Events...");
        if(true)pd.show();

        String auth = DM.getAuthString();


        DM.getApi().getAllEventings(auth,new Callback<EventResponse>() {
            @Override
            public void success(EventResponse events, Response response) {


                EventsFragment.this.events = events.getData();
                Log.d("hq", "events: "+events.getData().size()+"");
                listAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                pd.dismiss();

                if(events.getData().size()==0) emptyIV.setVisibility(View.VISIBLE);
                else emptyIV.setVisibility(View.GONE);

            }

            @Override
            public void failure(RetrofitError error) {

                Log.d("hq", "failed: "+error.getMessage());
                refreshLayout.setRefreshing(false);
                pd.dismiss();
            }
        });

    }

    @Override
    public void onRefresh() {
        loadData();

    }

    @Override
    public void onResume() {

        if(oneShotRefresh)
        {
            loadData();
            oneShotRefresh = true;
        }

        super.onResume();


    }


}

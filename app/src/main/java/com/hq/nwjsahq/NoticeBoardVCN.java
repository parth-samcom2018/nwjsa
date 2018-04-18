package com.hq.nwjsahq;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hq.nwjsahq.api.API;
import com.hq.nwjsahq.models.Event;
import com.hq.nwjsahq.models.Group;
import com.hq.nwjsahq.models.GroupResponse;
import com.hq.nwjsahq.models.MediaAlbum;
import com.hq.nwjsahq.models.Notification;
import com.hq.nwjsahq.models.NotificationResponse;
import com.hq.nwjsahq.views.TextPoster;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;
import java.util.Vector;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NoticeBoardVCN extends Fragment {

    //MODEL
    public Group group; //OPTIONAL!
    private List<Notification> notifications = new Vector<Notification>();
    private List<Group> userGroups = null;

    //VIEWS
    private SwipeRefreshLayout refreshLayout;
    private ListView listView;
    private ArrayAdapter listAdapter;
    private TextPoster textPoster;
    private ImageView emptyIV,userIV,iv;
    private CircleImageView useriv;
    private Button flagButton;
    private boolean online;

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;


    public NoticeBoardVCN() {
        // Required empty public constructor
    }

    private void postComment(final String text)
    {
        if(text.length() == 0)
        {
            Toast.makeText(this.getActivity(),"You must enter text",Toast.LENGTH_LONG).show();
            return;
        }

        DM.hideKeyboard(this.getActivity());



        if(group == null)
        {
            Toast.makeText(this.getActivity(),"Must be within a group to post",Toast.LENGTH_LONG).show();
            return;
        }


        final ProgressDialog pd = DM.getPD(getActivity(),"Posting Comment...");
        pd.show();

        Notification n = new Notification();
        n.text = text;
        n.familyId = group.groupId;
        n.familyName = group.groupName;

        Log.d("HQ","text: "+text+" familyId:" +group.groupId+ " familyName:"+group.groupName);


        /*DM.getApi().postNotification(DM.getAuthString(), n,  new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

                Toast.makeText(getActivity(),"Notification Posted!",Toast.LENGTH_LONG).show();
                textPoster.clearText();
                loadData(true);
                pd.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {

                Toast.makeText(getActivity(),"Post failed "+error.getMessage(),Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        });*/

        DM.getApi().postNotifications(DM.getAuthString(), n,  new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

                Toast.makeText(getActivity(),"Notification Posted!",Toast.LENGTH_LONG).show();
                textPoster.clearText();
                loadData(true);
                refreshLayout.setRefreshing(false);
                pd.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {

                Toast.makeText(getActivity(),"Post failed "+error.getMessage(),Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        });

    }

    private void makeInviteRequest(String email)
    {
        DM.hideKeyboard(NoticeBoardVCN.this.getActivity());

        final ProgressDialog pd = DM.getPD(getActivity(),"Inviting User...");
        pd.show();

        Log.d("HQ","groupID : "+this.group.groupId);

        /*DM.getApi().postInviteUser(DM.getAuthString(), "unknown", email, true, this.group.groupId, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

                Toast.makeText(getActivity(), "User has been invited!", Toast.LENGTH_LONG).show();
                pd.dismiss();

            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to invite user", Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        });*/
        DM.getApi().postInviteUsers(DM.getAuthString(), "unknown", email, true, this.group.groupId, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Toast.makeText(NoticeBoardVCN.this.getActivity(), "User has been invited!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(NoticeBoardVCN.this.getActivity(), "Failed to invite user", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

    private void inviteUserAction()
    {


        final EditText edittext = new EditText(this.getContext());
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setMessage("Please enter their email address to invite them to the group");
        alert.setTitle("Invite User");

        alert.setView(edittext);

        alert.setPositiveButton("Invite User", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String email = edittext.getText().toString();
                if(email.isEmpty() || !DM.isEmailValid(email))
                {
                    Toast.makeText(getActivity(), "You must provide a valid email", Toast.LENGTH_LONG).show();
                    return;
                }


                makeInviteRequest(email);


            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if(item.getItemId() == R.id.inviteUser)this.inviteUserAction();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


        inflater.inflate(R.menu.invite_user_menu, menu);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        if (group == null) {
            this.getActivity().setTitle("Noticeboard");
        } else {
            setHasOptionsMenu(true);
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notice_board_n, container, false);

        isOnline();

        flagButton = view.findViewById(R.id.flagButton);

        emptyIV = view.findViewById(R.id.empty);
        textPoster = view.findViewById(R.id.textposter);
        if (group == null) textPoster.setVisibility(View.GONE);

        //textPoster = (TextPoster) view.findViewById(R.id.textposter);
        textPoster.setOnSendListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(textPoster.getText());
            }
        });

        listView = view.findViewById(R.id.list);


        listAdapter = new ArrayAdapter(this.getActivity(), R.layout.main_cell_item) {


            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {

                if (convertView == null) {

                    convertView = LayoutInflater.from(NoticeBoardVCN.this.getActivity()).inflate(R.layout.main_cell_item, parent, false);
                }


                final Notification n = notifications.get(position);

                //Text or image...
                if (n.notificationTypeId == Notification.TYPE_MEDIA) {
                    convertView = LayoutInflater.from(NoticeBoardVCN.this.getContext()).inflate(R.layout.main_image_cell, parent, false);

                    iv = convertView.findViewById(R.id.bodyIV);


                    final TextView tv = convertView.findViewById(R.id.secondTV);
                    tv.setText("has Added a Photo");
                    tv.setTextColor(Color.WHITE);


                    //  iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    if (iv != null && n.thumbnailUrl != null) {

                        Log.d("hq", "thumb url:" + n.thumbnailUrl);
                        //   Picasso.with(NoticeBoardVC.this.getContext()).load(n.thumbnailUrl).into(iv);


                        Picasso.Builder builder = new Picasso.Builder(NoticeBoardVCN.this.getContext());
                        builder.listener(new Picasso.Listener() {
                            @Override
                            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                                Log.d("hq", "uri: " + uri.getPath());
                                exception.printStackTrace();
                            }
                        });

                        try {
                            Picasso p = builder.build();
                            //p.load(n.thumbnailUrl).networkPolicy(NetworkPolicy.NO_CACHE).into(iv);
                            //Picasso.with(getActivity()).load(n.thumbnailUrl).transform(new RoundedCornersTransform()).into(iv);
                            p.load(n.thumbnailUrl).placeholder(R.drawable.splashlogo).transform(new NoticeBoardVCN.RoundedCornersTransform()).into(iv);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                    }

                    Button flagButton = convertView.findViewById(R.id.flagButton);
                    flagButton.setOnClickListener(DM.getFlagOnClickListener(getActivity()));


                } else {
                    // has create event
                    convertView = LayoutInflater.from(NoticeBoardVCN.this.getContext()).inflate(R.layout.main_cell_item, parent, false);
                    TextView secondTV = convertView.findViewById(R.id.secondTV);
                    Button btnFlag = convertView.findViewById(R.id.flagButton);
                    //secondTV.setTextColor(Color.WHITE);
                    secondTV.setText(n.text);
                    Linkify.addLinks(secondTV, Linkify.WEB_URLS);

                    btnFlag.setOnClickListener(DM.getFlagOnClickListener(getActivity()));
                    secondTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (n.notificationTypeId == Notification.TYPE_NOTIFICATION) {

                                NotificationVC.notification = n;
                                Intent i = new Intent(NoticeBoardVCN.this.getActivity(), NotificationVC.class);
                                startActivity(i);
                            }
                        }
                    });
                }

                //top title in listitem

                TextView firstTV = convertView.findViewById(R.id.firstTV);
                String topString = "Added " + n.getTimeAgo() + " in the  <font color='#000000'>" + n.familyName + "</font> group";
                firstTV.setText(Html.fromHtml(topString));

                //comments
                final TextView thirdTV = convertView.findViewById(R.id.thirdTV);
                thirdTV.setText(n.getCommentsString());


                //username create event
                TextView usernameTV = convertView.findViewById(R.id.usernameTV);
                usernameTV.setText(n.memberName);

                userIV = (CircleImageView) convertView.findViewById(R.id.imageView);

                if (userIV != null && n.memberAvatar != null) {
                    Picasso p = Picasso.with(NoticeBoardVCN.this.getActivity());
                    p.setIndicatorsEnabled(true);
                    // Log.d("hq","avatar: "+n.memberAvatar);

                    try {
                        p.load(n.memberAvatar)
                                //.networkPolicy(NetworkPolicy.NO_CACHE)
                                .placeholder(R.drawable.splashlogo)
                                //.fetch();
                                .into(userIV);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }


                //CANNOT USE setItemClickListener in Listview because of flag
                // Log.d("hq","notification type: "+n.notificationTypeId);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if (n.notificationTypeId == Notification.TYPE_NOTIFICATION) {
                            NotificationVC.notification = n;
                            Intent i = new Intent(NoticeBoardVCN.this.getActivity(), NotificationVC.class);
                            startActivity(i);
                        }

                        if (n.notificationTypeId == Notification.TYPE_MEDIA) {
                            final ProgressDialog pd = DM.getPD(getActivity(), "Loading Media...");
                            pd.show();
                            DM.getApi().getMediaAlbum(DM.getAuthString(), n.notificationItemId, new Callback<MediaAlbum>() {
                                @Override
                                public void success(MediaAlbum mediaAlbum, Response response) {

                                    pd.dismiss();
                                    MediaDetailVC.mediaAlbum = mediaAlbum;
                                    MediaDetailVC.selectedMediaId = n.mediaId; //can be null

                                    Intent i = new Intent(NoticeBoardVCN.this.getActivity(), MediaDetailVC.class);
                                    startActivity(i);

                                }

                                @Override
                                public void failure(RetrofitError error) {

                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Could not load media, try later", Toast.LENGTH_LONG).show();

                                }
                            });

                            /*DM.getApi().getMediaAlbums(DM.getAuthString(), n.notificationItemId, new Callback<MediaAlbumResponse>() {
                                @Override
                                public void success(MediaAlbumResponse mediaAlbumResponse, Response response) {
                                    pd.dismiss();
                                    MediaDetailVC.mediaAlbum = mediaAlbumResponse.getData();
                                    MediaDetailVC.selectedMediaId = n.mediaId; //can be null

                                    Intent i = new Intent(NoticeBoardVC.this.getActivity(), MediaDetailVC.class);
                                    startActivity(i);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Could not load media, try later", Toast.LENGTH_LONG).show();
                                }
                            });*/
                        }



                        if (n.notificationTypeId == Notification.TYPE_EVENT) {

                            final ProgressDialog pd = DM.getPD(getActivity(), "Loading Event...");
                            pd.show();
                            DM.getApi().getEvent(DM.getAuthString(), n.notificationItemId, new Callback<Event>() {
                                @Override
                                public void success(Event event, Response response) {

                                    pd.dismiss();
                                    EventVC.event = event;
                                    Intent i = new Intent(NoticeBoardVCN.this.getActivity(), EventVC.class);
                                    startActivity(i);

                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Could not load event, try later", Toast.LENGTH_LONG).show();
                                }
                            });

                            /*DM.getApi().getEvents(DM.getAuthString(), n.notificationItemId, new Callback<EventResponse>() {
                                @Override
                                public void success(EventResponse eventResponse, Response response) {
                                    pd.dismiss();
                                    EventVC.event = eventResponse.getData();
                                    Intent i = new Intent(NoticeBoardVC.this.getActivity(), EventVC.class);
                                    startActivity(i);
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Could not load event, try later", Toast.LENGTH_LONG).show();
                                }
                            });*/

                        }
                    }
                });


                return convertView;
            }

            @Override
            public int getCount() {
                return notifications.size();
            }
        };
        listView.setAdapter(listAdapter);


        refreshLayout = view.findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(true);
            }
        });

        // loadData();
        return view;
    }


    // NEEDED with configChange in manifest, stops view changer from recalling onCreateView
    private boolean initialLoaded = false;
    public void loadIfUnloaded()
    {
        if(initialLoaded == false) loadData(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadIfUnloaded(); // only cause it's the initial
        loadData(true);

    }


    private void loadData(boolean withDialog)
    {

        initialLoaded = true;

        final ProgressDialog pd = DM.getPD(this.getActivity(),"Loading Notifications...");
        if(withDialog)pd.show();


        String auth = DM.getAuthString();
        API api = DM.getApi();
        Callback<NotificationResponse> cb = new Callback<NotificationResponse>() {
            @Override
            public void success(NotificationResponse ns, Response response) {
                notifications = ns.getData();
                listAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                pd.dismiss();

                if(ns.getData().size()==0) emptyIV.setVisibility(View.VISIBLE);
                else emptyIV.setVisibility(View.GONE);

            }

            @Override
            public void failure(RetrofitError error) {
                refreshLayout.setRefreshing(false);
                pd.dismiss();
            }
        };

        if(group == null)
        {
            //get all notification
            //api.getAllNotifications(auth,cb);
            api.getAllNotificationsnew(auth,cb);
        }
        else
        {
            //api.getGroupNotifications(auth,group.groupId,cb);
            api.getGroupNotificationsnew(auth,group.groupId,cb);
        }

        //Load user groups secretly in background
        /*DM.getApi().getAllGroups(auth, new Callback<List<Group>>() {
            @Override
            public void success(List<Group> gs, Response response) {
                userGroups = gs;

            }

            @Override
            public void failure(RetrofitError error) {


            }
        });*/


        DM.getApi().getAllGrouping(auth, new Callback<GroupResponse>() {
            @Override
            public void success(GroupResponse groupResponse, Response response) {
                userGroups = groupResponse.getData();
            }

            @Override
            public void failure(RetrofitError error) {
                //Toast.makeText(NoticeBoardVC.this.getActivity(), "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

            Toast.makeText(NoticeBoardVCN.this.getActivity(), "Internet is not Connected! ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }


    private class RoundedCornersTransform implements Transformation {

        public Bitmap getRoundedCornerBitmap(Bitmap bitmap, float r, float v, float r1, float v1) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = 12;

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            float r = size / 4f;

            Bitmap roundedBitmap = getRoundedCornerBitmap(squaredBitmap, r, r, r, r);

            squaredBitmap.recycle();

            return roundedBitmap;
        }

        @Override
        public String key() {
            return "rounded_corners";
        }
    }


}


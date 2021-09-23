package com.example.justtransfer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.InetAddresses;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class sendpage extends AppCompatActivity {
 ListView listView;
 EditText typeMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendpage);
        initializer();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = devicearray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(sendpage.this,"Connected to device"+device.deviceName,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(sendpage.this,"not connected to device"+device.deviceName,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public void sendMessage(View v) {
        ExecutorService executorService;
        executorService = Executors.newSingleThreadExecutor();
        String msg = typeMsg.getText().toString();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if(msg!=null && ishost)
                {
                    serverclass.write(msg.getBytes());
                }
            }
        });

    }



    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;


    List<WifiP2pDevice> peers=new ArrayList<WifiP2pDevice>();
    String[] devicvenamearray;
    WifiP2pDevice[] devicearray;

    Socket socket;
    Serverclass serverclass;

    boolean ishost;

    public void initializer()
    {
        typeMsg=findViewById(R.id.editTextTextMultiLine);
        listView = findViewById(R.id.listview);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this,getMainLooper(),null);
        receiver = new WiFiDirectBroadcastReceiver(manager,channel,this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }
    public void connect(View view)
    {
       manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
           @Override
           public void onSuccess() {
               Toast.makeText(sendpage.this,"Found",
                       Toast.LENGTH_SHORT).show();
           }

           @Override
           public void onFailure(int i) {
               Toast.makeText(sendpage.this,"Not Found",
                       Toast.LENGTH_SHORT).show();
           }
       });
    }



    WifiP2pManager.PeerListListener peerListListener =new WifiP2pManager.PeerListListener() {
         @Override
         public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
             if(!wifiP2pDeviceList.equals(peers))
             {
                 peers.clear();
                 peers.addAll(wifiP2pDeviceList.getDeviceList());
                 devicvenamearray = new String[wifiP2pDeviceList.getDeviceList().size()];
                 devicearray = new  WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];
                 int index=0;

                 for(WifiP2pDevice device: wifiP2pDeviceList.getDeviceList())
                 {
                     devicvenamearray[index]=device.deviceName;
                     devicearray[index] = device;
                 }
                 ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1,devicvenamearray);
                 listView.setAdapter(adapter);

                 if(peers.size()== 0)
                 {

                     Toast.makeText(sendpage.this,"No Devices",
                             Toast.LENGTH_SHORT).show();
                     return;
                 }
             }
         }
     };


    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                Toast.makeText(sendpage.this,"You are the host",
                        Toast.LENGTH_SHORT).show();
                 ishost = true;
                 serverclass = new Serverclass();
                 serverclass.start();
            }
            else {
                Toast.makeText(sendpage.this,"You are Client",
                        Toast.LENGTH_SHORT).show();
                 ishost = false;
            }
        }
    };
   @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
   public class Serverclass extends Thread {
      ServerSocket serverSocket;
      private InputStream inputStream;
      private OutputStream outputStream;



      public void write(byte[] bytes){
          try {
              outputStream.write(bytes);
          } catch (IOException e) {
              e.printStackTrace();
          }
      }

       @Override
       public void run() {
           try {
               serverSocket = new ServerSocket(8888);
               socket = serverSocket.accept();
               inputStream = socket.getInputStream();
               outputStream = socket.getOutputStream();

           } catch (IOException e) {
               e.printStackTrace();
           }

           ExecutorService executor = Executors.newSingleThreadExecutor();
           Handler handler = new Handler (Looper.getMainLooper());

           executor.execute(new Runnable() {
               @Override
               public void run() {
                   byte[] buffer = new byte[1024];
                   int bytes;

                   while(socket!=null)
                   {
                       try {
                           bytes = inputStream.read(buffer);
                           if(bytes>0) {
                               int finalBytes = bytes;
                               handler.post(new Runnable() {
                                   @Override
                                   public void run() {
                                       String temp = new String(buffer,0,finalBytes);
                                       TextView textView;
                                       textView=findViewById(R.id.textView3);
                                       textView.setText(temp);
                                   }
                               });
                           }


                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               }
           });

       }
   }

}
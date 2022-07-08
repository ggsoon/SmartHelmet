package com.example.bluetooth_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.AudioManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; //블루투스 소켓
    private OutputStream outputStream = null; //블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; //문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; //수신된 문자열 저장 버퍼
    private int readBufferPosition; //버퍼  내 문자 저장 위치

    private TextView power_onoff;
    private TextView textView_auto;
    private TextView textView_conncetDevice;
    private TextView textView_gas;
    private TextView textView_light;
    private TextView textView_touch;
    private TextView textView_impulse;
    private TextView textView_Location;
    private TextView textView_stateon;
    private TextView textView_equal;
    private TextView textView_gasstate;
    private TextView textView_lightstate;
    private TextView textView_impulsestate;
    private ImageButton button_help;
    private ToggleButton button_auto;
    private ImageButton button_state;
    private ToggleButton button_power;
    private Dialog dialog_help;
    private Dialog dialog_auto;
    private BackPressCloseHandler backkeyclickhandler;

    private ImageView ImageView_lightimage;
    private ImageView ImageView_impulseimage;
    private ImageView ImageView_gasimage;
    private ImageView ImageView_helmetimage;
    private ImageView light_state;
    private ImageView gas_state;

    private EditText editTextSend;
    private Button buttonSend;

    SoundPool soundPool;   //작성
    int soundID;      //작성
    int soundID2;      //작성

    boolean connect_status;
    int pairedDeviceCount; //페어링 된 기기의 크기를 저장할 변수
    int gas;
    int light;
    int touch;
    int impulse;
    static double lat;
    static float lon;
    String[] array = {"0"};
    LinearLayout back;

    //지오코더 객체 생성

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //onCreate 함수. 아래에 추가할 내용을 적는다
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backkeyclickhandler = new BackPressCloseHandler(this);

        //앱 소리 코드
        soundPool = new SoundPool(5,AudioManager.STREAM_MUSIC,0);   //작성
        soundID = soundPool.load(this,R.raw.impulse,1);   //작성, (mp3 파일 이름이 click_sound이다.)
        soundID2 = soundPool.load(this,R.raw.gas,1);   //작성, (mp3 파일 이름이 click_sound이다.)

        //위치권한 허용 코드
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);


        //각 변수의 id를 메인 xml과 일치시키는 작업
        ImageView_lightimage = (ImageView) findViewById(R.id.lightimage);
        ImageView_impulseimage = (ImageView) findViewById(R.id.impulseimage);
        ImageView_helmetimage = (ImageView) findViewById(R.id.helmetimage);
        ImageView_gasimage = (ImageView) findViewById(R.id.gasimage);
        textView_light = (TextView) findViewById(R.id.light);
        textView_gas = (TextView) findViewById(R.id.gas);
        textView_touch = (TextView) findViewById(R.id.touch);
        textView_impulse = (TextView) findViewById(R.id.impulse);
        textView_Location = (TextView) findViewById(R.id.location);
        textView_auto = (TextView) findViewById(R.id.auto_onoff);
        textView_gasstate = (TextView) findViewById(R.id.gasstate);
        textView_lightstate = (TextView) findViewById(R.id.lightstate);
        textView_impulsestate = (TextView) findViewById(R.id.impulsestate);
        textView_stateon = (TextView) findViewById(R.id.stateon);

        button_state = (ImageButton) findViewById(R.id.state);
        button_auto = (ToggleButton) findViewById(R.id.auto);
        button_help = (ImageButton) findViewById(R.id.help);
        editTextSend = (EditText)findViewById(R.id.editText_send);
        buttonSend = (Button)findViewById(R.id.button_send);

        back = (LinearLayout) findViewById(R.id.background);

        dialog_help = new Dialog(MainActivity.this);
        dialog_help.requestWindowFeature(getWindow().FEATURE_NO_TITLE); //타이틀제거
        dialog_help.setContentView(R.layout.dialog);
        dialog_auto = new Dialog(MainActivity.this);
        dialog_auto.requestWindowFeature(getWindow().FEATURE_NO_TITLE); //타이틀제거
//        dialog_auto.setContentView(R.layout.activity_alarm);

        String deviceName = null;

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendData(editTextSend.getText().toString());
            }
        });

        //블루투스 활성화 코드
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터를 디폴트 어댑터로 설정

        if (bluetoothAdapter == null) { //기기가 블루투스를 지원하지 않을때
            Toast.makeText(getApplicationContext(), "Bluetooth 미지원 기기입니다.", Toast.LENGTH_SHORT).show();
            //처리코드 작성
        } else { // 기기가 블루투스를 지원할 때
            if (bluetoothAdapter.isEnabled()) { // 기기의 블루투스 기능이 켜져있을 경우
                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            } else { // 기기의 블루투스 기능이 꺼져있을 경우
                // 블루투스를 활성화 하기 위한 대화상자 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택 값이 onActivityResult함수에서 콜백
                startActivityForResult(intent, REQUEST_ENABLE_BT);
                selectBluetoothDevice();
            }
        }
    }

    public void sendData(String text) {
        // 문자열에 개행문자("\n")를 추가해줍니다.
        text += "\n";
        try{
            // 데이터 송신
            outputStream.write(text.getBytes());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    public void selectBluetoothDevice() {
        //이미 페어링 되어있는 블루투스 기기를 탐색
        devices = bluetoothAdapter.getBondedDevices();
        //페어링 된 디바이스 크기 저장
        pairedDeviceCount = devices.size();
        //페어링 된 장치가 없는 경우
        if (pairedDeviceCount == 0) {
            //페어링 하기 위한 함수 호출
            Toast.makeText(getApplicationContext(), "먼저 Bluetooth 설정에 들어가 페어링을 진행해 주세요.", Toast.LENGTH_SHORT).show();
        }
        //페어링 되어있는 장치가 있는 경우
        else {
            //디바이스를 선택하기 위한 대화상자 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 된 블루투스 디바이스 목록");
            //페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            //모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            //list를 Charsequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            //해당 항목을 눌렀을 때 호출되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }


            });

            //뒤로가기 버튼 누를때 창이 안닫히도록 설정
            builder.setCancelable(false);
            //다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    @Override
    public void onBackPressed() { //뒤로가기 눌렀을때
        //super.onBackPressed();
        backkeyclickhandler.onBackPressed(); //2번누르면 종료
    }

    //연결 함수
    public void connectDevice(String deviceName) {
        //페어링 된 디바이스 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }

        }
        Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " 연결 완료!", Toast.LENGTH_SHORT).show();
        button_state.setBackgroundResource(R.drawable.ic_baseline_bluetoothon_24);
        textView_stateon.setText("켜짐");
        //UUID생성
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        connect_status = true;
        //Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }


        startLocationService();

        button_help.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View View) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.50.91:81/stream"));
            startActivity(intent);
            }

        });
        button_auto.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                showDialogauto();

            }

        });



    }

    public void onClick(View View) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.50.90:81/stream"));
        startActivity(intent);
    }




    public void receiveData() {
        final Handler handler = new Handler();
        //데이터 수신을 위한 버퍼 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        //데이터 수신을 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        //데이터 수신 확인
                        int byteAvailable = inputStream.available();
                        //데이터 수신 된 경우
                        if (byteAvailable > 0) {
                            //입력 스트림에서 바이트 단위로 읽어옴
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            //입력 스트림 바이트를 한 바이트씩 읽어옴
                            for (int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                //개행문자를 기준으로 받음 (한줄)
                                if (tempByte == '\n') {
                                    //readBuffer 배열을 encodeBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    //인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "UTF-8");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            array = text.split(",", 11);
                                            textView_light.setText(array[0]);
                                            textView_gas.setText(array[1]);
                                            textView_touch.setText(array[2]);
                                            textView_impulse.setText(array[3]);

                                            impulse =Integer.parseInt(array[3]);
                                            touch =Integer.parseInt(array[2]);
                                            gas =Integer.parseInt(array[1]);
                                            light=Integer.parseInt(array[0]);

                                            Intent intent = new Intent(MainActivity.this, SubActivity.class);
                                            intent.putExtra("위도", lat);
                                            intent.putExtra("경도", lon);

                                            int greenInt = Color.parseColor("#47CC10");
                                            int redInt = Color.parseColor("#FF1717");

                                            if (gas > 600) {
                                                textView_gasstate.setText("위험");
                                                textView_gasstate.setTextColor(redInt);
                                                ImageView_gasimage.setImageResource(R.drawable.gas);
                                                soundPool.play(soundID2,1f,1f,0,0,1f);   //작성
                                           }
                                            if (gas >= 0 & gas < 600) {
                                                textView_gasstate.setText("안전");
                                                textView_gasstate.setTextColor(greenInt);
                                                ImageView_gasimage.setImageResource(R.drawable.gas2);
                                            }
                                            if (light > 700) {
                                                textView_lightstate.setText("어두움");
                                                textView_lightstate.setTextColor(redInt);
                                                ImageView_lightimage.setImageResource(R.drawable.light);
                                            }
                                            if (light >= 0 & light < 700) {
                                                textView_lightstate.setText("밝음");
                                                textView_lightstate.setTextColor(greenInt);
                                                ImageView_lightimage.setImageResource(R.drawable.light2);
                                            }
                                            if (touch == 1) {
                                                textView_touch.setText("착용");
                                                textView_touch.setTextColor(greenInt);
                                                ImageView_helmetimage.setImageResource(R.drawable.helmet);
                                            }
                                            if (touch == 0) {
                                                textView_touch.setText("미착용");
                                                textView_touch.setTextColor(redInt);
                                                ImageView_helmetimage.setImageResource(R.drawable.helmet2);
                                            }
                                            if (impulse > 40000) {
                                                textView_impulsestate.setText("낙상 감지!");
                                                textView_impulsestate.setTextColor(redInt);
                                                ImageView_impulseimage.setImageResource(R.drawable.impulse);
                                                soundPool.play(soundID,1f,1f,0,0,1f);   //작성
                                            }
                                            if (impulse < 40000) {
                                                textView_impulsestate.setText("안전");
                                                textView_impulsestate.setTextColor(greenInt);
                                                ImageView_impulseimage.setImageResource(R.drawable.impulse2);
                                            }


                                        }
                                    });
                                } // 개행문자가 아닐경우
                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
                try {
                    //1초 마다 받아옴
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        workerThread.start();
    }


    private void startLocationService() {

        // get manager instance
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // set listener
        GPSListener gpsListener = new GPSListener();
        long minTime = 10000;
        float minDistance = 0;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, //실내에서 사용하므로 GPS가 아닌 3G로 값을 받아온다
                minTime,
                minDistance,
                gpsListener);
    }





    private class GPSListener implements LocationListener {

        public void onLocationChanged(Location location) {
            //capture location data sent by current provider
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = getCurrentaddress(latitude, longitude);
            Log.i("GPSLocationService", msg);
            textView_Location.setText(msg);
            //경도위도 -> 주소변환


        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    public String getCurrentaddress(double latitude, double longtitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latitude, longtitude, 7);
        } catch (IOException e) {
            Toast.makeText(this, "지오코더 서비스 사용 불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException i) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        return addresses.get(0).getAdminArea() + " " + addresses.get(0).getLocality() + " "
                + addresses.get(0).getSubLocality();
    }

    public void showDialoghelp() {


        WindowManager.LayoutParams Params = dialog_help.getWindow().getAttributes();
        Params.width = WindowManager.LayoutParams.MATCH_PARENT;
        Params.height = WindowManager.LayoutParams.MATCH_PARENT;
        ImageButton ExitButton = dialog_help.findViewById(R.id.exit);
        dialog_help.getWindow().setAttributes((WindowManager.LayoutParams) Params);
        dialog_help.show();
        dialog_help.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        ExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_help.dismiss(); //다이얼로그 닫기
            }
        });

    }
    public void showDialogauto() {


        WindowManager.LayoutParams Params = dialog_auto.getWindow().getAttributes();
        Params.width = WindowManager.LayoutParams.MATCH_PARENT;
        Params.height = WindowManager.LayoutParams.MATCH_PARENT;
        ImageButton ExitButton = dialog_auto.findViewById(R.id.exit);
        dialog_auto.getWindow().setAttributes((WindowManager.LayoutParams) Params);
        dialog_auto.show();
        dialog_auto.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        ExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_auto.dismiss(); //다이얼로그 닫기
            }
        });

    }
}
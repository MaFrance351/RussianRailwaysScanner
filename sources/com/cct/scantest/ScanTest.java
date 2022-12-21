package com.cct.scantest;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

public class ScanTest extends Activity implements View.OnClickListener {
    private static final String LOGTAG = "ScanTest";
    private Button btnExit;
    private Button btnScan;
    private FileWriter cmdOutput;
    /* access modifiers changed from: private */
    public InputStream dataInput;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            ScanTest.this.textCode.setText((String) msg.obj);
            new Thread(new SoundPlayer(ScanTest.this, (SoundPlayer) null)).start();
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mRestoreGpioState = new Runnable() {
        public void run() {
            if (!ScanTest.this.mRestoreGpioStateHandled) {
                ScanTest.this.mRestoreGpioStateHandled = true;
                ScanTest.this.setScanGpioState(false);
            }
        }
    };
    /* access modifiers changed from: private */
    public volatile boolean mRestoreGpioStateHandled = false;
    private SerialPort serialPort;
    /* access modifiers changed from: private */
    public TextView textCode;

    private class SoundPlayer implements Runnable {
        /* access modifiers changed from: private */
        public MediaPlayer mPlayer;

        private SoundPlayer() {
        }

        /* synthetic */ SoundPlayer(ScanTest scanTest, SoundPlayer soundPlayer) {
            this();
        }

        public void run() {
            this.mPlayer = MediaPlayer.create(ScanTest.this.getApplicationContext(), R.raw.ok);
            this.mPlayer.setLooping(false);
            this.mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    SoundPlayer.this.mPlayer.release();
                }
            });
            this.mPlayer.start();
        }
    }

    private final class SerialEventsListener implements SerialPortEventListener {
        private SerialEventsListener() {
        }

        /* synthetic */ SerialEventsListener(ScanTest scanTest, SerialEventsListener serialEventsListener) {
            this();
        }

        public void serialEvent(SerialPortEvent ev) {
            if (ev.getEventType() == 1) {
                byte[] readBuffer = new byte[512];
                String sReadBuff = "";
                while (ScanTest.this.dataInput.available() > 0) {
                    try {
                        sReadBuff = String.valueOf(sReadBuff) + new String(readBuffer, 0, ScanTest.this.dataInput.read(readBuffer), "UTF-8");
                    } catch (IOException e) {
                        Log.e(ScanTest.LOGTAG, "Error Reading from serial port", e);
                    }
                }
                Log.d(ScanTest.LOGTAG, "read data:" + sReadBuff);
                Message.obtain(ScanTest.this.mHandler, 1, sReadBuff).sendToTarget();
                if (!ScanTest.this.mRestoreGpioStateHandled) {
                    ScanTest.this.mHandler.removeCallbacks(ScanTest.this.mRestoreGpioState);
                    ScanTest.this.setScanGpioState(false);
                }
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.textCode = (TextView) findViewById(R.id.text_code);
        this.btnScan = (Button) findViewById(R.id.btn_scan);
        this.btnExit = (Button) findViewById(R.id.btn_exit);
        this.btnScan.setOnClickListener(this);
        this.btnExit.setOnClickListener(this);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        registerPort();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        unRegisterPort();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_scan) {
            this.textCode.setText("");
            laserScan();
            return;
        }
        setScanGpioState(false);
        unRegisterPort();
        Process.killProcess(Process.myPid());
    }

    private void laserScan() {
        setScanGpioState(false);
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(LOGTAG, e.toString());
        }
        setScanGpioState(true);
        this.mRestoreGpioStateHandled = false;
        this.mHandler.postDelayed(this.mRestoreGpioState, 3000);
    }

    /* access modifiers changed from: private */
    public void setScanGpioState(boolean state) {
        if (this.cmdOutput != null) {
            try {
                this.cmdOutput.write(state ? 48 : 49);
                this.cmdOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerPort() {
        try {
            this.serialPort = new RXTXPort("/dev/ttyMSM2");
            this.dataInput = this.serialPort.getInputStream();
            this.cmdOutput = new FileWriter("/dev/scan");
            this.serialPort.addEventListener(new SerialEventsListener(this, (SerialEventsListener) null));
            this.serialPort.notifyOnDataAvailable(true);
            this.serialPort.setSerialPortParams(9600, 8, 1, 0);
            this.serialPort.setFlowControlMode(0);
        } catch (IOException e) {
            Log.e(LOGTAG, "I/O Exception " + e.getMessage());
        } catch (PortInUseException e2) {
            Log.e(LOGTAG, "Port in use by " + e2.currentOwner);
        } catch (UnsupportedCommOperationException e3) {
            Log.e(LOGTAG, "Unsupported Operation " + e3.getMessage());
        } catch (TooManyListenersException e4) {
            Log.e(LOGTAG, "Too many listeners");
        }
    }

    private void unRegisterPort() {
        if (this.dataInput != null) {
            try {
                this.dataInput.close();
            } catch (IOException e) {
                Log.e(LOGTAG, "unRegisterPort Input", e);
            }
            this.dataInput = null;
        }
        if (this.cmdOutput != null) {
            try {
                this.cmdOutput.close();
            } catch (IOException e2) {
                Log.e(LOGTAG, "unRegisterPort Output", e2);
            }
        }
        if (this.serialPort != null) {
            this.serialPort.removeEventListener();
            this.serialPort.close();
        }
        this.serialPort = null;
    }
}

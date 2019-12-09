package com.example.brainchess;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.pwittchen.neurosky.library.*;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import static com.github.pwittchen.neurosky.library.message.enums.Signal.ATTENTION;
import static com.github.pwittchen.neurosky.library.message.enums.Signal.BLINK;

public class MainActivity extends AppCompatActivity {

    public static int x;
    int letters1 = 0;
    int letters2 = 0;
    int numbers1 = 0;
    int numbers2 = 0;
    int blinkOld;
    int blinkOldTemp;
    public static int tempx;
    boolean square1f = false;
    boolean square1 = false;
    boolean square2f = false;
    boolean square2 = false;
    NeuroSky neuroSky;
    Square[][] move;
    Board board;
    String fen;
    boolean attn = false;
    long start;
    long finish;
    long timeElapsed;
    Context context;
    File file;
    File path;
    String data;
    int dataPoint;
    String moveRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize NeuroSky object with listener


        final TextView boardDisplay =(TextView)findViewById(R.id.BoardDisplay);
        final TextView xint = (TextView)findViewById(R.id.xint);
        final TextView attentionDisplay = (TextView)findViewById(R.id.AttentionDisplay);
        final TextView currentCoordinates = (TextView)findViewById(R.id.coordinates);
        final Button dataButton = (Button) findViewById(R.id.dataButton);





        x = 1;
        tempx = 0;
        board = new Board();
        move = new Square[][]{
                {Square.A1, Square.B1, Square.C1, Square.D1, Square.E1, Square.F1, Square.G1, Square.H1},
                {Square.A2, Square.B2, Square.C2, Square.D2, Square.E2, Square.F2, Square.G2, Square.H2},
                {Square.A3, Square.B3, Square.C3, Square.D3, Square.E3, Square.F3, Square.G3, Square.H3},
                {Square.A4, Square.B4, Square.C4, Square.D4, Square.E4, Square.F4, Square.G4, Square.H4},
                {Square.A5, Square.B5, Square.C5, Square.D5, Square.E5, Square.F5, Square.G5, Square.H5},
                {Square.A6, Square.B6, Square.C6, Square.D6, Square.E6, Square.F6, Square.G6, Square.H6},
                {Square.A7, Square.B7, Square.C7, Square.D7, Square.E7, Square.F7, Square.G7, Square.H7},
                {Square.A8, Square.B8, Square.C8, Square.D8, Square.E8, Square.F8, Square.G8, Square.H8},
        };
        try{

        }
        catch(NullPointerException e){

        }

        context = getApplicationContext();
        path = context.getExternalFilesDir(null);
        file = new File(path, "collected.txt");
        dataPoint = 1;

        fen = board.getFen();

        board.loadFromFen(fen);
        boardDisplay.setText(board.toString().toUpperCase());


        neuroSky = new NeuroSky(new ExtendedDeviceMessageListener() {
            @Override public void onStateChange(State state) {
                // handle state change...
            }

            @Override public void onSignalChange(Signal signal) {
                blinkOld = 50;
                xint.setText(" " +x);
                attentionDisplay.setText(ATTENTION.getValue() + "");
                ATTENTION.getValue();
                currentCoordinates.setText("numbers1 is " +(numbers1) + "\nletters1 is " + (letters1) +" \nnumbers2 is " +(numbers2)+" \nletters2 is " + (letters2) );


                if (BLINK.getValue() > 51 && BLINK.getValue() != blinkOldTemp) {
                    blinkOld = BLINK.getValue();
                    blinkOldTemp = BLINK.getValue();
                    x++;
                }
                if (ATTENTION.getValue() > 70 && attn == false) {
                    blinkOld = BLINK.getValue();
                    blinkOldTemp = BLINK.getValue();
                    attn = true;
                    tempx = 3;
                }
                if(ATTENTION.getValue() < 50 && attn == true){
                    attn = false;
                }
                if(x == 10){
                    x = 1;
                    tempx = 0;
                }
                if (tempx == 3 && square1f == false && square1 == false){
                    numbers1 = x-1;
                    square1f = true;
                    tempx = 0;
                    x = 1;
                }
                if(tempx == 3 && square1f == true && square1 == false){
                   letters1 = x-1;
                   x = 1;
                   tempx = 0;
                   square1 = true;
                }

                if(tempx == 3 && square2f == false && square1 == true && square2 == false){
                    numbers2 = x-1;
                    x = 1;
                    tempx = 0;
                    square2f = true;
                }
                if(tempx == 3 && square2f == true){
                    letters2 = x-1;
                    x = 1;
                    tempx = 0;
                    square2 = true;
                }
                if(square1 == true && square2 == true){
                    finish = System.currentTimeMillis();
                    moveRecord = "Square " +move[numbers1][letters1].toString() + " moved to "+move[numbers2][letters2];

                    try{
                        timeElapsed = (finish - start)/1000;
                        board.doMove(new Move(move[numbers1][letters1],move[numbers2][letters2]));
                        fen = board.getFen();
                        board.loadFromFen(fen);
                        numbers1 = 0;
                        numbers2 = 0;
                        letters1 = 0;
                        letters2 = 0;
                        if(data != null){
                            data = data + " " +moveRecord+""+timeElapsed+" success " + "\n";
                            dataPoint++;
                        }
                        else{
                            data = ""+" " +moveRecord+" "+timeElapsed+" success " + "\n";
                            dataPoint++;
                        }
                        start = System.currentTimeMillis();
                    }
                    catch(NullPointerException c){
                        if(data != null){
                            data = data + " "+moveRecord+" "+timeElapsed+" fail " + "\n";
                            dataPoint++;
                        }
                        else{
                            data = " "  +moveRecord+" "+timeElapsed+" fail " + "\n";
                            dataPoint++;
                        }
                    }

                    dataButton.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            if (data != null){
                                writeToFile(data, file);
                            }
                            //wifiStrength.setText("" + level + " boop " +addresses.get(0).getAddressLine(0));

                        }
                    });
                    square1 = false;
                    square2 = false;
                    square1f = false;
                    square2f = false;
                    boardDisplay.setText(board.toString().toUpperCase());
                }
            }

            @Override public void onBrainWavesChange(Set<BrainWave> brainWaves) {

                // handle brain waves change...
            }
        });






// connect to the device
        while(neuroSky.isConnected() == false){
            try {
                neuroSky.connect();
            } catch (BluetoothNotEnabledException e) {
                // handle exception...
            }
        }
        start = System.currentTimeMillis();
        neuroSky.start();


    }//End of OnCreate
    private void writeToFile(String dataM , File file) {
        try {

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(dataM.getBytes());
            } finally {
                stream.close();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        neuroSky.stop();
        neuroSky.disconnect();
    }
}


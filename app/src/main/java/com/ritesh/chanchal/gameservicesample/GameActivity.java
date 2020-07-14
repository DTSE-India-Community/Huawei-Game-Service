package com.ritesh.chanchal.gameservicesample;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.games.EventsClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.RankingsClient;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.ritesh.chanchal.gameservicesample.common.SignInCenter;

import org.json.JSONException;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private Button[][] buttons = new Button[3][3];
    private boolean player1Turn = true;
    private int roundCount;
    private int player1Points;
    private int player2Points;
    private TextView textViewPlayer1;
    private TextView textViewPlayer2;

    private static final String EVENT_ID = "generate_event_Id_from AGC" ;
    private static final String LEADERBOARD_ID = "generate_leaderBoard_Id_from AGC" ;


    private static final String TAG = "GameActivity";
    public static final int SIGN_IN_REQUEST_CODE = 8888;

    private final static int HEARTBEAT_TIME = 15 * 60 * 1000;
    private Handler handler;
    private String playerId;
    private String sessionId = null;
    private Button loadEventBtn, btnLBTopScore, btnLBSummary;

    EventsClient eventsClient ;

    RankingsClient rankingsClient ;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        builder = new AlertDialog.Builder(this);
        eventsClient = Games.getEventsClient(this, SignInCenter.get().getAuthHuaweiId());
        rankingsClient = Games.getRankingsClient(this, SignInCenter.get().getAuthHuaweiId());
        enableRankingSwitchStatus (1);
        loadEventBtn = findViewById(R.id.button_loadevent);
        btnLBTopScore = findViewById(R.id.leaderboardtopscore);
        btnLBSummary = findViewById(R.id.leaderboardSummary);

        textViewPlayer1 = findViewById(R.id.text_view_p1);
        textViewPlayer2 = findViewById(R.id.text_view_p2);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "button_" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(this);
            }
        }

    Button buttonReset = findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetGame();
        }
    });
        loadEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEvent(true, "");
            }
        });
        btnLBTopScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, LeaderBoardActivity.class);
                intent.putExtra("LB", 1);
                startActivity(intent);
            }
        });
        btnLBSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, LeaderBoardActivity.class);
                intent.putExtra("LB", 2);
                startActivity(intent);
            }
        });
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent(GameActivity.this, LeaderBoardActivity.class);
        switch (id){
            case R.id.leaderboardSummary:

                intent.putExtra("LB", 2);
                startActivity(intent);
                return true;
            case R.id.leaderboardtopscore:
                intent.putExtra("LB", 1);
                startActivity(intent);
                return true;
            case R.id.loadCurrentPlayerRankingScore:
                intent.putExtra("LB", 3);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View v) {
        if (!((Button) v).getText().toString().equals("")) {
            return;
        }
        if (player1Turn) {
            ((Button) v).setText("X");
            ((Button) v).setTextColor(Color.RED);
        } else {
            ((Button) v).setText("O");
            ((Button) v).setTextColor(Color.parseColor("#FCDC04"));
        }
        roundCount++;
        if (checkForWin()) {
            if (player1Turn) {
                player1Wins();
            } else {
                player2Wins();
            }
        } else if (roundCount == 9) {
            draw();
        } else {
            player1Turn = !player1Turn;
        }
    }
    private boolean checkForWin() {
        String[][] field = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1])
                    && field[i][0].equals(field[i][2])
                    && !field[i][0].equals("")) {
                return true;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (field[0][i].equals(field[1][i])
                    && field[0][i].equals(field[2][i])
                    && !field[0][i].equals("")) {
                return true;
            }
        }
        if (field[0][0].equals(field[1][1])
                && field[0][0].equals(field[2][2])
                && !field[0][0].equals("")) {
            return true;
        }
        if (field[0][2].equals(field[1][1])
                && field[0][2].equals(field[2][0])
                && !field[0][2].equals("")) {
            return true;
        }
        return false;
    }
    private void player1Wins() {
       int score=  player1Points++;
//        Toast.makeText(this, "Player 1 wins!", Toast.LENGTH_SHORT).show();
        showDialog("Congrats! You won!");
        eventsClient.grow(EVENT_ID, 1);
        if(score == 6) {
            submitRanking(player1Points++);
        }
        updatePointsText();
        resetBoard();
    }
    private void player2Wins() {
        player2Points++;
//        Toast.makeText(this, "Player 2 wins!", Toast.LENGTH_SHORT).show();
        showDialog("You lost! player 2 wins.");
        updatePointsText();
        resetBoard();
    }
    private void draw() {
//        Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
        showDialog("No result. Draw!");
        resetBoard();
    }
    private void updatePointsText() {
        textViewPlayer1.setText("Player 1: " + player1Points);
        textViewPlayer2.setText("Player 2: " + player2Points);
    }
    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
            }
        }
        roundCount = 0;
        player1Turn = true;
    }
    private void resetGame() {
        player1Points = 0;
        player2Points = 0;
        updatePointsText();
        resetBoard();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("roundCount", roundCount);
        outState.putInt("player1Points", player1Points);
        outState.putInt("player2Points", player2Points);
        outState.putBoolean("player1Turn", player1Turn);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        roundCount = savedInstanceState.getInt("roundCount");
        player1Points = savedInstanceState.getInt("player1Points");
        player2Points = savedInstanceState.getInt("player2Points");
        player1Turn = savedInstanceState.getBoolean("player1Turn");
    }

    private void showLog(String log) {
        Log.d(TAG, log);
    }
    private void submitRanking(int score) {

        rankingsClient.submitRankingScore(LEADERBOARD_ID, score);
    }

    private void showDialog(String msg) {



        //Setting message manually and performing action on button click
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetGame();

                    }
                });
        AlertDialog alert = builder.create();
        //Setting the title manually
//        alert.setTitle("Tic Tac Toe");
        alert.show();

        TextView tvMsg = (TextView) alert.findViewById(android.R.id.message);
        tvMsg.setTypeface(Typeface.create("casual", Typeface.BOLD));
//
//
//        int titleId = getResources().getIdentifier( "alertTitle", "id", "android" );
//        if (titleId > 0) {
//            TextView tvTitle = (TextView) alert.findViewById(titleId);
//            tvTitle.setTypeface(Typeface.create("casual", Typeface.BOLD));
//
//        }

        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(getColor(R.color.colorPrimary));
        nbutton.setTypeface(Typeface.create("casual", Typeface.BOLD));

        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(getColor(R.color.colorPrimary));
        pbutton.setTypeface(Typeface.create("casual", Typeface.BOLD));
    }


    private void loadEvent(boolean forceReload, String idsString) {
        AuthHuaweiId authHuaweiId  = SignInCenter.get().getAuthHuaweiId();
        if (authHuaweiId == null) {
            showLog("signIn first");
            return;
        }
        String jString = "";
        try {
            jString = authHuaweiId.toJson();
        } catch (JSONException e) {
            showLog("signIn first");
        }
        Intent intent = new Intent(GameActivity.this, EventListActivity.class);
        intent.putExtra("forceReload", forceReload);
        intent.putExtra("mSign", jString);
        intent.putExtra("idsString", idsString);
        startActivity(intent);
    }

    private void enableRankingSwitchStatus (int status) {
        Task<Integer> task = rankingsClient.setRankingSwitchStatus(status);
        task.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer statusValue) {
                // success to set the value,the server will reponse the latest value.
                Log.d(TAG, "setRankingSwitchStatus success : " +statusValue) ;
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // errCode information
                if (e instanceof ApiException) {
                    String result = "Err Code:" + ((ApiException) e).getStatusCode();
                    Log.e(TAG , "setRankingSwitchStatus error : " + result);
                }
            }
        });
    }
}


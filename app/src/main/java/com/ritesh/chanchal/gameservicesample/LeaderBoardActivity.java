package com.ritesh.chanchal.gameservicesample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huawei.hmf.tasks.OnCanceledListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.RankingsClient;
import com.huawei.hms.jos.games.ranking.Ranking;
import com.huawei.hms.jos.games.ranking.RankingScore;
import com.huawei.hms.jos.games.ranking.RankingVariant;
import com.ritesh.chanchal.gameservicesample.common.SignInCenter;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoardActivity extends Activity {
    private static final String TAG = "LeaderBoardActivity";
    StringBuffer buffer ;
    private RankingsClient rankingsClient;
    private TextView tvLeaderboard, tvLeaderboardtilte;
    private ImageView iv;
    private static final String LEADERBOARD_ID = "5E405459D638E2EF659C93F85841A46087DCF5DF3ADB182B113DC1DFDA7B9FB9" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        tvLeaderboard = findViewById(R.id.tvLeaderboard);
        tvLeaderboardtilte = findViewById(R.id.tvLeaderboardtilte);
        iv = findViewById(R.id.imageviewlb);

        rankingsClient = Games.getRankingsClient(this, SignInCenter.get().getAuthHuaweiId());
        Intent intent = getIntent();
        int intentData = intent.getIntExtra("LB" , 1);
        buffer = new StringBuffer();
        if(intentData == 1) {
            tvLeaderboardtilte.setText("Leaderboard Top Scores");
            String rankingId = LEADERBOARD_ID;
            int timeDimension = 2;
            int maxResults = 20;
            long offsetPlayerRank = 0;
            int pageDirection = 0;
            Task<RankingsClient.RankingScores> task
                    = rankingsClient.getRankingTopScores(rankingId, timeDimension, maxResults, offsetPlayerRank, pageDirection);
            StringBuffer buffer = new StringBuffer();
            addClientRankingScoresListener(task, buffer.toString());
        }

        else if(intentData == 2) {
            tvLeaderboardtilte.setText("Leaderboard summary");
            Task<Ranking> task = rankingsClient.getRankingSummary(LEADERBOARD_ID, true);
            addRankingListener(task);
        }
        else if (intentData == 3) {
            tvLeaderboardtilte.setText("Current player Summary");
            Task<RankingScore> task = rankingsClient.getCurrentPlayerRankingScore(LEADERBOARD_ID, 2);
            addRankingScoreListener(task);
        }
    }


    private void addRankingScoreListener(final Task<RankingScore> task) {

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                Log.d(TAG ," failure. exception: " + e );
            }
        });
        task.addOnSuccessListener(new OnSuccessListener<RankingScore>() {
            @Override
            public void onSuccess(RankingScore s) {
                Log.d(TAG ," success "  );
                showScoreTaskLog(task);
            }
        });
        task.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG , " canceled. ");
            }
        });
    }
    private void showScoreTaskLog(Task<RankingScore> task) {
        StringBuffer buffer = new StringBuffer();
        if (task.getResult() == null) {
            buffer.append("RankingScore result is null");
            return;
        }
        buffer.append("=======RankingScore=======\n");
        RankingScore s = task.getResult();
        printRankingScoreLog(s, 0);

    }
    private void addRankingListener(final Task<Ranking> task) {



        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG , " failure. exception: " + e);
            }
        });
        task.addOnSuccessListener(new OnSuccessListener<Ranking>() {
            @Override
            public void onSuccess(Ranking s) {
                if (task.getResult() == null) {
                    Log.d(TAG, "Ranking result is null");
                    tvLeaderboard.setText("Ranking result is null");
                    return;
                }

                Ranking ranking = task.getResult();
                printRankingLog(ranking);
            }
        });
        task.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG,  " canceled. ");
            }
        });
    }
    private void printRankingLog(Ranking ranking) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("-------Ranking-------\n");
        if (ranking == null) {
            buffer.append("ranking is  null");
        } else {
            buffer.append("\n DisplayName:" + ranking.getRankingDisplayName());

//            buffer.append("\n RankingId:" + ranking.getRankingId());
//            showMetaInfo(" IconImageUri:" + ranking.getRankingImageUri());
//            buffer.append(" DisplayName:" + ranking.getRankingDisplayName());\
            iv.setVisibility(View.VISIBLE);
            Glide.with(this).load(ranking.getRankingImageUri()).into(iv);

            buffer.append("\n ScoreOrder:" + ranking.getRankingScoreOrder());

            if (ranking.getRankingVariants() != null) {
                buffer.append("\n Variants.size:" + ranking.getRankingVariants().size());
                if (ranking.getRankingVariants().size() > 0) {
                    showRankingVariant(ranking.getRankingVariants() , buffer);
                }
            }
        }
    }
    private void showRankingVariant(ArrayList<RankingVariant> list,StringBuffer buffer) {

        int index = 0;
        for (RankingVariant variant : list) {
            buffer.append("\n\n");
            buffer.append("  RankingVariant size->").append(index).append("\n");
            buffer.append("   getPlayerScoreTips():").append(variant.getPlayerScoreTips()).append("\n");
            buffer.append("   getDisplayRanking():").append(variant.getDisplayRanking()).append("\n");
            buffer.append("   getPlayerDisplayScore():").append(variant.getPlayerDisplayScore()).append("\n");
            buffer.append("   getRankTotalScoreNum():").append(variant.getRankTotalScoreNum()).append("\n");
            buffer.append("   getPlayerRank():").append(variant.getPlayerRank()).append("\n");
            buffer.append("   getPlayerRawScore():").append(variant.getPlayerRawScore()).append("\n");
            buffer.append("   timeDimension():").append(variant.timeDimension()).append("\n");
            buffer.append("   hasPlayerInfo():").append(variant.hasPlayerInfo()).append("\n")/*.append("- - - ")*/;
            tvLeaderboard.setText(buffer.toString());
            index++;
        }
    }
    private void addClientRankingScoresListener(final Task<RankingsClient.RankingScores> task, final String method) {

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG , method + " failure. exception: " + e);
            }
        });
        task.addOnSuccessListener(new OnSuccessListener<RankingsClient.RankingScores>() {
            @Override
            public void onSuccess(RankingsClient.RankingScores s) {
                Log.e(TAG ,  " method " + " success. ");
                Ranking ranking = task.getResult().getRanking();
                List<RankingScore> scoresBuffer = task.getResult().getRankingScores();

                for (int i = 0; i < scoresBuffer.size(); i++) {
                    printRankingScoreLog(scoresBuffer.get(i), i);
                }

            }
        });
        task.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG, method + " canceled. ");
            }
        });

    }
    private void printRankingScoreLog(RankingScore s, int index) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("------RankingScore " + index + "------\n");
        if (s == null) {
            buffer.append("rankingScore is null\n");
            return;
        }

        String displayScore = s.getRankingDisplayScore();
        buffer.append("    DisplayScore: " + displayScore).append("\n");

        buffer.append("    TimeDimension: " + s.getTimeDimension()).append("\n");
        buffer.append("    RawPlayerScore: " + s.getPlayerRawScore()).append("\n");
        buffer.append("    PlayerRank: " + s.getPlayerRank()).append("\n");

        String displayRank = s.getDisplayRank();
        buffer.append("    getDisplayRank: " + displayRank).append("\n");

        buffer.append("    ScoreTag: " + s.getScoreTips()).append("\n");
        buffer.append("    updateTime: " + s.getScoreTimestamp()).append("\n");

        String playerDisplayName = s.getScoreOwnerDisplayName();
        buffer.append("    PlayerDisplayName: " + playerDisplayName).append("\n");
        buffer.append("    PlayerHiResImageUri: " + s.getScoreOwnerHiIconUri()).append("\n");
        buffer.append("    PlayerIconImageUri: " + s.getScoreOwnerIconUri()).append("\n\n");
        Log.d(TAG , buffer.toString());
        tvLeaderboard.setText(buffer.toString());
    }
}
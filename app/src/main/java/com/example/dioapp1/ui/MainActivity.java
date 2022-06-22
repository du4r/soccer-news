package com.example.dioapp1.ui;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.dioapp1.R;
import com.example.dioapp1.data.MatchesApi;
import com.example.dioapp1.databinding.ActivityMainBinding;
import com.example.dioapp1.domain.Match;
import com.example.dioapp1.ui.adapter.MatchesAdapter;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MatchesApi matchesApi;
    private MatchesAdapter matchesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatActionButton();
    }

    private void setupHttpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://du4r.github.io/mockings/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        matchesApi = retrofit.create(MatchesApi.class);
    }

    private void setupFloatActionButton() {
        binding.fabSimulate.setOnClickListener(view ->{
            view.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Random random = new Random();
                    for (int i = 0; i < matchesAdapter.getItemCount(); i++) {
                       Match match = matchesAdapter.getMatches().get(i);
                       match.getHomeTeam().setScore(random.nextInt( match.getHomeTeam().getStars() + 1 ));
                        match.getAwayTeam().setScore(random.nextInt( match.getHomeTeam().getStars() + 1 ));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }
            });
        });
    }

    private void setupMatchesList() {
       /////////////////
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        findMatchesFromApi();
    }

    private void findMatchesFromApi() {
        binding.srlMatches.setRefreshing(true);
        matchesApi.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if(response.isSuccessful()){
                    List<Match> matches = response.body();
                    matchesAdapter = new MatchesAdapter(matches);
                    binding.rvMatches.setAdapter(matchesAdapter);
                }else{
                    showErrorMessage();
                }
                binding.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
             binding.srlMatches.setRefreshing(false);
            }
        });
    }

    private void setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener(this:: findMatchesFromApi);
    }



    private void showErrorMessage() {
        Snackbar.make(binding.rvMatches, R.string.error_api, Snackbar.LENGTH_SHORT).show();
    }
}
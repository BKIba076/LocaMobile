
package ca.cuniversel.ca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

import ca.cuniversel.ca.reseau.ClientApi;
import ca.cuniversel.ca.reseau.Logement;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListeActivity extends AppCompatActivity {

    private static final String TAG = "LocaMobile";

    private LinearLayout conteneurLogements;
    private TextView messageErreur;
    private EditText champVille;
    private Button boutonChercher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button boutonOuvrirConnexion =
                findViewById(R.id.boutonOuvrirConnexion);

        boutonOuvrirConnexion.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ListeActivity.this,
                    ca.cuniversel.ca.ConnexionActivity.class
            );
            startActivity(intent);
        });

        conteneurLogements = findViewById(R.id.conteneurLogements);
        messageErreur = findViewById(R.id.messageErreur);
        champVille = findViewById(R.id.champVille);
        boutonChercher = findViewById(R.id.boutonChercher);

        Log.d(TAG, "1");

        boutonChercher.setOnClickListener(v -> {
            String ville = champVille.getText().toString().trim();

            if (ville.isEmpty()) {
                chargerLogements();
            } else {
                rechercherParVille(ville);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerLogements();
    }

    private void chargerLogements() {

        conteneurLogements.removeAllViews();
        messageErreur.setVisibility(View.GONE);

        ClientApi.get().listerLogements().enqueue(new Callback<List<Logement>>() {

            @Override
            public void onResponse(Call<List<Logement>> call,
                                   Response<List<Logement>> response) {

                Log.d(TAG, "2");

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Nombre de logements : " + response.body().size());
                    afficher(response.body());
                } else {
                    afficherErreur("Erreur HTTP : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Logement>> call, Throwable t) {
                Log.e(TAG, "Erreur : " + t.getMessage());
                afficherErreur("Serveur injoignable : " + t.getMessage());
            }
        });
    }

    private void rechercherParVille(String ville) {

        conteneurLogements.removeAllViews();
        messageErreur.setVisibility(View.GONE);

        ClientApi.get().chercherParVille(ville).enqueue(new Callback<List<Logement>>() {

            @Override
            public void onResponse(Call<List<Logement>> call,
                                   Response<List<Logement>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().isEmpty()) {
                        afficherErreur("Aucun logement à " + ville + ".");
                    } else {
                        afficher(response.body());
                    }

                } else {
                    afficherErreur("Erreur HTTP : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Logement>> call, Throwable t) {
                Log.e(TAG, "Erreur : " + t.getMessage());
                afficherErreur("Serveur injoignable : " + t.getMessage());
            }
        });
    }

    private void afficher(List<Logement> logements) {

        conteneurLogements.removeAllViews();
        messageErreur.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Logement logement : logements) {

            View carte = inflater.inflate(
                    R.layout.item_logement,
                    conteneurLogements,
                    false
            );

            TextView titre = carte.findViewById(R.id.carteTitre);
            TextView ville = carte.findViewById(R.id.carteVille);
            TextView prix = carte.findViewById(R.id.cartePrix);

            titre.setText(logement.titre);
            ville.setText(logement.ville);

            prix.setText(String.format(
                    Locale.CANADA_FRENCH,
                    "%.2f $ / nuit",
                    logement.prixParNuit
            ));

            carte.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ListeActivity.this,
                        DetailActivity.class
                );

                intent.putExtra(DetailActivity.CLE_ID, logement.id);
                startActivity(intent);
            });

            conteneurLogements.addView(carte);
        }
    }

    private void afficherErreur(String message) {
        conteneurLogements.removeAllViews();
        messageErreur.setText(message);
        messageErreur.setVisibility(View.VISIBLE);
    }
}


package ca.cuniversel.ca;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import ca.cuniversel.ca.reseau.ClientApi;
import ca.cuniversel.ca.reseau.Logement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "LocaMobile";
    public static final String CLE_ID = "id";

    private TextView detailTitre;
    private TextView detailVille;
    private TextView detailPrix;
    private TextView detailVoyageurs;
    private TextView detailProprietaire;
    private TextView etat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailTitre = findViewById(R.id.detailTitre);
        detailVille = findViewById(R.id.detailVille);
        detailPrix = findViewById(R.id.detailPrix);
        detailVoyageurs = findViewById(R.id.detailVoyageurs);
        detailProprietaire = findViewById(R.id.detailProprietaire);
        etat = findViewById(R.id.etat);

        int id = getIntent().getIntExtra(CLE_ID, -1);

        if (id == -1) {
            etat.setText("Identifiant du logement invalide.");
            return;
        }

        chargerLogement(id);
    }

    private void chargerLogement(int id) {

        ClientApi.get().trouverLogement(id).enqueue(new Callback<Logement>() {

            @Override
            public void onResponse(Call<Logement> call,
                                   Response<Logement> response) {

                if (response.isSuccessful() && response.body() != null) {

                    Logement logement = response.body();

                    detailTitre.setText(logement.titre);
                    detailVille.setText(logement.ville);

                    detailPrix.setText(String.format(
                            Locale.CANADA_FRENCH,
                            "%.2f $ / nuit",
                            logement.prixParNuit));

                    detailVoyageurs.setText(
                            "Nombre de voyageurs : " + logement.nbVoyageurs);

                    detailProprietaire.setText(
                            "Proprietaire : " + logement.proprietaireNom);

                } else if (response.code() == 404) {

                    etat.setText("Logement introuvable (404).");

                } else {
                    etat.setText("Erreur HTTP : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Logement> call, Throwable t) {

                Log.e(TAG, "Erreur : " + t.getMessage());
                etat.setText("Serveur injoignable : " + t.getMessage());
            }
        });
    }
}

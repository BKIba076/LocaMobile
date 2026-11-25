
package ca.cuniversel.ca;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ca.cuniversel.ca.reseau.ClientApi;
import ca.cuniversel.ca.reseau.ConnexionRequete;
import ca.cuniversel.ca.reseau.Profil;
import ca.cuniversel.ca.reseau.Session;
import ca.cuniversel.ca.reseau.TokenReponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnexionActivity extends AppCompatActivity {

    private EditText courriel;
    private EditText motDePasse;
    private TextView resultat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        courriel = findViewById(R.id.courriel);
        motDePasse = findViewById(R.id.motDePasse);
        Button boutonConnexion = findViewById(R.id.boutonConnexion);
        resultat = findViewById(R.id.resultat);

        boutonConnexion.setOnClickListener(v -> {
            String email = courriel.getText().toString().trim();
            String motDePasseSaisi = motDePasse.getText().toString();

            if (email.isEmpty()) {
                courriel.setError("Entrez votre courriel");
                return;
            }

            if (motDePasseSaisi.isEmpty()) {
                motDePasse.setError("Entrez votre mot de passe");
                return;
            }

            ConnexionRequete requete =
                    new ConnexionRequete(email, motDePasseSaisi);

            ClientApi.get().connecter(requete).enqueue(
                    new Callback<TokenReponse>() {

                        @Override
                        public void onResponse(
                                Call<TokenReponse> call,
                                Response<TokenReponse> response) {

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                TokenReponse token = response.body();

                                Session.enregistrer(
                                        ConnexionActivity.this,
                                        token.token,
                                        token.nom
                                );

                                afficherProfil(token.token);

                            } else {
                                afficherErreurConnexion(response);
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<TokenReponse> call,
                                Throwable t) {

                            resultat.setText(
                                    "Erreur réseau : " + t.getMessage()
                            );
                        }
                    }
            );
        });
    }

    private void afficherProfil(String jeton) {

        ClientApi.get().monProfil("Bearer " + jeton).enqueue(
                new Callback<Profil>() {

                    @Override
                    public void onResponse(
                            Call<Profil> call,
                            Response<Profil> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Profil profil = response.body();

                            resultat.setText(
                                    "Connexion réussie !\n\n"
                                            + "Nom : " + profil.nom + "\n"
                                            + "Rôle : " + profil.role + "\n"
                                            + "Courriel : " + profil.courriel + "\n"
                                            + "Code HTTP : " + response.code()
                            );

                        } else {
                            resultat.setText(
                                    "Erreur profil HTTP : " + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Profil> call,
                            Throwable t) {

                        resultat.setText(
                                "Erreur réseau profil : " + t.getMessage()
                        );
                    }
                }
        );
    }

    private void afficherErreurConnexion(
            Response<TokenReponse> response) {

        String message = "Erreur de connexion";

        try {
            if (response.errorBody() != null) {
                String contenu = response.errorBody().string();

                JsonObject json =
                        JsonParser.parseString(contenu).getAsJsonObject();

                if (json.has("message")) {
                    message = json.get("message").getAsString();
                }
            }
        } catch (Exception e) {
            message = "Impossible de lire le message d'erreur";
        }

        resultat.setText(
                "Erreur " + response.code() + " : " + message
        );
    }
}

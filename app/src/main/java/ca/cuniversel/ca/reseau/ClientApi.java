package ca.cuniversel.ca.reseau;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientApi {
    private static LocaMobileApi instance;

    public static LocaMobileApi get() {
        if (instance == null){

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.2.27:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            instance = retrofit.create(LocaMobileApi.class);
        }

        return instance;
    }
}

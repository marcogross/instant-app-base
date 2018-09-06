/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.instantapps.samples.hello.feature;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This Activity displays a simple hello world text and a button to open the GoodbyeActivity.
 */
public class HelloActivity extends AppCompatActivity {

    private ImageView imagem;
    private Button galeria, btnFoto;
    private final int GALERIA_IMAGENS = 1;
    private final int PERMISSAO_REQUEST = 2;
    private final int TIRAR_FOTO = 3;

    //Elementos para buscar dados externos de API
    private Button botaoRecuperar;
    private TextView textoResultado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        //Elementos para buscar dados externos de API
        botaoRecuperar = findViewById(R.id.buttonRecuperar);
        textoResultado = findViewById(R.id.textResultado);

        botaoRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyTask task = new MyTask();
                String urlApi = "https://blockchain.info/ticker";
                String urlCep = "https://viacep.com.br/ws/41320250/json";
                //task.execute(urlApi);
                task.execute(urlCep);

            }
        });


        //PERMISSÃO PARA USO DA GALERIA DE IMAGENS -- Não disponível em instant apps
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSAO_REQUEST);
            }
        }
        */

        //ELEMENTOS DA TELA
        imagem = findViewById(R.id.ivImagem);
        galeria = findViewById(R.id.btnImagem);
        btnFoto = findViewById(R.id.btnFoto);

        //AÇÃO DE CLICLAR NO BOTÃO galeria CRIA UMA INTENT PARA USAR A GALERIA DE IMAGENS
        galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //INTENÇÃO DE USAR GALERIA DE IMAGENS
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALERIA_IMAGENS);
            }
        });

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, TIRAR_FOTO);
                }

            }
        });
    }

    //RECEBENDO O RESULTADO DAS INTENTS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //RECEBENDO O RESULTADO DA INTENT QUE ACESSA A GALERIA E USANDO A IMAGEM SELECIONADA EM NOSSA imageView
        if (resultCode == RESULT_OK && requestCode == GALERIA_IMAGENS) {
            Uri selectedImage = data.getData();
            String[] filePath = { MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            Bitmap imagemGaleria = (BitmapFactory.decodeFile(picturePath));
            imagem.setImageBitmap(imagemGaleria);

        }

        //RECEBENDO O RESULTADO DA INTENT QUE ACESSA A CÂMERA E USANDO A NOVA FOTO TIRADA EM NOSSA imageView
        if (requestCode == TIRAR_FOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imagem.setImageBitmap(imageBitmap);
        }
    }


    //Pegando dados de nossa API externa
    class MyTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String stringUrl = strings[0];
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;
            StringBuffer buffer = null;

            try {
                URL url = new URL(stringUrl);
                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();

                //O inputStream recupera os dados em bytes
                inputStream = conexao.getInputStream();

                //inputStreamReader lê os dados em bytes e os decodifica para caracteres
                inputStreamReader = new InputStreamReader(inputStream);

                //Objeto utilizado para leitura dos caracteres do inputStreamReader
                reader = new BufferedReader(inputStreamReader);
                buffer = new StringBuffer();
                String linha = "";

                while ( (linha = reader.readLine()) != null ) {
                    buffer.append(linha);

                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return buffer.toString();
        }


        @Override
        protected void onPostExecute(String resultado) {
            super.onPostExecute(resultado);

            //cep, logradouro, complemento, bairro, localidade, uf, unidade, ibge = null;
            String cep = null;
            String logradouro = null;
            String complemento = null;
            String bairro = null;
            String localidade = null;
            String uf = null;
            String unidade = null;
            String ibge = null;

            //Transformando o valor de resultado em um objeto JSON para que possamos manipulár-lo:
            try {
                JSONObject jsonObject = new JSONObject(resultado);
                cep = jsonObject.getString("cep");
                logradouro = jsonObject.getString("logradouro");
                complemento = jsonObject.getString("complemento");
                bairro = jsonObject.getString("bairro");
                localidade = jsonObject.getString("localidade");
                uf = jsonObject.getString("uf");
                unidade = jsonObject.getString("unidade");
                ibge = jsonObject.getString("ibge");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            textoResultado.setText(logradouro + ", " + bairro + ", " + complemento);
        }
    }

}

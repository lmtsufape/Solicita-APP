package com.solicita.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.solicita.helper.MaskCustom;
import com.solicita.helper.ValidacaoCPF;
import com.solicita.R;
import com.google.android.material.textfield.TextInputEditText;
import com.solicita.model.Curso;
import com.solicita.model.Unidade;
import com.solicita.network.ApiClient;
import com.solicita.network.ApiInterface;
import com.solicita.network.response.DefaultResponse;
import com.solicita.network.response.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.layout.simple_spinner_item;

public class CadastrarDiscenteActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    TextInputEditText campoNome, campoCPF, campoEmail, campoSenha, campoConfirmarSenha;
    Spinner spinnerVinculo, spinnerUnidade, spinnerCurso;
    Button buttonCadastro;
    CheckBox checkBox;
    ApiInterface apiInterface;
    Call<UserResponse> call;
    Context mContext;
    ArrayList<Curso> cursoArrayList;
    ArrayList<Unidade> unidadeArrayList;
    ArrayList<String> cursos = new ArrayList<>();
    ArrayList<String> unidade = new ArrayList<>();
    private int index;
    String idUnidade;
    String idCurso;
    GoogleApiClient googleApiClient;

    String SiteKey = "6LcgPvsUAAAAADb-PsgvX4Q7WJQQvtM1mLE6njKR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_discente);

        mContext = this;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        inicializarComponentes();
        buscarJSON();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(CadastrarDiscenteActivity.this)
                .build();
        googleApiClient.connect();

        buttonCadastro.setOnClickListener(v -> cadastrar());

        checkBox.setOnClickListener(v -> {
            if (checkBox.isChecked()) {
                SafetyNet.SafetyNetApi.verifyWithRecaptcha(googleApiClient, SiteKey).setResultCallback(recaptchaTokenResult -> {
                    Status status = recaptchaTokenResult.getStatus();
                    if ((status != null) && status.isSuccess()) {
                        Toast.makeText(getApplicationContext(), "Verificado com sucesso!", Toast.LENGTH_LONG).show();
                        int textoCheck = 0xFF0B7D2D;
                        checkBox.setTextColor(textoCheck);
                        buttonCadastro.setEnabled(true);
                        int azulBotao = 0xFF1B2E4F;
                        buttonCadastro.setBackgroundColor(azulBotao);
                    }
                });
            } else {
                checkBox.setTextColor(Color.BLACK);
                buttonCadastro.setEnabled(false);
                int azulBotaoDesativado = 0x9F1B2E4F;
                buttonCadastro.setBackgroundColor(azulBotaoDesativado);
            }

        });
    }

    private void buscarJSON() {

        Call<String> callCurso = apiInterface.getCursoJSONString();
  //      Call<String> callUnidade = apiInterface.getUnidadeJSONString();

        callCurso.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        spinnerCursoJSON(jsonResponse);
                       // spinnerUnidadeJSON(jsonResponse);

                    } else {
                        Log.i("onEmptyResponse", "Empty");
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println("Erroo");
            }
        });

  /*      callUnidade.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body();
                        //  spinnerCursoJSON(jsonResponse);
                        spinnerUnidadeJSON(jsonResponse);

                    } else {
                        Log.i("onEmptyResponse", "Empty");
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println("Erro");
            }
        });*/

    }

    public void spinnerCursoJSON(String response) {
        try {
            JSONObject object = new JSONObject(response);
            cursoArrayList = new ArrayList<>();
            JSONArray jsonArray = object.getJSONArray("cursos");

            for (int i = 0; i < jsonArray.length(); i++) {

                Curso curso = new Curso();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                curso.setNome(jsonObject.getString("nome"));

                cursoArrayList.add(curso);

            }
            for (int i = 0; i < cursoArrayList.size(); i++) {
                cursos.add(cursoArrayList.get(i).getNome());

                //      Log.d("this is my array", "arr: " + Arrays.toString(new ArrayList[]{cursos}));
                //      System.out.println("arr: "+ Arrays.toString(new ArrayList[]{cursos}));
            }

            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(CadastrarDiscenteActivity.this, simple_spinner_item, cursos);
            stringArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCurso.setAdapter(stringArrayAdapter);

            spinnerCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    index = parent.getSelectedItemPosition();
                    index++;
                    idCurso = String.valueOf(index);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

 /*   public void spinnerUnidadeJSON(String response) {
        try {
            JSONObject object = new JSONObject(response);
            unidadeArrayList = new ArrayList<>();
            JSONArray jsonArray = object.getJSONArray("unidade");

            for (int i = 0; i < jsonArray.length(); i++) {

                Unidade unidade = new Unidade();

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                unidade.setNome(jsonObject.getString("nome"));

                unidadeArrayList.add(unidade);

                //           Log.d("this is my array", "arr: " + Arrays.toString(new ArrayList[]{cursoArrayList}));
                //         System.out.println("arr: "+ Arrays.toString(new ArrayList[]{cursoArrayList}));
            }
            for (int i = 0; i < unidadeArrayList.size(); i++) {
                unidade.add(unidadeArrayList.get(i).getNome());

                //              Log.d("this is my array", "arr: " + Arrays.toString(new ArrayList[]{unidade}));
                //      System.out.println("arr: "+ Arrays.toString(new ArrayList[]{cursos}));

            }
            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(CadastrarDiscenteActivity.this, simple_spinner_item, unidade);
            stringArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUnidade.setAdapter(stringArrayAdapter);

            spinnerUnidade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    index = parent.getSelectedItemPosition();
                    index++;
                    idUnidade = String.valueOf(index);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    public void cadastrar() {

        String name = campoNome.getText().toString();
        String cpf = campoCPF.getText().toString();
        String vinculo = spinnerVinculo.getSelectedItem().toString();

        if (vinculo.equals("Matriculado")) {
            vinculo = "1";
        } else if (vinculo.equals("Egresso")) {
            vinculo = "2";
        } else if (vinculo.equals("Especial")) {
            vinculo = "3";
        } else if (vinculo.equals("REMT - Regime Especial de Movimentação Temporária")) {
            vinculo = "4";
        } else if (vinculo.equals("Desistente")) {
            vinculo = "5";
        } else if (vinculo.equals("Matrícula Trancada")) {
            vinculo = "6";
        } else {
            vinculo = "7";
        }

        String email = campoEmail.getText().toString();
        String password = campoSenha.getText().toString();
        String confirm_password = campoConfirmarSenha.getText().toString();

        spinnerVinculo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ValidacaoCPF validacaoCPF = new ValidacaoCPF();

        if (!name.isEmpty()) {//verifica nome
            if (validacaoCPF.isCPF(cpf)) {//verifica cpf valido
                if (!cpf.isEmpty()) {//verifica campo cpf
                    if (!vinculo.isEmpty()) {//verifica vinculo
                        if (!idUnidade.isEmpty()) {//verifica unidade academica
                            if (!idCurso.isEmpty()) {//verifica cursos
                                if (!email.isEmpty()) {//verifica e-mail
                                    if (!password.isEmpty()) {//verifica senha
                                        if (!confirm_password.isEmpty()) {//verifica confirmacao de senha
                                            if (password.equals(confirm_password)) {

                                                call = apiInterface.postCadastro(name, cpf, vinculo, idUnidade, idCurso, email, password);
                                                call.enqueue(new Callback<UserResponse>() {
                                                    @Override
                                                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                                                        if (response.isSuccessful()) {
                                                            if (response.code() == 201) {
                                                                DefaultResponse dr = response.body();
                                                                Toast.makeText(CadastrarDiscenteActivity.this, dr.getMessage(), Toast.LENGTH_LONG).show();
                                                                System.out.println(dr.getMessage());
                                                                startActivity(new Intent(CadastrarDiscenteActivity.this, LoginActivity.class));
                                                                finish();
                                                            }else if (response.code()==500){
                                                                Toast.makeText(CadastrarDiscenteActivity.this, "Erro ao realizar cadastro! Verifique os dados e tente novamente.", Toast.LENGTH_LONG).show();
                                                            }
                                                        }else{
                                                            Toast.makeText(CadastrarDiscenteActivity.this, "Erro ao realizar cadastro! Verifique os dados e tente novamente.", Toast.LENGTH_LONG).show();

                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<UserResponse> call, Throwable t) {
                                                        System.out.println("onFailure");
                                                        Toast.makeText(CadastrarDiscenteActivity.this, "Erro ao realizar cadastro! Verifique os dados e tente novamente.", Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(CadastrarDiscenteActivity.this, CadastrarDiscenteActivity.class));

                                                    }
                                                });


                                            } else {
                                                Toast.makeText(CadastrarDiscenteActivity.this, "As senhas devem ser iguais", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(CadastrarDiscenteActivity.this, "Confirme a senha", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(CadastrarDiscenteActivity.this, "Informe a senha", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(CadastrarDiscenteActivity.this, "Preencha o campo e-mail", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CadastrarDiscenteActivity.this, "Selecione o cursos", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CadastrarDiscenteActivity.this, "Selecione a unidade acadêmica", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CadastrarDiscenteActivity.this, "Selecione o tipo de vínculo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastrarDiscenteActivity.this, "Preecha o campo CPF.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CadastrarDiscenteActivity.this, "O CPF informado é inválido", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CadastrarDiscenteActivity.this, "Preencha o campo nome", Toast.LENGTH_SHORT).show();
        }
    }

    public void abrirTelaLogin(View view) {
        Intent abrirTelaLogin = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(abrirTelaLogin);
    }

    public void inicializarComponentes() {

        campoNome = findViewById(R.id.textProtNome);
        campoCPF = findViewById(R.id.textInfoCPF);
        campoCPF.addTextChangedListener(MaskCustom.insert(MaskCustom.CPF_MASK, campoCPF));
        spinnerVinculo = (findViewById(R.id.spinner));
        spinnerUnidade = findViewById(R.id.spinnerUnidade);
        spinnerCurso = findViewById(R.id.spinnerCurso);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
        campoConfirmarSenha = findViewById(R.id.editConfirmarSenha);
        buttonCadastro = findViewById(R.id.btCadastrar);
        checkBox = findViewById(R.id.checkBox);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //  Toast.makeText(this, "onConnected()", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Toast.makeText(this,"onConnectionSuspended: " + i, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //    Toast.makeText(this, "onConnectionFailed():\n" + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }
}
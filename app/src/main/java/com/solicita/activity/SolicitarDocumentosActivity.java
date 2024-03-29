package com.solicita.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.solicita.R;
import com.solicita.activity.ufape.MainActivityUfape;
import com.solicita.helper.SharedPrefManager;
import com.solicita.model.Documento;
import com.solicita.model.Perfil;
import com.solicita.model.Requisicao;
import com.solicita.network.ApiClient;
import com.solicita.network.ApiInterface;
import com.solicita.network.response.DefaultResponse;
import com.solicita.network.response.SolicitacaoResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.layout.simple_spinner_item;

public class SolicitarDocumentosActivity extends AppCompatActivity {

    private Spinner spinnerPerfil;

    private Button buttonSolicitar;
    private ApiInterface apiInterface;

    private ArrayList<Perfil> perfilArrayList;
    private ArrayList<String> perfil = new ArrayList<>();
    private ArrayList<String> perfilId = new ArrayList<>();

    private ArrayList<Documento> documentoArrayList;
    private ArrayList<Documento> documentoDetalhesArrayList;

    private ArrayList<String> documento = new ArrayList<>();
    private ArrayList<String> documentoDetalhes = new ArrayList<>();

    private ArrayList<String> solicitados = new ArrayList<>();

    private LinearLayout linearLayout;
    private CheckBox checkBox;

    private SharedPrefManager sharedPrefManager;

    private TextView textNomeUsuario;

    private Button buttonLogout, buttonHome, buttonCancelar;

    private int index;
    private String idPerfil = "";

    private Context context;

    private ProgressDialog progressDialog;

    private String cursoP, situacaoP, dataP, horaP;
    private String declaracaoVinculo = "", comprovanteMatricula = "", historico = "", programaDisciplina = "", outros = "", requisicaoPrograma = "", requisicaoOutros = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_documentos);

        sharedPrefManager = new SharedPrefManager(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        context = this;

        inicializarComponentes();

        textNomeUsuario.setText(sharedPrefManager.getSPNome());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Carregando...");
        progressDialog.setCancelable(false);

        buscarJSON();

        buttonHome.setOnClickListener(v -> irHome());

        buttonLogout.setOnClickListener(v -> logoutApp());

        buttonSolicitar.setOnClickListener(v -> {
            if (declaracaoVinculo != "1" && comprovanteMatricula != "1" && historico != "1" && programaDisciplina != "1" && outros != "1") {
                Toast.makeText(getApplicationContext(), "Selecione pelo menos um documento.", Toast.LENGTH_LONG).show();
            } else if ((programaDisciplina.equals("1") && requisicaoPrograma.equals("")) || ((outros.equals("1") && requisicaoOutros.equals("")))) {
                Toast.makeText(getApplicationContext(), "Preencha o campo com as informações relativas à disciplina e a finalidade do pedido.", Toast.LENGTH_LONG).show();
            } else if ((programaDisciplina.equals("1") && requisicaoPrograma.length() > 190) || (outros.equals("1") && requisicaoOutros.length() > 190)) {
                Toast.makeText(getApplicationContext(), "O campo só pode ter no máximo 190 caracteres.", Toast.LENGTH_LONG).show();
            } else {
                finalizarSolicitacao();
            }
        });

        buttonCancelar.setOnClickListener(v -> irHome());
    }

    private void finalizarSolicitacao() {

        String defaultt = idPerfil;

        Call<SolicitacaoResponse> solicitacaoResponseCall = apiInterface.postSolicitacao(defaultt, declaracaoVinculo, comprovanteMatricula, historico, programaDisciplina, outros, requisicaoPrograma, requisicaoOutros, sharedPrefManager.getSPToken());

        solicitacaoResponseCall.enqueue(new Callback<SolicitacaoResponse>() {

            @Override
            public void onResponse(Call<SolicitacaoResponse> call, Response<SolicitacaoResponse> response) {

                if (response.code() == 200) {

                    Perfil perfil = response.body().getPerfil();
                    Requisicao requisicao = response.body().getRequisicao();

                    cursoP = perfil.getCurso();
                    situacaoP = perfil.getSituacao();

                    dataP = requisicao.getData_pedido();
                    horaP = requisicao.getHora_pedido();

                    Intent abrirProtocolo = new Intent(getApplicationContext(), ConfirmacaoRequisicaoActivity.class);

                    abrirProtocolo.putExtra("curso", cursoP);
                    abrirProtocolo.putExtra("situacao", situacaoP);
                    abrirProtocolo.putExtra("data", dataP);
                    abrirProtocolo.putExtra("hora", horaP);
                    abrirProtocolo.putExtra("solicitados", solicitados);

                    System.out.println(solicitados);

                    startActivity(abrirProtocolo);

                } else {

                    Toast.makeText(getApplicationContext(), "Falha na comunicação com o servidor.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SolicitarDocumentosActivity.this, LoginActivity.class));

                }
            }

            @Override
            public void onFailure(Call<SolicitacaoResponse> call, Throwable t) {

            }
        });
    }

    private void buscarJSON() {

        progressDialog.show();

        Call<String> getUserPerfil = apiInterface.getUserPerfil(sharedPrefManager.getSPToken());
        getUserPerfil.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.code() == 200) {

                    String jsonResponse = response.body();
                    spinnerPerfilJSON(jsonResponse);

                    Call<String> callDocumento = apiInterface.getDocumentoJSONString();
                    callDocumento.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            progressDialog.dismiss();
                            if (response.isSuccessful()) {
                                String jsonResponse = response.body();
                                checkboxDocumentos(jsonResponse);
                            } else {

                                Toast.makeText(getApplicationContext(), "Falha na comunicação com o servidor.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(SolicitarDocumentosActivity.this, LoginActivity.class));

                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            System.out.println("onResponse");
                        }
                    });

                } else {

                    Toast.makeText(getApplicationContext(), "Falha na comunicação com o servidor.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SolicitarDocumentosActivity.this, LoginActivity.class));

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println("Falha getUserPerfil");
            }
        });
    }

    private void checkboxDocumentos(String response) {
        try {
            JSONObject object = new JSONObject(response);
            documentoArrayList = new ArrayList<>();
            documentoDetalhesArrayList = new ArrayList<>();
            JSONArray jsonArray = object.getJSONArray("documentos");

            for (int i = 0; i < jsonArray.length(); i++) {

                Documento documento = new Documento();
                Documento documentoDetalhes = new Documento();
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                documento.setTipo(jsonObject.getString("tipo"));
                documentoDetalhes.setDetalhes(jsonObject.getString("detalhes"));
                documentoArrayList.add(documento);
                documentoDetalhesArrayList.add(documentoDetalhes);
            }
            for (int i = 0; i < documentoArrayList.size(); i++) {
                documento.add(documentoArrayList.get(i).getTipo());
            }
            for (int i = 0; i < documentoDetalhesArrayList.size(); i++) {
                documentoDetalhes.add(documentoDetalhesArrayList.get(i).getDetalhes());
            }
            for (int i = 0; i < documento.size(); i++) {
                checkBox = new CheckBox(this);
                checkBox.setId(i);
                checkBox.setText(documento.get(i));
                linearLayout.addView(checkBox);

                EditText editText = new EditText(context);

                if (documentoDetalhesArrayList.get(i).getDetalhes().equals("true")) {
                    editText.setTextSize(18);
                    editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    editText.setVisibility(View.GONE);
                    linearLayout.addView(editText);
                }
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                    int valor = buttonView.getId();

                    if (isChecked && valor == 0) {
                        declaracaoVinculo = "1";
                        solicitados.add(buttonView.getText().toString());
                        System.out.println(solicitados);
                    } else if (isChecked && valor == 1) {
                        comprovanteMatricula = "1";
                        solicitados.add(buttonView.getText().toString());
                        System.out.println(solicitados);
                    } else if (isChecked && valor == 2) {
                        historico = "1";
                        solicitados.add(buttonView.getText().toString());
                        System.out.println(solicitados);
                    } else if (isChecked && valor == 3) {
                        solicitados.add(buttonView.getText().toString());
                        System.out.println(solicitados);
                        programaDisciplina = "1";
                        editText.setVisibility(View.VISIBLE);
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                requisicaoPrograma = editText.getText().toString();
                                System.out.println("Campo programa de disciplina: " + requisicaoPrograma);
                                if (requisicaoPrograma.isEmpty()) {
                                    editText.setError("Preencha este campo com as informações relativas à disciplina e a finalidade do pedido.");
                                    editText.requestFocus();
                                    return;
                                }
                                System.out.println("Programa de disciplina: " + requisicaoPrograma);
                            }
                        });
                        if (requisicaoPrograma.isEmpty()) {
                            editText.setError("Preencha este campo com as informações relativas à disciplina e a finalidade do pedido.");
                            editText.requestFocus();
                            return;
                        }

                    } else if (isChecked && valor == 4) {
                        solicitados.add(buttonView.getText().toString());
                        System.out.println(solicitados);
                        outros = "1";
                        editText.setVisibility(View.VISIBLE);
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                requisicaoOutros = editText.getText().toString();
                                System.out.println("Campo outros: " + requisicaoOutros);
                                if (requisicaoOutros.isEmpty()) {
                                    editText.setError("Preencha este campo com as informações referentes ao seu pedido.");
                                    editText.requestFocus();
                                    return;
                                }
                            }
                        });
                        if (requisicaoOutros.isEmpty()) {
                            editText.setError("Preencha este campo com as informações referentes ao seu pedido.");
                            editText.requestFocus();
                            return;
                        }
                    } else {
                        if (!isChecked && valor == 0) {
                            declaracaoVinculo = "";
                            solicitados.remove(buttonView.getText().toString());
                            System.out.println(solicitados);
                        }
                        if (!isChecked && valor == 1) {
                            comprovanteMatricula = "";
                            solicitados.remove(buttonView.getText().toString());
                            System.out.println(solicitados);
                        }
                        if (!isChecked && valor == 2) {
                            historico = "";
                            solicitados.remove(buttonView.getText().toString());
                            System.out.println(solicitados);
                        }
                        if (!isChecked && valor == 3) {
                            editText.setVisibility(View.GONE);
                            programaDisciplina = "";
                            solicitados.remove(buttonView.getText().toString());
                            System.out.println(solicitados);
                        }
                        if (!isChecked && valor == 4) {
                            editText.setVisibility(View.GONE);
                            outros = "";
                            solicitados.remove(buttonView.getText().toString());
                            System.out.println(solicitados);

                        }
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void spinnerPerfilJSON(String response) {

        try {
            JSONObject object = new JSONObject(response);
            perfilArrayList = new ArrayList<>();
            JSONArray jsonArray = object.getJSONArray("perfil");

            for (int i = 0; i < jsonArray.length(); i++) {
                Perfil perfil = new Perfil();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                perfil.setCurso(jsonObject.getString("default"));
                perfil.setSituacao(jsonObject.getString("situacao"));
                perfil.setId(jsonObject.getString("id"));

                perfilArrayList.add(perfil);
            }
            for (int i = 0; i < perfilArrayList.size(); i++) {
                perfil.add(perfilArrayList.get(i).getCurso() + " - " + perfilArrayList.get(i).getSituacao());

            }
            for (int i = 0; i < perfilArrayList.size(); i++) {
                perfilId.add(perfilArrayList.get(i).getId());
                System.out.println("ID do perfil: " + perfilId);
            }

            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(SolicitarDocumentosActivity.this, simple_spinner_item, perfil);
            stringArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPerfil.setAdapter(stringArrayAdapter);

            spinnerPerfil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    index = parent.getSelectedItemPosition();
                    idPerfil = perfilArrayList.get(index).getId();

                    System.out.println("ID Selecionado: " + idPerfil);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void logoutApp() {

        Call<DefaultResponse> responseCall = apiInterface.postLogout(sharedPrefManager.getSPToken());

        responseCall.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                DefaultResponse dr = response.body();
                Toast.makeText(SolicitarDocumentosActivity.this, dr.getMessage(), Toast.LENGTH_SHORT).show();
                sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_STATUS_LOGIN, false);
                startActivity(new Intent(SolicitarDocumentosActivity.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                clickBotaoHomeUfape();
                finish();
            }

            @Override
            public void onFailure(Call<DefaultResponse> call, Throwable t) {

            }
        });
    }

    private void irHome() {
        startActivity(new Intent(SolicitarDocumentosActivity.this, HomeAlunoActivity.class));

    }

    private void clickBotaoHomeUfape() {

        startActivity(new Intent(this, MainActivityUfape.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    public void clickBotaoHomeUfape(View view) {

        startActivity(new Intent(this, MainActivityUfape.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private void inicializarComponentes() {

        spinnerPerfil = findViewById(R.id.spinnerPerfil);
        linearLayout = findViewById(R.id.linear_docs);
        buttonSolicitar = findViewById(R.id.buttonSolicitar);
        textNomeUsuario = findViewById(R.id.textNomeUsuario);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonHome = findViewById(R.id.buttonHome);
        buttonCancelar = findViewById(R.id.buttonCancelar);

    }
}
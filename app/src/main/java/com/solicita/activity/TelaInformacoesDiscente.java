package com.solicita.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.solicita.R;
import com.solicita.helper.SharedPrefManager;
import com.solicita.model.Aluno;
import com.solicita.model.Perfil;
import com.solicita.model.Unidade;
import com.solicita.model.User;
import com.solicita.network.ApiClient;
import com.solicita.network.ApiInterface;
import com.solicita.network.response.DefaultResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelaInformacoesDiscente extends AppCompatActivity {

    public TextView textInfoNome, textInfoCPF, textInfoVinculo, textInfoUnidadeAcademica, textInfoCurso, textInfoEmail;

    ApiInterface apiInterface;
    SharedPrefManager sharedPrefManager;
    Context context;

    ArrayList<Perfil> listarPerfilArrayList;
    ArrayList<Unidade> listarUnidadesArrayList;
    ArrayList<Aluno> listarAlunoArrayList;
    ArrayList<User> listarUserArrayList;

    LinearLayout linearLayout;
    RadioGroup radioGroup;

    ArrayList<Perfil> perfilArrayList;
    ArrayList<String> perfil = new ArrayList<>();

    Button buttonExcluirPerfil;

    String idPerfil = "";
    String mensagemExcluir = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_informacoes_discente);

        sharedPrefManager = new SharedPrefManager(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        context = this;

        //inicializar componentes
        inicializarComponentes();
        buscarPerfisJSON();
        buscarInfoJSON();

        buttonExcluirPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                excluirPerfil();
            }
        });


    }

    public void radioGroupJSON(String response) {
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
                ///////           System.out.println("Perfis: "+ perfilArrayList.get(i).getCurso()+ " - "+ perfilArrayList.get(i).getSituacao());
            }

            radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.VERTICAL);


            for (int i = 0; i < perfil.size(); i++) {

                RadioGroup.LayoutParams rl2;
                //radioGroup.setId(i);
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(perfil.get(i));

                rl2 = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.MATCH_PARENT);
                radioGroup.addView(radioButton, rl2);

            }
            linearLayout.addView(radioGroup);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton radioButton = findViewById(checkedId);
                    Toast.makeText(getApplicationContext(), radioButton.getText(), Toast.LENGTH_LONG).show();
                    System.out.println(radioGroup.getCheckedRadioButtonId());
                    int valor = radioGroup.getCheckedRadioButtonId();

                    for (int i = 0; i < perfilArrayList.size(); i++) {
                        if (radioButton.getText().equals(perfilArrayList.get(i).getCurso() + " - " + perfilArrayList.get(i).getSituacao())) {
                            System.out.println("Valor do ID: " + perfilArrayList.get(i).getId());
                            idPerfil = perfilArrayList.get(i).getId();
                        }

                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void buscarPerfisJSON() {

        Call<String> stringCall = apiInterface.getUserPerfil(sharedPrefManager.getSPToken());
        stringCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.code() == 200) {
                    String jsonResponse = response.body();
                    radioGroupJSON(jsonResponse);

                } else {

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    public void buscarInfoJSON() {

        Call<String> perfilCall = apiInterface.getPerfilInfoJSONString(sharedPrefManager.getSPToken());
        perfilCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.code() == 200) {

                    String jsonResponse = response.body();
                    buscarInfo(jsonResponse);
/*                    Aluno aluno = response.body().getAluno();
                    User user = response.body().getUser();
                     Unidade unidade = response.body().getUnidade();

                    String nome = user.getName();
                    textInfoNome.setText(nome);
                    String cpf = aluno.getCpf();
                    textInfoCPF.setText(cpf);
                    String vinculo = perfil.getSituacao();
                    textInfoVinculo.setText(vinculo);
                    String curso = perfil.getCurso();
                    textInfoCurso.setText(curso);
                    String email = user.getEmail();
                    textInfoEmail.setText(email);

                    String unidades = unidade.getInstituicao_id();
                    textInfoUnidadeAcademica.setText(unidades);*/


                } else {

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

 /*       Call<PerfilResponse> perfilResponseCall = apiInterface.getPerfilInfoJSONString(sharedPrefManager.getSPToken());

        perfilResponseCall.enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (response.code()==200){

                    Perfil perfil = response.body().getPerfil();
                    Aluno aluno = response.body().getAluno();
                    User user = response.body().getUser();
                   // Unidade unidade = response.body().getUnidade();

                    String nome = user.getName();
                    textInfoNome.setText(nome);
                    String cpf = aluno.getCpf();
                    textInfoCPF.setText(cpf);
                    String vinculo = perfil.getSituacao();
                    textInfoVinculo.setText(vinculo);
                    String curso = perfil.getCurso();
                    textInfoCurso.setText(curso);
                    String email = user.getEmail();
                    textInfoEmail.setText(email);

                  //  String unidades = unidade.getInstituicao_id();
                    //textInfoUnidadeAcademica.setText(unidades);


                }else{

                }
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {

            }
        });*/
    }

    public void buscarInfo(String response) {
        try {
            JSONObject object = new JSONObject(response);

            listarPerfilArrayList = new ArrayList<>();
            listarUnidadesArrayList = new ArrayList<>();
            listarAlunoArrayList = new ArrayList<>();
            listarUserArrayList = new ArrayList<>();

            JSONArray jsonArrayPerfil = object.getJSONArray("perfil");
            JSONArray jsonArrayUnidades = object.getJSONArray("unidades");
            JSONArray jsonArrayAluno = object.getJSONArray("aluno");
            JSONArray jsonArrayUser = object.getJSONArray("user");

            for (int i = 0; i < jsonArrayPerfil.length(); i++) {
                Perfil perfil = new Perfil();
                JSONObject jsonObject = jsonArrayPerfil.getJSONObject(i);
                perfil.setUnidade_id(jsonObject.getString("unidade_id"));
                perfil.setCurso(jsonObject.getString("default"));
                perfil.setSituacao(jsonObject.getString("situacao"));

                listarPerfilArrayList.add(perfil);

            }
            for (int i = 0; i < jsonArrayUnidades.length(); i++) {
                Unidade unidade = new Unidade();
                JSONObject jsonObject = jsonArrayUnidades.getJSONObject(i);
                unidade.setNome(jsonObject.getString("nome"));
                unidade.setInstituicao_id(jsonObject.getString("instituicao_id"));

                listarUnidadesArrayList.add(unidade);
            }

            for (int i = 0; i < jsonArrayAluno.length(); i++) {
                Aluno aluno = new Aluno();
                JSONObject jsonObject = jsonArrayAluno.getJSONObject(i);
                aluno.setCpf(jsonObject.getString("cpf"));

                listarAlunoArrayList.add(aluno);
            }
            for (int i = 0; i < jsonArrayUser.length(); i++) {
                User user = new User();
                JSONObject jsonObject = jsonArrayUser.getJSONObject(i);
                user.setName(jsonObject.getString("name"));
                user.setEmail(jsonObject.getString("email"));

                listarUserArrayList.add(user);

            }

            for (int i = 0; i < jsonArrayPerfil.length(); i++) {
                for (int j = 0; j < jsonArrayUnidades.length(); j++) {
                    for (int k = 0; k < jsonArrayAluno.length(); k++) {
                        for (int l = 0; l < jsonArrayUser.length(); l++) {
                            if (listarPerfilArrayList.get(i).getUnidade_id().equals(listarUnidadesArrayList.get(j).getInstituicao_id())) {
                                textInfoUnidadeAcademica.setText(listarUnidadesArrayList.get(j).getNome());
                                textInfoCurso.setText(listarPerfilArrayList.get(i).getCurso());
                                textInfoVinculo.setText(listarPerfilArrayList.get(i).getSituacao());
                                textInfoCPF.setText(listarAlunoArrayList.get(k).getCpf());
                                textInfoNome.setText(listarUserArrayList.get(l).getName());
                                textInfoEmail.setText(listarUserArrayList.get(l).getEmail());
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void inicializarComponentes() {

        textInfoNome = findViewById(R.id.textProtNome);
        textInfoCPF = findViewById(R.id.textInfoCPF);
        textInfoVinculo = findViewById(R.id.textProtVinculo);
        textInfoUnidadeAcademica = findViewById(R.id.textInfoUnidadeAcademica);
        textInfoCurso = findViewById(R.id.textCursoAdap);
        textInfoEmail = findViewById(R.id.textInfoEmail);
        linearLayout = findViewById(R.id.linearLayout);
        buttonExcluirPerfil = findViewById(R.id.buttonExcluirPerfil);

    }

    public void irTelaEditarPerfil(View view) {
        Intent irTelaEditarPerfil = new Intent(getApplicationContext(), TelaEditarPerfil.class);
        startActivity(irTelaEditarPerfil);
    }

    public void irTelaAlterarSenha(View view) {
        Intent irTelaAlterarSenha = new Intent(getApplicationContext(), TelaAlterarSenha.class);
        startActivity(irTelaAlterarSenha);
    }

    public void irTelaAdicionarPerfil(View view) {
        Intent irTelaAdicionarPerfil = new Intent(getApplicationContext(), TelaAdicionarPerfil.class);
        startActivity(irTelaAdicionarPerfil);
    }

    public void excluirPerfil() {

        if (idPerfil.equals("")) {
            Toast.makeText(getApplicationContext(), "Selecione um perfil.", Toast.LENGTH_LONG).show();
        } else {

            AlertDialog.Builder dialogExluirPerfil = new AlertDialog.Builder(this);

            dialogExluirPerfil.setTitle("Exclusão de Perfil Acadêmico");
            dialogExluirPerfil.setMessage("Deseja realmente excluir o perfil selecionado?");

            dialogExluirPerfil.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Call<DefaultResponse> callExcluir = apiInterface.postExcluirPerfil(idPerfil, sharedPrefManager.getSPToken());
                    callExcluir.enqueue(new Callback<DefaultResponse>() {
                        @Override
                        public void onResponse(Call<DefaultResponse> call, Response<DefaultResponse> response) {
                            if (response.code() == 200) {

                                DefaultResponse dr = response.body();
                                Toast.makeText(getApplicationContext(), dr.getMessage(), Toast.LENGTH_LONG).show();

                            } else {

                            }
                        }

                        @Override
                        public void onFailure(Call<DefaultResponse> call, Throwable t) {

                        }
                    });
                }
            });
            dialogExluirPerfil.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialogExluirPerfil.create();
            dialogExluirPerfil.show();

        }
    }
}























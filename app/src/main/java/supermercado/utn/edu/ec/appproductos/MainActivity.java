package supermercado.utn.edu.ec.appproductos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import supermercado.utn.edu.ec.appproductos.model.Producto;
import supermercado.utn.edu.ec.appproductos.service.TaskProductoWS;
import supermercado.utn.edu.ec.appproductos.util.Control;

public class MainActivity extends AppCompatActivity {

    private List<Producto> listaProductos;
    private ListView listViewProductos;
    private List<String> productosList;
    private ArrayAdapter<String> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String g="Debes realizar los siguientes pasos:\n" +
                "\n" +
                "Banca Móvil\n" +
                "\n" +
                "Entra en la aplicación.\n" +
                "Pulsa en Acceso clientes.\n" +
                "Haz click en Si ha olvidado su contraseña pulse aquí.\n" +
                "Cumplimenta los datos solicitados.\n" +
                "Haz clic en Aceptar, deberás introducir tu clave de firma y el código enviado mediante SMS y te enviaremos la nueva contraseña a tu móvil.\n" +
                "Banca Internet\n" +
                "\n" +
                "Entra en www.bancopichincha.es\n" +
                "Selecciona el enlace de Acceso clientes.\n" +
                "Pulsa en recordar clave.\n" +
                "Cumplimenta los datos solicitados.\n" +
                "Haz clic en Continuar, deberás introducir tu clave de firma y te enviaremos una nueva contraseña a tu móvil o correo electrónico (según lo que hayas cumplimentado).";

        listViewProductos = (ListView) findViewById(R.id.listProductos);
        listaProductos = new ArrayList<>();
        productosList = new ArrayList<>();
        adaptador = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, productosList);
        listViewProductos.setAdapter(adaptador);
        listViewProductos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ProductoActivity.class);
                Producto producto = listaProductos.get(position);
                intent.putExtra("producto", producto);
                startActivity(intent);
            }
        });

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            boolean reset = bundle.getBoolean("reset");
            if(reset){
                this.listarProductos();
            }
        }
    }

    private void llenarLista() {
        productosList.clear();
        for (Producto p : listaProductos) {
            productosList.add(p.getNombre());
        }
    }

    public void listarProductos(View view) {
        Snackbar msgInternet = Snackbar.make(view, "Revise la conexión a Internet", Snackbar.LENGTH_SHORT);
        if (Control.isOnline(this)) {
            this.listarProductos();
        } else {
            msgInternet.show();
        }
    }

    private void listarProductos(){
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Conectando con el servidor ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final TaskProductoWS tarea = new TaskProductoWS();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    tarea.execute();
                    while (tarea.getStatus() == AsyncTask.Status.RUNNING) {
                        if (tarea.getStatus() == AsyncTask.Status.FINISHED) {
                            progressDialog.dismiss();
                            break;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tarea.getResultado()) {
                                listaProductos.clear();
                                listaProductos.addAll(tarea.getListaProductos());
                                llenarLista();
                                adaptador.notifyDataSetChanged();
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Productos listados", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "No se pudo listar los productos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("Error en hilo", "" + e.getMessage());
                    Log.e("Class Exception", "" + e.getClass());
                }
            }
        }).start();
    }
}

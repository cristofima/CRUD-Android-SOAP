package supermercado.utn.edu.ec.appproductos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import supermercado.utn.edu.ec.appproductos.model.Producto;
import supermercado.utn.edu.ec.appproductos.service.TaskProductoWS;
import supermercado.utn.edu.ec.appproductos.util.Control;

public class ProductoActivity extends AppCompatActivity {

    private TextView lblCodigo, lblNombre, lblPrecio, lblStock;
    private EditText txtCantidad;
    private Producto producto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);
        producto = (Producto) getIntent().getExtras().getSerializable("producto");
        this.initControls();
    }

    private void initControls() {
        lblCodigo = (TextView) findViewById(R.id.lblCodigo);
        lblNombre = (TextView) findViewById(R.id.lblNombre);
        lblPrecio = (TextView) findViewById(R.id.lblPrecio);
        lblStock = (TextView) findViewById(R.id.lblStock);

        txtCantidad = (EditText) findViewById(R.id.txtCantidad);

        if (producto != null) {
            lblCodigo.setText(String.valueOf(producto.getCodigo()));
            lblNombre.setText(producto.getNombre());
            lblPrecio.setText("$ "+String.valueOf(producto.getPrecio()));
            lblStock.setText(String.valueOf(producto.getStock()));
        }
    }

    public void comprarProducto(View view) {
        if (Control.isOnline(this)) {
            comprarProducto();
        } else {
            Toast.makeText(getApplicationContext(), "Revise la conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void comprarProducto() {
        String texto = txtCantidad.getText().toString();
        if (texto == null || texto.equals("")) {
            Toast.makeText(getApplicationContext(), "Ingrese la cantidad", Toast.LENGTH_SHORT).show();
        }
        int cant = 0;
        try {
            cant = Integer.parseInt(texto);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return;
        }
        final int idProducto = producto.getId();
        final int cantidad = cant;
        if (Control.isOnline(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Desea comprar el producto?")
                    .setTitle("Advertencia")
                    .setCancelable(false)
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton("Continuar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    final TaskProductoWS tarea = new TaskProductoWS(idProducto, cantidad);
                                    new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                tarea.execute();
                                                while (tarea.getStatus() == AsyncTask.Status.RUNNING) {
                                                    if (tarea.getStatus() == AsyncTask.Status.FINISHED) {
                                                        break;
                                                    }
                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (tarea.getResultado()) {
                                                            finish();
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            intent.putExtra("reset", true);
                                                            startActivity(intent);
                                                            Toast.makeText(getApplicationContext(), "Producto comprado", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getApplicationContext(), tarea.getMensaje(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Log.e("Error en hilo", e.getMessage());
                                                Log.e("Class Exception", "" + e.getClass());
                                            }
                                        }
                                    }).start();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(getApplicationContext(), "Revise la conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }
}

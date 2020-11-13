package supermercado.utn.edu.ec.appproductos.service;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import supermercado.utn.edu.ec.appproductos.model.Producto;

public class TaskProductoWS extends AsyncTask<String, Integer, Boolean> {

    private final String NAMESPACE = "http://Services/"; // Espacio de nombres utilizado en nuestro servicio web
    /*
        Dirección URL para realizar la conexión con el servicio web.
        En la URL se sustituye el nombre de máquina localhost por su dirección IP equivalente,
        que en el caso de aplicaciones Android ejecutadas en el emulador se corresponde con la
        dirección 10.0.2.2, en vez de la clásica 127.0.0.1.
    */
    private final String URL = "http://192.168.11.3:8080/ServiceProductos/ServiceProductos?wsdl";

    private String METHOD_NAME; // Nombre del método web concreto que vamos a ejecutar
    private String SOAP_ACTION; // Equivalente al METHOD_NAME, pero en la notación definida por SOAP

    private Map<Integer, String> METHODS_NAME;

    private SoapObject request, resSoap;
    private SoapSerializationEnvelope envelope;

    private HttpTransportSE transporte;

    private final int COMPRAR=1, SELECT = 2; // Tipos de operaciones
    private int OPERACION = 2; // Operación por defecto

    private Integer idProducto;
    private Integer cantidad;

    private Vector<?> responseVector;

    private List<Producto> listaProductos;

    private boolean resultado;

    private String mensaje;

    /*
        Constructor para seleccionar los productos
    */
    public TaskProductoWS() {
        this(null, null);
    }

    public TaskProductoWS(Integer idProducto,Integer cantidad) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;

        listaProductos = new ArrayList<>();

        METHODS_NAME = new TreeMap<>();
        METHODS_NAME.put(SELECT, "listarProductos");
        METHODS_NAME.put(COMPRAR, "comprarProducto");

        if (idProducto == null) {
            OPERACION = SELECT;
            this.METHOD_NAME = METHODS_NAME.get(SELECT);
        } else if (idProducto != null && cantidad!=null) {
            OPERACION = COMPRAR;
            this.METHOD_NAME = METHODS_NAME.get(COMPRAR);
        }

        this.SOAP_ACTION = NAMESPACE + METHOD_NAME;

        responseVector = new Vector<>();

        request = new SoapObject(NAMESPACE, METHOD_NAME); // Crea la petición (request) al método 'METHOD_NAME'
        /*
			Crea el contenedor SOAP (envelope), e indica la versión de SOAP que se va a usar
		*/
        envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = false; // Indica si trate de un WS .NET

         /*
			Crea el objeto que se encargará de realizar la comunicación HTTP con el servidor,
			al que le pasa la URL de conexión al WS
		*/
        transporte = new HttpTransportSE(URL);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        envelope.setOutputSoapObject(request); // Asocia la petición

        switch (OPERACION) {
            case COMPRAR:
                resultado = comprarProducto();
                break;
            case SELECT:
                resultado = getListado();
                break;
            default:
                break;
        }
        return resultado;
    }

    private PropertyInfo buildPropertyInfo(String name, Object val) {
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.setName(name);
        propertyInfo.setValue(val);
        propertyInfo.setType(val.getClass());
        return propertyInfo;
    }

    private boolean comprarProducto() {
        request.addProperty(buildPropertyInfo("id", this.idProducto));
        request.addProperty(buildPropertyInfo("cantidad", this.cantidad.intValue()));
        envelope.setOutputSoapObject(request);
        try {
            transporte.call(SOAP_ACTION, envelope);
            SoapPrimitive resultado_xml = (SoapPrimitive) envelope.getResponse();

            String res = resultado_xml.toString();

            if (!Boolean.parseBoolean(res))
                return false;
        } catch (SocketTimeoutException t) {
            Log.e("SocketTimeoutException", t.getMessage());
            this.mensaje =  t.getMessage();
            return false;
        } catch (IOException i) {
            Log.e("IOException", i.getMessage());
            this.mensaje =  i.getMessage();
            return false;
        } catch (XmlPullParserException e) {
            Log.e("XmlPullParserException", e.getMessage());
            this.mensaje =  e.getMessage();
            return false;
        }catch (Exception e) {
            this.mensaje =  e.getMessage();
            return false;
        }
        return true;
    }

    private boolean getListado() {
        try {
            transporte.call(SOAP_ACTION, envelope);

            // Recibe la respuesta del WS
            if (envelope.getResponse() instanceof Vector) {
                responseVector = (Vector<?>) envelope.getResponse();
                return responseVector != null;
            } else if (envelope.getResponse() instanceof SoapObject) {
                resSoap = (SoapObject) envelope.getResponse();
                return resSoap != null;
            }
            return false;
        } catch (SocketTimeoutException t) {
            Log.e("SocketTimeoutException", t.getMessage());
            this.mensaje =  t.getMessage();
            return false;
        } catch (IOException i) {
            Log.e("IOException", i.getMessage());
            this.mensaje =  i.getMessage();
            return false;
        } catch (XmlPullParserException e) {
            Log.e("XmlPullParserException", e.getMessage());
            this.mensaje =  e.getMessage();
            return false;
        }catch (Exception e) {
            this.mensaje =  e.getMessage();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (OPERACION == SELECT) {
                listaProductos.clear(); // Borrar todos clientes de la lista
                if (responseVector.size() > 0) {
                    for (int i = 0; i < responseVector.size(); i++) {
                        SoapObject ic = (SoapObject) responseVector.get(i);
                        Producto prod = new Producto(
                                Integer.parseInt(ic.getProperty("idproducto").toString()),
                                Integer.parseInt(ic.getProperty("codigoproducto").toString()),
                                ic.getProperty("nombreproducto").toString(),
                                Double.parseDouble(ic.getProperty("precioproducto").toString()),
                                Integer.parseInt(ic.getProperty("stock").toString())
                        );
                        listaProductos.add(prod);
                    }
                } else if (resSoap != null) {
                    SoapPrimitive idproducto = (SoapPrimitive) resSoap.getProperty(1);
                    SoapPrimitive codigoproducto = (SoapPrimitive) resSoap.getProperty(0);
                    SoapPrimitive nombreproducto = (SoapPrimitive) resSoap.getProperty(2);
                    SoapPrimitive precioproducto = (SoapPrimitive) resSoap.getProperty(3);
                    SoapPrimitive stock = (SoapPrimitive) resSoap.getProperty(4);
                    Producto prod = new Producto(
                            Integer.parseInt(idproducto.toString()),
                            Integer.parseInt(codigoproducto.toString()),
                            nombreproducto.toString(),
                            Double.parseDouble(precioproducto.toString()),
                            Integer.parseInt(stock.toString())
                    );
                    listaProductos.add(prod);
                }
            }
        }
    }

    public boolean getResultado() {
        return resultado;
    }
    public String getMensaje() {
        return mensaje;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }
}

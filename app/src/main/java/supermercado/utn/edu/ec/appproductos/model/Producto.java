package supermercado.utn.edu.ec.appproductos.model;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;

public class Producto implements KvmSerializable,Serializable {
    private int id;
    private int codigo;
    private String nombre;
    private double precio;
    private int stock;

    public Producto(int id,int codigo, String nombre, double precio, int stock) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public Producto(int codigo, String nombre, double precio, int stock) {
        this(-1,codigo,nombre,precio,stock);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public Object getProperty(int i) {
        switch(i ){
            case 0:
                return id;
            case 1:
                return codigo;
            case 2:
                return nombre;
            case 3:
                return precio;
            case 4:
                return stock;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 5;
    }

    @Override
    public void setProperty(int ind, Object val) {
        switch(ind) {
            case 1:
                id = Integer.parseInt(val.toString());
                break;
            case 0:
                codigo = Integer.parseInt(val.toString());
                break;
            case 2:
                nombre = val.toString();
                break;
            case 3:
                precio =Double.parseDouble(val.toString());
                break;
            case 4:
                stock = Integer.parseInt(val.toString());
                break;
            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int ind, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch(ind) {
            case 1:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "idproducto";
                break;
            case 0:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "codigoproducto";
                break;
            case 2:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "nombreproducto";
                break;
            case 3:
                propertyInfo.type = Double.class;
                propertyInfo.name = "precioproducto";
                break;
            case 4:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "stock";
                break;
            default:break;
        }
    }
}

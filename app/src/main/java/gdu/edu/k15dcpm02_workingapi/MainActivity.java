package gdu.edu.k15dcpm02_workingapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity
{
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    ArrayList<Product> ds_products;
    Product product;
    MyAdapter_Product myadapter;
    GridView grid1;

    private void Show_DialogSV(Product product, int option)
    {
        AlertDialog.Builder buider=new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater=getLayoutInflater();
        View alerlayout=inflater.inflate(R.layout.edit_layout, null);
        EditText txt_title=(EditText) alerlayout.findViewById(R.id.txt_title);
        EditText txt_price=(EditText) alerlayout.findViewById(R.id.txt_price);
        EditText txt_description=(EditText) alerlayout.findViewById(R.id.txt_description);
        EditText txt_category=(EditText) alerlayout.findViewById(R.id.txt_category);
        EditText txt_image=(EditText) alerlayout.findViewById(R.id.txt_image);

        buider.setView(alerlayout);

        if (option==1) /*Tạo mới*/
        {
            txt_title.setText("");
            txt_price.setText("");
            txt_description.setText("");
            txt_category.setText("");
            txt_image.setText("");
        }
        else if (option==2)/*Sửa*/ {

            txt_title.setText(product.getTitle());
            txt_price.setText(product.getPrice()+"");
            txt_description.setText(product.getDescription());
            txt_category.setText(product.getCategory());
            txt_image.setText(product.getImage());

        }

        buider.setTitle("Hiệu chỉnh");

        buider.setPositiveButton("Ghi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                product.setTitle(txt_title.getText().toString());
                double price=Double.valueOf(txt_price.getText().toString());
                product.setPrice(price);
                product.setDescription(txt_description.getText().toString());
                product.setCategory(txt_category.getText().toString());
                product.setImage(txt_image.getText().toString());

                if (option==2)
                {
                    try {
                        UpdateAPI("https://fakestoreapi.com/products/" + product.getId(), product);
                        myadapter.notifyDataSetChanged();
                    }catch (Exception ex)
                    {
                        Toast.makeText(MainActivity.this, "Lỗi:"+ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
                else if (option==1)
                {
                    try {
                        InsertAPI("https://fakestoreapi.com/products", product);

                        ds_products.add(product);
                        myadapter.notifyDataSetChanged();
                    }catch (Exception ex)
                    {
                        Toast.makeText(MainActivity.this, "Lỗi:"+ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }


                //Cập nhật vào backend



            }
        });

        buider.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        buider.show();


    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int pos=info.position;
        int id=item.getItemId();
        product=(Product) ds_products.get(pos);

        if (id==R.id.mnu_ctxEdit)
        {
            Show_DialogSV(product,2);
        }

        else if (id==R.id.mnu_ctxAdd)
        {
            Show_DialogSV(product,1);
            //action=1;/*Thêm mới*/
        }
        else if (id==R.id.mnu_ctxDel)
        {
            try {
                boolean ok=Accept_AlertDialog();
                Log.w("Check Accept Dialog","Check Accept_AlertDialog:"+ String.valueOf(ok));
                if (ok)
                {

                    DelteteAPI("https://fakestoreapi.com/products/" + String.valueOf(product.getId()));
                    ds_products.remove(product);
                    myadapter.notifyDataSetChanged();
                }

            }catch (Exception ex)
            {}

        }

        return super.onContextItemSelected(item);
    }


    private void ListProductFromAPI(String url_string)
    {
        OkHttpClient client = new OkHttpClient();
        Log.w("Bắt đầu chạy", url_string);

        Request request=new Request.Builder().url(url_string).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Error", "Network error");
                Log.w("Lỗi kết nối mạng:",e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json=response.body().string();
                Log.w("json111: ",json);
                //Toast.makeText(MainActivity.this,json,Toast.LENGTH_LONG).show();

                Moshi moshi=new Moshi.Builder().build();
                Type usertype= Types.newParameterizedType(List.class,Product.class);
                JsonAdapter<List<Product>> jsonadapter=moshi.adapter(usertype);
                final List<Product> products=jsonadapter.fromJson(json);
                ds_products=new ArrayList<>(products);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        Log.w("Vào thủ tục runOnUiThread:","Vào thủ tục runOnUiThread");
                        myadapter=new MyAdapter_Product(MainActivity.this,R.layout.item_listproduct,ds_products);
                        grid1.setAdapter(myadapter);
                    }
                });
            }
        });
    }


    public void UpdateAPI(String url_string, Product product) throws IOException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        OkHttpClient client = new OkHttpClient();
        JSONObject postdata = new JSONObject();
        try {
            postdata.put("title", product.getTitle());
            postdata.put("price", String.valueOf(product.getPrice()));
            postdata.put("category", product.getCategory());
            postdata.put("description", product.getDescription());
            postdata.put("image", product.getImage());
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RequestBody body = RequestBody.Companion.create(postdata.toString(), JSON);
        Request request = new Request.Builder()
                .url(url_string)
                .put(body)
//                .header("Accept", "application/json")
//                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {

                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();

                Log.w("Update response",mMessage);
                Log.w("Upadate message code",String.valueOf(response.code()));

            }
        });
    }


    public void InsertAPI(String url_string, Product product) throws IOException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        OkHttpClient client = new OkHttpClient();
        JSONObject postdata = new JSONObject();
        try {
            postdata.put("title", product.getTitle());
            postdata.put("price", String.valueOf(product.getPrice()));
            postdata.put("category", product.getCategory());
            postdata.put("description", product.getDescription());
            postdata.put("image", product.getImage());
        } catch(JSONException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RequestBody body = RequestBody.Companion.create(postdata.toString(), JSON);
        Request request = new Request.Builder()
                .url(url_string)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {

                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();

                Log.w("Insert response",mMessage);
                Log.w("Insert message code",String.valueOf(response.code()));

            }
        });
    }


    public void DelteteAPI(String url_string) throws IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url_string)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {

                String mMessage = e.getMessage().toString();
                Log.w("Delete failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();

                Log.w("Delete json response",mMessage);
                Log.w("Response code", String.valueOf(response.code()));
//                if (response.code()==200)
//                    Toast.makeText(MainActivity.this, "Đã XÓA sản phẩm thành công trên Restful API", Toast.LENGTH_SHORT).show();

            }
        });
    }

    boolean ok1=false;
    public boolean Accept_AlertDialog()
    {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        ok1 =true;
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        ok1 =false;
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Bạn có chắc chắn xóa sản phẩm này không?").setPositiveButton("Có", dialogClickListener)
                .setNegativeButton("Không", dialogClickListener).show();
        return ok1;
    };


    private void DisplayMenuBar()
    {
        androidx.appcompat.widget.Toolbar toolbar=(androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar1);
        //toolbar.setSubtitle("Test Subtitle");
        toolbar.inflateMenu(R.menu.menu_bar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.new_game) {
//                    Intent intent=new Intent(MainActivity.this, MainActivity_WebAcess.class);
//                    startActivity(intent);
                }

                return false;
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grid1=(GridView) findViewById(R.id.grid1);
        ListProductFromAPI("https://fakestoreapi.com/products");
        registerForContextMenu(grid1);

        DisplayMenuBar();





    }
}
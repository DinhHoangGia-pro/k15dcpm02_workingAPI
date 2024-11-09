package gdu.edu.k15dcpm02_workingapi;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.*;

import java.util.ArrayList;

public class MyAdapter_Product extends ArrayAdapter<Product>
{
    private Context context;/* Activity (tức là Form) cần xử lý */
    private int resource;/* File resouce item_listview mà mình định custom */
    private ArrayList<Product> dssanpham; /*List sv*/

    public MyAdapter_Product(Context context, int resource, ArrayList<Product> dssanpham)
    {
        super(context,resource,dssanpham);
        this.context=context;
        this.resource=resource;
        this.dssanpham=dssanpham;
    }



    @Override
    public View getView(int position, View current, ViewGroup parent) {
        MyAdapter_Product.ViewHolder_Product viewholder;

        if (current==null)
        {
            current= LayoutInflater.from(context).inflate(resource,parent,false);
            viewholder=new MyAdapter_Product.ViewHolder_Product();
            viewholder.iv_avatar=(ImageView) current.findViewById(R.id.iv_avatar);
            viewholder.txtTitle=(TextView) current.findViewById(R.id.txtTitle);
            viewholder.txtCategory=(TextView) current.findViewById(R.id.txtCategory);
            viewholder.txtPrice=(TextView) current.findViewById(R.id.txtPrice);
            viewholder.txtDescription=(TextView) current.findViewById(R.id.txtDescription);

            //Lưu lại viewholder
            current.setTag(viewholder);
        }
        else
        {
            viewholder=(MyAdapter_Product.ViewHolder_Product) current.getTag();
        }

        try {
            Product product = dssanpham.get(position);
            viewholder.txtTitle.setText(product.getTitle());
            viewholder.txtCategory.setText(product.getCategory());
            viewholder.txtPrice.setText(product.getPrice() + "");
            viewholder.txtDescription.setText(product.getDescription().substring(0,10)+"...");

            Picasso.get()
                    .load(product.getImage())
                    .into(viewholder.iv_avatar);

        }catch (Exception ex)
        {
            //System.out.println(ex.getMessage());
            //Toast.makeText(context, ex.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return current;
    };



    public class ViewHolder_Product
    {
        ImageView iv_avatar;
        TextView txtTitle,txtCategory,txtPrice,txtDescription;
    }

}


package ru.code22.mtrade;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class NomenclatureAdapter extends RecyclerViewCursorAdapter<NomenclatureAdapter.NomenclatureViewHolder> {

    private static ClickListener clickListener;

    public static final int TYPE_NOMENCLATURE_GROUP = 1;
    public static final int TYPE_NOMENCLATURE_ITEM = 2;

    private MySingleton g;

    boolean m_bInStock;
    boolean m_bPacks;
    String m_mode;

    /**
     * Column projection for the query to pull Movies from the database.
     */
    public static final String[] NOMENCLATURE_LIST_COLUMNS = new String[] {
            //MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            //MovieContract.MovieEntry.COLUMN_NAME
        // Номенклатура
        "_id", "isFolder", "h_groupDescr", "descr", "nomenclature_id", "quant_1", "quant_k_1", "quant_2", "quant_k_2", "required_sales", "image_file", "image_file_checksum", "nomenclature_color",
        // Прайс
        "price", "k", "edIzm",
        // Наценки по клиентам
        "priceAdd", "priceProcent",
        // Остатки
        "nom_quantity",
        // Продажи (история)
        "quantity_saled",
        // Продажи (в текущем периоде)
        "nom_quantity_saled_now",
        "image_width", "image_height",
        "quantity7_1", "quantity7_2", "quantity7_3", "quantity7_4"
    };



    /**
     * Index of the name column.
     */
    private static final int IS_FOLDER_INDEX = 1;
    private static final int GROUP_DESCR_INDEX = 2;
    private static final int DESCR_INDEX = 3;
    private static final int NOMENCLATURE_ID_INDEX = 4;
    private static final int QUANT_1_INDEX = 5;
    private static final int QUANT_K_1_INDEX = 6;
    private static final int QUANT_2_INDEX = 7;
    private static final int QUANT_K_2_INDEX = 8;
    private static final int REQUIRED_SALES_INDEX = 9;
    private static final int IMAGE_FILE_INDEX = 10;
    private static final int IMAGE_FILE_CHECKSUM_INDEX = 11;
    private static final int NOMENCLATURE_COLOR_INDEX = 12;
    private static final int PRICE_INDEX = 13;
    private static final int K_INDEX = 14;
    private static final int EDIZM_INDEX = 15;
    private static final int PRICE_ADD_INDEX = 16;
    private static final int PRICE_PROCENT_INDEX = 17;
    private static final int NOM_QUANTITY_INDEX = 18;
    private static final int QUANTITY_SALED_INDEX = 19;
    private static final int NOM_QUANTITY_SALED_NOW_INDEX = 20;

    private static final int IMAGE_WIDTH_INDEX = 21;
    private static final int IMAGE_HEIGHT_INDEX = 22;

    private static final int QUANTITY7_1_INDEX=23;
    private static final int QUANTITY7_2_INDEX=24;
    private static final int QUANTITY7_3_INDEX=25;
    private static final int QUANTITY7_4_INDEX=26;


    /**
     * Constructor.
     * @param context The Context the Adapter is displayed in.
     */
    public NomenclatureAdapter(Context context, String mode, boolean bInStock, boolean bPacks) {
        super(context);
        m_mode=mode;
        m_bInStock=bInStock;
        m_bPacks=bPacks;
        setupCursorAdapter(null, 0, R.layout.nomenclature_cardview, false);
        g = MySingleton.getInstance();
    }

    public void setParameters(boolean bInStock, boolean bPacks) {
        m_bInStock=bInStock;
        m_bPacks=bPacks;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor=mCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        if (cursor.getInt(IS_FOLDER_INDEX)==1)
            return TYPE_NOMENCLATURE_GROUP;
        return TYPE_NOMENCLATURE_ITEM;
    }

    @NonNull
    @Override
    public NomenclatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return new NomenclatureViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
        switch (viewType)
        {
        case TYPE_NOMENCLATURE_GROUP: {
            NomenclatureViewHolder viewHolder=new NomenclatureGroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.nomenclature_line_item_group, parent, false));
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
            return viewHolder;
        }
        case TYPE_NOMENCLATURE_ITEM: {

            NomenclatureViewHolder viewHolder=new NomenclatureItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.nomenclature_cardview, parent, false));
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setFullSpan(false);
            return viewHolder;
        }
        }
        return null;
    }


    /**
     * Moves the Cursor of the CursorAdapter to the appropriate position and binds the view for
     * that item.
     */
    @Override
    public void onBindViewHolder(@NonNull NomenclatureViewHolder holder, int position) {
        // Move cursor to this position
        mCursorAdapter.getCursor().moveToPosition(position);
        // здесь можно проверять также holder.getItemViewType()
        // Set the ViewHolder
        setViewHolder(holder);
        // Bind this view
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }

    public class NomenclatureGroupViewHolder extends NomenclatureViewHolder
    {
        public final TextView mNomenclatureGroup;
        public final TextView mNomenclatureGroupSales;

        public NomenclatureGroupViewHolder(View view) {
            super(view);
            mNomenclatureGroup=(TextView)view.findViewById(R.id.tvNomenclatureGroup);
            mNomenclatureGroupSales=(TextView)view.findViewById(R.id.tvNomenclatureGroupSales);
        }

        @Override
        public void bindCursor(Cursor cursor) {
            super.bindCursor(cursor);
            mNomenclatureGroup.setText(cursor.getString(GROUP_DESCR_INDEX));

            double quantity7_1=cursor.getDouble(QUANTITY7_1_INDEX);
            double quantity7_2=cursor.getDouble(QUANTITY7_2_INDEX);
            double quantity7_3=cursor.getDouble(QUANTITY7_3_INDEX);
            double quantity7_4=cursor.getDouble(QUANTITY7_4_INDEX);

            if (quantity7_1>0.001||quantity7_2>0.001||quantity7_3>0.001||quantity7_4>0.001) {
                StringBuilder sb = new StringBuilder();
                sb.append(Common.DoubleToStringFormat(quantity7_1, "%.3f"));
                sb.append("/");
                sb.append(Common.DoubleToStringFormat(quantity7_2, "%.3f"));
                sb.append("/");
                sb.append(Common.DoubleToStringFormat(quantity7_3, "%.3f"));
                sb.append("/");
                sb.append(Common.DoubleToStringFormat(quantity7_4, "%.3f"));
                mNomenclatureGroupSales.setText(sb.toString());
                mNomenclatureGroupSales.setVisibility(View.VISIBLE);
            } else
                mNomenclatureGroupSales.setVisibility(View.GONE);
        }
    }

    public class NomenclatureItemViewHolder extends NomenclatureViewHolder
    {

        public final TextView mNomenclatureDescr;
        public final ImageView mImageView;
        public final TextView mNomenclatureLinePrice;
        public final TextView mNomenclatureLineRests;
        public final TextView mNomenclatureLineSales;
        public final TextView mNomenclatureLineSalesHistoryPeriod;
        public final TextView mNomenclatureLineSalesNowPeriod;

        // для раскраски фона
        public final LinearLayout mLinearLayoutNomenclatureList_1;
        public final LinearLayout mLinearLayoutNomenclatureList_2;


        public NomenclatureItemViewHolder(View view) {
            super(view);
            mNomenclatureDescr=(TextView) view.findViewById(R.id.tvNomenclatureLineDescr);
            mImageView=(ImageView) view.findViewById(R.id.cardimage);
            mNomenclatureLinePrice=(TextView) view.findViewById(R.id.tvNomenclatureLinePrice);
            mNomenclatureLineRests=(TextView) view.findViewById(R.id.tvNomenclatureLineRests);
            mNomenclatureLineSales=(TextView) view.findViewById(R.id.tvNomenclatureLineSales);
            mNomenclatureLineSalesHistoryPeriod=(TextView) view.findViewById(R.id.tvNomenclatureLineSalesHistoryPeriod);
            mNomenclatureLineSalesNowPeriod=(TextView) view.findViewById(R.id.tvNomenclatureLineSalesNowPeriod);

            mLinearLayoutNomenclatureList_1=(LinearLayout)view.findViewById(R.id.LinearLayoutNomenclatureList_1);
            mLinearLayoutNomenclatureList_2=(LinearLayout)view.findViewById(R.id.LinearLayoutNomenclatureList_1);

        }

        @Override
        public void bindCursor(Cursor cursor) {
            super.bindCursor(cursor);

            mNomenclatureDescr.setText(cursor.getString(DESCR_INDEX));
            //mNomenclatureDescr.setTag(cursor.getInt(0)); // id
            //mImageView.setImageResource(R.drawable.ic_menu_camera);
            // первый параметр imageUrl
            //ImageLoader.getInstance().displayImage("https://avatars.mds.yandex.net/get-banana/50379/x25B_laQrT3yKGQMR4wVsOTNJ_banana_20161021_1-1.png/optimize", mImageView);

            //String[] fromColumns = {"h_groupDescr", "descr", "price", "nom_quantity", "zero", "quantity_saled", "nom_quantity_saled_now", "zero", "zero"};
            //int[] toViews = {R.id.tvNomenclatureGroup, R.id.tvNomenclatureLineDescr, R.id.tvNomenclatureLinePrice, R.id.tvNomenclatureLineRests, R.id.tvNomenclatureLineSales, R.id.tvNomenclatureLineSalesHistoryPeriod, R.id.tvNomenclatureLineSalesNowPeriod, R.id.ibtnNomenclatureLine, R.id.ibtnNomenclatureGroup};


            mNomenclatureLinePrice.setText(cursor.getString(PRICE_INDEX));//=(TextView) view.findViewById(R.id.tvNomenclatureLinePrice);
            // TODO "много"
            mNomenclatureLineRests.setText(cursor.getString(NOM_QUANTITY_INDEX));//=(TextView) view.findViewById(R.id.tvNomenclatureLineRests);
            // TODO расчет в зависимости от режима, текущий открытый документ (Желтым цветом выводим количество в данном документе)
            mNomenclatureLineSales.setText(cursor.getString(QUANTITY_SALED_INDEX));//=(TextView) view.findViewById(R.id.tvNomenclatureLineSales);
            mNomenclatureLineSalesHistoryPeriod.setText(cursor.getString(QUANTITY_SALED_INDEX));//=(TextView) view.findViewById(R.id.tvNomenclatureLineSalesHistoryPeriod);
            mNomenclatureLineSalesNowPeriod.setText(cursor.getString(NOM_QUANTITY_SALED_NOW_INDEX));//=(TextView) view.findViewById(R.id.tvNomenclatureLineSalesNowPeriod);

            int myBackgroundColor = Color.TRANSPARENT;
            if (g.Common.PRODLIDER||g.Common.TANDEM)
            {
                int color=cursor.getInt(NOMENCLATURE_COLOR_INDEX);
                if (color!=0)
                {
                    myBackgroundColor=0xFF000000|color;
                    //myBackgroundColor=Color.rgb(0, 130, 0);
                }
            }

            // это нет смысла закрашивать, это фон вокруг карточки
            //itemView.setBackgroundColor(Color.myBackgroundColor;

            // case R.id.tvNomenclatureLinePrice:
            // Цена и количество в текущей заявке
            if (cursor.getString(EDIZM_INDEX)==null)
            {
                // Цена не указана или не указан тип цены в заказе
                mNomenclatureLinePrice.setText("-");
            } else
            {
                double price=cursor.getDouble(PRICE_INDEX);
                double priceAdd=cursor.getDouble(PRICE_ADD_INDEX);
                double priceProcent=cursor.getDouble(PRICE_PROCENT_INDEX);
                //StringBuilder sb=new StringBuilder(Common.DoubleToStringFormat(price, "%.3f")).append(mContext.getResources().getString(R.string.default_currency));
                StringBuilder sb=new StringBuilder(String.format(g.Common.getCurrencyFormatted(mContext), Common.DoubleToStringFormat(price, "%.3f")));
                if (priceProcent!=0.0||priceAdd!=0)
                {
                    price+=Math.floor(price*priceProcent+0.00001)/100.0+priceAdd;
                    sb.append('(');
                    if (priceProcent!=0.0)
                    {
                        if (priceProcent>0.0)
                            sb.append("+");
                        sb.append(priceProcent).append("%");
                    }
                    if (priceAdd!=0.0)
                    {
                        if (priceAdd>0.0)
                            sb.append("+");
                        sb.append(priceAdd);
                    }
                    sb.append(')');
                }
                sb.append('/').append(cursor.getString(EDIZM_INDEX));
                mNomenclatureLinePrice.setText(sb.toString());
            }

            //case R.id.tvNomenclatureLineRests:
            //View w1=(View)view.getParent().getParent();
            //w1.setBackgroundColor(myBackgroundColor);
            double quantity=cursor.getDouble(NOM_QUANTITY_INDEX);
            if (g.Common.PHARAON||m_mode.equals("REFUND"))
            {
                mNomenclatureLineRests.setText("");
            } else
            if (quantity>Constants.MAX_QUANTITY)
            {
                mNomenclatureLineRests.setText(mContext.getString(R.string.much));
            } else
            {
                // Остатки на складе с единицей измерения
                if (m_bPacks)
                {
                    int quant_1_index=cursor.getColumnIndex("quant_1");
                    int quant_2_index=cursor.getColumnIndex("quant_2");
                    int quant_k_2_index=cursor.getColumnIndex("quant_k_2");
                    double k=cursor.getDouble(quant_k_2_index);
                    if (quantity>-0.0001&&quantity<0.0001)
                    {
                        mNomenclatureLineRests.setText("-");
                    } else
                    if (k<0.0001||k>=0.99999&&k<=1.000001)
                    {
                        // Считаем все равно базовую
                        String strQuantity=Double.toString(cursor.getDouble(NOM_QUANTITY_INDEX))+" "+cursor.getString(quant_1_index);
                        mNomenclatureLineRests.setText(strQuantity);
                    } else
                    {
                        // Упаковки
                        // если полная упаковка - пишем, например, так
                        // 15уп(*5шт)
                        // если неполная, то
                        // 15уп(*5шт)+3
                        long full_pack_quantity=Math.round(quantity/k);
                        double base_quantity=0;
                        if (full_pack_quantity*k>quantity+0.0001)
                        {
                            // Упаковки не целые, но округлилось в большую сторону
                            full_pack_quantity--;
                            base_quantity=quantity-full_pack_quantity*k;
                        } else
                        if (full_pack_quantity*k<quantity-0.0001)
                        {
                            // Упаковки не целые, округлилось в меньшую сторону
                            base_quantity=quantity-full_pack_quantity*k;
                        } else
                        {
                            // Упаковки целые
                            base_quantity=0.0;
                        }
                        StringBuilder sb=new StringBuilder(Long.toString(full_pack_quantity)).append(cursor.getString(quant_2_index)).append("(*").append(Common.DoubleToStringFormat(k, "%.3f")).append(cursor.getString(quant_1_index)).append(")");
                        if (base_quantity<-0.0001||base_quantity>0.0001)
                            sb.append('+').append(Common.DoubleToStringFormat(base_quantity, "%.3f"));
                        mNomenclatureLineRests.setText(sb.toString());
                    }
                } else
                {
                    // Базовые
                    int quant_1_index=cursor.getColumnIndex("quant_1");
                    //int nom_quantity_index=cursor.getColumnIndex("nom_quantity");
                    //double quantity=cursor.getDouble(nom_quantity_index);
                    String strQuantity;
                    if (quantity<-0.0001||quantity>0.0001)
                    {
                        strQuantity=Common.DoubleToStringFormat(quantity, "%.3f")+" "+cursor.getString(quant_1_index);
                    } else
                    {
                        strQuantity="-";
                    }
                    mNomenclatureLineRests.setText(strQuantity);
                }
            }

            //case R.id.tvNomenclatureLineSales:
            //{
            //View w1=(View)view.getParent().getParent();
            //w1.setBackgroundColor(myBackgroundColor);
            double quantity_total=0.0;
            double quantity_k=0.0;
            String quantity_ed="";
            //int nomenclature_id_index=cursor.getColumnIndex(MTradeContentProvider.NOMENCLATURE_ID_COLUMN);
            //int nomenclature_id_index=cursor.getColumnIndex("nomenclature_id");
            //int k_index=cursor.getColumnIndex("k");
            String nomenclatureId=cursor.getString(NOMENCLATURE_ID_INDEX);

            if (m_mode.equals("REFUND"))
            {
                for (MyDatabase.RefundLineRecord line:g.MyDatabase.m_refund_editing.lines)
                {
                    if (line.nomenclature_id.toString().equals(nomenclatureId))
                    {
                        double k=cursor.getDouble(K_INDEX);
                        if (quantity_total>-0.0001&&quantity_total<0.0001)
                        {
                            quantity_k=k;
                            quantity_total=line.quantity;
                            quantity_ed=line.ed;
                        } else
                        {
                            if (quantity_k-k>-0.0001&&quantity_k-k<0.0001)
                            {
                                // единицы совпали
                                quantity_total+=line.quantity;
                            } else
                            {
                                // Единицы разные - переводим в базовые единицы
                                // были не базовые
                                if (quantity_k<0.9999||quantity_k>1.00001)
                                {
                                    quantity_total=quantity_total*quantity_k;
                                    quantity_k=1.0;
                                    if (k>0.9999&&quantity_k<1.00001)
                                    {
                                        // в этой строке базовые
                                        quantity_ed=line.ed;
                                    } else
                                    {
                                        // разные, и базовых среди них нет
                                        quantity_ed="";
                                    }
                                }
                                quantity_total+=line.quantity*k;
                            }
                        }
                    }
                }
            } else
            {
                for (MyDatabase.OrderLineRecord line:g.MyDatabase.m_order_editing.lines)
                {
                    if (line.nomenclature_id.toString().equals(nomenclatureId))
                    {
                        double k=cursor.getDouble(K_INDEX);
                        if (quantity_total>-0.0001&&quantity_total<0.0001)
                        {
                            quantity_k=k;
                            quantity_total=line.quantity;
                            quantity_ed=line.ed;
                        } else
                        {
                            if (quantity_k-k>-0.0001&&quantity_k-k<0.0001)
                            {
                                // единицы совпали
                                quantity_total+=line.quantity;
                            } else
                            {
                                // Единицы разные - переводим в базовые единицы
                                // были не базовые
                                if (quantity_k<0.9999||quantity_k>1.00001)
                                {
                                    quantity_total=quantity_total*quantity_k;
                                    quantity_k=1.0;
                                    if (k>0.9999&&quantity_k<1.00001)
                                    {
                                        // в этой строке базовые
                                        quantity_ed=line.ed;
                                    } else
                                    {
                                        // разные, и базовых среди них нет
                                        quantity_ed="";
                                    }
                                }
                                quantity_total+=line.quantity*k;
                            }
                        }
                    }
                }
            }
            // Желтым цветом выводим количество в данном документе
            if (quantity_total<-0.0001||quantity_total>0.0001)
            {
                if (g.Common.m_app_theme.equals("DARK"))
                {
                    mNomenclatureLineSales.setTextColor(Color.YELLOW);
                } else
                {
                    mNomenclatureLineSales.setBackgroundColor(Color.YELLOW);
                }
                mNomenclatureLineSales.setText(Common.DoubleToStringFormat(quantity_total, "%.3f")+" "+quantity_ed);
                mNomenclatureLineSales.setVisibility(View.VISIBLE);
            } else
            {
                mNomenclatureLineSales.setVisibility(View.GONE);
            }

            //case R.id.tvNomenclatureLineSalesHistoryPeriod:
            //View w1=(View)view.getParent().getParent();
            //w1.setBackgroundColor(myBackgroundColor);
            //int quantity_saled_index=cursor.getColumnIndex("quantity_saled");
            if (cursor.getDouble(QUANTITY_SALED_INDEX)>0.0)
            {
                mNomenclatureLineSalesHistoryPeriod.setVisibility(View.VISIBLE);
                if (g.Common.m_app_theme.equals("DARK"))
                {
                    mNomenclatureLineSalesHistoryPeriod.setTextColor(Color.GREEN);
                } else
                {
                    mNomenclatureLineSalesHistoryPeriod.setTextColor(Color.rgb(0, 130, 0));
                }
            } else
            {
                mNomenclatureLineSalesHistoryPeriod.setVisibility(View.GONE);
            }

            //case R.id.tvNomenclatureLineSalesNowPeriod:
            //{
            //View w1=(View)view.getParent().getParent();
            int quantity_saled_index=cursor.getColumnIndex("nom_quantity_saled_now");
            int required_sales_index=cursor.getColumnIndex("required_sales");
            double quantity_saled=cursor.getDouble(quantity_saled_index);
            double required_sales=cursor.getDouble(required_sales_index);

            if (quantity_saled>0.0||required_sales>0.0)
            {
                if (required_sales<quantity_saled+0.0001)
                {
                    mNomenclatureLineSalesNowPeriod.setBackgroundColor(myBackgroundColor);
                    mNomenclatureLineSalesNowPeriod.setTextColor(Color.BLUE);
                    mNomenclatureLineSalesNowPeriod.setText(Common.DoubleToStringFormat(quantity_saled, "%.3f"));
                } else
                {
                    mNomenclatureLineSalesNowPeriod.setBackgroundColor(mContext.getResources().getColor(R.color.MY_GREEN)); // зеленый
                    mNomenclatureLineSalesNowPeriod.setTextColor(Color.BLUE);
                    StringBuilder sb=new StringBuilder();
                    sb.append(Common.DoubleToStringFormat(quantity_saled, "%.3f"));
                    sb.append(mContext.getResources().getString(R.string.space_from_space));
                    sb.append(Common.DoubleToStringFormat(required_sales, "%.3f"));
                    mNomenclatureLineSalesNowPeriod.setText(sb.toString());
                }
                //tv.setText("xxx");
                mNomenclatureLineSalesNowPeriod.setVisibility(View.VISIBLE);
            } else
            {
                mNomenclatureLineSalesNowPeriod.setBackgroundColor(myBackgroundColor);
                mNomenclatureLineSalesNowPeriod.setVisibility(View.GONE);
            }

            mLinearLayoutNomenclatureList_1.setBackgroundColor(myBackgroundColor);
            mLinearLayoutNomenclatureList_2.setBackgroundColor(myBackgroundColor);

            String id_imageFile=cursor.getString(IMAGE_FILE_INDEX);
            if (id_imageFile!=null&&!id_imageFile.isEmpty())
            {
                // если имя файла указано без расширения (так было давно), то добавляем расширение по умолчанию
                String imageFileName=(id_imageFile.indexOf(".")<0)?Common.idToFileName(id_imageFile)+".jpg":id_imageFile;
                File imagesPath=Common.getMyStorageFileDir(mContext, "goods");
                /*
                if (android.os.Build.VERSION.SDK_INT< Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    // получаем путь к изображениям на SD
                    imagesPath = new File(Environment.getExternalStorageDirectory(), "/mtrade/goods");
                } else
                {
                    imagesPath=new File(Environment.getDataDirectory(), "/data/"+ mContext.getPackageName()+"/temp");
                }
                 */
                File imageFile=new File(imagesPath, imageFileName);
                if (imageFile.exists())
                {
                    //Intent intent=new Intent(NomenclatureActivity.this, ImageActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //intent.putExtra("image_file", imageFile.getAbsolutePath());
                    //startActivityForResult(intent, OPEN_IMAGE_REQUEST);
                    //mImageView.getLayoutParams().width=cursor.getInt(IMAGE_WIDTH_INDEX);
                    //mImageView.getLayoutParams().height=cursor.getInt(IMAGE_HEIGHT_INDEX);
                    ImageLoader.getInstance().displayImage(Uri.fromFile(imageFile).toString(), mImageView);
                } else
                {
                    mImageView.setImageResource(R.drawable.visitingcard);
                }
            } else {
                mImageView.setImageResource(R.drawable.visitingcard);
            }

        }

    }




    public class NomenclatureViewHolder extends RecyclerViewCursorViewHolder implements View.OnClickListener, View.OnLongClickListener
    {

        public NomenclatureViewHolder(View view)
        {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void bindCursor(Cursor cursor) {
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }

    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public Cursor getCursor()
    {
        return mCursorAdapter.getCursor();
    }


    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }
}

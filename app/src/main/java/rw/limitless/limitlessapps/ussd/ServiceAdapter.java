package rw.limitless.limitlessapps.ussd;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.support.v7.widget.RecyclerView.Adapter;
import static android.support.v7.widget.RecyclerView.OnClickListener;

/**
 * Created by limitlessapps on 05/01/2018.
 */

public class ServiceAdapter extends Adapter<ServiceAdapter.ViewHolder>{
    private List<Service_item> serviceVOList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Service_item item);
    }

    public ServiceAdapter(List<Service_item> serviceVOList, OnItemClickListener listener){
        this.serviceVOList = serviceVOList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(serviceVOList.get(position), listener);
        Service_item serviceVO = serviceVOList.get(position);
        holder.title.setText(serviceVO.getTitle());
        holder.service_owner.setText(serviceVO.getService_owner());
    }

    @Override
    public int getItemCount() {
        return serviceVOList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView info;
        TextView title;
        TextView service_owner;

        public ViewHolder(View itemView) {
            super(itemView);
            info = (ImageView) itemView.findViewById(R.id.info);
            title = (TextView) itemView.findViewById(R.id.service_title);
            service_owner = (TextView) itemView.findViewById(R.id.service_owner);
        }

        public void bind(final Service_item item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}

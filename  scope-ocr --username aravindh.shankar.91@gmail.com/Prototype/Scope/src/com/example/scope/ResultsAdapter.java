package com.example.scope;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ResultsAdapter extends BaseAdapter {

	private static List<SegmentationResult> ocrResults;
	private LayoutInflater mInflater;

	public ResultsAdapter(Context context, List<SegmentationResult> ocrResults2) {
		ocrResults = ocrResults2;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return ocrResults.size();
	}

	@Override
	public Object getItem(int position) {
		return ocrResults.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.custom_row_view, null);
			holder = new ViewHolder();
			holder.txtResult = (TextView) convertView.findViewById(R.id.result);
			holder.txtCordinates = (TextView) convertView
					.findViewById(R.id.coordinates);
			holder.txtConfidence = (TextView) convertView
					.findViewById(R.id.confidence);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtResult.setText("Result: " + ocrResults.get(position).Result);
		holder.txtCordinates.setText("Co-ords: "+ ocrResults.get(position).X + ","
				+ ocrResults.get(position).Y);
		holder.txtConfidence.setText("Confidence: " + ocrResults.get(position).Confidence);

		return convertView;
	}

	static class ViewHolder {
		TextView txtResult;
		TextView txtCordinates;
		TextView txtConfidence;
	}
}

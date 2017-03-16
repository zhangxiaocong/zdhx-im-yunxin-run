package zhwx.ui.dcapp.repairs;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.netease.nim.demo.ECApplication;
import com.netease.nim.demo.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import zhwx.common.base.CCPAppManager;
import zhwx.common.model.ParameterValue;
import zhwx.common.util.ProgressThreadWrap;
import zhwx.common.util.RunnableWrap;
import zhwx.common.util.StringUtil;
import zhwx.common.util.ToastUtil;
import zhwx.common.util.Tools;
import zhwx.common.util.UrlUtil;
import zhwx.common.view.dialog.ECProgressDialog;
import zhwx.common.view.imagegallery.ViewImageInfo;
import zhwx.ui.dcapp.carmanage.view.ScrollTabHolderFragment;
import zhwx.ui.dcapp.repairs.adapter.RmImageGirdAdapter;
import zhwx.ui.dcapp.repairs.model.RepairDetail;

/**   
 * @Title: RepairDetailFragment.java
 * @Package zhwx.ui.dcapp.carmanage
 * @author Li.xin @ 中电和讯
 * @date 2016-3-17 下午4:33:18 
 */
public class RepairDetailFragment extends ScrollTabHolderFragment {

	private HashMap<String, ParameterValue> map;
	
	private Handler handler = new Handler();

	private ECProgressDialog mPostingdialog;
	
	private String id;
	
	private String status;

	private String evaluateFlag;

	private String json;
	
	private int startFlag;

	//R
	private TextView requestUserTV,requestTimeTV,requestDeviceTV,faultKindTV,faultDescriptionTV,
			repairHistoryTV,faultPlaceTV,requestPhoneTV,repairerTV,repairTimeTV,repairStatusTV
			,faultLeixingTV,faultReasonTV,elseTV;

	private GridView requestImgGV,repairImgGV;

	private RepairDetail repairDetail;

	public static Fragment newInstance(String id,int startFlag,String status,String evaluateFlag) {
		RepairDetailFragment f = new RepairDetailFragment();
		Bundle b = new Bundle();
		b.putString("id", id);
		b.putString("status", status);
		b.putString("evaluateFlag", evaluateFlag);
		b.putInt("startFlag", startFlag);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		id = getArguments().getString("id");
		status = getArguments().getString("status");
		evaluateFlag = getArguments().getString("evaluateFlag");
		startFlag = getArguments().getInt("startFlag",-1);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = null;
		v = inflater.inflate(R.layout.fragment_repair_detail, null);
		requestUserTV = (TextView) v.findViewById(R.id.requestUserTV);
		requestTimeTV = (TextView) v.findViewById(R.id.requestTimeTV);
		requestDeviceTV = (TextView) v.findViewById(R.id.requestDeviceTV);
		faultKindTV = (TextView) v.findViewById(R.id.faultKindTV);
		faultDescriptionTV = (TextView) v.findViewById(R.id.faultDescriptionTV);
		repairHistoryTV = (TextView) v.findViewById(R.id.repairHistoryTV);
		requestPhoneTV = (TextView) v.findViewById(R.id.requestPhoneTV);
		repairerTV = (TextView) v.findViewById(R.id.repairerTV);
		repairTimeTV = (TextView) v.findViewById(R.id.repairTimeTV);
		repairStatusTV = (TextView) v.findViewById(R.id.repairStatusTV);
		faultPlaceTV = (TextView) v.findViewById(R.id.faultPlaceTV);
		faultLeixingTV = (TextView) v.findViewById(R.id.faultLeixingTV);
		faultReasonTV = (TextView) v.findViewById(R.id.faultReasonTV);
		elseTV = (TextView) v.findViewById(R.id.elseTV);
		requestImgGV = (GridView) v.findViewById(R.id.requestImgGV);
		repairImgGV = (GridView) v.findViewById(R.id.repairImgGV);
		return v;
	}
	@Override
	public void onResume() {
		super.onResume();
		getDetail();
	}
	
	private void getDetail(){
		mPostingdialog = new ECProgressDialog(getActivity(), "正在获取信息");
		mPostingdialog.show();
		map = (HashMap<String, ParameterValue>) ECApplication.getInstance().getV3LoginMap();
		map.put("repairId", new ParameterValue(id));
		new ProgressThreadWrap(getActivity(), new RunnableWrap() {
			@Override
			public void run() {
				try {
					json = UrlUtil.getRepairDetail(ECApplication.getInstance().getV3Address(), map);
					handler.postDelayed(new Runnable() {
						public void run() {
							refreshData(json);
						}
					}, 5);
				} catch (IOException e) {
					e.printStackTrace();
					ToastUtil.showMessage("请求失败，请稍后重试");
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							mPostingdialog.dismiss();
						}
					}, 5);
				}
			}
		}).start();
	}
	
	private void refreshData(String json) {
		if(json.contains("<html>")){
			ToastUtil.showMessage("数据异常");
			return;
		}
		System.out.println(json);
		repairDetail = new Gson().fromJson(json,RepairDetail.class);
//		requestUserTV,requestTimeTV,requestDeviceTV,faultKindTV,faultDescriptionTV,
//		repairHistoryTV,faultPlaceTV,requestPhoneTV,repairerTV,repairTimeTV,repairStatusTV,
//		faultLeixingTV,faultReasonTV,elseTV

		//报修图片
		requestImgGV.setAdapter(new RmImageGirdAdapter(getActivity(), repairDetail.getRequestInfo().getImageList()));
		Tools.setGridViewHeightBasedOnChildren4(requestImgGV);
		requestImgGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ArrayList<ViewImageInfo> urls = new ArrayList<ViewImageInfo>();
				ViewImageInfo imageInfo;
				for (int i = 0; i < repairDetail.getRequestInfo().getImageList().size(); i++) {
					imageInfo = new ViewImageInfo("", ECApplication.getInstance().getV3Address()+repairDetail.getRequestInfo().getImageList().get(i));
					urls.add(imageInfo);
				}
				CCPAppManager.startChattingImageViewAction(getActivity(),position , urls);
			}
		});

		requestUserTV.setText(repairDetail.getRequestInfo().getRequestUserName());
		requestTimeTV.setText(repairDetail.getRequestInfo().getRequestTime());
		requestDeviceTV.setText(repairDetail.getRequestInfo().getDeviceName());
		faultKindTV.setText(repairDetail.getRequestInfo().getMalfunction());
		faultDescriptionTV.setText(repairDetail.getRequestInfo().getMalfunctionDescribe());
		repairHistoryTV.setText(StringUtil.isNotBlank(repairDetail.getRequestInfo().getRepairHistory())?repairDetail.getRequestInfo().getRepairHistory():"无");
		faultPlaceTV.setText(repairDetail.getRequestInfo().getMalfunctionPlace());
		requestPhoneTV.setText(repairDetail.getRequestInfo().getPhoneNum());

		//维修图片
		repairImgGV.setAdapter(new RmImageGirdAdapter(getActivity(), repairDetail.getRepairInfo().getRepairImageList()));
		Tools.setGridViewHeightBasedOnChildren4(repairImgGV);
		repairImgGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ArrayList<ViewImageInfo> urls = new ArrayList<ViewImageInfo>();
				ViewImageInfo imageInfo;
				for (int i = 0; i < repairDetail.getRepairInfo().getRepairImageList().size(); i++) {
					imageInfo = new ViewImageInfo("", ECApplication.getInstance().getV3Address()+repairDetail.getRepairInfo().getRepairImageList().get(i));
					urls.add(imageInfo);
				}
				CCPAppManager.startChattingImageViewAction(getActivity(),position, urls);
			}
		});

		repairerTV.setText(repairDetail.getRepairInfo().getWorkerName());
		repairTimeTV.setText(repairDetail.getRepairInfo().getRepairTime());
		repairStatusTV.setText(repairDetail.getRepairInfo().getRepairStatus());
		faultLeixingTV.setText(repairDetail.getRepairInfo().getMalfunctionKind());
		faultReasonTV.setText(repairDetail.getRepairInfo().getMalfunctionReason());
		elseTV.setText(StringUtil.isNotBlank(repairDetail.getRepairInfo().getGoodsSum())?repairDetail.getRepairInfo().getGoodsSum():"无");

//		actionLay.removeAllViews();
//		List<TextView> btns = getOrderButtonList(status,evaluateFlag);
//		if (btns.size() == 0) {
//			actionLay.setVisibility(View.GONE);
//		} else {
//			for (TextView button : btns) {
//				actionLay.addView(button);
//			}
//		}
		mPostingdialog.dismiss();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void adjustScroll(int scrollHeight) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {

	}
}

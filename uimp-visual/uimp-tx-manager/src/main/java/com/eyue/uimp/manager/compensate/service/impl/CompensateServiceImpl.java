/*
 *    Copyright (c) 2018-2025, uimp All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: uimp
 */

package com.eyue.uimp.manager.compensate.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.lorne.core.framework.exception.ServiceException;
import com.lorne.core.framework.utils.DateUtil;
import com.lorne.core.framework.utils.encode.Base64Utils;
import com.lorne.core.framework.utils.http.HttpUtils;
import com.eyue.uimp.manager.compensate.dao.CompensateDao;
import com.eyue.uimp.manager.compensate.model.TransactionCompensateMsg;
import com.eyue.uimp.manager.compensate.model.TxModel;
import com.eyue.uimp.manager.compensate.service.CompensateService;
import com.eyue.uimp.manager.config.ConfigReader;
import com.eyue.uimp.manager.manager.ModelInfoManager;
import com.eyue.uimp.manager.manager.service.TxManagerSenderService;
import com.eyue.uimp.manager.manager.service.TxManagerService;
import com.eyue.uimp.manager.model.ModelInfo;
import com.eyue.uimp.manager.model.ModelName;
import com.eyue.uimp.manager.netty.model.TxGroup;
import com.eyue.uimp.manager.netty.model.TxInfo;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author LCN on 2017/11/11
 */
@Service
public class CompensateServiceImpl implements CompensateService {
	private static final String SUCCESS = "success";
	private static final String SUCCESS1 = "SUCCESS";
	private Logger logger = LoggerFactory.getLogger(CompensateServiceImpl.class);

	@Autowired
	private CompensateDao compensateDao;

	@Autowired
	private ConfigReader configReader;

	@Autowired
	private TxManagerSenderService managerSenderService;

	@Autowired
	private TxManagerService managerService;

	@Autowired
	private Executor threadPool;

	@Override
	public boolean saveCompensateMsg(final TransactionCompensateMsg transactionCompensateMsg) {

		TxGroup txGroup = managerService.getTxGroup(transactionCompensateMsg.getGroupId());
		if (txGroup == null) {
			//???????????????????????????????????????
			txGroup = new TxGroup();
			txGroup.setNowTime(System.currentTimeMillis());
			txGroup.setGroupId(transactionCompensateMsg.getGroupId());
			txGroup.setIsCompensate(1);
		} else {
			managerService.deleteTxGroup(txGroup);
		}

		transactionCompensateMsg.setTxGroup(txGroup);

		final String json = JSON.toJSONString(transactionCompensateMsg);

		logger.info("Compensate->" + json);

		final String compensateKey = compensateDao.saveCompensateMsg(transactionCompensateMsg);

		//??????????????????????????????????????????????????????????????????????????????success???????????????????????????
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					String groupId = transactionCompensateMsg.getGroupId();
					JSONObject requestJson = new JSONObject();
					requestJson.put("action", "compensate");
					requestJson.put("groupId", groupId);
					requestJson.put("json", json);

					String url = configReader.getCompensateNotifyUrl();
					logger.error("Compensate Callback Address->" + url);
					String res = HttpUtils.postJson(url, requestJson.toJSONString());
					logger.error("Compensate Callback Result->" + res);
					if (configReader.isCompensateAuto()) {
						//????????????,????????????????????????
						if (res.contains(SUCCESS) || res.contains(SUCCESS1)) {
							//????????????
							autoCompensate(compensateKey, transactionCompensateMsg);
						}
					}
				} catch (Exception e) {
					logger.error("Compensate Callback Fails->" + e.getMessage());
				}
			}
		});

		return StringUtils.isNotEmpty(compensateKey);


	}

	@Override
	public void autoCompensate(final String compensateKey, TransactionCompensateMsg transactionCompensateMsg) {
		final String json = JSON.toJSONString(transactionCompensateMsg);
		logger.info("Auto Compensate->" + json);
		//????????????????????????...
		final int tryTime = configReader.getCompensateTryTime();
		boolean autoExecuteRes;
		try {
			int executeCount = 0;
			autoExecuteRes = executeCompensateMethod(json);
			logger.info("Automatic Compensate Result->" + autoExecuteRes + ",json->" + json);
			while (!autoExecuteRes) {
				logger.info("Compensate Failure, Entering Compensate Queue->" + autoExecuteRes + ",json->" + json);
				executeCount++;
				if (executeCount == 3) {
					autoExecuteRes = false;
					break;
				}
				try {
					Thread.sleep(tryTime * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				autoExecuteRes = executeCompensateMethod(json);
			}

			//????????????????????????
			if (autoExecuteRes) {
				compensateDao.deleteCompensateByKey(compensateKey);
			}

		} catch (Exception e) {
			logger.error("Auto Compensate Fails,msg:" + e.getLocalizedMessage());
			//??????????????????????????????
			autoExecuteRes = false;
		}

		//????????????????????????????????????
		String groupId = transactionCompensateMsg.getGroupId();
		JSONObject requestJson = new JSONObject();
		requestJson.put("action", "notify");
		requestJson.put("groupId", groupId);
		requestJson.put("resState", autoExecuteRes);

		String url = configReader.getCompensateNotifyUrl();
		logger.error("Compensate Result Callback Address->" + url);
		String res = HttpUtils.postJson(url, requestJson.toJSONString());
		logger.error("Compensate Result Callback Result->" + res);

	}


	@Override
	public List<ModelName> loadModelList() {
		List<String> keys = compensateDao.loadCompensateKeys();

		Map<String, Integer> models = new HashMap<>(16);

		for (String key : keys) {
			if (key.length() > 36) {
				String name = key.substring(11, key.length() - 25);
				int v = 1;
				if (models.containsKey(name)) {
					v = models.get(name) + 1;
				}
				models.put(name, v);
			}
		}
		List<ModelName> names = new ArrayList<>();

		for (String key : models.keySet()) {
			int v = models.get(key);
			ModelName modelName = new ModelName();
			modelName.setName(key);
			modelName.setCount(v);
			names.add(modelName);
		}
		return names;
	}

	@Override
	public List<String> loadCompensateTimes(String model) {
		return compensateDao.loadCompensateTimes(model);
	}

	@Override
	public List<TxModel> loadCompensateByModelAndTime(String path) {
		List<String> logs = compensateDao.loadCompensateByModelAndTime(path);

		List<TxModel> models = new ArrayList<>();
		for (String json : logs) {
			JSONObject jsonObject = JSON.parseObject(json);
			TxModel model = new TxModel();
			long currentTime = jsonObject.getLong("currentTime");
			model.setTime(DateUtil.formatDate(new Date(currentTime), DateUtil.FULL_DATE_TIME_FORMAT));
			model.setClassName(jsonObject.getString("className"));
			model.setMethod(jsonObject.getString("methodStr"));
			model.setExecuteTime(jsonObject.getInteger("time"));
			model.setBase64(Base64Utils.encode(json.getBytes()));
			model.setState(jsonObject.getInteger("state"));
			model.setOrder(currentTime);

			String groupId = jsonObject.getString("groupId");

			String key = path + ":" + groupId;
			model.setKey(key);

			models.add(model);
		}
		Collections.sort(models, new Comparator<TxModel>() {
			@Override
			public int compare(TxModel o1, TxModel o2) {
				if (o2.getOrder() > o1.getOrder()) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		return models;
	}

	@Override
	public boolean hasCompensate() {
		return compensateDao.hasCompensate();
	}

	@Override
	public boolean delCompensate(String path) {
		compensateDao.deleteCompensateByPath(path);
		return true;
	}

	@Override
	public void reloadCompensate(TxGroup txGroup) {
		TxGroup compensateGroup = getCompensateByGroupId(txGroup.getGroupId());
		if (compensateGroup != null) {

			if (compensateGroup.getList() != null && !compensateGroup.getList().isEmpty()) {
				//???????????? iterator??????????????????????????????
				Iterator<TxInfo> iterator = Lists.newArrayList(compensateGroup.getList()).iterator();
				for (TxInfo txInfo : txGroup.getList()) {
					while (iterator.hasNext()) {
						TxInfo cinfo = iterator.next();
						if (cinfo.getModel().equals(txInfo.getModel()) && cinfo.getMethodStr().equals(txInfo.getMethodStr())) {
							//??????????????????????????????????????????
							int oldNotify = cinfo.getNotify();

							if (oldNotify == 1) {
								//????????????
								txInfo.setIsCommit(0);
							} else {
								//????????????
								txInfo.setIsCommit(1);
							}
							//?????????????????????
							iterator.remove();
							break;
						}
					}
				}
			} else {//?????????List??????????????????????????????????????????????????????????????????????????????
				for (TxInfo txInfo : txGroup.getList()) {
					//????????????
					txInfo.setIsCommit(0);
				}
			}
		}
		logger.info("Compensate Loaded->" + JSON.toJSONString(txGroup));
	}

	@Override
	public TxGroup getCompensateByGroupId(String groupId) {
		String json = compensateDao.getCompensateByGroupId(groupId);
		if (json == null) {
			return null;
		}
		JSONObject jsonObject = JSON.parseObject(json);
		String txGroup = jsonObject.getString("txGroup");
		return JSON.parseObject(txGroup, TxGroup.class);
	}


	@Override
	@SneakyThrows
	public boolean executeCompensate(String path) {

		String json = compensateDao.getCompensate(path);
		if (json == null) {
			throw new ServiceException("no data existing");
		}

		boolean hasOk = executeCompensateMethod(json);
		if (hasOk) {
			// ????????????????????????
			compensateDao.deleteCompensateByPath(path);

			return true;
		}
		return false;
	}

	@SneakyThrows
	private boolean executeCompensateMethod(String json) {
		JSONObject jsonObject = JSON.parseObject(json);

		String model = jsonObject.getString("model");

		int startError = jsonObject.getInteger("startError");

		ModelInfo modelInfo = ModelInfoManager.getInstance().getModelByModel(model);
		if (modelInfo == null) {
			throw new ServiceException("current model offline.");
		}

		String data = jsonObject.getString("data");

		String groupId = jsonObject.getString("groupId");

		String res = managerSenderService.sendCompensateMsg(modelInfo.getChannelName(), groupId, data, startError);

		logger.debug("executeCompensate->" + json + ",@@->" + res);

		return "1".equals(res);
	}
}

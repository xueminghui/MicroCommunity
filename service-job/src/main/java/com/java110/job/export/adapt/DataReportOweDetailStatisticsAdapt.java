package com.java110.job.export.adapt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.dto.Dict.DictDto;
import com.java110.dto.RoomDto;
import com.java110.dto.data.ExportDataDto;
import com.java110.dto.report.QueryStatisticsDto;
import com.java110.intf.dev.IDictV1InnerServiceSMO;
import com.java110.intf.report.IBaseDataStatisticsInnerServiceSMO;
import com.java110.intf.report.IReportFeeStatisticsInnerServiceSMO;
import com.java110.job.export.IExportDataAdapt;
import com.java110.utils.util.Assert;
import com.java110.utils.util.StringUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 欠费明细
 */
@Service("dataReportOweDetailStatistics")
public class DataReportOweDetailStatisticsAdapt implements IExportDataAdapt {

    @Autowired
    private IDictV1InnerServiceSMO dictV1InnerServiceSMOImpl;

    @Autowired
    private IReportFeeStatisticsInnerServiceSMO reportFeeStatisticsInnerServiceSMOImpl;

    @Autowired
    private IBaseDataStatisticsInnerServiceSMO baseDataStatisticsInnerServiceSMOImpl;

    private static final int MAX_ROW = 100;

    @Override
    public SXSSFWorkbook exportData(ExportDataDto exportDataDto) {
        JSONObject reqJson = exportDataDto.getReqJson();
        Assert.hasKeyAndValue(reqJson, "communityId", "未包含小区");
        String startDate = reqJson.getString("startDate");
        String endDate = reqJson.getString("endDate");
        if (!StringUtil.isEmpty(startDate) && !startDate.contains(":")) {
            startDate += " 00:00:00";
            reqJson.put("startDate", startDate);
        }
        if (!StringUtil.isEmpty(endDate) && !endDate.contains(":")) {
            endDate += " 23:59:59";
            reqJson.put("endDate", endDate);
        }

        SXSSFWorkbook workbook = null;  //工作簿
        workbook = new SXSSFWorkbook();
        workbook.setCompressTempFiles(false);

        Sheet sheet = workbook.createSheet("欠费明细");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("房屋");
        row.createCell(1).setCellValue("业主");
        row.createCell(2).setCellValue("欠费");
        DictDto dictDto = new DictDto();
        dictDto.setTableName("pay_fee_config");
        dictDto.setTableColumns("fee_type_cd_show");
        List<DictDto> dictDtos = dictV1InnerServiceSMOImpl.queryDicts(dictDto);

        for (int dictIndex = 0; dictIndex < dictDtos.size(); dictIndex++) {
            row.createCell(3 + dictIndex).setCellValue(dictDtos.get(dictIndex).getName());
        }

        QueryStatisticsDto queryStatisticsDto = new QueryStatisticsDto();
        queryStatisticsDto.setCommunityId(reqJson.getString("communityId"));
        queryStatisticsDto.setStartDate(reqJson.getString("startDate"));
        queryStatisticsDto.setEndDate(reqJson.getString("endDate"));
        queryStatisticsDto.setConfigId(reqJson.getString("configId"));
        queryStatisticsDto.setFloorId(reqJson.getString("floorId"));
        queryStatisticsDto.setObjName(reqJson.getString("objName"));
        queryStatisticsDto.setFeeTypeCd(reqJson.getString("feeTypeCd"));
        queryStatisticsDto.setOwnerName(reqJson.getString("ownerName"));
        queryStatisticsDto.setLink(reqJson.getString("link"));
        long roomCount = getOweRoomCount(queryStatisticsDto);

        roomCount = (int) Math.ceil((double) roomCount / (double) MAX_ROW);

        for (int page = 1; page <= roomCount; page++) {
            queryStatisticsDto.setPage(page);
            queryStatisticsDto.setRow(MAX_ROW);
            List<RoomDto> rooms = getOweRoomInfo(queryStatisticsDto);
            // todo 计算 房屋欠费实收数据
            JSONArray datas = computeRoomOweReceivedFee(rooms,queryStatisticsDto);
            appendData(datas, sheet, dictDtos,(page - 1) * MAX_ROW);
        }
        return workbook;
    }

    /**
     * 封装数据到Excel中
     * @param datas
     * @param sheet
     * @param dictDtos
     */
    private void appendData(JSONArray datas, Sheet sheet, List<DictDto> dictDtos,int step) {
        Row row = null;
        JSONObject dataObj = null;
        String receivedFee = "";
        String feeTypeCd = "";
        JSONArray feeTypeCdData = null;
        String feeTypeCdValue = "";
        JSONObject feeTypeData = null;
        for (int roomIndex = 0; roomIndex < datas.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = datas.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(dataObj.getString("roomName"));
            row.createCell(1).setCellValue(dataObj.getString("ownerName")+"("+dataObj.getString("link")+")");
            row.createCell(2).setCellValue(dataObj.getString("oweFee"));

            for (int dictIndex = 0; dictIndex < dictDtos.size(); dictIndex++) {
                feeTypeCd = "oweFee"+dictDtos.get(dictIndex).getStatusCd();
                if(!dataObj.containsKey(feeTypeCd)){
                    row.createCell(3 + dictIndex).setCellValue(0);
                    continue;
                }
                feeTypeCdData = dataObj.getJSONArray(feeTypeCd);
                if(feeTypeCdData == null || feeTypeCdData.size() < 1){
                    row.createCell(3 + dictIndex).setCellValue(0);
                    continue;
                }
                feeTypeCdValue = "";
                for(int feeTypeIndex = 0;feeTypeIndex < feeTypeCdData.size(); feeTypeIndex++) {
                    feeTypeData = feeTypeCdData.getJSONObject(feeTypeIndex);
                    feeTypeCdValue += (feeTypeData.getString("feeName")+"("+feeTypeData.getString("endTime")+"~"+feeTypeData.getString("deadlineTime"))+")="+feeTypeData.getString("amountOwed");
                    feeTypeCdValue +="\r\n";
                }
                row.createCell(3 + dictIndex).setCellValue(feeTypeCdValue);
            }
        }

    }

    public long getOweRoomCount(QueryStatisticsDto queryStatisticsDto) {
        RoomDto roomDto = new RoomDto();
        roomDto.setFloorId(queryStatisticsDto.getFloorId());
        roomDto.setCommunityId(queryStatisticsDto.getCommunityId());
        roomDto.setOwnerName(queryStatisticsDto.getOwnerName());
        roomDto.setFloorId(queryStatisticsDto.getFloorId());
        roomDto.setLink(queryStatisticsDto.getLink());
        roomDto.setStartDate(queryStatisticsDto.getStartDate());
        roomDto.setEndDate(queryStatisticsDto.getEndDate());
        addRoomNumCondition(queryStatisticsDto, roomDto);
        return baseDataStatisticsInnerServiceSMOImpl.getOweRoomCount(roomDto);
    }

    public List<RoomDto> getOweRoomInfo(QueryStatisticsDto queryStatisticsDto) {
        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(queryStatisticsDto.getCommunityId());
        roomDto.setFloorId(queryStatisticsDto.getFloorId());
        roomDto.setPage(queryStatisticsDto.getPage());
        roomDto.setRow(queryStatisticsDto.getRow());
        roomDto.setOwnerName(queryStatisticsDto.getOwnerName());
        roomDto.setFloorId(queryStatisticsDto.getFloorId());
        roomDto.setLink(queryStatisticsDto.getLink());
        roomDto.setStartDate(queryStatisticsDto.getStartDate());
        roomDto.setEndDate(queryStatisticsDto.getEndDate());
        addRoomNumCondition(queryStatisticsDto, roomDto);
        return baseDataStatisticsInnerServiceSMOImpl.getOweRoomInfo(roomDto);
    }

    /**
     * roomNum 拆分 查询房屋信息
     *
     * @param queryStatisticsDto
     * @param roomDto
     */
    private void addRoomNumCondition(QueryStatisticsDto queryStatisticsDto, RoomDto roomDto) {
        if (StringUtil.isEmpty(queryStatisticsDto.getObjName())) {
            return;
        }
        if (!queryStatisticsDto.getObjName().contains("-")) {
            roomDto.setRoomNumLike(queryStatisticsDto.getObjName());
            return;
        }
        String[] objNames = queryStatisticsDto.getObjName().split("-");
        if (objNames.length == 2) {
            roomDto.setFloorNum(objNames[0]);
            roomDto.setUnitNum("0");
            roomDto.setRoomNum(objNames[1]);
            return;
        }
        objNames = queryStatisticsDto.getObjName().split("-", 3);
        if (objNames.length == 3) {
            roomDto.setFloorNum(objNames[0]);
            roomDto.setUnitNum(objNames[1]);
            roomDto.setRoomNum(objNames[2]);
        }

    }

    /**
     * 计算房屋欠费 实收费用
     *
     * @param rooms
     * @return
     */
    private JSONArray computeRoomOweReceivedFee(List<RoomDto> rooms, QueryStatisticsDto queryStatisticsDto) {
        if (rooms == null || rooms.size() < 1) {
            return new JSONArray();
        }

        JSONArray datas = new JSONArray();
        JSONObject data = null;

        List<String> objIds = new ArrayList<>();
        for (RoomDto roomDto : rooms) {
            objIds.add(roomDto.getRoomId());
            data = new JSONObject();
            data.put("roomId", roomDto.getRoomId());
            data.put("roomName", roomDto.getFloorNum() + "-" + roomDto.getUnitNum() + "-" + roomDto.getRoomNum());
            data.put("ownerName", roomDto.getOwnerName());
            data.put("ownerId", roomDto.getOwnerId());
            data.put("link", roomDto.getLink());
            datas.add(data);
        }

        queryStatisticsDto.setObjIds(objIds.toArray(new String[objIds.size()]));
        //todo 查询房屋 欠费信息 返回 payerObjId, feeTypeCd,feeName,endTime,deadlineTime,amountOwed
        List<Map> infos = reportFeeStatisticsInnerServiceSMOImpl.getObjOweFee(queryStatisticsDto);

        if (infos == null || infos.size() < 1) {
            return datas;
        }

        //todo 清洗数据 将数据转变成 map roomId feeTypeCd->array
        // todo 讲 payerObjId, feeTypeCd,feeName,endTime,deadlineTime,amountOwed 转换为 按payerObjId 纵向转换
        // todo  nInfo.put(info.get("payerObjId").toString(), info.get("payerObjId").toString());
        // todo  nInfo.put(info.get("feeTypeCd").toString(), tmpInfos);
        infos = washInfos(infos);

        BigDecimal oweFee = null;
        List<Map> itemFees = null;
        String feeTypeCd = "";

        DictDto dictDto = new DictDto();
        dictDto.setTableName("pay_fee_config");
        dictDto.setTableColumns("fee_type_cd_show");
        List<DictDto> dictDtos = dictV1InnerServiceSMOImpl.queryDicts(dictDto);

        // todo 根据房屋ID 和payerObjId 比较 合并数据，讲费用大类 横向 放入 data中，
        // todo 并且计算每个 房屋 费用大类的欠费 和房屋的总欠费
        for (int dataIndex = 0; dataIndex < datas.size(); dataIndex++) {
            oweFee = new BigDecimal(0.00);
            data = datas.getJSONObject(dataIndex);
            //todo 这里循环费用大类
            for (Map info : infos) {
                if (!data.getString("roomId").equals(info.get("payerObjId"))) {
                    continue;
                }
//                if(!info.containsKey("feeTypeCd")){
//                    continue;
//                }
                for (DictDto tDict : dictDtos) {
                    //feeTypeCd = info.get("feeTypeCd").toString();
                    feeTypeCd = tDict.getStatusCd();
                    if (!info.containsKey(feeTypeCd)) {
                        continue;
                    }
                    oweFee = oweFee.add(new BigDecimal(info.get(feeTypeCd + "oweFee").toString()));
                    data.put("oweFee" + feeTypeCd, info.get(feeTypeCd));
                }
            }
            data.put("oweFee", oweFee.doubleValue());
        }

        return datas;
    }

    /**
     * //todo 清洗数据 将数据转变成 map roomId feeTypeCd->array
     * // todo 讲 payerObjId, feeTypeCd,feeName,endTime,deadlineTime,amountOwed 转换为 按payerObjId 纵向转换
     * // todo  nInfo.put(info.get("feeTypeCd").toString(), tmpInfos);
     *
     * @param infos
     * @return
     */
    private List<Map> washInfos(List<Map> infos) {
        List<Map> newInfos = new ArrayList<>();
        for (Map info : infos) {
            generatorNewInfo(newInfos, info);
        }

        List<Map> tmpInfos = null;
        Map dInfo = null;
        for (Map nInfo : newInfos) {
            for (Map info : infos) {
                if (!nInfo.get("payerObjId").equals(info.get("payerObjId"))) {
                    continue;
                }
                tmpInfos = getTmpInfos(nInfo, info);
                if (tmpInfos == null) {
                    continue;
                }
                //todo 深拷贝
                dInfo = new HashMap();
                dInfo.putAll(info);
                tmpInfos.add(dInfo);
                //计算单项 欠费金额
                computeOweAmount(tmpInfos, info.get("feeTypeCd").toString(), nInfo);
                nInfo.put(info.get("feeTypeCd").toString(), tmpInfos);
            }
        }

        return newInfos;

    }

    /**
     * 计算每个费用大类的 欠费
     *
     * @param tmpInfos
     * @param feeTypeCd
     * @param nInfo
     */
    private void computeOweAmount(List<Map> tmpInfos, String feeTypeCd, Map nInfo) {
        if (tmpInfos == null || tmpInfos.size() < 1) {
            nInfo.put(feeTypeCd + "oweFee", 0.0);
            return;
        }
        BigDecimal oweFee = new BigDecimal(0.0);
        for (Map tInfo : tmpInfos) {
            oweFee = oweFee.add(new BigDecimal(tInfo.get("amountOwed").toString()));
        }
        oweFee = oweFee.setScale(2, BigDecimal.ROUND_HALF_UP);
        nInfo.put(feeTypeCd + "oweFee", oweFee.doubleValue());
    }

    private List<Map> getTmpInfos(Map nInfo, Map info) {
        if (!info.containsKey("feeTypeCd")) {
            return null;
        }
        String feeTypeCd = info.get("feeTypeCd").toString();
        if (nInfo.containsKey(feeTypeCd)) {
            return (List<Map>) nInfo.get(feeTypeCd);
        }

        return new ArrayList<>();
    }

    /**
     * 查询 新数据对方
     *
     * @param newInfos
     * @param info
     * @return
     */
    private void generatorNewInfo(List<Map> newInfos, Map info) {
        if (newInfos.size() < 1) {
            newInfos.add(info);
            return;
        }

        for (Map newInfo : newInfos) {
            if (newInfo.get("payerObjId").equals(info.get("payerObjId"))) {
                return;
            }
        }

        newInfos.add(info);
    }
}

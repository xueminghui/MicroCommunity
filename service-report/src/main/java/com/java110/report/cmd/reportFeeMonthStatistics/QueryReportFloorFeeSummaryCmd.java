package com.java110.report.cmd.reportFeeMonthStatistics;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.report.QueryStatisticsDto;
import com.java110.report.statistics.IFeeStatistics;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 楼栋费用统计
 */
@Java110Cmd(serviceCode = "reportFeeMonthStatistics.queryReportFloorFeeSummary")
public class QueryReportFloorFeeSummaryCmd extends Cmd {

    @Autowired
    private IFeeStatistics feeStatisticsImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        Assert.hasKeyAndValue(reqJson, "startDate", "未包含开始日期");
        Assert.hasKeyAndValue(reqJson, "endDate", "未包含结束日期");
        Assert.hasKeyAndValue(reqJson, "communityId", "未包含小区信息");
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        QueryStatisticsDto queryStatisticsDto = new QueryStatisticsDto();
        queryStatisticsDto.setCommunityId(reqJson.getString("communityId"));
        queryStatisticsDto.setStartDate(reqJson.getString("startDate"));
        queryStatisticsDto.setEndDate(reqJson.getString("endDate"));
        if(reqJson.containsKey("endDate") && !reqJson.getString("endDate").contains(":")) {
            queryStatisticsDto.setEndDate(reqJson.getString("endDate") + " 23:59:59");
        }
        queryStatisticsDto.setFeeTypeCd(reqJson.getString("feeTypeCd"));
        if(reqJson.containsKey("configIds")){
            queryStatisticsDto.setConfigIds(reqJson.getString("configIds").split(","));
        }

        List<Map> datas = feeStatisticsImpl.getFloorFeeSummary(queryStatisticsDto);

        if(datas == null || datas.size() < 1){
            context.setResponseEntity(ResultVo.createResponseEntity(datas));
            return;
        }
        BigDecimal feeRoomCountDec = null;
        BigDecimal oweRoomCountDec = null;
        BigDecimal feeRoomRate = null;
        BigDecimal curReceivedFee = null;
        BigDecimal curReceivableFee = null;
        for(Map data:datas){
            //todo 计算 户收费率
            if(Double.parseDouble(data.get("feeRoomCount").toString())>0){
                feeRoomCountDec = new BigDecimal(Double.parseDouble(data.get("feeRoomCount").toString()));
                oweRoomCountDec = new BigDecimal(Double.parseDouble(data.get("oweRoomCount").toString()));
                feeRoomRate = feeRoomCountDec.subtract(oweRoomCountDec).divide(feeRoomCountDec,4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2,BigDecimal.ROUND_HALF_UP);
                data.put("feeRoomRate",feeRoomRate.doubleValue());
            }else{
                data.put("feeRoomRate",0.0);
            }

            //todo 计算 收费率
            curReceivedFee = new BigDecimal(Double.parseDouble(data.get("curReceivedFee").toString()));
            curReceivableFee = new BigDecimal(Double.parseDouble(data.get("curReceivableFee").toString()));

            if(curReceivableFee.doubleValue()> 0){
                feeRoomRate = curReceivedFee.divide(curReceivableFee,4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(2,BigDecimal.ROUND_HALF_UP);
                data.put("feeRate",feeRoomRate.doubleValue());
            }else{
                data.put("feeRate",0.0);
            }
        }

        context.setResponseEntity(ResultVo.createResponseEntity(datas));
    }
}


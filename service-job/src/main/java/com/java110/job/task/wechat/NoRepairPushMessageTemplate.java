package com.java110.job.task.wechat;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.factory.CommunitySettingFactory;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.core.log.LoggerFactory;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.log.LogSystemErrorDto;
import com.java110.dto.org.OrgDto;
import com.java110.dto.org.OrgStaffRelDto;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairSettingDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.dto.task.TaskDto;
import com.java110.dto.user.UserDto;
import com.java110.intf.community.*;
import com.java110.intf.user.*;
import com.java110.job.msgNotify.IMsgNotify;
import com.java110.job.msgNotify.MsgNotifyFactory;
import com.java110.job.quartz.TaskSystemQuartz;
import com.java110.po.log.LogSystemErrorPo;
import com.java110.service.smo.ISaveSystemErrorSMO;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.MappingConstant;
import com.java110.utils.util.*;
import com.java110.vo.api.staff.ApiStaffDataVo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: MicroCommunity
 * @description: 定时任务 寻找派单指定时间以上无维修的单，发送通知给上级领导（经理）
 * @author: wuxw
 * @create: 2020-06-15 13:35
 **/
@Component
public class NoRepairPushMessageTemplate extends TaskSystemQuartz {

    private static Logger logger = LoggerFactory.getLogger(NoRepairPushMessageTemplate.class);



    @Autowired
    private ISaveSystemErrorSMO saveSystemErrorSMOImpl;

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMOImpl;

    @Autowired
    private IUserV1InnerServiceSMO userV1InnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMOImpl;

    @Autowired
    private IOrgStaffRelInnerServiceSMO iOrgStaffRelInnerServiceSMO;

    @Autowired
    private IOrgV1InnerServiceSMO orgV1InnerServiceSMOImpl;


    //键(派修单超时时间)
    public static final String REPAIR_OVERTIME_SETTING = "REPAIR_OVERTIME_SETTING";

    @Override
    protected void process(TaskDto taskDto) {
        logger.debug("开始执行微信模板信息推送" + taskDto.toString());
        //创建连接池
//        publicWeChatPushMessageTemplateJava110ThreadPoolFactory = Java110ThreadPoolFactory.getInstance().createThreadPool(DEFAULT_THREAD_NUM);
        // 获取小区
        List<CommunityDto> communityDtos = getAllCommunity();
        for (CommunityDto communityDto : communityDtos) {
            try {
                noticeUnDoRepairs(taskDto, communityDto);
            } catch (Exception e) {
                LogSystemErrorPo logSystemErrorPo = new LogSystemErrorPo();
                logSystemErrorPo.setErrId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_errId));
                logSystemErrorPo.setErrType(LogSystemErrorDto.ERR_TYPE_NOTICE);
                logSystemErrorPo.setMsg(ExceptionUtil.getStackTrace(e));
                saveSystemErrorSMOImpl.saveLog(logSystemErrorPo);
                logger.error("推送消息失败", e);
            }
        }
    }



    /**
     * 检测报修待办的订单，通知上级
     * @param taskDto
     * @param communityDto
     */
    public void noticeUnDoRepairs(TaskDto taskDto, CommunityDto communityDto) {
        RepairDto repairDto = new RepairDto();
        repairDto.setCommunityId(communityDto.getCommunityId());
        repairDto.setState(RepairDto.STATE_TAKING);
//        repairDto.setRepairWay(RepairDto.REPAIR_WAY_TRAINING);
//        repairDto.setCreateTime(new Data());
        //查询需要通知的订单
        List<RepairDto> repairDtos = repairInnerServiceSMOImpl.queryRepairs(repairDto);

        for (RepairDto tmpRepairDto : repairDtos) {
            // 检查派单时间是否超过了设置的派单超时时间
            //获取当前时间
            Date nowTime = new Date();
            //默认30分钟
            String repairTimeString = CommunitySettingFactory.getValue(communityDto.getCommunityId(), REPAIR_OVERTIME_SETTING);
            if (StringUtil.isEmpty(repairTimeString)) {
                repairTimeString = MappingCache.getValue(MappingConstant.REPAIR_DOMAIN,REPAIR_OVERTIME_SETTING);
            }
            long repairTime= Long.parseLong(Objects.requireNonNull(repairTimeString));
            long secRepairTime = repairTime * 2;
            long thirRepairTime = repairTime * 3;
            AtomicInteger pushTime = new AtomicInteger();
            // 第一次推送到小组经理级别
            pushTime.set(3);
            pushToUper(communityDto, tmpRepairDto, pushTime.get());
//            if (repairTime > 0 && (nowTime.getTime() - tmpRepairDto.getCreateTime().getTime()) >= (repairTime * 1000 * 60)  && (nowTime.getTime() - tmpRepairDto.getCreateTime().getTime()) < ((repairTime + 1.5) * 1000 * 60)) { //如果评价开始时间距离当前时间超过了配置时间，查询订单详情
//                pushTime.set(1);
//                pushToUper(communityDto, tmpRepairDto, pushTime.get());
//
//            }
//            // 第二次推送到区域经理级别
//            if (repairTime > 0 && (nowTime.getTime() - tmpRepairDto.getCreateTime().getTime()) >= (secRepairTime * 1000 * 60) && (nowTime.getTime() - tmpRepairDto.getCreateTime().getTime()) < ((secRepairTime + 1.5) * 1000 * 60)) { //如果评价开始时间距离当前时间超过了配置时间，查询订单详情
//                pushTime.set(2);
//                pushToUper(communityDto, tmpRepairDto, pushTime.get());
//            }
//
//            // 第三次推送到更上级经理级别
//            if (repairTime > 0 && (nowTime.getTime() - tmpRepairDto.getCreateTime().getTime()) >= (thirRepairTime * 1000 * 60) && (nowTime.getTime() - tmpRepairDto.getCreateTime().getTime()) < ((thirRepairTime + 1.5) * 1000 * 60)) { //如果评价开始时间距离当前时间超过了配置时间，查询订单详情
//                pushTime.set(3);
//                pushToUper(communityDto, tmpRepairDto, pushTime.get());
//            }

        }
    }

    private void pushToUper(CommunityDto communityDto, RepairDto tmpRepairDto, int pushTime) {
        RepairUserDto repairUserDto = new RepairUserDto();
        repairUserDto.setRepairId(tmpRepairDto.getRepairId());

        int count = repairUserInnerServiceSMOImpl.queryRepairUsersCount(repairUserDto);

        List<RepairUserDto> repairUserDtos = null;
        if (count <= 0) {
            return;
        }
        repairUserDtos = repairUserInnerServiceSMOImpl.queryRepairUsers(repairUserDto);
        boolean needNotic = false;
        long latestCreateTime = Long.MIN_VALUE;

        // 最后一个派单的维修工
        RepairUserDto lastDispatchDto = null;
        for (RepairUserDto tempDto : repairUserDtos) {
            if (!Objects.equals(tempDto.getState(), RepairUserDto.STATE_DISPATCH) || !Objects.equals(tempDto.getState(),RepairUserDto.STATE_TRANSFER ) || !Objects.equals(tempDto.getState(),RepairUserDto.STATE_SUBMIT )|| !Objects.equals(tempDto.getState(),RepairUserDto.STATE_DOING)) {
                needNotic = true;
            }
            long createTime = tempDto.getStartTime().getTime();
            if(Objects.equals(tempDto.getState(), RepairUserDto.STATE_DISPATCH) || Objects.equals(tempDto.getState(),RepairUserDto.STATE_TRANSFER ) || Objects.equals(tempDto.getState(),RepairUserDto.STATE_DOING)){
                if (createTime > latestCreateTime) {
                    latestCreateTime = createTime;
                    lastDispatchDto = tempDto;
                }
            }

        }

        // 不需要通知的跳过
        if(!needNotic || null == lastDispatchDto || null == lastDispatchDto.getStaffId() ) {
            return;
        }


        // 查询指派的维修工的领导
        // 发送通知，超过时间大于超时时间，发送通知给部门经理
        //超过两倍的超时时间，发送通知给区域经理

        UserDto userDto1 = new UserDto();
        userDto1.setUserId(lastDispatchDto.getStaffId());
        userDto1.setPage(1);
        userDto1.setRow(1);
        List<UserDto> userDtos = userV1InnerServiceSMOImpl.queryUsers(userDto1);
        Assert.listOnlyOne(userDtos, "用户不存在");

        OrgStaffRelDto orgStaffRelDto = new OrgStaffRelDto();
        orgStaffRelDto.setStaffId(lastDispatchDto.getStaffId());
        List<OrgStaffRelDto> orgStaffRelDtos = iOrgStaffRelInnerServiceSMO.queryOrgInfoByStaffIdsNew(orgStaffRelDto);
        ApiStaffDataVo pushStaffDataVo = null;
        if (orgStaffRelDtos.size() > 0) {
            for (OrgStaffRelDto orgStaffRelDto1 : orgStaffRelDtos) {

                UserDto staffDto1 = new UserDto();
                String curOrgId = orgStaffRelDto1.getOrgId();
                if (pushTime == 2 && Integer.parseInt(orgStaffRelDto1.getOrgLevel()) >= 2) {
                    curOrgId = orgStaffRelDto1.getParentOrgId();
                }
                if (pushTime == 3 && Integer.parseInt(orgStaffRelDto1.getOrgLevel()) >= 3) {
                    OrgDto orgDto1 = new OrgDto();
                    orgDto1.setOrgId(orgStaffRelDto1.getParentOrgId());
                    List<OrgDto> orgDtoList = orgV1InnerServiceSMOImpl.queryOrgs(orgDto1);
                    if (orgDtoList.size() <= 0) {
                        curOrgId = "-1";
                    }else {
                        curOrgId = orgDtoList.get(0).getParentOrgId();
                    }
                }
                if (Objects.equals(curOrgId,"-1" )) {
                    continue;
                }
                staffDto1.setOrgId(curOrgId);
                int countU = userInnerServiceSMOImpl.getStaffCount(staffDto1);
                if (countU > 0) {
                    List<ApiStaffDataVo> staffList = BeanConvertUtil.covertBeanList(userInnerServiceSMOImpl.getStaffs(staffDto1), ApiStaffDataVo.class);
                    for (ApiStaffDataVo apiStaffDataVo : staffList) {
                        //岗位,普通员工 1000 部门经理 2000 部门副经理 3000 部门组长 4000 分公司总经理 5000 分公司副总经理 6000 总经理助理 7000 总公司总经理 8000 总公司副总经理 9000"
                        if ((Objects.equals(apiStaffDataVo.getRelCd(), "2000") || Objects.equals(apiStaffDataVo.getRelCd(), "4000") )&& !Objects.equals(apiStaffDataVo.getStaffId(), lastDispatchDto.getStaffId())) {
                            pushStaffDataVo = apiStaffDataVo;
                        }

                    }
                }
            }
        }
        if (null != pushStaffDataVo) pushMsgToStaff(tmpRepairDto, pushStaffDataVo, communityDto);

    }


    private void pushMsgToStaff( RepairDto repairDto,ApiStaffDataVo apiStaffDataVo,CommunityDto communityDto) {
//        RepairUserDto repairUserDto = new RepairUserDto();
//        repairUserDto.setRuId(businessRepairUser.getString("ruId"));
//        List<RepairUserDto> repairUserDtos = repairUserInnerServiceSMO.queryRepairUsers(repairUserDto);
//
//        String state = repairUserDtos.get(0).getState();
//        if (RepairUserDto.STATE_SUBMIT.equals(state)) {
//            return;
//        }
//        //获取报修id
        String repairId = repairDto.getRepairId();
//        RepairDto repairDto = new RepairDto();
//        repairDto.setRepairId(repairId);
//        List<RepairDto> repairDtos = repairInnerServiceSMO.queryRepairs(repairDto);
        IMsgNotify msgNotify = null;
        if(RepairSettingDto.NOTIFY_WAY_SMS.equals(repairDto.getNotifyWay())) {
            msgNotify = MsgNotifyFactory.getMsgNotify(MsgNotifyFactory.NOTIFY_WAY_ALI);
        }else if(RepairSettingDto.NOTIFY_WAY_WECHAT.equals(repairDto.getNotifyWay())){
            msgNotify = MsgNotifyFactory.getMsgNotify(MsgNotifyFactory.NOTIFY_WAY_WECHAT);
        }else{
            return;
        }
//获取员工处理状态(10001 处理中；10002 结单；10003 退单；10004 转单；10005 提交；10006 已派单；10007 已评价；10008 已回访；10009 待支付；11000 待评价；12000 已支付；12001 暂停)
        //查询报修状态(1000 未派单；1100 接单；1200 退单；1300 转单；1400 申请支付；1500 支付失败；1700 待评价；1800 电话回访；1900 办理完成；2000 未办理结单)
        String repairState = repairDto.getState();
        //获取联系人姓名
        String repairName = repairDto.getRepairName();
        //获取联系人电话
        String tel = repairDto.getTel();
        //获取位置信息
        String repairObjName = repairDto.getRepairObjName();
        //报修对象ID
        String repairObjId = repairDto.getRepairObjId();
        //获取报修内容
        String context = repairDto.getContext();
        //获取派单时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(repairDto.getCreateTime());
        //获取小区id
        String communityId = repairDto.getCommunityId();

        JSONObject paramIn = new JSONObject();

        paramIn.put("repairName", repairName);
        paramIn.put("repairObjName", repairObjName);
        paramIn.put("tel", tel);
        paramIn.put("communityId", communityId);
        paramIn.put("context", context);
        paramIn.put("time", time);
        paramIn.put("repairObjId", repairObjId);
        paramIn.put("repairId", repairId);
        //给维修师傅推送信息
        sendStaffMsg(paramIn, communityDto,msgNotify);



    }


    /**
     * 派单给维修师傅推送信息
     *
     * @param paramIn
     * @param communityDto
     */
    private void sendStaffMsg(JSONObject paramIn, CommunityDto communityDto,IMsgNotify msgNotify) {
        JSONObject content = new JSONObject();
        content.put("repairId", paramIn.getString("repairId"));
        content.put("repairName", paramIn.getString("repairName"));
        content.put("tel", paramIn.getString("tel"));
        content.put("time", paramIn.getString("time"));
        String wechatUrl = MappingCache.getValue(MappingConstant.URL_DOMAIN, "STAFF_WECHAT_URL");
        content.put("url", wechatUrl);
        //获取具体位置
        String address = "";
        if (communityDto.getName().equals(paramIn.getString("repairObjName"))) {
            address = paramIn.getString("repairObjName");
        } else {
            address = communityDto.getName() + paramIn.getString("repairObjName");
        }
        content.put("address", address);
        msgNotify.sendOverTimeRepairStaffMsg(communityDto.getCommunityId(), paramIn.getString("staffId"), content);
    }

//    private void doSaveLog(Date startDate, Date endDate, String serviceCode, String reqJson, ResponseEntity<String> responseEntity, String userId) {
//        try {
//            TransactionLogPo transactionLogPo = new TransactionLogPo();
//            transactionLogPo.setAppId(AppDto.OWNER_WECHAT_PAY);
//            transactionLogPo.setCostTime((endDate.getTime() - startDate.getTime()) + "");
//            transactionLogPo.setIp("");
//            transactionLogPo.setServiceCode(serviceCode);
//            transactionLogPo.setSrcIp("127.0.0.1");
//            transactionLogPo.setState(responseEntity.getStatusCode() != HttpStatus.OK ? "F" : "S");
//            transactionLogPo.setTimestamp(DateUtil.getCurrentDate().getTime() + "");
//            transactionLogPo.setUserId(userId);
//            transactionLogPo.setTransactionId(userId);
//            transactionLogPo.setRequestHeader("");
//            transactionLogPo.setResponseHeader(responseEntity.getHeaders().toSingleValueMap().toString());
//            transactionLogPo.setRequestMessage(reqJson);
//            transactionLogPo.setResponseMessage(responseEntity.getBody());
//            saveTransactionLogSMOImpl.saveLog(transactionLogPo);
//        } catch (Exception e) {
//            logger.error("存日志失败", e);
//        }
//    }
}

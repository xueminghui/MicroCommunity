package com.java110.job.task.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Synchronized;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.core.factory.Java110ThreadPoolFactory;
import com.java110.core.factory.WechatFactory;
import com.java110.core.log.LoggerFactory;
import com.java110.core.smo.ISaveTransactionLogSMO;
import com.java110.dto.app.AppDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.file.FileRelDto;
import com.java110.dto.log.LogSystemErrorDto;
import com.java110.dto.notice.NoticeDto;
import com.java110.dto.org.OrgDto;
import com.java110.dto.org.OrgStaffRelDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.privilege.BasePrivilegeDto;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairSettingDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.dto.room.RoomDto;
import com.java110.dto.system.Business;
import com.java110.dto.task.TaskDto;
import com.java110.dto.user.UserDto;
import com.java110.dto.wechat.*;
import com.java110.intf.community.*;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.*;
import com.java110.job.msgNotify.IMsgNotify;
import com.java110.job.msgNotify.MsgNotifyFactory;
import com.java110.job.quartz.TaskSystemQuartz;
import com.java110.po.log.LogSystemErrorPo;
import com.java110.po.log.TransactionLogPo;
import com.java110.po.wechat.WechatSubscribePo;
import com.java110.service.smo.ISaveSystemErrorSMO;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.cache.UrlCache;
import com.java110.utils.constant.MappingConstant;
import com.java110.utils.constant.WechatConstant;
import com.java110.utils.util.*;
import com.java110.vo.api.staff.ApiStaffDataVo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.java110.utils.util.DateUtil.compareDate;

/**
 * @program: MicroCommunity
 * @description: 定时任务 寻找派单指定时间以上无维修的单，发送通知给上级领导（经理）
 * @author: wuxw
 * @create: 2020-06-15 13:35
 **/
@Component
public class NoRepairPushMessageTemplate extends TaskSystemQuartz {

    private static Logger logger = LoggerFactory.getLogger(NoRepairPushMessageTemplate.class);

    public static final int DEFAULT_THREAD_NUM = 20;

    public static final int DEFAULT_SUBSCRIBE_PERSON = 100;

    public static final int DEFAULT_QUERY_APP_OWNER_COUNT = 50;
    public static final int DEFAULT_QUERY_TIME = 30;

//    @Autowired
//    private INoticeInnerServiceSMO noticeInnerServiceSMOImpl;
//
//    @Autowired
//    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;
//
//    @Autowired
//    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;
//
//    @Autowired
//    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMOImpl;
//
//    @Autowired
//    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;
//
//    @Autowired
//    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;

    @Autowired
    private ISaveTransactionLogSMO saveTransactionLogSMOImpl;

//    @Autowired
//    private RestTemplate outRestTemplate;

    @Autowired
    private ISaveSystemErrorSMO saveSystemErrorSMOImpl;

//    @Autowired
//    private IWechatSubscribeV1InnerServiceSMO wechatSubscribeV1InnerServiceSMOImpl;
//
//    @Autowired
//    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;
//
//    @Autowired
//    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMO;

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMOImpl;

    @Autowired
    private IUserV1InnerServiceSMO userV1InnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMOImpl;

    @Autowired
    private IOrgV1InnerServiceSMO orgV1InnerServiceSMOImpl;

    @Autowired
    private IMenuInnerServiceSMO menuInnerServiceSMOImpl;

    @Autowired
    private IOrgStaffRelInnerServiceSMO iOrgStaffRelInnerServiceSMO;


    //模板信息推送地址
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    private static String getUser = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN";

//    private Java110ThreadPoolFactory<NoRepairPushMessageTemplate> publicWeChatPushMessageTemplateJava110ThreadPoolFactory = null;

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
            // 检查派单时间是否超过了30分钟
            //获取当前时间
            Date nowTime = new Date();
            //默认30分钟
            int autoEvaluateHour = 30;
            if ((nowTime.getTime() - tmpRepairDto.getCreateTime().getTime()) > (autoEvaluateHour * 1000 * 60)) { //如果评价开始时间距离当前时间超过了配置时间，查询订单详情

                RepairUserDto repairUserDto = new RepairUserDto();
                repairUserDto.setRepairId(tmpRepairDto.getRepairId());

                int count = repairUserInnerServiceSMOImpl.queryRepairUsersCount(repairUserDto);

                List<RepairUserDto> repairUserDtos = null;
                if (count <= 0) {
                    continue;
                }
                repairUserDtos = repairUserInnerServiceSMOImpl.queryRepairUsers(repairUserDto);
//                    boolean result = checkState(repairUserDtos, RepairUserDto.STATE_DISPATCH, RepairUserDto.STATE_DOING);
                RepairUserDto lastDispatchDto = null; // 最后一个派单的维修工
                Boolean needNotic = false;
                long latestCreateTime = Long.MIN_VALUE;

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
                    continue;
                }


                // 查询指派的维修工的领导
                // 发送通知，超过时间大于30分钟，发送通知给部门经理
                //超过60分钟，发送通知给区域经理
//
                // 判断是不是管理员，管理员反馈 物业 的所角色
                UserDto userDto1 = new UserDto();
                userDto1.setUserId(lastDispatchDto.getStaffId());
//                userDto1.setOrgId(lastDispatchDto.get);
                userDto1.setPage(1);
                userDto1.setRow(1);
                List<UserDto> userDtos = userV1InnerServiceSMOImpl.queryUsers(userDto1);
                Assert.listOnlyOne(userDtos, "用户不存在");

                OrgStaffRelDto orgStaffRelDto = new OrgStaffRelDto();
                orgStaffRelDto.setStaffId(lastDispatchDto.getStaffId());
                List<OrgStaffRelDto> orgStaffRelDtos = iOrgStaffRelInnerServiceSMO.queryOrgInfoByStaffIdsNew(orgStaffRelDto);
                if (orgStaffRelDtos.size() > 0) {
                    List<String> haveOrgList = new ArrayList<String>();
                    for (OrgStaffRelDto orgStaffRelDto1 : orgStaffRelDtos) {
                        OrgDto orgDto1 = new OrgDto();
                        orgDto1.setOrgId(orgStaffRelDto1.getOrgId());
//                        List<OrgDto> orgDtoList = orgV1InnerServiceSMOImpl.queryOrgs(orgDto1);
////                                findCompany(haveOrgList, orgDtoList);
//                        if (orgDtoList == null || orgDtoList.size() < 1) {
//                            return;
//                        }
//                        String orgId =  orgDtoList.get(0).getOrgId();
                        UserDto staffDto1 = new UserDto();
//                        staffDto1.setStaffId(lastDispatchDto.getStaffId());
                        staffDto1.setOrgId(orgStaffRelDto1.getOrgId());
                        int countU = userInnerServiceSMOImpl.getStaffCount(staffDto1);
                        List<ApiStaffDataVo> staffs = new ArrayList<>();
                        if (countU > 0) {
                            List<ApiStaffDataVo> staffList = BeanConvertUtil.covertBeanList(userInnerServiceSMOImpl.getStaffs(staffDto1), ApiStaffDataVo.class);
                            for (ApiStaffDataVo apiStaffDataVo : staffList) {
                                if ((Objects.equals(apiStaffDataVo.getRelCd(), "2000") || Objects.equals(apiStaffDataVo.getRelCd(), "4000") )&& !Objects.equals(apiStaffDataVo.getStaffId(), lastDispatchDto.getStaffId())) {
                                    pushMsgToStaff(tmpRepairDto,apiStaffDataVo, communityDto);
                                }

                            }
                        }
                    }
                }


            }


        }
    }


    private void pushMsgToStaff( RepairDto repairDto,ApiStaffDataVo apiStaffDataVo,CommunityDto communityDto) {
//        RepairUserDto repairUserDto = new RepairUserDto();
//        repairUserDto.setRuId(businessRepairUser.getString("ruId"));
//        List<RepairUserDto> repairUserDtos = repairUserInnerServiceSMO.queryRepairUsers(repairUserDto);
//        //获取员工处理状态(10001 处理中；10002 结单；10003 退单；10004 转单；10005 提交；10006 已派单；10007 已评价；10008 已回访；10009 待支付；11000 待评价；12000 已支付；12001 暂停)
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

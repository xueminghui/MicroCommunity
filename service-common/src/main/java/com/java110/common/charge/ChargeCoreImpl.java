package com.java110.common.charge;

import com.java110.common.smartMeter.ISmartMeterFactoryAdapt;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.account.AccountDto;
import com.java110.dto.chargeMachine.ChargeMachineDto;
import com.java110.dto.chargeMachineFactory.ChargeMachineFactoryDto;
import com.java110.dto.chargeMachineOrder.ChargeMachineOrderDto;
import com.java110.dto.chargeMachineOrder.NotifyChargeOrderDto;
import com.java110.dto.chargeMachineOrder.NotifyChargePortDto;
import com.java110.dto.chargeMachineOrderAcct.ChargeMachineOrderAcctDto;
import com.java110.dto.chargeMachinePort.ChargeMachinePortDto;
import com.java110.intf.acct.IAccountInnerServiceSMO;
import com.java110.intf.common.*;
import com.java110.po.accountDetail.AccountDetailPo;
import com.java110.po.chargeMachineOrder.ChargeMachineOrderPo;
import com.java110.po.chargeMachineOrderAcct.ChargeMachineOrderAcctPo;
import com.java110.po.chargeMachinePort.ChargeMachinePortPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.factory.ApplicationContextFactory;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 充电核心类
 */
@Service
public class ChargeCoreImpl implements IChargeCore {

    @Autowired
    private IAccountInnerServiceSMO accountInnerServiceSMOImpl;

    @Autowired
    private IChargeMachineFactoryV1InnerServiceSMO chargeMachineFactoryV1InnerServiceSMOImpl;

    @Autowired
    private IChargeMachineOrderV1InnerServiceSMO chargeMachineOrderV1InnerServiceSMOImpl;

    @Autowired
    private IChargeMachineV1InnerServiceSMO chargeMachineV1InnerServiceSMOImpl;

    @Autowired
    private IChargeMachinePortV1InnerServiceSMO chargeMachinePortV1InnerServiceSMOImpl;


    @Autowired
    private IChargeMachineOrderAcctV1InnerServiceSMO chargeMachineOrderAcctV1InnerServiceSMOImpl;


    @Override
    public ResultVo startCharge(ChargeMachineDto chargeMachineDto, ChargeMachinePortDto chargeMachinePortDto, String chargeType, double duration, String orderId) {

        ChargeMachineFactoryDto chargeMachineFactoryDto = new ChargeMachineFactoryDto();
        chargeMachineFactoryDto.setFactoryId(chargeMachineDto.getImplBean());
        List<ChargeMachineFactoryDto> chargeMachineFactoryDtos = chargeMachineFactoryV1InnerServiceSMOImpl.queryChargeMachineFactorys(chargeMachineFactoryDto);

        Assert.listOnlyOne(chargeMachineFactoryDtos, "充电桩厂家不存在");

        IChargeFactoryAdapt chargeFactoryAdapt = ApplicationContextFactory.getBean(chargeMachineFactoryDtos.get(0).getBeanImpl(), IChargeFactoryAdapt.class);
        if (chargeFactoryAdapt == null) {
            throw new CmdException("厂家接口未实现");
        }

        chargeMachinePortDto = chargeFactoryAdapt.getChargePortState(chargeMachineDto, chargeMachinePortDto);

        if (!ChargeMachinePortDto.STATE_FREE.equals(chargeMachinePortDto.getState())) {
            throw new IllegalArgumentException("充电插槽不是空闲状态");
        }

        return chargeFactoryAdapt.startCharge(chargeMachineDto, chargeMachinePortDto, chargeType, duration, orderId);
    }

    @Override
    public ResultVo stopCharge(ChargeMachineDto chargeMachineDto, ChargeMachinePortDto chargeMachinePortDto) {
        ChargeMachineFactoryDto chargeMachineFactoryDto = new ChargeMachineFactoryDto();
        chargeMachineFactoryDto.setFactoryId(chargeMachineDto.getImplBean());
        List<ChargeMachineFactoryDto> chargeMachineFactoryDtos = chargeMachineFactoryV1InnerServiceSMOImpl.queryChargeMachineFactorys(chargeMachineFactoryDto);

        Assert.listOnlyOne(chargeMachineFactoryDtos, "充电桩厂家不存在");

        IChargeFactoryAdapt chargeFactoryAdapt = ApplicationContextFactory.getBean(chargeMachineFactoryDtos.get(0).getBeanImpl(), IChargeFactoryAdapt.class);
        if (chargeFactoryAdapt == null) {
            throw new CmdException("厂家接口未实现");
        }

        ResultVo resultVo = chargeFactoryAdapt.stopCharge(chargeMachineDto, chargeMachinePortDto);
        if (resultVo.getCode() != ResultVo.CODE_OK) {
            return resultVo;
        }


        //订单退款
        returnOrderMoney(chargeMachineDto, chargeMachinePortDto, "用户手工结束");


        return resultVo;
    }

    /**
     * 订单退款
     *
     * @param chargeMachineDto
     * @param chargeMachinePortDto
     */
    private void returnOrderMoney(ChargeMachineDto chargeMachineDto, ChargeMachinePortDto chargeMachinePortDto, String remark) {
        // 退款
        ChargeMachineOrderDto chargeMachineOrderDto = new ChargeMachineOrderDto();
        chargeMachineOrderDto.setMachineId(chargeMachineDto.getMachineId());
        chargeMachineOrderDto.setPortId(chargeMachinePortDto.getPortId());
        chargeMachineOrderDto.setState(ChargeMachineOrderDto.STATE_DOING);
        List<ChargeMachineOrderDto> chargeMachineOrderDtos = chargeMachineOrderV1InnerServiceSMOImpl.queryChargeMachineOrders(chargeMachineOrderDto);

        if (chargeMachineOrderDtos == null || chargeMachineOrderDtos.size() < 1) {
            return;
        }

        String chargeHours = chargeMachineOrderDtos.get(0).getChargeHours();
        double cHours = Double.parseDouble(chargeHours);
        if (999 == cHours) {
            cHours = 10;
        }

        Date startTime = DateUtil.getDateFromStringA(chargeMachineOrderDtos.get(0).getStartTime());

        double usedHours = Math.ceil((DateUtil.getCurrentDate().getTime() - startTime.getTime()) / (60 * 60 * 1000.00));

        BigDecimal freeHours = new BigDecimal(cHours).subtract(new BigDecimal(usedHours));

        double returnMoney = freeHours.multiply(new BigDecimal(Double.parseDouble(chargeMachineDto.getDurationPrice()))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();


        ChargeMachineOrderPo chargeMachineOrderPo = new ChargeMachineOrderPo();
        chargeMachineOrderPo.setOrderId(chargeMachineOrderDtos.get(0).getOrderId());
        chargeMachineOrderPo.setRemark(remark);
        chargeMachineOrderPo.setState(ChargeMachineOrderDto.STATE_FINISH);
        BigDecimal amount = new BigDecimal(Double.parseDouble(chargeMachineOrderDtos.get(0).getAmount())).subtract(new BigDecimal(returnMoney)).setScale(2, BigDecimal.ROUND_HALF_UP);
        chargeMachineOrderPo.setAmount(amount.doubleValue() + "");
        chargeMachineOrderPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        chargeMachineOrderPo.setCommunityId(chargeMachineOrderDtos.get(0).getCommunityId());
        int flag = chargeMachineOrderV1InnerServiceSMOImpl.updateChargeMachineOrder(chargeMachineOrderPo);
        if (flag < 1) {
            throw new IllegalArgumentException("修改订单失败");
        }


        AccountDto accountDto = new AccountDto();
        accountDto.setAcctId(chargeMachineOrderDtos.get(0).getAcctDetailId());
        List<AccountDto> accountDtos = accountInnerServiceSMOImpl.queryAccounts(accountDto);

        AccountDetailPo accountDetailPo = new AccountDetailPo();
        accountDetailPo.setAcctId(accountDtos.get(0).getAcctId());
        accountDetailPo.setObjId(accountDtos.get(0).getObjId());
        accountDetailPo.setObjType(accountDtos.get(0).getObjType());
        accountDetailPo.setAmount(returnMoney + "");
        accountDetailPo.setDetailId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_detailId));
        accountInnerServiceSMOImpl.prestoreAccount(accountDetailPo);

        //充电表中加入退款金额

        ChargeMachineOrderAcctPo chargeMachineOrderAcctPo = new ChargeMachineOrderAcctPo();
        chargeMachineOrderAcctPo.setAcctDetailId(accountDetailPo.getDetailId());
        chargeMachineOrderAcctPo.setAmount((-1 * returnMoney) + "");

        chargeMachineOrderAcctPo.setCmoaId(GenerateCodeFactory.getGeneratorId("11"));
        chargeMachineOrderAcctPo.setOrderId(chargeMachineOrderDtos.get(0).getOrderId());
        chargeMachineOrderAcctPo.setAcctId(accountDtos.get(0).getAcctId());
        chargeMachineOrderAcctPo.setStartTime(chargeMachineOrderDtos.get(0).getStartTime());

        chargeMachineOrderAcctPo.setEndTime(chargeMachineOrderDtos.get(0).getEndTime());
        chargeMachineOrderAcctPo.setRemark("账户退款-" + remark);
        chargeMachineOrderAcctPo.setCommunityId(chargeMachineOrderDtos.get(0).getCommunityId());
        chargeMachineOrderAcctPo.setEnergy("0");

        chargeMachineOrderAcctV1InnerServiceSMOImpl.saveChargeMachineOrderAcct(chargeMachineOrderAcctPo);
    }

    @Override
    public ChargeMachinePortDto getChargePortState(ChargeMachineDto chargeMachineDto, ChargeMachinePortDto chargeMachinePortDto) {
        ChargeMachineFactoryDto chargeMachineFactoryDto = new ChargeMachineFactoryDto();
        chargeMachineFactoryDto.setFactoryId(chargeMachineDto.getImplBean());
        List<ChargeMachineFactoryDto> chargeMachineFactoryDtos = chargeMachineFactoryV1InnerServiceSMOImpl.queryChargeMachineFactorys(chargeMachineFactoryDto);

        Assert.listOnlyOne(chargeMachineFactoryDtos, "充电桩厂家不存在");

        IChargeFactoryAdapt chargeFactoryAdapt = ApplicationContextFactory.getBean(chargeMachineFactoryDtos.get(0).getBeanImpl(), IChargeFactoryAdapt.class);
        if (chargeFactoryAdapt == null) {
            throw new CmdException("厂家接口未实现");
        }

        return chargeFactoryAdapt.getChargePortState(chargeMachineDto, chargeMachinePortDto);
    }

    /**
     * 完成充电
     *
     * @param notifyChargeOrderDto
     * @return
     */
    @Override
    public ResultVo finishCharge(NotifyChargeOrderDto notifyChargeOrderDto) {

//        // todo 生成 充电订单
//        ChargeMachineOrderPo chargeMachineOrderPo = new ChargeMachineOrderPo();
//        chargeMachineOrderPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
//        chargeMachineOrderPo.setState(ChargeMachineOrderDto.STATE_FINISH);
//        chargeMachineOrderPo.setOrderId(notifyChargeOrderDto.getOrderId());
//        chargeMachineOrderV1InnerServiceSMOImpl.updateChargeMachineOrder(chargeMachineOrderPo);

        ChargeMachineDto chargeMachineDto = new ChargeMachineDto();
        chargeMachineDto.setMachineCode(notifyChargeOrderDto.getMachineCode());
        List<ChargeMachineDto> chargeMachineDtos = chargeMachineV1InnerServiceSMOImpl.queryChargeMachines(chargeMachineDto);

        //Assert.listOnlyOne(chargeMachineDtos, "充电桩 不存在");

        if(chargeMachineDtos == null || chargeMachineDtos.size() < 1){
            return new ResultVo(ResultVo.CODE_OK, "成功");
        }

        // todo 插槽是否空闲

        ChargeMachinePortDto chargeMachinePortDto = new ChargeMachinePortDto();
        chargeMachinePortDto.setMachineId(chargeMachineDtos.get(0).getMachineId());
        chargeMachinePortDto.setPortCode(notifyChargeOrderDto.getPortCode());
        chargeMachinePortDto.setState(ChargeMachinePortDto.STATE_WORKING);
        List<ChargeMachinePortDto> chargeMachinePortDtos = chargeMachinePortV1InnerServiceSMOImpl.queryChargeMachinePorts(chargeMachinePortDto);
        //Assert.listOnlyOne(chargeMachinePortDtos, "插槽空闲");
        if(chargeMachinePortDtos == null || chargeMachinePortDtos.size() < 1){
            return new ResultVo(ResultVo.CODE_OK, "成功");
        }

        ChargeMachinePortPo chargeMachinePortPo = new ChargeMachinePortPo();
        chargeMachinePortPo.setPortId(chargeMachinePortDtos.get(0).getPortId());
        chargeMachinePortPo.setState(ChargeMachinePortDto.STATE_FREE);
        chargeMachinePortV1InnerServiceSMOImpl.updateChargeMachinePort(chargeMachinePortPo);

        returnOrderMoney(chargeMachineDtos.get(0), chargeMachinePortDtos.get(0), notifyChargeOrderDto.getReason());

        return new ResultVo(ResultVo.CODE_OK, "成功");
    }

    @Override
    public ResponseEntity<String> heartbeat(NotifyChargeOrderDto notifyChargeOrderDto) {
        return new ResponseEntity<>("{\n" +
                "\"code\" : 200,\n" +
                "\"msg\" : \"success\"\n" +
                "}", HttpStatus.OK);
    }

    @Override
    public void queryChargeMachineState(List<ChargeMachineDto> chargeMachineDtos) {

        for(ChargeMachineDto chargeMachineDto : chargeMachineDtos) {
            try {
                ChargeMachineFactoryDto chargeMachineFactoryDto = new ChargeMachineFactoryDto();
                chargeMachineFactoryDto.setFactoryId(chargeMachineDto.getImplBean());
                List<ChargeMachineFactoryDto> chargeMachineFactoryDtos = chargeMachineFactoryV1InnerServiceSMOImpl.queryChargeMachineFactorys(chargeMachineFactoryDto);

                Assert.listOnlyOne(chargeMachineFactoryDtos, "充电桩厂家不存在");

                IChargeFactoryAdapt chargeFactoryAdapt = ApplicationContextFactory.getBean(chargeMachineFactoryDtos.get(0).getBeanImpl(), IChargeFactoryAdapt.class);
                if (chargeFactoryAdapt == null) {
                    throw new CmdException("厂家接口未实现");
                }
                chargeFactoryAdapt.queryChargeMachineState(chargeMachineDto);
            }catch (Exception e){
                e.printStackTrace();
                chargeMachineDto.setState(ChargeMachineDto.STATE_OFFLINE);
                chargeMachineDto.setStateName("离线");
            }
        }
    }
}
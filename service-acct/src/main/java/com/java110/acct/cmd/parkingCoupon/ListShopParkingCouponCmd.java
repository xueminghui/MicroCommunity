/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.acct.cmd.parkingCoupon;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.parkingCouponShop.ParkingCouponShopDto;
import com.java110.intf.acct.IParkingCouponShopV1InnerServiceSMO;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 类表述：查询
 * 服务编码：parkingCouponShop.listParkingCouponShop
 * 请求路劲：/app/parkingCouponShop.ListParkingCouponShop
 * add by 吴学文 at 2022-10-10 16:43:42 mail: 928255095@qq.com
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Java110Cmd(serviceCode = "parkingCoupon.listShopParkingCoupon")
public class ListShopParkingCouponCmd extends Cmd {

    private static Logger logger = LoggerFactory.getLogger(ListShopParkingCouponCmd.class);
    @Autowired
    private IParkingCouponShopV1InnerServiceSMO parkingCouponShopV1InnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
        Assert.hasKeyAndValue(reqJson, "shopId", "未包含商铺ID");
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        ParkingCouponShopDto parkingCouponShopDto = BeanConvertUtil.covertBean(reqJson, ParkingCouponShopDto.class);

        int count = parkingCouponShopV1InnerServiceSMOImpl.queryParkingCouponShopsCount(parkingCouponShopDto);

        List<ParkingCouponShopDto> parkingCouponShopDtos = null;

        if (count > 0) {
            parkingCouponShopDtos = parkingCouponShopV1InnerServiceSMOImpl.queryParkingCouponShops(parkingCouponShopDto);
        } else {
            parkingCouponShopDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reqJson.getInteger("row")), count, parkingCouponShopDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        cmdDataFlowContext.setResponseEntity(responseEntity);
    }
}
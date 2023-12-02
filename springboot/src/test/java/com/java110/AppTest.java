package com.java110;

import static org.junit.Assert.assertTrue;

import com.alibaba.fastjson.JSONObject;
import com.java110.dto.RoomAttrDto;
import com.java110.dto.community.CommunityDto;
import com.java110.utils.util.Base64Convert;
import com.java110.utils.util.DateUtil;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void should()
    {

        double money = 80;

        Map info =new HashMap<>();
        info.put("money",money);

        double a = Double.parseDouble(info.get("money").toString());
        System.out.println(a);
    }
}

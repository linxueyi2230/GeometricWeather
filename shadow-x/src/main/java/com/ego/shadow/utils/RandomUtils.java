package com.ego.shadow.utils;

import android.content.Context;
import android.util.Log;

import com.ego.shadow.Shadow;
import com.ego.shadow.db.DBHelper;

import java.util.Random;

public class RandomUtils {

    public static double reward(){

        String balance = DBHelper.with(Shadow.application).balance();
        if (Double.parseDouble(balance) >=80){
            Log.i("RandomReward", "随机生成奖励金：0.01");
            return 0.01;
        }

        StringBuilder log = new StringBuilder();
        log.append("随机生成奖励金：");

        Random random = new Random();

        int result = random.nextInt(30);
        if (result <= 0) {
            log.append("随机数 <=0 ,result：").append(result);
            result = 1;

        } else {
            log.append("随机数>0,result：").append(result);
        }

        double value = result / 100.00;

        log.append(" 奖励金额是：").append(value).append("元");

        Log.i("RandomReward", log.toString());

        return value;

    }

    public static void test(Context context) {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int type = random.nextInt(5);
            double reward = reward();
            DBHelper.with(context).reward(type, reward);
        }
    }

}

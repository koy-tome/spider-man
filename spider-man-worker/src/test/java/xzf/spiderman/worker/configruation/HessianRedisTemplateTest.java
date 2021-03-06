package xzf.spiderman.worker.configruation;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.junit4.SpringRunner;
import xzf.spiderman.worker.configuration.HessianRedisTemplate;
import xzf.spiderman.worker.entity.SpiderGroup;

import java.util.Arrays;
import java.util.concurrent.*;

import static xzf.spiderman.worker.configuration.WorkerConst.REDIS_RUNNING_SPIDER_GROUP_LOCK_PREFIX;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HessianRedisTemplateTest
{
    @Autowired
    private HessianRedisTemplate hessianRedisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void testCreate()
    {
        System.out.println(hessianRedisTemplate);
    }

    @Test
    public void setAndGetValue()
    {
        SpiderGroup spiderGroup = new SpiderGroup();
        spiderGroup.setId("test11222");
        spiderGroup.setName("hello");
        spiderGroup.setDesc("hahaha");

        ValueOperations<String ,Object> valueOperations= hessianRedisTemplate.opsForValue();

        valueOperations.set("test:spdiergorup", spiderGroup);

        SpiderGroup load =
                (SpiderGroup) valueOperations.get("test:spdiergorup");

        System.out.println("load="+ JSON.toJSONString(load));
    }

    @Test
    public void testRBPoll() throws Exception
    {
        System.out.println("before...");
        final Thread mainThread = Thread.currentThread();

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(8);
                mainThread.interrupt();
                System.out.println("other thread.. mianThread interrputed="+mainThread.isInterrupted());
            } catch (InterruptedException e) {
            }
        }).start();

        try {
            Object o = hessianRedisTemplate.opsForList().leftPop("test:spiderman.blockinglist", 1, TimeUnit.MINUTES);
            System.out.println("un-interrupted");
        }catch (Exception e )
        {
            System.out.println("mainThreadInterrput="+Thread.currentThread().isInterrupted());
            e.printStackTrace();
            // RedisCommandInterruptedException
            System.out.println(e.getMessage());
        }
        System.out.println("interrupted");
    }


    @Test
    public void testRBPollInThreadPool_1() throws Exception
    {
        System.out.println("before...");

        CopyOnWriteArrayList<Thread> list = new CopyOnWriteArrayList<>();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    Object o = hessianRedisTemplate.opsForList().leftPop("test:spiderman.blockinglist", 1, TimeUnit.MINUTES);
                    System.out.println("un-interrupted");
                }catch (Exception e){
                    System.out.println("interruped, exception="+e);
                    System.out.println(Thread.currentThread()+ "="+Thread.currentThread().isInterrupted());
                }
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                list.add(t);
                return t;
            }
        });
        executor.execute(task);

        TimeUnit.SECONDS.sleep(5L);

//        executor.shutdown();
//        System.out.println("invoke shutdown");

        executor.shutdownNow();
        System.out.println("invoke shutdownNow");


        TimeUnit.SECONDS.sleep(5L);

//        System.out.println("invoke list interrupted = "+list.size());
//
//        list.forEach(t->t.interrupt());
//
//        TimeUnit.SECONDS.sleep(10L);


    }

    @Test
    public void testSetIf() throws Exception
    {
        String key = "test:hello";

        boolean absent = hessianRedisTemplate.opsForValue().setIfAbsent(key,"0",3,TimeUnit.MINUTES);
        System.out.println("absent = " + absent);
        TimeUnit.MINUTES.sleep(1L);
        boolean fk = hessianRedisTemplate.opsForValue().setIfPresent(key,"fk", 1, TimeUnit.MINUTES);
        System.out.println(fk);
        System.out.println("present = " + fk);
    }

    @Test
    public void testLuaScript() throws Exception
    {
        hessianRedisTemplate.opsForValue().set(REDIS_RUNNING_SPIDER_GROUP_LOCK_PREFIX+"123","45678", 5,TimeUnit.MINUTES);


        String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
                "then " +
                "    return redis.call(\"del\",KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";

        Long ret = hessianRedisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class),
                Arrays.asList(REDIS_RUNNING_SPIDER_GROUP_LOCK_PREFIX + "123"), "45678");

        System.out.println("ret="+ret);


    }


}

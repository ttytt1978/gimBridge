package com.md.red5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Red5Session {

    public static Map<String, MyStreamListener> map = new ConcurrentHashMap<String, MyStreamListener>();
    public static List<IPusher> pusherList= Collections.synchronizedList(new ArrayList<IPusher>());
    static {
        map = new ConcurrentHashMap<String, MyStreamListener>();
        pusherList= Collections.synchronizedList(new ArrayList<IPusher>());
    }
}
